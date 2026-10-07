# Leçon 02 — Stream API : transformer des collections en une phrase

> 🧭 **Pont depuis la leçon 01** : vous savez maintenant *écrire* des actions (lambdas) et *choisir* leur type (`Predicate`, `Function`, `Consumer`…). Mais l'exercice 01 les utilisait encore **une par une**, avec une boucle `for` manuelle autour (`selectionner`). Cette leçon change d'échelle : un **Stream** est un **pipeline** (une chaîne) où chaque étape *reçoit* une lambda (`filter` prend un `Predicate`, `map` prend une `Function`) et où *vous ne gérez plus la boucle*. Leçon 01 = les **briques** ; leçon 02 = la **chaîne de montage** qui les assemble.

---

## 1. Objectifs d'apprentissage

À la fin de cette leçon, vous saurez :

- Expliquer **ce qu'est un Stream** (une vue de traitement, pas un stockage) et le distinguer d'une **collection**.
- Décrire le modèle **source → opérations intermédiaires → opération terminale**, et dire le rôle de chacun.
- Écrire un pipeline avec `filter`, `map`, `sorted`, `distinct`, `limit`, `skip` et `flatMap`.
- Terminer un pipeline avec `forEach`, `toList`, `count`, `reduce`, `findFirst`, `min`/`max`, `anyMatch`/`allMatch`.
- Expliquer la **paresse** (*lazy*) : rien ne se calcule sans opération terminale ; et l'**usage unique** : un Stream consommé ne se réutilise pas.
- Choisir entre `stream()` et `parallelStream()`, et utiliser `mapToInt`/`IntStream` pour les calculs numériques.

---

## 2. Explication simple

### 2.1 Qu'est-ce qu'un Stream ? (surtout : ce que ce N'EST PAS)

Un **`Stream`** (« flux », ou « courant ») est un objet qui représente **un traitement à appliquer** aux éléments d'une source (souvent une collection). Insistez sur le mot **traitement** : un Stream ne **stocke** rien.

| | **Collection** (`List`, `Set`…) | **Stream** |
|---|---|---|
| Rôle | **stocker** des données | **traiter** des données |
| Contenu | les éléments eux-mêmes | une **recette** d'opérations |
| Réutilisable | oui, à volonté | **non : usage unique** |
| Modifiable | oui (`add`, `remove`) | non (on produit un **nouveau** résultat) |

**Analogie** : une collection, c'est un **panier de pommes**. Un Stream, c'est le **tapis roulant** de l'usine : on y verse les pommes du panier, chaque poste fait son travail (laver = `filter`, éplucher = `map`, mettre en boîte = `collect`), et à la fin on récupère des **boîtes** — le panier d'origine est **intact**, le tapis ne sert **qu'une fois**.

**Pourquoi ce détour ?** Parce qu'il sépare le **quoi** du **comment**. Avec une boucle `for`, vous écrivez *comment* parcourir (l'index, la condition d'arrêt, l'accumulation). Avec un Stream, vous déclarez *quoi* faire (filtrer, transformer, agréger) ; **Java s'occupe du parcours**. Le code devient plus court, et surtout il **raconte l'intention** au lieu de la mécanique.

### 2.2 Le modèle en trois temps : source, intermédiaires, terminale

Tout pipeline Stream a **exactement** cette forme :

```text
source  ──►  opération(s) intermédiaire(s)  ──►  opération terminale
  │                       │                                │
une collection      transforment / filtrent           PRODUIT le résultat
(List, Set...)       (paresseuses : ne font            et CONSOMME le Stream
                      encore RIEN, voir 2.3)           (voir 2.4)
```

Exemple complet, qui se lit comme une phrase :

```java
List<String> quartiersUrgents = toutes.stream() // 1) SOURCE : le flux des réclamations
        .filter(r -> r.getPriorite() == Priorite.URGENTE) // 2) INTERMÉDIAIRE : ne garder que les urgentes
        .map(Reclamation::getQuartier) // 3) INTERMÉDIAIRE : transformer en quartier (String)
        .distinct() // 4) INTERMÉDIAIRE : enlever les doublons
        .toList(); // 5) TERMINALE : rassembler en List (et CONSOMMER le stream)
```

Lisez-le à voix haute : « *à partir de toutes, **filtre** les urgentes, **transforme** en quartier, **enlève** les doublons, **rassemble** en liste* ». Si votre pipeline **ne se lit pas comme une phrase**, il est probablement trop long ou mal découpé (voir bonnes pratiques).

### 2.3 Les opérations intermédiaires sont *paresseuses* (*lazy*)

**Paresseux** (*lazy*) veut dire : l'opération **ne fait rien** tant qu'aucune opération terminale n'a été appelée. `filter` et `map` ne font que **noter** l'instruction (« quand on me demandera le résultat, je filtrerai puis je transformerai »).

```java
Stream<Reclamation> paresseux = toutes.stream()
        .filter(r -> { // ce println NE S'AFFICHE PAS encore !
            System.out.println("filtrage de " + r.getId());
            return r.getPriorite() == Priorite.URGENTE;
        });
// ... rien ne s'est passé : aucune ligne affichée, aucun calcul.

List<Reclamation> resultat = paresseux.toList(); // TERMINALE : LÀ, tout se calcule d'un coup.
```

**Pourquoi la paresse est-elle une bonne idée ?** Pour **éviter le travail inutile**. Si vous enchaînez `filter(...).findFirst()`, Java s'arrête **dès le premier élément qui passe le filtre** (on appelle ça le **court-circuit**) : les éléments suivants ne sont **jamais examinés**. Avec une boucle classique qui filtre *toute* la liste avant de prendre le premier, vous auriez tout parcouru pour rien.

**Analogie** : c'est comme une **liste de courses avec un budget**. Tant que vous êtes chez vous (intermédiaires), vous ne faites que *prévoir*. Au magasin (terminale), la caissière s'arrête dès que le budget est atteint : inutile de scanner tout le caddie. La paresse + le court-circuit = on ne paie que ce qu'on consomme.

### 2.4 Les opérations terminales consomment : usage unique

Une opération terminale (`toList`, `forEach`, `count`, `reduce`, `findFirst`…) fait deux choses : elle **déclenche** le calcul, puis elle **ferme** le Stream. Un Stream consommé **ne peut plus servir** :

```java
Stream<Reclamation> flux = toutes.stream();
long n = flux.count(); // TERMINALE : le stream est maintenant CONSOMMÉ
List<Reclamation> liste = flux.toList(); // ❌ IllegalStateException: stream has already been operated upon or closed
```

**Pourquoi l'usage unique ?** Parce qu'un Stream est un **parcours en cours**, comme un **ticket de caisse qu'on déchire** : une fois utilisé, il n'existe plus. (Techniquement : les itérateurs internes sont épuisés.) Si vous avez besoin du « même » traitement deux fois, **recréez** le Stream (`toutes.stream()` coûte presque rien : c'est juste une nouvelle recette).

> 💡 **Note pour un développeur venant de JavaScript** : en JS, un tableau supporte `.filter().map()` **et reste réutilisable** — parce que chaque étape rend un **nouveau tableau** (un stockage). En Java, les étapes intermédiaires ne rendent **pas** de stockage mais un **nouveau Stream** (une nouvelle recette). D'où l'usage unique : pensez « tapis roulant », pas « tableau ».

### 2.5 Le catalogue des opérations (ce que chaque étape reçoit)

Chaque opération intermédiaire attend un **type précis** de lambda (rappelez-vous la leçon 01 : chaque lambda « colle » à une interface) :

| Opération | Ce qu'elle fait | Lambda reçue | Exemple |
|---|---|---|---|
| `filter` | **garde** les éléments qui passent le test | `Predicate` | `.filter(r -> r.getStatut() == NOUVELLE)` |
| `map` | **transforme** chaque élément (1 → 1) | `Function` | `.map(Reclamation::getQuartier)` |
| `flatMap` | **transforme puis aplati** (1 → 0..n, voir 2.6) | `Function` vers Stream | `.flatMap(q -> q.getRues().stream())` |
| `sorted` | **trie** (naturel ou via comparateur) | `Comparator` (optionnel) | `.sorted(comparing(Reclamation::getId))` |
| `distinct` | **enlève les doublons** (`equals`) | aucune | `.distinct()` |
| `limit(n)` | **garde** les n premiers | aucune | `.limit(3)` |
| `skip(n)` | **saute** les n premiers | aucune | `.skip(1)` |
| `peek` | **observe** sans transformer (débogage uniquement) | `Consumer` | `.peek(System.out::println)` |

Et les terminales les plus utiles :

| Opération | Ce qu'elle rend | Exemple |
|---|---|---|
| `toList()` | une `List` **immuable** du résultat | `.toList()` |
| `forEach` | **rien** : exécute une action par élément | `.forEach(System.out::println)` |
| `count` | le **nombre** d'éléments (`long`) | `.count()` |
| `reduce` | **agrège** tout en une valeur (voir 2.7) | `.reduce(0, (a, b) -> a + b)` |
| `findFirst` / `findAny` | un `Optional` du (premier / quelconque) élément | `.findFirst()` |
| `min` / `max` | un `Optional` du plus petit / grand (via comparateur) | `.min(comparing(...))` |
| `anyMatch` / `allMatch` / `noneMatch` | un `boolean` (au moins un / tous / aucun) | `.anyMatch(estUrgente)` |
| `collect` | un résultat via un **Collector** (leçon 03) | `.collect(toList())` |

**Comment choisir la terminale ?** Posez-vous la question du **résultat attendu** : une liste → `toList` ; un nombre → `count` ou `reduce` ; une question oui/non → `*Match` ; « le premier qui… » → `findFirst` ; un affichage → `forEach`. Le résultat **commande** la terminale.

### 2.6 `map` vs `flatMap` : transformer, ou transformer-puis-aplatir

- `map` transforme **1 élément en 1 élément** : `Reclamation` → son quartier (`String`).
- `flatMap` transforme **1 élément en 0..n éléments**, puis **aplatit** tout en un seul flux.

**Analogie** : `map`, c'est **traduire** chaque lettre (1 lettre → 1 traduction). `flatMap`, c'est **ouvrir** chaque enveloppe qui contient plusieurs lettres, et verser **toutes** les lettres sur la table en **un seul tas**. Sans `flatMap`, on aurait une « liste de listes » ; avec, une seule liste plate.

```java
// Chaque quartier a une LISTE de rues : on veut TOUTES les rues, à plat.
List<String> toutesLesRues = quartiers.stream() // Stream<Quartier>
        .flatMap(q -> q.getRues().stream()) // chaque quartier -> son Stream de rues, APLATI en un seul flux
        .distinct() // on enlève les doublons éventuels
        .toList(); // une seule List<String> plate
```

**Quand `flatMap` ?** Dès que votre `map` produirait un `Stream` ou une collection (une « liste de listes ») : c'est le signal. Sans lui, vous auriez `List<List<String>>` ; avec lui, `List<String>`.

### 2.7 `reduce` : agréger en une seule valeur

**Réduire** (*reduce*) veut dire **plier** tout le flux en **une seule valeur** : une somme, un maximum, une concaténation… On fournit une **valeur de départ** et une fonction qui **combine** l'accumulé avec l'élément courant.

```java
// Compter les caractères de toutes les descriptions : on part de 0, on additionne.
int totalCaracteres = toutes.stream()
        .map(r -> r.getDescription().length()) // Stream<Integer> : la longueur de chaque description
        .reduce(0, (accumule, longueur) -> accumule + longueur); // on PLIE : 0 + l1 + l2 + ...
```

Lisez `reduce(0, (acc, x) -> acc + x)` comme : « *pars de 0, puis pour chaque élément, ajoute-le à ce que tu as déjà* ». La fonction `(acc, x)` est un `BinaryOperator` (leçon 01 : deux entrées, une sortie de même type).

**Analogie** : c'est comme **plier une carte routière** : on part d'une grande surface (le flux), on replie étape par étape (la fonction de combinaison), jusqu'à un petit rectangle (la valeur unique).

### 2.8 `stream()` vs `parallelStream()`, et les streams numériques

- `toutes.stream()` : traitement **séquentiel** (un seul fil, dans l'ordre). **Le choix par défaut, toujours.**
- `toutes.parallelStream()` : le travail est **réparti sur plusieurs fils** (partie 10 : la concurrence). Tentant, mais :
  - sur de **petites** collections, le découpage **coûte plus cher** qu'il ne rapporte ;
  - avec des **effets de bord** (piège 3 de la leçon 01), le résultat devient **imprévisible**.

**Règle** : utilisez `parallelStream()` **uniquement après avoir mesuré** un vrai gain, sur de gros volumes et des lambdas **pures**. Sinon, `stream()`.

Pour les calculs numériques, préférez les **streams primitifs** (`IntStream`, `LongStream`, `DoubleStream`) : ils évitent le **boxing** et offrent des terminaux pratiques (`sum()`, `average()`, `summaryStatistics()`) :

```java
double moyenne = toutes.stream()
        .mapToInt(r -> r.getDescription().length()) // IntStream : des int, pas des Integer
        .average() // OptionalDouble : la moyenne (peut ne pas exister si le flux est vide !)
        .orElse(0.0); // valeur de repli (rappel Optional, partie 3)
```

> ⚠️ `average()` rend un `OptionalDouble` (et pas un `double` brut) : si la liste est **vide**, il n'y a **pas** de moyenne. Même honnêteté que `Optional` (partie 3) : l'absence est dans le type.

---

## 📖 Vocabulaire / Abréviations

> Les mots du pipeline, fixés avant les exemples : chacun réapparaîtra en section 3 puis dans la leçon 03.

| Terme | Définition en une ligne |
|---|---|
| **Stream** | Un objet qui représente un **traitement** (pas un stockage) appliqué aux éléments d'une source. |
| **Source** | Le point de départ du pipeline (souvent une collection via `.stream()`). |
| **Opération intermédiaire** | Une étape qui transforme/filtre (`filter`, `map`, `sorted`…) ; **paresseuse**, rend un nouveau Stream. |
| **Opération terminale** | L'étape qui **produit** le résultat (`toList`, `count`, `reduce`…) et **consomme** le Stream. |
| **Pipeline** | La chaîne complète source → intermédiaires → terminale, qui se lit comme une phrase. |
| **Paresseux (*lazy*)** | Rien ne se calcule avant l'opération terminale ; permet le **court-circuit**. |
| **Court-circuit** | L'arrêt anticipé dès que le résultat est connu (`findFirst`, `limit`, `anyMatch`…). |
| **Usage unique** | Un Stream consommé ne se réutilise pas (`IllegalStateException` sinon). |
| **`flatMap`** | Transforme 1 élément en 0..n éléments puis **aplatit** (évite les « listes de listes »). |
| **`reduce`** | **Plie** tout le flux en une seule valeur (valeur de départ + fonction de combinaison). |
| **`peek`** | Observe chaque élément sans le transformer (**débogage uniquement**). |
| **`IntStream` / `LongStream` / `DoubleStream`** | Streams de **primitifs** (pas d'objets) : pas de boxing, terminaux `sum()`/`average()`. |
| **`parallelStream()`** | Variante qui répartit le travail sur **plusieurs fils** (à n'utiliser qu'après mesure). |
| **`OptionalDouble` / `OptionalInt` / `OptionalLong`** | Les `Optional` des primitifs (rendus par `average()`, `min()`… sur streams numériques). |

---

## 3. Exemples concrets

> La section 2 a posé le modèle ; ici, on **fait tourner** des pipelines SignalCUA complets.

### 3.1 Le pipeline « phrase » : quartiers des urgentes, sans doublons

```java
// On part des 6 réclamations de démonstration (même jeu que l'exercice 01).
List<String> quartiersUrgents = toutes.stream() // 1) SOURCE : le flux des 6 réclamations
        .filter(r -> r.getPriorite() == Priorite.URGENTE) // 2) on ne garde que les urgentes (1 et 4)
        .map(Reclamation::getQuartier) // 3) on transforme en quartier : ["Medina", "Fann"]
        .distinct() // 4) on enlève les doublons éventuels
        .toList(); // 5) TERMINALE : on rassemble en liste
```

Comparez avec la version boucle (leçon 01, `selectionner` + accumulation manuelle) : le pipeline dit **la même chose en une phrase**, sans liste temporaire ni `if`.

### 3.2 Trier, paginer : `sorted`, `skip`, `limit`

```java
// Les 3 réclamations les plus récentes qui ne sont PAS résolues (une « page 1 » de 3 éléments).
List<Reclamation> page1 = toutes.stream()
        .filter(r -> r.getStatut() != StatutReclamation.RESOLUE) // on écarte la résolue (id 5)
        .sorted(Comparator.comparing(Reclamation::getDateDeclaration).reversed()) // plus récentes d'abord
        .limit(3) // on ne garde que les 3 premières -> [3, 4, 1]
        .toList();

// La « page 2 » : on saute les 3 premières, on prend les 3 suivantes.
List<Reclamation> page2 = toutes.stream()
        .filter(r -> r.getStatut() != StatutReclamation.RESOLUE)
        .sorted(Comparator.comparing(Reclamation::getDateDeclaration).reversed())
        .skip(3) // on SAUTE la page 1...
        .limit(3) // ... puis on prend la suivante -> [6, 2] (il n'en reste que 2)
        .toList();
```

**À remarquer** : `sorted` attend un `Comparator` — celui de la leçon 01 (`comparing(...).reversed()`) se **branche directement** dans le pipeline. Les deux leçons s'emboîtent.

### 3.3 Questions oui/non et recherche : `*Match`, `findFirst`, `min`

```java
// Y a-t-il AU MOINS une urgente non traitée ? (s'arrête dès la première trouvée : court-circuit)
boolean alerte = toutes.stream()
        .filter(r -> r.getStatut() != StatutReclamation.RESOLUE)
        .anyMatch(r -> r.getPriorite() == Priorite.URGENTE); // -> true (id 1 et 4)

// Tout est-il résolu ? (s'arrête dès le premier contre-exemple)
boolean toutResolu = toutes.stream()
        .allMatch(r -> r.getStatut() == StatutReclamation.RESOLUE); // -> false

// La réclamation la plus ancienne encore ouverte (min + Optional : peut ne pas exister !)
Optional<Reclamation> doyenne = toutes.stream()
        .filter(r -> r.getStatut() != StatutReclamation.RESOLUE)
        .min(Comparator.comparing(Reclamation::getDateDeclaration)); // -> Optional[id=2]
doyenne.ifPresent(System.out::println); // on affiche SEULEMENT si présente (rappel Optional, partie 3)
```

**À remarquer** : `findFirst`, `min`, `max` rendent un **`Optional`** (partie 3) : un flux filtré peut être **vide**, et Java l'avoue dans le type au lieu de rendre `null`.

### 3.4 Prouver la paresse : rien ne tourne sans terminale

```java
// On construit le pipeline SANS terminale : observez qu'AUCUN affichage ne se produit.
Stream<Reclamation> prevision = toutes.stream()
        .filter(r -> {
            System.out.println("examen de " + r.getId()); // trace : quand suis-je exécuté ?
            return r.getPriorite() == Priorite.URGENTE;
        });
// ... silence total : le filtre n'a examiné PERSONNE.

System.out.println("--- déclenchement ---");
long nombre = prevision.count(); // TERMINALE : LÀ, les 6 examens se produisent d'un coup.
System.out.println("urgentes = " + nombre); // -> urgentes = 2
```

Cet exemple est le **test mental** de la paresse : si vous voyez des « examen de … » **avant** le déclenchement, c'est que quelque chose a déjà consommé le stream (souvent un `peek` oublié ou un `forEach` intermédiaire).

### 3.5 Le numérique sans boxing : `mapToInt` et `summaryStatistics`

```java
// Statistiques des longueurs de description, en UNE passe, sans Integer intermédiaire.
IntSummaryStatistics stats = toutes.stream()
        .mapToInt(r -> r.getDescription().length()) // IntStream : des int primitifs
        .summaryStatistics(); // TERMINALE : compte, somme, min, moyenne, max — d'un coup
System.out.println("n=" + stats.getCount() + ", somme=" + stats.getSum()
        + ", min=" + stats.getMin() + ", moyenne=" + stats.getAverage() + ", max=" + stats.getMax());
```

`IntSummaryStatistics` (« statistiques résumées d'entiers ») est une petite classe qui accumule **compte, somme, min, moyenne, max** en un seul parcours : l'équivalent de 5 boucles en une phrase.

---

## 4. Bonnes pratiques modernes (2025-2026)

> Les exemples montraient le *comment* ; ici, le *comment bien*.

1. **Lisez votre pipeline comme une phrase.** S'il ne se lit pas (« stream filter map sorted limit collect » sans sens métier), extrayez des **variables intermédiaires nommées** (`Predicate<Reclamation> ouverte = ...`) ou découpez en deux pipelines.
2. **Limitez à 3-5 opérations.** Au-delà, c'est le « *stream spaghetti* » (anti-pattern de la roadmap, §5.2) : une boucle `for` claire vaut mieux qu'une chaîne de dix étapes. Ce n'est pas un aveu de faiblesse, c'est un choix de lisibilité.
3. **Préférez `toList()` / `collect` à `forEach` + mutation externe.** `forEach` qui remplit une liste est l'effet de bord déguisé (piège 3 de la leçon 01). La terminale **produit** le résultat : laissez-la faire.
4. **Utilisez les method references dans les étapes** (`Reclamation::getQuartier`, `System.out::println`) : le pipeline reste lisible verticalement.
5. **Restez en `stream()` séquentiel par défaut.** `parallelStream()` seulement après mesure, sur gros volumes et lambdas pures (erreur à éviter de la roadmap, §5.2).
6. **Calculez en `IntStream`/`LongStream`** (`mapToInt`, `mapToLong`) dès qu'il y a des nombres : pas de boxing, et des terminaux (`sum`, `average`, `summaryStatistics`) imbattables.
7. **`peek` = débogage provisoire uniquement.** Il ne transforme rien ; un `peek` qui modifie des éléments est un bug en puissance (surtout en parallèle). Retirez-le avant de commiter.

---

## 5. Pièges à éviter

> Chaque piège ci-dessous a été **vérifié** : le mauvais exemple lève une exception ou se comporte mal, le bon exemple fonctionne.

### Piège 1 — Réutiliser un Stream déjà consommé

```java
Stream<Reclamation> flux = toutes.stream();
long n = flux.count(); // TERMINALE : le stream est CONSOMMÉ
List<Reclamation> liste = flux.toList(); // ❌ MAUVAIS : IllegalStateException (déjà consommé)

// ✅ BON : on recrée le stream (une nouvelle « recette », coût quasi nul)
long n = toutes.stream().count();
List<Reclamation> liste = toutes.stream().toList();
```

**Pourquoi** : un Stream est un **parcours en cours**, pas un stockage (2.4). L'exception `IllegalStateException` (« état illégal ») dit exactement ça : « ce parcours est terminé ».

### Piège 2 — Oublier la terminale : le pipeline ne fait RIEN

```java
toutes.stream() // ❌ MAUVAIS : aucune terminale -> AUCUN calcul, AUCUN effet (silence total)
        .filter(r -> r.getPriorite() == Priorite.URGENTE)
        .map(Reclamation::getQuartier);

// ✅ BON : on termine (ici : rassembler en liste)
List<String> quartiers = toutes.stream()
        .filter(r -> r.getPriorite() == Priorite.URGENTE)
        .map(Reclamation::getQuartier)
        .toList();
```

**Pourquoi** : sans terminale, les intermédiaires sont **paresseux** (2.3) : ils attendent un déclenchement qui ne viendra jamais. C'est le bug **silencieux** le plus fréquent : tout compile, rien ne se passe.

### Piège 3 — `forEach` qui remplit une liste extérieure (effet de bord déguisé)

```java
List<String> quartiers = new ArrayList<>();
toutes.stream() // ❌ MAUVAIS (compile, mais c'est l'anti-pattern) : on remplit DEHORS
        .filter(r -> r.getPriorite() == Priorite.URGENTE)
        .forEach(r -> quartiers.add(r.getQuartier()));

// ✅ BON : la terminale PRODUIT le résultat — aucune liste externe
List<String> quartiers = toutes.stream()
        .filter(r -> r.getPriorite() == Priorite.URGENTE)
        .map(Reclamation::getQuartier)
        .toList();
```

**Pourquoi** : le premier code mélange deux mondes (le pipeline + la mutation externe) : illisible, et **faux en parallèle** (plusieurs fils ajouteraient en même temps dans la même liste). `toList()` existe précisément pour ça.

### Piège 4 — `peek` utilisé comme `map` ou comme action métier

```java
toutes.stream() // ❌ MAUVAIS : peek MODIFIE (ici : changement de statut caché dans une « observation »)
        .peek(Reclamation::demarrerTraitement) // effet de bord caché ! (transition NOUVELLE -> EN_COURS)
        .toList();

// ✅ BON : peek OBSERVE (débogage), map TRANSFORME, forEach AGIT — chacun son rôle
toutes.stream()
        .peek(r -> System.out.println("avant : " + r.getId())) // observation provisoire : OK
        .map(Reclamation::getQuartier) // transformation : map
        .forEach(System.out::println); // action finale : forEach
```

**Pourquoi** : `peek` (« jeter un œil ») est documenté comme **observation sans transformation**. L'utiliser pour modifier, c'est mentir au lecteur — et en `parallelStream`, l'ordre des `peek` devient imprévisible.
(Note : `demarrerTraitement()` est la méthode de transition `NOUVELLE` → `EN_COURS` du fil rouge, partie 2 : on la cite ici comme exemple d'action métier à ne **jamais** cacher dans un `peek`.)

### Piège 5 — `parallelStream()` par réflexe

```java
List<String> quartiers = toutes.parallelStream() // ❌ MAUVAIS par défaut : 6 éléments !
        .map(Reclamation::getQuartier)
        .toList(); // surcoût du découpage > gain ; et l'ordre n'est plus garanti sans tri explicite

// ✅ BON : séquentiel par défaut ; parallèle seulement mesuré, sur gros volumes, lambdas pures
List<String> quartiers = toutes.stream()
        .map(Reclamation::getQuartier)
        .toList();
```

**Pourquoi** : paralléliser, c'est **découper, distribuer, recoudre** — un coût fixe. Sur 6, 100 ou 1 000 éléments, ce coût dépasse le gain. Et sans `forEachOrdered` (la variante qui préserve l'ordre), l'ordre d'un `parallelStream` peut surprendre.

### Piège 6 — `map` sur un `Optional` au lieu de `flatMap` (les `Optional` vides deviennent des trous)

```java
// Chaque réclamation a un agent affecté... ou pas (Optional<String>).
List<Optional<String>> trous = toutes.stream() // ❌ MAUVAIS : une liste d'Optional !
        .map(Reclamation::getAgentAffecte) // Stream<Optional<String>> : les absents restent emballés
        .toList(); // [Optional[Amadou], Optional.empty, ...] : inexploitable tel quel

// ✅ BON : flatMap + Optional::stream (Java 9+) : les vides disparaissent, les présents s'aplatissent
List<String> agents = toutes.stream()
        .flatMap(r -> r.getAgentAffecte().stream()) // Optional.stream() : 0 ou 1 élément
        .toList(); // ["Amadou", ...] : plate et propre
```

**Pourquoi** : `Optional.stream()` (« le contenu comme un mini-flux : 0 ou 1 élément ») est fait pour ça. C'est le même réflexe que 2.6 : dès qu'une étape produit des « boîtes », `flatMap` les ouvre. (Note : `getAgentAffecte()` est un ajout d'exemple — `Optional<String>` vide = « pas encore d'agent », rappel partie 3.)

### Piège 7 — Modifier la source pendant le parcours

```java
List<Reclamation> vivantes = new ArrayList<>(toutes);
vivantes.stream() // ❌ MAUVAIS : on retire DE LA SOURCE pendant son propre parcours
        .filter(r -> r.getStatut() == StatutReclamation.RESOLUE)
        .forEach(vivantes::remove); // ConcurrentModificationException (ou comportement imprévisible)

// ✅ BON : on PRODUIT une nouvelle liste (la source reste intacte, le tapis roulant ne se mange pas lui-même)
List<Reclamation> nonResolues = toutes.stream()
        .filter(r -> r.getStatut() != StatutReclamation.RESOLUE)
        .toList();
```

**Pourquoi** : le Stream parcourt la source **pendant** que vous la modifiez : la `ConcurrentModificationException` (« modification concurrente » : la collection détecte qu'on la change en cours de parcours) est le garde-fou. Le style Stream dit : **ne mutez jamais la source, produisez un résultat**.

### Piège 8 — Le « stream spaghetti » : 10 opérations illisibles

```java
// ❌ MAUVAIS : tout en une phrase de 10 étapes — personne ne peut relire ça
var resultat = toutes.stream().filter(...).map(...).sorted(...).distinct().skip(...).limit(...).peek(...).map(...).filter(...).toList();

// ✅ BON : on nomme les étapes métier (variables intermédiaires) ou on découpe en 2 pipelines
Predicate<Reclamation> ouverte = r -> r.getStatut() != StatutReclamation.RESOLUE;
List<Reclamation> urgentesOuvertes = toutes.stream().filter(ouverte)
        .filter(r -> r.getPriorite() == Priorite.URGENTE)
        .sorted(Comparator.comparing(Reclamation::getDateDeclaration))
        .toList();
```

**Pourquoi** : passé 4-5 étapes, le pipeline ne se lit plus comme une phrase mais comme du bruit (anti-pattern de la roadmap, §5.2). Nommer (`ouverte`) ou découper **rend l'intention visible** — et chaque morceau devient testable.

---

## Checklist de validation

Avant de passer à l'exercice, vérifiez que vous savez faire **chacun** de ces points :

- [ ] Distinguer un **Stream** (traitement, usage unique) d'une **collection** (stockage, réutilisable).
- [ ] Découper un pipeline en **source → intermédiaires → terminale** et expliquer le rôle de chacun.
- [ ] Prédire ce qui s'exécute (et **quand**) grâce à la **paresse** ; citer le **court-circuit** (`findFirst`, `limit`, `anyMatch`).
- [ ] Écrire `filter`/`map`/`sorted`/`distinct`/`limit`/`skip` avec la bonne lambda (`Predicate`/`Function`/`Comparator`).
- [ ] Choisir `flatMap` quand une étape produit des « boîtes » (`Optional`, `List`, `Stream`).
- [ ] Terminer avec la bonne terminale selon le résultat voulu (`toList`, `count`, `reduce`, `findFirst`, `min`/`max`, `*Match`).
- [ ] Expliquer pourquoi un Stream consommé lève `IllegalStateException` si on le réutilise.
- [ ] Calculer en `IntStream` (`mapToInt`, `sum`, `average`, `summaryStatistics`) sans boxing.
- [ ] Justifier le choix `stream()` par défaut et dire quand (et seulement quand) envisager `parallelStream()`.

---

## 🔴 Fil rouge — ou en est SignalCUA ?

SignalCUA parle désormais **en pipelines** : requêtes de pilotage (`filter` + `map` + `distinct`), pagination (`sorted` + `skip` + `limit`), questions métier (`anyMatch`, `min` + `Optional`), statistiques numériques (`mapToInt` + `summaryStatistics`). Il reste à **ranger** les résultats en structures prêtes pour un rapport (comptes par statut, groupes par quartier) : c'est le rôle de la **leçon 03 — Collectors**, qui fournira la terminale « intelligente ».

---

➡️ **Prochaine étape** : la leçon 03 — **Collectors**. Vous savez *filtrer* et *transformer* ; maintenant on *range* : `toList`, `toMap`, `groupingBy`, `partitioningBy`, `joining`, `counting`. La terminale `collect(...)` remplacera partout les accumulations manuelles.

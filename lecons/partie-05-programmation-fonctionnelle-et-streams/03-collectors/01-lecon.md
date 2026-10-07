# Leçon 03 — Collectors : ranger les résultats en structures prêtes pour le rapport

> 🧭 **Pont depuis la leçon 02** : vous savez *filtrer* (`filter`), *transformer* (`map`) et *paginer* (`sorted` + `skip` + `limit`) — mais vos résultats sortaient toujours en **liste plate** (`toList`) ou en **nombre** (`count`). Or un tableau de bord ne montre pas des listes plates : il montre des **comptes par statut**, des **groupes par quartier**, un **index par identifiant**. Fabriquer ces structures à la main (une `Map` remplie dans une boucle) ramènerait les effets de bord que les Streams venaient d'éliminer. Les **Collectors** sont la terminale « intelligente » : ils **rangent** les éléments pendant que le pipeline les produit — sans boucle, sans mutation externe.

---

## 1. Objectifs d'apprentissage

À la fin de cette leçon, vous saurez :

- Expliquer **ce qu'est un `Collector`** (une « recette de rangement » passée à `collect`) et le distinguer d'une simple accumulation manuelle.
- Rassembler avec `toList`, `toSet`, `toCollection`, `joining`, `counting`, `summingInt`, `averagingInt`.
- Indexer avec **`toMap`** (clé + valeur) et gérer les **clés dupliquées** avec une fonction de fusion.
- Regrouper avec **`groupingBy`** (dont la variante à **collecteur en aval**) et bipartitionner avec **`partitioningBy`**.
- Combiner avec `mapping`, `filtering`, `flatMapping`, `minBy`/`maxBy`, `reducing`, `teeing` et `summarizingInt`.
- Choisir un collecteur **immuable** (`toUnmodifiableList`, `toUnmodifiableMap`…) quand le résultat ne doit plus bouger.

---

## 2. Explication simple

### 2.1 Qu'est-ce qu'un Collector ? (et pourquoi pas une boucle + une Map ?)

Un **`Collector`** (« collecteur ») est un objet qui décrit **comment ranger** les éléments d'un Stream : dans une liste, dans une Map indexée, groupés par critère, joints en texte… On le passe à la terminale **`collect(...)`** :

```java
// « collecte les quartiers des urgentes EN LISTE » : le Collector dit COMMENT ranger.
List<String> quartiers = toutes.stream()
        .filter(r -> r.getPriorite() == Priorite.URGENTE)
        .map(Reclamation::getQuartier)
        .collect(Collectors.toList()); // le Collector = la « recette de rangement »
```

**Pourquoi pas une boucle manuelle ?** Comparez :

```java
// ❌ Version boucle : vous gérez le contenant, le parcours, l'ajout — 3 responsabilités mélangées
Map<StatutReclamation, Long> comptes = new EnumMap<>(StatutReclamation.class);
for (Reclamation r : toutes) {
    comptes.merge(r.getStatut(), 1L, Long::sum); // merge = « ajoute 1, ou additionne si présent »
}

// ✅ Version Collector : vous déclarez le RANGEMENT, Java fait le reste
Map<StatutReclamation, Long> comptes = toutes.stream()
        .collect(Collectors.groupingBy(Reclamation::getStatut, Collectors.counting()));
```

La version Collector **raconte l'intention** (« groupe par statut en comptant ») au lieu de la mécanique (`merge`, `Long::sum`). Et elle est **sans effet de bord** : la Map est créée *par* le collecteur, pas mutée depuis l'extérieur.

**Analogie** : après le tapis roulant (leçon 02 : `filter`, `map`), il faut des **ouvriers de fin de chaîne** qui mettent en boîte, étiquettent, comptent. Le Collector, c'est la **fiche de poste** de cet ouvrier : « mets chaque pièce dans le bac de sa couleur et compte-les ». `collect(...)` embauche l'ouvrier ; la fiche (`groupingBy(...)`) dit quoi faire.

### 2.2 Les fondations : `toList`, `toSet`, `toCollection`, `joining`, `counting`

Ces collecteurs couvrent les rangements simples. Tous vivent dans la classe **`Collectors`** (avec un « s » : la **fabrique** de collecteurs, comme `Collections` est la fabrique d'utilitaires sur collections — rappel partie 3).

```java
List<String> liste = toutes.stream()
        .map(Reclamation::getQuartier)
        .collect(Collectors.toList()); // une List modifiable (ArrayList)

Set<String> uniques = toutes.stream()
        .map(Reclamation::getQuartier)
        .collect(Collectors.toSet()); // un Set : doublons éliminés (equals/hashCode, rappel partie 3)

TreeSet<String> tries = toutes.stream()
        .map(Reclamation::getQuartier)
        .collect(Collectors.toCollection(TreeSet::new)); // VOUS choisissez le contenant (ici : trié)
```

- `toCollection(Fournisseur)` prend un **`Supplier`** (leçon 01 !) qui fabrique le contenant vide : `TreeSet::new`, `LinkedHashSet::new`, `ArrayDeque::new`… C'est le pont entre les deux leçons : le Collector **reçoit** une lambda qui fabrique.
- `joining` colle des `String` en **un seul texte** (impossible avec `toList`) ; `counting` compte (équivalent à `count()`, mais **composable**, voir 2.5).

```java
String affiche = toutes.stream()
        .map(Reclamation::getQuartier)
        .distinct()
        .collect(Collectors.joining(", ", "[", "]")); // -> "[Medina, Plateau, Fann]"
// joining(délimiteur, préfixe, suffixe) : ici « , » entre, « [ » avant, « ] » après.

long n = toutes.stream()
        .filter(r -> r.getStatut() != StatutReclamation.RESOLUE)
        .collect(Collectors.counting()); // -> 5 (comme .count(), mais utilisable EN AVAL, voir 2.5)
```

**Quand `counting()` plutôt que `count()` ?** Quand le comptage est **une brique dans un plus grand rangement** (`groupingBy(..., counting())`) : une terminale `count()` termine le pipeline, un collecteur `counting()` se **combine**.

### 2.3 `toMap` : indexer (et la fonction de fusion qui sauve des doublons)

**Indexer** veut dire construire une **`Map` clé → valeur** pour retrouver un élément **sans parcourir** (rappel partie 3 : accès en O(1), temps constant). `toMap` prend **deux fonctions** : comment fabriquer la **clé**, comment fabriquer la **valeur**.

```java
// Index des réclamations par identifiant : id -> réclamation (recherche instantanée ensuite).
Map<Integer, Reclamation> parId = toutes.stream()
        .collect(Collectors.toMap(Reclamation::getId, r -> r)); // clé = l'id, valeur = elle-même
// r -> r (« l'identité ») s'écrit aussi Function.identity() : « la fonction qui rend son entrée ».
```

**Le piège central** (erreur à éviter de la roadmap, §5.3) : si **deux éléments produisent la même clé**, `toMap` **lève** `IllegalStateException` (« clés dupliquées »). Il faut alors une **troisième fonction** : la **fusion** (*merge function*), qui dit **laquelle garder** (ou comment combiner).

```java
// Dernière description signalée par quartier : en cas de doublon, on GARDE LA PLUS RÉCENTE.
Map<String, Reclamation> derniereParQuartier = toutes.stream()
        .collect(Collectors.toMap(
                Reclamation::getQuartier, // la CLÉ : le quartier
                r -> r, // la VALEUR : la réclamation elle-même
                (existante, candidate) -> // la FUSION : deux réclamations, même quartier — laquelle ?
                        candidate.getDateDeclaration().isAfter(existante.getDateDeclaration())
                                ? candidate // la candidate est plus récente : on la garde
                                : existante)); // sinon on garde l'existante
```

**Analogie** : `toMap`, c'est un **casier à étiquettes**. Si deux dossiers portent la **même étiquette**, le casier *de base* refuse (« doublon ! »). La fonction de fusion, c'est la **consigne affichée sur le casier** : « en cas de conflit, gardez le plus récent ». Sans consigne, pas de casier partagé.

**Quand `toMap` ?** Pour **retrouver** (`parId.get(3)`), **dédupliquer intelligemment** (fusion), ou **projeter** (id → description). Si le but est de *compter* ou *regrouper*, préférez `groupingBy` (2.4).

### 2.4 `groupingBy` et `partitioningBy` : ranger par critère

**Grouper** (*grouping*) veut dire répartir les éléments en **paquets étiquetés** : une `Map` dont la clé est le **critère** (le statut, le quartier…) et la valeur la **liste du paquet**.

```java
// Les réclamations rangées par statut : Map<StatutReclamation, List<Reclamation>>.
Map<StatutReclamation, List<Reclamation>> parStatut = toutes.stream()
        .collect(Collectors.groupingBy(Reclamation::getStatut));
// -> {NOUVELLE=[1, 3, 4], EN_COURS=[2, 6], RESOLUE=[5]}
```

**La variante puissante** : `groupingBy` accepte un **deuxième collecteur**, dit **en aval** (*downstream* = « plus bas dans le courant »), qui **transforme chaque paquet** au lieu de le laisser en liste brute :

```java
// Nombre de réclamations PAR STATUT : chaque paquet est COMPTÉ au lieu d'être listé.
Map<StatutReclamation, Long> comptesParStatut = toutes.stream()
        .collect(Collectors.groupingBy(Reclamation::getStatut, Collectors.counting()));
// -> {NOUVELLE=3, EN_COURS=2, RESOLUE=1}

// Plus ancienne réclamation PAR QUARTIER : chaque paquet est RÉDUIT à son minimum.
Map<String, Optional<Reclamation>> doyenneParQuartier = toutes.stream()
        .collect(Collectors.groupingBy(Reclamation::getQuartier,
                Collectors.minBy(Comparator.comparing(Reclamation::getDateDeclaration))));
// -> {Medina=Optional[1], Plateau=Optional[5], Fann=Optional[4]}
```

**Analogie** : `groupingBy` simple, c'est **trier le courrier par ville** (des paquets). Le collecteur en aval, c'est **ajouter une consigne par paquet** : « dans chaque paquet, *comptez* » (`counting`), « dans chaque paquet, *gardez la plus ancienne* » (`minBy`). Un seul passage, un rapport complet.

**Pourquoi `minBy` rend-il un `Optional` ?** Parce qu'un paquet **pourrait être vide** en théorie (jamais ici, mais le type reste honnête — même philosophie que `Optional`, partie 3).

**`partitioningBy`** est le petit frère : il coupe en **exactement deux paquets** selon un `Predicate` (`true` / `false`) :

```java
// Urgentes d'un côté, le reste de l'autre : Map<Boolean, List<Reclamation>>.
Map<Boolean, List<Reclamation>> parUrgence = toutes.stream()
        .collect(Collectors.partitioningBy(r -> r.getPriorite() == Priorite.URGENTE));
// -> {false=[2, 3, 5, 6], true=[1, 4]}
```

**Quand `partitioningBy` plutôt que `groupingBy` ?** Quand le critère est **binaire** (oui/non) : la Map a toujours **exactement 2 entrées** (`true` et `false`, même vides), ce qui simplifie le code qui lit le résultat (`parUrgence.get(true)` ne rend jamais `null`).

### 2.5 Les combinateurs : `mapping`, `filtering`, `flatMapping`, `reducing`, `teeing`

Les collecteurs **se combinent** comme des briques : un collecteur en aval peut lui-même contenir un autre collecteur. C'est ce qui rend `collect` plus puissant que n'importe quelle terminale simple.

```java
// Descriptions des urgentes, PAR STATUT, en une phrase : on groupe, puis dans chaque paquet
// on FILTRE (les urgentes) puis on TRANSFORME (en description).
Map<StatutReclamation, List<String>> descriptionsUrgentesParStatut = toutes.stream()
        .collect(Collectors.groupingBy(Reclamation::getStatut,
                Collectors.filtering(r -> r.getPriorite() == Priorite.URGENTE,
                        Collectors.mapping(Reclamation::getDescription, Collectors.toList()))));
// -> {NOUVELLE=[Nid de poule dangereux, Fuite d'eau], EN_COURS=[], RESOLUE=[]}
```

- `filtering(Predicate, aval)` : filtre **à l'intérieur** de chaque paquet (sans toucher aux autres paquets — contrairement à un `filter` en amont qui supprimerait des éléments du flux entier).
- `mapping(Function, aval)` : transforme les éléments **du paquet** avant de les ranger.
- `flatMapping` : comme `mapping`, mais la fonction produit 0..n éléments (le pendant collecteur de `flatMap`, leçon 02).
- `reducing` : plie un paquet en une valeur (le pendant collecteur de `reduce`, leçon 02).

Et `teeing` (« monter un T », comme un té de plomberie : **un flux, deux traitements, un seul résultat**) exécute **deux collecteurs** sur le même flux puis **fusionne** leurs résultats :

```java
// En UNE passe : la moyenne ET le max des longueurs de description.
record MoyenneEtMax(double moyenne, int max) {} // petit conteneur local (rappel record, partie 2)
MoyenneEtMax stats = toutes.stream()
        .map(Reclamation::getDescription)
        .collect(Collectors.teeing(
                Collectors.averagingInt(String::length), // traitement 1 : la moyenne
                Collectors.mapping(String::length, Collectors.maxBy(Integer::compare)), // traitement 2 : le max
                (moyenne, maxOpt) -> new MoyenneEtMax(moyenne, maxOpt.orElse(0)))); // FUSION des 2 résultats
```

**Quand `teeing` ?** Quand deux statistiques portent sur le **même** parcours et qu'on refuse de parcourir deux fois (gros volumes). Sinon, deux pipelines simples restent plus lisibles.

### 2.6 Immuable ou modifiable ? Choisir le bon contenant

Par défaut, `toList()` (Java 16+) rend une liste **immuable** (« qu'on ne peut pas modifier » : `add` lève `UnsupportedOperationException`), tandis que `Collectors.toList()` rend une `ArrayList` **modifiable**. Le tableau complet :

| Collecteur | Contenant | Modifiable ? | Quand ? |
|---|---|---|---|
| `Stream.toList()` | `List` (interne) | **non** | résultat final qu'on ne touche plus (**défaut recommandé**) |
| `Collectors.toList()` | `ArrayList` | oui | on va encore ajouter/retirer après |
| `Collectors.toUnmodifiableList()` | `List` | **non** (explicite) | comme `toList()`, mais en l'écrivant noir sur blanc |
| `Collectors.toSet()` / `toUnmodifiableSet()` | `HashSet` / set immuable | oui / non | mêmes règles, sans doublons |
| `Collectors.toMap(...)` | `HashMap` | oui | index qu'on enrichira |
| `Collectors.toUnmodifiableMap(...)` | `Map` | **non** | index figé (attention : **ni clés ni valeurs `null`**) |

**Règle 2025-2026** : exposez de l'**immuable** par défaut (le destinataire ne peut pas casser votre rapport par accident) ; ne rendez du modifiable que si l'appelant doit vraiment continuer à remplir. C'est l'esprit `List.copyOf` / `Map.copyOf` (partie 3) appliqué aux résultats de Streams.

> ⚠️ `toUnmodifiableMap` et `toMap` **refusent les valeurs `null`** (`NullPointerException`) : une Map « normale » (`HashMap`) les accepte, pas les versions immuables. Si vos données peuvent contenir `null`, filtrez avant (`filter(Objects::nonNull)`) ou gardez une `HashMap`.

---

## 📖 Vocabulaire / Abréviations

> Les mots du rangement, fixés avant les exemples : le rapport de la section 3 les utilisera tous.

| Terme | Définition en une ligne |
|---|---|
| **`Collector`** | Un objet « recette de rangement » passé à `collect(...)` (où et comment ranger chaque élément). |
| **`Collectors`** (avec s) | La **fabrique** de collecteurs (`toList`, `toMap`, `groupingBy`…) — à ne pas confondre avec `Collector`. |
| **`collect(...)`** | L'opération terminale qui **applique** un `Collector` au flux. |
| **Indexer** | Construire une `Map` clé → valeur pour **retrouver sans parcourir** (`toMap`). |
| **Fusion (*merge function*)** | La fonction qui tranche en cas de **clés dupliquées** dans `toMap` (laquelle garder / comment combiner). |
| **Grouper (*grouping*)** | Répartir en paquets étiquetés : `Map<Critère, List<Éléments>>` (`groupingBy`). |
| **En aval (*downstream*)** | Le collecteur appliqué **à l'intérieur de chaque paquet** (`groupingBy(critère, aval)`). |
| **Bipartitionner (*partitioning*)** | Couper en **exactement 2 paquets** selon un `Predicate` (`true` / `false`). |
| **`mapping` / `filtering` / `flatMapping`** | Les pendants « collecteur » de `map` / `filter` / `flatMap`, utilisables **en aval**. |
| **`reducing`** | Le pendant « collecteur » de `reduce` : plie un paquet en une valeur. |
| **`teeing`** | Exécute **2 collecteurs** sur le même flux puis **fusionne** leurs résultats (un seul parcours). |
| **Immuable** | Un résultat **non modifiable** (`add` lève `UnsupportedOperationException`) : le défaut sain à exposer. |
| **`Function.identity()`** | « La fonction qui rend son entrée » (`r -> r` écrit proprement). |
| **O(1)** | « Temps constant » : retrouver dans une `Map` coûte pareil quelle que soit sa taille (rappel partie 3). |

---

## 3. Exemples concrets

> La section 2 a expliqué les rangements ; ici, on **imprime le rapport SignalCUA** : comptes, groupes, index, texte.

### 3.1 Le rapport express : comptes par statut + texte des quartiers

```java
// Combien par statut ? (LE motif du fil rouge Étape 5 : groupingBy + counting)
Map<StatutReclamation, Long> comptes = toutes.stream()
        .collect(Collectors.groupingBy(Reclamation::getStatut, Collectors.counting()));
System.out.println(comptes); // -> {EN_COURS=2, NOUVELLE=3, RESOLUE=1} (ordre d'affichage non garanti !)

// Les quartiers touchés, en une ligne lisible (joining)
String ligne = toutes.stream()
        .map(Reclamation::getQuartier)
        .distinct()
        .sorted() // on trie pour un affichage STABLE (sinon l'ordre dépend du flux)
        .collect(Collectors.joining(", ", "Quartiers : ", "."));
System.out.println(ligne); // -> Quartiers : Fann, Medina, Plateau.
```

> ⚠️ **L'ordre d'une `Map` n'est pas garanti** : `{EN_COURS=2, NOUVELLE=3, RESOLUE=1}` peut s'afficher dans un autre ordre selon l'exécution. Pour un rapport **stable**, ajoutez un fournisseur de Map triée : `groupingBy(critère, TreeMap::new, aval)` (`TreeMap` = la Map triée par clés, rappel partie 3). L'exercice l'exigera ; la correction montrera les deux versions.

### 3.2 L'index et le groupe : `toMap` avec fusion + doyenne par quartier

```java
// Index id -> réclamation (recherche instantanée : parId.get(3))
Map<Integer, Reclamation> parId = toutes.stream()
        .collect(Collectors.toMap(Reclamation::getId, Function.identity()));

// Dernière réclamation PAR QUARTIER (fusion : en cas de doublon, la plus récente gagne)
Map<String, Reclamation> derniereParQuartier = toutes.stream()
        .collect(Collectors.toMap(Reclamation::getQuartier, r -> r,
                (existante, candidate) -> candidate.getDateDeclaration()
                        .isAfter(existante.getDateDeclaration()) ? candidate : existante));
// -> {Medina=id 3 (03/10), Plateau=id 2 (28/09), Fann=id 4 (02/10)}

// Plus ancienne PAR QUARTIER (groupingBy + minBy en aval : le motif du fil rouge Étape 5)
Map<String, Optional<Reclamation>> doyenneParQuartier = toutes.stream()
        .collect(Collectors.groupingBy(Reclamation::getQuartier,
                Collectors.minBy(Comparator.comparing(Reclamation::getDateDeclaration))));
// -> {Medina=Optional[6] (30/09), Plateau=Optional[5] (20/09), Fann=Optional[4] (02/10)}
```

**À remarquer** : `toMap` + fusion et `groupingBy` + `minBy` répondent à des questions **miroirs** (« la plus récente » vs « la plus ancienne ») avec des outils **complémentaires** : la fusion tranche *pendant* le remplissage, `minBy` réduit *après* le paquet. Les deux sont au programme de l'exercice.

### 3.3 Couper en deux : `partitioningBy` urgent / non-urgent

```java
// Deux paquets garantis : true (urgentes) et false (les autres), même si l'un est vide.
Map<Boolean, List<Reclamation>> parUrgence = toutes.stream()
        .collect(Collectors.partitioningBy(r -> r.getPriorite() == Priorite.URGENTE));
System.out.println("urgentes : " + parUrgence.get(true).size()); // -> urgentes : 2
System.out.println("autres : " + parUrgence.get(false).size()); // -> autres : 4
```

**À remarquer** : `parUrgence.get(true)` ne rend **jamais** `null` (le paquet existe toujours, fût-il vide) : c'est l'avantage sur un `groupingBy` à clé `Boolean`, où une clé absente rendrait `null` et obligerait à un `getOrDefault` (rappel partie 3).

---

## 4. Bonnes pratiques modernes (2025-2026)

> Les exemples montraient le *comment* ; ici, le *comment bien*.

1. **Passez toujours une fonction de fusion à `toMap`** dès que les clés *peuvent* se répéter — même si « en pratique, les id sont uniques ». Le jour où un doublon arrive, vous aurez un comportement **choisi** au lieu d'une `IllegalStateException` en production. (Erreur à éviter de la roadmap, §5.3.)
2. **Utilisez `groupingBy` + aval pour les rapports** : c'est l'outil n°1 pour transformer du « plat » (liste, lignes de base — partie 8) en structures de tableau de bord (conseil de pro de la roadmap, §5.3).
3. **Stabilisez l'ordre des rapports** avec `groupingBy(critère, TreeMap::new, aval)` ou `toMap(..., LinkedHashMap::new, ...)` quand l'affichage doit être **reproductible** (tests, exports) : une `HashMap` n'offre aucune garantie d'ordre.
4. **Exposez de l'immuable** (`Stream.toList()`, `toUnmodifiableMap`) pour les résultats finaux : le destinataire ne peut pas corrompre votre rapport par accident.
5. **Préférez `partitioningBy` au `groupingBy` booléen** : 2 paquets garantis, pas de `null`, lecture directe avec `.get(true)` / `.get(false)`.
6. **Nommez les collecteurs complexes** : un `groupingBy` à double aval imbriqué mérite une **variable intermédiaire nommée** (`Collector<...> comptesParStatut = ...`) ou une **méthode privée** — sinon c'est le « collector spaghetti », cousin du « stream spaghetti ».
7. **Ne collectez que ce dont vous avez besoin** : `mapping`/`filtering` en aval évitent de construire des paquets complets pour n'en lire qu'un champ. Moins d'objets = moins de mémoire.

---

## 5. Pièges à éviter

> Chaque piège ci-dessous a été **vérifié** : le mauvais exemple lève une exception, le bon exemple fonctionne.

### Piège 1 — `toMap` sans fusion sur clés dupliquées

```java
Map<String, Reclamation> parQuartier = toutes.stream() // ❌ MAUVAIS : 3 réclamations « Medina » !
        .collect(Collectors.toMap(Reclamation::getQuartier, r -> r)); // IllegalStateException: Duplicate key Medina

// ✅ BON : on donne la consigne de fusion (ici : garder la plus récente)
Map<String, Reclamation> parQuartier = toutes.stream()
        .collect(Collectors.toMap(Reclamation::getQuartier, r -> r,
                (existante, candidate) -> candidate.getDateDeclaration()
                        .isAfter(existante.getDateDeclaration()) ? candidate : existante));
```

**Pourquoi** : `toMap` à 2 arguments **refuse** les doublons par défaut (choix sain : mieux vaut une erreur franche qu'un écrasement silencieux). La fusion à 3 arguments transforme l'erreur en **règle métier**.

### Piège 2 — `toMap` / `toUnmodifiableMap` avec une valeur `null`

```java
Map<Integer, String> descriptions = toutes.stream() // ❌ MAUVAIS si une description est null
        .collect(Collectors.toMap(Reclamation::getId, Reclamation::getDescription)); // NullPointerException !

// ✅ BON : on filtre les null AVANT (Objects::nonNull = « garder les non-nuls »)
Map<Integer, String> descriptions = toutes.stream()
        .filter(r -> r.getDescription() != null) // on écarte les descriptions absentes
        .collect(Collectors.toMap(Reclamation::getId, Reclamation::getDescription));
```

**Pourquoi** : les Maps produites par `toMap`/`toUnmodifiableMap` **interdisent** les clés et valeurs `null` (contrairement à une `HashMap` remplie à la main). `Objects::nonNull` est la method reference du test « non nul » (`Objects` = classe utilitaire d'objets, `java.util`).

### Piège 3 — `groupingBy` sur un critère `null`

```java
Map<String, List<Reclamation>> parQuartier = toutes.stream() // ❌ MAUVAIS si un quartier est null
        .collect(Collectors.groupingBy(Reclamation::getQuartier)); // NullPointerException !

// ✅ BON : on écarte (ou on remplace) les critères nuls AVANT de grouper
Map<String, List<Reclamation>> parQuartier = toutes.stream()
        .filter(r -> r.getQuartier() != null) // on écarte les quartiers absents
        .collect(Collectors.groupingBy(Reclamation::getQuartier));
```

**Pourquoi** : `groupingBy` utilise le critère comme **clé de Map**, et sa Map interne refuse les clés `null`. Même garde-fou que le piège 2 : **assainir avant de ranger**.

### Piège 4 — Croire que `toList()` rend une liste modifiable

```java
List<String> quartiers = toutes.stream()
        .map(Reclamation::getQuartier)
        .toList(); // liste IMMUABLE (Java 16+)
quartiers.add("Ouakam"); // ❌ MAUVAIS : UnsupportedOperationException (« opération non supportée ») !

// ✅ BON : si l'on doit encore remplir après, on choisit explicitement le modifiable
List<String> quartiers = toutes.stream()
        .map(Reclamation::getQuartier)
        .collect(Collectors.toList()); // ArrayList modifiable : add autorisé
```

**Pourquoi** : `Stream.toList()` (sans `Collectors`) rend une liste **immuable** par conception (2.6) : c'est le choix sain par défaut, mais il faut le **savoir** quand on veut encore modifier après. L'exception `UnsupportedOperationException` dit exactement : « cette liste ne supporte pas l'ajout ».

### Piège 5 — Sommer des `int` qui débordent (`summingInt`)

```java
int total = lignes.stream() // ❌ MAUVAIS si les montants cumulés dépassent ~2 milliards
        .collect(Collectors.summingInt(Ligne::getMontant)); // int + int = int : DÉBORDEMENT silencieux !

// ✅ BON : on somme en long (summingLong) dès que les montants sont métier (factures, budgets...)
long total = lignes.stream()
        .collect(Collectors.summingLong(Ligne::getMontant)); // long : ~9 milliards de milliards, tranquille
```

**Pourquoi** : `summingInt` additionne en `int` (max ≈ 2,1 milliards, rappel partie 1) : au-delà, le total **reboucle en négatif sans erreur** (le débordement entier est silencieux en Java). Pour de l'argent ou des volumes, `summingLong` (voire `BigDecimal`, partie 13) est la règle.

### Piège 6 — Comparer des `Map` ou exiger un ordre sans `TreeMap`

```java
Map<StatutReclamation, Long> comptes = toutes.stream()
        .collect(Collectors.groupingBy(Reclamation::getStatut, Collectors.counting()));
// ❌ MAUVAIS en test/export : l'ordre d'affichage VARIE (HashMap) — le test devient « flaky » (instable)

// ✅ BON : on impose une Map triée (fournisseur en 2e position) pour un rapport REPRODUCTIBLE
Map<StatutReclamation, Long> comptes = toutes.stream()
        .collect(Collectors.groupingBy(Reclamation::getStatut, TreeMap::new, Collectors.counting()));
// -> {NOUVELLE=3, EN_COURS=2, RESOLUE=1} TOUJOURS dans cet ordre (l'enum suit son ordre de déclaration)
```

**Pourquoi** : la forme à 3 arguments de `groupingBy` (critère, **fournisseur de Map**, aval) contrôle le contenant : `TreeMap::new` trie par clés (ici l'`enum`, dans son ordre de déclaration), `LinkedHashMap::new` garde l'ordre d'insertion. Un rapport **reproductible** se teste et s'exporte ; un rapport à ordre aléatoire, non.

### Piège 7 — Le collecteur « oignon » : 3 niveaux d'imbrication illisibles

```java
// ❌ MAUVAIS : groupingBy(filtering(mapping(...))) en une ligne — qui fait quoi, et dans quel ordre ?
var rapport = toutes.stream().collect(groupingBy(Reclamation::getStatut,
        filtering(r -> r.getPriorite() == Priorite.URGENTE,
                mapping(Reclamation::getDescription, toList()))));

// ✅ BON : on nomme l'aval (une variable = une intention lisible)
Collector<Reclamation, ?, List<String>> descriptionsUrgentes =
        Collectors.filtering(r -> r.getPriorite() == Priorite.URGENTE,
                Collectors.mapping(Reclamation::getDescription, Collectors.toList()));
Map<StatutReclamation, List<String>> rapport = toutes.stream()
        .collect(Collectors.groupingBy(Reclamation::getStatut, descriptionsUrgentes));
```

**Pourquoi** : `Collector<T, ?, R>` (« prend des T, rend des R », le `?` = détail interne qu'on ignore) nommé `descriptionsUrgentes` **raconte** ce que fait l'aval. Passé 2 niveaux d'imbrication, nommer n'est pas du luxe : c'est ce qui rend le code **relisible dans 6 mois**.

### Piège 8 — `joining` sur des non-`String` sans `map` préalable

```java
List<Integer> ids = List.of(1, 2, 3);
String s = ids.stream().collect(Collectors.joining(", ")); // ❌ MAUVAIS : NE COMPILE PAS
// joining ne sait coller que des String : « incompatible types: Integer cannot be converted to CharSequence »

// ✅ BON : on transforme D'ABORD en String (map), puis on colle
String s = ids.stream().map(String::valueOf).collect(Collectors.joining(", ")); // -> "1, 2, 3"
// (String::valueOf = « convertir en texte » : la method reference de la conversion universelle)
```

**Pourquoi** : `joining` travaille sur des `CharSequence` (« séquences de caractères » : `String`, `StringBuilder`…) : il ne convertit **rien** tout seul. La `map` de conversion est obligatoire — et explicite, donc lisible.

---

## Checklist de validation

Avant de passer à l'exercice, vérifiez que vous savez faire **chacun** de ces points :

- [ ] Expliquer ce qu'est un `Collector` et pourquoi il remplace l'accumulation manuelle (pas d'effet de bord).
- [ ] Rassembler avec `toList`/`toSet`/`toCollection`, coller avec `joining`, compter avec `counting`.
- [ ] Indexer avec `toMap` (clé + valeur) et écrire une **fonction de fusion** pour les doublons.
- [ ] Grouper avec `groupingBy` simple, puis avec **aval** (`counting`, `minBy`/`maxBy`, `mapping`).
- [ ] Couper en deux avec `partitioningBy` et dire pourquoi `.get(true)` ne rend jamais `null`.
- [ ] Combiner `filtering`/`mapping` en aval, et expliquer `teeing` en une phrase.
- [ ] Choisir entre modifiable et **immuable** (`toList` vs `Collectors.toList`, `toUnmodifiableMap`).
- [ ] Stabiliser un rapport avec `TreeMap::new` et dire pourquoi l'ordre `HashMap` varie.
- [ ] Expliquer pourquoi `toMap` refuse les valeurs `null` et comment assainir avant.

---

## 🔴 Fil rouge — ou en est SignalCUA ?

SignalCUA imprime désormais un **vrai rapport** : comptes par statut (`groupingBy` + `counting`), doyenne par quartier (`groupingBy` + `minBy`), index par id (`toMap`), quartiers en une ligne (`joining`), coupe urgent/non-urgent (`partitioningBy`). C'est l'**Étape 5 — statistiques SignalCUA** (voir `lecons/fil-rouge-signalcua.md`). La **phase console** (parties 1-5) est terminée : le projet sait représenter, échouer proprement et traiter ses données.

---

➡️ **Prochaine étape** : la partie 5 est terminée. La **partie 6 — Build Tools (Maven / Gradle)** change de dimension : fini le code « à la main » dans un dossier, on structure SignalCUA en **vrai projet buildable** (dépendances, arborescence standard, compilation reproductible) — le socle sans lequel Spring Boot (partie 7) serait inutilisable.
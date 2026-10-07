# Leçon 01 — Lambdas et interfaces fonctionnelles : décrire une action plutôt qu'un objet

> 🧭 **Pont depuis la partie 4** : à la fin de la partie 3, vous saviez **stocker** et **retrouver** des données (collections, `Optional`). La partie 4 vous a appris à **échouer proprement** (exceptions métier). Il manque encore une brique : **passer du comportement en paramètre**. Jusqu'ici, quand vous écriviez `triParStatut(liste)`, la méthode décidait elle-même comment comparer ; impossible de dire « trie selon *ce* critère que je te donne au moment de l'appel ». Les **lambdas** résolvent exactement ce problème : elles permettent de donner **une action** (une petite fonction) à une méthode, comme on donne aujourd'hui une **valeur**. Cette leçon prépare directement la suite de la partie 5 : les *Streams* ne sont, au fond, qu'une chaîne d'actions passées à `filter`, `map`, `sorted`…

---

## 1. Objectifs d'apprentissage

À la fin de cette leçon, vous saurez :

- Expliquer **ce qu'est une interface fonctionnelle** (une interface à **une seule méthode abstraite**, dite *SAM*) et **pourquoi** une lambda peut la remplacer.
- Écrire une **lambda** dans sa forme complète puis dans sa forme abrégée, et comprendre chaque morceau (`->`, paramètres, corps).
- Reconnaître et utiliser les **4 interfaces fonctionnelles standard** : `Function`, `Predicate`, `Consumer`, `Supplier` (et savoir quand en utiliser d'autres : `BiFunction`, `UnaryOperator`, `BinaryOperator`).
- Écrire une **method reference** (`Classe::methode`) et distinguer ses **4 formes**.
- Comprendre la règle de la variable **« effectively final »** capturée par une lambda, et **pourquoi** Java l'impose.
- Composer un **`Comparator`** avec `Comparator.comparing(...)` — votre premier vrai usage professionnel d'une lambda.

---

## 2. Explication simple

### 2.1 Le point de départ : passer du *comportement* en paramètre

Une méthode, en Java, reçoit des **valeurs** en paramètres : `ajouter(Reclamation r)`, `compter(int max)`. Mais comment passer une **action** ? Par exemple : « affiche-moi chaque réclamation selon *la façon dont je veux l'afficher* ».

**Pourquoi ce besoin existe-t-il ?** Parce qu'un même traitement sert souvent avec une variation minuscule. Trier une liste peut se faire par identifiant, par quartier ou par date : le **cadre** (l'algorithme de tri) est identique, seule la **règle de comparaison** change. Dupliquer l'algorithme trois fois serait absurde.

**Analogie** : imaginez un **grille-pain**. Le grille-pain (l'algorithme) fait toujours la même chose, mais vous pouvez lui passer **différentes tranches de pain** (les données). Une lambda, c'est pouvoir lui passer aussi une **consigne** : « grille fort », « grille juste un peu ». On paramètre **le comportement**, pas seulement la donnée.

**Comment y arrive-t-on en Java ?** Une méthode ne peut pas recevoir « du code » directement. Mais elle peut recevoir un **objet** qui contient une méthode. Donc *passer une action* = *passer un objet dont une méthode réalise cette action*. C'est là qu'interviennent les interfaces fonctionnelles.

### 2.2 De la classe anonyme à la lambda : le déclic

Vous connaissez déjà les **interfaces** (partie 2, leçon 03). Rappel : une interface est un **contrat** — une liste de méthodes qu'une classe s'engage à implémenter.

Regardons comment on « passait une action » **avant** les lambdas (Java 7 et avant). Imaginons une interface `Salueur` avec une seule méthode `saluer(String nom)` :

```java
// Une interface à UNE seule méthode : c'est ce qu'on appelle une interface fonctionnelle.
interface Salueur {
    void saluer(String nom);
}

// Avant Java 8 : on créait une CLASSE ANONYME (une classe déclarée et instanciée d'un coup, sans nom).
Salueur bonjour = new Salueur() {
    @Override
    public void saluer(String nom) {
        System.out.println("Bonjour " + nom); // le corps utile est ICI...
    }
};
bonjour.saluer("Amadou"); // -> Bonjour Amadou
```

Observez toute la « cérémonie » (`new Salueur() {`, `@Override`, `public void saluer(...) {`, `}`) pour n'écrire qu'**une seule ligne utile**. Les lambdas suppriment cette cérémonie :

```java
// La MÊME chose, avec une lambda : on ne garde que l'essentiel (le paramètre -> le corps).
Salueur bonjour = nom -> System.out.println("Bonjour " + nom);
bonjour.saluer("Amadou"); // -> Bonjour Amadou
```

**Ce qui a changé** : le compilateur **sait déjà** que `Salueur` n'a qu'une méthode, `saluer`. Il devine donc le nom de la méthode, son type de retour, son `@Override`, le `new`… Il ne reste que **les paramètres** et **le corps**. Une lambda n'est **pas** une méthode « libre » : c'est **la réalisation d'une interface fonctionnelle**, écrite de façon condensée.

> 💡 **Note pour un développeur venant de JavaScript/TypeScript** : une lambda Java est très proche d'une **arrow function** (`nom => console.log("Bonjour " + nom)`). La grande différence : en Java, une lambda **doit** correspondre à une **interface qui existe** (elle ne « flotte » pas toute seule) — le compilateur rattache la lambda à un type précis. Vous verrez comment (2.4).

### 2.3 Anatomie d'une lambda

Une lambda a **trois parties** :

```text
   (paramètres)        ->          corps
   ────────────        ──          ─────
   (a, b)              ->          a + b
```

- **Les paramètres** : entre parenthèses, comme une méthode. Les types sont **facultatifs** (souvent devinés) ; s'il n'y a **qu'un seul** paramètre, on peut **omettre les parenthèses**.
- **La flèche `->`** (le « *arrow* ») : sépare paramètres et corps. Elle se lit « donne » : `a -> a + 1` se lit « à partir de `a`, donne `a + 1` ».
- **Le corps** : soit une **expression** (`a + b`), soit un **bloc** `{ ... }` avec `return` explicite.

```java
// 1) Plusieurs paramètres, corps-expression (pas de return : la valeur EST le résultat)
Function<Integer, Integer> carre = x -> x * x;

// 2) Corps-bloc : accolades + return obligatoire
Function<Integer, Integer> carre2 = x -> {
    int resultat = x * x;
    return resultat;
};

// 3) Zéro paramètre : parenthèses vides obligatoires
Supplier<String> maintenant = () -> "il est l'heure";

// 4) Un paramètre sans parenthèses (autorisé)
Predicate<String> vide = s -> s.isEmpty();
```

**Quand une lambda, quand un bloc ?** Un corps-expression (`x -> x + 1`) est plus lisible quand c'est court. Dès qu'il y a plusieurs instructions, utilisez un bloc (`{ ... }`) : c'est plus clair.

### 2.4 Interface fonctionnelle = une seule méthode (SAM)

Une **interface fonctionnelle** est une interface qui possède **exactement une méthode abstraite**. On l'appelle aussi **SAM** (*Single Abstract Method* = « une seule méthode abstraite »).

**Pourquoi cette contrainte ?** Parce que **la lambda ne dit pas quelle méthode elle réalise**. Le compilateur la déduit : s'il n'y a **qu'une** méthode, il n'y a **aucun doute**. Si l'interface en avait plusieurs, il ne saurait pas à laquelle rattacher `->`.

```java
@FunctionalInterface // (facultatif mais recommandé) : demande au compilateur de VÉRIFIER la contrainte
interface Calcul {
    int appliquer(int a, int b); // UNE seule méthode abstraite -> la lambda réalisera celle-ci
}
```

- L'annotation **`@FunctionalInterface`** n'est pas obligatoire, mais elle **protège** : si quelqu'un ajoute plus tard une deuxième méthode abstraite, le compilateur le signale tout de suite. Bonne pratique : **mettez-la toujours**.
- Une interface fonctionnelle peut contenir des méthodes `default` ou `static` (partie 2) : elles ont un corps et **ne comptent pas** dans la contrainte SAM. Seules les méthodes **abstraites** comptent.

> ⚠️ **D'où vient le mot « fonctionnel » ?** Pas de panique : cela **ne veut pas dire** que Java devient un langage « fonctionnel » pur. Cela désigne juste le fait de **manipuler des fonctions comme des valeurs** (les passer, les retourner). Java reste un langage orienté objet ; les lambdas sont une **boîte à outils** posée dessus.

### 2.5 Les 4 interfaces fonctionnelles standard (celles à connaître)

Java fournit déjà les interfaces fonctionnelles dont on a besoin **99 % du temps**, dans le paquet `java.util.function`. Les quatre essentielles :

| Interface | Rôle | Entrée | Sortie | Exemple de lambda |
|---|---|---|---|---|
| **`Function<T, R>`** | *transforme* une valeur | `T` | `R` | `String::length` (`String` → `int`) |
| **`Predicate<T>`** | *teste* une condition | `T` | `boolean` | `s -> s.isEmpty()` |
| **`Consumer<T>`** | *consomme* (agit, sans rendre de valeur) | `T` | rien (`void`) | `System.out::println` |
| **`Supplier<T>`** | *fournit* une valeur, sans entrée | rien | `T` | `() -> new ArrayList<>()` |

**Analogie** : ce sont quatre **métiers**.
- `Function` = un **traducteur** (entre `T` et `R`).
- `Predicate` = un **portier** qui dit oui ou non (`boolean`).
- `Consumer` = un **consommateur** qui reçoit et agit (ne rend rien).
- `Supplier` = un **distributeur** qui donne quelque chose sans rien demander.

Quelques **variantes** utiles (3 à connaître de nom) :

- `BiFunction<T, U, R>` : **deux** entrées, une sortie (ex : `(a, b) -> a + b`).
- `UnaryOperator<T>` : une entrée, une sortie de **même** type (ex : `x -> x + 1`).
- `BinaryOperator<T>` : deux entrées, une sortie de même type (ex : `(a, b) -> Math.max(a, b)`).

**Pourquoi les standard plutôt que vos propres interfaces ?** Pour que **tout le monde parle la même langue**. Quand une méthode attend un `Predicate<Reclamation>`, n'importe quel développeur sait immédiatement ce que c'est. Réservez une interface custom aux cas **métier** (un nom qui raconte l'intention, comme on le verra dans l'exercice).

### 2.6 Les method references : la forme la plus courte

Quand une lambda ne fait que **déléguer** à une méthode existante, on peut encore simplifier avec une **method reference** (« référence de méthode »), écrite avec un double deux-points `::`.

```java
Reclamation r = ...;

// Lambda : on appelle une méthode existante avec le paramètre reçu
Function<Reclamation, String> quartierParLambda = rec -> rec.getQuartier();

// Method reference : équivalent, en plus court (on « cite » juste la méthode)
Function<Reclamation, String> quartierParRef = Reclamation::getQuartier;

// De même pour Consumer : afficher chaque texte
Consumer<String> affiche1 = texte -> System.out.println(texte);
Consumer<String> affiche2 = System.out::println; // équivalent, plus lisible
```

Il existe **4 formes** (à reconnaître, même si la première couvre la plupart des cas) :

| Forme | Écriture | Exemple | Équivalent lambda |
|---|---|---|---|
| 1. Méthode d'**instance** d'un type quelconque | `Type::methode` | `Reclamation::getStatut` | `r -> r.getStatut()` |
| 2. Méthode **statique** | `Type::methode` | `Integer::parseInt` | `s -> Integer.parseInt(s)` |
| 3. Méthode d'**une instance précise** | `objet::methode` | `System.out::println` | `x -> System.out.println(x)` |
| 4. **Constructeur** | `Type::new` | `ArrayList::new` | `() -> new ArrayList<>()` |

**Analogie** : une lambda, c'est donner la **recette** ; une method reference, c'est donner l'**adresse du restaurant** qui la prépare déjà. Quand le « restaurant » existe, inutile de recopier la recette !

**Quand utiliser une method reference ?** Dès que la lambda ne fait que transmettre ses arguments à une méthode existante. Dès qu'elle ajoute une logique (un `+`, un `&&`, un test), gardez la lambda : elle exprime mieux l'intention.

### 2.7 Ce que la lambda peut « voir » autour d'elle : la capture (et la règle du *effectively final*)

Une lambda peut lire une variable déclarée **à l'extérieur** d'elle. On dit qu'elle la **capture** (« closure » en JavaScript : une fermeture qui emporte son environnement).

```java
String prefixe = "[SignalCUA] "; // variable EXTERNE à la lambda

Consumer<String> journal = message -> System.out.println(prefixe + message);
//                                                       ^^^^^^^ la lambda CAPTURE « prefixe »
```

Mais il y a **une règle stricte** : la variable capturée doit être **« effectively final »** (littéralement « effectivement finale » = **jamais réassignée** après sa première affectation).

```java
String prefixe = "[SignalCUA] ";
Consumer<String> journal = message -> System.out.println(prefixe + message);
prefixe = "[Autre] "; // ❌ ERREUR DE COMPILATION : prefixe n'est plus « effectively final »
```

**Pourquoi Java impose-t-il ça ?** Pour **éviter une surprise**. Une lambda peut être exécutée **plus tard** (voire sur un autre fil d'exécution, voir leçon 02). Si la variable capturée pouvait changer entre-temps, la lambda travaillerait avec une valeur **différente de celle qu'elle « croyait » avoir** au moment de sa création — une source terrible de bugs. En exigeant une valeur **stable**, Java rend le comportement **prévisible**.

**Analogie** : c'est comme glisser un **mot dans une bouteille**. On écrit le mot une fois, on ferme la bouteille ; on ne peut plus modifier le mot ensuite. En JavaScript, la bouteille resterait **ouverte** (la *closure* voit les réassignations) — pratique, mais piégeux. En Java, elle est **scellée**.

> ⚠️ **Exemple concret du piège** (en venant de JavaScript) : ajouter des éléments dans un `for` avec une lambda qui utilise l'index. En JavaScript, `let i` capturé marche « comme on l'espère ». En Java, l'index de boucle **change** à chaque tour : la lambda **refuse** de compiler. La solution : copier la valeur dans une variable locale par tour (ou éviter la lambda, voir leçon 02).

### 2.8 La mise en bouche : `Comparator.comparing(...)`

Un **`Comparator`** (« comparateur ») est une interface fonctionnelle qui compare deux objets et dit lequel est « avant » (`compare(a, b)` renvoie négatif / zéro / positif). Depuis Java 8, on n'écrit presque plus jamais cette méthode à la main : `Comparator.comparing(...)` **reçoit une `Function` qui extrait la clé de tri**, et fabrique le comparateur.

```java
List<Reclamation> liste = ...;

// Trier par identifiant : on EXTRAIT la clé avec une lambda...
liste.sort(Comparator.comparing(r -> r.getId()));

// ... ou avec une method reference (la forme recommandée)
liste.sort(Comparator.comparing(Reclamation::getId));

// Trier par quartier PUIS par date : on enchaîne avec thenComparing (...)
liste.sort(Comparator.comparing(Reclamation::getQuartier)
                      .thenComparing(Reclamation::getDateDeclaration));

// Inverser l'ordre : .reversed()
liste.sort(Comparator.comparing(Reclamation::getId).reversed());
```

**Pourquoi c'est important ?** Parce que c'est votre **premier usage professionnel** de lambda : passer une `Function` d'extraction plutôt que réécrire une méthode `compare`. Et c'est **exactement** le mécanisme que les Streams (leçon 02) utiliseront avec `sorted(...)`.

---

## 📖 Vocabulaire / Abréviations

> Maintenant que vous comprenez l'idée, fixons les mots-clés. Chacun de ces termes réapparaîtra dans les exemples de la section 3 et dans toute la suite de la partie 5.

| Terme | Définition en une ligne |
|---|---|
| **Lambda** | Une petite fonction écrite « à la volée » (`parametres -> corps`), sans nom ni classe visible. |
| **Interface fonctionnelle** | Une interface avec **exactement une méthode abstraite** (SAM) ; cible d'une lambda. |
| **SAM** | *Single Abstract Method* : « une seule méthode abstraite » — la contrainte qui définit une interface fonctionnelle. |
| **`@FunctionalInterface`** | Annotation qui demande au compilateur de **vérifier** la contrainte SAM (protection contre les ajouts accidentels). |
| **`Function<T, R>`** | Interface standard qui **transforme** un `T` en `R` (méthode `apply`). |
| **`Predicate<T>`** | Interface standard qui **teste** un `T` et rend `boolean` (méthode `test`). |
| **`Consumer<T>`** | Interface standard qui **consomme** un `T` sans rien rendre (méthode `accept`). |
| **`Supplier<T>`** | Interface standard qui **fournit** un `T` sans entrée (méthode `get`). |
| **`BiFunction<T, U, R>`** | Variante de `Function` à **deux** entrées. |
| **`UnaryOperator<T>` / `BinaryOperator<T>`** | Variantes où entrée et sortie sont du **même** type (une / deux entrées). |
| **Method reference** | Écriture `Classe::methode` : cite une méthode existante au lieu d'écrire une lambda qui la délègue. |
| **Capture / closure** | Le fait qu'une lambda lise une variable **extérieure** à elle (elle « emporte » son environnement). |
| **Effectively final** | « Effectivement final » : une variable **jamais réassignée**, seule autorisée à être capturée. |
| **`Comparator`** | Interface fonctionnelle qui **compare** deux objets (`compare(a, b)` : négatif / 0 / positif). |
| **`Comparator.comparing(...)`** | Fabrique un comparateur à partir d'une **fonction d'extraction de clé** (`thenComparing` enchaîne, `reversed` inverse). |
| **Inférence de type** | Le compilateur **devine** les types (paramètres de lambda, génériques) à partir du contexte, sans que vous les écriviez. |
| **`Runnable`** | Interface standard « tâche à exécuter plus tard » (méthode `run()`, sans entrée ni sortie). |

---

## 3. Exemples concrets

> La section 2 a expliqué le *comment penser* ; ici, on **regarde tourner**. Chaque exemple suit le modèle SignalCUA (réclamations).

### 3.1 Ma première interface fonctionnelle métier : filtrer à façon

On crée une interface **métier** (son nom raconte l'intention) et une méthode qui la **reçoit** en paramètre : le comportement est **injecté** à l'appel.

```java
@FunctionalInterface                 // on protège la contrainte SAM (une seule méthode abstraite)
interface FiltreReclamation {        // une interface « qui dit si une réclamation m'intéresse »
    boolean accepter(Reclamation r); // le contrat : vrai = « je la garde », faux = « je la jette »
}

// Une méthode qui REÇOIT le filtre en paramètre (le comportement est injecté)
static List<Reclamation> filtrer(List<Reclamation> toutes, FiltreReclamation filtre) {
    List<Reclamation> gardees = new ArrayList<>(); // une liste vide où l'on mettra les retenues
    for (Reclamation r : toutes) {                 // on parcourt TOUTES les réclamations
        if (filtre.accepter(r)) {                  // on applique LE FILTRE REÇU (c'est lui qui décide)
            gardees.add(r);                        // si oui, on la garde
        }
    }
    return gardees;                                // on rend la liste des retenues
}

// À l'appel : on donne LE COMPORTEMENT sous forme de lambda
List<Reclamation> urgentes = filtrer(toutes, r -> r.getPriorite() == Priorite.URGENTE);
List<Reclamation> deMedina = filtrer(toutes, r -> "Medina".equals(r.getQuartier()));
List<Reclamation> recentes  = filtrer(toutes, r -> r.getDateDeclaration().isAfter(hier));
```

Observez : la méthode `filtrer` n'a **jamais** été modifiée. Chaque appel apporte son **propre critère**. C'est la puissance de « passer du comportement ».

### 3.2 Les 4 standard en action, sur SignalCUA

```java
import java.util.function.Function;   // transforme : T -> R (méthode apply)
import java.util.function.Predicate;  // teste : T -> boolean (méthode test)
import java.util.function.Consumer;   // consomme : T -> rien (méthode accept)
import java.util.function.Supplier;   // fournit : rien -> T (méthode get)

// Predicate : « est-ce urgent ? » (un portier qui dit oui / non)
Predicate<Reclamation> estUrgente = r -> r.getPriorite() == Priorite.URGENTE;
boolean aTraiterVite = estUrgente.test(premiere); // on « pose la question » avec test(...)

// Predicate se COMPOSE : et (...) / ou (...) / non (...). negate = « pas »
Predicate<Reclamation> estDeMedina = r -> "Medina".equals(r.getQuartier());
Predicate<Reclamation> urgentEtMedina = estUrgente.and(estDeMedina); // les deux à la fois
Predicate<Reclamation> pasUrgente = estUrgente.negate();             // l'inverse

// Function : « extraire le quartier » (un traducteur Reclamation -> String)
Function<Reclamation, String> quartierDe = Reclamation::getQuartier; // method reference
String q = quartierDe.apply(premiere); // on « applique » la transformation avec apply(...)

// Function se CHAÎNE : andThen (...) (« puis ... »)
Function<String, Integer> longueur = String::length;                 // String -> int
Function<Reclamation, Integer> longueurQuartier = quartierDe.andThen(longueur);
// longueurQuartier : Reclamation -> String -> int (quartier, PUIS longueur)

// Consumer : « afficher » (on consomme, on ne rend rien)
Consumer<Reclamation> afficher = System.out::println; // println existe déjà : on la cite
afficher.accept(premiere); // on « consomme » avec accept(...) : affiche via toString()

// Supplier : « fournir une réclamation de secours » (aucune entrée, une sortie)
Supplier<Reclamation> secours = () -> new Reclamation(0, "Inconnu", Priorite.BASSE,
        StatutReclamation.NOUVELLE, "à qualifier", LocalDateTime.now());
Reclamation r0 = secours.get(); // on « récupère » la valeur avec get()
```

### 3.3 Trier sans écrire `compare` : `Comparator.comparing(...)` en situation

```java
List<Reclamation> registre = new ArrayList<>(...); // trois réclamations de démonstration

// Trier par identifiant croissant (method reference : la forme pro)
registre.sort(Comparator.comparing(Reclamation::getId));

// Trier par quartier, puis par date de déclaration (enchaînement)
registre.sort(Comparator.comparing(Reclamation::getQuartier)
                      .thenComparing(Reclamation::getDateDeclaration));

// Les plus récentes d'abord : on inverse
registre.sort(Comparator.comparing(Reclamation::getDateDeclaration).reversed());

// Trier par priorité « métier » : on extrait un NOMBRE d'ordre (Function), pas l'enum brute
// (URGENTE=0, HAUTE=1, NORMALE=2, BASSE=3 : défini une fois dans Priorite via un champ ordre)
registre.sort(Comparator.comparing(r -> r.getPriorite().getOrdre()));
```

> 💡 **Détail à ne pas rater** : `getOrdre()` n'existe pas « par magie » — c'est un **champ que nous avons ajouté** à l'`enum Priorite` (partie 2, leçon 05 : un `enum` peut porter des champs). Trier directement sur l'`enum` trierait par **ordre de déclaration** (`ordinal()`), ce qui « marche par hasard » mais casse dès qu'on réordonne l'enum. Mieux vaut une **clé explicite** (`getOrdre()`).

### 3.4 La capture *effectively final*, vue à l'exécution

```java
String ville = "Dakar"; // jamais réassignée -> « effectively final » : la capture est LÉGALE
Consumer<String> signer = message -> System.out.println("[" + ville + "] " + message);
signer.accept("import terminé"); // -> [Dakar] import terminé

// Mais ceci NE COMPILE PAS (décommentez pour voir l'erreur du compilateur) :
// String compteur = "un";
// Runnable tache = () -> System.out.println(compteur); // Runnable = interface « exécuter plus tard » (run(), sans entrée ni sortie)
// compteur = "deux"; // ❌ variable used in lambda expression should be final or effectively final
```

**Comment contourner proprement ?** Si la valeur doit vraiment changer, **copiez-la** dans une variable fraîche au moment où vous en avez besoin, ou — mieux — **restructurez** pour ne pas dépendre d'un état mutable (la leçon 02 montrera comment les Streams évitent ce problème par construction).

---

## 4. Bonnes pratiques modernes (2025-2026)

> Les exemples montraient le *comment* ; ici, le *comment bien*. Ce sont les réflexes attendus en entreprise aujourd'hui.

1. **Préférez une method reference à une lambda qui ne fait que déléguer.** `Reclamation::getStatut` plutôt que `r -> r.getStatut()` : plus court, et le **nom de la méthode raconte l'intention**. (Conseil de pro de la roadmap, §5.1.)
2. **Utilisez les interfaces standard** (`Predicate`, `Function`…) **avant** d'en créer une. Une interface custom n'a de sens que si son **nom métier** apporte du sens (`FiltreReclamation`, `RegleDePriorisation`).
3. **Gardez les lambdas courtes** (1 à 3 lignes). Dès qu'il faut un bloc de 10 lignes, extrayez une **méthode privée** et citez-la en method reference : le code redevient lisible et **testable**.
4. **Écrivez des lambdas pures** : pas d'effet de bord (ne modifiez ni une variable extérieure, ni la collection parcourue, ni l'objet reçu). Une lambda est censée **calculer**, pas **changer le monde**. (Anti-pattern de la roadmap, §5.1 : dangereux en parallèle, voir leçon 02.)
5. **Triez avec `Comparator.comparing(...).thenComparing(...)`** plutôt qu'en écrivant `compare` à la main. C'est le standard industriel pour les tris multi-critères.
6. **Mettez toujours `@FunctionalInterface`** sur vos interfaces fonctionnelles : le compilateur devient un **gardien** de la contrainte SAM.
7. **Nommez de façon expressive** : `estUrgente`, `quartierDe`, `urgentEtMedina` plutôt que `p`, `f`, `x`. Une lambda sans nom doit se comprendre **par son usage**.

---

## 5. Pièges à éviter

> Chaque piège ci-dessous a été **vérifié** : le mauvais exemple ne compile pas ou se comporte mal, le bon exemple fonctionne. Comprenez le *pourquoi* avant de passer à l'exercice.

### Piège 1 — Oublier que la lambda doit « coller » à l'interface (signature incompatible)

```java
Predicate<Reclamation> malTypé = r -> r.getPriorite(); // ❌ MAUVAIS : getPriorite() rend un Priorite, pas un boolean
// Le compilateur attend boolean (Predicate.test) : « incompatible types: Priorite cannot be converted to boolean »

// ✅ BON : la lambda rend bien un boolean
Predicate<Reclamation> estUrgente = r -> r.getPriorite() == Priorite.URGENTE;
```

**Pourquoi** : `Predicate.test` **doit** rendre `boolean`. Le compilateur vérifie la **signature complète** (paramètres + retour), pas seulement « à peu près ».

### Piège 2 — Capturer une variable réassignée (le classique en venant de JavaScript)

```java
String filtre = "Medina";
Predicate<Reclamation> duQuartier = r -> filtre.equals(r.getQuartier());
filtre = "Plateau"; // ❌ MAUVAIS : ERREUR DE COMPILATION (filtre n'est plus effectively final)

// ✅ BON : une valeur stable, jamais réassignée
final String filtreFixe = "Medina"; // ou simplement : ne jamais la réassigner après
Predicate<Reclamation> duQuartierFixe = r -> filtreFixe.equals(r.getQuartier());
```

**Pourquoi** : la lambda doit travailler avec une valeur **stable** (raison détaillée en 2.7). Le mot-clé `final` rend la contrainte **explicite**.

### Piège 3 — L'effet de bord : modifier l'extérieur depuis la lambda

```java
List<Reclamation> urgentes = new ArrayList<>();
toutes.forEach(r -> { // ❌ MAUVAIS (compile mais dangereux) : on remplit une liste extérieure
    if (r.getPriorite() == Priorite.URGENTE) urgentes.add(r);
});

// ✅ BON : la sélection est un CALCUL, délégué à un stream (leçon 02) — pas de remplissage externe
List<Reclamation> urgentes = toutes.stream()
        .filter(r -> r.getPriorite() == Priorite.URGENTE)
        .toList();
```

**Pourquoi** : même quand ça compile (on modifie un objet, pas la variable), c'est un **effet de bord** caché : dangereux en parallèle, illisible, contraire au style fonctionnel. Les Streams (leçon 02) rendent ce motif inutile.

### Piège 4 — Une lambda qui lève une exception *checked*

```java
Consumer<String> ecrire = ligne -> Files.writeString(chemin, ligne); // ❌ MAUVAIS : NE COMPILE PAS
// Files.writeString lève IOException (checked) ; Consumer.accept ne déclare AUCUN throws :
// « unreported exception IOException; must be caught or declared to be thrown »

// ✅ BON : on attrape la checked DANS la lambda (ici : on la convertit en unchecked)
Consumer<String> ecrireSur = ligne -> {
    try {
        Files.writeString(chemin, ligne, StandardCharsets.UTF_8);
    } catch (IOException e) {
        throw new UncheckedIOException(e); // UncheckedIOException = « l'IOException en version unchecked »
    }
};
```

**Pourquoi** : une lambda **ne peut pas lever** une exception que l'interface ne déclare pas. Rappel partie 4 : les interfaces standard (`Consumer`, `Function`…) ne déclarent **aucun** `throws`. Il faut donc attraper la *checked* **à l'intérieur**.

### Piège 5 — La surcharge ambiguë : deux interfaces, une seule lambda

```java
static void traiter(Predicate<Reclamation> p) { ... }
static void traiter(Function<Reclamation, Boolean> f) { ... }

traiter(r -> r.getPriorite() == Priorite.URGENTE); // ❌ MAUVAIS : AMBIGU, ne compile pas
// Les deux interfaces « collent » (un paramètre, un boolean) : le compilateur ne sait pas choisir.

// ✅ BON : on précise le type attendu (variable intermédiaire typée)
Predicate<Reclamation> estUrgente = r -> r.getPriorite() == Priorite.URGENTE;
traiter(estUrgente);
```

**Pourquoi** : la lambda n'a **pas de type propre** ; c'est le contexte (la méthode appelée) qui la type. Si deux contextes collent, il faut **lever l'ambiguïté** explicitement.

### Piège 6 — Le `return` qui ne sort pas de la méthode

```java
static Reclamation chercherUrgente(List<Reclamation> toutes) {
    toutes.forEach(r -> {
        if (r.getPriorite() == Priorite.URGENTE) {
            // return r; // ❌ IMPOSSIBLE : un return dans une lambda sort de LA LAMBDA, pas de chercherUrgente
        }
    });
    return null;
}

// ✅ BON : on laisse le terminal du stream porter le résultat (leçon 02)
static Optional<Reclamation> chercherUrgente(List<Reclamation> toutes) {
    return toutes.stream().filter(r -> r.getPriorite() == Priorite.URGENTE).findFirst();
}
```

**Pourquoi** : une lambda est **son propre petit monde** : `return` y sort de la lambda, jamais de la méthode englobante. Pour « sortir tôt » d'un parcours, utilisez les terminaux de Stream (`findFirst`, `anyMatch`…).

### Piège 7 — Écrire `compare` à la main au lieu de `Comparator.comparing`

```java
// ❌ MAUVAIS : verbeux et source d'erreurs (inversion de a et b, soustraction qui déborde avec des int)
liste.sort((a, b) -> a.getId() - b.getId());

// ✅ BON : on déclare la CLÉ, le framework fait le reste
liste.sort(Comparator.comparingInt(Reclamation::getId)); // comparingInt = variante pour clés int
```

**Pourquoi** : `comparing(...)` (et ses variantes `comparingInt`, `comparingLong`, `comparingDouble`) **élimine** toute la mécanique de comparaison. Notez `comparingInt` : pour une clé `int`, il évite le **boxing** (partie 1 : l'emballage coûteux `int` → `Integer`).

### Piège 8 — La lambda « couteau suisse » de 20 lignes

```java
// ❌ MAUVAIS : une lambda qui fait tout (valide, formate, journalise...) : intestable, illisible
toutes.forEach(r -> { /* 20 lignes qui mélangent 4 responsabilités */ });

// ✅ BON : on extrait une méthode privée nommée, citée en method reference
toutes.forEach(Signalisation::journaliserEtQualifier); // le NOM raconte l'intention ; la méthode est testable
```

**Pourquoi** : une lambda **anonyme** ne peut pas être **testée unitairement** (partie 9) ni **réutilisée**. Dès qu'elle dépasse 3 lignes ou mélange des responsabilités, donnez-lui un **nom** (méthode) : c'est la même logique, mais adressable.

---

## Checklist de validation

Avant de passer à l'exercice, vérifiez que vous savez faire **chacun** de ces points :

- [ ] Expliquer pourquoi une lambda peut remplacer une classe anonyme (le compilateur connaît déjà la méthode unique).
- [ ] Définir **SAM** et dire ce que vérifie `@FunctionalInterface`.
- [ ] Écrire une lambda sous ses 3 formes : corps-expression, corps-bloc avec `return`, zéro/un/plusieurs paramètres.
- [ ] Choisir la bonne interface standard : `Predicate` (test), `Function` (transformation), `Consumer` (action), `Supplier` (fourniture).
- [ ] Réécrire `r -> r.getX()` en `Classe::getX` et citer les 4 formes de method reference.
- [ ] Expliquer la règle **effectively final** et sa raison (stabilité de la valeur capturée).
- [ ] Composer `estUrgente.and(estDeMedina)`, `negate()`, `quartierDe.andThen(longueur)`.
- [ ] Trier avec `Comparator.comparing(...).thenComparing(...).reversed()`.
- [ ] Expliquer pourquoi une lambda ne peut pas lever une `checked` que l'interface ne déclare pas.

---

## 🔴 Fil rouge — ou en est SignalCUA ?

SignalCUA a gagné une **boîte à outils comportementale** : une interface `FiltreReclamation`, des `Predicate`/`Function`/`Consumer` prêts, et des tris expressifs par `Comparator.comparing`. Le registre ne sait pas encore **transformer** ses listes en une phrase (c'est le rôle de la **leçon 02 — Stream API**, qui utilisera ces mêmes lambdas comme configuration de pipeline).

---

➡️ **Prochaine étape** : la leçon 02 — **Stream API**. Vous avez appris à *écrire* des actions (lambdas) ; maintenant on les *enchaîne* : `filter`, `map`, `sorted`, `reduce`… Chaque étape du pipeline recevra une lambda — tout ce qui précède trouve ici son usage.
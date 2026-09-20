# Leçon 02 — Generics : écrire du code une fois pour tous les types

> 🧭 **Pont depuis la leçon 01** : vous venez d'écrire `List<Reclamation>`, `Map<Integer, Reclamation>`, `Deque<Reclamation>`, `PriorityQueue<Reclamation>`… et vous avez accepté sans broncher ces chevrons `<...>`. Ils viennent d'être partout — il est temps de savoir les écrire **soi-même**. Cette leçon répond aux questions restées ouvertes : d'où vient ce `<T>` ? Pourquoi le compilateur refuse `List<int>` ? Pourquoi un `Set<String>` ne peut-il vraiment pas contenir d'`Integer` ? Et que signifient ces `<? extends ...>` que vous croiserez dans toutes les bibliothèques (Spring compris) ?

---

## 1. Objectifs d'apprentissage

À la fin de cette leçon, vous saurez :

- Expliquer **pourquoi** les generics existent (le problème des `cast` et des erreurs découvertes trop tard).
- Écrire une **classe générique** (`Boite<T>`, `Historique<T>`, `Paire<K, V>`) et comprendre que `T` est choisi **à l'utilisation**.
- Écrire une **méthode générique** (`<T> T premier(List<T>)`) et une méthode à **borne** (`<T extends Comparable<T>>`).
- Utiliser le **diamant** `<>` et expliquer ce que le compilateur déduit.
- Lire et écrire les **wildcards** : `?`, `? extends T`, `? super T`, en appliquant la règle **PECS**.
- Expliquer l'**effacement de type** (*type erasure*) et ses trois conséquences concrètes (`new T[]`, `instanceof List<String>`, `List<int>` interdits).
- Reconnaître et corriger les pièges : *raw type*, invariance, surcharge impossible, `T` en contexte `static`.

---

## 2. Explication simple

### 2.1 Le problème d'avant : la boîte fourre-tout et les casts

**Pourquoi ?** Avant Java 5 (2004), une `List` contenait des `Object` — c'est-à-dire « n'importe quoi ». On y rangeait tout, et **on** devait dire au compilateur quoi ressortir :

```java
// ❌ Le style d'avant 2004 : aucun garde-fou
List liste = new ArrayList();
liste.add("Medina");            // une String
liste.add(42);                  // et un Integer : rien ne l'interdit !
String q = (String) liste.get(1);   // on ment au compilateur...
// → ClassCastException À L'EXÉCUTION (parfois des semaines après, en production)
```

**Analogie** : c'est un **carton non étiqueté** dans un déménagement. On y met des assiettes et des livres, et c'est au moment de déballer (à l'exécution) qu'on découvre que « le carton "cuisine" » contient des briques.

**Comment ?** Les **generics** (mot à mot : « les génériques ») permettent d'**étiqueter le carton** : `List<String>` est une liste qui **ne peut contenir que** des `String`. Le compilateur vérifie alors **avant** l'exécution :

```java
// ✅ Le style moderne
List<String> liste = new ArrayList<>();
liste.add("Medina");
liste.add(42);                  // ⛔ ERREUR DE COMPILATION : int cannot be converted to String
String q = liste.get(0);        // aucun cast : le compilateur SAIT que c'est une String
```

La différence fondamentale, à retenir toute votre carrière : **l'erreur passe de l'exécution (trop tard, chez l'utilisateur) à la compilation (tout de suite, sur votre machine)**. C'est tout le bénéfice.

**Quand ?** Dès que le **même code** doit fonctionner pour **plusieurs types** : un conteneur, un résultat, un historique, une réponse d'API. Et, à l'inverse, jamais pour « tout mélanger » : si votre `Boite<T>` vous sert à ranger indifféremment des `Reclamation` et des `String`, c'est le signe d'un défaut de conception, pas d'une réussite.

### 2.2 Une classe générique : `T`, le type qu'on laisse en attente

**Analogie** : une **étiquette vierge** collée sur un carton. Vous écrivez le contenu **au moment de l'utiliser** : `Boite<String>` (carton « chaînes »), `Boite<Reclamation>` (carton « réclamations »). Le **code** de la boîte, lui, est écrit **une seule fois**.

**Comment ?** On déclare un **paramètre de type** — une lettre entre chevrons — juste après le nom de la classe :

```java
public class Boite<T> {                 // T = "un type, à choisir plus tard"

    private T contenu;                  // le champ est du type T

    public void mettre(T valeur) {      // on reçoit un T
        this.contenu = valeur;
    }

    public T prendre() {                // on ressort un T
        return contenu;
    }
}
```

À l'utilisation, on **remplace** `T` :

```java
Boite<String> b1 = new Boite<>();       // ici T = String
b1.mettre("Nid de poule");
String s = b1.prendre();                // pas de cast, pas de risque

Boite<Reclamation> b2 = new Boite<>();  // ici T = Reclamation — MÊME code source
b2.mettre(r1);
Reclamation r = b2.prendre();           // les méthodes de Reclamation sont accessibles
```

> 📖 **Vocabulaire** : **paramètre de type** = le `T` de la déclaration (« le type en attente »). **Argument de type** = la valeur donnée à l'usage (`String`, `Reclamation`). **Paramétré** = « qui a reçu son argument de type » (`Boite<String>` est une boîte paramétrée).

**Et `T`, c'est quoi exactement ?** Une simple **convention de nommage**, héritée de la littérature : `T` = *Type*, `E` = *Element* (dans `List<E>`), `K` = *Key* et `V` = *Value* (dans `Map<K, V>`), `R` = *Result*, `U`, `S` pour des seconds/troisièmes types. Vous pouvez écrire `<Patate>`, ça compilerait — mais personne ne vous remercierait.

### 2.3 Le « diamant » `<>` : le compilateur devine le type

**Pourquoi ?** Écrire `Boite<String> b = new Boite<String>();` répète deux fois la même information. Depuis Java 7, on peut laisser le compilateur **déduire** l'argument de type à droite.

**Comment ?**

```java
List<Reclamation> liste = new ArrayList<>();     // le <> s'appelle le "diamant"
Map<String, List<Reclamation>> index = new HashMap<>();   // deviné aussi dans ce cas complexe
```

Le compilateur lit la **déclaration à gauche** et en déduit le type de droite. ⚠️ Le diamant ne fonctionne **que** lorsque le contexte fournit le type (une déclaration, un argument de méthode, un `return`). Dans `var boite = new Boite<>();` — `var` ne donne aucun type, donc l'inférence n'a rien à quoi se raccrocher : le compilateur y voit un `Boite<Object>`.

> 📖 **Vocabulaire** : **inférence de type** = le compilateur devine le type à partir du contexte. **`var`** (Java 10) = « le type de cette variable est celui de l'expression de droite » (les génériques et `var` feront bon ménage dès que le contexte est explicite, par exemple `var liste = new ArrayList<Reclamation>();`).

### 2.4 Une méthode générique : `<T>` avant le type de retour

**Pourquoi ?** Vous avez besoin d'une **opération** valable pour tout type, sans créer une classe générique : « donne-moi le premier élément d'une liste », « affiche-moi n'importe quel objet ».

**Comment ?** On place `<T>` **avant** le type de retour, et on l'utilise dans la signature :

```java
public static <T> T premier(List<T> elements) {   // "pour tout type T, je rends un T"
    if (elements.isEmpty()) {
        throw new IllegalArgumentException("Liste vide");
    }
    return elements.get(0);                       // aucun cast : le compilateur connaît le type
}
```

L'appel se fait sans préciser `T` (le compilateur le déduit) — et le retour est **typé** :

```java
String q  = premier(List.of("Medina", "Fann"));    // T = String (déduit)
Integer n = premier(List.of(7, 9, 3));             // T = Integer (déduit)
String erreur = premier(List.of(7, 9, 3));         // ⛔ erreur de compilation : Integer -> String
```

Dernière ligne : voilà le gain. Le compilateur **relie** le type de l'entrée et celui de la sortie. C'est ce que feront plus tard vos propres méthodes utilitaires.

### 2.5 Les bornes : `extends` pour dire « un type, mais pas n'importe lequel »

**Pourquoi ?** Dans `maximum(List<T>)`, vous devez comparer les éléments entre eux (`compareTo`). Mais `T` peut être n'importe quoi — y compris un type **non comparable**. Le compilateur vous en empêchera (heureusement). Il faut donc **restreindre** `T`.

**Comment ?** Une **borne** s'écrit avec `extends` — au sens « **est un** » / « est ou hérite de » (cela vaut aussi pour les interfaces !) :

```java
public static <T extends Comparable<T>> T maximum(List<T> elements) {
    T meilleur = elements.get(0);
    for (T element : elements) {
        if (element.compareTo(meilleur) > 0) {    // legal : T GARANTIT compareTo
            meilleur = element;
        }
    }
    return meilleur;
}
```

Décomposons `<T extends Comparable<T>>` : « T est un type qui sait se comparer à un autre T ». C'est exactement la promesse des `String`, des `Integer`, des `LocalDate`… et c'est pourquoi `maximum(List.of("Medina", "Fann"))` compile tandis que `maximum` sur une liste de `Reclamation` (qui n'implémente pas `Comparable`, voir leçon 01) refuserait de compiler.

> 📖 **Vocabulaire** : **borne (*bound*)** = restriction posée sur un paramètre de type. **`extends` dans les generics** se lit « est un / implémente » — pour les **classes comme pour les interfaces**. Il n'existe pas de borne `super` : `<T super X>` n'est pas valide.
> Une classe peut aussi être **elle-même** générique et bornée : `class Trieur<T extends Comparable<T>> { ... }`.

### 2.6 Les wildcards : `?`, `? extends`, `? super` (et la règle PECS)

**Pourquoi ?** Les generics sont **invariants** : `List<String>` **n'est pas** une `List<Object>`, même si `String` est un `Object`. C'est volontaire : sinon on pourrait ajouter un `Integer` dans une `List<String>` via la porte dérobée d'une `List<Object>` (le code ci-dessous ne compile pas, et c'est une **bonne** nouvelle) :

```java
List<Object> objets = new ArrayList<String>();   // ⛔ incompatible types
```

Mais alors comment écrire une méthode qui accepte **aussi** les sous-types ? Avec un **wildcard** (mot à mot : « joker »), le `?` :

```java
public static double somme(List<? extends Number> nombres)   // accepte List<Integer>, List<Double>...
public static void vider(List<? super Integer> destination)  // accepte List<Integer>, List<Number>, List<Object>
```

**Comment retenir lequel choisir ?** Par la **règle PECS** (*Producer Extends, Consumer Super*), traduisible ainsi :

> **Un conteneur qui PRODUIT (dont je LIS les éléments) → `? extends`.**
> **Un conteneur qui CONSOMME (dans lequel J'ÉCRIS) → `? super`.**

| Écriture | Je peux… | Je ne peux pas… | Sert quand le paramètre est… |
|---|---|---|---|
| `List<? extends T>` | **lire** des éléments (comme des `T`) | **ajouter** (le vrai type est inconnu) | une **source** (producteur) |
| `List<? super T>` | **ajouter** des `T` | garantir le type des éléments lus (ils peuvent être `Object`) | une **destination** (consommateur) |
| `List<?>` | lire, compter, parcourir | ajouter (sauf `null`) | « n'importe quelle liste », en lecture seule |

C'est exactement ce que fait la méthode de copie ci-dessous — l'archétype de PECS :

```java
public static <T> void copier(List<? extends T> source,        // producteur : on lit
                             List<? super T> destination) {    // consommateur : on écrit
    for (T element : source) {
        destination.add(element);
    }
}
```

**Pourquoi ça ne « casse » rien** : le compilateur garantit que tout élément lu de `source` **est** un `T`, et que `destination` accepte **au moins** des `T`. Vous ne pouvez donc rien mettre de travers.

> 📖 **Vocabulaire** : **wildcard** = `?`, « un type inconnu ». **Invariance** = une `List<String>` n'est **pas** une `List<Object>` (et réciproquement). **PECS** = *Producer Extends, Consumer Super* : la phrase à retenir pour choisir le bon joker.
> **À quoi cela sert-il dans la vraie vie ?** À lire les signatures comme celle de Spring : `ResponseEntity<T>`, `JpaRepository<T, ID>`, `Optional<T>`. Et à écrire vos propres méthodes utilitaires acceptant des familles de types.

### 2.7 L'effacement de type (« type erasure ») : un décor de compilation

**Pourquoi ?** Question légitime : puisque `Boite<String>` et `Boite<Reclamation>` sont deux types différents, la JVM crée-t-elle deux classes ? **Non.** Pour ne pas casser les programmes écrits avant 2004, Java **efface** les types génériques après la compilation : dans le fichier `.class`, il ne reste qu'un `Boite` manipulant des `Object` (avec les *casts* réintroduits automatiquement là où il faut).

**Analogie** : les generics sont une **étiquette collée le temps de la compilation**, retirée avant l'exécution. Le contrôleur vérifie l'étiquette à l'entrée (compilation), mais à l'intérieur de l'entrepôt (la JVM), les cartons redeviennent anonymes.

**Conséquences concrètes** — trois choses **impossibles**, avec les messages **réellement renvoyés par `javac 21`** :

```java
// ⛔ A) On ne peut pas créer un tableau d'un type paramétré
public static <T> T[] creerTableau() {
    return new T[10];
}
// error: generic array creation

// ⛔ B) On ne peut pas tester un type paramétré avec instanceof
public static boolean estListeDeString(Object o) {
    return o instanceof List<String>;
}
// error: Object cannot be safely cast to List<String>

// ⛔ C) Les types primitifs ne sont pas des paramètres de type
List<int> nombres = new ArrayList<>();
// error: unexpected type   required: reference   found: int
```

**Comment vivre avec ?**

- **A)** Préférez les **collections** aux tableaux quand vous êtes en générique (`List<T>` au lieu de `T[]`). Si un tableau est indispensable (interopérabilité, performance), déclarez-le avec un type supérieur non générique (`Object[]`) et arrêtez-vous là.
- **B)** On teste le **type brut**, puis on convertit en assumant le risque :
  ```java
  if (o instanceof List<?> liste) {      // ✅ Java 16+ : pattern matching d'instanceof
      System.out.println("liste de " + liste.size() + " éléments");
  }
  ```
- **C)** On utilise les **classes enveloppes** (`Integer` au lieu de `int`, `Double`, `Boolean`…) — Java fait la conversion automatiquement (*autoboxing*, vu en partie 1). C'est précisément pourquoi on écrit `List<Integer>` et jamais `List<int>`.

**Et le danger ?** L'effacement explique aussi le piège du *raw type* (section 5) : comme la vérification disparaît à l'exécution, un passage par un type brut laisse entrer n'importe quoi — l'erreur ressort plus tard, en `ClassCastException`.

### 2.8 Les generics que vous utilisez déjà (et ceux de Spring)

Vous ne partez pas de zéro : la bibliothèque standard en est pleine, et vous en avez déjà croisé plusieurs sans les nommer.

| Type générique | Ses paramètres | Où vous l'avez vu |
|---|---|---|
| `List<E>`, `Set<E>`, `Queue<E>` | `E` = le type des **éléments** | leçon 01 |
| `Map<K, V>` | `K` = clé, `V` = valeur | leçon 01 |
| `Optional<T>` | `T` = le type de la valeur **peut-être présente** | **leçon 03** |
| `Comparable<T>` / `Comparator<T>` | le type **comparé** ou comparé à | leçon 01 |
| `Boite<T>`, `Paire<K, V>` | vos propres types génériques | cette leçon |
| `ResponseEntity<T>` (Spring) | le type du **corps** de la réponse HTTP | partie 7 |
| `JpaRepository<T, ID>` (Spring Data) | l'**entité** et le type de sa **clé** | partie 8 |

Cette dernière ligne mérite d'être retenue : votre futur dépôt de réclamations s'écrira `interface ReclamationRepository extends JpaRepository<Reclamation, Long>` — les generics ne sont pas un exercice scolaire, c'est le vocabulaire de Spring.

---

## 📖 Vocabulaire / Abréviations

| Terme | Définition en une ligne |
|---|---|
| **Generics** | Mécanisme qui rend un type **paramétrable** (`Boite<T>`, `List<String>`). |
| **Paramètre de type** | La lettre déclarée dans la classe/méthode (`<T>`), = « type en attente ». |
| **Argument de type** | Le type donné à l'usage (`Boite<String>` → l'argument est `String`). |
| **Paramétré / brut (*raw type*)** | `Boite<String>` (typé) / `Boite` sans `<>` (non typé ⇒ vérifications perdues). |
| **Diamant `<>`** | Écriture qui laisse le compilateur **déduire** l'argument de type. |
| **Inférence de type** | Le compilateur devine le type d'après le contexte. |
| **`var`** | Mot-clé (Java 10) qui demande au compilateur de déduire le type de la variable. |
| **Borne (*bound*)** | Restriction sur un paramètre de type (`<T extends Comparable<T>>`). |
| **Wildcard (`?`)** | « Un type inconnu » : `List<?>`, `List<? extends Number>`, `List<? super Integer>`. |
| **PECS** | *Producer Extends, Consumer Super* : la règle de choix d'un wildcard. |
| **Invariance** | `List<String>` n'est **pas** `List<Object>` : les generics ne « montent » pas d'eux-mêmes. |
| **Effacement de type (*type erasure*)** | Les generics disparaissent après compilation : la JVM ne voit que des `Object`. |
| **Autoboxing / unboxing** | Conversion automatique `int` ⇄ `Integer` (partie 1). |
| **Classe enveloppe (*wrapper*)** | `Integer`, `Double`… : la version objet d'un type primitif. |
| **Heap pollution** | Contamination d'une collection typée par un élément de mauvais type (via un *raw type*). |
| **`Comparable` / `Comparator`** | Savoir se comparer / fournir une règle de comparaison externe (leçon 01). |
| **Convention `T`, `E`, `K`, `V`, `R`** | Noms d'usage des paramètres : Type, Element, Key, Value, Result. |
| **Surcharge (*overload*)** | Plusieurs méthodes de même nom avec des paramètres différents. |
| **Signature** | Nom + types des paramètres d'une méthode (sert aussi à la surcharge). |

---

## 3. Exemples concrets

Nous écrivons quatre fichiers : deux types génériques (`Boite<T>`, `Paire<K, V>`), une classe d'outils génériques (`Outils`) et le programme de démonstration. Ils couvrent **tout** ce que la section 2 a expliqué.

**Fichier 1 — `Boite.java` : notre premier type générique.**

```java
public class Boite<T> {                     // T = le type "en attente", choisi par l'utilisateur

    private T contenu;                      // champ du type T

    public void mettre(T valeur) {          // on stocke un T
        this.contenu = valeur;
    }

    public T prendre() {                    // on ressort un T, sans aucun cast
        return contenu;
    }
}
```

**Fichier 2 — `Paire.java` : un record générique à deux paramètres.**

```java
// Un record generique : K = type de la cle, V = type de la valeur
public record Paire<K, V>(K cle, V valeur) {

    public String affichage() {
        return cle + " = " + valeur;        // toString() est appelé sur les deux
    }
}
```

> 💡 **Rappel de la partie 2** : un `record` génère `equals`, `hashCode` et `toString` — mais avec **deux** paramètres de type, ces méthodes restent correctes automatiquement. C'est pour cela qu'un `record` est le type idéal des valeurs « produit » et des clés de `Map` (leçon 01, piège 2).

**Fichier 3 — `Outils.java` : méthodes génériques, borne et PECS.**

```java
import java.util.List;

public class Outils {

    // 1. Méthode générique : le <T> se place AVANT le type de retour
    public static <T> T premier(List<T> elements) {
        if (elements.isEmpty()) {
            throw new IllegalArgumentException("Liste vide");
        }
        return elements.get(0);                  // retourne un T : aucun cast à écrire
    }

    // 2. Borne : T doit être comparable À LUI-MÊME, sinon .compareTo n'existe pas
    public static <T extends Comparable<T>> T maximum(List<T> elements) {
        T meilleur = elements.get(0);
        for (T element : elements) {
            if (element.compareTo(meilleur) > 0) {   // > 0 = "plus grand que"
                meilleur = element;
            }
        }
        return meilleur;
    }

    // 3. Wildcards, règle PECS :
    //    Producteur Extends (on LIT dans source), Consommateur Super (on ÉCRIT dans destination)
    public static <T> void copier(List<? extends T> source, List<? super T> destination) {
        for (T element : source) {
            destination.add(element);
        }
    }

    // 4. Utile pour compter n'importe quoi par clé (Map de comptage générique)
    public static <K> void incrementer(java.util.Map<K, Integer> compteurs, K cle) {
        Integer actuel = compteurs.get(cle);
        compteurs.put(cle, actuel == null ? 1 : actuel + 1);
    }
}
```

**Fichier 4 — `MainGenerics.java` : la démonstration complète.**

```java
import java.util.ArrayList;
import java.util.List;

public class MainGenerics {

    public static void main(String[] args) {

        // ============ 1. Une classe générique : Boite<T> ============
        Boite<String> boiteTexte = new Boite<>();
        boiteTexte.mettre("Nid de poule");
        String texte = boiteTexte.prendre();          // pas de cast : le type est connu
        System.out.println("1) Boite<String> -> " + texte.toUpperCase());   // méthode de String OK

        Boite<Integer> boiteNombre = new Boite<>();
        boiteNombre.mettre(42);
        int nombre = boiteNombre.prendre();           // déballage automatique vers int
        System.out.println("   Boite<Integer> -> " + (nombre + 1));

        // ============ 2. Un record générique : Paire<K, V> ============
        Paire<String, Integer> statistique = new Paire<>("Medina", 12);
        System.out.println("2) Paire -> " + statistique.affichage());
        System.out.println("   cle typee : " + statistique.cle().toUpperCase());  // String

        // ============ 3. Méthodes génériques ============
        System.out.println("3) premier(List<String>) -> "
                + Outils.premier(List.of("alpha", "beta")));
        System.out.println("   premier(List<Integer>) -> "
                + Outils.premier(List.of(7, 9, 3)));

        // ============ 4. Borne <T extends Comparable<T>> ============
        System.out.println("4) maximum de [7, 9, 3] -> " + Outils.maximum(List.of(7, 9, 3)));
        System.out.println("   maximum de [\"Medina\", \"Fann\", \"Plateau\"] -> "
                + Outils.maximum(List.of("Medina", "Fann", "Plateau")));

        // ============ 5. Wildcards / PECS ============
        List<Integer> source = List.of(1, 2, 3);
        List<Number> destination = new ArrayList<>();
        Outils.copier(source, destination);           // List<Integer> -> List<? super Integer>
        System.out.println("5) copie List<Integer> vers List<Number> -> " + destination);

        java.util.Map<String, Integer> compteurs = new java.util.HashMap<>();
        Outils.incrementer(compteurs, "Medina");
        Outils.incrementer(compteurs, "Medina");
        Outils.incrementer(compteurs, "Plateau");
        System.out.println("   comptage générique -> " + compteurs);

        // ============ 6. Type erasure : les generics disparaissent à l'exécution ============
        List<String> textes = new ArrayList<>();
        List<Integer> nombres = new ArrayList<>();
        System.out.println("6) les deux classes au runtime sont identiques : "
                + (textes.getClass() == nombres.getClass()));
        System.out.println("   nom réel de la classe : " + textes.getClass().getSimpleName());

        // Le danger : passer par un "raw type" contourne la vérification...
        List brut = textes;            // raw type (sans <>) : le compilateur ne vérifie plus
        brut.add(42);                  // un Integer entre dans une List<String>
        try {
            String element = textes.get(0);      // la vérification se fait ICI, trop tard
            System.out.println("   element : " + element);
        } catch (ClassCastException e) {
            System.out.println("   [danger] ClassCastException au lieu d'une erreur de compilation");
        }
    }
}
```

Compilation et exécution :

```bash
javac -encoding UTF-8 -Xlint:all *.java     # -Xlint:all : demander TOUS les avertissements
java MainGenerics
```

**Sortie réellement obtenue** (Java 21.0.7) — commençons par les **avertissements**, car ils sont précieux :

```text
MainGenerics.java:55: warning: [rawtypes] found raw type: List
        List brut = textes;            // raw type (sans <>) : le compilateur ne vérifie plus
        ^
  missing type arguments for generic class List<E>
  where E is a type-variable:
    E extends Object declared in interface List
MainGenerics.java:56: warning: [unchecked] unchecked call to add(E) as a member of the raw type List
        brut.add(42);                  // un Integer entre dans une List<String>
                ^
  where E is a type-variable:
    E extends Object declared in interface List
2 warnings
```

Puis la sortie du programme :

```text
1) Boite<String> -> NID DE POULE
   Boite<Integer> -> 43
2) Paire -> Medina = 12
   cle typee : MEDINA
3) premier(List<String>) -> alpha
   premier(List<Integer>) -> 7
4) maximum de [7, 9, 3] -> 9
   maximum de ["Medina", "Fann", "Plateau"] -> Plateau
5) copie List<Integer> vers List<Number> -> [1, 2, 3]
   comptage générique -> {Plateau=1, Medina=2}
6) les deux classes au runtime sont identiques : true
   nom réel de la classe : ArrayList
   [danger] ClassCastException au lieu d'une erreur de compilation
```

**Lisons cette sortie ensemble :**

1. **`Boite<String>` et `Boite<Integer>` utilisent le même code source** — et `texte.toUpperCase()` est possible **sans cast** : le compilateur sait que c'est une `String`. Le `+ (nombre + 1)` prouve l'*unboxing* (`Integer` → `int`).
2. **`Paire<String, Integer>`** : deux types indépendants dans un même objet ; `statistique.cle()` est une `String` (donc `.toUpperCase()` est disponible).
3. **`premier`** renvoie tour à tour une `String` et un `Integer` : le type de retour **suit** le type de l'argument.
4. **`maximum`** fonctionne pour des `Integer` **et** des `String` : la borne `<T extends Comparable<T>>` autorise les deux, et garantit que `compareTo` existe.
5. **PECS en action** : `List<Integer>` copiée dans une `List<Number>` — impossible sans wildcards (les generics sont invariants). Le comptage générique illustre `<K>` sur une `Map<K, Integer>`.
6. **`true` entre les deux `getClass()`** : l'effacement est bien réel, la JVM ne connaît que `ArrayList`. Enfin, la `ClassCastException` attrapée montre le prix du *raw type* : l'erreur a été **déplacée** de la compilation vers l'exécution.

---

## 4. Bonnes pratiques modernes (2025-2026)

1. **Ne déclarez jamais un *raw type*** : `List` sans `<>` fait taire le compilateur et prépare une `ClassCastException`. Compilez avec `javac -Xlint:all` (et, en Maven/Gradle, `-Werror` pour transformer les avertissements en erreurs) — partie 6.
2. **Respectez les conventions de nommage** : `T` (type), `E` (élément d'une collection), `K`/`V` (clé/valeur), `R` (résultat). Un nom plus parlant reste préférable à `A`, `B`, `C`.
3. **Limitez-vous à deux ou trois paramètres de type.** Au-delà, la signature devient illisible : c'est le signal qu'il faut un type dédié (classe ou `record`).
4. **Une méthode générique commence par `<T>`** : `public static <T> List<T> fusion(...)`. Oublier ce `<T>` est la cause n°1 des erreurs « *cannot find symbol: class T* ».
5. **Réservez les wildcards aux paramètres de méthode** (PECS). Dans un **champ** ou une **variable locale**, utilisez un paramètre de type : un `List<?>` stocké est inutilisable en écriture.
6. **Un `record` générique pour les paires et les résultats** : `record Resultat<T>(T valeur, String message)` est plus clair qu'une `Map<String, Object>` ou un `Object[]` — et il obtient `equals`/`hashCode` gratuitement.
7. **N'introduisez du générique que là où plusieurs types sont réellement concernés.** Un conteneur utilisé pour un seul type n'a pas besoin d'être générique.
8. **Préférez les méthodes « usines » statiques génériques** quand l'inférence compte : `Paire.de("Medina", 12)` déduit les deux types, là où le constructeur exige parfois de les écrire.
9. **Anticipez Spring** : les generics sont partout en partie 7-8 (`ResponseEntity<T>`, `Optional<T>`, `JpaRepository<T, ID>`, `List<Reclamation>` dans un contrôleur). Comprendre `<T>` aujourd'hui vous fera lire le code Spring sans hésiter demain.
10. **Testez bornes et wildcards avec `javac`** : la meilleure façon de comprendre les generics est de **provoquer** les erreurs — c'est précisément ce que fait la section suivante.

---

## 5. Pièges à éviter

Les messages ci-dessous sont ceux **réellement produits par `javac 21`** sur les fichiers de démonstration — vous les rencontrerez tels quels.

### Piège 1 — Le *raw type* : l'avertissement qu'on ignore à ses dépens

```java
// ❌ MAUVAIS : List sans <> (raw type)
List brut = textes;                   // warning: [rawtypes] found raw type: List
brut.add(42);                         // warning: [unchecked] unchecked call to add(E)
String s = textes.get(0);             // ⛔ ClassCastException À L'EXÉCUTION (vérifié)

// ✅ BON : on type, le compilateur se charge du reste
List<String> textes = new ArrayList<>();
textes.add("Medina");
// textes.add(42);                    // erreur de COMPILATION : le problème n'atteint jamais la prod
```

**Pourquoi c'est dangereux** : deux avertissements (que tout le monde ignore) transforment une erreur de compilation — gratuite — en plantage d'exécution chez l'utilisateur.

### Piège 2 — Croire que `List<String>` « est un » `List<Object>`

```java
// ❌ NE COMPILE PAS : les generics sont invariants
List<Object> objets = new ArrayList<String>();
// error: incompatible types: ArrayList<String> cannot be converted to List<Object>

// ✅ BON : on choisit le type voulu, ou on passe par un wildcard dans une SIGNATURE de méthode
List<Object> objets = new ArrayList<>();
List<? extends Object> lectureSeule = new ArrayList<String>();   // accepté
```

**Pourquoi cette règle existe** : autoriser la ligne interdite permettrait `objets.add(42)`, donc un `Integer` dans une liste de `String`. L'invariance est une **protection**, pas une brimade.

### Piège 3 — Vouloir ajouter dans un `? extends`

```java
// ❌ REFUSÉ : on ne connaît pas le "vrai" type de la liste fournie
public static void remplir(List<? extends Number> nombres) {
    nombres.add(42);
}
// error: incompatible types: int cannot be converted to CAP#1
//   where CAP#1 is a fresh type-variable:
//     CAP#1 extends Number from capture of ? extends Number

// ✅ BON : pour ÉCRIRE, on utilise "? super" (règle PECS)
public static void remplir(List<? super Integer> nombres) {
    nombres.add(42);                  // accepté : la liste contient AU MOINS des Integer
}
```

**Lecture du message** : `CAP#1` (« *capture* n°1 ») est le nom que le compilateur donne au type **inconnu** capturé par `?`. Comme il est inconnu, il refuse l'ajout — sauf si vous déplacez la borne du bon côté (`? super`).

### Piège 4 — Surcharger deux méthodes au même effacement

```java
// ❌ REFUSÉ par le compilateur (et c'est logique)
public static void traiter(List<String> textes) { }
public static void traiter(List<Integer> nombres) { }
// error: name clash: traiter(List<Integer>) and traiter(List<String>) have the same erasure

// ✅ BON : des noms différents (ou des types différents hors generics)
public static void traiterTextes(List<String> textes) { }
public static void traiterNombres(List<Integer> nombres) { }
```

**Pourquoi** : après effacement, les deux signatures deviennent `traiter(List)` — la JVM ne saurait pas laquelle appeler. Même mécanisme que l'impossibilité de `instanceof List<String>`.

### Piège 5 — Utiliser `T` dans un contexte `static`

```java
public class Boite<T> {
    // ❌ REFUSÉ : T appartient à l'INSTANCE, pas à la classe
    public static T creerVide() { return null; }
    // error: non-static type variable T cannot be referenced from a static context

    // ✅ BON (1) : une méthode d'instance
    public T creerVideInstance() { return null; }

    // ✅ BON (2) : une méthode STATIQUE qui DÉCLARE son propre paramètre de type
    public static <U> Boite<U> creerVide() { return new Boite<>(); }
}
```

**Pourquoi** : `Boite<String>` et `Boite<Integer>` partagent la même classe ; un membre `static` n'appartient à aucune instance, donc « `T` » n'y a aucune valeur. D'où la méthode statique qui **redéclare** `<U>` — exactement le mécanisme de votre classe `Outils`.

### Piège 6 — Génériciser par principe

```java
// ❌ MAUVAIS : un générique pour un usage unique n'apporte rien et complique la lecture
public class BoitePourReclamation<T extends Reclamation> { ... }

// ✅ BON : un type précis, simple à lire
public class BoiteReclamations {
    private final List<Reclamation> contenu = new ArrayList<>();
    // ...
}
```

### Piège 7 — Empiler les paramètres de type « au cas où »

```java
// ❌ ILLISIBLE : trois paramètres dont deux ne servent qu'à un seul appel
public class Triple<A, B, C> { /* ... */ }

// ✅ BON : un type nommé qui dit ce qu'il contient
public record Repartition<T>(T valeur, String quartier, int total) { }
```

---

## Checklist de validation

Avant de passer à la leçon 03, vérifiez que vous savez faire **chacun** de ces points :

- [ ] Expliquer pourquoi les generics déplacent les erreurs **de l'exécution vers la compilation**.
- [ ] Écrire une classe générique `Boite<T>` et l'utiliser avec deux types différents (`String`, `Reclamation`).
- [ ] Distinguer **paramètre de type** (`<T>` dans la déclaration) et **argument de type** (`String` dans `Boite<String>`).
- [ ] Utiliser le **diamant** `<>` et dire ce que le compilateur déduit exactement.
- [ ] Écrire une méthode générique (`<T> T premier(List<T>)`) et une méthode **bornée** (`<T extends Comparable<T>>`).
- [ ] Expliquer ce que garantit la borne `<T extends Comparable<T>>` et pourquoi `Reclamation` (non `Comparable`) serait refusée.
- [ ] Utiliser `List<? extends T>` en **lecture** et `List<? super T>` en **écriture**, et réciter la règle **PECS**.
- [ ] Expliquer l'**effacement de type** et en citer trois conséquences (`new T[]`, `instanceof List<String>`, `List<int>`).
- [ ] Reconnaître un *raw type* et corriger l'avertissement `[rawtypes]`/`[unchecked]`.
- [ ] Dire pourquoi `List<Object> l = new ArrayList<String>();` ne compile pas (invariance).
- [ ] Expliquer pourquoi `T` est interdit dans un membre `static`, et comment le contourner avec `<U>`.
- [ ] Citer au moins deux types génériques de Spring (`ResponseEntity<T>`, `JpaRepository<T, ID>`) et dire ce que représentent leurs paramètres.

---

➡️ **Prochaine étape** : vous savez typer, borner et paramétrer. Il reste **le** problème que la leçon 01 a laissé ouvert : « et si la valeur n'existe pas ? ». Aujourd'hui, `findById(99)` renvoie `null`, et rien n'oblige l'appelant à s'en préoccuper — le programme plantera plus loin, à un endroit sans rapport. La **leçon 03 — Optional** apporte la réponse : un type générique (`Optional<T>` — vous venez d'acquérir le vocabulaire pour le comprendre !) qui **rend l'absence explicite et vérifiée par le compilateur**.

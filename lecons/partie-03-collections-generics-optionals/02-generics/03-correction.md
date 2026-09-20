# Correction détaillée — Exercice 02 « Des outils génériques pour SignalCUA »

> 🧭 **Comment ce fichier s'articule** : vous venez de tenter `02-exercice.md`. Voici la solution complète, les choix expliqués, la **sortie réellement obtenue**, la réponse au bonus « provoquer les erreurs », la checklist et des conseils. Si votre code compile et produit la même sortie, c'est gagné — même si le vôtre est écrit autrement.

## Correction pas à pas

### Étape 1 — La `Boite<T>` complète

```java
// Boite<T> complete, avec valeur par defaut et test de vacuite
public class Boite<T> {

    private T contenu;                                     // null tant que rien n'est mis

    public void mettre(T valeur) {
        this.contenu = valeur;
    }

    public T prendre() {
        return contenu;
    }

    public boolean estVide() {
        return contenu == null;
    }

    public T prendreOuDefaut(T valeurParDefaut) {          // evite de rendre null a l'appelant
        return contenu == null ? valeurParDefaut : contenu;
    }
}
```

**Deux choix à noter :**

- **`estVide()` se base sur `null`** : ici c'est acceptable car la boîte est un conteneur de démonstration. Dans un vrai code, on préférerait un `Optional<T>` interne (leçon 03) : `null` et « vide » sont deux idées qu'il vaut mieux ne pas confondre.
- **`prendreOuDefaut`** applique un principe vu en leçon 01 : ne pas **obliger** l'appelant à gérer un `null`. La décision (« quelle valeur si vide ? ») appartient à l'appelant, mais l'API l'exprime clairement.

### Étape 2 — `Paire<K, V>` et l'inversion

```java
// Record generique : deux types independants K et V
public record Paire<K, V>(K cle, V valeur) {

    // On renvoie une NOUVELLE paire, avec les types croises : Paire<V, K>
    public Paire<V, K> inverser() {
        return new Paire<>(valeur, cle);
    }

    public String affichage() {
        return cle + " = " + valeur;
    }
}
```

**Le point clé de généricité** : la méthode `inverser()` **échange les deux paramètres de type** dans son type de retour — `Paire<V, K>`. C'est une opération impossible à exprimer sans generics (avec des `Object`, le compilateur ne saurait plus rien du résultat). Le `new Paire<>(valeur, cle)` utilise le **diamant** : le compilateur déduit `Paire<V, K>` du type de retour attendu.

### Étape 3 — Les méthodes génériques (`premier`, `maximum`)

```java
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OutilsReclamation {

    // 1. Methode generique : retourne le premier element d'une liste, quel que soit T
    public static <T> T premier(List<T> elements) {
        if (elements.isEmpty()) {
            throw new IllegalArgumentException("La liste est vide");
        }
        return elements.get(0);
    }

    // 2. Methode generique bornee : T doit savoir se comparer aux autres T
    public static <T extends Comparable<T>> T maximum(List<T> elements) {
        if (elements.isEmpty()) {
            throw new IllegalArgumentException("La liste est vide");
        }
        T meilleur = elements.get(0);
        for (T element : elements) {
            if (element.compareTo(meilleur) > 0) {
                meilleur = element;
            }
        }
        return meilleur;
    }
```

**Ce que la borne apporte** : sans `<T extends Comparable<T>>`, la ligne `element.compareTo(meilleur)` ne compilerait pas (« *cannot find symbol: method compareTo(T)* »), car `T` pourrait être n'importe quel type. La borne est donc **la condition qui rend l'implémentation légale** — et, bonus, elle documente la contrainte pour le lecteur.

> 💡 **Pourquoi `Reclamation` serait refusée** : notre classe `Reclamation` n'implémente pas `Comparable<Reclamation>`. `maximum(listeDeReclamations)` ne compilerait donc pas. Vouloir comparer des réclamations est un vrai besoin métier : vous le ferez proprement avec un `Comparator` (leçon 01, section 2.5) — ou en rendant `Reclamation` comparable plus tard, si l'ordre naturel a un sens.

### Étape 4 — Les wildcards (PECS) et l'étape 5 — le comptage générique

```java
    // 3. PECS : source produit (extends), destination consomme (super)
    public static <T> void copierTout(List<? extends T> source, List<? super T> destination) {
        for (T element : source) {
            destination.add(element);
        }
    }

    // 4. Map de comptage generique : la cle peut etre n'importe quel type K
    public static <K> Map<K, Integer> compter(List<K> elements, Map<K, Integer> resultat) {
        for (K element : elements) {
            Integer actuel = resultat.get(element);
            resultat.put(element, actuel == null ? 1 : actuel + 1);
        }
        return resultat;
    }

    // Petite aide d'affichage (utilisee dans le main)
    public static <T> List<T> repeter(T valeur, int fois) {
        List<T> liste = new ArrayList<>();
        for (int i = 0; i < fois; i++) {
            liste.add(valeur);          // ajoute le MEME objet/valeur 'fois' fois
        }
        return liste;
    }
}
```

**Pourquoi `copierTout` accepte-t-elle `List<String>` → `List<Object>` ?** Parce que dans `copierTout(urgentes, journal)`, le compilateur choisit `T = Object` : `List<Reclamation>` est un producteur de `Object` (`? extends Object`) et `List<Object>` un consommateur de `Object` (`? super Object`). Sans les wildcards, il faudrait `T = Reclamation` des deux côtés — donc une `List<Reclamation>` en destination, ce qui ne correspond pas au besoin.

**Le détail qui compte dans `compter`** : `resultat.get(element)` peut renvoyer `null` (clé absente la première fois) ; l'opérateur ternaire fait le travail. La version « moderne » de la leçon 01 (`resultat.merge(element, 1, Integer::sum)`) arrivera en partie 5 avec les lambdas — **les deux sont correctes**, la seconde est plus courte.

### Étape 6 — `Historique<T>`, la classe générique réutilisable

```java
import java.util.ArrayList;
import java.util.List;

// Une classe generique reutilisable : un journal d'elements de type T
public class Historique<T> {

    private final List<T> elements = new ArrayList<>();     // le type interne reste generique

    public void ajouter(T element) {
        elements.add(element);
    }

    public T dernier() {
        if (elements.isEmpty()) {
            throw new IllegalStateException("Historique vide");
        }
        return elements.get(elements.size() - 1);          // List.getLast() existe en Java 21
    }

    public int taille() {
        return elements.size();
    }

    public List<T> tout() {
        return List.copyOf(elements);                      // copie immuable
    }

    public boolean contient(T element) {
        return elements.contains(element);
    }
}
```

**Trois enseignements de cette classe :**

1. **Le conteneur interne est générique** : `Historique<Reclamation>` contient une `List<Reclamation>`, `Historique<String>` une `List<String>` — **le code source est unique**. C'est tout l'intérêt des generics.
2. **`tout()` renvoie `List.copyOf(elements)`** : l'encapsulation vue en leçon 01 (copie défensive) **fonctionne telle quelle** avec les generics — la copie conserve le type `T`.
3. **`dernier()` utilise `elements.get(elements.size() - 1)`** : en Java 21, `elements.getLast()` (issu de `SequencedCollection`) est plus lisible. La version longue reste utile pour comprendre ce qui se passe.

> ⚠️ **Note de conception honnête** : un `Historique<T>` **sans borne** accepte tout, y compris `null`... et `contient(null)` fonctionne (au lieu de lever une exception). Pour un historique d'événements, on ajouterait volontiers une petite validation dans `ajouter` — la partie 4 (gestion des exceptions) vous montrera comment structurer ce refus proprement.

### Étape 7 — Le programme de démonstration

```java
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainGenericsExercice {

    public static void main(String[] args) {

        // ============ 1. Boite<T> : le meme code pour des types differents ============
        Boite<String> boiteTexte = new Boite<>();
        System.out.println("1) boite vide ? " + boiteTexte.estVide());
        boiteTexte.mettre("Nid de poule");
        System.out.println("   contenu : " + boiteTexte.prendre()
                + " | vide ? " + boiteTexte.estVide());

        Boite<Priorite> boitePriorite = new Boite<>();
        System.out.println("   valeur par defaut : " + boitePriorite.prendreOuDefaut(Priorite.NORMALE));

        // ============ 2. Paire<K, V> et son inversion ============
        Paire<String, Integer> parQuartier = new Paire<>("Medina", 3);
        System.out.println("2) " + parQuartier.affichage());
        Paire<Integer, String> inversee = parQuartier.inverser();      // types croises
        System.out.println("   inversee : " + inversee.affichage());
        System.out.println("   cle de l'inversee est un Integer : " + (inversee.cle() + 1));

        // ============ 3. Premier / maximum (methode bornee) ============
        List<String> quartiers = List.of("Plateau", "Medina", "Fann");
        System.out.println("3) premier(quartiers) : " + OutilsReclamation.premier(quartiers));
        System.out.println("   maximum(quartiers) : " + OutilsReclamation.maximum(quartiers));
        System.out.println("   maximum([3, 168, 48]) : "
                + OutilsReclamation.maximum(List.of(3, 168, 48)));

        // ============ 4. PECS : copier d'une liste vers une liste plus "large" ============
        List<Reclamation> urgentes = List.of(
                new Reclamation(1, "Cable arrache", "Medina", Priorite.URGENTE));
        List<Object> journal = new ArrayList<>();
        OutilsReclamation.copierTout(urgentes, journal);   // List<Reclamation> -> List<Object>
        OutilsReclamation.copierTout(List.of("Demarrage"), journal);
        OutilsReclamation.copierTout(List.of(42), journal);
        System.out.println("4) journal : " + journal.size() + " elements de types differents");

        // ============ 5. Map de comptage generique ============
        List<String> visites = List.of("Medina", "Plateau", "Medina", "Medina", "Fann");
        Map<String, Integer> compteurs = new HashMap<>();
        OutilsReclamation.compter(visites, compteurs);
        System.out.println("5) comptage : " + compteurs);
```

        // ============ 6. Historique<T> : une classe generique reutilisable ============
        Historique<Reclamation> historique = new Historique<>();
        historique.ajouter(new Reclamation(2, "Lampadaire eteint", "Plateau", Priorite.URGENTE));
        historique.ajouter(new Reclamation(3, "Poubelles", "Medina", Priorite.BASSE));
        System.out.println("6) historique : " + historique.taille() + " element(s), dernier = #"
                + historique.dernier().getId());
        Historique<String> historiqueTexte = new Historique<>();      // meme classe, autre type
        historiqueTexte.ajouter("Changement de statut");
        System.out.println("   second historique (String) : " + historiqueTexte.dernier());

        // ============ 7. Erasure : l'information de type disparait a l'execution ============
        List<String> a = new ArrayList<>();
        List<Reclamation> b = new ArrayList<>();
        System.out.println("7) List<String> et List<Reclamation> = meme classe runtime : "
                + (a.getClass() == b.getClass()));
        System.out.println("   donc ceci ne compile PAS : o instanceof List<String>");
    }
}
```

## Vérification par exécution

```bash
javac -encoding UTF-8 *.java      # ajoutez -Xlint:all : ici, AUCUN avertissement ne doit sortir
java MainGenericsExercice
```

**Résultat obtenu (Java 21.0.7), compilation sans erreur ni avertissement :**

```text
1) boite vide ? true
   contenu : Nid de poule | vide ? false
   valeur par defaut : NORMALE
2) Medina = 3
   inversee : 3 = Medina
   cle de l'inversee est un Integer : 4
3) premier(quartiers) : Plateau
   maximum(quartiers) : Plateau
   maximum([3, 168, 48]) : 168
4) journal : 3 elements de types differents
5) comptage : {Plateau=1, Medina=3, Fann=1}
6) historique : 2 element(s), dernier = #3
   second historique (String) : Changement de statut
7) List<String> et List<Reclamation> = meme classe runtime : true
   donc ceci ne compile PAS : o instanceof List<String>
```

**Lecture ligne par ligne :**

| Ligne | Ce qui est prouvé |
|---|---|
| 1 | `Boite<T>` fonctionne pour `String`, et `prendreOuDefaut` rend `NORMALE` sur une boîte `Priorite` vide (aucun `cast`). |
| 2 | `inverser()` a bien croisé les types : `cle()` est un `Integer` (sinon `+ 1` refuserait de compiler). |
| 3 | `premier` **et** `maximum` fonctionnent sur des `String` **et** des `Integer` avec le **même** code (borne `Comparable`). |
| 4 | **PECS** : trois types distincts copiés dans un même `List<Object>` grâce à `? extends` / `? super`. |
| 5 | Le comptage générique donne `Medina=3` : `<K>` fonctionne pour toute clé. |
| 6 | `Historique<T>` sert pour `Reclamation` **et** pour `String` : zéro duplication. |
| 7 | **Effacement** : `List<String>` et `List<Reclamation>` sont la même classe, donc `instanceof List<String>` est impossible. |

> ℹ️ **Un détail observé et instructif** : dans la ligne 3, `maximum` sur `["Plateau", "Medina", "Fann"]` renvoie `Plateau`. Contre-intuitif ? Non : l'ordre naturel des `String` est **alphabétique**, et `"Plateau"` vient après `"Medina"` et `"Fann"` (P > M > F). C'est exactement le genre de vérification mentale qui évite 90 % des bugs de tri.

## Réponse au bonus — les trois erreurs, telles que `javac` les affiche

Les messages ci-dessous sont **réellement obtenus** en compilant un fichier de test (Java 21) :

```text
ErreursGenerics.java:9: error: generic array creation            (cas d'un tableau générique)
ErreursGenerics.java:13: error: Object cannot be safely cast to List<String>
ErreursGenerics.java:21: error: unexpected type
        List<int> nombres = new ArrayList<>();
             ^
  required: reference
  found:    int
```

**Explication de chacune (à savoir restituer) :**

| Erreur | Message exact | Cause | Type d'erreur |
|---|---|---|---|
| `o instanceof List<String>` | `Object cannot be safely cast to List<String>` | effacement de type : à l'exécution, `List<String>` **n'existe pas** | **compilation** |
| `new T[10]` | `generic array creation` | un tableau a besoin de son type au runtime, effacé pour `T` | **compilation** |
| `List<int>` | `unexpected type … required: reference, found: int` | un paramètre de type doit être une **référence** (classe enveloppe) | **compilation** |
| `List<Object> l = new ArrayList<String>();` | `incompatible types: ArrayList<String> cannot be converted to List<Object>` | **invariance** des generics | **compilation** |

**La bonne conclusion** : ces quatre erreurs sont **toutes** détectées par le compilateur — c'est précisément le bénéfice des generics. Les deux travaux pratiques les plus formateurs : essayer de **contourner** chacune d'elles, et constater que le contournement (`raw type`) déplace le problème vers une `ClassCastException` d'exécution.

## Erreurs fréquentes et comment les reconnaître

| Message / symptôme | Cause | Correction |
|---|---|---|
| `cannot find symbol: class T` | `<T>` non déclaré sur la méthode | écrire `public static <T> T maMethode(...)` |
| `cannot find symbol: method compareTo(T)` | borne manquante | `<T extends Comparable<T>>` |
| `[rawtypes] found raw type` | `List` écrit sans `<>` | typer : `List<String>` |
| `[unchecked] unchecked call to add(E)` | appel de méthode sur un *raw type* | supprimer le *raw type* (à sa source) |
| `name clash ... have the same erasure` | deux surcharges identiques après effacement | renommer l'une des deux méthodes |
| `non-static type variable T cannot be referenced from a static context` | `T` de la classe utilisé dans un `static` | redéclarer `<U>` sur la méthode statique |
| `incompatible types: ... CAP#1` | écriture dans un `? extends` | utiliser `? super` (PECS) |
| `ClassCastException` à l'exécution | un *raw type* a laissé passer un mauvais type | typer partout, ne jamais ignorer `-Xlint` |

## Checklist de validation

Reprenez chaque point **sur votre code** :

- [ ] `Boite<T>` compile et sert avec **trois** types différents, sans aucun `cast`.
- [ ] `Paire<V, K> inverser()` renvoie les types croisés, prouvé par un appel qui n'aurait pas compilé sinon (`.cle() + 1`).
- [ ] `premier` et `maximum` sont **génériques** (le même code sert pour `String` et `Integer`) et refusent une liste vide.
- [ ] `maximum` est **borné** par `Comparable` et je sais expliquer pourquoi la borne est nécessaire.
- [ ] `copierTout` utilise `? extends` en source et `? super` en destination (PECS), prouvé par la copie de trois types dans un `List<Object>`.
- [ ] `compter` renvoie `Medina=3`, avec la clé typée par `<K>`.
- [ ] `Historique<T>` est une **classe générique** utilisée avec `Reclamation` puis `String`, sans duplication de code.
- [ ] Je distingue **paramètre** et **argument** de type, et j'explique le **diamant** `<>`.
- [ ] Je reproduis les **trois erreurs de compilation** du bonus et je les relie à l'**effacement** ou à l'**invariance**.
- [ ] `javac -Xlint:all` sur mon dossier ne produit **aucun** avertissement.
- [ ] J'explique ce que deviendront ces notions en partie 7-8 (`ResponseEntity<T>`, `JpaRepository<T, ID>`, `Optional<T>`).

## Conseils pour progresser

1. **Gardez `Historique<T>`** : c'est le premier composant *réutilisable* que vous écrivez vous-même. Un journal d'événements est un besoin récurrent (audit, débogage) et vous le retrouverez en partie 11 (journalisation).
2. **Relisez vos signatures de méthodes en cherchant le bon wildcard** : à chaque fois que vous écrivez un paramètre `List<MonType>`, demandez-vous « *est-ce que j'aurais dû accepter les sous-types ?* ». Si oui → `? extends`. Même méthode pour `? super`.
3. **Entraînez-vous à lire les messages `CAP#1`** : ils font peur mais disent simplement « *le type est inconnu, donc je refuse* ». Retenez qu'ils apparaissent presque toujours en lien avec PECS.
4. **`-Xlint:all` doit devenir un réflexe** : en partie 6 (Maven/Gradle), vous le mettrez dans la configuration du build, avec `-Werror` pour que les avertissements **bloquent** la construction. Le code qui passera sera alors garanti sans *raw type*.
5. **Anticipez le trio générique à venir** : `Optional<T>` (leçon 03) est un type générique qui porte soit **une** valeur de type `T`, soit **rien**. Presque tout ce que vous venez d'apprendre (`<T>`, bornes, `record`, `List.copyOf`) s'y appliquera directement.

➡️ **Suite de votre parcours** : vous savez désormais écrire et lire du code générique. Le dernier manque du fil rouge est l'absence de valeur : `findById(99)` renvoie `null`, et rien n'oblige à traiter ce cas. La **leçon 03 — Optional** en fait un type à part entière, vérifié par le compilateur, et vous ouvre le motif `findById` que vous utiliserez dans Spring Boot.

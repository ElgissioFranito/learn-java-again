# Correction détaillée — Exercice 01 « Importer des réclamations depuis un fichier »

> 🧭 **Comment ce fichier s'articule** : vous venez de tenter `02-exercice.md`. Voici la solution complète, les choix expliqués, la **sortie réellement obtenue** (Java 21), les erreurs fréquentes, la checklist et des conseils. Si votre programme produit la même sortie, l'objectif est atteint.

## Correction pas a pas

### Les fichiers du projet

Cinq fichiers, dans le même dossier :

| Fichier | Rôle |
|---|---|
| `Priorite.java` | l'`enum` des priorités |
| `Reclamation.java` | la donnée immuable |
| `RapportImport.java` | le `record` qui transporte le résultat de l'import |
| `ImportateurReclamations.java` | **le cœur** : lecture + analyse + erreurs |
| `MainImport.java` | la démonstration exécutable |

### Étape 1 — Le modèle (rappel partie 2)

```java
// Priorite.java — un enum borne l'ensemble des valeurs valides.
public enum Priorite {
    URGENTE, HAUTE, NORMALE, BASSE
}
```

```java
// Reclamation.java — champs final : une réclamation ne change pas après création.
public class Reclamation {

    private final int id;
    private final String quartier;
    private final Priorite priorite;
    private final String description;

    public Reclamation(int id, String quartier, Priorite priorite, String description) {
        this.id = id;
        this.quartier = quartier;
        this.priorite = priorite;
        this.description = description;
    }

    public int getId() { return id; }
    public String getQuartier() { return quartier; }
    public Priorite getPriorite() { return priorite; }
    public String getDescription() { return description; }

    @Override
    public String toString() {
        return "#" + id + " [" + priorite + "] " + quartier + " : " + description;
    }
}
```

### Étape 2 — Le rapport d'import

```java
// RapportImport.java — un record transporte deux listes : les valides et les messages d'erreur.
import java.util.List;

public record RapportImport(List<Reclamation> valides, List<String> erreurs) {
}
```

**Choix technique** : on ne s'arrête **pas** à la première ligne invalide. On **collecte** les erreurs pour tout montrer à l'utilisateur d'un coup. Un rapport est plus utile qu'un arrêt brutal.

### Étape 3 — L'importateur (le cœur)

```java
// ImportateurReclamations.java
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

// `throws IOException` : exception CHECKED -> le compilateur impose de la gerer.
public class ImportateurReclamations {

    public RapportImport importer(Path chemin) throws IOException {
        List<Reclamation> valides = new ArrayList<>();
        List<String> erreurs = new ArrayList<>();

        // try-with-resources : le BufferedReader est ferme automatiquement (meme en cas d'erreur).
        try (BufferedReader lecteur = Files.newBufferedReader(chemin, StandardCharsets.UTF_8)) {
            String ligne;
            int numero = 0;
            while ((ligne = lecteur.readLine()) != null) {
                numero++;
                if (ligne.isBlank()) {
                    continue; // une ligne vide n'est pas une erreur : on l'ignore
                }
                try {
                    valides.add(analyser(valides.size() + 1, numero, ligne));
                } catch (IllegalArgumentException e) {
                    erreurs.add(e.getMessage()); // erreur de DONNÉE : on note et on continue
                }
            }
        }
        return new RapportImport(valides, erreurs);
    }

    private Reclamation analyser(int id, int numero, String ligne) {
        String[] morceaux = ligne.split(";");
        if (morceaux.length != 3) {
            throw new IllegalArgumentException(
                    "Ligne " + numero + " : 3 champs attendus (quartier;priorite;description), trouve " + morceaux.length);
        }
        String quartier = morceaux[0].trim();
        String prioriteTexte = morceaux[1].trim().toUpperCase();
        String description = morceaux[2].trim();

        Priorite priorite;
        try {
            priorite = Priorite.valueOf(prioriteTexte);
        } catch (IllegalArgumentException cause) {
            throw new IllegalArgumentException(
                    "Ligne " + numero + " : priorite inconnue \"" + prioriteTexte + "\"", cause); // on garde la cause
        }
        return new Reclamation(id, quartier, priorite, description);
    }
}
```

**Les choix techniques de cet extrait :**

1. **`throws IOException` sur `importer`** : `Files.newBufferedReader` peut lever une exception *checked*. On a le choix : la capturer ici, ou la **déclarer**. On choisit de la **déclarer** — c'est le niveau « au-dessus » (le `main`) qui sait quoi afficher à l'utilisateur.
2. **`try-with-resources`** : le `BufferedReader` est fermé **automatiquement**, même si `analyser` lève une exception. Aucun `finally` à écrire, aucune fuite possible.
3. **`analyser` séparée de la boucle** : une méthode = une responsabilité. `analyser` **lève** ; `importer` **capture et continue**. Cette séparation rend le code lisible et testable (partie 9).
4. **Deux messages d'erreur distincts** : « 3 champs attendus » (structure) et « priorité inconnue » (valeur). Un bon message dit **quoi** et **où** (le numéro de ligne).
5. **`catch (IllegalArgumentException cause)` + relance avec `cause`** : on **transforme** une erreur peu explicite (`No enum constant Priorite.INCONNUE`) en un message **métier** (« priorité inconnue INCONNUE »), tout en **conservant** la cause pour le diagnostic.

### Étape 4 — Le `main` de démonstration

```java
// MainImport.java
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

public class MainImport {

    public static void main(String[] args) throws IOException {

        // 1) Un fichier d'exemple (texte brut en trois blocs, comme vu en partie 1).
        Path fichier = Path.of("reclamations.txt");
        String contenu = """
                Medina;URGENTE;Nid de poule dangereux
                Plateau;normale;Poubelles non ramassees
                Medina;INCONNUE;Lampadaire eteint
                Fann;HAUTE;Fuite d'eau
                ligne mal formee sans separateur
                """;
        Files.writeString(fichier, contenu, StandardCharsets.UTF_8);

        // 2) Import nominal
        ImportateurReclamations importateur = new ImportateurReclamations();
        RapportImport rapport = importateur.importer(fichier);

        System.out.println("== Import de " + fichier + " ==");
        System.out.println("Valides (" + rapport.valides().size() + ") :");
        rapport.valides().forEach(r -> System.out.println("   " + r));
        System.out.println("Erreurs (" + rapport.erreurs().size() + ") :");
        rapport.erreurs().forEach(e -> System.out.println("   - " + e));

        // 3) Fichier absent : exception CHECKED (doit etre geree)
        System.out.println();
        System.out.println("== Import d'un fichier absent ==");
        try {
            importateur.importer(Path.of("inexistant.txt"));
        } catch (NoSuchFileException e) {
            System.out.println("Fichier introuvable : " + e.getFile());
        }
    }
}
```

### Étape 5 (bonus) — Le piège de l'ordre des `catch`

```java
// ✅ CORRECT : du plus précis au plus general
try {
    importateur.importer(chemin);
} catch (NoSuchFileException e) {          // la FILLE d'abord
    System.out.println("Fichier absent : " + e.getFile());
} catch (IOException e) {                   // la MERE ensuite
    System.out.println("Autre erreur d'E/S : " + e.getMessage());
}

// ❌ INCORRECT (ne compile pas) : si on INVERSE...
// try { ... }
// catch (IOException e) { ... }          // la mère capture DÉJÀ la fille
// catch (NoSuchFileException e) { ... }  // "exception NoSuchFileException has already been caught"
```

**Explication** : `NoSuchFileException` est une fille d'`IOException`. Quand la mère est capturée en premier, tous ses types sont déjà couverts : le `catch` de la fille devient **inatteignable**, et le compilateur refuse avec le message **`has already been caught`**. C'est une **protection** : elle vous force à ordonner du précis vers le général.

## Verification par exécution

```bash
javac -encoding UTF-8 *.java      # Priorite, Reclamation, RapportImport, ImportateurReclamations, MainImport
java MainImport
```

**Résultat obtenu (Java 21.0.7), aucune erreur à la compilation :**

```text
== Import de reclamations.txt ==
Valides (3) :
   #1 [URGENTE] Medina : Nid de poule dangereux
   #2 [NORMALE] Plateau : Poubelles non ramassees
   #3 [HAUTE] Fann : Fuite d'eau
Erreurs (2) :
   - Ligne 3 : priorite inconnue "INCONNUE"
   - Ligne 5 : 3 champs attendus (quartier;priorite;description), trouve 1

== Import d'un fichier absent ==
Fichier introuvable : inexistant.txt
```

**Lecture ligne par ligne :**

| # | Ce qui est prouve |
|---|---|
| `#1 [URGENTE]` et `#3 [HAUTE]` (lignes 1 et 4) | Les lignes valides sont **conservées** ; la ligne vide éventuelle serait ignorée. |
| `#2 [NORMALE]` | `normale` en minuscules a été **normalisé** grâce à `.toUpperCase()` puis `Priorite.valueOf`. |
| `Erreurs (2)` avec **deux messages distincts** | La ligne 3 (priorité `INCONNUE`) ET la ligne 5 (mauvais nombre de champs) sont **toutes deux** signalées : l'import **n'a pas été interrompu** par la première erreur. |
| `Fichier introuvable : inexistant.txt` | L'`IOException` **checked** a bien été **gérée** (message lisible) au lieu de faire planter le programme. |

> ℹ️ **Remarque sur les identifiants** : `#1`, `#2`, `#3` sont numérotés **dans l'ordre des lignes valides** (`valides.size() + 1`), pas selon le numéro de ligne du fichier. La ligne invalide (n° 5) ne consomme donc pas d'identifiant. C'est un choix d'affichage, pas de modèle.

## Erreurs frequentes et comment les reconnaitre

| Message / symptôme | Cause | Correction |
|---|---|---|
| `unreported exception IOException; must be caught or declared to be thrown` | vous appelez une méthode checked sans `try` ni `throws` | ajouter `throws IOException` (ou capturer) |
| `cannot find symbol: method readLine()` | le type de la variable n'est pas un `BufferedReader` | vérifier l'import `java.io.BufferedReader` |
| La ligne vide produit une erreur | vous analysez les lignes vides | tester `ligne.isBlank()` **avant** `analyser` |
| `java.util.NoSuchElementException: No line found` | mélange avec `Scanner` (partie 1) au lieu de `readLine()` | utiliser `readLine()` (+ test `!= null`) |
| Toutes les lignes partent en erreur après la première | le `try/catch` est **autour** de la boucle au lieu d'**être dedans** | déplacer le `catch` **à l'intérieur** de la boucle |
| `No enum constant Priorite.INCONNUE` affiché brut | vous n'avez pas transformé l'exception | relancer avec un message métier + `cause` |
| `exception FileNotFoundException has already been caught` | ordre des `catch` inversé | mettre la fille **avant** la mère |
| `Le fichier reste verrouillé / connexion épuisée` | ressource non fermée (pas de `try-with-resources`) | envelopper la lecture dans `try (...)` |

## Checklist de validation

Reprenez chaque point **sur votre code** :

- [ ] `importer` déclare `throws IOException` (vérifié dans la signature).
- [ ] La lecture se fait dans un `try-with-resources` ; aucun `close()` manuel n'apparaît.
- [ ] Une ligne vide **ne produit pas** d'erreur.
- [ ] Le fichier de sortie montre **3 valides** et **2 erreurs** (comme la sortie ci-dessus).
- [ ] Le `catch` est **dans** la boucle : l'erreur d'une ligne n'empêche pas d'analyser les suivantes.
- [ ] Le message de priorité inconnue **conserve la cause** (`new IllegalArgumentException(msg, cause)`).
- [ ] Le fichier absent produit un **message lisible** (pas de stack trace brut en sortie).
- [ ] Je sais dire pourquoi `NoSuchFileException` doit être capturé **avant** `IOException`.

## Conseils pour progresser

1. **Ajoutez un champ de plus au fichier** (par exemple la date) et observez le nombre de `catch` à ajuster : vous verrez nettement la frontière « données » (unchecked) vs « I/O » (checked).
2. **Remplacez le message d'erreur par un logger** : `log.error(...)` au lieu de `erreurs.add(...)`. C'est exactement le saut qu'on fera en **partie 11**. Les erreurs de données sont aussi importantes que les erreurs techniques.
3. **Testez la « ligne vide » et la « fin de fichier »** : ce sont les deux cas limites les plus oubliés. En partie 9, `assertThrows(IOException.class, ...)` vérifiera le fichier absent.
4. **Garde-fou de cohérence** : notez que `NoSuchFileException` est **une** des nombreuses filles d'`IOException`. En capturant la fille avant la mère, vous traitez le cas fréquent spécifiquement et le reste génériquement — un motif à réutiliser partout.

➡️ **Suite de votre parcours** : SignalCUA sait importer sans se planter. Mais les erreurs y restent **anonymes** (`IllegalArgumentException`). La **leçon 02 — Exceptions métier custom** leur donne un **nom** et un **contexte**, et pose la hiérarchie d'exceptions que la partie 7 traduira en codes HTTP.

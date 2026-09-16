# Leçon 03 — Tableaux et Strings

> 🧭 **Pont depuis la leçon 02** : vous savez prendre des décisions et répéter des actions. Mais vos boucles de la leçon 02 parcouraient des tableaux créés « comme ça » : il est temps de les comprendre en profondeur — comment ils sont construits, leurs limites (taille fixe !), et pourquoi en pratique moderne on leur préfère souvent l'`ArrayList`. D'autre part, vos variables `String` ne faisaient qu'être affichées : cette leçon vous apprend à les **découper, comparer, formater**. Deux sujets en un, car ils partagent un point crucial : ce sont tous les deux des **objets**, avec les pièges que cela implique (références, `==` vs `.equals()` — rappel de la leçon 01).

---

## 1. Objectifs d'apprentissage

À la fin de cette leçon, vous saurez :

- Créer et parcourir un tableau à taille fixe, et connaître sa limite principale.
- Remplacer un tableau par un `ArrayList` quand la taille doit changer.
- Manipuler une `String` : `.length()`, `.substring()`, `.split()`, `.trim()`, `.contains()`, `.format()`/`.formatted()`.
- Construire une chaîne dans une boucle avec `StringBuilder` (et savoir pourquoi).
- Distinguer ce qu'il faut comparer avec `==` et ce qu'il faut comparer avec `.equals()`.

---

## 2. Explication simple

### 2.1 Le tableau : une boîte à compartiments fixe

**Pourquoi ?** Vous avez plusieurs réclamations : vous voulez les ranger ensemble sous un seul nom, pas créer `reclamation1`, `reclamation2`, `reclamation3`…

**Comment ?** Un tableau est comme une **placard à cases numérotées** : un seul placard (`tab`), plusieurs cases (les éléments), chaque case portant un numéro (l'**index**, qui commence à **0**).

```java
// Création : deux façons
int[] scores = new int[3];          // 3 cases vides (remplies de 0 par défaut)
int[] jours = {10, 5, 8};           // 3 cases remplies directement

// Lecture / écriture par index
System.out.println(jours[0]);       // 10 (la PREMIÈRE case est l'index 0 !)
jours[1] = 6;                       // on remplace la 2e case
```

**La grande limite : la taille est figée à la création.** Un placard à 3 cases restera à 3 cases pour toujours : impossible d'en ajouter une 4e. Si la 4e réclamation arrive… il faut reconstruire un tableau plus grand et tout recopier. Fastidieux.

**Quand ?** Tableau quand la quantité est connue et fixe (les 7 jours de la semaine). Pour tout le reste → `ArrayList` (ci-dessous).

> 💡 On a déjà croisé cette limite en leçon 02 (le « tableau parallèle » fragile des quartiers) : même si la taille est fixe, rien ne garantit que les deux tableaux restent synchronisés. Gardons ce souvenir, il motive la leçon 04.

### 2.2 L'ArrayList : une boîte qui grandit toute seule

**Pourquoi ?** 95 % du temps réel, on ne connaît pas la quantité à l'avance : les réclamations arrivent au fil de l'eau.

**Comment ?** L'`ArrayList` (fournie avec Java) est un tableau « vivant » : elle s'agrandit quand on ajoute, se réduit quand on retire. **Analogie** : une chaîne de wagons à laquelle on attache et détache des wagons librement.

```java
// import nécessaire : ArrayList vit dans java.util
import java.util.ArrayList;

ArrayList<String> quartiers = new ArrayList<>();
quartiers.add("Medina");            // ajoute à la fin
quartiers.add("Plateau");
quartiers.add("Grand Yoff");

System.out.println(quartiers.size());      // 3 (notez : size(), pas length !)
System.out.println(quartiers.get(1));      // Plateau (lire l'index 1)
quartiers.remove("Plateau");               // retire un élément
System.out.println(quartiers.contains("Medina")); // true
```

⚠️ Détail qui surprend : l'`ArrayList` ne peut contenir que des **objets**, pas des primitifs. `ArrayList<int>` ne compile pas → on écrit `ArrayList<Integer>` et l'auto-boxing (leçon 01) fait le reste en silence.

**Quand ?** Dès que la quantité varie. La roadmap est claire : tableau → cas rares ; `ArrayList` → 95 % des cas réels (les collections avancées arrivent en partie 3).

### 2.3 La String : un texte immuable

**Pourquoi ?** Comprendre la `String`, c'est comprendre un objet très particulier : elle est **imuable** (on dit « immuable » ou *immutable*) : on ne peut JAMAIS modifier son contenu, seulement en créer une nouvelle.

**Analogie** : une `String` est comme un texte **imprimé sur une pierre gravée**. Vous ne pouvez pas gratter et réécrire un mot — vous pouvez seulement graver une nouvelle pierre. Chaque « modification » fabrique donc une nouvelle pierre (un nouvel objet en mémoire) :

```java
String nom = "Awa";
String majuscule = nom.toUpperCase(); // nom est INTACT ; majuscule est un NOUVEL objet "AWA"
```

**Conséquence directe (le piège n°1)** : comparer des `String` avec `==` compare les pierres (les références), pas les gravures (le contenu). Toujours `.equals()` pour le contenu — rappel de la leçon 01.

**Les méthodes à connaître** (elles renvoient toutes des NOUVELLES chaînes, elles ne modifient jamais l'originale) :

```java
String r = "  Reclamation-Voirie-42  ";

r.trim()                    // "Reclamation-Voirie-42" : coupe les espaces aux extrémités
r.length()                  // longueur du texte (avec les parenthèses, contrairement aux tableaux !)
r.contains("Voirie")        // true
r.startsWith("  Rec")       // true
r.indexOf("-")              // 13 (position de la première occurrence, -1 si absent)
r.substring(15)             // "oirie-42  " : à partir de l'index 15
r.split("-")                // tableau {"  Reclamation", "Voirie", "42  "}
r.replace("-", " ")         // "  Reclamation Voirie 42  "
```

⚠️ Piège annoncé par la roadmap : `.split()` découpe selon une **regex** (expression régulière — un mini-langage de motifs). Pour un simple `-` c'est transparent, mais des caractères comme `.` ou `|` ont une signification spéciale en regex (ex. `split("\\.")` avec deux antislashs pour découper sur un point). Retenez : si un `split` se comporte bizarrement, pensez regex.

Et `.equals()` est sensible à la casse (majuscule/minuscule) : pour comparer des emails par exemple, préférez `.equalsIgnoreCase()`.

### 2.4 StringBuilder : construire un texte sans casser les pierres

**Pourquoi ?** Si chaque `+` sur une `String` grave une nouvelle pierre, concaténer dans une boucle de 1 000 tours fabrique 1 000 pierres : du gaspillage (complexité O(n²) — le coût explose quand la taille grandit).

**Comment ?** `StringBuilder` est un **brouillon effaçable** : on accumule dedans, et on ne grave la pierre qu'à la fin :

```java
// ❌ dans une boucle : une nouvelle String à chaque tour
String rapport = "";
for (int i = 1; i <= 1000; i++) {
    rapport = rapport + "ligne " + i + "\n"; // 1000 nouvelles pierres !
}

// ✅ un seul brouillon, une seule gravure finale
StringBuilder sb = new StringBuilder();
for (int i = 1; i <= 1000; i++) {
    sb.append("ligne ").append(i).append("\n"); // modifie le brouillon, pas de copie
}
String rapportOk = sb.toString(); // la gravure finale, une seule fois
```

**Quand ?** Dès qu'on concatène dans une boucle. Pour 2-3 morceaux hors boucle, le `+` reste parfaitement lisible et correct.

### 2.5 Formater : `.formatted()` et text blocks

```java
// .formatted() (Java 15+) : remplace les valeurs dans des "%s" (texte), "%d" (entier), "%.2f" (décimal à 2 chiffres)
String fiche = "Quartier: %s | Reclamations: %d | Taux: %.2f%%"
        .formatted("Medina", 12, 82.5);
System.out.println(fiche); // Quartier: Medina | Reclamations: 12 | Taux: 82.50%

// Text block (leçon 01) + formatted : idéal pour un gabarit multi-lignes
String rapport = """
        RAPPORT MENSUEL
        ---------------
        Quartier : %s
        Total    : %d
        """.formatted("Medina", 12);
```

**Quand ?** Dès qu'un texte mélange des valeurs et du texte fixe : plus lisible que `"Quartier: " + q + " | Total: " + t + "..."`.

---

## 3. Exemples concrets

Créez un fichier `ExempleTableauxStrings.java` (exécution dans `02-exercice.md`) :

```java
import java.util.ArrayList; // OBLIGATOIRE pour utiliser ArrayList

public class ExempleTableauxStrings {
    public static void main(String[] args) {
        // ----- 1. Tableau à taille fixe -----
        String[] quartiersFixe = {"Medina", "Plateau", "Grand Yoff"};
        System.out.println("Taille : " + quartiersFixe.length); // 3, SANS parenthèses
        // quartiersFixe[3] = "Fann"; // ❌ planterait : l'index max est 2

        // ----- 2. ArrayList : la taille bouge -----
        ArrayList<String> quartiers = new ArrayList<>();
        quartiers.add("Medina");
        quartiers.add("Plateau");
        quartiers.add("Grand Yoff");
        quartiers.add("Fann");              // pas de "taille dépassée" : ça grandit
        System.out.println("Taille : " + quartiers.size()); // 4, AVEC parenthèses

        for (String q : quartiers) {        // le for-each marche aussi ici
            System.out.println("Quartier : " + q);
        }

        // ----- 3. Manipulation de String -----
        String brut = "  Reclamation-Voirie-Grand-Yoff  ";
        String propre = brut.trim();                 // on retire les espaces extérieurs
        System.out.println("[" + propre + "]");
        System.out.println("Longueur : " + propre.length());
        String[] morceaux = propre.split("-");       // découpe selon le tiret
        for (String morceau : morceaux) {
            System.out.println("Morceau : " + morceau);
        }
        System.out.println(propre.toUpperCase());    // TOUT en majuscules (nouvelle String)

        // ----- 4. StringBuilder : le rapport des quartiers -----
        StringBuilder sb = new StringBuilder();
        sb.append("RAPPORT\n");
        sb.append("-------\n");
        for (String q : quartiers) {
            sb.append("- ").append(q).append("\n");  // accumule sans créer de String
        }
        System.out.println(sb);

        // ----- 5. formatted + text block -----
        int total = quartiers.size();
        String resume = """
                Resume : %d quartiers suivis.
                Premier : %s
                """.formatted(total, quartiers.get(0));
        System.out.println(resume);
    }
}
```

---

## 4. Bonnes pratiques modernes (2025-2026)

- **`ArrayList` par défaut**, tableau seulement pour des tailles fixes et connues (7 jours, 12 mois…).
- **`StringBuilder`** pour toute concaténation dans une boucle ; `+` toléré hors boucle pour 2-3 morceaux.
- **`.formatted()`** plutôt que `String.format()` (plus lisible, Java 15+).
- **Text blocks `"""`** pour le multi-lignes (SQL, JSON de test) — jamais de `+ "\n" +`.
- **`.trim()` systématique sur les saisies utilisateur** avant traitement («  Medina  » avec espaces est une autre String pour `.equals()` !).
- **Ne parsez pas de CSV/JSON « à la main » avec `split()`** : les vrais formats ont des cas tordus (guillemets, virgules dans les valeurs…). On utilisera des bibliothèques dédiées (OpenCSV, Jackson) en partie 6+. `split()` est pour des formats simples que VOUS contrôlez.
- **`equalsIgnoreCase()`** pour les comparaisons insensibles à la casse (emails, codes saisis par l'utilisateur).

## 5. Pièges à éviter

### Piège 1 — Concaténer avec `+` dans une boucle
```java
// ❌ MAUVAIS : 10 000 lignes = 10 000 String jetables (coût O(n²))
String csv = "";
for (String ligne : lignes) {
    csv = csv + ligne + "\n";
}

// ✅ BON : un brouillon, une gravure
StringBuilder sb = new StringBuilder();
for (String ligne : lignes) {
    sb.append(ligne).append("\n");
}
String csvOk = sb.toString();
```

### Piège 2 — Croire qu'une méthode String modifie l'originale
```java
// ❌ MAUVAIS : le résultat de trim() est JETÉ, brut garde ses espaces
String brut = "  Medina  ";
brut.trim();
System.out.println(brut.equals("Medina")); // false !

// ✅ BON : une String étant immuable, on RÉCUPÈRE le résultat
String propre = brut.trim();
System.out.println(propre.equals("Medina")); // true
```

### Piège 3 — `length` (tableau) vs `length()` (String) vs `size()` (ArrayList)
```java
int[] tab = {1, 2, 3};
String txt = "abc";
var liste = new ArrayList<String>();
liste.add("x");
System.out.println(tab.length);   // 3  : champ, sans parenthèses
System.out.println(txt.length()); // 3  : méthode, avec parenthèses
System.out.println(liste.size()); // 1  : méthode, nom différent
// Trois façons de demander « quelle taille ? » — incohérence historique de Java,
// à savoir par cœur (le compilateur vous rappellera à l'ordre, ne vous inquiétez pas).
```

### Piège 4 — Index de début inclus, fin exclue
```java
String ref = "REC-2026-042";
System.out.println(ref.substring(4, 8)); // "2026" : l'index 4 est inclus, le 8 exclu
// Règle Java générale : les bornes sont [début, fin) — fin toujours exclue.
```

### Anti-pattern — parser du CSV sérieux avec split()
```java
// ❌ MAUVAIS : casse sur une valeur contenant une virgule ou un guillemet
String[] champs = ligneCsv.split(","); // "Medina, Rue 5" -> 2 morceaux au lieu d'1 !

// ✅ BON : une bibliothèque dédiée (OpenCSV, Jackson) dès que le format est réel.
// Pour l'instant : split() uniquement sur des formats simples que vous maîtrisez.
```

---

## 📖 Vocabulaire / Abréviations

| Terme | Définition en une ligne |
|---|---|
| **Tableau (array)** | Ensemble de cases numérotées de taille FIXE, toutes du même type. |
| **Index** | Numéro d'une case, à partir de **0** (dernier index = taille − 1). |
| **`.length`** | Taille d'un tableau (champ, sans parenthèses). |
| **`.length()`** | Longueur d'une `String` (méthode, avec parenthèses). |
| **`.size()`** | Nombre d'éléments d'une `ArrayList` (méthode). |
| **`ArrayList`** | Liste qui grandit/rétrécit dynamiquement (classe de `java.util`). |
| **Générique `<T>`** | Le `<String>` de `ArrayList<String>` : précise le type contenu (voir partie 3). |
| **Immuable (immutable)** | Qui ne peut pas être modifié après création ; toute « modification » crée un nouvel objet. |
| **Regex (expression régulière)** | Mini-langage de motifs utilisé par `.split()` ; certains caractères (`.`, `|`) y sont spéciaux. |
| **`StringBuilder`** | Objet « brouillon » pour accumuler du texte efficacement, gravé une seule fois via `toString()`. |
| **`.append()`** | Ajoute du texte au bout du `StringBuilder`. |
| **Concaténation** | Coller des textes bout à bout (`+`, `append`…). |
| **`.formatted()`** | Méthode (Java 15+) qui remplace `%s`, `%d`, `%.2f`… par des valeurs dans un gabarit. |
| **`%s / %d / %.2f`** | Emplacements de format : texte / entier / décimal à 2 chiffres. |
| **Complexité O(n²)** | Notation « le coût croît comme le carré de la taille » : double la taille = 4× le travail. |

## Checklist de validation

Avant de passer à la leçon 04, vérifiez que vous savez :

- [ ] Créer un tableau de deux façons et expliquer pourquoi sa taille est figée.
- [ ] Ajouter, lire, retirer, tester la présence d'un élément dans une `ArrayList`.
- [ ] Savoir quand on écrit `.length`, `.length()` ou `.size()`.
- [ ] Expliquer l'immuabilité d'une `String` (la pierre gravée) et ses conséquences.
- [ ] Découper une chaîne avec `.split()` et la nettoyer avec `.trim()`.
- [ ] Utiliser `StringBuilder` dans une boucle et justifier pourquoi.
- [ ] Formater un texte avec `.formatted()` et un text block.
- [ ] Comparer deux `String` avec `.equals()` / `.equalsIgnoreCase()`.

➡️ **Prochaine étape** : jusqu'ici, une réclamation était éparpillée en variables séparées (`quartier`, `delaiJours`, `statut`…) — le fragile « tableau parallèle » de la leçon 02 l'a montré. La leçon 04 introduit enfin la **classe** : un modèle qui regroupe données ET comportements sous un seul nom. C'est le cœur de Java, et le point de départ du fil rouge SignalCUA.





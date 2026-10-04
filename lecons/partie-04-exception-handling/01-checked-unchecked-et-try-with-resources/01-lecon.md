# Leçon 01 — Exceptions : checked, unchecked et try-with-resources

> 🧭 **Pont depuis la partie 3** : jusqu'ici, quand une recherche ne trouvait rien, vous avez appris à le dire **proprement** avec `Optional` (leçon 03 de la partie 3) — une *absence attendue*, qui reste un résultat normal. Mais toutes les erreurs ne sont pas des absences : un fichier peut **ne pas exister**, une ligne de texte peut être **mal formée**, une valeur peut être **invalide**. Ces situations-là ne se « renvoient » pas comme une valeur : elles **interrompent** le déroulement normal du programme. Le mécanisme qui les représente s'appelle une **exception**. Cette leçon explique comment le langage le gère, **pourquoi** Java distingue deux grandes familles d'exceptions, et **comment** écrire du code qui échoue sans se planter.

---

## 1. Objectifs d'apprentissage

À la fin de cette leçon, vous saurez :

- Expliquer **ce qu'est une exception** et d'où vient ce mécanisme dans Java.
- Distinguer une **exception checked** d'une **exception unchecked**, et dire **pourquoi** Java fait cette différence (un point propre à Java, sans équivalent en TypeScript).
- Utiliser **`throw`** (lever une exception) et **`throws`** (déclarer qu'une méthode peut en lever une).
- Écrire un bloc **`try / catch / finally`** correct, y compris le **multi-catch** et l'**ordre** des `catch`.
- **Fermer une ressource** proprement avec le **`try-with-resources`**, et dire pourquoi c'est mieux qu'un `finally`.
- **Lire une stack trace** (« trace de pile ») pour trouver la ligne exacte de l'erreur.
- Reconnaître les **anti-patterns** qui font disparaître l'information (le fameux `catch (Exception e) {}`).

---

## 2. Explication simple

### 2.1 Qu'est-ce qu'une exception, et d'où ça vient ?

**Pourquoi ce mécanisme existe-t-il ?** Une méthode `diviser(int a, int b)` ne peut pas toujours rendre un résultat : si `b` vaut `0`, il n'y a **aucun** résultat possible. Autrefois (en C, par exemple), on renvoyait un « code d'erreur » spécial (`-1`, `NULL`…) que l'appelant **pouvait oublier de tester** — et l'erreur passait alors inaperçue. Java a choisi une autre voie : **quand une opération échoue, elle *lève* une exception**. Le déroulement normal s'arrête, et le langage cherche une **section prête à la rattraper** quelque part dans la pile des appels.

**Analogie** : imaginez un détecteur de fumée dans un immeuble. Tant qu'il ne sonne pas, tout suit son cours. S'il sonne, **tout s'interrompt** et l'alarme remonte étage par étage jusqu'à ce que quelqu'un réagisse (ou jusqu'au bout, où l'immeuble « s'arrête »). Une exception, c'est cette alarme : un objet qui **porte l'information du problème** (type, message, trace) et qui **remonte** la chaîne des appels.

**Comment ça remonte ?** Une méthode qui lève une exception s'arrête net. Si personne ne la rattrape dans cette méthode, elle **remonte** à la méthode appelante, et ainsi de suite — on dit qu'elle se **propage**. Si aucune méthode ne la rattrape, le programme s'arrête et affiche la **stack trace** (voir 2.7).

**Quand l'utiliser ?** Pour signaler qu'une **opération n'a pas pu se dérouler**. Ce n'est pas un outil pour « sortir d'une boucle » (voir le piège n°8).

### 2.2 La hiérarchie : `Throwable`, `Error`, `Exception`, `RuntimeException`

Tout ce qu'on peut « lancer » en Java est un objet qui **hérite** de la classe **`Throwable`**. C'est le sommet de l'arbre. En dessous, deux branches :

```text
Throwable
├── Error                 (erreurs graves du systeme / de la machine virtuelle — on ne les gere PAS)
└── Exception             (erreurs « normales » de l'application)
    ├── RuntimeException  (unchecked : NON verifiees par le compilateur)
    │   ├── NullPointerException
    │   ├── IllegalArgumentException   <- déjà utilisee dans la partie 3
    │   └── ...
    └── ... (toutes les autres = checked)
        ├── IOException
        ├── SQLException
        └── ...
```

**L'analogie** : `Throwable` est la famille des « objets-qui-remontent ». `Error` regroupe des catastrophes que **vous ne pouvez pas réparer** (`OutOfMemoryError` : plus de mémoire ; `StackOverflowError` : récursion infinie). On ne les attrape **jamais** : la seule réaction utile est de laisser le programme s'arrêter (les réparer à chaud est illusoire).

`Exception`, au contraire, regroupe des situations que le langage considère comme **réparables** : fichier absent, réseau coupé, saisie invalide.

Enfin, `RuntimeException` est une sous-famille d'`Exception` un peu spéciale — c'est **elle qui explique toute la distinction** de la section suivante.

> 📖 **Vocabulaire** : **throwable** = « qui peut être lancé » ; **machine virtuelle (VM)** = le programme qui exécute votre code Java ; **hérite** = reprend les caractéristiques de la classe du dessus (partie 2, `extends`).

### 2.3 Checked vs unchecked : pourquoi le compilateur intervient

C'est **LA** particularité de Java. On classe les exceptions `Exception` (celles qui **ne** sont **pas** des `RuntimeException`) en deux familles :

| Famille | Qui en fait partie | Le compilateur... | Concrètement |
|---|---|---|---|
| **Checked** (« vérifiées ») | tout ce qui hérite d'`Exception` **sans** passer par `RuntimeException` : `IOException`, `SQLException`… | **vous OBLIGE** à les gérer : soit les `catch`, soit les déclarer avec `throws` | « je t'oblige à prévoir le cas où ça échoue » |
| **Unchecked** (« non vérifiées ») | tout ce qui hérite de `RuntimeException` : `NullPointerException`, `IllegalArgumentException`, `ArithmeticException`… | ne dit **rien** : aucune obligation de déclaration | « ça peut arriver, mais je ne t'oblige à rien » |

**Pourquoi une telle distinction ?** Parce que les deux familles signalent **deux natures de problème différentes**.

- Une exception **checked** signale un problème **extérieur au programme**, **imprévisible mais possible**, et surtout **récupérable** : le fichier n'existe pas → on peut essayer une autre méthode, demander le chemin à l'utilisateur… C'est pour cela que le compilateur impose de **prévoir** ce cas.
- Une exception **unchecked** signale plutôt un **bug de programmation** ou une **règle métier violée** : on a passé `null` là où il ne fallait pas, une valeur hors des bornes, une transition interdite. En théorie, cela n'aurait pas dû arriver ; le compilateur ne peut pas le vérifier à l'avance.

**Analogie** : vérifier une exception *checked*, c'est comme **vérifier sa valise à un comptoir** : elle est *déclarée*, tracée, obligatoire. Une exception *unchecked*, c'est plutôt **un délit de conduite** : on ne remplit rien à l'avance, mais si ça arrive, la justice (le programme) réagit. On dit d'ailleurs que ces exceptions sont **« à l'exécution »** (*runtime*), et qu'elles sont **« fail-fast »** : elles échouent tôt et fort plutôt que de propager une valeur fausse.

> 💡 **Note pour un développeur venant de TypeScript** : en TypeScript / JavaScript, il n'existe **aucune** distinction checked/unchecked, et le compilateur **ne vous force jamais** à gérer une erreur. Java a fait un autre choix, et c'est **le cœur** de cette leçon.

> ⚠️ **Point d'histoire (le « pourquoi c'est comme ça »)** : Java date de 1995. À l'époque, l'idée des exceptions *checked* était considérée comme une **bonne pratique** : « oblige l'appelant à traiter les erreurs réelles ». Aujourd'hui, l'industrie a largement changé d'avis (voir la section 4) : trop d'exceptions checked finissent par **polluer toutes les signatures** (`throws IOException, SQLException, ParseException…`). Le langage, lui, n'a jamais pu retirer ce mécanisme : il doit rester compatible. On vit donc avec — intelligemment.

### 2.4 La propagation : qui rattrape quoi ?

Reprenons une chaîne d'appels réelle :

```java
public static void main(String[] args) {
    lireConfig();                        // (3) le "main" appelle lireConfig
}

static void lireConfig() {
    chargerFichier("config.txt");         // (2) lireConfig appelle chargerFichier
}

static void chargerFichier(String nom) throws IOException {
    Files.readString(Path.of(nom));       // (1) si le fichier manque, une IOException est LEVEE ici
}
```

Si `config.txt` est absent, l'`IOException` **remonte** : `chargerFichier` s'arrête → `lireConfig` s'arrête → `main` s'arrête → si personne n'a fait de `catch`, **le programme se termine** et affiche la stack trace.

Le point important : **une exception ne s'arrête pas à la méthode qui l'a levée**. Elle cherche, en remontant, la première méthode qui **déclare la rattraper** (`catch`) — ou celle qui **déclare la laisser passer** (`throws`).

**Analogie** : l'alarme sonne au 3e étage ; si le 3e et le 2e ne réagissent pas, c'est le 1er (le `main`) qui décide. Les exceptions, c'est pareil : **on choisit le niveau auquel on réagit**.

### 2.5 `try` / `catch` / `finally`

Trois mots-clés :

```java
try {
    // code "sous surveillance" : ce qui peut lever une exception
} catch (IOException e) {
    // code exécuté SI une IOException a été levée dans le try
} finally {
    // code TOUJOURS exécuté (qu'il y ait eu une erreur ou non)
}
```

- **`try`** : l'interrupteur de l'alarme. On y place les instructions risquées.
- **`catch`** : le pompier. Il **capture** l'exception, l'examine (`e`), et réagit. Un `catch` **ne se déclenche que** si une exception compatible a été levée.
- **`finally`** : le « quoi qu'il arrive ». Il s'exécute **toujours** — que le `try` réussisse, lève une exception, ou même que le `catch` en lève une à son tour. Usages classiques : libérer une ressource, restaurer un état.

Trois détails que le débutant ignore souvent :

1. **Plusieurs `catch`** : on peut capter des types différents avec des blocs distincts :
   ```java
   try { ... }
   catch (FileNotFoundException e) { ... }   // cas le plus précis D'ABORD
   catch (IOException e)           { ... }   // cas plus large ENSUITE
   ```
   **Règle** : du **plus précis au plus général**. `FileNotFoundException` est une fille d'`IOException` : si vous mettez `IOException` d'abord, le bloc suivant devient **inatteignable** et le code **ne compile pas** (voir piège n°6).
2. **Multi-catch** : pour traiter deux types **de la même façon** :
   ```java
   catch (IOException | SQLException e) { ... }
   ```
   `e` est alors typé sur leur ancêtre commun.
3. **Ne capturez pas `Exception` trop vite** : capter `Exception` attrape **tout** (checked ET unchecked), c'est pratique mais ça masque les bugs (piège n°3).

> 🔎 **Astuce lecture de code** : `catch (IOException e)` se lit « si une `IOException` a été levée, nomme-la `e` ».

### 2.6 `try-with-resources` : fermer une ressource proprement

**Le problème** : certains objets ont besoin d'être **libérés** explicitement quand on a fini. Exemples : un fichier ouvert, une connexion réseau, une connexion à une base de données. On les appelle des **ressources**. Si on oublie de les fermer, elles restent « ouvertes » : fuite de mémoire, fichier verrouillé, connexions épuisées…

**Analogie** : ouvrir un fichier, c'est comme **ouvrir un robinet d'eau**. Si vous ne le fermez pas, ça déborde — même si une erreur survient en cours de route.

**La mauvaise façon** (avant Java 7) : fermer à la main dans un `finally`. C'est **verbeux** et on peut se tromper. Le code ci-dessous est **volontairement** lourd :

```java
BufferedReader lecteur = null;
try {
    lecteur = Files.newBufferedReader(Path.of("data.txt"));
    // ... lire ...
} finally {
    if (lecteur != null) {
        lecteur.close();   // encore une ligne qui peut elle-meme lever une exception !
    }
}
```

**La bonne façon (depuis Java 7)** : le **`try-with-resources`**. On déclare la ressource **entre parenthèses** après `try`. Java la **ferme automatiquement** à la sortie du bloc — **même en cas d'exception**.

```java
try (BufferedReader lecteur = Files.newBufferedReader(Path.of("data.txt"))) {
    // ... lire ...
}   // <- lecteur.close() est appelé ICI, automatiquement
```

**Comment ça marche ?** Toute ressource utilisable ainsi doit **implémenter l'interface `AutoCloseable`** (ou sa fille `Closeable`) — c'est-à-dire fournir une méthode `close()`. C'est une simple **règle de contrat** : « je sais me fermer moi-même ».

**Plusieurs ressources ?** On les sépare par `;`, et elles sont fermées dans **l'ordre inverse** de leur ouverture (la dernière ouverte est la première fermée) :

```java
try (InputStream in  = new FileInputStream("source.bin");
     OutputStream out = new FileOutputStream("copie.bin")) {
    // ... copie ...
}   // out.close() puis in.close()
```

> 🧠 **À retenir** : dès qu'un objet a une méthode `close()` (fichier, flux, connexion), **utilisez `try-with-resources`**, jamais une fermeture manuelle.

**Bonus (le détail « expert »)** : si une exception est levée **dans le `try`** ET qu'une autre survient **pendant l'appel à `close()`**, Java n'en perd aucune : la première reste l'exception principale, la seconde est attachée comme **exception supprimée** (accessible via `getSuppressed()`). Un `finally` manuel, lui, **écraserait** la première par la seconde — un piège réel.

### 2.7 Lire une stack trace

Quand une exception n'est rattrapée nulle part, Java affiche une **stack trace**. C'est votre meilleur outil de diagnostic. Exemple (réellement produit par `new int[]{1,2,3}[5]`) :

```text
Exception in thread "main" java.lang.ArrayIndexOutOfBoundsException: Index 5 out of bounds for length 3
    at Main.main(Main.java:4)
```

**Lecture** :

- **Première ligne** : le **type** (`ArrayIndexOutOfBoundsException`) et le **message** (`Index 5 out of bounds for length 3`).
- **Lignes `at …`** : la **pile d'appels**, **du plus récent (en haut) au plus ancien (en bas)**. La première ligne `at` désigne **l'endroit exact** où l'erreur a été levée : ici `Main.java`, **ligne 4**.

> 🔧 **Règle de dépannage** : commencez **toujours** par la **première ligne `at`** — c'est le point d'explosion. Les lignes du dessous montrent le chemin qui y a mené. (En production, vous verrez souvent la même exception avec des dizaines de lignes : cherchez la première qui concerne **votre** package.)

---

## 📖 Vocabulaire / Abréviations

| Terme | Définition d'une ligne |
|---|---|
| **Exception** | Objet décrivant une opération qui n'a pas pu se dérouler ; interrompt le flux normal et « remonte » les appels. |
| **Throwable** | La classe racine de tout ce qu'on peut lever en Java (`Error` + `Exception`). |
| **Error** | Sous-famille de `Throwable` réservée aux pannes graves non récupérables (`OutOfMemoryError`) : on ne la gère pas. |
| **Checked** | Exception que le compilateur **oblige** à gérer (`catch` ou `throws`) ; issue d'`Exception` hors `RuntimeException`. |
| **Unchecked** | Exception **non obligatoire** à gérer ; issue de `RuntimeException`. |
| **RuntimeException** | La classe mère des exceptions *unchecked* ; signale surtout un bug ou une règle métier violée. |
| **`throw`** | Mot-clé qui **lève** une exception, dans le corps d'une méthode. |
| **`throws`** | Mot-clé sur la **signature** d'une méthode qui **déclare** qu'elle peut lever tel type. |
| **Propagation** | Fait, pour une exception, de remonter d'appel en appel jusqu'à un `catch` (ou jusqu'à l'arrêt du programme). |
| **`try`/`catch`/`finally`** | Structure de gestion : surveiller / réagir / exécuter toujours. |
| **Multi-catch** | Capter plusieurs types d'un coup : `catch (A | B e)`. |
| **Stack trace** | Texte qui liste les appels menant à l'erreur, du plus récent au plus ancien. |
| **Ressource** | Objet à libérer (fichier, flux, connexion) qui possède une méthode `close()`. |
| **`AutoCloseable` / `Closeable`** | Interface des ressources « qui savent se fermer » ; prérequis du `try-with-resources`. |
| **`try-with-resources`** | `try` qui ferme automatiquement ses ressources, erreur ou pas. |
| **Exception supprimée** (*suppressed*) | Exception secondaire (survenue à la fermeture) attachée à la principale au lieu de l'écraser. |
| **Fail-fast** | Principe : échouer tôt et fort plutôt que propager une valeur fausse. |
| **Swallow** (*avaler*) | Attraper une exception et ne rien en faire : l'information disparaît. |
| **`printStackTrace()`** | Méthode qui écrit la trace sur la console : à éviter en production (on utilise un *logger*). |
| **Logger** | Outil d'écriture de journaux structurés (partie 11) ; alternative propre à `printStackTrace`. |

---

## 3. Exemples concrets

> 🔗 **Comment cette section s'articule** : la section 2 a *expliqué*. Ici, tout est **exécutable** et **commenté**. Le fil rouge **SignalCUA** apparaît dès l'exemple 3.3 (lecture d'un fichier) : c'est exactement ce que vous coderez dans `02-exercice.md`.

### 3.1 Lever et déclarer : `throw` et `throws`

```java
// Une méthode qui VALIDE et LÈVE une exception unchecked quand la règle est violée.
public static int diviser(int a, int b) {
    if (b == 0) {
        throw new IllegalArgumentException("Division par zéro interdite (b = 0)");
        //    ^ mot-clé : on LÈVE une nouvelle exception, avec un message explicite
    }
    return a / b;
}

// Une méthode qui lit un fichier : `throws IOException` DECLARE que l'exception peut remonter.
// Sans cette déclaration, la ligne Files.readString(...) NE COMPILE PAS (IOException est checked).
public static String lire(String nom) throws IOException {
    return Files.readString(Path.of(nom));   // peut lever une IOException (checked)
}
```

- **`throw`** (singulier) : l'action de lever. On l'écrit **dans** le corps d'une méthode.
- **`throws`** (pluriel, sur la signature) : la **déclaration**. « Cette méthode *peut* lever tel type. » Si le type est **checked**, le compilateur impose `throws` sur la méthode ou un `catch` à l'intérieur.

**Pourquoi un message ?** Un message clair (`"Division par zéro interdite (b = 0)"`) transforme une stack trace muette en **indice exploitable**. Un message vide oblige à relire tout le code.

### 3.2 `try` / `catch` / `finally` exécutés sous vos yeux

```java
public static void main(String[] args) {
    System.out.println("A) avant le try");
    try {
        System.out.println("B) dans le try");
        int x = 10 / 0;                       // LEVE ArithmeticException
        System.out.println("C) jamais affiché");
    } catch (ArithmeticException e) {
        System.out.println("D) catch : " + e.getMessage());   // "D) catch : / by zero"
    } finally {
        System.out.println("E) finally : toujours exécuté");
    }
    System.out.println("F) après le bloc");
}
```

**Sortie réelle :**

```text
A) avant le try
B) dans le try
D) catch : / by zero
E) finally : toujours exécuté
F) après le bloc
```

**Ce que ça prouve** : `C)` n'apparaît **jamais** (le `try` s'est arrêté net à la ligne fautive), le `catch` a pris le relais, le `finally` s'est exécuté, puis le programme a continué normalement. **Sans `catch`, le programme se serait arrêté à `B)`.**

### 3.3 Le fil rouge : lire un fichier avec `try-with-resources`

Le cœur de l'exercice. On lit un fichier texte **ligne par ligne**, chaque ligne décrivant une réclamation `quartier;priorite;description`.

```java
// `throws IOException` : la lecture d'un fichier est une opération CHECKED.
public RapportImport importer(Path chemin) throws IOException {
    List<Reclamation> valides = new ArrayList<>();
    List<String> erreurs = new ArrayList<>();

    // try-with-resources : le BufferedReader sera fermé automatiquement, erreur ou pas.
    try (BufferedReader lecteur = Files.newBufferedReader(chemin, StandardCharsets.UTF_8)) {
        String ligne;
        // readLine() renvoie null quand on atteint la fin du fichier : condition d'arrêt.
        while ((ligne = lecteur.readLine()) != null) {
            if (ligne.isBlank()) {
                continue;   // ligne vide = pas une erreur, on l'ignore
            }
            try {
                valides.add(analyser(valides.size() + 1, ligne));  // peut lever IllegalArgumentException
            } catch (IllegalArgumentException e) {
                erreurs.add(e.getMessage());    // erreur de DONNÉE : on note et on continue
            }
        }
    }
    return new RapportImport(valides, erreurs);
}
```

Deux garanties :
1. **Le fichier est toujours fermé** (même si `analyser` lève une exception) : c'est tout le bénéfice du `try-with-resources`.
2. L'absence du fichier est une exception **checked** (`NoSuchFileException`, fille d'`IOException`) : le compilateur **impose** de s'en occuper (soit `throws`, soit `catch`).

### 3.4 Distinguer erreur de donnée et erreur technique dans l'analyse

```java
private Reclamation analyser(int id, String ligne) {
    // On découpe la ligne sur le point-virgule : "Medina;URGENTE;Nid de poule dangereux"
    String[] champs = ligne.split(";");
    if (champs.length != 3) {
        // Donnée invalide : on LÈVE une unchecked avec un message PRÉCIS.
        throw new IllegalArgumentException(
                "Champs attendus : 3, trouvé " + champs.length);
    }
    String quartier = champs[0].trim();
    String prioriteTexte = champs[1].trim().toUpperCase();
    String description = champs[2].trim();

    Priorite priorite;
    try {
        // valueOf() lève une IllegalArgumentException si la valeur n'existe pas dans l'enum.
        priorite = Priorite.valueOf(prioriteTexte);
    } catch (IllegalArgumentException cause) {
        // On RELANCE une exception plus explicite en CONSERVANT la cause (2e argument).
        throw new IllegalArgumentException(
                "Priorité inconnue \"" + prioriteTexte + "\"", cause);
    }
    return new Reclamation(id, quartier, priorite, description);
}
```

**Pourquoi `IllegalArgumentException` (unchecked) ici ?** Parce qu'une **ligne mal formée** n'est pas une panne extérieure : c'est une **donnée invalide**. On veut qu'elle **remonte** (fail-fast) — l'appelant décidera s'il arrête tout l'import ou ignore la ligne. On ne force donc personne à la déclarer dans sa signature.

---

## 4. Bonnes pratiques modernes (2025-2026)

1. **Ciblez le `catch` le plus précis possible.** `catch (MonExceptionPrecise e)` plutôt que `catch (Exception e)`. Plus le `catch` est large, plus il attrape de choses imprévues — donc de bugs.
2. **Règle par défaut : privilégiez les exceptions *unchecked* pour la logique métier.** C'est le conseil de la roadmap pour cette partie, et le choix de tout l'écosystème Spring. Réservez les *checked* aux **I/O bas niveau réellement récupérables** (fichier, réseau), là où vous savez quoi faire de l'erreur.
3. **`try-with-resources` est la norme.** Dès qu'un objet a une méthode `close()`, utilisez-le. On n'écrit plus de fermeture manuelle en `finally` depuis 2011.
4. **Ne « mangez » jamais une exception silencieusement.** `catch (Exception e) {}` est l'anti-pattern n°1. Si vous attrapez, **décidez** : relancer, transformer, ou **journaliser** avec contexte.
5. **En production, on journalise (logging), on n'imprime pas.** `e.printStackTrace()` écrit sur la console du serveur — que personne ne consulte. La bonne pratique est un **logger** (SLF4J/Logback, partie 11) : `log.error("Import du fichier {} impossible", chemin, e);`.
6. **Relancez en gardant le contexte.** Quand vous transformez une exception, transmettez la **cause** : `throw new ReclamationInvalideException("Ligne " + n + " invalide", e);` (leçon 02). Java affichera alors `Caused by: …`.
7. **N'utilisez pas les exceptions pour le flux normal.** Par exemple, ne les utilisez **pas** pour « sortir d'une boucle ». Lever une exception coûte cher (création d'objet + remplissage de la stack trace) et obscurcit le code.
8. **Documentez ce que lève une méthode.** En partie 9, vos tests utiliseront `assertThrows(MaException.class, () -> ...)` : plus les exceptions sont précises, plus les tests sont clairs.

---

## 5. Pièges à éviter

> 🎯 **Comment lire les pièges** : pour chacun, un **❌ MAUVAIS**, **pourquoi** c'est dangereux, puis la **✅ version correcte** juste à côté.

### Piège 1 — Le `catch` vide qui avale l'information

```java
// ❌ MAUVAIS : l'erreur disparaît, personne ne saura jamais que ça a échoué
try {
    importer(chemin);
} catch (IOException e) {
    // rien du tout
}

// ✅ BON : on journalise avec contexte (et/ou on relance)
try {
    importer(chemin);
} catch (IOException e) {
    log.error("Import impossible du fichier {}", chemin, e);
    throw e;                       // on laisse remonter : l'appelant décidera
}
```

**Pourquoi c'est dangereux** : c'est l'**exception swallowing** (« avaler l'exception »). Un bug devient **invisible** : le programme continue comme si tout allait bien. En production, c'est la cause n°1 des incidents « impossibles à diagnostiquer ».

### Piège 2 — `e.printStackTrace()` en production

```java
// ❌ MAUVAIS : ça part sur la console du serveur, que personne ne lit
} catch (Exception e) {
    e.printStackTrace();
}

// ✅ BON : un logger structuré, avec un message ET l'objet exception
} catch (Exception e) {
    log.error("Échec de l'import depuis {}", chemin, e);
}
```

**Pourquoi** : `printStackTrace` écrit sur la **sortie standard**, pas dans vos journaux. En production, on consulte les logs (fichiers, agrégateur). Le détail arrive en **partie 11 — Logging**.

### Piège 3 — `catch (Exception e)` trop large

```java
// ❌ MAUVAIS : attrape AUSSI les bugs de votre code (NullPointerException, etc.)
try {
    int total = somme / nombre;
} catch (Exception e) {
    System.out.println("Erreur de calcul");
}

// ✅ BON : on attrape SEULEMENT ce qu'on sait gérer
} catch (ArithmeticException e) {
    System.out.println("Division impossible : " + e.getMessage());
}
```

**Pourquoi** : un `catch (Exception e)` avale non seulement les erreurs attendues, mais aussi les **bugs** (NPE, index hors bornes…), qui disparaissent alors silencieusement. On veut que les bugs **fassent échouer** le programme, pas qu'ils se cachent.

### Piège 4 — `return` dans un `finally`

```java
// ❌ MAUVAIS : le return du finally ÉCRASE tout, meme une exception
static int f() {
    try {
        throw new IllegalStateException("bug");
    } finally {
        return 42;               // l'exception est PERDUE : f() renvoie 42 !
    }
}

// ✅ BON : le finally ne fait que liberer / nettoyer
static int f() {
    try {
        throw new IllegalStateException("bug");
    } finally {
        // nettoyage uniquement (ici, rien a nettoyer)
    }
}
```

**Pourquoi** : un `return` (ou un `throw`) **dans** `finally` annule ce qui se passait avant. C'est un piège méconnu qui masque des bugs. **Règle** : un `finally` libère, il ne décide pas.

### Piège 5 — Fermer à la main au lieu d'un `try-with-resources`

```java
// ❌ MAUVAIS : verbeux, et on peut oublier de fermer en cas d'exception
BufferedReader lecteur = Files.newBufferedReader(chemin);
String ligne = lecteur.readLine();   // si ça lève, le fichier reste OUVERT
lecteur.close();

// ✅ BON : fermeture garantie, code plus court
try (BufferedReader lecteur = Files.newBufferedReader(chemin)) {
    String ligne = lecteur.readLine();
}
```

### Piège 6 — Mauvais ordre des `catch` (ne compile pas)

```java
// ❌ MAUVAIS : IOException d'abord -> FileNotFoundException devient inatteignable
try { ... }
catch (IOException e) { ... }           // trop général, capturé en premier
catch (FileNotFoundException e) { ... } // ERREUR DE COMPILATION : déjà capturé

// ✅ BON : du plus précis au plus général
try { ... }
catch (FileNotFoundException e) { ... }
catch (IOException e) { ... }
```

### Piège 7 — Attraper `Error` (ou tout `Throwable`)

```java
// ❌ MAUVAIS : on tente de récupérer d'une catastrophe mémoire
try {
    tableaux = new int[Integer.MAX_VALUE];
} catch (OutOfMemoryError e) {        // Error : on ne la gère PAS
    System.out.println("on continue ?");
}

// ✅ BON : on ne gère que Exception ; on laisse Error arrêter le programme
try {
    tableau = new int[10_000];
} catch (Exception e) {
    System.out.println("échec maîtrisé : " + e.getMessage());
}
```

**Pourquoi** : `Error` (mémoire épuisée, pile saturée) ne se répare pas « sur le moment ». Continuer donne des comportements imprévisibles.

### Piège 8 — Lever une exception pour le contrôle de flux

```java
// ❌ MAUVAIS : une exception pour sortir d'une boucle -> lent et illisible
try {
    for (int i = 0; i < 1000; i++) {
        if (reclamations.get(i).getId() == cible) {
            throw new Found();            // détourner le flux normal
        }
    }
} catch (Found e) { ... }

// ✅ BON : un simple return dit la même chose, sans coût
for (Reclamation r : reclamations) {
    if (r.getId() == cible) {
        return r;
    }
}
return null; // ou Optional.empty() (partie 3)
```

**Pourquoi** : lever une exception fait **remplir toute la stack trace** — c'est cher. Et surtout, c'est **contre l'intention** : une exception signale un échec **imprévu**, pas une étape normale.

---

## Checklist de validation

Avant de passer à la leçon 02, vérifiez que vous savez faire **chacun** de ces points :

- [ ] Expliquer, avec une analogie, ce qu'est une exception et pourquoi elle « remonte » les appels.
- [ ] Citer les trois branches de `Throwable` (`Error`, `Exception`, `RuntimeException`) et dire ce qu'on ne gère jamais (`Error`).
- [ ] Donner un exemple d'exception **checked** et un exemple **unchecked**, et expliquer **pourquoi** Java les distingue.
- [ ] Écrire `throw new IllegalArgumentException("...")` dans une méthode et expliquer la différence avec `throws` sur la signature.
- [ ] Écrire un `try/catch/finally` et prédire, ligne par ligne, ce qui s'exécute.
- [ ] Écrire deux `catch` avec le **bon ordre** (précis → général) et expliquer l'erreur de compilation si on inverse.
- [ ] Écrire un `try-with-resources` (une puis deux ressources) et dire **quand** la ressource est fermée.
- [ ] Lire une stack trace : trouver le **type**, le **message**, et la **première ligne `at`** (ligne du bug).
- [ ] Citer les 4 anti-patterns majeurs : `catch` vide, `printStackTrace`, `catch (Exception e)` trop large, `return` dans `finally`.

---

## 🔴 Fil rouge — ou en est SignalCUA ?

Cette leçon arme SignalCUA pour **importer des réclamations depuis un fichier** sans se planter : lecture avec `try-with-resources`, erreurs de données signalées par une exception **unchecked** (`IllegalArgumentException`), fichier absent géré comme une exception **checked** (`IOException`). Vous construisez la brique de base de l'Étape 4 (voir `lecons/fil-rouge-signalcua.md`).

---

➡️ **Prochaine étape** : vous savez désormais **lever** et **attraper** des exceptions. Mais une `IllegalArgumentException` ne dit pas **quel** problème métier a échoué — dans les logs, un `NullPointerException` et une « valeur invalide » se ressemblent. La **leçon 02 — Exceptions métier custom** crée des exceptions **nommées** (`ReclamationNotFoundException`, `ReclamationInvalideException`) qui portent le **sens** du problème et son **contexte** — le type exact que la partie 7 transformera en code HTTP (404, 409…).

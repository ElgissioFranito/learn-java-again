# 00 — Introduction de la partie 4 : la gestion des exceptions

> 🧭 **Pourquoi ce fichier existe, et pourquoi il se lit APRÈS les leçons ?** Les 2 leçons de cette partie définissent chaque terme nouveau dans leur **mini-glossaire** (`📖 Vocabulaire`). Mais quelques notions **transversales** apparaissent dans le **code** des exercices et corrections **sans y être définies** « à part entière » : des classes de fichiers, l'encodage, des codes HTTP, des outils de journalisation... Ce fichier les **rassemble**. Lisez-le en **dernier**, comme un **filet de rattrapage** : si un mot du code vous a échappé, il est probablement ici.

---

## 🧭 Le pont d'entrée : de la partie 3 à la partie 4

À la fin de la partie 3, SignalCUA savait **représenter** ses données : un registre typé (generics), honnête sur l'absence (`Optional`), et capable de **mesurer ses délais** (`java.time`). Mais le projet avait encore un angle mort : **comment échouer**. Une recherche qui échoue, un fichier mal formé, une valeur invalide — autant de situations que la partie 3 laissait en suspens.

| Leçon | Ce qu'elle ajoute | Le problème qu'elle règle |
|---|---|---|
| 01 — Checked vs unchecked, `try-with-resources` | lever (`throw`), déclarer (`throws`), attraper (`try/catch`), fermer proprement (`try-with-resources`) | « le programme plante dès qu'une opération échoue » |
| 02 — Exceptions métier custom | une hiérarchie d'exceptions **nommées** (`SignalcuaException` + filles), **contexte**, **cause**, propagation jusqu'à la frontière | « toutes les erreurs se ressemblent dans les logs » |

Chaque leçon **s'appuie sur la précédente** : la leçon 02 réutilise `throw`/`catch` de la leçon 01, et la racine `SignalcuaException` est *unchecked* précisément parce que la leçon 01 a expliqué la différence checked/unchecked. L'**Étape 4** du fil rouge SignalCUA se construit au fil de ces 2 leçons (voir `lecons/fil-rouge-signalcua.md`).

---

## 🛠️ Le vocabulaire transversal de la partie 4 (à lire si un mot vous échappe)

> Ces termes ne sont **pas** définis dans les mini-glossaires des leçons 01 et 02 (ou seulement en passant). Tous sont **utilisés** dans le code des exercices ou des corrections.

### A. Fichiers et entrées/sorties (utilisés leçon 01)

| Terme | Définition complète |
|---|---|
| **I/O / E/S** | *Input/Output* (« Entrées/Sorties ») : toute opération de lecture ou d'écriture de données (fichier, réseau, clavier). |
| **`IOException`** | L'exception *checked* **mère** de la plupart des erreurs d'E/S ; on ne peut pas l'ignorer (le compilateur l'impose). |
| **`NoSuchFileException` / `FileNotFoundException`** | Des **filles** d'`IOException` signalant qu'un fichier n'existe pas ; c'est pourquoi on les capture **avant** leur mère (leçon 01, piège 6). |
| **`Path`** | Un **objet** représentant un chemin de fichier (`Path.of("data.txt")`). Depuis Java 7, il remplace l'ancien `java.io.File`. |
| **`Files`** | Classe **utilitaire** (`java.nio.file.Files`) regroupant des méthodes statiques de lecture/écriture : `readString`, `writeString`, `newBufferedReader`. |
| **NIO** | *New I/O* : la bibliothèque moderne de gestion de fichiers et flux (`java.nio`), alternative à l'ancienne `java.io`. |
| **`BufferedReader`** | Un lecteur de **texte** qui lit par blocs (tampon) et fournit `readLine()`. C'est **une ressource** : il faut la fermer (d'où le `try-with-resources`). |
| **Encodage / UTF-8** | Correspondance entre **octets** et **caractères**. UTF-8 prend en charge les accents ; `StandardCharsets.UTF_8` désigne cet encodage dans le code. |
| **Tampon (*buffer*)** | Zone mémoire temporaire où l'on accumule des données avant de les traiter (lire/écrire par gros paquets est plus rapide qu'octet par octet). |

### B. Erreurs et typage (utilisés leçons 01 et 02)

| Terme | Définition complète |
|---|---|
| **`Throwable`** | La **racine** de tout ce qui peut être levé en Java (`Error` + `Exception`) — leçon 01, section 2.2. |
| **NPE (`NullPointerException`)** | Exception *unchecked* levée quand on appelle une méthode sur `null` ; déjà vue en partie 3. |
| **Autoboxing / unboxing** | Conversion **automatique** entre un primitif (`int`) et sa classe enveloppe (`Integer`). Piège : déballer un `Integer` valant `null` en `int` lève un **NPE** (évoqué dans la correction 02). |
| **`enum`** | Type fermé à valeurs fixées (partie 2) ; utilisé pour `Priorite` et `StatutReclamation`. |
| **`record`** | Classe immuable à champs, générée automatiquement (partie 2) ; utilisé pour `RapportImport`. |
| **`Optional`** | L'absence **dans le type de retour** (partie 3) ; comparé aux exceptions en leçon 02, section 2.7. |
| **`fail-fast`** | Principe : échouer **tôt** et **fort** plutôt que propager une valeur fausse — leçon 01, section 2.3. |
| **Frontière (*boundary*)** | Le point où le code parle au monde extérieur ; c'est là qu'on traduit les exceptions — leçon 02, section 2.6. |

### C. Ce vers quoi la partie 4 pointe (pour ne pas être surpris plus tard)

| Terme | Définition complète |
|---|---|
| **HTTP** | *HyperText Transfer Protocol* : le protocole du web. Une API REST répond avec un **code** et un **corps**. |
| **Code HTTP 400 / 404 / 409** | 400 = requête invalide ; 404 = ressource introuvable ; 409 = conflit (état incompatible). Cibles futures de nos exceptions métier. |
| **`@ControllerAdvice`** | Classe **Spring** (partie 7) qui **centralise** la traduction « exception → réponse HTTP », en un seul endroit. |
| **Logger / SLF4J / Logback** | Outils d'écriture de **journaux** : SLF4J est la **façade** (l'API) et Logback une **implémentation** (partie 11). Alternative propre à `printStackTrace`. |
| **`assertThrows`** | Méthode de **test** JUnit (partie 9) vérifiant qu'un bloc de code **lève** bien une exception donnée. |
| **JDK / JVM** | Le **JDK** contient `javac` (compilateur) et `java` (exécuteur) ; la **JVM** exécute le programme. Rappel de `00-Introduction-partie` de la partie 1. |

---

## 🧩 Ce que la partie 4 vous a fait construire

À la fin de la leçon 02, vous disposez d'une **Étape 4 du fil rouge complète** :

- un **importateur de fichier** (exercice 01) : lecture en `try-with-resources` (ressource toujours fermée), lignes vides ignorées, lignes mal formées signalées par une exception *unchecked* **sans arrêter** l'import, fichier absent géré comme une *checked* ;
- une **hiérarchie d'exceptions métier** (exercice 02) : `SignalcuaException extends RuntimeException` → `ReclamationNotFoundException` (porte l'`id`), `ReclamationInvalideException`, `TransitionStatutInterditeException` ;
- une `Reclamation` qui **valide** ses données et ses transitions de statut (`fail-fast`) ;
- un `findById` qui **lève** au lieu de renvoyer `null`, et un service qui **propage** jusqu'à une **frontière** ;
- un réflexe de fond : distinguer **absence normale** (`Optional`) et **erreur** (exception).

---

➡️ **Prochaine étape** : la partie 5 — **Programmation fonctionnelle & Stream API**. SignalCUA « échoue proprement » ; il va maintenant **traiter** ses collections autrement qu'avec des boucles `for` : `filter`, `map`, `Collectors` — le vocabulaire déjà croisé avec `Optional` et la lecture de fichier. Rien n'apparaît sans lien : la partie 5 réutilise les objets, collections, `Optional` et exceptions que vous venez de consolider.

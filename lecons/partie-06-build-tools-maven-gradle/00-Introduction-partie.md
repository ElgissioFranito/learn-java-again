# 00 — Introduction de la partie 6 : builder SignalCUA comme un vrai projet

> 🧭 **Pourquoi ce fichier existe, et pourquoi il se lit APRÈS les leçons ?** Les 2 leçons de cette partie définissent chaque terme nouveau dans leur **mini-glossaire**. Mais quelques notions **transversales** apparaissent dans le texte ou les commandes sans y être définies « à part entière ». Ce fichier les **rassemble**. Lisez-le en **dernier**, comme un **filet de rattrapage**.

---

## 🧭 Le pont d'entrée : de la partie 5 à la partie 6

À la fin de la partie 5, SignalCUA savait **représenter** (parties 1-2), **stocker** (partie 3), **échouer proprement** (partie 4) et **interroger** ses données (partie 5) — mais tout vivait **à la main** dans un dossier, compilé avec `javac`. Dès qu'une bibliothèque externe entre en jeu (JUnit, puis Spring Boot), cette organisation ne tient plus.

| Leçon | Ce qu'elle ajoute | Le problème qu'elle règle |
|---|---|---|
| 01 — Maven | recette `pom.xml`, tiroirs `src/...`, cycle `compile → test → package`, scopes, wrapper `./mvnw`, Initializr, 1er test JUnit vert | « `javac` à la main ne gère ni dépendances ni reproductibilité » |
| 02 — Gradle (aperçu) | lire un `build.gradle.kts` avec des yeux Maven, commandes jumelles, tableau de choix | « un projet imposé en Gradle est illisible » |

La leçon 02 traduit chaque bloc Gradle en notion Maven de la leçon 01. L'**Étape 6** du fil rouge — **projet Maven buildable + test vert** — se construit dans la leçon 01, la leçon 02 ne migrant rien (voir `lecons/fil-rouge-signalcua.md`).

---

## 🛠️ Le vocabulaire transversal de la partie 6 (à lire si un mot vous échappe)

> Ces termes ne sont **pas** dans les mini-glossaires des leçons 01 et 02. Tous sont **utilisés** dans le texte, les commandes ou les corrections.

### A. Ce que Maven manipule sous le capot

| Terme | Définition complète |
|---|---|
| **XML / balise** | Format texte à `<balise>contenu</balise>` (cousin du HTML). Le `pom.xml` en est écrit. |
| **Classpath (`-cp`)** | La **liste des endroits** où `javac`/`java` cherchent les classes. Maven le construit pour vous. |
| **Surefire / compiler-plugin** | Les deux ouvriers rencontrés : le premier **compile**, le second **lance les tests**. |
| **Changelog** | Le **journal des changements** d'une lib, version par version : à lire avant toute mise à jour. |
| **`~/.m2`** | Le dépôt local : `~` = dossier personnel, `.m2` = garde-manger Maven dedans. |
| **`mvnw.cmd` / `gradlew.bat`** | Les pendants **Windows** de `mvnw` / `gradlew` (même rôle). |
| **Packaging `Jar` (vs `War`)** | `Jar` = appli autonome (`java -jar`) ; `War` = archive pour serveur externe (ancien usage). |


| **JDK / `JAVA_HOME`** | Le *Java Development Kit* (`javac` + `java`) ; `JAVA_HOME` dit où il est installé. En Maven elle doit viser un JDK 21 ; en Gradle la `toolchain` s'en charge. |
| **LTS** | *Long Term Support* : version maintenue des années (21...) vs éphémères (6 mois). On vise toujours une LTS. |
| **Groovy / Kotlin** | Deux langages de la JVM : Groovy, souple et historique des builds ; Kotlin, moderne et typé, base du `.kts`. |

### C. Ce vers quoi la partie 6 pointe (pour ne pas être surpris en partie 7)

| Terme | Définition complète |
|---|---|
| **`spring-boot-starter-parent`** | Le **parent** du `pom.xml` Spring Boot (BOM + réglages) : Initializr le pose, on ne l'écrit pas. |
| **`spring-boot-starter-test`** | Le **panier de test** Spring Boot (JUnit + Mockito + AssertJ) : votre JUnit, en version groupée. |
| **Mockito / AssertJ** | Les **faux objets** pour tests / les **assertions lisibles** : partie 9. |
| **Nexus** | Un **garde-manger privé** d'entreprise (proxy du Central + `.jar` maison). |
| **Multi-modules / `build cache`** | Projet en sous-projets liés / cache partagé : optimisations avancées, secondaires ici. |

---

## 🧩 Ce que la partie 6 vous a fait construire

À la fin de la leçon 01, vous disposez d'une **Étape 6 du fil rouge complète** :

- un **vrai projet Maven** `signalcua` (coordonnées `fr.cua.signalcua:signalcua:0.0.1-SNAPSHOT`, Java 21, `.gitignore` avec `target/`) ;
- les classes des parties 1-5 **déplacées sans réécriture** en `model`, `repository`, `exception` (seuls `package` + `import` changent) ;
- un **test JUnit vert** (`Tests run: 2, Failures: 0`) rejoué à chaque `package` ;
- le réflexe : **une commande = tout le build** (`./mvnw clean package`), identique partout ;
- en bonus (leçon 02) : le jumeau `build.gradle.kts` décodé et le choix Maven-par-défaut argumenté.

---

➡️ **Prochaine étape** : la partie 6 clôt la **phase Maven** : SignalCUA est structuré, prouvé par un test — sans changer une ligne de logique. La **partie 7 — Spring Boot & Injection de dépendances** prend ce projet tel quel : le `pom.xml` y gagnera le parent Spring Boot, et le registre deviendra un `@Service` exposé en HTTP. Sans cette partie, Spring Boot serait un tas de `.jar` incompréhensibles.

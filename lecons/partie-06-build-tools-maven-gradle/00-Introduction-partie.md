# 00 — Introduction de la partie 6 : builder SignalCUA comme un vrai projet

> 🧭 **Pourquoi ce fichier existe, et comment le lire ?** Le **pont d'entrée** (section suivante) se lit **AVANT** les leçons. Le **vocabulaire transversal** (sections A/B/C) est un **filet de rattrapage** à lire **APRÈS** si un mot vous échappe, mais chaque terme y est aussi défini **en une phrase dès sa première utilisation** dans les leçons (règle zéro-étonnement).
>
> 🔧 **Formateur Java** : on distingue l'essentiel (Maven + 1 test vert) du secondaire (Gradle = lecture seule, Nexus/multi-modules = juste de nom).

---

## 🧭 Le pont d'entrée : de la partie 5 à la partie 6

À la fin de la partie 5, SignalCUA savait **représenter** (parties 1-2), **stocker** (partie 3), **échouer proprement** (partie 4) et **interroger** ses données (partie 5) — mais tout vivait **à la main** dans un dossier, compilé avec `javac`. Dès qu'une bibliothèque externe entre en jeu (JUnit, puis Spring Boot), cette organisation ne tient plus.

| Leçon | Ce qu'elle ajoute | Le problème qu'elle règle |
|---|---|---|
| 01 — Maven | recette `pom.xml`, tiroirs `src/...`, cycle `compile → test → package`, scopes, wrapper `./mvnw`, Initializr, 1er test JUnit vert | « `javac` à la main ne gère ni dépendances ni reproductibilité » |
| 02 — Gradle (aperçu) | lire un `build.gradle.kts` avec des yeux Maven, commandes jumelles, tableau de choix | « un projet imposé en Gradle est illisible » |

La leçon 02 traduit chaque bloc Gradle en notion Maven de la leçon 01.

> 🔧 **`mvn` ou `./mvnw` ? (à lire AVANT la leçon 01, zéro installation surprise)**
>
> - **`mvn`** = la commande **globale**. Elle n'existe que **si vous avez installé Maven** sur votre poste. Pratique, mais pas garantie ailleurs (collègue, CI, serveur).
> - **`mvnw`** (`mvnw.cmd` sur Windows) = le **petit script fourni DANS le projet** (avec son dossier `.mvn/`), généré par Spring Initializr. **Rien à installer** : au premier `./mvnw ...`, il **télécharge** la bonne version de Maven puis rejoue votre commande. Ensuite tout le monde utilise **exactement la même version**.
> - **`./`** devant = « prends le script D'ICI » (Linux/Mac : le dossier courant n'est pas cherché par défaut, par sécurité). Preuve : `mvnw` tapé seul répond toujours `commande introuvable` (même sur un poste avec Maven 3.9.9) — toujours `./mvnw` depuis `signalcua/`.
> - **`PATH`** = la liste des dossiers où le shell cherche les commandes (`echo $PATH`). `mvn` marche parce que votre `~/.bashrc` y a ajouté `MAVEN_HOME/bin` ; sans ça, même `mvn` répondrait « introuvable ». `JAVA_HOME` dit où est le JDK 21.
> - **Règle pro** : dans un projet à wrapper, toujours `./mvnw`, jamais `mvn` global — même si `mvn` est installé chez vous. Même logique côté Gradle : `gradle` global vs `./gradlew` wrapper.
>
> **Analogie (formateur Java)** : `mvn` = votre propre tournevis ; `mvnw` = le tournevis **fourni dans la boîte du meuble**, au bon format. On utilise celui de la boîte pour que tout le monde visse pareil.

### Ce qu'il faut avoir / ce qui arrive tout seul

| Il vous faut... | À installer ? | D'où ça vient ? |
|---|---|---|
| **JDK 21** (`javac` + `java`) | **Oui : à vérifier** (`java -version` doit afficher 21) | installateur (Temurin/Oracle...) ; pour Maven global, `JAVA_HOME` doit pointer dessus (Gradle `toolchain` s'en charge seul, leçon 02) |
| **`mvn` global** | **Optionnel** (pratique si vous l'avez) | installation manuelle — **non requis** si le projet a son wrapper |
| **`mvnw` + `.mvn/`** | **Fourni, rien à installer** | déjà dans le zip Initializr, versionné en Git |
| **`~/.m2`** (garde-manger) | **Créé tout seul** | Maven y range les `.jar` téléchargés depuis le Central |
| **`start.spring.io` (Initializr)** | **Site web, pas un install** | génère `pom.xml` + arborescence + wrapper |
| **`target/`** | **Généré, jamais commité** | recréé par `./mvnw package` (pendant Gradle : `build/`) |

L'**Étape 6** du fil rouge — **projet Maven buildable + test vert** — se construit dans la leçon 01, la leçon 02 ne migrant rien (voir `lecons/fil-rouge-signalcua.md`).

---

## 🛠️ Le vocabulaire transversal de la partie 6 (à lire si un mot vous échappe)

> Ces termes ne sont **pas** dans les mini-glossaires des leçons 01 et 02. Tous sont **utilisés** dans le texte, les commandes ou les corrections.

### A. Ce que Maven manipule sous le capot

| Terme | Définition complète |
|---|---|
| **XML / balise** | Format texte à `<balise>contenu</balise>` (cousin du HTML). Le `pom.xml` en est écrit. |
| **Classpath (`-cp`)** | La **liste des endroits** où `javac`/`java` cherchent les classes. Maven le construit pour vous. |
| **Surefire / compiler-plugin** | Les deux ouvriers rencontrés : `maven-compiler-plugin` **compile**, `maven-surefire-plugin` **lance les tests**. |
| **Changelog** | Le **journal des changements** d'une lib, version par version : à lire avant toute mise à jour. |
| **`~/.m2`** | Le dépôt local : `~` = dossier personnel, `.m2` = garde-manger Maven dedans. |
| **`mvnw.cmd` / `gradlew.bat`** | Les pendants **Windows** de `mvnw` / `gradlew` (même rôle). |
| **Packaging `Jar` (vs `War`)** | `Jar` = appli autonome (`java -jar`) ; `War` = archive pour serveur externe (ancien usage). |

### B. Ce que votre poste doit avoir (installé / fourni / généré)

| Terme | Définition complète |
|---|---|
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

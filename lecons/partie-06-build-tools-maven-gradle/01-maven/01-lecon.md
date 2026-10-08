# Leçon 01 — Maven : structurer et builder SignalCUA comme un vrai projet

> 🧭 **Pont depuis la partie 5** : à la fin de la partie 5, SignalCUA savait tout faire **en mémoire** : représenter des réclamations (parties 1-2), les stocker en collections (partie 3), échouer proprement (partie 4), les interroger en pipelines (partie 5). Mais tout ce code vivait encore **à la main** : quelques fichiers `.java` posés dans un dossier, compilés un par un avec `javac`, sans structure commune, sans dépendances gérées. Dès qu'on voudra utiliser une bibliothèque externe (JUnit pour tester, puis Spring Boot en partie 7), cette organisation manuelle ne tiendra plus. Cette leçon apporte le chaînon manquant : **Maven**, l'outil qui donne au projet une structure standard, déclare ses dépendances et le construit (**build**) de façon reproductible.

---

## 1. Objectifs d'apprentissage

À la fin de cette leçon, vous saurez :

- Expliquer **pourquoi** un outil de build est indispensable dès qu'on utilise des dépendances externes, et **quand** utiliser Maven plutôt que `javac` à la main.
- Décrire le rôle du fichier **`pom.xml`** (la recette du projet) : `groupId`, `artifactId`, `version`, `properties`, `dependencies`, `build`.
- Utiliser l'**arborescence standard** `src/main/java`, `src/main/resources`, `src/test/java` et y ranger SignalCUA en packages (`model`, `repository`, `exception`).
- Exécuter le **cycle de vie** : `compile`, `test`, `package`, `install` (et `clean`), et distinguer **phase**, **plugin** et **goal**.
- Choisir le bon **scope** de dépendance (`compile`, `test`, `provided`) et lire un arbre de dépendances avec `dependency:tree`.
- Démarrer un projet via **Spring Initializr** et builder avec le **wrapper** `./mvnw` sans installer Maven à la main.
- Écrire un premier **test JUnit 5** et le lancer avec `./mvnw test`.

---

## 2. Explication simple

### 2.1 Pourquoi un outil de build ? (le problème que `javac` seul ne règle pas)

Jusqu'ici, pour compiler vous faisiez quelque chose comme :

```text
javac Reclamation.java RegistreReclamations.java Main.java
java Main
```

Ça marche avec 5 fichiers. Mais imaginez la suite : vous voulez utiliser **JUnit** (LA bibliothèque de tests Java : elle exécute vos méthodes `@Test` et vérifie vos assertions ; détaillée en partie 9, ici juste le minimum), puis **Spring Boot** (le framework qui fera votre API en partie 7 : des dizaines de bibliothèques liées entre elles). Il faudrait alors **télécharger chaque fichier `.jar`** (un `.jar` = *Java Archive*, un zip de classes exécutable via `java -jar`) **à la main**, trouver les bonnes versions compatibles, les passer un par un à `javac` avec `-cp` (le **classpath** = la liste des endroits où `javac`/`java` cherchent les classes ; Maven le construira pour vous, voir 📖 Vocabulaire), recommencer sur chaque machine... C'est fragile et non reproductible.

Un outil de **build** (construction) automatise tout cela. **Analogie** : si `javac` est une **cuillère** (on mélange à la main), Maven est une **recette + un robot cuiseur** : la recette dit *quels ingrédients* (dépendances) et *quelles étapes* (compiler, tester, emballer), le robot va chercher les ingrédients, exécute les étapes **toujours dans le même ordre**, et rend le même plat sur toutes les machines.

> 📦 **Si vous venez de JavaScript/npm (optionnel, vous pouvez sauter)** : `package.json` ↔ `pom.xml` (la recette), `node_modules` ↔ dépôt local Maven `~/.m2` (le garde-manger : `~` = votre dossier personnel, `.m2` = réserve Maven dedans), `npm install` ↔ `./mvnw package` (va chercher + construit). Si vous ne venez pas de JS, ignorez ce parallèle : l'analogie **recette + robot cuiseur** ci-dessus suffit.

**Quand utiliser Maven ?** Dès que le projet a **plus de 2-3 fichiers**, ou **une seule dépendance externe**, ou doit être construit **ailleurs** que sur votre machine (CI = *Continuous Integration*, le serveur qui rebuild à chaque commit ; voir 📖 Vocabulaire — collègue, serveur). En pratique entreprise 2025-2026 : **toujours**, sauf exercice d'une page. C'est pourquoi la partie 7 (Spring Boot) l'exige : Spring Boot n'est qu'un paquet de dépendances Maven bien choisies.

> Pourquoi on passe de ce constat à la section suivante : une fois admis qu'il faut une « recette », il faut apprendre à la lire. C'est le rôle du `pom.xml`.

### 2.2 Le `pom.xml` : la carte d'identité + la recette

**POM** signifie *Project Object Model* (« modèle objet du projet ») : un fichier **XML** (format texte à balises comme `<balise>contenu</balise>`, cousin du HTML) posé à la **racine** du projet. Tout Maven part de lui.

```xml
<project> <!-- racine : tout le fichier vit dans cette balise -->
  <modelVersion>4.0.0</modelVersion> <!-- version du FORMAT pom, toujours 4.0.0 : ne pas toucher -->
  <groupId>fr.cua.signalcua</groupId> <!-- QUI fabrique : organisation, nom de domaine inversé -->
  <artifactId>signalcua</artifactId> <!-- QUOI : nom du projet -->
  <version>0.0.1-SNAPSHOT</version> <!-- QUELLE version : chantier (voir ci-dessous) -->
  <properties> <!-- réglages communs, écrits UNE fois -->
    <java.version>21</java.version> <!-- on vise Java 21 (LTS) -->
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding> <!-- accents gérés -->
  </properties>
  <dependencies> <!-- les INGRÉDIENTS externes -->
    <dependency> <!-- UN ingrédient : JUnit, pour les tests uniquement -->
      <groupId>org.junit.jupiter</groupId>
      <artifactId>junit-jupiter</artifactId>
      <version>5.11.3</version>
      <scope>test</scope> <!-- visible UNIQUEMENT en test (voir 2.5) -->
    </dependency>
  </dependencies>
</project>
```

Trois notions à fixer :

- **Les coordonnées `groupId` + `artifactId` + `version`** : l'**adresse postale** du projet. `groupId` = qui (`fr.cua.signalcua`, en minuscules), `artifactId` = quoi (`signalcua`), `version` = quelle édition. Elles identifient votre projet de façon unique, comme chaque dépendance téléchargée.
- **`SNAPSHOT`** : suffixe « version **chantier** ». `0.0.1-SNAPSHOT` = « la future 0.0.1, pas finie ». Maven la re-télécharge à chaque build. Une version sans `SNAPSHOT` (`1.0.0`) est **figée pour toujours**. Pendant le développement : toujours `SNAPSHOT` ; à la livraison : on fige.
- **`properties`** : des **variables** (nom + valeur) pour ne pas répéter `21` ou `UTF-8` à dix endroits. Passage à Java 25 demain ? **Une seule ligne** change.

> Pourquoi on passe d'ici à l'arborescence : le `pom.xml` dit *quoi* construire, mais Maven impose aussi *où* poser les fichiers.


### 2.3 L'arborescence standard : toujours les mêmes tiroirs

Maven impose une disposition **identique dans tous les projets du monde** :

```text
signalcua/                  <- racine : le pom.xml vit ici
  pom.xml                   <- la recette
  mvnw                      <- le wrapper (voir 2.7), généré, jamais écrit à la main
  src/
    main/
      java/                 <- VOTRE code (ex. fr/cua/signalcua/model/Reclamation.java)
      resources/            <- fichiers NON-java embarqués (config, textes)
    test/
      java/                 <- code de TEST (ex. RegistreReclamationsTest.java)
      resources/            <- fichiers servant aux tests
  target/                   <- résultat du build : GÉNÉRÉ, jamais commité
```

**Analogie** : comme une **pharmacie** où chaque médicament a son tiroir étiqueté, identique partout. Un développeur arrivant sur un projet Maven inconnu sait **instantanément** où est le code et où sont les tests, sans lire de doc.

**Rappel partie 1 (leçon packages 1.6)** : sous `src/main/java`, le code se range en **packages** (dossiers = espaces de noms). Pour SignalCUA : `fr.cua.signalcua.model` (les données : `Reclamation`, `StatutReclamation`, `Priorite`), `fr.cua.signalcua.repository` (le stockage : `RegistreReclamations`), `fr.cua.signalcua.exception` (les erreurs : `SignalcuaException` et filles). Le chemin dossier **reflète** le package : `fr/cua/signalcua/model/Reclamation.java` commence par `package fr.cua.signalcua.model;`. Maven impose le préfixe `src/main/java` ; vos packages restent votre organisation métier.

**Et `target/` ?** Le **plan de travail** du robot : fichiers compilés, rapports de tests, `.jar` final. Régénéré à chaque build : on ne le commite **jamais** (voir piège 1).

> Pourquoi on passe d'ici au cycle de vie : tiroirs rangés, recette écrite. Reste l'ordre de travail du robot.

### 2.4 Le cycle de vie : les mêmes étapes, dans le même ordre

Construire = enchaîner des étapes. Le **cycle de vie** (*lifecycle*) liste des **phases** exécutées **dans l'ordre**, chacune rejouant les précédentes :

```text
validate -> compile -> test -> package -> verify -> install
 vérifie    compile   lance    emballe   contrôles   installe dans
 le pom     le code   les tests  en .jar  qualité    le dépôt local
```

Concrètement : `./mvnw test` exécute `validate` + `compile` + `test`. `./mvnw package` va jusqu'à `package` : il **compile ET teste avant d'emballer** — un projet aux tests rouges ne produit **jamais** de `.jar`. `./mvnw install` copie le `.jar` fini dans le **dépôt local** (garde-manger `~/.m2`) pour d'autres projets de la machine.

**`clean`** = le **ménage** : supprime `target/`. Combinaison la plus courante : `./mvnw clean package` (« nettoie puis reconstruis tout »).

**Phase vs plugin vs goal (à ne plus confondre)** : la **phase** est une *étape du planning* (`compile`, `test`). Le **plugin** est l'*ouvrier* (`maven-compiler-plugin` compile, `maven-surefire-plugin` lance les tests — `compiler-plugin` compile, `surefire` lance les tests, voir 📖 Vocabulaire). Le **goal** est la *tâche précise* (`compiler:compile`). Le cycle **attache** des goals aux phases : demander la phase `test` appelle le goal `surefire:test`. Vous n'appellerez presque jamais un goal directement, sauf `dependency:tree` (voir 2.6).

> Pourquoi on passe d'ici aux scopes : le cycle compile et teste ; encore faut-il dire *quelles dépendances sont visibles à quelle étape*.

### 2.5 Les scopes : qui voit quoi, et quand

Chaque dépendance déclare un **scope** (portée = « à quelles étapes est-elle visible ? ») :

| Scope | Visible... | Exemple SignalCUA |
|---|---|---|
| `compile` (défaut, on ne l'écrit pas) | partout : compilation + tests + `.jar` final | une lib métier embarquée |
| `test` | **uniquement** pour compiler et lancer les tests | **JUnit** : utile en test, inutile dans le `.jar` livré |
| `provided` | compilation + tests, **mais PAS embarquée** (l'environnement la fournira) | API fournie par le serveur en production |
| `runtime` (mention) | PAS à la compilation, oui à l'exécution | un pilote de base de données chargé par réflexion |

**Analogie** : comme les **badges d'accès** d'un bâtiment. `compile` = badge complet (tous les étages). `test` = badge « salle de test uniquement ». `provided` = « apportez votre propre matériel, la salle fournit les tables ». Mettre JUnit en `compile` (oubli du scope) = laisser le matériel de test **dans le colis livré au client** : inutile et lourd (voir piège).

> Pourquoi on passe d'ici aux dépendances transitives : chaque dépendance amène ses propres dépendances. Il faut voir l'arbre entier.

### 2.6 Les dépendances transitives et le BOM : l'arbre et le menu unique

Quand vous déclarez JUnit, Maven télécharge aussi **ses propres dépendances** (moteur, assertions...). Ce sont les dépendances **transitives** (les invités des invités). Parfois deux invités exigent **deux versions différentes** du même plat : c'est un **conflit de versions**.

La commande qui montre tout :

```bash
./mvnw dependency:tree # affiche l'arbre COMPLET des dépendances résolues
```

Extrait typique (indentation = niveau) :

```text
fr.cua.signalcua:signalcua:jar:0.0.1-SNAPSHOT
+- org.junit.jupiter:junit-jupiter:jar:5.11.3:test
|  +- org.junit.jupiter:junit-jupiter-api:jar:5.11.3:test
|  \- org.junit.platform:junit-platform-commons:jar:1.11.3:test
```

**Analogie** : comme un **arbre généalogique** du projet. Quand « ça ne marche plus après ajout d'une lib », le premier réflexe pro = `dependency:tree` pour trouver le doublon.

Le **BOM** (*Bill of Materials*, « nomenclature ») règle le problème **en amont** : c'est un `pom.xml` spécial (ex. `spring-boot-dependencies`) qui **fixe d'un coup** toutes les versions compatibles d'un écosystème. L'importer (partie 7), c'est utiliser la **liste d'ingrédients validée par le chef** (même idée que le "menu du restaurant" Maven, mais à l'échelle d'un écosystème : versions testées ensemble) au lieu de composer plat par plat au risque d'incompatibilités. Sans BOM : versions incohérentes, erreurs bizarres.

> Pourquoi on passe d'ici au wrapper : l'arbre est sain, le menu est fixé. Reste à builder **sans installer Maven** sur chaque machine.





### 2.7 Le wrapper `mvnw` : Maven sans installer Maven

Le **wrapper** est un petit script (`mvnw` sur Linux/Mac, `mvnw.cmd` sur Windows — rappel de l'introduction de partie : `mvn` = votre install globale optionnelle, `mvnw` = le script fourni dans le projet) **versionné dans le projet** avec son dossier `.mvn/`. Au premier lancement, il **télécharge** la bonne version de Maven, puis rejoue votre commande. Ensuite, tout le monde (vous, CI, collègue) utilise **exactement la même version**.

```bash
./mvnw -version # le ./ dit : « prends le script D'ICI, pas un mvn global »
./mvnw clean package # nettoie, compile, teste, emballe : la commande de tous les jours
```

**Pourquoi `./` devant ?** Sur Linux, le dossier courant n'est pas dans le chemin de recherche par sécurité : `./mvnw` dit explicitement « le script de CE dossier ». Sans `./`, le shell chercherait un programme `mvnw` installé globalement et échouerait.

**Règle pro** : dans un projet avec wrapper, on n'appelle **jamais** `mvn` global — toujours `./mvnw`. C'est lui qui garantit la reproductibilité.

> Pourquoi on passe d'ici à Initializr : le wrapper transporte Maven ; Initializr fournit le projet de départ déjà câblé avec.

### 2.8 Spring Initializr : ne jamais écrire un `pom.xml` Spring Boot à la main

**Spring Initializr** (`start.spring.io`) est le **générateur officiel** de projets Spring Boot : on choisit le langage (Java), la version (21), le packaging (`Jar`), les dépendances de départ, et on télécharge un **zip** contenant `pom.xml` + arborescence + wrapper + classe `main`. 

Marche à suivre (phase Maven pure : on génère SANS cocher de dépendance `web`, Spring ne démarrera pas encore) :

1. Aller sur `start.spring.io`.
2. Project : **Maven**, Language : **Java**, Spring Boot : version **stable** proposée (3.x), Packaging : **Jar**, Java : **21**.
3. Group : `fr.cua.signalcua`, Artifact : `signalcua`.
4. Dependencies : **aucune** pour l'instant (JUnit est inclus par défaut via `spring-boot-starter-test` quand on génèrera le vrai projet en partie 7 ; ici on ajoute JUnit à la main pour comprendre).
5. **Generate** : dézipper, ouvrir le dossier, lancer `./mvnw test`.

**Pourquoi c'est la voie obligatoire ?** Parce qu'un `pom.xml` Spring Boot correct exige le **parent `spring-boot-starter-parent`** (qui apporte le BOM + les réglages de plugins) avec des versions compatibles au patch près. L'écrire à la main = erreurs garanties. Initializr = recette validée par l'équipe Spring.

> Pourquoi on passe d'ici au test : le projet est généré et buildable. Première chose utile à en faire : un test vert.

### 2.9 Premier test JUnit 5 : prouver que le registre marche

**JUnit** est LA bibliothèque de tests Java. **JUnit 5** (nom de code **Jupiter**) est la version moderne : on l'utilise via annotations. Un **test** = une méthode annotée `@Test` qui **vérifie** (assertion) un comportement et **échoue** si la réalité diffère. Détail complet en partie 9 ; ici le minimum vital.

```java
package fr.cua.signalcua.repository; // MÊME package que la classe testée (accès direct)

import fr.cua.signalcua.model.Priorite; // l'enum des urgences (partie 2)
import fr.cua.signalcua.model.Reclamation; // la donnée testée
import fr.cua.signalcua.model.StatutReclamation; // l'enum des statuts
import org.junit.jupiter.api.Test; // @Test = « cette méthode EST un test »
import static org.junit.jupiter.api.Assertions.assertEquals; // vérifie une égalité
import static org.junit.jupiter.api.Assertions.assertThrows; // vérifie qu'une exception est levée

class RegistreReclamationsTest { // par convention : ClasseTestée + Test, dans src/test/java
    @Test // JUnit exécute cette méthode comme un test
    void creerPuisRetrouver() { // nom en français, phrase métier : on comprend SANS lire le corps
        RegistreReclamations registre = new RegistreReclamations(); // ARRANGE : on prépare
        registre.ajouter(new Reclamation(1, "Medina", Priorite.NORMALE, // ACT : on agit
                StatutReclamation.NOUVELLE, "Nid de poule"));
        assertEquals(1, registre.findById(1).getId()); // ASSERT : on vérifie (attendu, réel)
    }

    @Test
    void introuvableLeve() { // findById lève au lieu de rendre null (partie 4, leçon 02)
        RegistreReclamations registre = new RegistreReclamations();
        assertThrows(ReclamationNotFoundException.class, // on EXIGE cette exception...
                () -> registre.findById(999)); // ...quand on cherche un id absent (lambda, partie 5)
    }
}
```

Lancement et sortie attendue :

---

## 📖 Vocabulaire / Abréviations

> Les mots de l'usine, fixés avant les exemples : chacun réapparaîtra en section 3 puis dans l'exercice.

| Terme | Définition en une ligne |
|---|---|
| **Build** | L'enchaînement compiler → tester → emballer, exécuté par l'outil (pas à la main). |
| **POM** | *Project Object Model* : le `pom.xml`, recette + carte d'identité du projet. |
| **Artifact** | Le livrable (ici un `.jar`) — ou toute bibliothèque identifiée par ses coordonnées. |
| **`groupId` / `artifactId` / `version`** | Les **coordonnées** : qui / quoi / quelle édition. |
| **`SNAPSHOT`** | Suffixe « version chantier » : re-téléchargée à chaque build, jamais figée. |
| **Dépendance transitive** | La dépendance d'une dépendance, téléchargée automatiquement. |
| **Scope** | La **portée** d'une dépendance (`compile`, `test`, `provided`). |
| **BOM** | *Bill of Materials* : le « menu du chef » qui fixe des versions compatibles. |
| **Phase / Plugin / Goal** | L'étape du planning / l'ouvrier / sa tâche précise. |
| **Wrapper (`mvnw`)** | Le script versionné qui fige la version de Maven du projet. |
| **Initializr** | Le générateur officiel (`start.spring.io`) de projet Spring Boot. |
| **Dépôt central / local** | Le supermarché mondial des `.jar` / votre garde-manger (`~/.m2`). |
| **`target/`** | Le plan de travail généré : à ignorer dans Git, jamais commité. |
| **`.jar`** | *Java Archive* : zip de classes exécutable via `java -jar`. |
| **JUnit / Jupiter** | LA bibliothèque de tests Java / nom de code de JUnit 5 (`@Test`). |
| **Assertion** | Une vérification dans un test : échoue si faux. |
| **CVE** | *Common Vulnerabilities and Exposures* : une faille référencée. |
| **CI** | *Continuous Integration* : le serveur qui rebuild à chaque commit. |


```bash
./mvnw test # compile le code + les tests, puis exécute les tests (plugin Surefire)
```

```text
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

Lisez : « 2 tests lancés, 0 échec, 0 erreur : build OK ». Un test rouge afficherait `FAILURE` + le nom du test + l'assertion violée, et **bloquerait** `package` (pas de `.jar`).

**Le cycle ARRANGE-ACT-ASSERT** (préparer-agir-vérifier) : le squelette mental de TOUS les tests à venir (partie 9). Retenez-le dès maintenant.


---

## 3. Exemples concrets

> La section 2 a posé la recette, les tiroirs et les étapes ; ici, on **construit** SignalCUA pour de vrai.

### 3.1 Le `pom.xml` SignalCUA complet et commenté

Le fichier à poser à la racine (chaque bloc déjà expliqué en 2.2, ici en version copiable) :

```xml
<?xml version="1.0" encoding="UTF-8"?> <!-- en-tête XML : version + encodage -->
<project xmlns="http://maven.apache.org/POM/4.0.0"> <!-- espace de noms Maven -->
  <modelVersion>4.0.0</modelVersion> <!-- format du pom : toujours 4.0.0 -->
  <groupId>fr.cua.signalcua</groupId> <!-- QUI : l'organisation -->
  <artifactId>signalcua</artifactId> <!-- QUOI : le projet -->
  <version>0.0.1-SNAPSHOT</version> <!-- chantier : re-résolue à chaque build -->
  <name>signalcua</name> <!-- nom lisible (logs, IDE) -->
  <description>Gestion des reclamations citoyennes (CUA)</description>
  <properties> <!-- variables communes -->
    <java.version>21</java.version> <!-- cible Java 21 LTS -->
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
  </properties>
  <dependencies> <!-- ingrédients -->
    <dependency> <!-- JUnit 5 : tests uniquement -->
      <groupId>org.junit.jupiter</groupId>
      <artifactId>junit-jupiter</artifactId>
      <version>5.11.3</version> <!-- édition stable -->
      <scope>test</scope> <!-- tests seuls, pas dans le .jar -->
    </dependency>
  </dependencies>
  <build> <!-- fabrication -->
    <plugins>
      <plugin> <!-- ouvrier compilateur, réglé sur Java 21 -->
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-compiler-plugin</artifactId>
        <configuration>
          <source>21</source> <!-- syntaxe acceptée : 21 -->
          <target>21</target> <!-- bytecode produit : 21 -->
        </configuration>
      </plugin>
    </plugins>
  </build>
</project>
```

### 3.2 Migrer les classes dans l'arborescence (rappel packages 1.6)

Avant (phase console) : tout dans un dossier. Après :

```text
src/main/java/fr/cua/signalcua/model/Reclamation.java
src/main/java/fr/cua/signalcua/model/StatutReclamation.java
src/main/java/fr/cua/signalcua/model/Priorite.java
src/main/java/fr/cua/signalcua/repository/RegistreReclamations.java
src/main/java/fr/cua/signalcua/exception/SignalcuaException.java
src/main/java/fr/cua/signalcua/exception/ReclamationNotFoundException.java
src/test/java/fr/cua/signalcua/repository/RegistreReclamationsTest.java
```

Règle de migration : **on déplace, on ne réécrit pas** — seule la première ligne (`package ...;`) et les `import` inter-packages changent. La logique (transitions, `Optional`, streams) reste identique : Maven structure, il ne modifie pas.

### 3.3 Les 4 commandes du quotidien et le `.gitignore`

> On a la recette (§3.1) et les tiroirs (§3.2) : il reste le geste quotidien — une commande = tout le build (les sorties complètes sont rejouées dans la correction).

```bash
./mvnw test # 1) compile le code + les tests, puis exécute les tests (plugin Surefire)
./mvnw package # 2) tout jusqu'au .jar : target/signalcua-0.0.1-SNAPSHOT.jar
java -jar target/signalcua-0.0.1-SNAPSHOT.jar # 3) exécute le livrable
./mvnw clean package # 4) reconstruction complète depuis zéro (ménage + build)
```

Fichier `.gitignore` (à la racine, à côté du `pom.xml`) :

```text
target/ # le plan de travail ne se commite jamais
```

---

## 4. Bonnes pratiques modernes (2025-2026)

> Ces règles sont celles des équipes Java/Spring aujourd'hui ; l'exercice les applique toutes.

- **Toujours Initializr d'abord** : jamais de `pom.xml` Spring Boot écrit à la main (parent + BOM compatibles garantis).
- **Toujours `./mvnw`, jamais `mvn` global** : la version de Maven fait partie du projet (reproductibilité, CI).
- **Java 21 LTS + JUnit 5 (`jupiter`) uniquement** : JUnit 4 est legacy ; ne pas mélanger les deux.
- **BOM dès Spring Boot** : `spring-boot-dependencies` (via le parent) fixe les versions compatibles — ne pas surcharger une version sans raison notée en commentaire.
- **`target/` ignoré + wrapper versionné** : `target/` dans `.gitignore` ; `mvnw` + `.mvn/` committés.
- **Zéro dépendance « au cas où »** : chaque dépendance = surface CVE + poids du build. On ajoute quand on utilise, on retire quand on n'utilise plus.
- **`versions:display-dependency-updates` régulier** (roadmap §6.1) : revue mensuelle, changelog lu, tests verts avant de figer.
- **Un test vert avant chaque `package`** : le cycle impose les tests ; ne jamais les sauter (`-DskipTests`) sauf build de diagnostic local et temporaire.

> Pourquoi on passe d'ici aux pièges : les règles sont posées ; voyons ce qui casse quand on les ignore.

## 5. Pièges à éviter

### Piège 1 — Committer `target/`

```text
# ❌ MAUVAIS : git add target/ (des centaines de .class + .jar versionnés, conflits garantis)
# ✅ BON : .gitignore contient target/ ; seul le code + pom.xml sont versionnés
```

**Pourquoi** : `target/` est **régénérable** (`./mvnw package` le reconstruit) et **dépend de la machine** (chemins, horodatage). Le versionner = bruit + conflits + dépôt obèse.

### Piège 2 — Copier-coller des dépendances sans `dependency:tree`

```xml
<!-- ❌ MAUVAIS : 3 libs ajoutées depuis un blog, versions au hasard -->
<!-- ✅ BON : ajouter UNE dépendance, lancer ./mvnw dependency:tree, vérifier l'arbre -->
```

**Pourquoi** : chaque dépendance amène ses transitives ; deux versions du même artifact = comportement imprévisible (la première gagnante l'emporte silencieusement). L'arbre rend le conflit **visible**.

### Piège 3 — Versions incohérentes sans BOM

```xml
<!-- ❌ MAUVAIS : spring-core 6.1.8 + spring-context 6.0.5 (mélange de générations) -->
<!-- ✅ BON : parent spring-boot-starter-parent (BOM) : UNE version pilote tout l'écosystème -->
```

**Pourquoi** : les libs d'un écosystème sont testées **ensemble** à versions fixées. Mélanger = `NoSuchMethodError` à l'exécution (méthode présente à la compilation, absente au runtime).

### Piège 4 — JUnit en scope `compile` (ou sans scope)

```xml
<!-- ❌ MAUVAIS : junit-jupiter SANS <scope>test</scope> (embarqué dans le .jar livré) -->
<!-- ✅ BON : <scope>test</scope> (présent en test, absent du livrable) -->
```

**Pourquoi** : le scope par défaut est `compile` = embarqué. Livrer JUnit au client = poids + surface d'attaque inutiles.

### Piège 5 — Écrire le `pom.xml` Spring Boot à la main

```text
# ❌ MAUVAIS : pom.xml recopié d'un tutoriel 2021 (javax.*, Spring Boot 2.x, Java 8)
# ✅ BON : start.spring.io -> Java 21, Boot 3.x (jakarta.*), packaging Jar
```

**Pourquoi** : un tutoriel pré-2023 utilise `javax.*` (ancien nom) alors que Boot 3.x exige `jakarta.*` (roadmap §7.2). Initializr élimine toute cette classe d'erreurs.

### Piège 6 — `mvn` global au lieu du wrapper

```bash
# ❌ MAUVAIS : mvn package (version de LA machine : 3.6 ici, 3.9 là-bas, résultats différents)
./mvnw package # ✅ BON : version DU projet, identique partout
```

**Pourquoi** : « ça marche chez moi » vient presque toujours d'une différence d'outil. Le wrapper fige l'outil comme le `pom.xml` fige les libs.

### 3.4 Lire l'arbre et traquer les mises à jour (rappel §2.6, ici en version copiable)

```bash
./mvnw dependency:tree # QUI a amené QUOI : premier réflexe en cas de conflit
./mvnw versions:display-dependency-updates # QUOI mettre à jour (sécurité : CVE)
```

On ne met jamais à jour aveuglément : on lit le changelog (journal des changements), on teste, puis on fige.

**Et les 4 commandes du quotidien (§3.3, rappel en une table pour l'exercice) :**

```bash
./mvnw test # 1) compiler + lancer les tests (retour rapide pendant le dev)
./mvnw package # 2) tout jusqu'au .jar : target/signalcua-0.0.1-SNAPSHOT.jar
java -jar target/signalcua-0.0.1-SNAPSHOT.jar # 3) exécuter le livrable
./mvnw clean package # 4) reconstruction complète depuis zéro
```

Fichier `.gitignore` (à la racine, à côté du `pom.xml`) :

```text
target/ # le plan de travail ne se commite jamais
```


---

## Checklist de validation

Avant de passer à l'exercice, vérifiez que vous savez faire **chacun** de ces points :

- [ ] Expliquer pourquoi `javac` à la main ne suffit plus dès qu'il y a une dépendance externe (et faire le parallèle `pom.xml` ↔ `package.json`).
- [ ] Lire un `pom.xml` : citer les coordonnées, les `properties`, les `dependencies`, le `build`.
- [ ] Ranger du code dans `src/main/java` / `src/test/java` avec les packages `model`, `repository`, `exception`.
- [ ] Prédire ce qu'exécute `./mvnw test` vs `./mvnw package` vs `./mvnw install` (et le rôle de `clean`).
- [ ] Distinguer **phase** (étape), **plugin** (ouvrier), **goal** (tâche) avec un exemple.
- [ ] Choisir `compile` / `test` / `provided` pour JUnit, une lib métier, une API fournie par le serveur.
- [ ] Lire un `dependency:tree` et expliquer BOM (« menu du chef ») avec vos mots.
- [ ] Justifier `./mvnw` (pas `mvn`) et Initializr (pas de `pom.xml` Spring Boot à la main).
- [ ] Écrire un test `@Test` + `assertEquals` / `assertThrows` et interpréter `Tests run: 2, Failures: 0`.

---

## 🔴 Fil rouge — où en est SignalCUA ?

SignalCUA quitte le mode « fichiers en vrac » : même code métier (réclamations, registre, exceptions), mais rangé en **vrai projet Maven** (`fr.cua.signalcua.*`), construit en **une commande** (`./mvnw package`), avec un **premier test vert** qui prouve que le registre marche. L'exercice 01 réalise exactement cette migration. Il restera à savoir lire l'outil concurrent (Gradle, leçon 02), puis Spring Boot prendra ce projet tel quel en partie 7.

---

➡️ **Prochaine étape** : l'**exercice 01** — migrer SignalCUA sous Maven + 1 test JUnit vert. Puis la **leçon 02 — Gradle (aperçu)** : le même besoin, une autre philosophie ; objectif = savoir lire, pas tout maîtriser.


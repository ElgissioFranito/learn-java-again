# Correction détaillée — Exercice 02 « Lire le jumeau Gradle »

> 🧭 **Comment ce fichier s'articule** : vous venez de tenter `02-exercice.md` (5 questions de lecture sur le `build.gradle.kts` SignalCUA). Voici les réponses ligne par ligne, chacune rattachée à sa notion Maven. Exercice de **lecture seule** : rien à exécuter (aucun code Java neuf, juste un fichier de build à décoder) — la « vérification » est la confrontation de vos réponses à celles-ci.

## Correction pas à pas

### Question 1 — Les coordonnées : `fr.cua.signalcua:signalcua:0.0.1-SNAPSHOT`

- `group = "fr.cua.signalcua"` (dans `build.gradle.kts`) = le `<groupId>` Maven.
- `rootProject.name = "signalcua"` (dans `settings.gradle.kts`) = le `<artifactId>` Maven. **Pourquoi deux fichiers ?** Gradle sépare l'enseigne (`settings` : nom + modules) de la recette (`build` : quoi construire) — en Maven les deux vivent dans le `pom.xml`.
- `version = "0.0.1-SNAPSHOT"` = la `<version>` Maven (chantier, même sens).
- Triplet : `fr.cua.signalcua:signalcua:0.0.1-SNAPSHOT` — **identique** au `pom.xml` : même projet, deux habits.

### Question 2 — JUnit : fourni par Maven Central, visible en test seul

1. `repositories { mavenCentral() }` = le supermarché mondial des `.jar`, **le même** que Maven utilise par défaut (en Maven il est implicite, en Gradle on l'écrit).
2. Découpage de `testImplementation("org.junit.jupiter:junit-jupiter:5.11.3")` : `test` = portée test (comme `<scope>test</scope>`) ; `Implementation` = embarquée au runtime **de son périmètre** (ici : le runtime des tests) ; `org.junit.jupiter` = group, `junit-jupiter` = artifact, `5.11.3` = version (les `:` séparent, comme le triplet projet).
3. Traduction Maven : `<scope>test</scope>` → JUnit **absent** du livrable (`build/libs/*.jar`), exactement comme `target/*.jar` en Maven.

### Question 3 — Java 21, installé par Gradle lui-même

`languageVersion.set(JavaLanguageVersion.of(21))` = cible Java 21 (pendant de `<source>/<target>21`). Le bloc `toolchain` (« outillage exact ») signifie : si le JDK 21 manque, **Gradle le télécharge lui-même** — la machine n'a pas besoin du bon JDK d'avance. En Maven, c'est l'inverse : `JAVA_HOME` de la machine doit pointer vers un JDK 21.

### Question 4 — Les 5 paires de commandes

| Besoin | Gradle | Maven |
|---|---|---|
| Lister les étapes | `./gradlew tasks --all` | (fixe : cycle `validate → ... → install` connu par cœur) |
| Compiler + tester | `./gradlew test` | `./mvnw test` |
| Livrer | `./gradlew build` (livrable : `build/libs/*.jar`) | `./mvnw package` (livrable : `target/*.jar`) |
| Nettoyer | `./gradlew clean` (supprime `build/`) | `./mvnw clean` (supprime `target/`) |
| Arbre des dépendances | `./gradlew dependencies` | `./mvnw dependency:tree` |

**Méthode à garder** : `tasks --all` → `dependencies` → `build.gradle.kts` (étapes custom ? qui amène quoi ? quelle version Java ?).

### Question 5 — Refuser la migration (exemple de réponse complète)

« SignalCUA se build en quelques secondes avec un `pom.xml` standard qu'Initializr a généré : le gain Gradle serait **inmesurable**. La migration coûterait la relecture d'un build programmable sur mesure pour **zéro besoin** (pas de monorepo, pas de Kotlin, pas de lenteur). J'accepterai Gradle le jour où `package` dépassera la minute **mesurée**, où le projet deviendra multi-modules, ou où un projet imposé me l'amènera — et ce jour-là, je saurai le lire grâce à cette leçon. »

## Erreurs fréquentes et comment les reconnaître

- Chercher l'`artifactId` dans `build.gradle.kts` : il est dans `settings` (`rootProject.name`). **Remède** : enseigne ≠ recette.
- Lire `testImplementation` comme « test seulement à la compilation » : `Implementation` inclut le runtime **du périmètre test**. **Remède** : préfixe = portée, suffixe = embarquement.
- Confondre `build/` et `target/` : `build/` = Gradle, `target/` = Maven. Les deux s'ignorent dans Git.
- Vouloir « tout maîtriser » : l'objectif est la **lecture**, pas l'écriture (roadmap : aperçu).

## Checklist de validation

- [ ] Triplet `g:a:v` reconstitué depuis 2 fichiers, égalité Maven constatée.
- [ ] Dépôt + dépendance découpés, scope `test` traduit, non-embarquement justifié.
- [ ] Java 21 + `toolchain` expliqués (Gradle gère le JDK).
- [ ] 5 paires de commandes citées sans hésiter.
- [ ] Choix Maven-pour-SignalCUA argumenté en 3 phrases + cas futurs cités.

## Conseils pour progresser

- Gardez le tableau §2.1 + la table Q4 sous les yeux : ce sont vos deux antisèches Gradle pour les mois à venir.
- Face à un vrai projet Gradle imposé un jour : appliquez la méthode (`tasks --all` → `dependencies` → fichier) avant de toucher quoi que ce soit.
- Prochaine étape : le `00-Introduction-partie.md` (filet de rattrapage), puis la partie 7 démarre Spring Boot sur votre projet Maven.

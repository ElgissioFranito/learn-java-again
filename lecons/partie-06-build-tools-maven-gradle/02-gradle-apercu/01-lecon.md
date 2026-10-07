# Leçon 02 — Gradle (aperçu) : savoir lire l'autre outil

> 🧭 **Pont depuis la leçon 01** : vous savez structurer un projet avec Maven (recette `pom.xml`, tiroirs `src/...`, étapes `compile` → `test` → `package`, wrapper `./mvnw`). Mais en entreprise, certains projets — surtout gros monorepos, projets Kotlin ou builds où la vitesse compte — utilisent **Gradle**, l'autre grand outil de build Java. Cette leçon ne vous rend pas expert Gradle : elle vous apprend à **lire** un projet Gradle, à **retrouver** chaque notion Maven dedans, et à **choisir** entre les deux. La roadmap le dit explicitement : *à voir, pas à approfondir maintenant*.

---

## 1. Objectifs d'apprentissage

À la fin de cette leçon, vous saurez :

- Expliquer en une phrase ce qu'est Gradle et **quand** le choisir plutôt que Maven (et inversement).
- Distinguer `build.gradle` (Groovy) de `build.gradle.kts` (Kotlin DSL, recommandé 2025-2026).
- Retrouver dans un `build.gradle.kts` chaque notion Maven : coordonnées, dépendances, scopes, version Java, tâche de build.
- Exécuter `./gradlew tasks`, `./gradlew test`, `./gradlew build` en comprenant l'équivalence avec `./mvnw`.
- Justifier le choix **Maven par défaut** pour vos projets CUA solo, et dire dans quel cas vous basculeriez sur Gradle.

---

## 2. Explication simple

### 2.1 Gradle en une phrase (et pourquoi il existe à côté de Maven)

**Gradle** est un outil de build qui fait **le même métier** que Maven (dépendances + étapes + livrable), mais avec une philosophie différente : là où Maven impose une recette **rigide et déclarative** (XML : on décrit *quoi*, l'ordre est fixé), Gradle propose un **script programmable** (on décrit *quoi* et, si besoin, *comment*, avec de la vraie logique : conditions, boucles, calculs).

**Analogie** : Maven = **menu du restaurant** (entrée-plat-dessert dans l'ordre, pas de surprise). Gradle = **cuisine équipée** (mêmes ingrédients possibles, mais vous pouvez inventer vos propres recettes et ustensiles). Le menu est plus rassurant seul ; la cuisine équipée paie quand on cuisine pour 50 tables (gros monorepo) ou quand chaque seconde compte (cache incrémental : Gradle ne refait que ce qui a changé).

**Le lien avec npm, encore** : si Maven ≈ `package.json` rigide, Gradle ≈ un `package.json` + des scripts `node` sur mesure. Même besoin, curseur différent entre *convention* (Maven) et *flexibilité* (Gradle).

**Quand choisir quoi ?** Tableau de décision (à garder) :

| Situation | Choix | Pourquoi |
|---|---|---|
| Projet solo / équipe petite, Spring Boot classique (vos projets CUA) | **Maven** | lisible, prévisible, standard entreprise, Initializr le génère |
| Gros monorepo multi-modules, builds de plusieurs minutes | **Gradle** | cache incrémental + parallélisme = builds bien plus rapides |
| Écosystème Kotlin (KMP, Compose) | **Gradle** | l'outil natif du monde Kotlin/Android |
| On vous impose un projet Gradle existant | **Gradle** | on ne réécrit pas un build qui marche : on apprend à le lire |

> Pourquoi on passe d'ici aux fichiers : le choix est fait (Maven par défaut, lecture de Gradle en second). Voyons à quoi ressemble ce second outil.

### 2.2 `build.gradle` vs `build.gradle.kts` : deux syntaxes, un seul rôle

Un projet Gradle se reconnaît à son fichier de build à la racine (le pendant du `pom.xml`) :

- **`build.gradle`** : écrit en **Groovy** (un langage souple sur la JVM). Syntaxe très courte, mais permissive : deux écritures différentes peuvent faire la même chose, ce qui complique la relecture en équipe.
- **`build.gradle.kts`** : écrit en **Kotlin DSL** (*Domain Specific Language* = un mini-langage dédié au build, avec autocomplétion et vérification de types). Plus verbeux, mais **prévisible** : l'IDE signale les erreurs avant l'exécution.

**Recommandation 2025-2026 : `build.gradle.kts`** pour tout nouveau projet Gradle (c'est le défaut d'IntelliJ et d'Android Studio). Si vous tombez sur un vieux `build.gradle` Groovy, sachez le lire (mêmes blocs, guillemets simples au lieu de doubles le plus souvent), sans l'imiter pour du neuf.

Les autres fichiers familiers : `settings.gradle.kts` (le **nom** du projet + la liste des modules, comme l'enseigne du bâtiment), `gradlew` / `gradlew.bat` (le **wrapper**, pendant exact de `mvnw` : même règle, toujours `./gradlew`, jamais `gradle` global), dossier `build/` (le pendant de `target/` : généré, jamais commité).

> Pourquoi on passe d'ici à la lecture guidée : vous connaissez les noms. Reste à ouvrir un vrai fichier et à y retrouver Maven.

### 2.3 Lire un `build.gradle.kts` avec des yeux Maven (le cœur de la leçon)

Voici le pendant exact du `pom.xml` SignalCUA de la leçon 01, en Kotlin DSL. Chaque bloc renvoie à sa notion Maven :

```kotlin
plugins { // les OUVRIERS (pendant du <build> Maven) : ici le plugin Java + l'application
    java // sait compiler et tester du Java (comme compiler-plugin + surefire-plugin réunis)
    application // sait produire un livrable exécutable (comme package + main déclaré)
}
group = "fr.cua.signalcua" // QUI : le groupId Maven, même valeur, autre syntaxe
version = "0.0.1-SNAPSHOT" // QUELLE édition : chantier, comme en Maven
repositories { // D'OÙ viennent les dépendances : le supermarché (pendant du dépôt central)
    mavenCentral() // Maven Central, le même que Maven utilise par défaut
}
dependencies { // les INGRÉDIENTS (pendant de <dependencies>)
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.3") // JUnit, tests seuls
}
java { // réglage du compilateur (pendant de <source>/<target> = 21)
    toolchain { // toolchain = « la tronçonneuse exacte » : Gradle peut télécharger le JDK 21 lui-même
        languageVersion.set(JavaLanguageVersion.of(21)) // on vise Java 21, comme en Maven
    }
}
```

Lisez la dépendance `testImplementation("org.junit.jupiter:junit-jupiter:5.11.3")` par morceaux : `test` = scope test (comme `<scope>test</scope>`), `Implementation` = « embarquée au runtime » (détail : il existe aussi `testCompileOnly`, `testRuntimeOnly` — même idée que les scopes Maven, découpée plus fin), puis les coordonnées `group:artifact:version` **en une seule chaîne** avec des `:` (au lieu de 3 balises XML).

**Équivalences de commandes** (à retenir) :

| Besoin | Maven | Gradle |
|---|---|---|
| Voir les étapes disponibles | (fixe : cycle connu) | `./gradlew tasks` (liste les **tâches**) |
| Compiler + tester | `./mvnw test` | `./gradlew test` |
| Tout jusqu'au livrable | `./mvnw package` | `./gradlew build` |
| Nettoyer | `./mvnw clean` | `./gradlew clean` |
| Voir l'arbre des dépendances | `./mvnw dependency:tree` | `./gradlew dependencies` |

**Phase vs tâche** : Maven a des **phases** fixes (`test`, `package`) ; Gradle a des **tâches** (`tasks`) que chaque projet peut **redéfinir et enrichir** (`build` dépend de `test`, qui dépend de `compileJava`...). C'est cette programmabilité qui fait la puissance — et le danger : un build Gradle sur mesure mal documenté est plus dur à déboguer qu'un `pom.xml` standard.

### 2.4 Ce qu'on ne fait PAS maintenant (secondaire assumé)

Volontairement hors périmètre (partie 13 les citera « à connaître de nom » si besoin) : écrire un plugin custom, configurer le cache distant (`build cache`), le parallélisme fin (`--parallel`, `configuration cache`), les builds multi-modules complexes, la publication sur Nexus/Maven Central. **Une phrase chacun suffit** : ce sont des optimisations d'équipes avancées, pas des prérequis pour builder un Spring Boot CUA.

> Pourquoi on passe d'ici au vocabulaire : le cœur (lire Gradle) est acquis, le secondaire est borné. On fixe les mots, puis on lit pour de vrai.

---

## 📖 Vocabulaire / Abréviations

> Les mots Gradle, fixés avant les exemples : chacun est défini par son équivalent Maven (zéro étonnement).

| Terme | Définition en une ligne |
|---|---|
| **Gradle** | L'outil de build programmable (script Groovy/Kotlin), concurrent de Maven. |
| **`build.gradle` / `build.gradle.kts`** | Le fichier de build Gradle, en Groovy / en Kotlin DSL (recommandé). |
| **Kotlin DSL** | Le mini-langage de build typé (autocomplétion, erreurs détectées tôt). |
| **Groovy** | Le langage souple historique des builds Gradle (court mais permissif). |
| **`settings.gradle.kts`** | Le fichier « enseigne » : nom du projet + liste des modules. |
| **`gradlew`** | Le wrapper Gradle : même rôle et même règle que `mvnw`. |
| **Tâche (*task*)** | L'unité de travail Gradle (`compileJava`, `test`, `build`) : programmable et chaînable. |
| **`implementation` / `testImplementation`** | Les configurations de dépendances : embarquée / embarquée en test seul. |
| **`repositories { mavenCentral() }`** | La déclaration du supermarché de `.jar` (le même que Maven). |
| **Toolchain** | Le mécanisme qui fait télécharger le bon JDK par Gradle lui-même. |
| **Cache incrémental** | Gradle ne refait que ce qui a changé : d'où sa vitesse sur gros projets. |
| **`build/`** | Le plan de travail généré (pendant de `target/`) : jamais commité. |
| **Monorepo** | Un seul dépôt Git contenant plusieurs modules/projets liés. |
| **KMP** | *Kotlin Multiplatform* : du Kotlin partagé entre plateformes (raison pro-Gradle). |

---

## 3. Exemples concrets

> La section 2 a donné les équivalences ; ici, on **lit** un projet Gradle SignalCUA pour de vrai.

### 3.1 Le `build.gradle.kts` SignalCUA commenté, face au `pom.xml`

Reprenez le fichier du §2.3 (copiable tel quel). Sous chaque bloc, sa traduction Maven :

```kotlin
plugins { // = <build><plugins> : les ouvriers
    java // = maven-compiler-plugin + surefire-plugin (compiler + tester)
    application // = package jar + classe main déclarée
}
group = "fr.cua.signalcua" // = <groupId> (l'artifactId vient de settings.gradle.kts)
version = "0.0.1-SNAPSHOT" // = <version> (chantier, même sens)
repositories { // = dépôt central (implicite en Maven, explicite ici)
    mavenCentral() // le même supermarché
}
dependencies { // = <dependencies>
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.3") // = JUnit + <scope>test</scope>
}
java { // = <source>/<target> 21
    toolchain { // Gradle peut installer le JDK 21 tout seul
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}
```

Le `settings.gradle.kts` qui va avec (2 lignes) :

```kotlin
rootProject.name = "signalcua" // = <artifactId> : le nom du projet
```

Et le `.gitignore` (pendant du Maven) :

```text
build/ # le plan de travail Gradle ne se commite jamais (comme target/)
```

### 3.2 Les 3 commandes qui suffisent à lire un projet inconnu

```bash
./gradlew tasks --all # 1) TOUTES les tâches : la table des matières du build sur mesure
./gradlew test # 2) compiler + tester (comme ./mvnw test)
./gradlew build # 3) tout jusqu'au livrable dans build/libs/*.jar (comme ./mvnw package)
```

**Méthode de lecture** (à appliquer à tout projet Gradle imposé) : `tasks --all` d'abord (quelles tâches custom ?), `dependencies` ensuite (qui amène quoi ?), `build.gradle.kts` enfin (quelle version Java ? quels plugins ?). Dans cet ordre, jamais d'étonnement.

---

## 4. Bonnes pratiques modernes (2025-2026)

- **Kotlin DSL (`.kts`) pour tout neuf** : typé, autocomplété, erreurs vues dans l'IDE — Groovy seulement en lecture d'ancien projet.
- **Toujours `./gradlew`, jamais `gradle` global** : même règle que `mvnw` (reproductibilité, CI).
- **`build/` ignoré + wrapper versionné** : `build/` dans `.gitignore` ; `gradlew` + `gradle/wrapper/` committés.
- **Toolchain Java 21** : le JDK fait partie du build (plus de « mauvais JDK sur la machine »).
- **Ne pas convertir un projet Maven qui marche** : Gradle se justifie par un besoin (vitesse, monorepo, Kotlin), pas par la mode.
- **Documenter toute tâche custom** : une tâche sur mesure sans commentaire = un piège pour le futur vous (et la raison pour laquelle Maven reste préférable en solo).

> Pourquoi on passe d'ici aux pièges : mêmes familles d'erreurs que Maven, avec la souplesse en plus.

## 5. Pièges à éviter

### Piège 1 — Committer `build/`

```text
# ❌ MAUVAIS : git add build/ (comme target/ : régénérable, dépendant de la machine)
# ✅ BON : build/ dans .gitignore ; seul le code + build.gradle.kts sont versionnés
```

**Pourquoi** : même raison que `target/` en Maven (leçon 01, piège 1) : bruit + conflits + dépôt obèse.

### Piège 2 — `gradle` global au lieu du wrapper

```bash
# ❌ MAUVAIS : gradle build (version de LA machine)
./gradlew build # ✅ BON : version DU projet
```

**Pourquoi** : « ça marche chez moi », même cause qu'en Maven (leçon 01, piège 6).

### Piège 3 — Choisir Gradle « parce que c'est moderne » sans besoin

```text
# ❌ MAUVAIS : migrer SignalCUA en Gradle « pour voir » (2 outils à maintenir, zéro gain en solo)
# ✅ BON : SignalCUA reste Maven ; Gradle = lecture seule jusqu'au jour où un besoin l'exige
```

**Pourquoi** : la flexibilité se paie en complexité de débogage. En solo CUA, la prévisibilité Maven vaut plus que la vitesse Gradle.

### Piège 4 — Écrire du neuf en Groovy par copier-coller

```groovy
// ❌ MAUVAIS : copier un vieux build.gradle Groovy trouvé en ligne (permissif, erreurs au runtime)
```

```kotlin
// ✅ BON : écrire en build.gradle.kts (erreurs vues dans l'IDE avant l'exécution)
testImplementation("org.junit.jupiter:junit-jupiter:5.11.3")
```

**Pourquoi** : Groovy accepte presque tout et échoue tard ; Kotlin DSL vérifie tôt. En 2025-2026, le défaut IntelliJ/Android Studio est `.kts`.

---

## Checklist de validation

Avant de passer à l'exercice, vérifiez que vous savez faire **chacun** de ces points :

- [ ] Dire en une phrase ce qu'est Gradle et citer 3 cas où il bat Maven (monorepo, vitesse, Kotlin).
- [ ] Distinguer `build.gradle` (Groovy, lecture seule d'ancien) de `build.gradle.kts` (Kotlin DSL, défaut neuf).
- [ ] Traduire chaque bloc d'un `build.gradle.kts` en notion Maven (plugins, group/version, repositories, dependencies, toolchain).
- [ ] Découper `testImplementation("g:a:v")` en scope + coordonnées.
- [ ] Associer `./gradlew tasks/test/build/clean/dependencies` à son équivalent `./mvnw`.
- [ ] Justifier Maven par défaut pour un projet CUA solo.

---

## 🔴 Fil rouge — où en est SignalCUA ?

SignalCUA **ne change pas** dans cette leçon : il reste un projet **Maven** (leçon 01). Ce que vous gagnez ici, c'est la capacité à **lire** son jumeau Gradle (le `build.gradle.kts` du §3.1 construit exactement le même projet) et à **expliquer** pourquoi on garde Maven. L'exercice 02 est donc une lecture guidée, pas une migration.

---

➡️ **Prochaine étape** : l'**exercice 02** — lire le `build.gradle.kts` SignalCUA et répondre à 5 questions de lecture. Puis le **`00-Introduction-partie.md`** (filet de rattrapage des termes transverses) clôturera la partie 6, avant la **partie 7** qui démarrera Spring Boot sur ce projet Maven.



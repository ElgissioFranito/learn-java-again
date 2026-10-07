# Exercice 02 — Lire le jumeau Gradle de SignalCUA

> 🧭 **Comment ce fichier s'articule** : la leçon (`01-lecon.md`) a appris à traduire chaque bloc Gradle en notion Maven. Ici, **pas de migration** : on vous donne le `build.gradle.kts` jumeau du `pom.xml` SignalCUA, et vous répondez à 5 questions de lecture en retrouvant chaque réponse dans le fichier. La correction (`03-correction.md`) donne les réponses ligne par ligne.

---

## 🎯 Objectif de l'exercice

Prouver qu'on sait **lire** un projet Gradle imposé : retrouver coordonnées, dépôt, dépendance + scope, version Java, commandes — sans rien installer (lecture seule, comme face à un projet hérité).

**Fichier de départ** (le `build.gradle.kts` de la leçon §3.1, rappelé ici pour travailler sans jongler) :

```kotlin
plugins { // les OUVRIERS : Java + application
    java
    application
}
group = "fr.cua.signalcua" // QUI
version = "0.0.1-SNAPSHOT" // QUELLE édition
repositories { // D'OÙ viennent les .jar
    mavenCentral()
}
dependencies { // les INGRÉDIENTS
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.3")
}
java { // réglage compilateur
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}
```

Et le `settings.gradle.kts` : `rootProject.name = "signalcua"`.

## 📋 Énoncé

### Question 1 — Les coordonnées complètes

Reconstituez le triplet `group:artifact:version` du projet (indice : l'`artifact` est dans `settings.gradle.kts`). Même triplet que le `pom.xml` Maven ? (attendu : oui, `fr.cua.signalcua:signalcua:0.0.1-SNAPSHOT`).

### Question 2 — D'où vient JUnit, et où est-il visible ?

1. Quel dépôt fournit les `.jar` ? 2. Découper `testImplementation("org.junit.jupiter:junit-jupiter:5.11.3")` : scope ? group ? artifact ? version ? 3. Traduction en XML Maven (`<scope>` ?) — JUnit est-il embarqué dans le livrable ? (attendu : non).

### Question 3 — Quelle version Java, et qui l'installe ?

Que vaut la version Java cible ? Qui garantit le bon JDK : la machine ou Gradle (indice : `toolchain`) ? Traduction Maven (`<source>/<target>`) ?

### Question 4 — Les commandes jumelles

Donnez pour chaque besoin la commande Gradle ET son équivalent Maven : lister les étapes, tester, livrer, nettoyer, voir l'arbre des dépendances.

### Question 5 — Le choix argumenté (le vrai objectif de l'aperçu)

Un collègue propose de migrer SignalCUA en Gradle « pour la vitesse ». En 3 phrases : pourquoi refuser (taille du projet ? gain mesuré ? coût de maintenance ?), et dans quel cas futur accepteriez-vous Gradle ?

## ✅ Critères de réussite

- [ ] Q1 : triplet complet reconstitué, égalité Maven constatée (même projet, deux habits).
- [ ] Q2 : dépôt nommé, `testImplementation` découpé, scope `test` traduit, non-embarquement justifié.
- [ ] Q3 : Java 21 + `toolchain` = Gradle gère le JDK (vs `source/target` Maven).
- [ ] Q4 : 5 paires de commandes correctes (`tasks --all`, `test`, `build`, `clean`, `dependencies`).
- [ ] Q5 : refus argumenté (solo, build de secondes, complexité) + cas d'acceptation futur (monorepo, lenteur mesurée, Kotlin).

## 💡 Indications (lisez seulement si bloqué)

- `group = ...` + `rootProject.name` + `version = ...` = le triplet `g:a:v` (les `:` séparent, comme dans la dépendance JUnit).
- `test` + `Implementation` : le préfixe = la portée (comme `<scope>`), la suite = l'embarquement runtime.
- `toolchain` : le mot dit tout — Gradle apporte « l'outil exact » (le JDK), la machine n'a qu'à suivre.
- Q5 : relisez le tableau de décision §2.1 — la réponse y est déjà, reformulez-la pour SignalCUA.

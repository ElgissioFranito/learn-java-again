# Exercice 01 — Structurer SignalCUA en projet Maven + premier test vert

> 🧭 **Comment ce fichier s'articule** : la leçon (`01-lecon.md`) a expliqué la recette (`pom.xml`), les tiroirs (`src/...`), les étapes (`compile` → `test` → `package`), les scopes et le wrapper. Ici, vous **migrez** le code SignalCUA des parties 1-5 (on **déplace**, on ne réécrit pas) dans une arborescence Maven, puis vous **prouvez** que le registre marche avec un test JUnit vert (`./mvnw test`). La correction (`03-correction.md`) suit votre tentative et montre la sortie réellement obtenue.

---

## 🎯 Objectif de l'exercice

Obtenir un dossier `signalcua/` qui se construit en **une commande** (`./mvnw test` vert, puis `./mvnw package` avec `.jar`), avec les classes rangées en packages `fr.cua.signalcua.*` et un test `RegistreReclamationsTest` (2 tests : retrouver + introuvable lève).

**Socle de départ** : reprenez vos classes des parties 1-5 en version **simplifiée** (pour tenir l'exercice en une session) : `Priorite` (enum `BASSE, NORMALE, HAUTE, URGENTE`), `StatutReclamation` (enum `NOUVELLE, EN_COURS, RESOLUE`), `Reclamation` (champs `id`, `quartier`, `priorite`, `statut`, `description` + `getId()` + transitions `demarrerTraitement()` / `marquerResolue()` avec contrôles), `RegistreReclamations` (`ajouter`, `findById` qui lève `ReclamationNotFoundException`), `SignalcuaException` (racine `extends RuntimeException`) + `ReclamationNotFoundException` (porte l'`id`). Si votre `Reclamation` complète a plus de champs (dates...), gardez-la telle quelle : l'exercice marche aussi, ajoutez juste le `package` et les `import`.

## 📋 Énoncé

### Étape 1 — Créer l'arborescence et le `pom.xml`

1. Créez les dossiers (chemins **relatifs**, depuis la racine de votre espace de travail) :
```text
signalcua/pom.xml
signalcua/src/main/java/fr/cua/signalcua/model/
signalcua/src/main/java/fr/cua/signalcua/repository/
signalcua/src/main/java/fr/cua/signalcua/exception/
signalcua/src/test/java/fr/cua/signalcua/repository/
```
2. Écrivez le `pom.xml` de la leçon §3.1 (coordonnées `fr.cua.signalcua:signalcua:0.0.1-SNAPSHOT`, Java 21, JUnit `jupiter` 5.11.3 en scope `test`, plugin compilateur 21).
3. Ajoutez un `.gitignore` contenant `target/` à côté du `pom.xml`.

### Étape 2 — Migrer les classes (déplacer, pas réécrire)

1. Copiez chaque classe dans son package et ajoutez sa première ligne `package ...;` :
- `model/` : `Priorite.java`, `StatutReclamation.java`, `Reclamation.java` → `package fr.cua.signalcua.model;`
- `repository/` : `RegistreReclamations.java` → `package fr.cua.signalcua.repository;` + imports vers `model` et `exception`.
- `exception/` : `SignalcuaException.java`, `ReclamationNotFoundException.java` → `package fr.cua.signalcua.exception;`
2. Vérifiez qu'**aucune logique métier n'a changé** : mêmes transitions, même `findById` qui lève, mêmes `Optional` si vous les aviez.

### Étape 3 — Écrire le test et builder

1. Écrivez `src/test/java/fr/cua/signalcua/repository/RegistreReclamationsTest.java` (le test de la leçon §2.9 : `creerPuisRetrouver` + `introuvableLeve`).
2. Lancez `./mvnw test` depuis `signalcua/` : attendez `Tests run: 2, Failures: 0, Errors: 0` + `BUILD SUCCESS`.
3. Lancez `./mvnw package` : vérifiez `target/signalcua-0.0.1-SNAPSHOT.jar` existe.
4. Lancez `./mvnw dependency:tree` : repérez la ligne `junit-jupiter:jar:5.11.3:test`.

## ✅ Critères de réussite

- [ ] `./mvnw test` affiche `Tests run: 2, Failures: 0, Errors: 0` et `BUILD SUCCESS`.
- [ ] `./mvnw package` produit `target/signalcua-0.0.1-SNAPSHOT.jar`.
- [ ] `target/` n'est **pas** versionné (présent dans `.gitignore`).
- [ ] Chaque `.java` est au bon chemin avec le bon `package` ; la logique métier est inchangée.
- [ ] `dependency:tree` montre JUnit en scope `test` avec ses transitives.

## 💡 Indications (lisez seulement si bloqué)

- `package fr.cua.signalcua.model;` = **première ligne** du fichier, avant les `import`. Le chemin dossier doit **correspondre** : `.../model/Reclamation.java`.
- `ReclamationNotFoundException` : `package fr.cua.signalcua.exception;` + constructeur `(int id)` qui stocke l'id et appelle `super("Reclamation introuvable : " + id)`.
- `RegistreReclamations` : `package fr.cua.signalcua.repository;` + 3 imports (`model.Reclamation`, `exception.ReclamationNotFoundException`, `java.util.*`).
- Premier lancement de `./mvnw` : il télécharge Maven (connexion requise) puis rejoue la commande ; c'est normal si c'est long une fois.
- `Tests run: 2` mais `Failures: 1` ? Lisez le nom du test + la ligne d'assertion : c'est votre registre qui parle, pas Maven.

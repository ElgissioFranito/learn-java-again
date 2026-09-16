# Exercice 06 — SignalCUA prend ses quartiers dans des packages

> 🧭 **Où en sommes-nous ?** La leçon 06 (`01-lecon.md`) a donné des adresses à vos classes. Cet exercice restructure le projet SignalCUA de la leçon 05 en arborescence packagée — et vous fait vivre une fois la compilation manuelle que Maven (partie 6) automatisera. Correction dans `03-correction.md`.

## Étape 0 — L'arborescence cible

Depuis votre dossier `exercices`, créez :

```bash
mkdir -p src/fr/cua/signalcua/model
mkdir -p src/fr/cua/signalcua/app
```

Puis **déplacez** vos fichiers de la leçon 05 :
- `Reclamation.java` → `src/fr/cua/signalcua/model/`
- `MainSignal.java` → `src/fr/cua/signalcua/app/`

## Étape 1 — Déclarer les packages

1. Dans `Reclamation.java` : ajoutez en première ligne `package fr.cua.signalcua.model;` et rendez `public` la classe ET son constructeur complet ainsi que `afficher()` (sinon : `cannot be accessed from outside package` — visibilité package-private, leçon 06).
2. Dans `MainSignal.java` : ajoutez `package fr.cua.signalcua.app;` et l'`import` de `Reclamation`.
3. Compilez et exécutez :

```bash
javac -d build src/fr/cua/signalcua/model/Reclamation.java src/fr/cua/signalcua/app/MainSignal.java
java -cp build fr.cua.signalcua.app.MainSignal
```

**Si vous obtenez `Could not find or load main class`** : relisez le piège 2 de la leçon (adresse complète, avec points, sans `.java`, et bien exécuté depuis la racine `exercices/`).

## Étape 2 — Tester le package-private

1. Dans `model/`, créez une classe `OutilInterne.java` **sans modificateur** (`class OutilInterne { ... }`) avec une méthode `static String prefixe()` qui renvoie `"REC-"`.
2. Dans `MainSignal` (package `app`), essayez d'utiliser `OutilInterne` : constatez l'**erreur de compilation** (invisible hors de son package).
3. Retirez la tentative de `MainSignal` et utilisez plutôt `prefixe()` **dans le constructeur de `Reclamation`** pour composer un identifiant textuel affiché par `afficher()` (ex. `REC-1`).

## Étape 3 — Vérifier l'arborescence compilée

```bash
# Affiche les fichiers .class générés : vous devez retrouver
# l'arborescence des packages dans build/
find build -name "*.class"
```

## Critères de réussite

- [ ] `find build -name "*.class"` montre `build/fr/cua/signalcua/model/Reclamation.class` et `.../app/MainSignal.class`.
- [ ] Le programme s'exécute avec l'adresse complète.
- [ ] Vous avez constaté que `OutilInterne` est invisible depuis `app`.
- [ ] `afficher()` affiche le préfixe `REC-` avant l'id.

Puis comparez avec `03-correction.md`.

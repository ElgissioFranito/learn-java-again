# Exercice 02 — La famille des réclamations

> 🧭 **Comment ce fichier s'articule** : la leçon (`01-lecon.md`) vous a montré `extends`, `super(...)`, `@Override` et le polymorphisme sur l'exemple complet de la hiérarchie SignalCUA. Ici, vous reconstruisez cette famille **vous-même**, avec une variante que la leçon n'a pas codée — le vrai test de compréhension. La correction (`03-correction.md`) attend après votre tentative.

---

## 🎯 Objectif de l'exercice

Construire la hiérarchie de types de l'Étape 2 du fil rouge SignalCUA : une classe mère `Reclamation` et trois filles qui redéfinissent `traiter()`, traitées en une seule boucle polymorphe.

## 📋 Énoncé

Étape par étape :

### Étape 1 — La mère
Créez `Reclamation` avec : champs `private` (`id`, `description` final), constructeur validant la description, méthode `traiter()` **générique** (passe le statut à `EN_COURS` et affiche « Traitement générique de #id »), et une méthode `protected changerStatut(String)` que les filles pourront utiliser. Faites passer les getters `getId()`, `getDescription()`, `getQuartier()`, `getStatut()`.

### Étape 2 — Trois filles
- `ReclamationVoirie extends Reclamation` : champ additionnel `gravite` (`String`, final). Son `traiter()` affiche `#id [VOIRIE/gravite] équipe envoyée : description`.
- `ReclamationProprete extends Reclamation` : champ additionnel `recyclable` (`boolean`, final). Son `traiter()` affiche `#id [PROPRETE] tri à faire` ou `#id [PROPRETE] collecte standard` selon `recyclable`.
- `ReclamationEclairage extends Reclamation` : champ additionnel `numeroLampadaire` (`String`, final). Son `traiter()` affiche `#id [ECLAIRAGE] intervention au lampadaire numero (quartier)`.

Chaque constructeur fils **doit** appeler `super(...)` en première instruction. Chaque `traiter()` **doit** porter `@Override` et appeler `changerStatut("EN_COURS")`.

### Étape 3 — Le polymorphisme
Créez `MainSignal.java` :
1. Un tableau `Reclamation[]` avec une réclamation de chaque type.
2. Une seule boucle for-each qui appelle `r.traiter()` sur chacun.
3. Après la boucle, affichez le statut de chaque réclamation (`getStatut()`) pour prouver que le polymorphisme a bien exécuté les versions filles (chacune passe en `EN_COURS`).

### Étape 4 (bonus) — Compléter, pas remplacer
Dans `ReclamationVoirie`, faites en sorte que `traiter()` **appelle d'abord** `super.traiter()` (le comportement générique de la mère : passage à `EN_COURS`), **puis** ajoute sa ligne d'affichage spécifique. Le statut doit toujours passer à `EN_COURS`.

## ✅ Critères de réussite

- [ ] Le code compile sans erreur ni warning `@Override`.
- [ ] La boucle ne mentionne NULLE PART `ReclamationVoirie`/`Proprete`/`Eclairage` après le tableau.
- [ ] Chaque fille affiche SA ligne, et toutes passent en `EN_COURS`.
- [ ] `changerStatut` est inaccessible depuis `MainSignal` (essayez : le compilateur doit refuser).
- [ ] Le bonus fonctionne : `super.traiter()` appelé, statut bien à `EN_COURS`.

## 💡 Indications (lisez seulement si bloqué)

- Dans la fille, `id` et `description` restent `private` chez la mère : utilisez les getters (`getId()`, ...), pas les champs.
- Pour le bonus : `super.traiter();` en première ligne du `traiter()` redéfini.

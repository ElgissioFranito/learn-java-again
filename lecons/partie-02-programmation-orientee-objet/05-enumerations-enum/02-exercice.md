# Exercice 05 — Le fil rouge passe au vrai `enum`

> 🧭 **Comment ce fichier s'articule** : la leçon (`01-lecon.md`) a présenté l'`enum` et sa mise en œuvre dans SignalCUA. Ici, vous l'appliquez vous-même, avec un second `enum` (la priorité) pour consolider. La correction (`03-correction.md`) vous attend après votre tentative.

---

## 🎯 Objectif de l'exercice

Remplacer le statut `String` de `Reclamation` par un `enum StatutReclamation` avec libellés, ajouter un `enum Priorite` au choix, et consommer les deux via des switchs exhaustifs.

## 📋 Énoncé

### Étape 1 — L'`enum` du statut
Créez `StatutReclamation` avec trois valeurs (`NOUVELLE`, `EN_COURS`, `RESOLUE`), chacune portant un **libellé** (`String`, via un constructeur `private`) :
- `NOUVELLE` → « Nouvelle demande »
- `EN_COURS` → « En cours de traitement »
- `RESOLUE` → « Réclamation résolue »

Ajoutez le getter `getLibelle()`.

### Étape 2 — La `Reclamation` au statut typé
Reprenez la `Reclamation` de la leçon 01 (champs private, constructeur validé, transitions contrôlées) et changez le type de `statut` : `String` → `StatutReclamation`. Les transitions utilisent les constantes du type et comparent avec `==`. Ajoutez la méthode `getBadge()` : un switch **sans `default`** renvoyant `"🆕"`, `"⏳"` ou `"✅"` selon le statut.

### Étape 3 — Un second `enum` : la priorité
Créez `Priorite` avec `BASSE`, `NORMALE`, `URGENTE`, chacune portant un champ `int delaiHeuresMax` (délai maximal de prise en charge) : 168, 48, 4. Ajoutez le getter correspondant.

### Étape 4 — L'`enum` au menu
Dans `MainEnum.java` :
1. Créez une `Reclamation`, faites le cycle complet, affichez la ligne + le badge à chaque étape.
2. Parcourez `Priorite.values()` et affichez pour chaque valeur : `NOM → délai max : Xh`.
3. Stockez un statut sous forme de texte (`"EN_COURS"`), puis relisez-le avec `valueOf()` et affichez son libellé.
4. Tentez `valueOf("EN_ATTAENTE")` dans un `try/catch` et affichez le refus.

### Étape 5 (bonus) — Le scénario du piège `ordinal()`
Écrivez (en commentaire dans `main`, ou dans un fichier `BonusOrdinal.java`) le scénario suivant en 5 lignes de pseudo-code ou d'explication : un système stocke `statut.ordinal()` en base ; un `EN_PAUSE` est inséré en 2e position ; que devient la donnée historique « 1 » ? Prouvez-le en Java : affichez `ordinal()` avant et après l'ajout d'une valeur au milieu (deux versions de l'`enum`).

## ✅ Critères de réussite

- [ ] `StatutReclamation` porte ses libellés et n'expose rien de mutable.
- [ ] La `Reclamation` ne contient plus AUCUNE `String` de statut.
- [ ] Les transitions comparent avec `==` sur les constantes.
- [ ] `getBadge()` est un switch exhaustif sans `default`.
- [ ] `Priorite` porte son délai et le main l'affiche pour toutes ses valeurs.
- [ ] Le bonus démontre concrètement le décalage de l'`ordinal()`.

## 💡 Indications (lisez seulement si bloqué)

- La comparaison `statut != StatutReclamation.NOUVELLE` remplace `!statut.equals("NOUVELLE")`.
- Pour le bonus : créez deux fichiers d'`enum` dans deux dossiers différents (version A sans `EN_PAUSE`, version B avec) et comparez les sorties d'un même programme d'affichage.

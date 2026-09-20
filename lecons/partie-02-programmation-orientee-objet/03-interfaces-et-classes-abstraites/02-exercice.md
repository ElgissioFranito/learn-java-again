# Exercice 03 — Le contrat `Traitable`

> 🧭 **Comment ce fichier s'articule** : la leçon (`01-lecon.md`) a expliqué interfaces, classes abstraites et composition. Ici vous appliquez : extraire un contrat de la famille SignalCUA et y faire entrer un objet hors-famille. La correction (`03-correction.md`) vous attend APRÈS votre tentative.

---

## 🎯 Objectif de l'exercice

Créer l'interface `Traitable`, la faire signer par deux réclamations **et** par un `Agent` (hors-famille), puis traiter le tout dans une boucle polymorphe typée par le contrat. En bonus, écrire une classe abstraite et choisir le bon outil.

## 📋 Énoncé

Étape par étape :

### Étape 1 — Le contrat
Créez l'interface `Traitable` avec une seule méthode : `void traiter();` (sans corps).

### Étape 2 — Deux signataires dans la famille
Reprenez (ou recréez) `Reclamation` (mère minimaliste de la leçon 02 : champs + constructeur + getters) et faites implémenter `Traitable` par `ReclamationVoirie` (champ `gravite`) et `ReclamationProprete` (champ `recyclable`). Chaque `traiter()` affiche la ligne propre au type et passe le statut à `EN_COURS` (via la méthode `protected changerStatut` de la mère).

### Étape 3 — Un signataire hors-famille
Créez la classe `Agent implements Traitable` (champ `nom` final). Son `traiter()` affiche : `nom + " prend en charge la file de réclamations"`.

### Étape 4 — La boucle par contrat
Dans `MainTraitable.java`, créez un tableau de type `Traitable[]` contenant une `ReclamationVoirie`, une `ReclamationProprete` et un `Agent`. Une seule boucle appelle `traiter()` sur chacun. Vérifiez que chaque objet exécute SA version.

### Étape 5 (bonus) — La classe abstraite au bon endroit
Un collègue propose de transformer la mère `Reclamation` en **classe abstraite** avec une méthode abstraite `traiter()`. Réfléchissez puis tranchez :
- (a) Est-ce que « créer une Reclamation générique » a du sens dans SignalCUA ?
- (b) L'`Agent` pourrait-il toujours entrer dans la boucle si la mère devient abstraite ? (Indice : l'Agent n'hérite PAS de `Reclamation`.)
- (c) Écrivez 3 lignes de conclusion : quelle solution garde les deux avantages (boucle ouverte + famille partagée) ?

## ✅ Critères de réussite

- [ ] `Traitable` ne contient AUCUN corps de méthode.
- [ ] Les deux réclamations déclarent `extends Reclamation implements Traitable`.
- [ ] `Agent` n'a AUCUN lien d'héritage avec `Reclamation` mais entre dans la même boucle.
- [ ] Le tableau de `MainTraitable` est typé `Traitable[]`, pas `Reclamation[]`.
- [ ] Le bonus répond aux 3 questions avec une conclusion claire.

## 💡 Indications (lisez seulement si bloqué)

- La déclaration combinée s'écrit : `public class ReclamationVoirie extends Reclamation implements Traitable {` — le `extends` d'abord, le `implements` ensuite.
- Pour le bonus : relisez le tableau de décision de la leçon (section 2.4) — la question (b) est le cœur de l'affaire.

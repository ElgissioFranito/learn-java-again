# Exercice 01 — Blindez la `Reclamation`

> 🧭 **Comment ce fichier s'articule** : la leçon (`01-lecon.md`) a expliqué le pourquoi/comment/quand de l'encapsulation. Ici, vous l'appliquez vous-même sur le projet SignalCUA (Étape 2 du fil rouge, point de départ). La correction détaillée (`03-correction.md`) est là APRÈS votre tentative — pas avant : le réflexe de lire la correction d'abord ruine l'exercice.

---

## 🎯 Objectif de l'exercice

Prendre la classe `Reclamation` héritée de la partie 1 (champs ouverts à tous) et la transformer en classe encapsulée : rien ne peut plus la corrompre de l'extérieur, et ses transitions de statut sont contrôlées.

## 📋 Énoncé

Étape par étape — faites-les **dans l'ordre** :

### Étape 1 — Partir de l'ancienne classe
Recréez la version « dangereuse » de la partie 1 (champs accessibles, constructeur validant seulement la description), puis compilez-la. C'est votre point de départ.

### Étape 2 — Fermer les portes
Rendez les quatre champs `private`. Rendez `id`, `description` et `quartier` `final` (ils ne changent jamais après création). Compilez : quelles erreurs apparaissent ? Notez-les — chaque erreur est un endroit du code qui profitait de la porte ouverte.

### Étape 3 — Installer le guichet
Ajoutez les getters pour les 4 champs. **Pas de setter** pour `id`, `description`, `quartier`. Pour `statut` : **ne pas écrire `setStatut`** — à la place, deux méthodes métier :
- `demarrerTraitement()` : refuse si le statut n'est pas `NOUVELLE`, sinon passe à `EN_COURS`.
- `marquerResolue()` : refuse si le statut n'est pas `EN_COURS`, sinon passe à `RESOLUE`.

Chaque refus lève une `IllegalStateException` avec un message clair.

### Étape 4 — Compléter les constructeurs
Constructeur complet : `Reclamation(int id, String description, String quartier)` qui valide **les trois paramètres** (id > 0, description non vide, quartier non vide) et initialise `statut = "NOUVELLE"`.
Constructeur surchargé : `Reclamation(int id, String description)` qui délègue au complet avec `this(...)` et la valeur `"Non précisé"` pour le quartier.


### Étape 5 — Ajouter le comportement (anti-anémique)
Ajoutez :
- `estOuverte()` : renvoie `true` si le statut est `NOUVELLE` ou `EN_COURS`.
- `getLigneAffichage()` : renvoie `#id [statut] description (quartier)`.

### Étape 6 — Un programme de démonstration
Écrivez `MainEncapsulation.java` qui :
1. Crée une réclamation avec la version surchargée (sans quartier).
2. Tente (en commentaires) les trois tricheries : `r.id = -7`, `r.statut = "PLATANE"`, `r.setDescription("x")` — et vérifie à la compilation que chacune est refusée.
3. Fait le cycle : démarrer → résoudre → puis re-tenter `demarrerTraitement()` dans un `try/catch` et afficher le message de refus.
4. Tente aussi `new Reclamation(0, "", "X")` dans un `try/catch` pour vérifier la validation du constructeur.

## ✅ Critères de réussite

- [ ] Le code compile.
- [ ] Aucun champ n'est modifiable de l'extérieur (essayez-le vraiment : le compilateur doit refuser).
- [ ] Impossible d'avoir un statut `RESOLUE` sans être passé par `EN_COURS`.
- [ ] Impossible de créer une réclamation avec description vide ou id ≤ 0.
- [ ] Aucun setter de « façade » : les transitions passent par les méthodes métier.

## 💡 Indications (lisez seulement si bloqué)

- Le garde-fou d'une méthode de transition suit le même schéma que le constructeur de la partie 1 : `if (condition invalide) throw new IllegalStateException(...)`, puis mutation.
- `estOuverte()` est un getter « calculé » : il lit `statut` et renvoie un `boolean` avec `||`.

# Exercice 05 — Le compteur du registre SignalCUA

> 🧭 **Où en sommes-nous ?** La leçon 05 (`01-lecon.md`) a expliqué `static` (le mystère de `main` est levé !) et les constantes. Cet exercice enrichit votre classe `Reclamation` de la leçon 04 avec un compteur de classe et des constantes — et vous fait expérimenter la différence instance/classe de vos propres mains. Correction dans `03-correction.md`.

## Étape 0 — Préparer

Reprenez vos fichiers `Reclamation.java` et `MainSignal.java` de la leçon 04 (dans votre dossier `exercices`), ou recréez-les. Exécution habituelle :

```bash
javac Reclamation.java MainSignal.java
java MainSignal
```

## Étape 1 — Les constantes

Dans `Reclamation`, déclarez :

- `public static final int MAX_URGENCE = 5;`
- `public static final String STATUT_INITIAL = "NOUVELLE";`

Puis **remplacez** le `"NOUVELLE"` littéral du constructeur par `STATUT_INITIAL`, et l'égalité de `estUrgente()` par une comparaison avec `STATUT_INITIAL`.

## Étape 2 — Le compteur de classe

Ajoutez un champ `static int compteurCreees = 0;` et incrémentez-le dans le constructeur **complet** (réfléchissez : où l'incrémenter pour qu'il compte une fois exactement, même quand on passe par la surcharge qui délègue ?). Ajoutez la méthode `static int totalCreees()`.

## Étape 3 — Vérifier de vos yeux

Dans `MainSignal` :

1. affichez `totalCreees()` **avant** toute création (attendu : 0) ;
2. créez 3 réclamations (dont une via la surcharge sans quartier) ;
3. affichez `totalCreees()` (attendu : 3) ;
4. tentez `r1.compteurCreees = 999;` puis ré-affichez `Reclamation.totalCreees()`. **Notez ce que vous observez** — et pourquoi ça démontre que le champ est unique.

## Étape 4 — Le diagnostic en méthode de classe

Transférez le `diagnostic(int nb)` de la leçon 02 dans `Reclamation` comme méthode `static` — c'est une fonction utilitaire pure (ne dépend que de son paramètre) : elle est à sa place là. Appelez-la depuis `main` : `Reclamation.diagnostic(12)` doit renvoyer `"Charge elevee"`.

## Critères de réussite

- [ ] Plus aucun `"NOUVELLE"` littéral dans la classe (seulement `STATUT_INITIAL`).
- [ ] Le compteur vaut 0 avant création, 3 après.
- [ ] `r1.compteurCreees = 999;` change LE compteur unique (démonstration).
- [ ] `diagnostic` est `static` et appelée via `Reclamation.diagnostic(...)`.

Puis comparez avec `03-correction.md`.

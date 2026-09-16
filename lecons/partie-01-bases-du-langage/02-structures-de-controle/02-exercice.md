# Exercice 02 — Le tableau de bord du service voirie

> 🧭 **Où en sommes-nous ?** La leçon 01 (`01-lecon.md`) vous a donné les variables ; cette leçon vous a donné les décisions (`if`, `switch`) et les répétitions (boucles). Cet exercice combine les deux pour construire un mini tableau de bord du service voirie — un avant-goût direct du fil rouge SignalCUA. La correction est dans `03-correction.md` : tentez d'abord !

## Étape 0 — Préparer

Dans votre dossier `exercices` (créé à l'exercice 01), créez un fichier `TableauBord.java`. Pour exécuter :

```bash
# Compile le fichier (vérifie le code) puis exécute la classe
javac TableauBord.java
java TableauBord
```

*(Avec VS Code + extension Java, le bouton ▶ au-dessus du `main` fait les deux étapes.)*

## Étape 1 — Les données de départ

```java
// Un tableau de noms de quartiers et un tableau PARALLÈLE
// du nombre de réclamations (l'indice i des deux correspond au même quartier)
String[] quartiers = {"Medina", "Plateau", "Grand Yoff", "Fann"};
int[] reclamations = {12, 0, 15, 6};
```

## Étape 2 — Afficher chaque quartier (for classique)

Avec une boucle `for` classique (avec index), affichez :
`Medina : 12 reclamations` etc. **Rappel du piège** : les indices vont de `0` à `longueur - 1`.

## Étape 3 — Ignorer les quartiers vides (continue)

Reprenez la boucle : si un quartier a **0 réclamation**, sautez son affichage avec `continue`.

## Étape 4 — Calculer le total et le quartier le plus chargé (for-each)

En une seule boucle `for-each` sur `reclamations`, calculez :
1. le **total** de réclamations ;
2. le **maximum** (nombre de réclamations du quartier le plus chargé).

## Étape 5 — Un message selon le niveau (switch expression)

Écrivez une méthode de diagnostic utilisant un `switch` expression avec `->` :

```java
// Retourne le message correspondant au nombre de réclamations :
//   0  -> "Rien a signaler"
//   1..9 (10 exclu) -> "Charge normale"
//   10..19 -> "Charge elevee"
//   20 et plus -> "Sature"
// Astuce : on ne peut pas mettre de condition > dans un case ;
// pensez à l'ORDRE des cas et à la limite supérieure de chaque tranche,
// ou utilisez un if/else si vous préférez — mais essayez le switch d'abord.
static String diagnostic(int nb) { ... }
```

*(Le mot-clé `static` devant la méthode sera expliqué en détail leçon 05 ; pour l'instant, recopiez la signature telle quelle — c'est ce qu'il faut pour l'appeler depuis `main`.)*

Appelez `diagnostic(...)` pour chaque quartier (dans une boucle) et affichez le résultat.

## Critères de réussite

- [ ] Le programme compile et s'exécute sans erreur.
- [ ] Aucune `ArrayIndexOutOfBoundsException`.
- [ ] Les quartiers à 0 réclamation n'apparaissent pas.
- [ ] Le total affiché est **33** et le maximum **15**.
- [ ] Le `switch` (ou `if/else`) des tranches donne bien 4 messages différents.

Comparez ensuite avec `03-correction.md`.

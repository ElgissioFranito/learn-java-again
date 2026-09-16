# Exercice 03 — Le registre des réclamations

> 🧭 **Où en sommes-nous ?** La leçon 03 (`01-lecon.md`) vous a donné tableaux, `ArrayList`, méthodes de `String` et `StringBuilder`. Cet exercice les combine pour construire un **registre** de réclamations — une version « brut de fonderie » du fil rouge SignalCUA (le vrai objet `Reclamation` arrive en leçon 04, vous verrez combien cet exercice en justifie le besoin !). Correction dans `03-correction.md`.

## Étape 0 — Préparer

Dans votre dossier `exercices`, créez `Registre.java` et exécutez comme d'habitude :

```bash
javac Registre.java
java Registre
```

## Étape 1 — Importer et préparer les données

```java
// Importez ArrayList (depuis java.util)
// Créez une ArrayList<String> nommée "signalements" contenant ces entrées brutes :
//   "  Voirie_Nid de poule_Route Dakar  "
//   "Eclairage_Lampadaire HS_Rue 12 "
//   "  Proprete_Tas d'ordures_Marche Medina"
// Le séparateur de champs est le soulignement "_".
// (Notez les espaces parasites : ils seront votre ennemi en étape 3.)
```

## Étape 2 — Afficher le registre

Avec une boucle `for-each`, affichez chaque signalement numéroté :
`1. <signalement>` — utilisez le `for` classique avec index si vous voulez facilement le numéro.

## Étape 3 — Nettoyer et découper

Pour chaque signalement, sans modifier la liste d'origine :
1. retirez les espaces des extrémités (`.trim()`) ;
2. découpez selon le tiret `-` pour obtenir : catégorie, objet, lieu ;
3. affichez une fiche propre :

```text
#1  Catégorie : Voirie     | Objet : Nid de poule | Lieu : Route Dakar
```

## Étape 4 — Compter les catégories ( StringBuilder )

Avec un `StringBuilder`, construisez un rapport multi-lignes du style :

```text
RAPPORT SIGNALCUA
-----------------
Voirie : 1 signalement
Eclairage : 1 signalement
Proprete : 1 signalement
Total : 3
```

Indice : parcourez la liste, récupérez le premier morceau (la catégorie) de chaque entrée, et cumulez. Pour compter sans `Map` (qui arrive en partie 3), trois variables `int` suffisent : `nbVoirie`, `nbEclairage`, `nbProprete` — incrémentez celle qui correspond (un `switch` ou des `if` font l'affaire ; c'est la révision de la leçon 02).

## Étape 5 — Comparer sans se tromper

```java
String statut1 = "nouvelle";
String statut2 = "NOUVELLE";
// Affichez le résultat de statut1.equals(statut2) puis de
// statut1.equalsIgnoreCase(statut2). Expliquez la différence dans un commentaire.
```

## Critères de réussite

- [ ] Le programme compile et s'exécute.
- [ ] Les fiches de l'étape 3 n'ont **aucun espace parasite**.
- [ ] Le rapport de l'étape 4 est construit avec un `StringBuilder` (pas de `+` dans la boucle).
- [ ] Vous pouvez dire pourquoi `brut.trim();` tout seul ne sert à rien.

Puis comparez avec `03-correction.md`.

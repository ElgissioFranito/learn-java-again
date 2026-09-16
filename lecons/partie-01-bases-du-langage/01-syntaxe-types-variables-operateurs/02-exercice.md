# Exercice 01 — Les variables de la mairie

> 🧭 **Où en sommes-nous ?** Dans `01-lecon.md`, vous avez appris les types, les variables et les opérateurs. Ce fichier met ça en pratique sur des données réelles de la mairie (le projet fil rouge **SignalCUA** — gestion des réclamations citoyennes — arrive à la leçon 04 ; ici on s'entraîne d'abord sur des données simples). La correction complète est dans `03-correction.md` : essayez d'abord seul !

## Étape 0 — Préparer votre environnement (une seule fois)

1. Vérifiez que Java est installé : ouvrez un terminal et tapez :

```bash
# Affiche la version de Java installée. Si une erreur apparaît,
# installez un JDK 21+ (ex. Temurin) avant de continuer.
java -version
```

2. Créez un dossier de travail et un fichier :

```bash
# Crée un dossier "exercices" puis va dedans
mkdir exercices
cd exercices
```

3. Avec VS Code (recommandé : installez l'extension « Extension Pack for Java »), créez un fichier `MaMairie.java` dans ce dossier.

## Étape 1 — Déclarer des variables bien typées

Dans la méthode `main`, déclarez ces variables avec **le bon type** (réfléchissez : entier ? à virgule ? texte ? vrai/faux ?) :

| Donnée | Valeur | Réflexe à avoir |
|---|---|---|
| Nom du quartier | `"Grand Yoff"` | texte |
| Nombre de réclamations du jour | `37` | entier |
| Nombre total d'habitants du quartier | `281 000` | attention à la taille ! |
| Taux de résolution (en %) | `82.5` | nombre à virgule |
| La mairie est ouverte | oui | vrai/faux |
| Code du service (une seule lettre) | `V` (voirie) | un seul caractère |
| Coût moyen d'une intervention (en FCFA) | `4500.75` | monnaie ! Attention au piège |

Affichez chaque valeur avec `System.out.println("... : " + valeur);`.

## Étape 2 — Calculer et prédire

Ajoutez ces calculs et **notez sur papier votre prédiction AVANT d'exécuter** :

```java
// a) Moyenne de réclamations par agent (3 agents) — attention au piège !
int nbReclamations = 37;
int nbAgents = 3;

// b) Le même calcul, mais avec le bon résultat (2.5 attendu pas 2.0)

// c) Le total des habitants sur 2 quartiers de 281 000 habitants chacun
//    (indice : 2 x 281 000 = 562 000, ça tient dans un int ; mais essayez
//    avec 2 x 1 500 000 000 — que se passe-t-il ?)

// d) 0.1 + 0.2 affiché tel quel
```

## Étape 3 — Comparer et décider avec le ternaire

```java
// La mairie affiche un panneau : si le taux de résolution >= 80,
// le message est "Objectif atteint", sinon "Objectif non atteint".
// Écrivez ce message avec l'opérateur ternaire dans une variable String.
```

## Étape 4 — Le réflexe `.equals()`

```java
String statut1 = "NOUVELLE";
String statut2 = new String("NOUVELLE"); // crée un nouvel objet, même contenu
// Affichez le résultat de (statut1 == statut2), puis de statut1.equals(statut2).
// Notez l'écart sur papier avant d'exécuter.
```

## Critères de réussite

- [ ] Le programme compile sans erreur (`javac MaMairie.java` ou le bouton ▶ de VS Code).
- [ ] Chaque variable utilise le type le plus adapté (pas de `double` pour le nom du quartier !).
- [ ] Vous pouvez expliquer pourquoi `37 / 3` ne donne pas la bonne moyenne.
- [ ] Vous avez constaté de vos yeux le comportement de `==` vs `.equals()`.
- [ ] Vous avez constaté l'overflow avec deux grands nombres.

Quand c'est fait, comparez avec `03-correction.md`. Sinon, relisez la section 2 de la leçon et réessayez — l'erreur fait partie de l'apprentissage.

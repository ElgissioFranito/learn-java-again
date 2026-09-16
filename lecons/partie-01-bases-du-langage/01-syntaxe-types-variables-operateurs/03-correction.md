# Correction 01 — Les variables de la mairie

> 🧭 **Articulation** : voici la solution pas à pas de `02-exercice.md`. Ne lisez pas en diagonale : comparez chaque étape avec VOTRE code, et notez les différences. La checklist de validation finale reprend celle de la leçon.

## Étape 1 — Correction des variables

```java
public class MaMairie {
    public static void main(String[] args) {
        // Nom du quartier : du texte -> String (guillemets doubles)
        String quartier = "Grand Yoff";

        // 37 réclamations : un petit entier -> int suffit largement
        int nbReclamations = 37;

        // 281 000 habitants : tient dans un int (max ~2 milliards)
        // Le "_" est un séparateur visuel, ignoré par Java
        int nbHabitants = 281_000;

        // 82.5 % : nombre à virgule -> double (pas de souci ici,
        // un pourcentage n'est pas de l'argent)
        double tauxResolution = 82.5;

        // "La mairie est ouverte" : oui/non -> boolean
        boolean mairieOuverte = true;

        // Code service : UNE lettre -> char (apostrophes simples !)
        char codeService = 'V';

        // Coût en FCFA : c'est de la MONNAIE -> BigDecimal (jamais double !)
        java.math.BigDecimal coutMoyen =
                new java.math.BigDecimal("4500.75");

        System.out.println("Quartier : " + quartier);
        System.out.println("Reclamations du jour : " + nbReclamations);
        System.out.println("Habitants : " + nbHabitants);
        System.out.println("Taux de resolution : " + tauxResolution + " %");
        System.out.println("Mairie ouverte : " + mairieOuverte);
        System.out.println("Code service : " + codeService);
        System.out.println("Cout moyen intervention : " + coutMoyen + " FCFA");
    }
}
```

**Choix techniques expliqués :**

- `int` pour `nbHabitants` : 281 000 < 2 147 483 647, donc `int` suffit. `long` serait correct aussi mais `int` est le type naturel pour les entiers « humains ».
- `BigDecimal` et non `double` pour le coût : le coût est de l'argent. Un `double` stockerait 4500.75 de façon approximative en base 2. On construit le `BigDecimal` depuis une `String` pour garder la valeur exacte.
- `'V'` avec apostrophes simples : c'est un `char`. `"V"` (guillemets) serait une `String` de un caractère — ça compile aussi, mais `char` est plus précis.

## Étape 2 — Correction des calculs

```java
// a) MAUVAIS reflexe (le piège) :
int nbReclamations = 37;
int nbAgents = 3;
System.out.println(nbReclamations / nbAgents);
// PREDICTION : 12.33... — RESULTAT REEL : 12
// Pourquoi ? Division entre deux int = division entière, le reste est jeté.

// b) BON reflexe : forcer un operande en double AVANT la division
double moyenne = (double) nbReclamations / nbAgents;
System.out.println(moyenne); // 12.333333333333334
// Notez le cast autour de nbReclamations UNIQUEMENT : dès qu'un
// operande est double, la division devient "à virgule".

// c) L'overflow :
int grosNombre = 1_500_000_000;
System.out.println(grosNombre * 2);
// RESULTAT REEL : -1_294_967_296 (un nombre NEGATIF absurde !)
// Pourquoi ? 3 milliards dépasse la capacité d'un int (~2,147 milliards).
// Java ne lève PAS d'erreur : le résultat "boucle" et devient faux.
long totalOk = (long) grosNombre * 2; // 3_000_000_000 : correct
System.out.println(totalOk);

// d) Le double imprécis :
System.out.println(0.1 + 0.2); // 0.30000000000000004
// Pourquoi ? 0.1 n'est pas représentable exactement en base 2.
// D'où la règle : jamais de double pour l'argent -> BigDecimal.
```

## Étape 3 — Correction du ternaire

```java
double tauxResolution = 82.5;

// Ternaire : condition ? valeurSiVrai : valeurSiFaux
String message = (tauxResolution >= 80)
        ? "Objectif atteint"
        : "Objectif non atteint";
System.out.println(message); // Objectif atteint
```

**Pourquoi le ternaire ici ?** Deux valeurs possibles, une seule condition : c'est le cas idéal. Avec plus de deux cas (ex. « atteint / partiel / échec »), un `if/else` ou un `switch` serait plus lisible — c'est le sujet de la leçon 02.

## Étape 4 — Correction du `==` vs `.equals()`

```java
String statut1 = "NOUVELLE";
String statut2 = new String("NOUVELLE"); // un NOUVEL objet en mémoire

System.out.println(statut1 == statut2);      // false !
System.out.println(statut1.equals(statut2)); // true

// POURQUOI ?
// "==" compare les REFERENCES (l'adresse du coffre, pas son contenu).
// statut1 et statut2 sont deux objets distincts -> deux adresses -> false.
// ".equals()" compare le CONTENU -> "NOUVELLE" == "NOUVELLE" -> true.

// REGLE : pour String, Integer, Double... toujours .equals(), jamais "==".
// (Les primitifs int, boolean... se comparent bien avec "==" :
//  la variable CONTIENT la valeur, il n'y a pas de référence.)
```

## Checklist de validation (récapitulatif)

- [ ] Je sais écrire et exécuter un programme avec `main`.
- [ ] Je choisis le bon type pour chaque donnée (`int`, `long`, `double`, `boolean`, `char`, `String`).
- [ ] Je sais expliquer primitif vs objet (la valeur vs la référence).
- [ ] Je sais prédire le résultat de `37 / 3` et corriger avec un cast.
- [ ] J'utilise `.equals()` pour comparer le contenu des objets.
- [ ] Je n'utilise jamais `double` pour de l'argent (mais `BigDecimal`).
- [ ] J'ai vu de mes yeux un overflow d'`int` et je sais le prévenir avec `long`.

## 💡 Conseils

1. **Faites l'exercice sans copier-coller la correction** : taper le code à la main grave les réflexes.
2. **Gardez ce fichier `MaMairie.java`** : on le réutilisera en leçon 02 pour y ajouter des conditions.
3. **Réflexe « prédire puis exécuter »** : noter sa prédiction avant d'exécuter est le meilleur moyen de vérifier qu'on a compris (et d'être surpris quand on se trompe — c'est là qu'on apprend).
4. Ne vous inquiétez pas si `BigDecimal` vous semble verbeux : c'est normal, vous apprendrez à l'apprivoiser avec la pratique.

➡️ **Prochaine étape** : leçon 02 — structures de contrôle (`if`, `switch`, boucles) pour que votre programme prenne des décisions.



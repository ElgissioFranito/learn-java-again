# Leçon 07 — Lecture des entrées utilisateur (Scanner)

> 🧭 **Pont depuis la leçon 06** : vos classes sont bien rangées, mais jusqu'ici toutes les réclamations étaient écrites **en dur** dans le code (`new Reclamation(1, "Nid de poule", ...)`). Cette leçon donne à votre programme son dialogue avec l'humain : le `Scanner`, qui lit ce que l'utilisateur tape au clavier. C'est la dernière brique de la partie 1 — à la fin, l'Étape 1 du fil rouge sera complètement interactive. Un avertissement d'emblée (la roadmap le souligne) : c'est un sujet **transitoire** — une vraie application Spring Boot ne lit jamais le clavier, elle reçoit des requêtes HTTP (partie 7). Apprenez-le vite, sans y investir des semaines.

---

## 1. Objectifs d'apprentissage

À la fin de cette leçon, vous saurez :

- Créer un `Scanner` et lire des lignes (`nextLine`) et des nombres (`nextInt`, `nextDouble`).
- Éviter LE piège classique du `nextInt()` suivi de `nextLine()` (la ligne vide fantôme).
- Gérer une saisie invalide (l'utilisateur tape du texte au lieu d'un nombre).
- Écrire une petite boucle de menu console avec `do-while` (révision de la leçon 02).
- Finaliser l'Étape 1 du fil rouge : saisir une réclamation au clavier.

---

## 2. Explication simple

### 2.1 Qu'est-ce que le Scanner ?

**Pourquoi ?** `System.out.println` est la sortie (le programme parle). Il faut aussi l'**entrée** (le programme écoute) : c'est le rôle de `System.in`, le flux du clavier, brut et peu commode. Le `Scanner` est un **interprète commode** posé sur ce flux : il découpe, convertit et vous rend des `String`, des `int`, des `double`.

**Analogie** : `System.in` est un micro qui délivre un son brut ; le `Scanner` est le transcripteur qui vous dit « l'utilisateur a dit ce mot » ou « ce nombre ».

**Comment ?**

```java
import java.util.Scanner; // Scanner vit dans java.util (leçon 06 !)

Scanner clavier = new Scanner(System.in); // poser le transcripteur sur le micro

System.out.print("Votre nom : ");
String nom = clavier.nextLine();  // attend que l'utilisateur tape + Entrée

System.out.print("Votre age : ");
int age = clavier.nextInt();      // attend un nombre entier

System.out.println("Bonjour " + nom + ", vous avez " + age + " ans");
```

**Quand ?** Programmes console d'apprentissage et petits outils CLI (scripts en ligne de commande). Point final pour un vrai produit : HTTP, jamais le clavier.

> 📖 **CLI** : *Command Line Interface* — programme qu'on pilote en tapant des commandes dans un terminal (par opposition à une interface graphique ou à une API web).

### 2.2 Les méthodes de lecture utiles

```java
String ligne      = clavier.nextLine();   // toute la ligne jusqu'à Entrée (String)
int entier        = clavier.nextInt();    // un entier (plante si autre chose)
double decimal    = clavier.nextDouble(); // un décimal (avec point : 12.5)
String mot        = clavier.next();       // UN mot (s'arrête au premier espace)
```

⚠️ Rappels des leçons précédentes qui restent valables : une saisie humaine peut contenir des espaces parasites → `.trim()` (leçon 03) ; les `Scanner` sont des objets → un seul par programme, créé une fois.

### 2.3 LE piège n°1 : `nextInt()` puis `nextLine()`

**Pourquoi c'est piégeux ?** Quand vous tapez `42` puis **Entrée**, le clavier envoie en réalité **deux choses** : les caractères `4` et `2`, puis le caractère « retour à la ligne » (`\n`). `nextInt()` lit et consomme `42`… mais **laisse le `\n` en attente**. Le `nextLine()` suivant lit alors ce retour à la ligne restant : il renvoie une **chaîne vide**, sans attendre l'utilisateur !

```java
int age = clavier.nextInt();     // tape "42" + Entrée
String quartier = clavier.nextLine(); // renvoie "" immédiatement ! Piège consommé.

System.out.println("[" + quartier + "]"); // [] — vide !
```

**La correction standard** : après un `nextInt()` (ou `nextDouble()`…), « avalez » le retour à la ligne résiduel avec un `nextLine()` jetable :

```java
int age = clavier.nextInt();
clavier.nextLine();              // consomme le \n résiduel (valeur jetée)
String quartier = clavier.nextLine(); // maintenant, ça attend vraiment l'utilisateur
```

**Comment s'en souvenir ?** `nextInt` mange le nombre mais pas l'assiette (`\n`). Avant de servir le plat suivant, débarrassez l'assiette.

### 2.4 Gérer une saisie invalide

**Pourquoi ?** Que se passe-t-il si l'utilisateur tape « douze » là où `nextInt` attend un nombre ? Une `InputMismatchException` (exception = erreur d'exécution signalée en plantant — détaillée en partie 4) : le programme s'arrête brutalement. Pour un menu console d'apprentissage, on préfère **redemander poliment**.

**Comment ?** La méthode `hasNextInt()` vérifie AVANT de lire que le prochain morceau est bien un entier :

```java
System.out.print("Votre age : ");
while (!clavier.hasNextInt()) {          // tant que ce n'est PAS un entier
    clavier.nextLine();                  // vider la saisie invalide
    System.out.print("Ce n'est pas un nombre. Recommencez : ");
}
int age = clavier.nextInt();
clavier.nextLine();                      // et le piège du \n résiduel, toujours
```

C'est une boucle de garde (leçon 02) : redemander tant que la donnée n'est pas valide — exactement ce que fait un constructeur (leçon 04), mais version dialogue.

### 2.5 Le menu console avec `do-while`

**Pourquoi le `do-while` plutôt que `while` ?** Un menu doit s'afficher **au moins une fois** avant même le premier choix : c'est la définition même du `do-while` (leçon 02).

```java
int choix;
do {
    System.out.println("""
            === SIGNALCUA ===
            1. Signaler un probleme
            2. Lister les signalements
            0. Quitter
            Votre choix : """);
    choix = clavier.nextInt();
    clavier.nextLine(); // avalez le \n (piège 2.3)

    // switch expression de la leçon 02 : des CAS FIXES -> l'outil idéal
    String message = switch (choix) {
        case 1 -> "Nouveau signalement";
        case 2 -> "Liste des signalements";
        case 0 -> "Au revoir";
        default -> "Choix inconnu";
    };
    System.out.println(message);
} while (choix != 0); // on continue tant que l'utilisateur ne quitte pas
```

---

## 3. Exemples concrets

Version interactive complète de SignalCUA (suite de la leçon 06, mêmes packages) :

```java
// src/fr/cua/signalcua/app/MainSignal.java
package fr.cua.signalcua.app;

import fr.cua.signalcua.model.Reclamation;
import java.util.ArrayList;
import java.util.Scanner;

public class MainSignal {
    public static void main(String[] args) {
        Scanner clavier = new Scanner(System.in);      // UN seul Scanner
        ArrayList<Reclamation> reclamations = new ArrayList<>();
        int prochainId = 1;                            // l'auto-incrément "à la main"

        int choix;
        do {
            System.out.println("""

                    === SIGNALCUA ===
                    1. Signaler un probleme
                    2. Lister les signalements
                    0. Quitter
                    Votre choix :""");
            choix = clavier.nextInt();
            clavier.nextLine();                        // avalez le \n résiduel

            switch (choix) {
                case 1 -> {
                    // ----- Saisie d'une réclamation -----
                    System.out.print("Description : ");
                    String description = clavier.nextLine().trim();

                    System.out.print("Quartier : ");
                    String quartier = clavier.nextLine().trim();

                    try {
                        // On passe l'id ACTUEL ; on ne l'incrémente qu'en cas de succès
                        Reclamation r = new Reclamation(prochainId, description, quartier);
                        prochainId++; // le prochain signalement aura l'id suivant
                        reclamations.add(r);
                        System.out.println("Enregistre :");
                        r.afficher();
                    } catch (IllegalArgumentException e) {
                        System.out.println("Refus : " + e.getMessage());
                    }
                }
                case 2 -> {
                    // ----- Affichage de toutes les réclamations -----
                    System.out.println("--- " + reclamations.size() + " signalement(s) ---");
                    for (Reclamation r : reclamations) {
                        r.afficher();
                    }
                }
                case 0 -> System.out.println("Au revoir");
                default -> System.out.println("Choix inconnu");
            }
        } while (choix != 0);

        clavier.close(); // fermer le Scanner en fin de programme
    }
}
```

Deux nouveautés à démystifier :

- **`prochainId` incrémenté APRÈS le succès** : subtilité importante — si l'id était incrémenté dans l'appel (`prochainId++` en argument), il serait consommé AVANT que le constructeur ne valide ; un refus « brûlerait » un id. On passe l'id actuel, et on n'incrémente qu'après la création réussie.
- **`try { ... } catch (...) { ... }`** : « essaie ce bloc ; si une exception survient, exécute le bloc de rattrapage au lieu de planter ». C'est la première rencontre avec la gestion d'erreurs — le sujet entier de la partie 4 ; ici, retenez juste le réflexe : les saisies utilisateur sont par définition imprévisibles, on ne les fait jamais planter le programme sans raison.

Exemple de session :

```text
=== SIGNALCUA ===
1. Signaler un probleme
2. Lister les signalements
0. Quitter
Votre choix :
1
Description : Nid de poule
Quartier : Medina
Enregistre : REC-1
...
```

---

## 4. Bonnes pratiques modernes (2025-2026)

- **UN seul `Scanner` sur `System.in`** pour tout le programme, créé une fois — jamais plusieurs (ils se disputeraient les caractères).
- **`.trim()` sur chaque saisie** avant traitement (leçon 03) : l'humain tape des espaces parasites.
- **Validez avant d'utiliser** (`hasNextInt()`, constructeur qui refuse l'invalide) : le programme ne doit jamais planter sur une saisie maladroite.
- **`do-while` pour les menus** : le menu s'affiche au moins une fois, par définition.
- **Fermez le `Scanner`** (`clavier.close()`) en fin de programme.
- **Ne surinvestissez pas** (roadmap) : c'est une étape transitoire. La vraie application recevra des requêtes HTTP — le `Scanner` ne servira qu'aux exercices et aux petits outils CLI.
- **Pour aller plus loin un jour** (pas maintenant) : `BufferedReader`/`System.console()` pour des besoins avancés (mot de passe masqué, performances) — la roadmap indique explicitement « à voir, pas à approfondir ».

---

## 5. Pièges à éviter

### Piège 1 — Le `\n` résiduel après `nextInt()`
```java
// ❌ MAUVAIS : nextLine() renvoie la chaîne VIDE restante
int age = clavier.nextInt();
String nom = clavier.nextLine(); // "" — l'utilisateur n'a rien pu taper !

// ✅ BON : débarrassez l'assiette avant de servir
int age = clavier.nextInt();
clavier.nextLine();              // consomme le \n résiduel
String nom = clavier.nextLine(); // là, ça attend vraiment la saisie
```

### Piège 2 — Plusieurs Scanner sur System.in
```java
// ❌ MAUVAIS : deux transcripteurs sur le même micro — comportement imprévisible
Scanner a = new Scanner(System.in);
Scanner b = new Scanner(System.in);

// ✅ BON : un seul, partagé (passez-le en paramètre aux méthodes qui en ont besoin)
Scanner clavier = new Scanner(System.in);
```

### Piège 3 — nextInt() sans garde sur une saisie non numérique
```java
// ❌ MAUVAIS : "douze" fait planter le programme
int age = clavier.nextInt(); // InputMismatchException

// ✅ BON : vérifier avant de lire, redemander sinon
while (!clavier.hasNextInt()) {
    clavier.nextLine();
    System.out.print("Nombre requis. Recommencez : ");
}
int age = clavier.nextInt();
```

### Anti-pattern — faire confiance à la saisie utilisateur
```java
// ❌ MAUVAIS : on encaisse la saisie brute dans l'objet
String description = clavier.nextLine();
Reclamation r = new Reclamation(id, description, quartier); // et si c'est "   " ?

// ✅ BON : nettoyer AVANT, et laisser le constructeur trancher EN DERNIER RECOURS
String description = clavier.nextLine().trim();
try {
    Reclamation r = new Reclamation(id, description, quartier); // refus si vide
} catch (IllegalArgumentException e) {
    System.out.println("Refus : " + e.getMessage());
}
```

---

## 📖 Vocabulaire / Abréviations

| Terme | Définition en une ligne |
|---|---|
| **Flux d'entrée (`System.in`)** | Canal brut par lequel arrivent les données du clavier. |
| **`Scanner`** | Utilitaire `java.util` qui découpe et convertit le flux d'entrée en données exploitables. |
| **`nextLine()`** | Lit toute la ligne jusqu'à Entrée (retourne une `String`). |
| **`nextInt()` / `nextDouble()`** | Lit un entier / un décimal (plante si la saisie n'en est pas un). |
| **`hasNextInt()`** | Vérifie SANS consommer que le prochain élément est un entier. |
| **`InputMismatchException`** | Erreur levée quand `nextInt()` reçoit autre chose qu'un nombre. |
| **Exception** | Erreur d'exécution signalée par un plantage contrôlé (sujet de la partie 4). |
| **`try` / `catch`** | « Essaie ce bloc ; si une exception survient, exécute le plan B au lieu de planter ». |
| **`e.getMessage()`** | Le message porté par l'exception attrapée (ex. « La description est obligatoire »). |
| **CLI** | *Command Line Interface* : programme piloté au clavier dans un terminal. |
| **API** | *Application Programming Interface* : interface qu'un programme consomme via le réseau (partie 7), pas un humain au clavier. |
| **Buffer** | Zone mémoire d'attente où s'accumulent les caractères tapés avant d'être lus. |

## Checklist de validation

Avant de conclure la partie 1, vérifiez que vous savez :

- [ ] Créer UN `Scanner` et lire une ligne, un entier, un décimal.
- [ ] Expliquer le piège du `\n` résiduel et le corriger (`nextInt()` puis `nextLine()` jetable).
- [ ] Protéger une lecture numérique avec `hasNextInt()` et redemander.
- [ ] Écrire un menu console avec `do-while` et un `switch` expression.
- [ ] Encaisser une saisie invalide avec `try/catch` sans faire planter le programme.
- [ ] Avoir finalisé l'Étape 1 du fil rouge : réclamations saisies au clavier, listées, comptées.

➡️ **Fin de la partie 1 !** Vous maîtrisez types, variables, décisions, boucles, tableaux/Strings, classes, `static`, packages et saisie clavier — et SignalCUA vit en console. **La suite logique** : la partie 2 (POO) va rendre vos objets solides (encapsulation), réutilisables (héritage, interfaces) et modernes (records, enum) — le statut `String` de `Reclamation` deviendra enfin un vrai `enum`, corrigeant l'anti-pattern repéré dès la leçon 01.





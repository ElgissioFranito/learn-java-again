# Leçon 02 — Structures de contrôle (conditions, boucles)

> 🧭 **Pont depuis la leçon 01** : vous savez maintenant déclarer des variables et faire des calculs. Mais un programme qui ne fait que calculer de haut en bas est rigide : il ne réagit pas aux données. Il manque deux capacités : **prendre des décisions** (« si le statut est URGENT, affiche une alerte ») et **répéter des actions** (« pour chaque réclamation, affiche-la »). C'est exactement ce que font les structures de contrôle, le sujet de cette leçon. Toutes vos variables de la leçon 01 (`boolean`, `int`, `String`) vont devenir les ingrédients de ces décisions.

---

## 1. Objectifs d'apprentissage

À la fin de cette leçon, vous saurez :

- Écrire des conditions avec `if/else if/else` et combiner plusieurs conditions.
- Utiliser le `switch` **expression** moderne (syntaxe `->`), plus sûr que l'ancien.
- Choisir la bonne boucle : `for`, `for-each`, `while`, `do-while`.
- Utiliser `break` et `continue` à bon escient.
- Aplatir des conditions imbriquées avec les **early returns**.

---

## 2. Explication simple

### 2.1 `if / else` : la décision de base

**Pourquoi ?** Un programme utile réagit différemment selon les données : une réclamation urgente ne se traite pas comme une réclamation normale.

**Comment ?** `if` signifie « si la condition est vraie, exécute ce bloc » :

```java
int delaiJours = 10;

if (delaiJours > 7) {
    // Exécuté SEULEMENT si la condition est vraie
    System.out.println("Reclamation en retard !");
} else if (delaiJours > 3) {
    // Exécuté si la 1re condition est fausse ET celle-ci vraie
    System.out.println("Delai bientot depasse");
} else {
    // Exécuté si TOUTES les conditions précédentes sont fausses
    System.out.println("Delai respecte");
}
```

**Analogie** : un aiguillage de voie ferrée. Le train (l'exécution du programme) ne peut prendre qu'un seul des chemins, selon la position de l'aiguille (la condition).

**Quand ?** Dès que le comportement dépend d'une donnée. C'est l'outil le plus utilisé du programmeur.

> 💡 Les conditions sont des expressions `boolean` (leçon 01) : `>`, `<`, `>=`, `==`, `!=`, combinées avec `&&` (ET) et `||` (OU). Un `if` sans `boolean` ne compile pas.

### 2.2 Le `switch` expression moderne

**Pourquoi ?** Quand on compare **une même valeur** à plusieurs cas précis (un statut, un code…), une cascade d'`if/else if` devient lourde. Le `switch` est fait pour ça.

**Comment ?** Java moderne (14+) utilise la syntaxe `->` qui est **plus sûre** : pas de `break` à oublier, et le compilateur vérifie que tous les cas sont couverts quand on utilise un `enum` (partie 2) :

```java
String statut = "EN_COURS";

// switch EXPRESSION : le switch RENVOIE une valeur qu'on stocke
String action = switch (statut) {
    case "NOUVELLE" -> "Affecter un agent";
    case "EN_COURS" -> "Relancer l'agent";
    case "RESOLUE"  -> "Archiver";
    default         -> "Statut inconnu"; // obligatoire : que faire sinon ?
};
System.out.println(action); // Relancer l'agent
```

**Quand ?** `switch` quand on compare UNE valeur à des cas fixes. `if` quand les conditions sont hétérogènes (ex. `x > 5 && y < 2`).

> 💡 **L'ancien `switch`** (avec `case ... :` et `break`) existe encore dans beaucoup de code. Son piège : sans `break`, l'exécution « tombe » dans le cas suivant (**fallthrough**). La syntaxe `->` élimine ce risque — utilisez-la systématiquement.

### 2.3 Les boucles

**Pourquoi ?** Traiter 500 réclamations en écrivant 500 fois le même code serait absurde. La boucle dit « répète ce bloc jusqu'à condition ».

**Comment ? Quatre outils, un par situation :**

```java
// 1. for : quand on connaît le NOMBRE d'itérations (ou un index)
for (int i = 0; i < 3; i++) {
    // i vaut 0, puis 1, puis 2
    System.out.println("Tour numero " + i);
}
// Lecture : "part de i=0 ; continue tant que i<3 ; à chaque fin de tour, i++ (ajoute 1)"

// 2. for-each : pour parcourir CHAQUE élément d'une collection — le plus utilisé
String[] quartiers = {"Medina", "Plateau", "Grand Yoff"};
for (String quartier : quartiers) {
    System.out.println("Quartier : " + quartier);
}
// Lecture : "pour chaque String nommée quartier DANS quartiers, fais ceci"

// 3. while : répète TANT QUE la condition est vraie (vérifiée AVANT chaque tour)
int attente = 0;
while (attente < 3) {
    System.out.println("En attente depuis " + attente + " h");
    attente++; // ⚠️ sans cette ligne, la boucle ne s'arrête JAMAIS (boucle infinie)
}

// 4. do-while : comme while, mais le corps est exécuté AU MOINS UNE FOIS
//    (utile pour ré-afficher un menu jusqu'à un choix valide — leçon 07)
```

**Comment choisir ?**

| Situation | Boucle |
|---|---|
| Parcourir une collection/tableau | `for-each` |
| Nombre d'itérations connu (compteur, index) | `for` |
| On ne sait pas combien de tours, condition d'arrêt connue | `while` |
| Le corps doit s'exécuter au moins une fois | `do-while` |

### 2.4 `break` et `continue`

```java
for (int i = 0; i < 10; i++) {
    if (i == 3) {
        continue; // passe directement au tour suivant (saute le reste du corps)
    }
    if (i == 6) {
        break; // sort DÉFINITIVEMENT de la boucle
    }
    System.out.println(i); // affiche 0, 1, 2, 4, 5 puis s'arrête
}
```

**Analogie** : `continue` = « celui-là, je le passe » ; `break` = « j'arrête tout, je sors ».

### 2.5 Early return : aplatir les conditions

**Pourquoi ?** Des `if` imbriqués sur 3+ niveaux deviennent illisibles (« pyramide de malheur »). L'astuce des pros : **traiter d'abord les cas d'erreur et sortir immédiatement**. (Un `return` quitte la méthode en cours en renvoyant éventuellement une valeur — nous approfondirons les méthodes en leçon 04.)

```java
// ❌ IMBRIQUÉ (difficile à suivre) :
String valider(int age, boolean aPaye) {
    if (age >= 18) {
        if (aPaye) {
            return "Acces autorise";
        } else {
            return "Paiement requis";
        }
    } else {
        return "Majeur requis";
    }
}

// ✅ EARLY RETURN (plat et lisible) :
String valider(int age, boolean aPaye) {
    if (age < 18) {
        return "Majeur requis";       // cas d'erreur traité et terminé
    }
    if (!aPaye) {
        return "Paiement requis";     // cas d'erreur suivant
    }
    return "Acces autorise";          // le "chemin heureux" reste au premier niveau
}
```

---

## 3. Exemples concrets

Créez un fichier `ExempleControle.java` (méthode d'exécution dans `02-exercice.md`) :

```java
public class ExempleControle {
    public static void main(String[] args) {
        // ----- 1. if/else sur une réclamation -----
        int delaiJours = 10;
        boolean urgente = true;

        // Combinaison de deux conditions avec OU logique
        if (urgente || delaiJours > 7) {
            System.out.println("ALERTE : a traiter en priorite");
        } else {
            System.out.println("Traitement normal");
        }

        // ----- 2. switch expression sur un statut -----
        String statut = "EN_COURS";
        String action = switch (statut) {
            case "NOUVELLE" -> "Affecter un agent";
            case "EN_COURS" -> "Relancer l'agent";
            case "RESOLUE"  -> "Archiver";
            default         -> "Statut inconnu";
        };
        System.out.println("Action : " + action);

        // ----- 3. for-each sur un tableau -----
        String[] quartiers = {"Medina", "Plateau", "Grand Yoff"};
        int nbReclamationsMedina = 12;
        int nbReclamationsPlateau = 8;
        int nbReclamationsGrandYoff = 15;

        // (Pour cet exemple simple, on affiche un tableau parallèle d'entiers)
        int[] nbReclamations = {12, 8, 15};
        for (int i = 0; i < quartiers.length; i++) {
            // .length donne la TAILLE du tableau (3 ici)
            System.out.println(quartiers[i] + " : " + nbReclamations[i] + " reclamations");
        }

        // ----- 4. for classique pour un total -----
        int total = 0;
        for (int nb : nbReclamations) {
            total = total + nb; // on accumule à chaque tour
        }
        System.out.println("Total : " + total + " reclamations");

        // ----- 5. while avec un compteur -----
        int relances = 0;
        while (relances < 3) {
            relances++;
            System.out.println("Relance numero " + relances);
        }

        // ----- 6. continue : ignorer les quartiers déjà traités -----
        for (int nb : nbReclamations) {
            if (nb == 0) {
                continue; // rien à afficher pour ce quartier
            }
            System.out.println("A traiter : " + nb);
        }
    }
}
```

---

## 4. Bonnes pratiques modernes (2025-2026)

- **`switch` expression `->` par défaut** : plus concis, plus sûr (pas de fallthrough possible). Gardez l'ancien `switch ... : break;` seulement pour lire du code ancien.
- **Early returns** en début de méthode pour valider les cas d'erreur, puis le « chemin heureux » au premier niveau.
- **`for-each` dès que possible** : moins d'erreurs d'index que le `for` classique.
- **Conditions positives** : `if (estValide)` plutôt que `if (!estInvalide)` — les doubles négations fatiguent le lecteur.
- **Extraire les conditions complexes** dans des variables `boolean` bien nommées :

```java
// ❌ condition dense, difficile à lire
if (r.getDelai() > 7 && (r.getPriorite() == 1 || r.getQuartier().equals("Medina"))) { ... }

// ✅ la même chose, auto-documentée
boolean enRetard = delai > 7;
boolean prioritaire = priorite == 1 || quartier.equals("Medina");
if (enRetard && prioritaire) { ... }
```

- **Pattern matching dans `switch` (Java 21+)** : `switch (obj) { case Reclamation r when r.urgente() -> ...; }` — retenez que ça existe, nous le pratiquerons en partie 2 (leçon 04).

## 5. Pièges à éviter

### Piège 1 — Le fallthrough de l'ancien switch
```java
// ❌ MAUVAIS : sans break, tous les cas suivants s'exécutent aussi !
switch (statut) {
    case "NOUVELLE":
        affecterAgent();      // s'exécute...
        // pas de break -> on "tombe" dans EN_COURS !
    case "EN_COURS":
        relancerAgent();      // ...et ça aussi. Bug silencieux.
        break;
}

// ✅ BON : la flèche -> ne tombe jamais
switch (statut) {
    case "NOUVELLE" -> affecterAgent();
    case "EN_COURS" -> relancerAgent();
}
```

### Piège 2 — Confondre `=` et `==`
```java
// ❌ MAUVAIS : "=" AFFECTE (ne compile pas ici car int n'est pas boolean,
//  mais passe avec des boolean : if (actif = false) met actif à false !)
int note = 15;
// if (note = 20) { ... } // ERREUR de compilation : "int" n'est pas "boolean"

// ✅ BON : "==" COMPARE
if (note == 20) { ... }
```

### Piège 3 — La boucle infinie
```java
// ❌ MAUVAIS : le compteur n'est jamais incrémenté -> la boucle ne s'arrête jamais
int i = 0;
while (i < 5) {
    System.out.println(i); // affiche 0 à l'infini...
}
// La ligne i++; a été oubliée dans le corps.

// ✅ BON : s'assurer que la condition finira par être fausse
int i = 0;
while (i < 5) {
    System.out.println(i);
    i++;
}
```

### Piège 4 — Erreur d'un sur les bornes (`<=` vs `<`)
```java
int[] tab = {10, 20, 30}; // indices valides : 0, 1, 2 (taille = 3)

// ❌ MAUVAIS : i <= tab.length accède à l'indice 3 qui N'EXISTE PAS
for (int i = 0; i <= tab.length; i++) {
    System.out.println(tab[i]); // ArrayIndexOutOfBoundsException à i=3
}

// ✅ BON : strictement inférieur à la taille
for (int i = 0; i < tab.length; i++) {
    System.out.println(tab[i]);
}
```

### Anti-pattern — la pyramide de malheur
```java
// ❌ MAUVAIS : 4 niveaux d'imbrication, le cerveau lâche
if (reclamation != null) {
    if (reclamation.estUrgente()) {
        if (!reclamation.estAssignee()) {
            assigner(reclamation); // où suis-je déjà ?
        }
    }
}

// ✅ BON : early return (dans une méthode), conditions combinées ou garde inversée
if (reclamation == null || !reclamation.estUrgente() || reclamation.estAssignee()) {
    return; // rien à faire, on sort
}
assigner(reclamation); // le chemin heureux, sans indentation
```

---

## 📖 Vocabulaire / Abréviations

| Terme | Définition en une ligne |
|---|---|
| **Structure de contrôle** | Instruction qui décide quelles lignes du programme s'exécutent et dans quel ordre. |
| **Condition** | Expression qui vaut `true` ou `false`, testée par `if`/`while`/`switch`. |
| **Bloc** | Groupe d'instructions délimité par `{ }`. |
| **Switch expression** | Version moderne du `switch` (Java 14+) qui **renvoie une valeur** et utilise `->`. |
| **Fallthrough** | Comportement de l'ancien `switch` où l'exécution « tombe » dans le cas suivant sans `break`. |
| **Boucle** | Structure qui répète un bloc d'instructions. |
| **Itération** | Un « tour » de boucle. |
| **Index** | Position d'un élément dans un tableau (commence à **0** en Java !). |
| **Boucle infinie** | Boucle dont la condition d'arrêt n'est jamais atteinte (le programme se fige). |
| **`break`** | Sort immédiatement de la boucle ou du `switch` en cours. |
| **`continue`** | Abandonne le tour de boucle en cours et passe au suivant. |
| **Early return** | Style où l'on sort d'une méthode dès qu'un cas est tranché, pour garder le code plat. |
| **Garde (guard clause)** | Condition d'erreur testée en début de méthode avec early return. |
| **`ArrayIndexOutOfBoundsException`** | Erreur d'exécution levée quand on accède à un indice de tableau qui n'existe pas. |
| **Pattern matching** | Capacité (Java 21+) de tester le type d'un objet directement dans un `switch`. |

## Checklist de validation

Avant de passer à la leçon 03, vérifiez que vous savez :

- [ ] Écrire un `if/else if/else` et combiner des conditions avec `&&` et `||`.
- [ ] Écrire un `switch` expression avec `->` et un cas `default`.
- [ ] Choisir entre `for`, `for-each`, `while` et `do-while` selon la situation.
- [ ] Expliquer ce que font `break` et `continue`.
- [ ] Expliquer pourquoi l'ancien `switch` est dangereux (fallthrough).
- [ ] Détecter et corriger une boucle infinie.
- [ ] Expliquer pourquoi on boucle avec `<` et non `<=` sur un tableau.
- [ ] Transformer des `if` imbriqués en early returns.

➡️ **Prochaine étape** : vous savez stocker et décider. Il reste à manipuler des **groupes de données** (plusieurs réclamations d'un coup) et du **texte avancé** — c'est le sujet de la leçon 03 : tableaux et `String`.





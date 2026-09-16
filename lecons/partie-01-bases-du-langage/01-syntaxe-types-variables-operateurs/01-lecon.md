# Leçon 01 — Syntaxe, types, variables et opérateurs

> 🧭 **Pont depuis…** : vous n'avez encore rien codé en Java dans ce parcours. Cette leçon est la toute première brique : avant de faire quoi que ce soit (conditions, boucles, objets, Spring Boot), il faut savoir **stocker des valeurs** et **faire des calculs**. Si vous venez du monde JavaScript/TypeScript, beaucoup de choses vous sembleront familières — mais Java est plus « strict » : chaque variable a un type fixé à l'avance. C'est cette strictitude qui rend les gros projets fiables.

---

## 1. Objectifs d'apprentissage

À la fin de cette leçon, vous saurez :

- Écrire et exécuter un premier programme Java avec la méthode `main`.
- Déclarer des variables avec les types de base (`int`, `long`, `double`, `boolean`, `char`, `String`) et choisir le bon type.
- Utiliser les opérateurs arithmétiques, de comparaison, logiques et le ternaire.
- Convertir un type en un autre (le « casting ») sans mauvaise surprise.
- Éviter les 4 pièges classiques du débutant : division entière, `==` sur les objets, débordement (overflow) d'`int`, et `double` pour l'argent.

---

## 2. Explication simple

### 2.1 Qu'est-ce qu'une variable ?

**Pourquoi ?** Un programme sert à transformer des données (un nom, un montant, un statut…). Pour travailler sur ces données, il faut pouvoir les ranger en mémoire et leur donner un nom.

**Comment ?** Une variable est comme une **boîte étiquetée** dans un entrepôt :
- l'**étiquette** = le nom de la variable (ex. `prix`),
- le **contenu** = la valeur (ex. `12.50`),
- et en Java, la boîte a une **taille et une forme fixées** : on déclare au départ ce qu'elle peut contenir (`double` pour un nombre à virgule, etc.). On ne peut pas ensuite y mettre autre chose.

**Quand ?** Partout, tout le temps. C'est l'atome de base de tout programme.

```java
// Déclaration : TYPE nom = valeur;
int age = 30;              // boîte de type "entier" contenant 30
double prix = 12.50;       // boîte de type "nombre à virgule"
boolean estUrgent = false; // boîte ne pouvant contenir que true ou false
```

### 2.2 Types primitifs vs types objets

Java a deux familles de types, et c'est LA première chose qui surprend :

| Famille | Exemples | Ce que contient la variable |
|---|---|---|
| **Types primitifs** (8 au total) | `int`, `long`, `double`, `boolean`, `char` | directement la valeur |
| **Types objets** | `Integer`, `Long`, `Double`, `Boolean`, `String` | une « référence » vers un objet en mémoire |

**Analogie** : un type primitif, c'est un billet de 10 € dans votre poche (la valeur elle-même). Un type objet, c'est un reçu papier qui dit « il y a 10 € dans le coffre n°42 » (une adresse vers la valeur).

**Pourquoi deux familles ?** Raisons historiques de performance : les primitifs sont légers et rapides. Les objets sont nécessaires dès qu'on veut des fonctionnalités supplémentaires (les collections de la partie 3 ne peuvent contenir que des objets, pas des primitifs).

**Comment ça se joue concrètement ?** Java convertit automatiquement l'un vers l'autre, c'est l'**auto-boxing** (primitif → objet) et l'**unboxing** (objet → primitif) :

```java
Integer nb = 5;        // auto-boxing : le int 5 est "emballé" dans un Integer
int memeNb = nb;       // unboxing : l'Integer est "déballé" en int
```

⚠️ Mais attention : cette commodité a un piège, expliqué en section 5.

### 2.3 Les types numériques et le mot-clé `final`

- `int` : entier, jusqu'à ~2 milliards (2 147 483 647).
- `long` : entier « grand format », jusqu'à ~9 trillions (9 223 372 036 854 775 807).
- `double` : nombre à virgule flottante (approximatif ! voir 2.4).
- `char` : UN seul caractère, entre apostrophes simples : `'A'`.
- `String` : du texte entre guillemets doubles : `"Bonjour"`. (Techniquement c'est un objet, pas un primitif.)

**Quand choisir `long` plutôt qu'`int` ?** Dès qu'une quantité peut dépasser 2 milliards : population d'un pays, nombre de secondes depuis 1970, identifiants générés en masse…

**Le mot-clé `final`** : il interdit de réassigner la variable après sa première affectation.

```java
final double TAUX_TVA = 0.18; // constante : ne peut plus changer
// TAUX_TVA = 0.20;           // ❌ ERREUR de compilation
```

⚠️ Nuance importante : `final` rend **la boîte immuable, pas forcément le contenu**. Pour un objet, `final` empêche de remplacer la référence, mais l'objet pointé peut rester modifiable. Pour les primitifs, `final` = valeur figée, point.

> 💡 **D'où vient ce nom « final » ?** En Java, la déclaration se fait **à l'avance** et le compilateur vérifie tout avant même d'exécuter le programme (contrairement à JS où le type est découvert à l'exécution). `final` est un contrat que le compilateur fait respecter.

### 2.4 Le cas particulier de `double` : attention à l'argent

**Pourquoi ?** Un `double` est stocké en base 2 (des puissances de 2), alors que nous comptons en base 10. Certains nombres « ronds » pour nous (0.1) sont **impairs en base 2**, donc stockés de façon approximative.

**Comment le constater ?**

```java
System.out.println(0.1 + 0.2); // affiche 0.30000000000000004 (et non 0.3 !)
```

**Quand ?** Pour des calculs scientifiques ou statistiques, `double` suffit. **Jamais pour de l'argent** : utilisez la classe `BigDecimal` (fournie avec Java) qui stocke le nombre en base 10, exactement.

### 2.5 Les opérateurs

```java
// Arithmétiques
int a = 10, b = 3;
int somme = a + b;      // 13
int reste = a % b;      // 1 (modulo : reste de la division 10 ÷ 3)

// Comparaisons (le résultat est un boolean)
boolean egal = (a == b);        // false
boolean plusGrand = (a > b);    // true

// Logiques : ET (&&), OU (||), NON (!)
boolean ok = (a > 5) && (b < 5); // true : les deux conditions sont vraies
boolean nonOk = !ok;              // false : inverse

// Ternaire : condition ? valeurSiVrai : valeurSiFaux
String mention = (a >= 10) ? "admis" : "ajourné";
```

> 💡 **Pourquoi `&&` et pas juste `&` ?** `&&` s'arrête dès que le résultat est certain (« court-circuit ») : si la première condition est fausse, la seconde n'est même pas évaluée. C'est plus rapide et évite des erreurs (ex. `objet != null && objet.valeur > 0`).

### 2.6 Conversion de types (casting)

**Pourquoi ?** Parfois on a un `int` et on a besoin d'un `double` (ou l'inverse). Java distingue deux cas :

```java
// 1. Conversion implicite (élargissement) : automatique, sans risque.
//    On range un petit dans un grand.
int entier = 5;
double grand = entier;      // OK : 5.0 — Java le fait tout seul

// 2. Conversion explicite (rétrécissement) : il faut le demander, au risque
//    de perdre des informations. On "force" avec (type) devant la valeur.
double prix = 12.75;
int tronque = (int) prix;   // 12 — la partie décimale est PERDUE (pas arrondie !)
```

**Analogie** : verser le contenu d'un petit verre dans un seau = sans risque (implicite). Verser le contenu d'un seau dans un petit verre = ça déborde, il faut accepter de perdre du liquide (explicite).

### 2.7 Le mot-clé `var` (Java 10+)

`var` demande au compilateur de **déduire** le type à partir de la valeur de droite. La variable reste fortement typée (le type est fixé une fois pour toutes) :

```java
var nom = "Awa";     // le compilateur déduit String
// nom = 42;         // ❌ erreur : nom est un String, le type est fixé
```

**Quand l'utiliser ?** Quand le type est **évident** à droite : `var liste = new ArrayList<String>()`. À éviter quand le type n'est pas visible. En entreprise Java, le typage explicite reste la norme (contrairement à TypeScript où l'inférence domine).

---

## 3. Exemples concrets

Créez un fichier `ExempleTypes.java` et exécutez-le (méthode pas à pas dans `02-exercice.md`) :

```java
public class ExempleTypes {
    public static void main(String[] args) {
        // ----- 1. Les types de base -----
        int nbReclamations = 42;          // entier
        long population = 2_500_000_000L; // grand entier, notez le "L" final
        double tauxAcceptation = 0.75;    // nombre à virgule (approximatif)
        char niveau = 'A';                // UN caractère, apostrophes simples
        boolean estTraitee = false;       // vrai/faux
        String quartier = "Medina";       // texte, guillemets doubles

        // Le "_" dans 2_500_000_000 est purement visuel : il sépare les milliers.

        System.out.println("Reclamations : " + nbReclamations);
        System.out.println("Population : " + population);
        System.out.println("Quartier : " + quartier + ", niveau " + niveau);

        // ----- 2. Le piège de la division entière -----
        System.out.println(5 / 2);    // affiche 2  : division ENTRE int = résultat tronqué
        System.out.println(5.0 / 2);  // affiche 2.5 : au moins un operande est double
        System.out.println((double) 5 / 2); // affiche 2.5 : on force le cast AVANT la division

        // ----- 3. Le piège du == avec les objets -----
        Integer x = 1000;
        Integer y = 1000;
        System.out.println(x == y);        // false ! On compare les "reçus", pas les valeurs
        System.out.println(x.equals(y));   // true  : on compare le CONTENU

        // ----- 4. L'overflow (débordement) silencieux -----
        int max = Integer.MAX_VALUE;  // le plus grand int possible : 2 147 483 647
        System.out.println(max + 1);  // affiche -2147483648 ! Pas d'erreur, résultat FAUX

        // ----- 5. BigDecimal pour l'argent -----
        java.math.BigDecimal prixEau = new java.math.BigDecimal("0.1");
        java.math.BigDecimal prixElec = new java.math.BigDecimal("0.2");
        System.out.println(prixEau.add(prixElec)); // affiche 0.3 exactement

        // ----- 6. Text block (Java 15+) pour du texte multi-lignes -----
        String json = """
                {
                  "quartier": "Medina",
                  "nbReclamations": 42
                }
                """;
        System.out.println(json);
    }
}
```

---

## 4. Bonnes pratiques modernes (2025-2026)

- **`var` avec parcimonie** : uniquement quand le type est évident à droite de `=`. Jamais pour les paramètres ou valeurs de retour de méthodes publiques.
- **Text blocks `"""`** pour toute chaîne multi-lignes (SQL, JSON de test) : plus lisible que `+ "\n" +`.
- **`long` par défaut pour les identifiants** générés en masse (base de données) et pour les durées en millisecondes.
- **`BigDecimal` pour tout ce qui est monétaire**, créé à partir d'une `String` (`new BigDecimal("0.1")`) et non d'un `double` (sinon l'imprécision est déjà dedans !).
- **Réflexe null** : à chaque déclaration, demandez-vous « cette valeur peut-elle être `null` ? ». C'est LE réflexe n°1 en Java — le compilateur ne vous protégera pas comme TypeScript avec `strictNullChecks`.
- **Nommage** : variables en `camelCase` (ex. `nbReclamations`), constantes en `MAJUSCULES_AVEC_UNDERSCORES` (ex. `TAUX_TVA`), classes en `PascalCase`.
- **Activez les warnings du compilateur** dès le premier jour (`javac -Xlint:all`) et ne les ignorez jamais.

## 5. Pièges à éviter

### Piège 1 — La division entière
```java
// ❌ MAUVAIS : on veut une moyenne, on obtient 2
int somme = 5, nb = 2;
double moyenne = somme / nb;      // 2.0 (la division int est déjà tronquée !)

// ✅ BON : forcer au moins un operande en double AVANT de diviser
double moyenneOk = (double) somme / nb;  // 2.5
```

### Piège 2 — Comparer des objets avec `==`
```java
// ❌ MAUVAIS : compare les références (les "reçus"), pas le contenu
if (statut1 == statut2) { ... }

// ✅ BON : .equals() compare le contenu
if (statut1.equals(statut2)) { ... }
```

### Piège 3 — L'overflow silencieux d'`int`
```java
// ❌ MAUVAIS : aucun message d'erreur, le résultat est simplement FAUX
int totalVues = nbVues1 + nbVues2; // peut déborder

// ✅ BON : anticiper le volume, utiliser long
long totalVuesOk = (long) nbVues1 + nbVues2;
```

### Piège 4 — `double` pour l'argent
```java
// ❌ MAUVAIS : 0.30000000000000004 en facture...
double total = 0.1 + 0.2;

// ✅ BON : BigDecimal, construit depuis une String
java.math.BigDecimal totalOk = new java.math.BigDecimal("0.1")
        .add(new java.math.BigDecimal("0.2")); // 0.3
```

### Anti-pattern — le « Stringly typed »
```java
// ❌ MAUVAIS : un statut codé en String libre → fautes de frappe possibles
String statut = "EN_COUR"; // personne ne détectera la coquille !

// ✅ BON : un enum (voir partie 2, leçon 05) rend le statut impossible à mal orthographier.
// Pour l'instant, retenez juste le principe : une valeur qui n'a qu'un petit
// nombre d'options possibles ne doit pas être une String libre.
```

---

## 📖 Vocabulaire / Abréviations

| Terme | Définition en une ligne |
|---|---|
| **Variable** | Boîte nommée en mémoire, de type fixé, contenant une valeur. |
| **Type primitif** | Type de base (8 en tout : `int`, `long`, `double`, `boolean`…) contenant directement la valeur. |
| **Type objet / classe** | Type dont la variable contient une référence (une « adresse ») vers la valeur en mémoire. |
| **Référence** | Adresse qui pointe vers un objet en mémoire (comme un reçu pointant vers un coffre). |
| **Auto-boxing / Unboxing** | Conversion automatique primitif ↔ objet (`int` ↔ `Integer`). |
| **`null`** | Valeur spéciale signifiant « cette variable ne pointe vers rien ». |
| **Casting** | Conversion explicite d'un type vers un autre, avec `(type)` devant la valeur. |
| **Élargissement / rétrécissement** | Conversion sans perte (petit → grand, automatique) vs avec perte possible (grand → petit, à forcer). |
| **Overflow (débordement)** | Dépassement de la capacité d'un type numérique : résultat faux sans aucun message d'erreur. |
| **Modulo (`%`)** | Opérateur donnant le reste d'une division (`10 % 3` = 1). |
| **Ternaire (`? :`)** | Expression conditionnelle compacte : `condition ? valeurSiVrai : valeurSiFaux`. |
| **Compilation** | Étape où le compilateur `javac` vérifie votre code et le traduit en instructions exécutables — les erreurs de type sont attrapées ici, avant l'exécution. |
| **`String`** | Classe Java représentant du texte (imuable : toute « modification » crée une nouvelle chaîne). |
| **`BigDecimal`** | Classe du JDK pour des nombres décimaux exacts (obligatoire pour l'argent). |
| **Text block** | Chaîne multi-lignes délimitée par `"""` (Java 15+). |
| **LTS** | *Long Term Support* : version de Java garantie supportée longtemps (21 et 25 sont LTS). |

## Checklist de validation

Avant de passer à la leçon 02, vérifiez que vous savez :

- [ ] Écrire un programme avec un `main` et l'exécuter.
- [ ] Choisir entre `int`, `long`, `double`, `boolean`, `char` et `String` pour une donnée donnée.
- [ ] Expliquer la différence entre type primitif et type objet (la boîte vs le reçu).
- [ ] Prédire le résultat de `5 / 2` et savoir comment obtenir `2.5`.
- [ ] Comparer le contenu de deux `String`/`Integer` avec `.equals()` et non `==`.
- [ ] Expliquer pourquoi on n'utilise jamais `double` pour de l'argent.
- [ ] Utiliser `final` pour une constante, et savoir ce que `final` fige exactement.
- [ ] Décider quand utiliser `var` et quand écrire le type explicitement.

➡️ **Prochaine étape** : maintenant que vous savez **stocker** des valeurs, il faut apprendre à **prendre des décisions** et à **répéter des actions** — c'est le rôle des structures de contrôle (`if`, `switch`, boucles) de la leçon 02.





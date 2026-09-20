# Leçon 05 — Énumérations (enum)

> 🧭 **Pont depuis la leçon 04** : le trio record + sealed + pattern matching modélise à merveille des états qui **portent des données** (`EnCours(agentAssigne)`, `Resolue(dateResolution)`). Mais dans le fil rouge, il reste des valeurs **sans aucune donnée** : `NOUVELLE`, `EN_COURS`, `RESOLUE` « tout court ». Trois records pour ça, c'est de l'artillerie lourde. Java a l'outil exact depuis toujours — modernisé par le Java récent : l'**énumération (`enum`)**. C'est la dernière leçon de la partie 2, et le remplacement définitif du `String statut` hérité de la partie 1.

---

## 1. Objectifs d'apprentissage

À la fin de cette leçon, vous saurez :

- Écrire un `enum` simple (`enum StatutReclamation { NOUVELLE, EN_COURS, RESOLUE }`) et expliquer pourquoi il est plus sûr qu'un `String` libre.
- Donner à un `enum` des **champs, un constructeur et des méthodes** (ex. un libellé affichable par valeur).
- Utiliser `values()`, `valueOf()`, `.name()` — et savoir pourquoi **`.ordinal()` ne doit jamais servir d'identifiant**.
- Écrire un `switch` sur un `enum` (exhaustif, vérifié par le compilateur).
- Éviter le piège « stringly typed » (constantes `String`/`int` éparpillées pour représenter un ensemble fini).
- Remplacer le statut `String` de SignalCUA par un vrai `enum StatutReclamation`.

---

## 2. Explication simple

### 2.1 Le problème : le statut reste une `String` libre

**Pourquoi ?** Depuis la leçon 01, le statut est protégé par des méthodes de transition — mais son type reste `String`. Rien n'empêche, dans une méthode maladroite, d'écrire `changerStatut("EN_ATTAENTE")` : le compilateur accepte n'importe quel texte. On compare encore avec `.equals("NOUVELLE")` partout (risque de faute de frappe silencieuse : `"NOUVELLE "` avec espace ne matchera jamais).

**Analogie** : c'est le carnet de commandes d'un restaurant où l'on note les plats « au doigt mouillé » — « le goujonne » pour « la dorade ». À l'inverse, un menu **numéroté** (« le 3, s'il vous plaît ») élimine l'erreur : il n'existe qu'un ensemble fini de choix, écrits une fois pour toutes. L'`enum`, c'est le menu numéroté du code.

**Comment ?** Le mot-clé `enum` (pour *enumeration* : énumération) déclare le type ET sa liste complète de valeurs :

```java
public enum StatutReclamation {
    NOUVELLE, EN_COURS, RESOLUE     // les TROIS seules valeurs qui existent
}
```

Désormais, un champ `private StatutReclamation statut;` ne peut valoir QUE l'une des trois valeurs. `changerStatut("EN_ATTAENTE")` ne compile même plus — pas besoin de validation : le type la fait.

**Quand ?** Dès qu'un champ ne peut prendre qu'un **nombre limité et connu de valeurs** : statuts, rôles, catégories, priorités, jours de la semaine. La roadmap compare l'`enum` à l'union type littéral de TypeScript (`'NOUVELLE' | 'EN_COURS' | 'RESOLUE'`), avec en plus la sécurité du typage fort à la compilation.

### 2.2 Un `enum` avec champs, constructeur et méthodes

**Pourquoi ?** Souvent chaque valeur a besoin de porter une information : un libellé affichable (« En cours de traitement » plutôt que « EN_COURS »), une couleur, un délai. L'`enum` est une **vraie classe** : il accepte champs, constructeur et méthodes.

**Analogie** : les plats du menu numéroté ont chacun un prix et une description — le numéro suffit à l'appréhender, mais chaque ligne du menu détient ses infos propres.

**Comment ?** Le constructeur d'un `enum` est toujours `private` (impossible de créer une valeur en plus des constantes déclarées — la liste est fermée, comme une famille scellée) :

```java
public enum StatutReclamation {

    // 1. Les constantes : la liste fermée (chacune appelle le constructeur)
    NOUVELLE("Nouvelle demande", 1),
    EN_COURS("En cours de traitement", 2),
    RESOLUE("Réclamation résolue", 3);

    // 2. Les champs de chaque valeur
    private final String libelle;
    private final int ordreTraitement;   // 1er, 2e, 3e pas du cycle de vie

    // 3. Le constructeur : TOUJOURS private (la liste est fermée)
    private StatutReclamation(String libelle, int ordreTraitement) {
        this.libelle = libelle;
        this.ordreTraitement = ordreTraitement;
    }

    // 4. Des méthodes, comme toute classe
    public String getLibelle() { return libelle; }
    public int getOrdreTraitement() { return ordreTraitement; }
}
```

Utilisation : `StatutReclamation.EN_COURS.getLibelle()` renvoie « En cours de traitement ». Chaque valeur est un objet unique, partagé (créé une seule fois — c'est un singleton, concept qui reviendra en partie 7).

### 2.3 Les méthodes intégrées : `values()`, `valueOf()`, `name()`, `ordinal()`

Chaque `enum` hérite automatiquement de quatre méthodes :

```java
StatutReclamation[] tous = StatutReclamation.values();
// → un tableau des trois valeurs, dans l'ordre de déclaration

StatutReclamation s = StatutReclamation.valueOf("EN_COURS");
// → la valeur correspondant au TEXTE ; lève IllegalArgumentException si inconnu

String texte = StatutReclamation.EN_COURS.name();
// → "EN_COURS" : le nom de la constante, STABLE (à stocker en base, dans une API...)

int position = StatutReclamation.EN_COURS.ordinal();
// → 1 : la position dans la déclaration — VOIR LE PIÈGE ci-dessous
```

> 📖 **Vocabulaire** : **`ordinal()`** = position (0, 1, 2...) de la valeur dans sa déclaration. **`name()`** = son nom exact en `String`. **Singleton** = objet unique, partagé dans tout le programme.

### 2.4 Le piège fatal de `ordinal()`

**Pourquoi c'est dangereux** : si vous stockez l'`ordinal()` comme identifiant (en base de données, dans un fichier, dans une API), tout fonctionne... jusqu'au jour où on **insère une valeur** entre `NOUVELLE` et `EN_COURS` : toutes les positions se décalent d'un cran, et les anciennes données (stockées avec l'ancienne numérotation) deviennent silencieusement **fausses**. `EN_COURS` (stockée « 1 ») devient, après insertion d'un `EN_PAUSE` en 2e position, la valeur d'`EN_PAUSE`. Aucune erreur nulle part. C'est le genre de bug que l'on découvre des mois plus tard.

**La règle** : pour stocker/transmettre, utilisez toujours **`.name()`** (le texte), ou un code explicite (`case "PAUSE" -> ...`). Jamais l'`ordinal()`.

### 2.5 Le `switch` sur un `enum` : exhaustif, comme le pattern matching

Comme avec les familles scellées (leçon 04), le compilateur **vérifie l'exhaustivité** d'un switch sur un `enum` : si vous oubliez une valeur, il refuse de compiler.

```java
// Avec un switch expression (vu en partie 1, leçon 02 — ici sur un enum)
String badge = switch (statut) {
    case NOUVELLE -> "🆕";
    case EN_COURS -> "⏳";
    case RESOLUE  -> "✅";
};   // pas de default : le compilateur SAIT que les 3 valeurs suffisent
```

Ajoutez demain un `EN_PAUSE` à l'`enum` ? Tout switch incomplet casse à la compilation et pointe l'endroit à corriger — le même filet de sûreté que la leçon 04, en plus léger.

### 2.6 `enum` ou `sealed` + records ? Le choix

Les deux leçons se complètent :

| Situation | Outil |
|---|---|
| Valeurs simples, sans donnée (`NOUVELLE`, `EN_COURS`) | **`enum`** (une ligne par valeur) |
| Valeurs portant des données différentes (`EnCours(agent)`, `Resolue(date)`) | **sealed + records** (leçon 04) |
| Valeurs avec des champs identiques (libellé, ordre) | **`enum` avec champs** (section 2.2) |

Règle 2025-2026 : commencez par l'`enum` (plus simple) ; passez aux records scellés quand les états doivent porter des données distinctes. Les deux se combinent : une méthode peut renvoyer un `enum`, le switch peut aussi matcher les deux.

---

## 📖 Vocabulaire / Abréviations

| Terme | Définition en une ligne |
|---|---|
| **`enum`** | Type à ensemble fermé de valeurs constantes (statuts, rôles, catégories). |
| **Constante** | Valeur nommée, créée une fois pour toutes (`StatutReclamation.NOVELLE`... non : `NOUVELLE`). |
| **`values()`** | Renvoie le tableau de toutes les valeurs, dans l'ordre de déclaration. |
| **`valueOf(String)`** | Renvoie la valeur dont le nom correspond au texte (erreur sinon). |
| **`name()`** | Le nom exact de la constante en `String` — ce qu'on stocke/partage. |
| **`ordinal()`** | La position de la constante (0, 1, 2...) — à ne JAMAIS utiliser comme identifiant. |
| **Singleton** | Objet unique partagé dans tout le programme (chaque valeur d'`enum` en est un). |
| **Stringly typed** | Anti-pattern : des `String`/`int` éparpillés pour représenter un ensemble fini de valeurs. |
| **Union type (TS)** | Équivalent TypeScript : un type qui ne peut valoir qu'une liste de valeurs littérales. |
| **Exhaustivité** | Garantie du compilateur : un switch couvre toutes les valeurs d'un `enum`. |
| **Constructeur d'enum** | Toujours `private` : personne ne peut ajouter de valeur à la liste. |

---

## 3. Exemples concrets : le fil rouge passe au vrai `enum`

**Fichier 1 — l'`enum` avec libellés** (`StatutReclamation.java`, celui de la section 2.2). Rien à recopier de plus : réutilisez-le tel quel.

**Fichier 2 — la classe `Reclamation` réglée au statut `enum`** (`Reclamation.java`) :

```java
public class Reclamation {

    private final int id;
    private final String description;
    private final String quartier;
    private StatutReclamation statut;   // ← le type N'EST PLUS String : fin des "PLATANE"

    public Reclamation(int id, String description, String quartier) {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("La description est obligatoire");
        }
        this.id = id;
        this.description = description;
        this.quartier = quartier;
        this.statut = StatutReclamation.NOUVELLE;   // la constante de départ, typée
    }

    // Les transitions de la leçon 01, désormais avec des valeurs du type fermé
    public void demarrerTraitement() {
        if (statut != StatutReclamation.NOUVELLE) {   // != : pour un enum, comparaison d'identité, SÛRE
            throw new IllegalStateException(
                "Impossible de démarrer une réclamation " + statut);
        }
        this.statut = StatutReclamation.EN_COURS;
    }

    public void marquerResolue() {
        if (statut != StatutReclamation.EN_COURS) {
            throw new IllegalStateException(
                "Impossible de résoudre une réclamation " + statut);
        }
        this.statut = StatutReclamation.RESOLUE;
    }

    // La logique métier avec le switch exhaustif de la section 2.5
    public String getBadge() {
        return switch (statut) {
            case NOUVELLE -> "🆕 à traiter";
            case EN_COURS -> "⏳ en traitement";
            case RESOLUE  -> "✅ clôturée";
        };
    }

    public String getLigneAffichage() {
        return "#" + id + " [" + statut + "] " + description + " (" + quartier + ")";
    }

    public int getId()               { return id; }
    public String getDescription()   { return description; }
    public String getQuartier()      { return quartier; }
    public StatutReclamation getStatut() { return statut; }
}
```

> 📖 **Vocabulaire** : **`!=` sur un enum** = comparaison d'identité (même objet), sûre car chaque valeur est un singleton — pas besoin de `.equals()` (et impossible de se tromper de casse).

**Fichier 3 — le programme de démonstration** (`MainEnum.java`) :

```java
public class MainEnum {
    public static void main(String[] args) {

        // 1. Le cycle de vie, désormais avec un type fermé
        Reclamation r = new Reclamation(1, "Nid de poule", "Medina");
        System.out.println(r.getLigneAffichage() + " → " + r.getBadge());
        r.demarrerTraitement();
        r.marquerResolue();
        System.out.println(r.getLigneAffichage() + " → " + r.getBadge());

        // 2. values() : parcourir toutes les valeurs (utile pour un menu)
        for (StatutReclamation s : StatutReclamation.values()) {
            System.out.println(s.name() + " = « " + s.getLibelle() + " » (étape " + s.getOrdreTraitement() + ")");
        }

        // 3. valueOf() : du texte vers la valeur (déchiffre une donnée stockée)
        StatutReclamation lu = StatutReclamation.valueOf("EN_COURS");
        System.out.println("Relu depuis le stockage : " + lu.getLibelle());

        // 4. valueOf() avec un texte inconnu : l'exception prévue
        try {
            StatutReclamation.valueOf("PLATANE");
        } catch (IllegalArgumentException e) {
            System.out.println("Valeur inconnue : PLATANE (le type refuse)");
        }
    }
}
```

Compiler et exécuter :

```bash
javac StatutReclamation.java Reclamation.java MainEnum.java
java MainEnum
```

Sortie attendue :

```text
#1 [NOUVELLE] Nid de poule (Medina) → 🆕 à traiter
#1 [RESOLUE] Nid de poule (Medina) → ✅ clôturée
NOUVELLE = « Nouvelle demande » (étape 1)
EN_COURS = « En cours de traitement » (étape 2)
RESOLUE = « Réclamation résolue » (étape 3)
Relu depuis le stockage : En cours de traitement
Valeur inconnue : PLATANE (le type refuse)
```

---

## 4. Bonnes pratiques modernes (2025-2026)

1. **Un `enum` dès qu'un champ a un ensemble fini et stable de valeurs** : statuts, rôles, priorités, catégories. C'est le réflexe de base — ne reposez plus jamais sur des `String` libres pour ça.
2. **Stockez/partagez toujours `.name()`** : en base de données, dans un fichier, dans une API — jamais l'`ordinal()`.
3. **Donnez des libellés aux valeurs** : `NOUVELLE` pour le code, « Nouvelle demande » pour l'utilisateur — via les champs d'`enum`, pas en recopiant les textes ailleurs.
4. **Switch exhaustif sans `default`** sur les `enum` : le compilateur doit attraper tout nouveau cas non traité (leçon 04, même réflexe).
5. **Anticipez le combo enum + switch expression** : la combinaison est citée par la roadmap comme LA pratique 2025-2026 pour du code exhaustif et vérifié.
6. **Chemin vers Spring** : en partie 8, un `enum` se mappe vers la base avec `@Enumerated(EnumType.STRING)` — toujours le `name()`, jamais l'ordre.

---

## 5. Pièges à éviter

### Piège 1 — L'`ordinal()` comme identifiant stocké

```java
// ❌ FATAL (à terme) : la valeur numérique en base / API
int codeStocke = statut.ordinal();          // NOUVELLE=0, EN_COURS=1...
// ...on insère EN_PAUSE en 2e position un mois plus tard :
// le "1" historique en base désigne désormais EN_PAUSE. Silencieusement.

// ✅ BON : le nom, stable
String codeStocke = statut.name();          // "EN_COURS" reste "EN_COURS", quel que soit l'ordre
```

### Piège 2 — Recréer un `enum` à la main (« stringly typed »)

```java
// ❌ MAUVAIS : des String/int constants éparpillés (l'anti-pattern vu en partie 1)
public static final String STATUT_NOUVELLE = "NOUVELLE";
public static final String STATUT_EN_COURS = "EN_COURS";
public static final String STATUT_RESOLUE  = "RESOLUE";
// rien n'empêche d'écrire "nouvelle" ou "STATUT_EN_COURS " avec espace → bug silencieux

// ✅ BON : un type fermé, une seule source de vérité
public enum StatutReclamation { NOUVELLE, EN_COURS, RESOLUE }
```

### Piège 3 — Comparer avec `.equals()` ou la casse

```java
// ❌ FRAGILE : la comparaison de textes
if (statut.equals("EN_COURS")) { ... }   // casse si "en_cours", "EN_COURS "...

// ✅ BON : comparaison d'identité entre singletons — zéro erreur possible
if (statut == StatutReclamation.EN_COURS) { ... }
```

### Anti-pattern — le `enum` qui devient un service

```java
// ❌ MAUVAIS : de la grosse logique métier dans l'enum
public enum StatutReclamation {
    NOUVELLE, EN_COURS, RESOLUE;

    public void envoyerEmailAgent() { ... }   // appel réseau ? dans des CONSTANTES ?
}

// ✅ BON : l'enum reste une DONNÉE (libellés, ordre, propriétés simples) ;
//    les services (emails, sauvegardes) vivent dans des classes dédiées (partie 7)
```

---

## Checklist de validation

Avant de conclure la partie 2, vérifiez que vous savez :

- [ ] Écrire un `enum` simple et expliquer pourquoi il est plus sûr qu'un `String`.
- [ ] Donner à un `enum` des champs, un constructeur `private` et des méthodes.
- [ ] Utiliser `values()`, `valueOf()` et `name()` — et savoir quand chacun sert.
- [ ] Expliquer avec un scénario pourquoi l'`ordinal()` ne doit jamais être stocké.
- [ ] Écrire un switch exhaustif sur un `enum` (sans `default`).
- [ ] Remplacer un statut `String` par un `enum` dans du code existant (le fil rouge).

➡️ **Prochaine étape** : la partie 2 est close — vos objets sont solides (encapsulation), organisés en familles (héritage, polymorphisme), contractualisés (interfaces), modernes (records, sealed) et leurs ensembles de valeurs sont fermés (`enum`). La partie 3 attaque la suite logique : **plusieurs** objets — Collections (`List`, `Set`, `Map`), Generics et `Optional`.




# Leçon 01 — Encapsulation et constructeurs

> 🧭 **Pont depuis la partie 1** : à la fin de la partie 1 (leçon 07), SignalCUA vivait en console : un menu qui lisait des réclamations au clavier et les comptait par quartier. Mais un détail vous tendait les bras depuis la leçon 04 : les champs de `Reclamation` (`id`, `description`, `statut`…) sont **modifiables directement par n'importe quel code** — n'importe qui peut écrire `r.statut = "RESOLUE"` et fabriquer un mensonge. Cette leçon ouvre la **partie 2 (POO)** en corrigeant exactement ça : l'**encapsulation** — rendre les données intouchables de l'extérieur et ne laisser passer que des portes contrôlées.

---

## 1. Objectifs d'apprentissage

À la fin de cette leçon, vous saurez :

- Expliquer **pourquoi** un champ doit être `private` (et ce qui casse s'il ne l'est pas).
- Écrire des **getters** et des **setters** — et surtout savoir **quand ne PAS en écrire**.
- Valider les données dans un **setter** pour garantir que l'objet reste toujours cohérent.
- Enchaîner des **constructeurs surchargés** avec `this(...)`.
- Rendre un objet **immuable** (champs `final`, zéro setter) quand aucune mutation n'est nécessaire.
- Appliquer le tout à SignalCUA : une `Reclamation` que rien ne peut corrompre de l'extérieur.

---

## 2. Explication simple

### 2.1 Le problème : l'objet « troué »

**Pourquoi ?** Avec la classe `Reclamation` de la partie 1, ce code compile et s'exécute sans aucune protestation :

```java
Reclamation r = new Reclamation(1, "Nid de poule", "Medina");
r.statut = "PLATANE";     // 😱 aucun contrôle : ce statut n'existe pas !
r.description = null;     // 😱 on vient de vider la description
r.id = -7;                // 😱 un identifiant négatif
```

C'est comme une maison dont les murs sont en verre : tout le monde peut entrer et déplacer les meubles. Un objet est fiable seulement si **personne de l'extérieur ne peut le mettre dans un état absurde**.

**Comment ?** Java fournit le mot-clé **`private`** (un « modificateur d'accès » : un mot posé devant un champ ou une méthode pour dire QUI a le droit de l'utiliser) :

```java
public class Reclamation {
    private int id;             // accessible UNIQUEMENT depuis l'intérieur de la classe
    private String statut;
}
```

Désormais, `r.statut = "PLATANE"` écrit **ailleurs** ne compile plus. L'erreur apparaît **à la compilation**, donc avant même d'exécuter le programme — exactement la protection que la partie 1 vous a fait aimer (le compilateur qui attrape les erreurs).

**Quand ?** Toujours, par défaut. La règle des pros : tout champ commence `private`, et on n'en sort que si on a une raison explicite.

> 📖 **Vocabulaire** : **modificateur d'accès** = mot-clé (`private`, `public`, `protected`, ou rien) qui définit qui peut utiliser un champ ou une méthode d'une classe (un « membre »).

### 2.2 Getters et setters : le guichet de la pharmacie

**Pourquoi ?** Si tout est `private`, comment lire ou modifier les données de l'extérieur ? On crée des **portes officielles** : les **getters** (lire) et les **setters** (modifier).

**Analogie** : c'est le guichet d'une pharmacie. Le stock de médicaments (les champs) est en coulisses — vous n'y accédez pas directement. Vous passez par un pharmacien : il peut refuser (pas d'ordonnance ? pas de médicament). Le guichet = la porte contrôlée.

**Comment ?** Deux conventions universelles (que tous les IDE génèrent aussi) :

```java
// GETTER : renvoie la valeur. "get" + nom du champ, toujours public
public String getStatut() {
    return this.statut;
}

// SETTER : modifie la valeur. "set" + nom du champ, type de retour "void"
public void setStatut(String nouveauStatut) {
    // C'EST ICI qu'on met le garde-fou, sinon le guichet ne sert à rien :
    if (!nouveauStatut.equals("NOUVELLE")
            && !nouveauStatut.equals("EN_COURS")
            && !nouveauStatut.equals("RESOLUE")) {
        throw new IllegalArgumentException("Statut inconnu : " + nouveauStatut);
    }
    this.statut = nouveauStatut;
}
```

**Le point crucial** : un setter **sans validation** est une fausse sécurité — c'est un champ public déguisé. Toute la valeur de l'encapsulation tient dans ce `if`.

**Quand écrire un setter ?** La question à se poser à chaque champ, mot pour mot : *« quelqu'un a-t-il vraiment besoin de modifier ce champ après la création ? »*

- `id` : non (un identifiant ne change jamais) → **pas de setter**.
- `statut` : oui (la réclamation avance dans son traitement) → **un setter validé**, ou mieux (voir 2.4) une méthode métier comme `marquerResolue()`.
- `description` : discutable. En 2025-2026, si le besoin n'existe pas, pas de setter → champ `final`.

### 2.3 Les getters : lecture quasi libre, mais pas automatique

Un getter peut renvoyer **autre chose que la variable brute** : il est aussi l'endroit où centraliser un formatage :

```java
public String getLigneAffichage() {
    return "#" + id + " [" + statut + "] " + description;
}
```

Retenez surtout : un getter est presque toujours inoffensif (il ne change rien) ; c'est le **setter qui porte le risque**. Le réflexe à combattre : générer getters/setters pour **tous** les champs sans réfléchir. C'est précisément le piège n°1 cité par la roadmap.

### 2.4 Compléter la mécanique des constructeurs

La leçon 04 de la partie 1 a posé les bases (constructeur validé, surcharge, `this(...)`). Trois compléments pour finir la boîte à outils :

**a) Le constructeur par défaut qui disparaît.** Si vous n'écrivez AUCUN constructeur, Java en fabrique un gratuit, sans paramètres (le « constructeur par défaut »). Mais dès que vous en écrivez un, le gratuit **disparaît** : si du code appelait `new Reclamation()` alors que vous n'avez écrit que le constructeur complet, la compilation échoue. C'est voulu : ça empêche de créer un objet « vide » qui contournerait votre validation.

**b) `this(...)` doit être la PREMIÈRE instruction.** Le chaînage des constructeurs se résume à une règle stricte :

```java
Reclamation(int id, String description) {
    this(id, description, "Non précisé"); // ✅ toujours en 1ère ligne
    // ... ensuite seulement le code spécifique (s'il y en a)
}
```

**c) L'immutabilité par défaut.** Si un champ ne doit jamais changer après la création, déclarez-le `final` (mot-clé vu en leçon 01 de la partie 1 : la « boîte » ne pourra plus être re-remplie) et **n'écrivez pas de setter** :

```java
private final int id;         // assigné une fois dans le constructeur, plus jamais
private String statut;        // lui a le droit de changer (avec contrôle)
```

**Quand ?** C'est la grande recommandation 2025-2026 : **immutabilité par défaut**, mutabilité seulement quand un besoin explicite existe. Et dès la leçon 04, vous verrez un outil fait pour ça : le `record`.

> 📖 **Vocabulaire** : **immuable** (*immutable*) = qui ne peut plus être modifié après sa création. **Constructeur par défaut** = constructeur sans paramètres que Java crée automatiquement quand vous n'en écrivez aucun.

### 2.5 L'anti-modèle à connaître : le « Anemic Domain Model »

Un dernier terme qui reviendra sans cesse en Spring Boot (parties 7-8). Un **Anemic Domain Model** (modèle de domaine anémique — « anémique » = sans force) désigne une classe qui n'est qu'un sac de getters/setters, **sans aucune logique métier** — toute l'intelligence finit « en vrac » ailleurs (dans les services). L'encapsulation bien comprise, c'est aussi ça : mettre la logique qui concerne le statut **dans la classe** (ex. `estUrgente()`, `marquerResolue()`), pas dans le code qui l'utilise. On y revient en détail dans les pièges (section 5).

---

## 📖 Vocabulaire / Abréviations

| Terme | Définition en une ligne |
|---|---|
| **Encapsulation** | Protéger les données d'une classe derrière un accès contrôlé (`private` + portes officielles). |
| **`private`** | Modificateur d'accès : utilisable uniquement à l'intérieur de la classe. |
| **`public`** | Modificateur d'accès : utilisable depuis partout. |
| **Modificateur d'accès** | Mot-clé définissant qui a le droit d'utiliser un champ ou une méthode. |
| **Membre** | Champ ou méthode d'une classe. |
| **Getter** | Méthode publique qui lit un champ (convention : `getNomDuChamp`). |
| **Setter** | Méthode publique qui modifie un champ (convention : `setNomDuChamp`) — doit valider. |
| **Constructeur par défaut** | Constructeur sans paramètres créé par Java si vous n'en écrivez aucun. |
| **`final`** | Référence non re-modifiable : la valeur est assignée une seule fois. |
| **Immuabilité / immuable** | Objet qui ne peut pas être modifié après création. |
| **État** | L'ensemble des valeurs des champs d'un objet à un instant donné. |
| **Cohérence** | Fait que l'objet reste toujours dans une configuration valide (jamais « troué »). |
| **Anemic Domain Model** | Anti-modèle : classe réduite à des getters/setters, sans logique métier. |
| **POO** | *Programmation Orientée Objet* : paradigme où le programme s'organise en classes interagissant ensemble. |

---

## 3. Exemples concrets : la `Reclamation` blindée

Voici la classe complète, prête à copier (un fichier `Reclamation.java`), qui remplace la version de la partie 1 :

```java
// Fichier : Reclamation.java
public class Reclamation {

    // ----- CHAMPS : tous private, l'extérieur ne les touche jamais directement -----
    private final int id;              // final : un identifiant ne change JAMAIS
    private final String description;  // final : on ne réécrit pas un signalement
    private final String quartier;     // final : le lieu du problème est fixe
    private String statut;             // seul champ mutable : le traitement avance

    // ----- CONSTRUCTEUR COMPLET : la porte d'entrée contrôlée -----
    public Reclamation(int id, String description, String quartier) {
        if (description == null || description.isBlank()) {
            // "isBlank()" = vide ou seulement des espaces (vu en partie 1, leçon 03)
            throw new IllegalArgumentException("La description est obligatoire");
        }
        if (quartier == null || quartier.isBlank()) {
            throw new IllegalArgumentException("Le quartier est obligatoire");
        }
        if (id <= 0) {
            throw new IllegalArgumentException("L'id doit être positif");
        }
        this.id = id;
        this.description = description;
        this.quartier = quartier;
        this.statut = "NOUVELLE";      // état de départ, toujours le même
    }

    // ----- CONSTRUCTEUR SURCHARGÉ : délègue au complet avec this(...) -----
    public Reclamation(int id, String description) {
        this(id, description, "Non précisé"); // DOIT être la 1ère instruction
    }

    // ----- GETTERS : lecture libre (inoffensive) -----
    public int getId()              { return id; }
    public String getDescription()  { return description; }
    public String getQuartier()     { return quartier; }
    public String getStatut()       { return statut; }

    // ----- PAS DE SETTER pour id/description/quartier : ils sont final ! -----

    // ----- MÉTHODES MÉTIER au lieu d'un setter brut : la porte dit POURQUOI -----
    public void demarrerTraitement() {
        if (!statut.equals("NOUVELLE")) {
            throw new IllegalStateException(
                "Impossible de démarrer une réclamation " + statut);
        }
        this.statut = "EN_COURS";
    }

    public void marquerResolue() {
        if (!statut.equals("EN_COURS")) {
            // On refuse de résoudre une réclamation qui n'a jamais été traitée
            throw new IllegalStateException(
                "Impossible de résoudre une réclamation " + statut);
        }
        this.statut = "RESOLUE";
    }

    // ----- COMPORTEMENT : la logique vit AVEC les données (anti-anémique) -----
    public boolean estOuverte() {
        return statut.equals("NOUVELLE") || statut.equals("EN_COURS");
    }

    public String getLigneAffichage() {
        return "#" + id + " [" + statut + "] " + description + " (" + quartier + ")";
    }
}
```

> 📖 **Vocabulaire** : **`IllegalStateException`** = exception standard signalant que l'objet est dans un état qui interdit cette action (ici, résoudre une réclamation jamais traitée). Complète l'`IllegalArgumentException` de la partie 1.

Et son utilisation — chaque tentative de tricherie est bloquée, soit à la compilation, soit par une exception claire :

```java
// Fichier : MainEncapsulation.java
public class MainEncapsulation {
    public static void main(String[] args) {
        Reclamation r = new Reclamation(1, "Nid de poule", "Medina");
        System.out.println(r.getLigneAffichage()); // #1 [NOUVELLE] Nid de poule (Medina)

        // r.id = -7;               ← ne compile plus : "id has private access" ✅
        // r.statut = "PLATANE";    ← ne compile plus ✅
        // r.setDescription("..."); ← n'existe pas : la description est immuable ✅

        r.demarrerTraitement();     // NOUVELLE → EN_COURS (transition contrôlée)
        r.marquerResolue();         // EN_COURS → RESOLUE (transition contrôlée)
        System.out.println(r.getStatut()); // RESOLUE

        try {
            r.demarrerTraitement(); // refusée : on ne repart pas d'une réclamation résolue
        } catch (IllegalStateException e) {
            System.out.println("Refus : " + e.getMessage());
        }
    }
}
```

Compiler et exécuter (même routine que la partie 1) :

```bash
javac Reclamation.java MainEncapsulation.java   # compile les deux fichiers
java MainEncapsulation                          # exécute le point d'entrée (le main)
```

---

## 4. Bonnes pratiques modernes (2025-2026)

1. **`private` par défaut, `final` par défaut** : partez du principe qu'un champ n'est ni visible ni modifiable, puis ouvrez des portes **quand un besoin prouvé apparaît** — jamais « au cas où ».
2. **Pas de setter quand une méthode métier suffit** : `marquerResolue()` vaut mieux que `setStatut("RESOLUE")`, car elle dit *pourquoi* on change l'état et peut refuser les transitions impossibles.
3. **Un setter sans validation est un bug en puissance** : si vous écrivez un setter, il valide. Sinon, supprimez-le.
4. **La logique vit dans la classe** : `estOuverte()` dans `Reclamation`, pas un `if` recopié douze fois dans le code appelant. C'est le vaccin contre le *Anemic Domain Model*.
5. **Zéro effet de bord dans le constructeur** : il construit et valide, il ne sauvegarde pas en base ni n'envoie d'email (détaillé en piège 3).
6. Anticipez : en partie 8, les **entités JPA** (objets mappés vers une base de données) ont leurs propres contraintes — mais l'esprit reste identique : un objet qui se protège lui-même.

---

## 5. Pièges à éviter

### Piège 1 — Les getters/setters générés en rafale « au cas où »

```java
// ❌ MAUVAIS : réflexe d'IDE — 4 champs, 8 méthodes, aucune intention
private int id;
public void setId(int id) { this.id = id; }     // pourquoi autoriser un id à changer ?

// ✅ BON : question posée champ par champ — "qui modifie ça après création ?"
private final int id;                            // personne → pas de setter, final
public int getId() { return id; }                // la lecture, oui
```

*Pourquoi c'est dangereux* : chaque setter est une porte ouverte à un état incohérent — vous offrez au futur bug un accès dont il n'avait peut-être même pas besoin.

### Piège 2 — Le setter qui ne valide rien

```java
// ❌ MAUVAIS : champ public déguisé — n'importe quelle valeur passe
public void setStatut(String statut) { this.statut = statut; }
// Appelant : r.setStatut("PLATANE") → objet corrompu, sans message d'erreur

// ✅ BON : le guichet refuse les ordonnances fantaisistes
public void setStatut(String statut) {
    if (!statut.equals("NOUVELLE") && !statut.equals("EN_COURS")
            && !statut.equals("RESOLUE")) {
        throw new IllegalArgumentException("Statut inconnu : " + statut);
    }
    this.statut = statut;
}
```

### Piège 3 — Le constructeur qui fait trop

```java
// ❌ MAUVAIS : pourquoi naître déclencherait-il un email ?
public Reclamation(...) {
    envoyerEmailAgent();
    sauvegarderEnBase();
}

// ✅ BON : le constructeur construit. Les autres actions sont des méthodes
//    explicites appelées quand ON décide : enregistrer(), notifier().
```

### Anti-pattern — le Anemic Domain Model

```java
// ❌ MAUVAIS : la classe ne sait rien faire, toute la logique est dehors
class Reclamation { int id; String statut; /* + 6 getters/setters */ }
// ...et dans le code appelant, recopié partout :
if (r.getStatut().equals("NOUVELLE") || r.getStatut().equals("EN_COURS")) { ... }

// ✅ BON : la règle métier vit AVEC les données qu'elle concerne
public boolean estOuverte() {
    return statut.equals("NOUVELLE") || statut.equals("EN_COURS");
}
```

*Pourquoi c'est grave* : si la règle change (un nouveau statut « EN_PAUSE »), il faut retrouver et corriger les douze copies dispersées du `if` — et on en oubliera une.

---

## Checklist de validation

Avant de passer à la leçon 02, vérifiez que vous savez :

- [ ] Expliquer l'encapsulation avec vos mots (et l'analogie du guichet).
- [ ] Rendre les champs `private` et justifier `final` pour l'immutabilité.
- [ ] Écrire un getter et un setter **validé** — et savoir quand supprimer le setter.
- [ ] Chaîner deux constructeurs avec `this(...)` (et rappeler qu'il doit être en 1ère ligne).
- [ ] Remplacer un setter brut par une méthode métier à transitions contrôlées.
- [ ] Définir le *Anemic Domain Model* et expliquer pourquoi on l'évite.
- [ ] Avoir fait passer la classe `Reclamation` de la partie 1 à la version blindée de cette leçon.

➡️ **Prochaine étape** : notre `Reclamation` est désormais une forteresse individuelle. Mais SignalCUA reçoit des réclamations de **types différents** (voirie, propreté, éclairage) qui partagent l'essentiel et se distinguent par leur traitement. Recopier trois fois la classe serait absurde : la leçon 02 introduit l'**héritage et le polymorphisme** — construire une famille de classes à partir d'une mère commune.



# Leçon 05 — Variables de classe (`static`) et constantes

> 🧭 **Pont depuis la leçon 04** : votre classe `Reclamation` fonctionne, mais deux mystères subsistent. D'abord, pourquoi `main` (et le `diagnostic()` de la leçon 02) porte un mot-clé `static` jamais expliqué ? Ensuite, comment compter « combien de réclamations ont été créées au total » — une information qui appartient à la **classe entière**, pas à un objet particulier ? Cette leçon répond aux deux : `static` = « qui appartient à la classe, pas aux objets ».

---

## 1. Objectifs d'apprentissage

À la fin de cette leçon, vous saurez :

- Distinguer clairement variable d'**instance** (propre à chaque objet) et variable de **classe** (`static`, partagée).
- Utiliser `static final` pour déclarer une **constante** correctement nommée.
- Écrire et appeler une méthode `static`, et savoir quand c'est justifié (ou pas).
- Comprendre le bloc `static { ... }` et quand il s'exécute.
- Éviter le piège majeur : l'état global mutable.

---

## 2. Explication simple

### 2.1 Champ d'instance vs champ de classe

**Pourquoi ?** Reprenons l'analogie du plan et des maisons (leçon 04). Chaque maison bâtie a SA couleur (champ d'instance : chaque objet a sa copie). Mais le plan lui-même peut porter une information commune à toutes : par exemple le nom de l'architecte, unique, attaché au PLAN et non aux maisons.

**Comment ?**

```java
public class Reclamation {
    // Champ d'INSTANCE : chaque objet a le sien
    int id;
    String quartier;

    // Champ de CLASSE (static) : UNE seule copie, partagée par tous
    static int compteur = 0; // combien de réclamations créées au total ?
}
```

```java
Reclamation a = new Reclamation(); // (constructeur simplifié pour l'exemple)
Reclamation b = new Reclamation();
a.id = 1;          // modifie l'instance a SEULEMENT, b.id reste inchangé
Reclamation.compteur = 42;  // modifie LE compteur unique, vu par toutes les instances
```

**Analogie** : le champ d'instance est comme votre numéro de siège dans un bus (chacun le sien) ; le champ de classe est le numéro de la ligne de bus (un seul, partagé par tous les passagers).

**Accès** : par convention on accède à un `static` via le **nom de la classe** (`Reclamation.compteur`), pas via un objet (`a.compteur` — ça compile mais c'est trompeur et découragé).

### 2.2 `static final` : la constante

**Pourquoi ?** Certaines valeurs ne changeront JAMAIS (une limite, un taux). Les figer dans une constante nommée évite les nombres magiques (`5` perdu au milieu du code) et les erreurs de frappe.

**Comment ?** `static` (une seule copie) + `final` (non réassignable, leçon 01) :

```java
public class Reclamation {
    public static final int MAX_URGENCE = 5;
    public static final String STATUT_INITIAL = "NOUVELLE";

    // Usage : Reclamation.MAX_URGENCE — lisible partout, identique partout
}
```

Convention : constantes en `MAJUSCULES_AVEC_UNDERSCORES` (leçon 01).

### 2.3 Les méthodes `static`

**Pourquoi ?** Certaines méthodes n'ont besoin d'AUCUN objet pour travailler : `Math.max(3, 7)` ne demande pas de construire un `Math` ! Une méthode qui ne touche à aucun champ d'instance peut vivre « sur la classe ».

**Comment ?**

```java
public class Reclamation {
    static int compteur = 0;

    static int totalCreees() {
        return compteur; // OK : compteur est static, pas besoin d'instance
    }

    // ❌ IMPOSSIBLE :
    // static boolean estUrgente() { return statut.equals(...); }
    // "statut" est un champ d'instance : DE QUELLE réclamation parle-t-on ?
    // Sans objet, la question n'a pas de sens.
}
```

```java
// Appel : Classe.methode(), pas d'objet nécessaire
System.out.println(Reclamation.totalCreees()); // 0 pour l'instant
```

**Mystère `main` résolu** : la machine virtuelle Java doit démarrer votre programme AVANT qu'aucun objet n'existe. Elle appelle donc `main` sur la **classe** : `static void main(String[] args)`. C'est pourquoi vous ne pouvez pas appeler directement une méthode d'instance (non `static`) depuis `main` sans créer d'objet d'abord — et pourquoi notre `diagnostic()` de la leçon 02 était `static` : il ne dépendait d'aucun objet.

### 2.4 Le bloc d'initialisation statique

**Pourquoi ?** Parfois un `static` a besoin de PLUSIEURS lignes pour être préparé (ex. remplir une table de correspondance). Le bloc `static { ... }` s'exécute **une seule fois**, au chargement de la classe en mémoire, avant toute création d'objet.

**Comment ?**

```java
public class Reclamation {
    static final String[] STATUTS_VALIDES = new String[3];

    static {
        // Exécuté UNE fois, quand la classe est chargée
        STATUTS_VALIDES[0] = "NOUVELLE";
        STATUTS_VALIDES[1] = "EN_COURS";
        STATUTS_VALIDES[2] = "RESOLUE";
    }
}
```

**Quand ?** Rarement nécessaire. Retenez surtout quand ça s'exécute : une seule fois, au chargement de la classe — utile à reconnaître dans du code existant.

### 2.5 Le piège majeur : l'état global mutable

**Pourquoi ?** Un `static` **non final** (= modifiable) est une variable globale que TOUT le programme peut changer. Environnement multi-thread (partie 10), tests d'unité qui se polluent entre eux, bugs impossibles à reproduire : c'est une source classique de cauchemars. La roadmap le dit clairement : **réservez `static` aux vraies constantes et aux méthodes utilitaires pures** (sans état), pas au partage d'état applicatif.

**Comment (le bon usage pour notre compteur) ?** Un compteur de classe est pédagogiquement parfait et sans danger en mono-thread ; gardez juste en tête qu'en vrai projet, un compteur global serait suspect — et qu'en Spring Boot (partie 7), un bean `@Component` fait ce travail proprement, sans `static`.

---

## 3. Exemples concrets

Faites évoluer votre classe `Reclamation` de la leçon 04 :

```java
// Fichier : Reclamation.java
public class Reclamation {
    // ----- Constantes de classe (static final) -----
    public static final int MAX_URGENCE = 5;
    public static final String STATUT_INITIAL = "NOUVELLE";

    // ----- Variable de classe : compteur partagé -----
    static int compteurCreees = 0;

    // ----- Champs d'instance -----
    int id;
    String description;
    String quartier;
    String statut;

    Reclamation(int id, String description, String quartier) {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("La description est obligatoire");
        }
        this.id = id;
        this.description = description;
        this.quartier = quartier;
        this.statut = STATUT_INITIAL;   // on utilise la constante, pas un nombre magique
        compteurCreees++;               // à chaque "new", LE compteur unique s'incrémente
    }

    Reclamation(int id, String description) {
        this(id, description, "Non précisé");
    }

    boolean estUrgente() { return this.statut.equals("NOUVELLE"); }

    void afficher() {
        System.out.println("#" + id + " [" + statut + "] " + description + " (" + quartier + ")");
    }

    // ----- Méthode de classe : appartient à la classe, pas aux objets -----
    static int totalCreees() {
        return compteurCreees;
    }
}
```

```java
// Fichier : MainSignal.java
public class MainSignal {
    public static void main(String[] args) {
        System.out.println("Avant creation : " + Reclamation.totalCreees()); // 0

        Reclamation r1 = new Reclamation(1, "Nid de poule", "Medina");
        Reclamation r2 = new Reclamation(2, "Lampadaire HS", "Plateau");
        r1.afficher();
        r2.afficher();

        System.out.println("Apres creation : " + Reclamation.totalCreees()); // 2
        System.out.println("Limite d'urgence : " + Reclamation.MAX_URGENCE); // 5
    }
}
```

---

## 4. Bonnes pratiques modernes (2025-2026)

- **Constantes `static final` nommées** partout où une valeur « magique » apparaîtrait (`MAX_URGENCE`, `TAUX_TVA`…).
- **Méthodes `static` uniquement pour du pur** : le résultat ne dépend que des paramètres, aucun champ d'instance touché, aucun état modifié (`Math.max` est le modèle canonique du JDK).
- **Accès par le nom de classe** : `Reclamation.MAX_URGENCE`, jamais `instance.MAX_URGENCE`.
- **Pas d'état global mutable** : un `static` modifiable est un bug en attente (tests pollués, multi-thread). Si vous en sentez le besoin, c'est souvent qu'un concept métier attend sa propre classe — ou, plus tard, un bean Spring (partie 7).
- **Pas de « singleton maison »** avec des champs `static` partout pour simuler un état global : en Spring Boot, un bean `@Component` (partie 7) fait ce travail proprement et testable.

---

## 5. Pièges à éviter

### Piège 1 — Confondre les deux familles
```java
// ❌ MAUVAIS : croire que chaque objet a son compteur
static int compteur = 0; // en réalité : UN SEUL pour toute la classe !
// ...ou l'inverse, croire qu'un champ d'instance est partagé :
int id = 0; // chaque objet a le SIEN ; le modifier sur a ne touche pas b

// ✅ BON : la question à poser — « cette info appartient-elle à UN objet
//    (d'instance) ou à TOUTE la classe (static) ? »
```

### Piège 2 — Accéder à un static via une instance
```java
// ❌ MAUVAIS : trompeur — on croit que ça dépend de r1, mais non
r1.totalCreees();

// ✅ BON : la lecture montre bien que c'est au niveau classe
Reclamation.totalCreees();
```

### Piège 3 — La méthode static qui veut toucher un champ d'instance
```java
// ❌ NE COMPILE PAS : de quelle réclamation parle-t-on ?
static boolean estUrgente() {
    return statut.equals("NOUVELLE"); // statut appartient aux OBJETS
}

// ✅ BON : soit la méthode n'est pas static (elle parle d'UN objet),
// soit elle reçoit tout ce qu'il lui faut en paramètre :
static boolean statutEstUrgent(String statut) {
    return statut.equals("NOUVELLE");
}
```

### Anti-pattern — l'état global mutable
```java
// ❌ MAUVAIS : tout le programme peut écrire là ; les tests se polluent entre eux
public static String statutActuel = "NOUVELLE";
public static ArrayList<Reclamation> TOUTES = new ArrayList<>(); // état global !

// ✅ BON : l'état vit dans un objet dédié qu'on passe explicitement
class RegistreReclamations { // (deviendra un bean Spring en partie 7)
    private ArrayList<Reclamation> contenu = new ArrayList<>();
    void ajouter(Reclamation r) { this.contenu.add(r); }
}
```

---

## 📖 Vocabulaire / Abréviations

| Terme | Définition en une ligne |
|---|---|
| **Variable de classe (`static`)** | Champ unique partagé par toutes les instances, attaché à la classe. |
| **Variable d'instance** | Champ dont chaque objet possède sa propre copie. |
| **Constante (`static final`)** | Valeur de classe non réassignable, nommée en `MAJUSCULES`. |
| **Nombre magique** | Valeur brute (ex. `5`) perdue dans le code sans explication — remplacer par une constante. |
| **Méthode de classe (`static`)** | Méthode appelable sans objet (`Classe.methode()`), sans accès aux champs d'instance. |
| **Méthode utilitaire pure** | Méthode `static` dont le résultat ne dépend que des paramètres reçus. |
| **Bloc `static`** | Bloc `static { ... }` exécuté une seule fois, au chargement de la classe. |
| **Chargement de classe** | Moment où la JVM (la machine virtuelle Java) lit la classe en mémoire pour la première fois. |
| **JVM** | *Java Virtual Machine* : le programme qui exécute votre code Java compilé. |
| **État global mutable** | Variable `static` modifiable visible de partout : source classique de bugs. |
| **Singleton maison** | Anti-pattern : classe qui simule un état global via des champs `static`. |
| **Bean `@Component`** | En Spring Boot (partie 7) : objet géré par le framework, remplaçant propre des singletons maison. |

## Checklist de validation

Avant de passer à la leçon 06, vérifiez que vous savez :

- [ ] Expliquer la différence champ d'instance / champ de classe (siège vs numéro de ligne de bus).
- [ ] Déclarer et utiliser une constante `static final` bien nommée.
- [ ] Écrire une méthode `static` et expliquer pourquoi `main` l'est.
- [ ] Expliquer pourquoi une méthode `static` ne peut pas accéder aux champs d'instance.
- [ ] Savoir quand le bloc `static { ... }` s'exécute.
- [ ] Expliquer pourquoi l'état global mutable est dangereux.
- [ ] Ajouter un compteur de classe à `Reclamation` (nombre total créées).

➡️ **Prochaine étape** : votre projet compte maintenant plusieurs classes (`Reclamation`, `MainSignal`…). Quand il en comptera des dizaines, il faudra des tiroirs : les **packages**, l'organisation du code — la leçon 06.




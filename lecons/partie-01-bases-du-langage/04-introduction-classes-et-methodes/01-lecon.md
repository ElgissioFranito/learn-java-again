# Leçon 04 — Introduction aux classes et méthodes

> 🧭 **Pont depuis la leçon 03** : vous avez terminé l'exercice en éclatant des chaînes par index (`morceaux[0]`, `morceaux[1]`…) — ça marche, mais c'est fragile et illisible. Et depuis la leçon 02, le « tableau parallèle » vous faisait tenir des données liées à bout de bras. Il est temps d'introduire LA notion centrale de Java : la **classe**, qui regroupe des données (les champs) et des comportements (les méthodes) sous un seul nom. Cette leçon lance l'**étape 1 du fil rouge SignalCUA** : la classe `Reclamation`.

---

## 1. Objectifs d'apprentissage

À la fin de cette leçon, vous saurez :

- Déclarer une classe avec ses **champs**, un **constructeur** et des **méthodes**.
- Créer des objets avec `new` et comprendre la différence classe / objet.
- Utiliser `this` pour distinguer le champ du paramètre.
- **Surcharger** une méthode (plusieurs signatures) et comprendre pourquoi le compilateur choisit la bonne.
- Valider les paramètres d'un constructeur (et savoir pourquoi c'est vital).
- Implémenter l'**étape 1 du fil rouge SignalCUA** : créer des `Reclamation` et les parcourir.

---

## 2. Explication simple

### 2.1 Qu'est-ce qu'une classe ?

**Pourquoi ?** Une réclamation, ce n'est pas « un quartier » ni « un délai » : c'est **un tout** — un identifiant, une description, un quartier, un statut, qui se déplacent et se traitent ensemble. Envelopper ce tout dans une seule « chose » nommée évite les données éparpillées (la douleur des leçons 02 et 03).

**Comment ?** Une classe est un **plan de construction** (comme le plan d'une maison), et un **objet** est une maison concrète bâtie à partir de ce plan. Du même plan, on peut bâtit autant de maisons que voulu :

```java
public class Reclamation {
    // ----- LES CHAMPS : les données que chaque réclamation porte -----
    int id;
    String description;
    String quartier;
    String statut; // String pour l'instant ; deviendra un enum en partie 2
}
```

```java
// Ailleurs, on BÂTIT un objet avec "new" :
Reclamation rec = new Reclamation(); // rec est UN objet réclamation parmi tant d'autres
rec.id = 1;
rec.description = "Nid de poule";
```

**Quand ?** Dès que des données vont ensemble, elles méritent une classe. C'est le réflexe de base de Java (tout le langage tourne autour) : dès la partie 7, Spring Boot n'est au fond qu'un gestionnaire sophistiqué de classes.

> 📖 **Vocabulaire** : **champ** (*field*) = variable qui vit à l'intérieur d'un objet. **Instancier** = créer un objet à partir de la classe (avec `new`). Un objet est aussi appelé une **instance** de la classe.

### 2.2 Le constructeur : la porte d'entrée contrôlée

**Pourquoi ?** Avec le code ci-dessus, rien n'empêche de créer une réclamation SANS description ni quartier — un objet « troué ». Le **constructeur** est une méthode spéciale qui s'exécute obligatoirement à la création : on en profite pour exiger toutes les données et les vérifier.

**Comment ?** Il porte le nom de la classe, **sans type de retour** :

```java
public class Reclamation {
    int id;
    String description;
    String quartier;
    String statut;

    // ----- CONSTRUCTEUR : appelé par "new" -----
    Reclamation(int id, String description, String quartier) {
        // Validation d'abord (early return des leçons précédentes, version constructeur) :
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("La description est obligatoire");
        }
        // "this.description" = le CHAMP ; "description" seul = le PARAMÈTRE
        this.id = id;
        this.description = description;
        this.quartier = quartier;
        this.statut = "NOUVELLE"; // statut de départ, toujours le même
    }
}
```

Deux nouveautés à démystifier :

- **`this`** = « l'objet en cours de construction ». Dans `this.description = description`, les deux mots se ressemblent mais désignent deux choses : à gauche le champ de l'objet, à droite le paramètre reçu. Sans `this`, Java assignerait le paramètre… à lui-même (bug silencieux).
- **`throw new IllegalArgumentException(...)`** : « refuse de construire l'objet et plante avec ce message ». C'est la façon standard de signaler une donnée invalide dès la création — mieux vaut un crash clair au moment de la faute qu'un objet incohérent qui explosera plus loin, sans dire pourquoi. (Les exceptions détaillées arrivent en partie 4 ; ici, retenez le réflexe.)

### 2.3 Les méthodes : les comportements

**Pourquoi ?** Une réclamation ne devrait pas seulement « contenir » des données : elle devrait **savoir faire des choses** (savoir si elle est urgente, se décrire). Répartir ces savoir-faire dans les classes rend le code lisible et évite de recopier la même logique partout.

**Comment ?** Une méthode a la forme : `typeDeRetour nom(paramètres) { corps }` :

```java
// Dans la classe Reclamation :

// "boolean" = la méthode RENVOIE un vrai/faux
boolean estUrgente() {
    return this.statut.equals("NOUVELLE");
}

// "void" = la méthode ne renvoie rien, elle fait juste une action
void afficher() {
    System.out.println("#%d [%s] %s (%s)".formatted(id, statut, description, quartier));
    // Dans une méthode d'instance, "id" signifie "this.id" : le champ de CET objet
}
```

> 💡 **Nom de la classe** : la roadmap insiste — donnez des noms métier (`Reclamation`, `Agent`, `Quartier`), jamais `Data`, `Item` ou `Object1`. C'est la base d'un code lisible… pour vous-même dans 6 mois.

### 2.4 La surcharge (overloading)

**Pourquoi ?** Parfois on veut créer une réclamation avec ou sans quartier fourni. Plutôt que deux noms différents (`creerAvecQuartier`, `creerSansQuartier`), on garde UN nom avec plusieurs « signatures » : c'est la **surcharge**.

**Comment ?** Même nom, paramètres différents (nombre ou types) — le compilateur choisit en regardant les arguments que vous passez :

```java
Reclamation(int id, String description, String quartier) {
    // ... le constructeur complet (ci-dessus)
}

// Surcharge : on délègue au constructeur complet avec une valeur par défaut
Reclamation(int id, String description) {
    this(id, description, "Non precisé"); // appelle l'autre constructeur
}

// Méthode surchargée : même nom, types différents
void afficher() { /* fiche détaillée */ }
void afficher(boolean compact) { /* fiche courte si compact vaut true */ }
```

**Piège à connaître :** deux surcharges ne peuvent se distinguer QUE par les types/nombres de paramètres, jamais par le type de retour seul. Et attention : « surcharge » (overloading, même nom, paramètres différents) est différente de la « redéfinition » (overriding, réécrire une méthode héritée — partie 2).

### 2.5 Le cycle de vie d'un objet : pas de `delete` !

**Pourquoi ?** Vous venez peut-être de C/C++ où il faut détruire la mémoire à la main. En Java, non : quand plus aucun morceau du programme ne référence un objet (plus aucun « reçu » vers le « coffre », rappel leçon 01), la **garbage collection** (ramassage des ordures) le supprime automatiquement. Vous créez avec `new` ; Java nettoie seul.

**Conséquence pratique** : ne gardez pas des références inutiles (ex. un objet stocké dans une liste « pour rien ») — un objet référencé ne sera JAMAIS ramassé, même si vous ne l'utilisez plus.

---

## 3. Exemples concrets : l'Étape 1 du fil rouge SignalCUA

Voici le programme complet demandé par la roadmap à cette étape (deux fichiers dans le même dossier) :

```java
// Fichier : Reclamation.java
public class Reclamation {
    // Champs (pour l'instant accessibles directement ; l'encapsulation arrive en partie 2)
    int id;
    String description;
    String quartier;
    String statut;

    // Constructeur complet
    Reclamation(int id, String description, String quartier) {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("La description est obligatoire");
        }
        this.id = id;
        this.description = description;
        this.quartier = quartier;
        this.statut = "NOUVELLE";
    }

    // Surcharge : quartier non précisé
    Reclamation(int id, String description) {
        this(id, description, "Non précisé");
    }

    // Comportements
    boolean estUrgente() {
        return this.statut.equals("NOUVELLE");
    }

    void afficher() {
        System.out.println("#" + id + " [" + statut + "] " + description + " (" + quartier + ")");
    }
}
```

```java
// Fichier : MainSignal.java
import java.util.ArrayList;

public class MainSignal {
    public static void main(String[] args) {
        // 1. Créer quelques réclamations "à la main"
        Reclamation r1 = new Reclamation(1, "Nid de poule", "Medina");
        Reclamation r2 = new Reclamation(2, "Lampadaire HS", "Plateau");
        Reclamation r3 = new Reclamation(3, "Tas d'ordures"); // surcharge : sans quartier

        // 2. Les ranger dans une liste (pas de tableau parallèle : chaque
        //    réclamation porte TOUTES ses données, impossible de désynchroniser)
        ArrayList<Reclamation> reclamations = new ArrayList<>();
        reclamations.add(r1);
        reclamations.add(r2);
        reclamations.add(r3);

        // 3. Les afficher avec une boucle for-each
        for (Reclamation r : reclamations) {
            r.afficher();
        }

        // 4. Compter par quartier, avec StringBuilder
        StringBuilder sb = new StringBuilder();
        sb.append("Reclamations par quartier\n");
        for (Reclamation r : reclamations) {
            sb.append("- ").append(r.quartier).append("\n");
        }
        System.out.println(sb);
    }
}
```

Résultat attendu :

```text
#1 [NOUVELLE] Nid de poule (Medina)
#2 [NOUVELLE] Lampadaire HS (Plateau)
#3 [NOUVELLE] Tas d'ordures (Non précisé)
Reclamations par quartier
- Medina
- Plateau
- Non précisé
```

---

## 4. Bonnes pratiques modernes (2025-2026)

- **Validez dans le constructeur** : refusez immédiatement les données absurdes (`null`, chaînes vides avec `.isBlank()`, `id` négatif). Un objet naît toujours **cohérent**.
- **Constructeurs simples** : pas d'appel réseau, pas de calculs lourds dedans — juste affecter et vérifier.
- **Noms métier** : `Reclamation`, `Agent`, `Quartier` — jamais `Data`, `Item`, `Helper`.
- **Faites savoir aux objets** : préférez `r.estUrgente()` (l'objet connaît sa logique) à un `if (r.statut.equals("NOUVELLE"))` écrit ailleurs et recopié 10 fois.
- **Immutabilité quand possible** : des champs `final` non modifiables après création simplifient tout (approfondi en partie 2). Pour l'instant, gardez juste le réflexe de vous demander « ce champ doit-il vraiment changer ? ».
- **Les records (Java 16+)** : pour les classes qui ne sont qu'un paquet de données (pas de logique), un `record` remplace avantageusement la classe classique — aperçu en partie 2, leçon 04. D'ici là, la classe classique vous apprend les fondations.
- **Évitez les classes « boîte à outils » statiques** (`Utils`, `Helper` fourre-tout) : c'est souvent le symptôme d'un concept métier qui mériterait sa propre classe.

---

## 5. Pièges à éviter

### Piège 1 — Le constructeur qui ne valide rien
```java
// ❌ MAUVAIS : on peut créer une réclamation "trouée"
Reclamation(int id, String description) {
    this.id = id;                 // id négatif accepté !
    this.description = description; // null accepté !
}

// ✅ BON : l'objet naît cohérent ou ne naît pas
Reclamation(int id, String description) {
    if (id <= 0) {
        throw new IllegalArgumentException("id doit être positif");
    }
    if (description == null || description.isBlank()) {
        throw new IllegalArgumentException("description obligatoire");
    }
    this.id = id;
    this.description = description;
}
```

### Piège 2 — Oublier `this` quand champ et paramètre portent le même nom
```java
// ❌ MAUVAIS : assigne le paramètre à LUI-MÊME, le champ reste vide (bug silencieux)
Reclamation(String quartier) {
    quartier = quartier;
}

// ✅ BON : this.xxx = le champ de l'objet en cours
Reclamation(String quartier) {
    this.quartier = quartier;
}
```

### Piège 3 — Le constructeur qui fait trop
```java
// ❌ MAUVAIS : validation complexe + sauvegarde en base + envoi d'email
Reclamation(...) {
    envoyerEmailAgent();      // pourquoi naître déclencherait un email ?
    sauvegarderEnBase();      // un constructeur ne devrait pas avoir d'effets de bord
}

// ✅ BON : le constructeur construit. Les autres actions sont des méthodes
//    explicites appelées quand ON décide (sauvegarder(), notifier()).
```

### Anti-pattern — la classe « sac de données » sans comportement
```java
// ❌ MOYEN : toute la logique est dehors, la classe ne sait rien faire
class Reclamation { int id; String statut; /* ... */ }
// ...et ailleurs, partout dans le code :
if (r.statut.equals("NOUVELLE") || r.statut.equals("EN_COURS")) { ... } // recopié 12 fois

// ✅ BON : la logique vit AVEC les données qu'elle concerne
class Reclamation {
    boolean estEnCoursDeTraitement() {
        return statut.equals("NOUVELLE") || statut.equals("EN_COURS");
    }
}
```

---

## 📖 Vocabulaire / Abréviations

| Terme | Définition en une ligne |
|---|---|
| **Classe** | Plan de construction qui regroupe champs et méthodes. |
| **Objet / instance** | Exemplaire concret bâti à partir de la classe avec `new`. |
| **Instancier** | Créer une instance (`new Reclamation(...)`). |
| **Champ (field)** | Variable qui vit à l'intérieur d'un objet. |
| **Méthode** | Comportement d'une classe : bloc de code appelable, avec ou sans valeur de retour. |
| **Constructeur** | Méthode spéciale (nom = classe, pas de type de retour) exécutée à chaque `new`. |
| **`this`** | Référence à l'objet en cours ; distingue le champ du paramètre homonyme. |
| **`this(...)`** | Appel d'un autre constructeur de la même classe (pour la surcharge). |
| **Surcharge (overloading)** | Plusieurs méthodes de même nom avec des paramètres différents. |
| **Signature** | Nom d'une méthode + ses types de paramètres (ce qui permet au compilateur de la choisir). |
| **`void`** | Type de retour « rien » : la méthode exécute une action sans renvoyer de valeur. |
| **`return`** | Quitte la méthode en renvoyant (ou non) une valeur. |
| **`IllegalArgumentException`** | Exception standard pour signaler qu'un paramètre reçu est invalide. |
| **Garbage collection** | Nettoyage automatique de la mémoire : les objets sans référence sont supprimés. |
| **POO** | *Programmation Orientée Objet* : paradigme où le programme s'organise en classes interagissant ensemble (partie 2 entière). |

## Checklist de validation

Avant de passer à la leçon 05, vérifiez que vous savez :

- [ ] Expliquer la différence entre classe (le plan) et objet (la maison).
- [ ] Écrire une classe avec champs, constructeur validé et méthodes.
- [ ] Créer des objets avec `new` et comprendre ce que fait `this`.
- [ ] Surcharger un constructeur et déléguer avec `this(...)`.
- [ ] Expliquer pourquoi on valide les paramètres du constructeur.
- [ ] Prédire ce que fait le garbage collection (et pourquoi on ne « delete » jamais).
- [ ] Avoir construit la classe `Reclamation` de l'étape 1 du fil rouge.

➡️ **Prochaine étape** : notre classe `Reclamation` vit dans un fichier sans adresse. Quand le projet grandira (des dizaines de classes !), il faudra les organiser : c'est le rôle des **packages**, sujet de la leçon 06. Mais avant : un mot-clé est apparu sans explication depuis la leçon 02 (`static` devant `main` et `diagnostic`) — la leçon 05 l'explique enfin.




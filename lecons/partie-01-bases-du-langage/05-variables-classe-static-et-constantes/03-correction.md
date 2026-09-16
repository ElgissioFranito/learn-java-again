# Correction 05 — Le compteur du registre SignalCUA

> 🧭 **Articulation** : solution pas à pas de `02-exercice.md`. Le point clé est l'étape 2 (où incrémenter le compteur) et l'étape 3 (la preuve de l'unicité). Checklist finale reprise en bas.

## Étape 1 — Les constantes

```java
public class Reclamation {
    // Constantes de classe : une seule copie, non réassignables
    public static final int MAX_URGENCE = 5;
    public static final String STATUT_INITIAL = "NOUVELLE";

    // ... champs d'instance inchangés ...

    Reclamation(int id, String description, String quartier) {
        // ...
        this.statut = STATUT_INITIAL;   // plus de littéral éparpillé
    }

    boolean estUrgente() {
        return this.statut.equals(STATUT_INITIAL);
        // Avantage : si la valeur initiale change demain (ex. "RECUE"),
        // UN seul endroit à modifier — et le code reste lisible.
    }
}
```

**Pourquoi c'est mieux que le littéral** : le mot « magique » `"NOUVELLE"` pouvait se retrouver recopié à 4 endroits ; une faute de frappe dans une seule copie créait un bug silencieux. La constante centralise la vérité.

## Étape 2 — Le compteur (le piège de la surcharge)

```java
static int compteurCreees = 0;

Reclamation(int id, String description, String quartier) {
    if (id <= 0) { throw new IllegalArgumentException("L'id doit être positif"); }
    if (description == null || description.isBlank()) {
        throw new IllegalArgumentException("La description est obligatoire");
    }
    this.id = id;
    this.description = description;
    this.quartier = quartier;
    this.statut = STATUT_INITIAL;
    compteurCreees++;   // ICI, et seulement ici
}

Reclamation(int id, String description) {
    this(id, description, "Non précisé"); // délègue : le compteur s'incrémente
                                          // UNE fois, dans le constructeur complet
}

static int totalCreees() {
    return compteurCreees;
}
```

**Le raisonnement attendu** : si on incrémentait aussi dans la surcharge, une création via `(4, "desc")` compterait DEUX fois (une dans la surcharge, une dans le complet qu'elle appelle). **Une seule responsabilité d'incrémentation, dans le constructeur complet** : toute création y passe obligatoirement. C'est le même principe que la validation : logique centralisée dans la porte principale.

## Étape 3 — La preuve de l'unicité

```java
public class MainSignal {
    public static void main(String[] args) {
        System.out.println(Reclamation.totalCreees()); // 0

        Reclamation r1 = new Reclamation(1, "Nid de poule", "Medina");
        Reclamation r2 = new Reclamation(2, "Lampadaire HS", "Plateau");
        Reclamation r3 = new Reclamation(3, "Eau coupée"); // surcharge

        System.out.println(Reclamation.totalCreees()); // 3 (la surcharge compte 1 fois)

        // La démonstration :
        r1.compteurCreees = 999;         // on "modifie le compteur de r1"...
        System.out.println(Reclamation.totalCreees()); // ...mais ça affiche 999 !
        // POURQUOI ? Il n'y a qu'UN SEUL compteur, partagé. "r1.compteurCreees"
        // n'est pas "le compteur de r1" : c'est LE compteur de la classe,
        // accédé par un chemin trompeur. D'où la règle : accès par le NOM DE CLASSE.
    }
}
```

Ce que vous deviez observer : **le `999` se voit partout**, y compris via `Reclamation.totalCreees()`. C'est la preuve expérimentale que `static` = une seule copie partagée.

## Étape 4 — Le diagnostic en méthode de classe

```java
// Dans Reclamation.java :
static String diagnostic(int nb) {
    // Utilitaire PUR : ne dépend que du paramètre, ne touche aucun champ.
    // Les early returns de la leçon 02 sont toujours d'actualité.
    if (nb == 0)  return "Rien a signaler";
    if (nb < 10)  return "Charge normale";
    if (nb < 20)  return "Charge elevee";
    return "Sature";
}

// Dans main :
System.out.println(Reclamation.diagnostic(12)); // Charge elevee
```

**Pourquoi `static` ici est le bon choix** : `diagnostic` ne lit ni n'écrit aucun champ d'instance — elle n'a besoin d'aucun objet. C'est la définition d'une méthode utilitaire pure. À l'inverse, `estUrgente()` N'EST PAS `static` : elle parle du statut d'UNE réclamation précise.

## Checklist de validation (récapitulatif)

- [ ] Je distingue variable d'instance et variable de classe sans hésiter.
- [ ] Je déclare des constantes `static final` en `MAJUSCULES` et je supprime les nombres/mots magiques.
- [ ] Je sais où centraliser un effet (incrément, validation) quand des constructeurs se surchargent.
- [ ] J'ai prouvé l'unicité du `static` de mes propres yeux (l'expérience du 999).
- [ ] Je sais quand une méthode doit être `static` (utilitaire pur) et quand non (parle d'un objet).
- [ ] J'explique pourquoi `main` est `static` (appel avant tout objet).

## 💡 Conseils

1. **Le réflexe de la question unique** : devant tout champ ou méthode, demandez « appartient-il à UN objet, ou à toute la classe ? ». Si la réponse hésite, c'est presque toujours un champ d'instance.
2. **Constantes = documentation exécutable** : `Reclamation.MAX_URGENCE` explique l'intention, `5` ne l'explique pas.
3. **Méfiance instinctive envers un `static` non final** : chaque fois que vous en écrivez un, demandez-vous s'il ne mérite pas de vivre dans un objet dédié (ou plus tard, un bean Spring).

➡️ **Prochaine étape** : leçon 06 — les **packages** : donner une adresse à vos classes et préparer le terrain des dizaines de classes à venir.



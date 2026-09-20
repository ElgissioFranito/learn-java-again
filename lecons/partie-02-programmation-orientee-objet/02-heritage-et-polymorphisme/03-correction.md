# Correction détaillée — Exercice 02 « La famille des réclamations »

> 🧭 **Comment ce fichier s'articule** : vous venez de faire (ou tenter) l'exercice (`02-exercice.md`). Voici la solution complète, les choix expliqués, puis la checklist et les conseils.

## Correction pas à pas

### Étape 1 — La mère

```java
// Fichier : Reclamation.java
public class Reclamation {

    private final int id;
    private final String description;
    private final String quartier;
    private String statut;

    public Reclamation(int id, String description, String quartier) {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("La description est obligatoire");
        }
        this.id = id;
        this.description = description;
        this.quartier = quartier;
        this.statut = "NOUVELLE";
    }

    // Le comportement GÉNÉRIQUE — les filles le redéfiniront
    public void traiter() {
        this.statut = "EN_COURS";
        System.out.println("Traitement générique de #" + id);
    }

    // L'outil réservé à la famille : protected = classe + filles + package
    protected void changerStatut(String nouveau) {
        this.statut = nouveau;
    }

    public int getId()             { return id; }
    public String getDescription() { return description; }
    public String getQuartier()    { return quartier; }
    public String getStatut()      { return statut; }
}
```

**Choix expliqué** : `changerStatut` est `protected` car seules les filles ont besoin d'ajuster le statut ; `MainSignal` (hors famille) n'y a pas accès — vérifiez-le, le compilateur refuse.

### Étape 2 — Les trois filles

```java
// Fichier : ReclamationVoirie.java
public class ReclamationVoirie extends Reclamation {

    private final String gravite;

    public ReclamationVoirie(int id, String description, String quartier, String gravite) {
        super(id, description, quartier);   // 1ère instruction : la partie "mère"
        this.gravite = gravite;
    }

    @Override
    public void traiter() {
        changerStatut("EN_COURS");          // la méthode protected de la mère
        System.out.println("#" + getId() + " [VOIRIE/" + gravite + "] équipe envoyée : "
            + getDescription());
    }
}
```

```java
// Fichier : ReclamationProprete.java
public class ReclamationProprete extends Reclamation {

    private final boolean recyclable;

    public ReclamationProprete(int id, String description, String quartier, boolean recyclable) {
        super(id, description, quartier);
        this.recyclable = recyclable;
    }

    @Override
    public void traiter() {
        changerStatut("EN_COURS");
        String tri = recyclable ? "tri à faire" : "collecte standard"; // ternaire (partie 1)
        System.out.println("#" + getId() + " [PROPRETE] " + tri + " : " + getDescription());
    }
}
```

```java
// Fichier : ReclamationEclairage.java
public class ReclamationEclairage extends Reclamation {

    private final String numeroLampadaire;

    public ReclamationEclairage(int id, String description, String quartier, String numeroLampadaire) {
        super(id, description, quartier);
        this.numeroLampadaire = numeroLampadaire;
    }

    @Override
    public void traiter() {
        changerStatut("EN_COURS");
        System.out.println("#" + getId() + " [ECLAIRAGE] intervention au lampadaire "
            + numeroLampadaire + " (" + getQuartier() + ")");
    }
}
```

### Étape 3 — La boucle polymorphe

```java
// Fichier : MainSignal.java
public class MainSignal {
    public static void main(String[] args) {
        Reclamation[] recs = {                                  // tableau du type MÈRE
            new ReclamationVoirie(1, "Nid de poule", "Medina", "majeure"),
            new ReclamationProprete(2, "Dépôt sauvage", "Fass", true),
            new ReclamationEclairage(3, "Lampadaire éteint", "Sicap", "L-42")
        };

        for (Reclamation r : recs) {
            r.traiter();    // polymorphisme : chaque objet exécute SA version
        }

        for (Reclamation r : recs) {
            System.out.println("#" + r.getId() + " statut = " + r.getStatut());
        }
    }
}
```

Sortie attendue :

```text
#1 [VOIRIE/majeure] équipe envoyée : Nid de poule
#2 [PROPRETE] tri à faire : Dépôt sauvage
#3 [ECLAIRAGE] intervention au lampadaire L-42 (Sicap)
#1 statut = EN_COURS
#2 statut = EN_COURS
#3 statut = EN_COURS
```

### Étape 4 (bonus) — Compléter la mère, pas la remplacer

```java
@Override
public void traiter() {
    super.traiter();   // exécute le traiter() de la MÈRE : statut → EN_COURS + affichage générique
    System.out.println("#" + getId() + " [VOIRIE/" + gravite + "] équipe envoyée : "
        + getDescription()); // puis la spécificité voirie
}
```

**Choix expliqué** : `super.traiter()` réutilise le travail de la mère au lieu de le recopier — si demain la version générique journalise quelque chose, la fille en profite automatiquement. C'est le bon usage de la redéfinition : *étendre* le comportement, pas le dupliquer.

### Explication des choix techniques

1. **`super(...)` obligatoire en 1ère ligne** : la partie « mère » doit exister (avec sa validation) avant d'initialiser la spécificité fille. Java l'impose, et c'est logique : on ne meuble pas une maison avant d'avoir posé ses murs.
2. **`@Override` partout** : sans lui, une faute de frappe (`treater()`) créerait une méthode nouvelle, sans erreur de compilation — et la fille n'exécuterait jamais sa spécialité dans la boucle polymorphe.
3. **Getters plutôt que champs dans les filles** : `id` est `private` chez la mère ; les filles utilisent `getId()`. On aurait pu mettre `id` en `protected`, mais l'encapsulation de la leçon 01 reste la priorité.
4. **Un seul type dans la boucle** : `Reclamation[]` — c'est TOUT le bénéfice du polymorphisme. Ajouter demain une quatrième type de réclamation ne nécessite de modifier **aucune** boucle existante.

## Checklist de validation

- [ ] La hiérarchie compile : `extends` sur les 3 filles, `super(...)` en 1ère ligne de chaque constructeur.
- [ ] Chaque fille porte `@Override` et je sais expliquer le bug silencieux que l'annotation empêche.
- [ ] La boucle de `MainSignal` n'a AUCUN `if` sur le type : le polymorphisme fait le travail.
- [ ] `changerStatut` est `protected` et je ne peux pas l'appeler depuis `MainSignal`.
- [ ] Le bonus compile et le statut passe bien à `EN_COURS` via `super.traiter()`.
- [ ] Je sais dire pourquoi `EstimationInsee extends Reclamation` serait une erreur (pas une relation « est-un »).

## 💡 Conseils

1. **Testez Liskov mentalement** : remplacez chaque fille par la mère dans vos raisonnements — si quelque chose casse, la hiérarchie est suspecte.
2. **La leçon 03 va étoffer ce design** : l'interface `Traitable` y deviendra un **contrat** que plusieurs familles pourront signer, pas seulement les réclamations. Conservez vos fichiers.
3. **Ne descendez jamais sous 3 niveaux** : une `ReclamationUrgenteVoirie` qui étend `ReclamationVoirie` est le début de la spirale — préférez un champ ou une interface.

➡️ **Prochaine étape** : la leçon 03 introduit **interfaces et classes abstraites** — le contrat pur et la base partielle, qui sont aussi le socle des `Repository`/`Service` que Spring injectera en partie 7.

## ✔️ Validation du code

Le code de cette correction (les 4 fichiers : mère, 3 filles, `MainSignal`) a été **compilé et exécuté** (`javac *.java` puis `java MainSignal`) dans un dossier temporaire — la sortie observée correspond **exactement** à celle annoncée :

```text
#1 [VOIRIE/majeure] équipe envoyée : Nid de poule
#2 [PROPRETE] tri à faire : Dépôt sauvage
#3 [ECLAIRAGE] intervention au lampadaire L-42 (Sicap)
#1 statut = EN_COURS
#2 statut = EN_COURS
#3 statut = EN_COURS
```

Aucune erreur de compilation ni d'exécution. Une erreur possible au moment d'écrire le code : **oublier `super(...)` dans un constructeur fils** — Java tenterait alors d'appeler le constructeur sans paramètres de la mère, qui n'existe pas (on n'a écrit que le constructeur complet), et la compilation échoue avec « constructor Reclamation in class Reclamation cannot be applied to given types ». Si vous la rencontrez, c'est le `super(...)` manquant.



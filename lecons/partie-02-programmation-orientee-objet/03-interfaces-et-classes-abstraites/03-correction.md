# Correction détaillée — Exercice 03 « Le contrat `Traitable` »

> 🧭 **Comment ce fichier s'articule** : vous venez de tenter l'exercice (`02-exercice.md`). Voici la solution complète, les choix expliqués, la checklist et les conseils.

## Correction pas à pas

### Étape 1 — Le contrat

```java
// Fichier : Traitable.java
public interface Traitable {
    void traiter();   // promesse sans corps : chaque signataire devra fournir le sien
}
```

### Étape 2 — La mère et deux signataires

```java
// Fichier : Reclamation.java (mère, reprise de la leçon 02)
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

    protected void changerStatut(String nouveau) { this.statut = nouveau; }

    public int getId()             { return id; }
    public String getDescription() { return description; }
    public String getQuartier()    { return quartier; }
    public String getStatut()      { return statut; }
}
```

```java
// Fichier : ReclamationVoirie.java
public class ReclamationVoirie extends Reclamation implements Traitable {
    private final String gravite;

    public ReclamationVoirie(int id, String description, String quartier, String gravite) {
        super(id, description, quartier);
        this.gravite = gravite;
    }

    @Override
    public void traiter() {
        changerStatut("EN_COURS");
        System.out.println("#" + getId() + " [VOIRIE/" + gravite + "] équipe envoyée : " + getDescription());
    }
}
```

```java
// Fichier : ReclamationProprete.java
public class ReclamationProprete extends Reclamation implements Traitable {
    private final boolean recyclable;

    public ReclamationProprete(int id, String description, String quartier, boolean recyclable) {
        super(id, description, quartier);
        this.recyclable = recyclable;
    }

    @Override
    public void traiter() {
        changerStatut("EN_COURS");
        String tri = recyclable ? "tri à faire" : "collecte standard";
        System.out.println("#" + getId() + " [PROPRETE] " + tri + " : " + getDescription());
    }
}
```

### Étape 3 — Le signataire hors-famille

```java
// Fichier : Agent.java
public class Agent implements Traitable {   // AUCUN extends : pas une réclamation
    private final String nom;

    public Agent(String nom) { this.nom = nom; }

    @Override
    public void traiter() {
        System.out.println(nom + " prend en charge la file de réclamations");
    }
}
```

### Étape 4 — La boucle par contrat

```java
// Fichier : MainTraitable.java
public class MainTraitable {
    public static void main(String[] args) {
        Traitable[] travail = {       // typé par le CONTRAT, pas par une famille
            new ReclamationVoirie(1, "Nid de poule", "Medina", "majeure"),
            new ReclamationProprete(2, "Dépôt sauvage", "Fass", true),
            new Agent("Fatou")
        };
        for (Traitable t : travail) {
            t.traiter();              // polymorphisme par interface
        }
    }
}
```

Sortie attendue :

```text
#1 [VOIRIE/majeure] équipe envoyée : Nid de poule
#2 [PROPRETE] tri à faire : Dépôt sauvage
Fatou prend en charge la file de réclamations
```

### Étape 5 (bonus) — La décision

- **(a)** Non : créer une `Reclamation` « générique » n'a pas de sens métier — toute réclamation réelle est voirie, propreté ou éclairage. Une classe abstraite `Reclamation` serait donc *justifiée* pour interdire `new Reclamation(...)`.
- **(b)** Non, et c'est le piège : si la mère devient abstraite, elle reste la MÈRE de la famille — l'`Agent` n'hérite pas d'elle et ne pourrait toujours pas entrer dans une boucle typée `Reclamation`. La classe abstraite ne remplace pas le contrat.
- **(c) Conclusion** : il faut **les deux outils, chacun à son poste** — l'interface `Traitable` ouvre la boucle à toutes les familles (Agent inclus), et si l'on veut empêcher les réclamations génériques, on rend la mère `abstract`. Aucun des deux ne fait le travail de l'autre.

### Explication des choix techniques

1. **`Traitable` typé sur la boucle** : le polymorphisme de la leçon 02 fonctionnait, mais enfermé dans la famille. L'interface casse ce mur : le tableau accepte tout signataire, de n'importe quelle hiérarchie.
2. **`extends ... implements ...`** : l'ordre syntaxique est imposé (héritage d'abord, contrats ensuite). Une fille peut hériter de la mère ET signer le contrat : les deux sont indépendants.
3. **Le statut reste dans la famille** : `traiter()` de la voirie/propreté utilise `changerStatut` (`protected` de la mère). L'`Agent` n'a pas de statut — son `traiter()` n'en touche pas : chaque signataire gère SON affaire, le contrat n'impose que la promesse.

## ✔️ Validation du code

Ce code a été **compilé et exécuté** (`javac *.java` puis `java MainTraitable`) dans un dossier temporaire — la sortie observée correspond **exactement** à celle annoncée ci-dessus. Aucune erreur.

Erreur fréquente à connaître : si un signataire **oublie** d'implémenter `traiter()`, la compilation échoue avec « ReclamationVoirie is not abstract and does not override abstract method traiter() in Traitable ». C'est le contrat forcé par le compilateur — lisez ce message : il nomme la méthode manquante.

## Checklist de validation

- [ ] `Traitable` est un contrat pur (aucun corps).
- [ ] Les deux réclamations font `extends Reclamation implements Traitable`.
- [ ] `Agent` entre dans la boucle sans aucun lien d'héritage avec `Reclamation`.
- [ ] La boucle est typée `Traitable[]`.
- [ ] Le bonus conclut : interface (boucle ouverte) + classe abstraite (si besoin d'interdire le générique) — chacun son rôle.

## 💡 Conseils

1. **Gardez `Traitable` pour la suite** : la leçon 04 transformera le statut en types fermés (`sealed`) et le `switch` en pattern matching — sur les mêmes objets.
2. **Dites-le avec vos mots** : « l'interface décrit ce que les objets SAVOIR faire ; l'héritage décrit ce qu'ils SONT ». Si une phrase comme celle-ci vous vient naturellement, la leçon est acquise.
3. **En partie 7**, vous retrouverez exactement ce schéma : un contrôleur qui appelle `traitementService.traiter(id)` — sans jamais voir la classe concrète.

➡️ **Prochaine étape** : la leçon 04 — records, sealed et pattern matching, les trois outils « Java moderne » de la roadmap.


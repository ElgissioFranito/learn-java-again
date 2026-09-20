# Correction détaillée — Exercice 01 « Blindez la `Reclamation` »

> 🧭 **Comment ce fichier s'articule** : vous venez de faire (ou tenter) l'exercice (`02-exercice.md`). Ici, la solution complète pas à pas, avec les choix techniques justifiés, puis la checklist de validation et des conseils. Ne passez à la suite qu'après avoir comparé votre code avec celui-ci.

## Correction pas à pas

### Étapes 1-2 — De l'ancienne classe aux champs `private`

Le point clé de l'étape 2 : après avoir rendu les champs `private`, le compilateur signale chaque ligne qui écrivait directement un champ (`r.statut = ...`). Ce n'est pas un problème à « régler » : c'est le but. Les seuls endroits autorisés restent l'intérieur de la classe (constructeur et méthodes).

### Étapes 2-4 — La classe complète

```java
// Fichier : Reclamation.java
public class Reclamation {

    // ÉTAPE 2 : private + final pour ce qui ne change JAMAIS
    private final int id;
    private final String description;
    private final String quartier;
    private String statut;              // seul champ non final : il évolue

    // ÉTAPE 4 : le constructeur complet, qui valide TOUT
    public Reclamation(int id, String description, String quartier) {
        // Garde-fous (même schéma que la partie 1, avec les 3 paramètres) :
        if (id <= 0) {
            throw new IllegalArgumentException("L'id doit être positif");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("La description est obligatoire");
        }
        if (quartier == null || quartier.isBlank()) {
            throw new IllegalArgumentException("Le quartier est obligatoire");
        }
        this.id = id;
        this.description = description;
        this.quartier = quartier;
        this.statut = "NOUVELLE";       // état de départ
    }

    // ÉTAPE 4 : la surcharge délègue — this(...) en PREMIÈRE ligne
    public Reclamation(int id, String description) {
        this(id, description, "Non précisé");
    }

    // ÉTAPE 3 : le guichet de lecture
    public int getId()                  { return id; }
    public String getDescription()      { return description; }
    public String getQuartier()         { return quartier; }
    public String getStatut()           { return statut; }

    // PAS de setId, pas de setDescription, pas de setQuartier : final.
    // PAS de setStatut non plus : les transitions passent par les méthodes métier.

    // ÉTAPE 3 : transition 1 — entrer en traitement
    public void demarrerTraitement() {
        if (!statut.equals("NOUVELLE")) {   // on ne part QUE de NOUVELLE
            throw new IllegalStateException(
                "Impossible de démarrer une réclamation " + statut);
        }
        this.statut = "EN_COURS";
    }

    // ÉTAPE 3 : transition 2 — clôturer
    public void marquerResolue() {
        if (!statut.equals("EN_COURS")) {   // on ne clôture QUE depuis EN_COURS
            throw new IllegalStateException(
                "Impossible de résoudre une réclamation " + statut);
        }
        this.statut = "RESOLUE";
    }

    // ÉTAPE 5 : le comportement (anti-anémique)
    public boolean estOuverte() {
        return statut.equals("NOUVELLE") || statut.equals("EN_COURS");
    }

    public String getLigneAffichage() {
        return "#" + id + " [" + statut + "] " + description + " (" + quartier + ")";
    }
}
```

### Étape 6 — Le programme de démonstration

```java
// Fichier : MainEncapsulation.java
public class MainEncapsulation {
    public static void main(String[] args) {

        // 1. Version surchargée (quartier "Non précisé")
        Reclamation r = new Reclamation(1, "Fuite d'eau");

        // 2. Les tricheries — décommentez-les UNE par UNE pour voir le refus :
        // r.id = -7;            // erreur : "id has private access"
        // r.statut = "PLATANE"; // erreur : "statut has private access"
        // r.setDescription("x");// erreur : la méthode n'existe pas

        System.out.println(r.getLigneAffichage()); // #1 [NOUVELLE] Fuite d'eau (Non précisé)

        // 3. Le cycle de vie contrôlé
        r.demarrerTraitement();
        r.marquerResolue();
        System.out.println(r.getStatut());          // RESOLUE

        try {
            r.demarrerTraitement();                 // repartir d'une résolue : refusé
        } catch (IllegalStateException e) {
            System.out.println("Refus : " + e.getMessage());
        }

        // 4. La validation du constructeur en action
        try {
            new Reclamation(0, "", "X");            // id ≤ 0 ET description vide
        } catch (IllegalArgumentException e) {
            System.out.println("Création refusée : " + e.getMessage());
        }
    }
}
```

Compilation et exécution :

```bash
javac Reclamation.java MainEncapsulation.java
java MainEncapsulation
```

Sortie attendue :

```text
#1 [NOUVELLE] Fuite d'eau (Non précisé)
RESOLUE
Refus : Impossible de démarrer une réclamation RESOLUE
Création refusée : L'id doit être positif
```

### Explication des choix techniques

1. **`final` sur id/description/quartier** : la question « qui modifie ça après création ? » répond « personne » → pas de setter, champ `final`. Le compilateur garantit ensuite l'immutabilité, gratuitement.
2. **Méthodes métier au lieu de `setStatut`** : un `setStatut` accepterait `RESOLUE` depuis `NOUVELLE` (sauter le traitement). Les méthodes de transition encodent le **cycle de vie** : `NOUVELLE → EN_COURS → RESOLUE`, jamais de saut.
3. **`IllegalStateException` vs `IllegalArgumentException`** : argument invalide à l'appel → `IllegalArgumentException` ; objet dans un état qui interdit l'action → `IllegalStateException`. Deux exceptions standard, deux intentions différentes.
4. **Validation dans le constructeur, pas seulement à la saisie** : la classe refuse elle-même les données fausses, où qu'elles viennent — c'est ça, l'encapsulation.

## Checklist de validation

- [ ] Tous mes champs sont `private` (et `final` quand ils ne changent pas).
- [ ] Je sais expliquer pourquoi il n'y a pas de `setStatut`, et ce qu'on utilise à la place.
- [ ] Mes méthodes de transition refusent les chemins impossibles (`IllegalStateException`).
- [ ] Mon constructeur valide les trois paramètres et la surcharge délègue avec `this(...)`.
- [ ] Aucun setter de « façade » sans validation dans mon code.
- [ ] J'ai décommenté les tricheries dans `main` et constaté le refus du compilateur.

## 💡 Conseils

1. **Réutilisez cette classe dans toute la partie 2** : les leçons suivantes (héritage, interfaces, records) partent toutes de cette `Reclamation`.
2. **Idempotence** : appeler `marquerResolue()` deux fois de suite lève une exception à la seconde — c'est correct (on ne clôture pas deux fois). Poser la question « que se passe-t-il si on appelle deux fois ? » est le cœur de l'encapsulation.
3. **Ne confondez pas** encapsulation (= protection de l'état) et immuabilité (= pas d'état modifiable) : les deux cohabitent très bien ici.

➡️ **Prochaine étape** : l'exercice 02 (héritage) utilisera cette classe comme mère — vos getters et votre validation y resserviront immédiatement.

## ✔️ Validation du code

Le code de cette correction a été **compilé et exécuté** (`javac` puis `java`) dans un dossier temporaire — la sortie observée correspond **exactement** à celle annoncée ci-dessus :

```text
#1 [NOUVELLE] Fuite d'eau (Non précisé)
RESOLUE
Refus : Impossible de démarrer une réclamation RESOLUE
Création refusée : L'id doit être positif
```

Aucune erreur de compilation ni d'exécution n'a été rencontrée. Les deux erreurs volontaires (`IllegalStateException` et `IllegalArgumentException`) sont **rattrapées par le `try/catch`** du programme : c'est le comportement attendu, pas un plantage.


```

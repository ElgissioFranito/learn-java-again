# Correction détaillée — Exercice 05 « Le fil rouge passe au vrai `enum` »

> 🧭 **Comment ce fichier s'articule** : vous venez de tenter l'exercice (`02-exercice.md`). Solution complète, choix expliqués, validation du code exécuté, checklist et conseils.

## Correction pas à pas

### Étape 1 — L'`enum` du statut

```java
// Fichier : StatutReclamation.java
public enum StatutReclamation {

    NOUVELLE("Nouvelle demande"),
    EN_COURS("En cours de traitement"),
    RESOLUE("Réclamation résolue");

    private final String libelle;

    private StatutReclamation(String libelle) {   // toujours private : liste fermée
        this.libelle = libelle;
    }

    public String getLibelle() { return libelle; }
}
```

### Étape 2 — La `Reclamation` au statut typé

```java
// Fichier : Reclamation.java
public class Reclamation {

    private final int id;
    private final String description;
    private final String quartier;
    private StatutReclamation statut;             // le type fermé remplace String

    public Reclamation(int id, String description, String quartier) {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("La description est obligatoire");
        }
        this.id = id;
        this.description = description;
        this.quartier = quartier;
        this.statut = StatutReclamation.NOUVELLE;
    }

    public void demarrerTraitement() {
        if (statut != StatutReclamation.NOUVELLE) {   // comparaison d'identité, sûre
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

    public String getBadge() {
        return switch (statut) {          // exhaustif : pas de default
            case NOUVELLE -> "🆕";
            case EN_COURS -> "⏳";
            case RESOLUE  -> "✅";
        };
    }

    public String getLigneAffichage() {
        return "#" + id + " [" + statut + "] " + description + " (" + quartier + ")";
    }

    public int getId()                   { return id; }
    public String getDescription()       { return description; }
    public String getQuartier()          { return quartier; }
    public StatutReclamation getStatut() { return statut; }
}
```

### Étape 3 — Le second `enum`

```java
// Fichier : Priorite.java
public enum Priorite {

    BASSE(168),      // une semaine
    NORMALE(48),     // deux jours
    URGENTE(4);      // quatre heures

    private final int delaiHeuresMax;

    private Priorite(int delaiHeuresMax) {
        this.delaiHeuresMax = delaiHeuresMax;
    }

    public int getDelaiHeuresMax() { return delaiHeuresMax; }
}
```

### Étape 4 — L'`enum` au menu

```java
// Fichier : MainEnum.java
public class MainEnum {
    public static void main(String[] args) {

        // 1. Cycle de vie avec le type fermé
        Reclamation r = new Reclamation(1, "Nid de poule", "Medina");
        System.out.println(r.getLigneAffichage() + " " + r.getBadge());
        r.demarrerTraitement();
        System.out.println(r.getLigneAffichage() + " " + r.getBadge());
        r.marquerResolue();
        System.out.println(r.getLigneAffichage() + " " + r.getBadge());

        // 2. Parcours de Priorite.values()
        for (Priorite p : Priorite.values()) {
            System.out.println(p.name() + " → délai max : " + p.getDelaiHeuresMax() + "h");
        }

        // 3. Stocker puis relire avec valueOf()
        String stocke = StatutReclamation.EN_COURS.name();      // "EN_COURS"
        StatutReclamation relu = StatutReclamation.valueOf(stocke);
        System.out.println("Relu : " + relu.getLibelle());

        // 4. valueOf() refuse l'inconnu
        try {
            StatutReclamation.valueOf("EN_ATTAENTE");
        } catch (IllegalArgumentException e) {
            System.out.println("Valeur inconnue : EN_ATTAENTE");
        }
    }
}
```

Sortie attendue :

```text
#1 [NOUVELLE] Nid de poule (Medina) 🆕
#1 [EN_COURS] Nid de poule (Medina) ⏳
#1 [RESOLUE] Nid de poule (Medina) ✅
BASSE → délai max : 168h
NORMALE → délai max : 48h
URGENTE → délai max : 4h
Relu : En cours de traitement
Valeur inconnue : EN_ATTAENTE
```

### Étape 5 (bonus) — Prouver le piège de l'`ordinal()`

Version A de l'`enum` (sans `EN_PAUSE`) :

```java
public enum StatutReclamation { NOUVELLE, EN_COURS, RESOLUE }
// EN_COURS.ordinal() == 1
```

Version B (un mois plus tard, `EN_PAUSE` inséré) :

```java
public enum StatutReclamation { NOUVELLE, EN_PAUSE, EN_COURS, RESOLUE }
// EN_COURS.ordinal() == 2 ; EN_PAUSE.ordinal() == 1  ← l'ancien "1" de la base
```

La donnée historique « 1 » (stockée pour EN_COURS) désigne désormais **EN_PAUSE** : le système affiche « en pause » pour des réclamations en cours, sans aucune erreur nulle part. Preuve par l'affichage de `ordinal()` dans les deux versions — les positions glissent, les `.name()` restent fixes.

### Explication des choix techniques

1. **`==` sur les constantes** : chaque valeur d'`enum` est un singleton — l'identité suffit et élimine les risques de casse/espaces. `.equals()` fonctionnerait, mais apporte du bruit pour rien.
2. **Constructeur `private` obligatoire** : c'est le compilateur qui ferme la liste — personne ne peut ajouter une valeur hors déclaration (exactement l'esprit d'une famille scellée).
3. **`name()` pour le stockage** : le texte est stable quel que soit l'ordre de déclaration ; c'est ce que la partie 8 utilisera via `@Enumerated(EnumType.STRING)`.

## ✔️ Validation du code

Le code des étapes 1 à 4 a été **compilé et exécuté** (`javac *.java` puis `java MainEnum`) dans un dossier temporaire : la sortie observée correspond **exactement** à celle annoncée. Le scénario du bonus a aussi été vérifié : la version A affiche `EN_COURS ordinal = 1`, la version B (avec `EN_PAUSE` en 2e position) affiche `EN_COURS ordinal = 2` et `EN_PAUSE ordinal = 1` — le décalage démontré.

Erreurs fréquentes à connaître :
- Oublier le **point-virgule** après la dernière constante quand l'`enum` a des champs (`RESOLUE("...", 3);`) → erreur de compilation immédiate.
- Un **switch non exhaustif** sur l'`enum` sans `default` → le compilateur refuse (message sur les valeurs non couvertes) : c'est le filet voulu.

## Checklist de validation

- [ ] `StatutReclamation` et `Priorite` portent leurs champs, constructeur `private` compris.
- [ ] La `Reclamation` ne contient plus aucune `String` de statut, comparaisons avec `==`.
- [ ] `getBadge()` : switch exhaustif, sans `default`.
- [ ] Le main parcourt `values()`, stocke/relit via `name()`/`valueOf()` et encaisse l'inconnu.
- [ ] Le bonus démontre le décalage de l'`ordinal()` (avant/après insertion).

## 💡 Conseils

1. **Réfléchi `enum` > réflexe String** : avant d'ajouter un champ, demandez-vous « combien de valeurs possibles ? » — si la réponse est finie et connue, c'est un `enum`.
2. **La partie 3 (Collections)** travaillera sur des *listes* d'objets — vos `Reclamation` typées par `enum` y deviennent des éléments de `List<Reclamation>` : gardez précieusement vos fichiers.
3. **En partie 8**, le même `enum` se stockera en base avec `@Enumerated(EnumType.STRING)` : la discipline `name()` d'aujourd'hui est un investissement direct.

➡️ **Prochaine étape** : la partie 3 — Collections, Generics, Optionals. Vous y organiserez DES dizaines de réclamations, plus seulement une.


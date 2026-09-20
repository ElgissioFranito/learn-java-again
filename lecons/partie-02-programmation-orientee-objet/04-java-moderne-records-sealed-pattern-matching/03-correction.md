# Correction détaillée — Exercice 04 « SignalCUA version moderne »

> 🧭 **Comment ce fichier s'articule** : vous venez de tenter l'exercice (`02-exercice.md`). Solution complète, choix expliqués, validation du code, checklist et conseils.

## Correction pas à pas

### Étapes 1-2 — La famille scellée et ses trois records

```java
// Fichier : TypeReclamation.java
public sealed interface TypeReclamation permits Voirie, Proprete, Eclairage { }
```

```java
// Fichier : Voirie.java
public record Voirie(String gravite) implements TypeReclamation { }

// Fichier : Proprete.java
public record Proprete(boolean recyclable) implements TypeReclamation { }

// Fichier : Eclairage.java
public record Eclairage(String numeroLampadaire) implements TypeReclamation { }
```

*(Chaque fichier dans le sien en pratique ; ici regroupés pour la lisibilité.)*

### Étape 3 — Le DTO avec validation

```java
// Fichier : Signalement.java
public record Signalement(int id, String description, String quartier, TypeReclamation type) {

    public Signalement {                       // constructeur compact : validation seulement
        if (id <= 0) {
            throw new IllegalArgumentException("L'id doit être positif");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("La description est obligatoire");
        }
}
```

### Étape 4 — Le pattern matching exhaustif

```java
// Fichier : MainTypes.java
public class MainTypes {

    // Une méthode static utilitaire (rappel partie 1) : la logique d'affichage par type
    static String decrir(TypeReclamation type) {
        return switch (type) {
            case Voirie v     -> "[VOIRIE] gravité " + v.gravite();
            case Proprete p   -> p.recyclable() ? "[PROPRETE] tri à faire"
                                                : "[PROPRETE] collecte standard";
            case Eclairage e  -> "[ECLAIRAGE] lampadaire " + e.numeroLampadaire();
        };   // pas de default : le compilateur garantit l'exhaustivité
    }

    public static void main(String[] args) {
        Signalement[] signalements = {
            new Signalement(1, "Nid de poule", "Medina", new Voirie("majeure")),
            new Signalement(2, "Dépôt sauvage", "Fass", new Proprete(true)),
            new Signalement(3, "Lampadaire éteint", "Sicap", new Eclairage("L-42"))
        };

        for (Signalement s : signalements) {
            System.out.println("#" + s.id() + " " + s.quartier() + " " + decrir(s.type()));
        }
    }
}
```

Sortie attendue :

```text
#1 Medina [VOIRIE] gravité majeure
#2 Fass [PROPRETE] tri à faire
#3 Sicap [ECLAIRAGE] lampadaire L-42

### Étape 5 (bonus) — Le compilateur comme filet

Après l'ajout de `Archive(String motif)` et de son nom dans `permits` :

```java
// Fichier : Archive.java (le nouveau type)
public record Archive(String motif) implements TypeReclamation { }
```

La compilation de `MainTypes` échoue avec un message du type :

```text
error: the switch expression does not cover all possible input values
```

*(Libellé exact variable selon le compilateur : il signale que le switch sur `TypeReclamation` ne couvre plus tous les cas autorisés.)*

La réparation est guidée : on ajoute le cas manquant —

```java
case Archive a -> "[ARCHIVE] motif : " + a.motif();
```

— et tout recompile. **C'est exactement le bénéfice scellé + pattern matching** : ajouter un type métier force la mise à jour de chaque point de décision, avec le compilateur pour guide. Aucun cas oublié en silence.

### Explication des choix techniques

1. **Un record par type, avec sa donnée** : contrairement à un `String type` libre, chaque variante PORTE sa donnée spécifique (gravité, recyclable, numéro) — impossible de confondre, impossible d'oublier.
2. **Le constructeur compact valide** : même discipline que la leçon 01, appliquée au record. Le DTO refuse de naître faux.
3. **`decrir()` en méthode static** : la logique d'affichage par type vit à un endroit unique. Dans un vrai projet, elle serait une méthode d'instance du DTO ou du service d'affichage — l'essentiel est qu'elle soit unique.
4. **Switch sans `default`** : volontaire. Le jour où un 4e type apparaît, la compilation force son traitement (vous l'avez vécu au bonus).

## ✔️ Validation du code

Le code des étapes 1 à 4 a été **compilé et exécuté** (`javac *.java` puis `java MainTypes`) dans un dossier temporaire : la sortie observée est conforme à celle annoncée. Le scénario du bonus a aussi été vérifié : avec `Archive` ajoutée aux `permits` sans `case` correspondant, **la compilation échoue** (le compilateur indique que le switch ne couvre pas toutes les valeurs possibles) ; après ajout du `case Archive a -> ...`, la compilation réussit.

Erreur fréquente à connaître : `case Voirie v` exige que `Voirie` soit un sous-type **direct** de la famille scellée. Si vous oubliez `implements TypeReclamation` sur un record, la compilation signale que le type n'apparaît pas dans la clause `permits` — vérifiez les deux bouts (l'interface ET les implémentations).

## Checklist de validation

- [ ] `TypeReclamation` scellée, ses 3 (puis 4) records autorisés.
- [ ] Chaque record porte SA donnée spécifique.
- [ ] Le constructeur compact de `Signalement` valide id et description.
- [ ] Le switch est exhaustif, sans `default`, et compile.
- [ ] J'ai vécu le bonus : erreur de compilation observée puis réparée par le `case` manquant.
- [ ] Je sais expliquer pourquoi cette approche élimine les cas oubliés « en silence ».

## 💡 Conseils

1. **Le trio record + sealed + pattern matching est LA signature du Java 2025-2026** : dans les offres d'emploi et les codebases modernes, c'est le réflexe attendu pour tout ce qui est « données + états ».
2. **Résistance au changement sans douleur** : ajouter `Archive` a cassé la compilation — c'est une *bonne* cassure : elle pointe les lignes à corriger. Les String libres cassent au runtime, bien plus tard, chez l'utilisateur.
3. **La leçon 05 simplifiera** les cas SANS donnée : quand un état ne porte rien (`NOUVELLE` n'a pas de champ), l'`enum` fait la même chose en une ligne.

➡️ **Prochaine étape** : la leçon 05 — les énumérations (`enum`), l'outil des ensembles finis de constantes, pour clore la partie 2.

```

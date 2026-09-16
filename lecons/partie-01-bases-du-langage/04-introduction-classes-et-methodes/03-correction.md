# Correction 04 — La classe `Reclamation` (Fil rouge — Étape 1)

> 🧭 **Articulation** : solution pas à pas de `02-exercice.md`. C'est la première vraie étape du fil rouge : comparez votre classe ligne par ligne avec la nôtre. La checklist finale reprend celle de la leçon.

## Étape 1 — `Reclamation.java`

```java
public class Reclamation {
    // ----- Champs : les données portées par chaque réclamation -----
    int id;
    String description;
    String quartier;
    String statut; // String pour l'instant : deviendra un enum en partie 2

    // ----- Constructeur complet : la porte contrôlée -----
    Reclamation(int id, String description, String quartier) {
        if (id <= 0) {
            throw new IllegalArgumentException("L'id doit être positif");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("La description est obligatoire");
        }
        this.id = id;                 // this.id = champ ; id = paramètre
        this.description = description;
        this.quartier = quartier;
        this.statut = "NOUVELLE";     // tout objet naît NOUVELLE
    }

    // ----- Surcharge : délégation au constructeur complet -----
    Reclamation(int id, String description) {
        this(id, description, "Non précisé");
        // this(...) DOIT être la première instruction du constructeur
    }

    // ----- Comportements -----
    boolean estUrgente() {
        return this.statut.equals("NOUVELLE");
    }

    void afficher() {
        // Dans une méthode d'instance, "id" veut dire "this.id"
        System.out.println("#" + id + " [" + statut + "] " + description
                + " (" + quartier + ")");
    }
}
```

**Choix techniques :**
- Les validations **avant** les affectations : si une donnée est invalide, on refuse AVANT de créer un objet à moitié rempli.
- La surcharge **délègue** avec `this(...)` : la logique de validation vit à UN SEUL endroit (le constructeur complet). Si la règle change demain, un seul endroit à corriger.
- `estUrgente()` vit dans la classe : la définition d'« urgent » ne sera plus recopiée dans tout le programme.

## Étape 2 — `MainSignal.java`

```java
import java.util.ArrayList;

public class MainSignal {
    public static void main(String[] args) {
        // 1. Création à la main (le "new" appelle le constructeur)
        Reclamation r1 = new Reclamation(1, "Nid de poule", "Medina");
        Reclamation r2 = new Reclamation(2, "Lampadaire HS", "Plateau");
        Reclamation r3 = new Reclamation(3, "Eau coupée", "Medina");
        Reclamation r4 = new Reclamation(4, "Canalisation cassée"); // surcharge

        // 2. Une SEULE liste : chaque objet porte toutes ses données
        ArrayList<Reclamation> reclamations = new ArrayList<>();
        reclamations.add(r1);
        reclamations.add(r2);
        reclamations.add(r3);
        reclamations.add(r4);

        // 3. Affichage en for-each
        for (Reclamation r : reclamations) {
            r.afficher();
        }

        // 4. Total + urgentes (la logique est dans l'objet : estUrgente())
        System.out.println("Total : " + reclamations.size());
        System.out.println("--- Urgentes ---");
        for (Reclamation r : reclamations) {
            if (r.estUrgente()) {   // pas de .equals("NOUVELLE") ici !
                r.afficher();
            }
        }
    }
}
```

## Étape 3 — Le compte par quartier

```java
// Tableaux parallèles (dernier recours, on le remplacera en partie 3 par une Map)
String[] quartiersConnus = new String[10]; // au plus 10 quartiers différents
int[] comptes = new int[10];
int nbQuartiers = 0; // combien de cases réellement utilisées

for (Reclamation r : reclamations) {
    boolean dejaCompte = false;
    // 1. Ce quartier est-il déjà dans quartiersConnus ?
    for (int i = 0; i < nbQuartiers; i++) {
        if (quartiersConnus[i].equals(r.quartier)) {
            comptes[i]++;       // oui : on incrémente son compte
            dejaCompte = true;
            break;              // inutile de continuer à chercher
        }
    }
    // 2. Sinon, on l'enregistre comme nouveau quartier
    if (!dejaCompte) {
        quartiersConnus[nbQuartiers] = r.quartier;
        comptes[nbQuartiers] = 1;
        nbQuartiers++;
    }
}

// 3. Le rapport, gravé une seule fois (StringBuilder, leçon 03)
StringBuilder sb = new StringBuilder();
sb.append("Reclamations par quartier\n");
for (int i = 0; i < nbQuartiers; i++) {
    sb.append("- ").append(quartiersConnus[i]).append(" : ").append(comptes[i]).append("\n");
}
System.out.println(sb);
// Medina : 2, Plateau : 1, Non précisé : 1
```

**Remarquez la double boucle imbriquée** : pour chaque réclamation, on parcourt les quartiers connus. Ça marche, mais c'est verbeux et fragile (taille 10 en dur). En partie 3, la `Map<String, Integer>` fera ce travail en 3 lignes — cette douleur est voulue !

## Étape 4 — Le test de validation

```java
// Décommenté, cette ligne produit au lancement :
// Exception in thread "main" java.lang.IllegalArgumentException: La description est obligatoire
Reclamation mauvaise = new Reclamation(99, "   ");
// "   " passe le test != null mais est rejetée par .isBlank() (espaces seuls = vide).
// Le programme s'arrête NET : aucun objet incohérent n'a été créé. C'est le but.
```

## Checklist de validation (récapitulatif)

- [ ] J'explique classe vs objet (plan vs maison) et j'instancie avec `new`.
- [ ] Ma classe a des champs, un constructeur validé, des méthodes.
- [ ] Je sais quand et pourquoi utiliser `this.` (champ vs paramètre).
- [ ] J'ai surchargé un constructeur et délégué avec `this(...)`.
- [ ] Mes constructeurs refusent les données invalides (`IllegalArgumentException`).
- [ ] La logique métier (`estUrgente()`) vit dans la classe, pas recopiée dehors.
- [ ] L'Étape 1 du fil rouge SignalCUA fonctionne de bout en bout.

## 💡 Conseils

1. **Deux fichiers dans le même dossier** = ils se « voient » automatiquement (pas d'`import` nécessaire tant qu'on n'a pas de packages — c'est le sujet de la leçon 06).
2. **Gardez `Reclamation.java` précieusement** : il va évoluer aux leçons 05 (compteur `static`), 06 (packages) et 07 (saisie clavier), puis traverser toute la partie 2.
3. Le test de l'étape 4 « qui plante exprès » est une pratique de pro : vérifier que la protection se déclenche vraiment. On en fera de vrais tests automatisés en partie 9 (JUnit).

➡️ **Prochaine étape** : leçon 05 — le mystérieux `static` (variables de classe et constantes) : pourquoi `main` n'a pas besoin d'objet, et comment compter toutes les réclamations créées.



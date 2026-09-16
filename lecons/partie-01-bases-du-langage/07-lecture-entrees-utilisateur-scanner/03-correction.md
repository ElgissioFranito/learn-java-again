# Correction 07 — SignalCUA interactif (Fil rouge — fin de l'Étape 1)

> 🧭 **Articulation** : solution pas à pas de `02-exercice.md`. C'est le point culminant de la partie 1 : toutes les briques se rejoignent. Comparez surtout vos étapes 2 (try/catch) et 4 (blindage). Checklist finale reprise en bas.

## Code complet corrigé — `MainSignal.java`

```java
package fr.cua.signalcua.app;

import fr.cua.signalcua.model.Reclamation;
import java.util.ArrayList;
import java.util.Scanner;

public class MainSignal {
    public static void main(String[] args) {
        Scanner clavier = new Scanner(System.in);      // UN seul Scanner
        ArrayList<Reclamation> reclamations = new ArrayList<>();
        int prochainId = 1;

        int choix;
        do {
            System.out.println("""

                    === SIGNALCUA ===
                    1. Signaler un probleme
                    2. Lister les signalements
                    0. Quitter
                    Votre choix :""");

            // ----- Étape 4 : blindage AVANT de lire -----
            while (!clavier.hasNextInt()) {            // tant que ce n'est pas un nombre
                clavier.nextLine();                    // vider la saisie invalide
                System.out.print("Nombre requis. Recommencez : ");
            }
            choix = clavier.nextInt();
            clavier.nextLine();                        // avalez le \n résiduel (piège n°1)

            switch (choix) {
                case 1 -> {
                    System.out.print("Description : ");
                    String description = clavier.nextLine().trim(); // nettoyage leçon 03

                    System.out.print("Quartier : ");
                    String quartier = clavier.nextLine().trim();

                    try {
                        // On passe l'id ACTUEL ; incrément seulement en cas de succès
                        Reclamation r = new Reclamation(prochainId, description, quartier);
                        prochainId++;
                        reclamations.add(r);
                        System.out.println("Enregistre :");
                        r.afficher();
                    } catch (IllegalArgumentException e) {
                        // Le constructeur a refusé : on affiche poliment
                        System.out.println("Refus : " + e.getMessage());
                    }
                }
                case 2 -> {
                    if (reclamations.isEmpty()) {      // ArrayList.isEmpty() : taille 0 ?
                        System.out.println("Aucun signalement");
                    } else {
                        System.out.println("--- " + reclamations.size() + " signalement(s) ---");
                        for (Reclamation r : reclamations) {
                            r.afficher();
                        }
                    }
                }
                case 0 -> System.out.println("Au revoir");
                default -> System.out.println("Choix inconnu");
            }
        } while (choix != 0);

        clavier.close();
    }
}
```

## Explications des points clés

**Pourquoi le `nextLine()` jetable est-il indispensable (étape 4 de l'exercice) ?**
Quand vous tapez `1` + Entrée, le buffer (la zone d'attente du clavier) contient `1` puis `\n`. `nextInt()` lit le `1` mais **laisse le `\n`**. Sans nettoyage, le premier `nextLine()` du case 1 avalerait ce `\n` : la « description » serait vide et le constructeur refuserait — sans que vous ayez pu taper quoi que ce soit. Le `clavier.nextLine()` juste après `nextInt()` débarrasse l'assiette.

**Pourquoi `try/catch` ici et pas une validation avant ?**
Défense en profondeur : `.trim()` nettoie, MAIS une saisie peut quand même être vide (l'utilisateur tape Entrée directement). Plutôt que de dupliquer le test `isBlank()` dans le `main`, on laisse **le constructeur** (source de vérité, leçon 04) trancher, et on rattrape son refus. La règle métier vit à UN endroit.

**Pourquoi la garde `hasNextInt()` plutôt que try/catch ?**
Deux techniques valides, mais `hasNextInt()` est ici plus lisible : on vérifie AVANT de lire et on redemande dans une boucle claire. Le `try/catch` autour de `nextInt()` marcherait aussi — vous saurez le préférer en connaissance de cause après la partie 4.

**`isEmpty()`** : méthode d'`ArrayList` équivalente à `size() == 0` — plus expressive.

## Session de test complète (à reproduire)

```text
=== SIGNALCUA ===
1. Signaler un probleme
2. Lister les signalements
0. Quitter
Votre choix :
abc
Nombre requis. Recommencez : 1
Description :
Refus : La description est obligatoire
1
Description : Nid de poule
Quartier : Medina
Enregistre :
REC-1 [NOUVELLE] Nid de poule (Medina)
2
--- 1 signalement(s) ---
REC-1 [NOUVELLE] Nid de poule (Medina)
0
Au revoir
```

Notez que l'id `REC-1` n'est consommé que si la création réussit : on incrémente `prochainId` UNIQUEMENT après le succès. Si l'incrément avait été dans l'appel (`prochainId++` en argument), le refus de la première tentative aurait « brûlé » l'id 1 — le premier signalement accepté aurait été REC-2. Un détail, mais le genre de détail qui distingue un programme soigné.

## Checklist de validation (récapitulatif)

- [ ] J'utilise UN `Scanner` pour tout le programme.
- [ ] J'explique et corrige le piège du `\n` résiduel après `nextInt()`.
- [ ] Mon menu ne plante sur aucune saisie (`hasNextInt()` + boucle de redemande).
- [ ] Les saisies sont nettoyées (`.trim()`) et validées en dernier recours par le constructeur (`try/catch`).
- [ ] L'id n'est consommé que si la création réussit.
- [ ] L'Étape 1 du fil rouge SignalCUA est 100 % interactive et fonctionnelle.

## 💡 Conseils

1. **Le menu console est votre terrain de jeu** : ajoutez librement un choix 3 (« statistiques ») pour réviser boucles et StringBuilder — chaque ajout consolide les leçons 01 à 07.
2. **Ne pleurez pas le `Scanner` en partie 7** : sa logique (lire une requête, valider, répondre) se transferera mot pour mot aux contrôleurs Spring — change seulement le canal (HTTP au lieu du clavier).
3. **Compilez tous les .java d'un coup** : `javac -d build $(find src -name "*.java")` évite d'oublier un fichier quand le projet grandit.

➡️ **Prochaine étape** : la partie 2 — Programmation Orientée Objet. Premier arrêt : l'**encapsulation et les constructeurs**, qui vont rendre votre `Reclamation` inaltérable de l'extérieur (des champs `private`, plus de champs modifiables à la sauvette).



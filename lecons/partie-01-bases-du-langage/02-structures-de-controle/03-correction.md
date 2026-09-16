# Correction 02 — Le tableau de bord du service voirie

> 🧭 **Articulation** : solution pas à pas de `02-exercice.md`. Comparez chaque étape avec votre code, surtout les bornes de boucles (piège 4 de la leçon). La checklist de validation finale reprend celle de la leçon.

## Code complet corrigé

```java
public class TableauBord {

    // Méthode de diagnostic (le "static" est expliqué en leçon 05 :
    // pour l'instant, retenez que ça permet de l'appeler depuis main)
    static String diagnostic(int nb) {
        // Les tranches ouvertes (1..9, 10..19, 20+) ne se prêtent pas
        // bien au switch (qui compare des valeurs FIXES). C'est exactement
        // le cas où if/else est le bon outil — savoir choisir l'outil
        // fait partie de l'apprentissage !
        if (nb == 0) {
            return "Rien a signaler";
        }
        if (nb < 10) {       // entre 1 et 9
            return "Charge normale";
        }
        if (nb < 20) {       // entre 10 et 19
            return "Charge elevee";
        }
        return "Sature";     // 20 et plus
        // Remarquez les early returns (section 2.5 de la leçon) :
        // chaque cas est tranché et on sort immédiatement.
    }

    public static void main(String[] args) {
        String[] quartiers = {"Medina", "Plateau", "Grand Yoff", "Fann"};
        int[] reclamations = {12, 0, 15, 6};

        // ----- Étape 2 : for classique avec index -----
        for (int i = 0; i < quartiers.length; i++) {
            // i < length (et pas <=) : les indices valides vont de 0 à length-1
            System.out.println(quartiers[i] + " : " + reclamations[i] + " reclamations");
        }

        // ----- Étape 3 : continue pour les quartiers vides -----
        System.out.println("--- Quartiers a traiter ---");
        for (int i = 0; i < quartiers.length; i++) {
            if (reclamations[i] == 0) {
                continue; // saute l'affichage de ce quartier
            }
            System.out.println(quartiers[i] + " : " + reclamations[i]);
        }

        // ----- Étape 4 : total et maximum en une seule boucle for-each -----
        int total = 0;
        int maximum = 0;
        for (int nb : reclamations) {
            total = total + nb;          // accumulation du total
            if (nb > maximum) {
                maximum = nb;            // nouveau record en cours de route
            }
        }
        System.out.println("Total : " + total);       // 33
        System.out.println("Maximum : " + maximum);   // 15

        // ----- Étape 5 : diagnostic par quartier -----
        System.out.println("--- Diagnostic ---");
        for (int i = 0; i < quartiers.length; i++) {
            System.out.println(quartiers[i] + " -> " + diagnostic(reclamations[i]));
        }
        // Medina -> Charge elevee, Plateau -> Rien a signaler (sauté
        // seulement à l'étape 3 ; ici on affiche tout exprès),
        // Grand Yoff -> Charge elevee, Fann -> Charge normale
    }
}
```

## Explications des choix techniques

**Étape 2 — pourquoi `i < quartiers.length` et pas `<=` ?**
Un tableau de 4 éléments a des indices 0, 1, 2, 3. `length` vaut 4 : l'indice 4 n'existe pas. `<=` provoquerait une `ArrayIndexOutOfBoundsException`. C'est LE réflexe à graver : **dernier indice = length - 1**.

**Étape 4 — pourquoi `for-each` ici ?**
On n'a pas besoin de l'index (on lit chaque valeur une fois). Le for-each élimine d'office le risque d'erreur de bornes. On utilise `for` avec index uniquement quand il faut la position (étapes 2, 3 et 5 : on affiche `quartiers[i]` ET `reclamations[i]`, deux tableaux parallèles).

**Étape 5 — pourquoi `if/else` plutôt que `switch` ?**
Le `switch` compare une valeur à des **cas fixes** (`case 0`, `case "NOUVELLE"`). Des tranches ouvertes (« entre 1 et 9 », « 20 et plus ») ne s'y expriment pas proprement. Règle de choix : valeurs précises → `switch` ; plages ou conditions multiples → `if/else`. Le `switch` expression reste le bon outil dès la partie 2 avec les `enum` de statuts (`NOUVELLE`, `EN_COURS`, `RESOLUE`).

**Le mot `static`** : ignorons-le pour l'instant (il sera entièrement expliqué en leçon 05). Ce qu'il faut retenir : avec `static`, `diagnostic(...)` est appelable directement depuis `main`.

## Checklist de validation (récapitulatif)

- [ ] J'écris un `if/else if/else` et je combine avec `&&` et `||`.
- [ ] J'écris un `switch` expression avec `->` et je sais quand préférer `if/else` (tranches).
- [ ] Je choisis la bonne boucle selon la situation (index nécessaire ? valeur seulement ? condition d'arrêt ?).
- [ ] J'explique `break` et `continue` et je les utilise à bon escient.
- [ ] Je boucle avec `<` et non `<=` sur un tableau (dernier indice = length - 1).
- [ ] Je détecte une boucle infinie (compteur jamais mis à jour).
- [ ] J'aplatis des conditions imbriquées avec des early returns.

## 💡 Conseils

1. **Testez les cas limites** : appelez `diagnostic(0)`, `diagnostic(9)`, `diagnostic(10)`, `diagnostic(19)`, `diagnostic(20)` — si un message est faux à une frontière, votre condition est décalée d'un cran.
2. **Le tableau parallèle** (deux tableaux reliés par l'indice) marche, mais c'est fragile : si vous oubliez de trier les deux ensemble, les données se désynchronisent. C'est exactement le problème que résoudra la **classe `Reclamation`** en leçon 04 — gardez cette frustration en mémoire, elle va payer !
3. Si votre boucle affiche un élément de trop ou de trop peu, vérifiez d'abord les bornes (`<` vs `<=`, indice de départ).

➡️ **Prochaine étape** : leçon 03 — tableaux et `String` en profondeur, pour manipuler proprement les groupes de données et le texte.



<!-- SUITE-CORR2A -->

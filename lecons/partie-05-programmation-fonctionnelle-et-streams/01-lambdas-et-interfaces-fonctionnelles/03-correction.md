# Correction détaillée — Exercice 01 « Outiller SignalCUA avec des lambdas »

> 🧭 **Comment ce fichier s'articule** : vous venez de tenter `02-exercice.md`. Voici la solution complète, les choix expliqués, la **sortie réellement obtenue** (Java 21), les erreurs fréquentes, la checklist et des conseils. Si votre programme produit la même sortie, l'objectif est atteint.

## Correction pas a pas

### Les fichiers du projet

Six fichiers, dans le même dossier :

| Fichier | Rôle |
|---|---|
| `Priorite.java` | l'`enum` des urgences (rappel partie 2) |
| `StatutReclamation.java` | l'`enum` des statuts (rappel partie 2) |
| `Reclamation.java` | la donnée + `dateDeclaration` (`LocalDateTime`, rappel partie 3) |
| `FiltreReclamation.java` | l'interface fonctionnelle **métier** (`@FunctionalInterface`) |
| `OutilsReclamations.java` | la méthode `selectionner` qui **reçoit** le filtre |
| `Main.java` | la démonstration exécutable |

### Le socle commun : imports + jeu de données + aide d'affichage

Toutes les étapes ci-dessous partagent ce point de départ (à écrire **une fois** en haut de `Main.java`) :

```java
import java.time.LocalDateTime; // rappel partie 3 : construire des dates fixes
import java.util.ArrayList; // une liste modifiable (on va la trier)
import java.util.Comparator; // l'interface fonctionnelle « comparer deux objets »
import java.util.List; // l'interface des listes
import java.util.function.Consumer; // standard : consomme sans rien rendre (accept)
import java.util.function.Function; // standard : transforme T -> R (apply)
import java.util.function.Predicate; // standard : teste T -> boolean (test)
import java.util.function.Supplier; // standard : fournit sans entrée (get)

public class Main {
    public static void main(String[] args) {
        // 6 réclamations de démonstration (dates FIXES pour un tri déterministe).
        List<Reclamation> toutes = new ArrayList<>(List.of(
                new Reclamation(1, "Medina", Priorite.URGENTE, StatutReclamation.NOUVELLE,
                        "Nid de poule dangereux", LocalDateTime.of(2026, 10, 1, 9, 0)),
                new Reclamation(2, "Plateau", Priorite.NORMALE, StatutReclamation.EN_COURS,
                        "Poubelles non ramassees", LocalDateTime.of(2026, 9, 28, 14, 30)),
                new Reclamation(3, "Medina", Priorite.HAUTE, StatutReclamation.NOUVELLE,
                        "Lampadaire eteint", LocalDateTime.of(2026, 10, 3, 8, 15)),
                new Reclamation(4, "Fann", Priorite.URGENTE, StatutReclamation.NOUVELLE,
                        "Fuite d'eau", LocalDateTime.of(2026, 10, 2, 18, 45)),
                new Reclamation(5, "Plateau", Priorite.BASSE, StatutReclamation.RESOLUE,
                        "Graffiti", LocalDateTime.of(2026, 9, 20, 10, 0)),
                new Reclamation(6, "Medina", Priorite.NORMALE, StatutReclamation.EN_COURS,
                        "Trottoir encombre", LocalDateTime.of(2026, 9, 30, 11, 20))));
        // ... les étapes 3 à 6 s'écrivent ici, dans l'ordre ...
    }

    // Affiche les identifiants sur une ligne : [3, 1, 2] (lisible pour vérifier un tri).
    private static void afficherIds(List<Reclamation> liste) {
        StringBuilder sb = new StringBuilder("["); // un CONSTRUCTEUR DE TEXTE (voir ci-dessous)
        for (int i = 0; i < liste.size(); i++) { // parcours par index pour gérer les virgules
            if (i > 0) {
                sb.append(", "); // une virgule AVANT chaque élément sauf le premier
            }
            sb.append(liste.get(i).getId()); // l'identifiant courant
        }
        sb.append("]"); // on ferme le crochet
        System.out.println(sb); // on affiche la ligne construite
    }
}
```

**`StringBuilder` — le seul nouvel outil de ce fichier :** c'est un **constructeur de texte modifiable** (classe `java.lang`, donc sans import). Concaténer des `String` avec `+` dans une boucle crée un objet intermédiaire à chaque tour (rappel partie 1 : les `String` sont **immuables**) ; `StringBuilder` accumule dans un **tampon interne** (`append(...)` = « ajoute au bout ») puis on l'affiche d'un coup. Pour 6 éléments, `+` suffirait ; `StringBuilder` est le réflexe pro dès qu'on construit du texte en boucle.

### Étape 1 — L'interface fonctionnelle métier

```java
// FiltreReclamation.java - UNE seule méthode abstraite : c'est une SAM, donc une cible de lambda.
@FunctionalInterface // le compilateur VERIFIE la contrainte (protection contre les ajouts futurs)
interface FiltreReclamation { // un nom METIER : « qui dit si une réclamation m'intéresse »
    boolean accepter(Reclamation r); // le contrat : vrai = « je la garde »
}
```

**Le choix technique :** on aurait pu utiliser directement `Predicate<Reclamation>`. Mais `FiltreReclamation` **raconte le métier** : dans une signature `selectionner(toutes, filtre)`, le nom dit ce qui se passe. Réservez les standard aux cas génériques, les customs aux intentions métier (bonne pratique n°2 de la leçon).

### Étape 2 — La méthode qui reçoit un comportement

```java
import java.util.ArrayList; // une liste modifiable pour accumuler les retenues
import java.util.List; // l'interface des listes (rappel partie 3)

// OutilsReclamations.java - le cadre FIXE, le critère INJECTÉ.
class OutilsReclamations {
    static List<Reclamation> selectionner(List<Reclamation> toutes, FiltreReclamation filtre) {
        List<Reclamation> gardees = new ArrayList<>(); // une liste vide pour les retenues
        for (Reclamation r : toutes) { // on parcourt TOUTES les reclamations
            if (filtre.accepter(r)) { // on applique LE FILTRE RECU (c'est lui qui decide)
                gardees.add(r); // si oui, on la garde
            }
        }
        return gardees; // on rend la liste des retenues
    }
}
```

**Le choix technique :** `selectionner` est `static` car elle ne dépend d'**aucun état** (partie 1, leçon 05 : une méthode sans état d'instance peut être statique). Notez qu'**aucun critère** n'est écrit en dur : la méthode ne sait même pas ce que « urgent » veut dire.

### Étape 3 — Trois filtres à l'appel

```java
Consumer<Reclamation> afficher = System.out::println; // method reference : on CITE println

// --- 3.1 : les URGENTE (lambda simple : un paramètre, un test) ---
System.out.println("== Urgentes ==");
List<Reclamation> urgentes =
        OutilsReclamations.selectionner(toutes, r -> r.getPriorite() == Priorite.URGENTE);
urgentes.forEach(afficher); // chaque retenue est affichée

// --- 3.2 : le quartier visé (capture effectively final) ---
System.out.println("== Medina ==");
String quartierVise = "Medina"; // jamais réassignée APRÈS : la capture est LÉGALE
List<Reclamation> deMedina =
        OutilsReclamations.selectionner(toutes, r -> quartierVise.equals(r.getQuartier()));
deMedina.forEach(afficher);

// --- 3.3 : URGENTE et Medina (prédicats COMPOSÉS, pas de && manuel) ---
System.out.println("== Urgentes de Medina ==");
Predicate<Reclamation> estUrgente = r -> r.getPriorite() == Priorite.URGENTE;
Predicate<Reclamation> estDeMedina = r -> "Medina".equals(r.getQuartier());
Predicate<Reclamation> urgentEtMedina = estUrgente.and(estDeMedina); // les deux à la fois
// FiltreReclamation et Predicate ont la MÊME signature : on adapte via une lambda « pont »
List<Reclamation> urgentesMedina =
        OutilsReclamations.selectionner(toutes, r -> urgentEtMedina.test(r));
urgentesMedina.forEach(afficher);
```

**Les choix techniques :**

- `quartierVise.equals(...)` (et pas `r.getQuartier().equals(...)`) : si le quartier était `null`, la version « constante d'abord » **ne lève pas** de NPE (rappel partie 3, leçon 03).
- La lambda « pont » `r -> urgentEtMedina.test(r)` : `FiltreReclamation.accepter` et `Predicate.test` ont la **même forme** (un paramètre, un `boolean`). La lambda adapte l'un à l'autre. C'est volontaire : l'exercice montre que les interfaces standard et métier **cohabitent**.
- `forEach(afficher)` : `forEach` attend un `Consumer` ; `System.out::println` en est un. Pas de `x -> System.out.println(x)` (bonne pratique n°1).

### Étape 4 — Un tri multi-critères en une ligne

```java
// --- 4.1 : tri par identifiant (comparingInt : la variante pour clés int, sans boxing) ---
System.out.println("== Tri par id ==");
toutes.sort(Comparator.comparingInt(Reclamation::getId)); // -> [1, 2, 3, 4, 5, 6]
afficherIds(toutes);

// --- 4.2 : tri par quartier PUIS identifiant (enchaînement) ---
System.out.println("== Tri par quartier puis id ==");
toutes.sort(Comparator.comparing(Reclamation::getQuartier)
        .thenComparing(Reclamation::getId)); // -> [4, 1, 3, 6, 2, 5]
afficherIds(toutes); // Fann(4) ; Medina(1, 3, 6) ; Plateau(2, 5)

// --- 4.3 : les plus récentes d'abord (clé date + reversed) ---
System.out.println("== Plus recentes d'abord ==");
toutes.sort(Comparator.comparing(Reclamation::getDateDeclaration).reversed()); // -> [3, 4, 1, 6, 2, 5]
afficherIds(toutes); // 03/10, 02/10, 01/10, 30/09, 28/09, 20/09
```

**Le choix technique :** `comparingInt` plutôt que `comparing` pour une clé `int` : il compare les primitifs directement, **sans boxing** `int` → `Integer` (partie 1). Sur quelques éléments, c'est négligeable ; sur des milliers, ça compte — et c'est gratuit.

### Étape 5 — Transformer et composer : `Function` + `andThen`

```java
// --- ETAPE 5 : Function chaînée (quartier PUIS longueur) ---
System.out.println("== Longueur du quartier ==");
Function<Reclamation, String> quartierDe = Reclamation::getQuartier; // le traducteur 1
Function<String, Integer> longueur = String::length; // le traducteur 2
Function<Reclamation, Integer> longueurQuartier = quartierDe.andThen(longueur); // la chaîne
for (Reclamation r : toutes) { // on applique la chaîne à chacune
    System.out.println("id=" + r.getId() + " -> " + longueurQuartier.apply(r));
}
```

**Le choix technique :** `andThen` (« puis ») construit une **nouvelle** fonction sans toucher aux deux d'origine (rappel partie 3 : les objets bien conçus sont **immuables**). L'ordre d'affichage suit le tri précédent (`[3, 4, 1, 6, 2, 5]`) : `andThen` ne trie rien, il **transforme**.

### Étape 6 (bonus) — Le `Supplier` de secours

```java
// --- ETAPE 6 (bonus) : le fournisseur de secours ---
System.out.println("== Secours ==");
Supplier<Reclamation> secours = () -> new Reclamation(0, "Inconnu", Priorite.BASSE,
        StatutReclamation.NOUVELLE, "a qualifier", LocalDateTime.of(2026, 10, 5, 12, 0));
Reclamation s1 = secours.get(); // premier appel : un objet NEUF
Reclamation s2 = secours.get(); // deuxième appel : un AUTRE objet neuf
System.out.println(s1);
System.out.println(s2);
System.out.println("deux objets distincts (s1 == s2) ? " + (s1 == s2)); // -> false
```

**Le choix technique :** chaque `.get()` exécute le corps `() -> new ...` : deux appels = deux `new` = deux **références** différentes, donc `==` rend `false` (rappel partie 1 : `==` compare les références pour les objets). C'est exactement le motif `orElseGet(() -> ...)` croisé avec `Optional` (partie 3) : fournir une valeur de secours **à la demande**, pas avant.

## Verification par exécution

Compilation et exécution réelles (Java 21) :

```text
javac -d out Priorite.java StatutReclamation.java Reclamation.java FiltreReclamation.java OutilsReclamations.java Main.java
java -cp out Main
```

Sortie obtenue (identique à ce que votre programme doit afficher) :

```text
== Urgentes ==
Reclamation{id=1, quartier='Medina', priorite=URGENTE, statut=NOUVELLE, description='Nid de poule dangereux', dateDeclaration=2026-10-01T09:00}
Reclamation{id=4, quartier='Fann', priorite=URGENTE, statut=NOUVELLE, description='Fuite d'eau', dateDeclaration=2026-10-02T18:45}
== Medina ==
Reclamation{id=1, quartier='Medina', priorite=URGENTE, statut=NOUVELLE, description='Nid de poule dangereux', dateDeclaration=2026-10-01T09:00}
Reclamation{id=3, quartier='Medina', priorite=HAUTE, statut=NOUVELLE, description='Lampadaire eteint', dateDeclaration=2026-10-03T08:15}
Reclamation{id=6, quartier='Medina', priorite=NORMALE, statut=EN_COURS, description='Trottoir encombre', dateDeclaration=2026-09-30T11:20}
== Urgentes de Medina ==
Reclamation{id=1, quartier='Medina', priorite=URGENTE, statut=NOUVELLE, description='Nid de poule dangereux', dateDeclaration=2026-10-01T09:00}
== Tri par id ==
[1, 2, 3, 4, 5, 6]
== Tri par quartier puis id ==
[4, 1, 3, 6, 2, 5]
== Plus recentes d'abord ==
[3, 4, 1, 6, 2, 5]
== Longueur du quartier ==
id=3 -> 6
id=4 -> 4
id=1 -> 6
id=6 -> 6
id=2 -> 7
id=5 -> 7
== Secours ==
Reclamation{id=0, quartier='Inconnu', priorite=BASSE, statut=NOUVELLE, description='a qualifier', dateDeclaration=2026-10-05T12:00}
Reclamation{id=0, quartier='Inconnu', priorite=BASSE, statut=NOUVELLE, description='a qualifier', dateDeclaration=2026-10-05T12:00}
deux objets distincts (s1 == s2) ? false
```

Lisez la sortie comme une preuve : les **3 sélections** retiennent les bons sous-ensembles (1+4 urgentes ; 1+3+6 de Medina ; 1 seule « urgente de Medina »), les **3 tris** donnent les bons ordres, les longueurs (Medina=6, Fann=4, Plateau=7) sont exactes, et le `Supplier` fournit bien deux objets distincts.

## Erreurs frequentes et comment les reconnaitre

- **`variable used in lambda expression should be final or effectively final`** → vous avez réassigné `quartierVise` après sa capture. Ne la touchez plus après la première affectation (piège 2 de la leçon).
- **`incompatible types: Priorite cannot be converted to boolean`** → votre lambda de `Predicate` rend le mauvais type (piège 1). Ajoutez la comparaison (`== Priorite.URGENTE`).
- **`reference to traiter is ambiguous`** → deux surcharges collent à votre lambda (piège 5). Typez une variable intermédiaire.
- **Le tri « par quartier puis id » semble faux** → vous avez oublié le `thenComparing` et trié deux fois : le second tri **écrase** le premier. `sort` ne « cumule » pas : enchaînez **dans** le comparateur.
- **`s1 == s2` rend `true`** → vous avez stocké l'objet dans une variable puis retourné la même : le `Supplier` doit contenir le `new` **dans** la lambda, pas avant.

## Checklist de validation

- [ ] `FiltreReclamation` porte `@FunctionalInterface` et une seule méthode abstraite — et vous savez dire pourquoi.
- [ ] `selectionner` ne contient aucun critère en dur ; le comportement vient du paramètre.
- [ ] Vous écrivez une capture *effectively final* sans erreur et expliquez la règle.
- [ ] Vous composez `Predicate` (`and`/`or`/`negate`) et `Function` (`andThen`) au lieu de réécrire la logique.
- [ ] Vous triez en une ligne avec `comparing(...).thenComparing(...).reversed()`.
- [ ] Vous convertissez `x -> System.out.println(x)` en `System.out::println` par réflexe.
- [ ] Vous expliquez pourquoi chaque `.get()` du `Supplier` crée un nouvel objet.

## Conseils pour progresser

1. **Rejouez l'étape 3.3 sans la lambda « pont »** : changez la signature de `selectionner` pour accepter directement un `Predicate<Reclamation>`. Comparez : quand l'interface métier apporte-t-elle vraiment quelque chose ?
2. **Provoquez volontairement les erreurs** de la section précédente (réassignez `quartierVise`, rendez un `Priorite` dans un `Predicate`) et lisez le message du compilateur : c'est en le voyant **une fois** qu'on le reconnaît ensuite en 5 secondes.
3. **Préparez la leçon 02** : tout ce que `selectionner` fait à la main (parcourir, tester, accumuler) s'écrira `toutes.stream().filter(...).toList()`. Devinez déjà à quoi sert chaque morceau.
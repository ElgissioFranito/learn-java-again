# Exercice 01 — Outiller SignalCUA avec des lambdas

> 🧭 **Comment ce fichier s'articule** : la leçon (`01-lecon.md`) a expliqué les interfaces fonctionnelles, les 4 standard, les method references et la capture *effectively final*. Ici, vous construisez la **boîte à outils comportementale** de SignalCUA : une interface fonctionnelle métier, des tris expressifs et des prédicats composés. La correction (`03-correction.md`) suit votre tentative.

---

## 🎯 Objectif de l'exercice

Donner au registre SignalCUA des **filtres et tris à la carte** : une interface `FiltreReclamation` injectée dans une méthode, des `Predicate<Reclamation>` composés (`and`/`or`/`negate`), un tri multi-critères en une ligne, et des method references là où elles simplifient.

**Fichiers de départ** : vos `Priorite` (enum), `StatutReclamation` (enum) et `Reclamation` (issus des parties 2-3). Si vous ne les avez plus, recréez ce modèle minimal :

```java
enum Priorite { URGENTE, HAUTE, NORMALE, BASSE } // l'ordre compte : on triera dessus plus tard
enum StatutReclamation { NOUVELLE, EN_COURS, RESOLUE }

class Reclamation {
    private final int id; // identifiant unique, jamais modifié
    private final String quartier; // quartier concerné, jamais modifié
    private final Priorite priorite; // niveau d'urgence, jamais modifié
    private final StatutReclamation statut; // état d'avancement
    private final String description; // texte libre du signalement
    // + constructeur, getters getId()/getQuartier()/getPriorite()/getStatut()/getDescription(), toString()
}
```

## 📋 Enoncé

### Étape 1 — L'interface fonctionnelle métier

Créez `FiltreReclamation`, une interface fonctionnelle métier :

```java
@FunctionalInterface
interface FiltreReclamation {
    boolean accepter(Reclamation r); // vrai = « je la garde »
}
```

### Étape 2 — La méthode qui reçoit un comportement

Ajoutez une méthode utilitaire (dans une classe `OutilsReclamations` ou en `static` dans votre `Main`) :

```java
static List<Reclamation> selectionner(List<Reclamation> toutes, FiltreReclamation filtre)
```

Contraintes : **aucun critère en dur** à l'intérieur ; la méthode applique uniquement `filtre.accepter(r)`.

### Étape 3 — Trois filtres à l'appel (lambdas expressives)

Dans un `main`, construisez 6 réclamations de démonstration (quartiers `Medina`, `Plateau`, `Fann` ; priorités variées) puis écrivez :

1. les réclamations `URGENTE` (lambda simple : `r -> ...`) ;
2. celles du quartier `Medina` (capture d'une variable locale `quartierVise`, **jamais réassignée**) ;
3. celles qui sont `URGENTE` **et** de `Medina` (composez avec des `Predicate` et `and`, **sans** réécrire le test à la main).

Affichez chaque résultat avec `System.out::println` (method reference).

### Étape 4 — Un tri multi-critères en une ligne

Toujours dans le `main` :

1. Triez par identifiant (`Comparator.comparingInt(Reclamation::getId)`).
2. Triez par quartier **puis** par identifiant (`comparing(...).thenComparing(...)`).
3. Triez les plus récentes d'abord (il faut une `dateDeclaration` de type `LocalDateTime` sur `Reclamation`, rappel partie 3, puis `.reversed()`).

Affichez les identifiants après chaque tri (par exemple `[3, 1, 2]`).

### Étape 5 — Transformer et composer : `Function` + `andThen`

Écrivez :

```java
Function<Reclamation, String> quartierDe = Reclamation::getQuartier; // method reference
Function<String, Integer> longueur = String::length; // method reference
Function<Reclamation, Integer> longueurQuartier = quartierDe.andThen(longueur); // chaîne
```

Puis affichez la longueur du quartier de chaque réclamation via `longueurQuartier.apply(r)`.

### Étape 6 (bonus) — Le `Supplier` de secours

Écrivez un `Supplier<Reclamation>` qui fournit une réclamation « à qualifier » (id `0`, quartier `"Inconnu"`, priorité `BASSE`, statut `NOUVELLE`, description `"à qualifier"`). Appelez `.get()` deux fois et affichez : vous devez obtenir **deux objets distincts** (prouvez-le par `==`, qui doit rendre `false` — rappel partie 1 : `==` compare les **références** pour les objets).

## ✅ Criteres de reussite

- [ ] `FiltreReclamation` porte `@FunctionalInterface` et **une seule** méthode abstraite.
- [ ] `selectionner` ne contient **aucun critère en dur** (le filtre vient du paramètre).
- [ ] Les 3 sélections affichent les bons sous-ensembles (vérifié à l'exécution).
- [ ] La capture de `quartierVise` compile (variable **jamais réassignée**).
- [ ] Le filtre combiné utilise `Predicate.and(...)` (pas un `&&` réécrit à la main).
- [ ] Les 3 tris affichent les bons ordres d'identifiants.
- [ ] `longueurQuartier` est construit par `andThen`, **sans** lambda intermédiaire manuelle.
- [ ] Le bonus montre deux appels `.get()` donnant deux objets (`==` rend `false`).
- [ ] Les affichages passent par `System.out::println` (method reference, pas `x -> System.out.println(x)`).

## 💡 Indications (lisez seulement si bloqué)

- Une méthode `static` dans `Main` suffit pour `selectionner` : pas besoin d'une classe dédiée.
- Pour le `Predicate` combiné : convertissez vos lambdas en `Predicate<Reclamation>` typés, puis `.and(...)`.
- `LocalDateTime.of(2026, 10, 1, 9, 0)` construit une date fixe (rappel partie 3) — idéal pour un tri déterministe.
- Pour afficher juste les identifiants : une petite boucle `for` suffit (`System.out.print(r.getId() + " ")`).
- `==` sur deux objets compare les **adresses mémoire** (références), pas le contenu : deux `new` donnent toujours `false`.

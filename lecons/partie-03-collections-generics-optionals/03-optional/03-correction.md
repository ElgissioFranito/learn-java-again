# Correction détaillée — Exercice 03 « Le registre passe à `Optional` »

> 🧭 **Comment ce fichier s'articule** : vous venez de tenter `02-exercice.md`. Voici la solution complète, les choix expliqués, la **sortie réellement obtenue**, les erreurs fréquentes, la checklist et des conseils. Si votre programme produit la même sortie, l'objectif est atteint.

## Correction pas à pas

### Étape 1 et 2 — Le registre : trois recherches qui renvoient `Optional`

```java
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

// Correction de l'exercice 03 : le registre de l'exercice 01, mais avec Optional
public class RegistreReclamations {

    private final List<Reclamation> toutes = new ArrayList<>();
    private final Map<Integer, Reclamation> parId = new HashMap<>();
    private final Map<String, List<Reclamation>> parQuartier = new HashMap<>();
    private int prochainId = 1;

    public Reclamation ajouter(String description, String quartier, Priorite priorite) {
        Reclamation r = new Reclamation(prochainId, description, quartier, priorite);
        prochainId++;
        toutes.add(r);
        parId.put(r.getId(), r);
        List<Reclamation> duQuartier = parQuartier.get(quartier);
        if (duQuartier == null) {
            duQuartier = new ArrayList<>();
            parQuartier.put(quartier, duQuartier);
        }
        duQuartier.add(r);
        return r;
    }

    // ETAPE 1 : l'absence est desormais dans le type de retour
    public Optional<Reclamation> findById(int id) {
        return Optional.ofNullable(parId.get(id));
    }

    // ETAPE 2a : la premiere urgence, ou rien
    public Optional<Reclamation> premiereUrgente() {
        for (Reclamation r : toutes) {
            if (r.getPriorite() == Priorite.URGENTE) {
                return Optional.of(r);
            }
        }
        return Optional.empty();        // "rien trouve" est un cas NORMAL, pas une erreur
    }

    // ETAPE 2b : la premiere du quartier, ou rien
    public Optional<Reclamation> premiereDuQuartier(String quartier) {
        List<Reclamation> liste = parQuartier.get(quartier);
        if (liste == null || liste.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(liste.get(0));
    }

    public List<Reclamation> toutes() {
        return List.copyOf(toutes);
    }

    public int taille() {
        return toutes.size();
    }
}
```

**Les trois choix techniques de cet extrait :**

1. **`findById` : une seule ligne.** `Optional.ofNullable(parId.get(id))` remplace le `return parId.get(id)` de l'exercice 01. Le comportement d'indexation (`HashMap`) ne change pas : c'est **le contrat** qui change, pas la mécanique.
2. **`premiereUrgente` : `Optional.empty()` et non `null`.** Une recherche qui ne trouve rien n'est **pas une erreur** : c'est un résultat. Le typer ainsi oblige l'appelant à décider (repli, exception, action).
3. **`premiereDuQuartier` : deux causes d'absence** (quartier inconnu → `liste == null` ; quartier connu mais vide → `liste.isEmpty()`). Les deux sont traitées par le **même** `Optional.empty()` : c'est tout l'intérêt d'avoir **une seule** représentation de l'absence. Dans l'exercice 01, ce cas renvoyait une liste vide ; ici, la méthode « première » cherche **un** élément, donc l'absence se dit en `Optional`.

### Étape 3 et 4 — Le service : traduire l'absence sans jamais écrire `if`

```java
// Correction de l'exercice 03 : le service qui traduit l'absence en exception METIER
public class ServiceReclamations {

    private final RegistreReclamations registre;      // dependance passee au constructeur

    public ServiceReclamations(RegistreReclamations registre) {
        this.registre = registre;
    }

    // ETAPE 3 : l'absence devient une exception expliquee
    public Reclamation recupererObligatoirement(int id) {
        return registre.findById(id)
                .orElseThrow(() -> new IllegalStateException("Reclamation " + id + " introuvable"));
    }

    // ETAPE 4a : map + orElse (aucun if)
    public String descriptionOuInconnue(int id) {
        return registre.findById(id).map(r -> r.getDescription()).orElse("inconnue");
    }

    // ETAPE 4b : filter (existe ET urgente)
    public boolean estUrgente(int id) {
        return registre.findById(id)
                .filter(r -> r.getPriorite() == Priorite.URGENTE)
                .isPresent();
    }

    // ETAPE 4c : flatMap (la recherche suivante rend elle-meme un Optional)
    public int idSuivant(int id) {
        return registre.findById(id)
                .flatMap(r -> registre.findById(r.getId() + 1))
                .map(r -> r.getId())
                .orElse(-1);
    }

    // ETAPE 4d : ifPresent (agir sans extraire)
    public void demarrerSiPresente(int id) {
        registre.findById(id).ifPresent(r -> r.demarrerTraitement());
    }
}
```

**Pourquoi cette classe est importante** : elle sépare deux responsabilités.

- Le **registre** stocke et **constate** l'absence (`Optional`).
- Le **service** décide de la **règle métier** : « pour l'API publique, l'absence est une **erreur** » (`orElseThrow`), « pour l'affichage, l'absence a une **valeur de repli** » (`orElse`).

C'est très exactement la séparation que vous retrouverez en partie 7 : le contrôleur web décidera qu'une absence devient un **404**, le service lèvera une exception métier, et le dépôt Spring renverra `Optional`.

**Deux points de style, typiques d'une revue de code :**

- **La dépendance passe par le constructeur** : `ServiceReclamations(registre)`. C'est déjà de l'injection de dépendances « à la main » — celle que Spring automatisera en partie 7 (`@Service`, `@RequiredArgsConstructor`).
- **`idSuivant` enchaîne `flatMap` puis `map` puis `orElse`** : trois lignes qui décrivent *quoi* faire, jamais *comment* tester le vide. Comparez avec la version « ancienne » qui aurait exigé trois `if (x != null)` imbriqués.

### Étape 5 et 6 — Le programme de démonstration

```java
import java.util.Optional;

public class MainService {

    // Etape 5 : une methode qui rend son appel VISIBLE
    private static String descriptionParDefaut() {
        System.out.println("      >> (calcul effectue)");
        return "inconnue";
    }

    public static void main(String[] args) {

        RegistreReclamations registre = new RegistreReclamations();
        ServiceReclamations service = new ServiceReclamations(registre);

        registre.ajouter("Nid de poule", "Medina", Priorite.NORMALE);          // #1
        registre.ajouter("Lampadaire éteint", "Plateau", Priorite.URGENTE);    // #2
        registre.ajouter("Poubelles non ramassées", "Medina", Priorite.BASSE); // #3

        // ============ 1. Les trois recherches renvoient Optional ============
        System.out.println("1) findById(2) present ? " + service.recupererObligatoirement(2).getId()
                + " | findById(42) present ? " + registre.findById(42).isPresent());
        System.out.println("   premiereUrgente -> "
                + registre.premiereUrgente().map(r -> r.getDescription()).orElse("aucune"));
        System.out.println("   premiereDuQuartier(\"Medina\") -> "
                + registre.premiereDuQuartier("Medina").map(r -> r.getDescription()).orElse("aucune"));
        System.out.println("   premiereDuQuartier(\"Fann\") -> "
                + registre.premiereDuQuartier("Fann").map(r -> r.getDescription()).orElse("aucune"));

        // ============ 2. L'absence traduite en exception METIER ============
        try {
            service.recupererObligatoirement(42);
        } catch (IllegalStateException e) {
            System.out.println("2) recupererObligatoirement(42) -> " + e.getClass().getSimpleName()
                    + " : " + e.getMessage());
        }

        // ============ 3. Manipuler l'absence sans if ============
        System.out.println("3) descriptionOuInconnue(1)  -> " + service.descriptionOuInconnue(1));
        System.out.println("   descriptionOuInconnue(42) -> " + service.descriptionOuInconnue(42));
        System.out.println("   estUrgente(2) -> " + service.estUrgente(2)
                + " | estUrgente(1) -> " + service.estUrgente(1)
                + " | estUrgente(42) -> " + service.estUrgente(42));
        System.out.println("   idSuivant(1) -> " + service.idSuivant(1)
                + " | idSuivant(3) -> " + service.idSuivant(3));
        service.demarrerSiPresente(3);
        service.demarrerSiPresente(42);          // rien ne se passe : cas normal
        System.out.println("   apres demarrerSiPresente(3) : statut #3 = "
                + registre.findById(3).map(r -> r.getStatut().toString()).orElse("?")
                + " | #1 inchange = " + registre.findById(1).map(r -> r.getStatut().toString()).orElse("?"));

        // ============ 4. Etape 5 : orElse (avide) contre orElseGet (paresseux) ============
        String avecOrElse = registre.findById(1).map(r -> r.getDescription())
                .orElse(descriptionParDefaut());
        System.out.println("4) orElse(...)    -> " + avecOrElse);

        String avecOrElseGet = registre.findById(1).map(r -> r.getDescription())
                .orElseGet(() -> descriptionParDefaut());
        System.out.println("   orElseGet(...) -> " + avecOrElseGet
                + "  (aucun \"(calcul effectue)\" ci-dessus : orElseGet n'a pas appele la methode)");

        // ============ 5. Bonus : les deux erreurs provoquées ============
        try {
            Optional.of(null);
        } catch (NullPointerException e) {
            System.out.println("5) Optional.of(null) -> " + e.getClass().getSimpleName()
                    + " (au moment de la CREATION)");
        }
        try {
            registre.findById(42).orElseThrow();
        } catch (java.util.NoSuchElementException e) {
            System.out.println("   orElseThrow() sans argument -> " + e.getClass().getSimpleName()
                    + " : " + e.getMessage());
        }
    }
}
```

## Vérification par exécution

```bash
javac -encoding UTF-8 *.java      # StatutReclamation, Priorite, Reclamation, RegistreReclamations, ServiceReclamations, MainService
java MainService
```

**Résultat obtenu (Java 21.0.7), aucune erreur à la compilation :**

```text
1) findById(2) present ? 2 | findById(42) present ? false
   premiereUrgente -> Lampadaire éteint
   premiereDuQuartier("Medina") -> Nid de poule
   premiereDuQuartier("Fann") -> aucune
2) recupererObligatoirement(42) -> IllegalStateException : Reclamation 42 introuvable
3) descriptionOuInconnue(1)  -> Nid de poule
   descriptionOuInconnue(42) -> inconnue
   estUrgente(2) -> true | estUrgente(1) -> false | estUrgente(42) -> false
   idSuivant(1) -> 2 | idSuivant(3) -> -1
   apres demarrerSiPresente(3) : statut #3 = EN_COURS | #1 inchange = NOUVELLE
      >> (calcul effectue)
4) orElse(...)    -> Nid de poule
   orElseGet(...) -> Nid de poule  (aucun "(calcul effectue)" ci-dessus : orElseGet n'a pas appele la methode)
5) Optional.of(null) -> NullPointerException (au moment de la CREATION)
   orElseThrow() sans argument -> NoSuchElementException : No value present
```

**Lecture ligne par ligne :**

| Ligne | Ce qui est prouvé |
|---|---|
| 1 | `findById(2)` fonctionne (retour explicite), `findById(42)` est **présent=false** sans exception. |
| 1 bis | `premiereUrgente()` trouve l'urgence `#2`, `premiereDuQuartier("Medina")` trouve `#1`, et un quartier **inconnu** donne `aucune` — **trois cas d'absence gérés sans un seul `if`**. |
| 2 | L'absence devient une **exception métier lisible** : `IllegalStateException : Reclamation 42 introuvable`. |
| 3 | `estUrgente` combine **existence ET condition** : `true` pour `#2`, `false` pour `#1` (existe mais non urgente) et pour `#42` (n'existe pas). |
| 3 bis | `idSuivant(1)` → `2` (la suivante existe) ; `idSuivant(3)` → `-1` (rien après) : c'est `flatMap` qui a rendu ce cas possible sans imbriquer deux `Optional`. |
| 3 ter | `demarrerSiPresente(3)` a démarré `#3` (statut `EN_COURS`) ; `demarrerSiPresente(42)` **ne fait rien** — et c'est le comportement attendu : pas d'exception pour une absence, car ici l'absence n'est pas une erreur. `#1` reste `NOUVELLE`, preuve que les actions sont bien ciblées. |
| 4 | **LA preuve avide/paresseux** : `>> (calcul effectue)` apparaît **avant** la ligne `4)`, alors que la réclamation existait. `orElseGet` n'a rien déclenché. |
| 5 | `Optional.of(null)` échoue **à la création** (pas à l'utilisation) ; `orElseThrow()` nu produit `NoSuchElementException : No value present` — un message **technique**, sans contexte métier : à éviter. |

> ℹ️ **Remarquez le statut affiché** : `EN_COURS` est le `name()` de la constante d'`enum` (leçon 05 de la partie 2). Plus tard, en partie 7, l'API renverra plutôt le libellé (« En cours de traitement ») — c'est un choix de présentation, pas de modèle.

## Erreurs fréquentes et comment les reconnaître

| Message / symptôme | Cause | Correction |
|---|---|---|
| `NullPointerException` **à la création** d'un `Optional` | `Optional.of(x)` avec `x == null` | `Optional.ofNullable(x)` |
| `NoSuchElementException: No value present` | `get()` ou `orElseThrow()` nu sur un `Optional` vide | `orElse`, `orElseGet`, `orElseThrow(() -> …)` |
| `incompatible types: Optional<Reclamation> cannot be converted to Reclamation` | vous gardez l'ancien type de retour | soit `.orElse…` côté appelant, soit changer le type annoncé |
| Le calcul de repli s'exécute pour rien | `orElse(calcul())` | `orElseGet(() -> calcul())` |
| `Optional<Optional<Reclamation>>` | `map` avec une fonction qui rend un `Optional` | `flatMap` |
| `cannot find symbol: method getDescription()` sur un `Optional` | vous appelez une méthode de `Reclamation` sur l'enveloppe | ajouter `.map(r -> …)` |
| La méthode de recherche renvoie `null` au lieu d'un `Optional` | un `return null` oublié dans une branche | `return Optional.empty();` |

## Checklist de validation

Reprenez chaque point **sur votre code** :

- [ ] `findById`, `premiereUrgente`, `premiereDuQuartier` renvoient `Optional<Reclamation>` (vérifié dans la signature).
- [ ] Aucun `return null` dans une méthode qui renvoie `Optional`.
- [ ] `recupererObligatoirement(42)` produit un message **métier** (il contient l'identifiant), pas un message technique.
- [ ] `descriptionOuInconnue`, `estUrgente`, `idSuivant`, `demarrerSiPresente` : **zéro** `if (x != null)` et **zéro** `get()`.
- [ ] `idSuivant` utilise bien `flatMap` (sinon il ne compile pas : `map` produirait `Optional<Optional<…>>`).
- [ ] `demarrerSiPresente(42)` (identifiant absent) **ne lève pas d'exception** : c'est un cas normal.
- [ ] La sortie du programme montre `>> (calcul effectue)` **une seule fois**, avant la ligne `4)` — donc `orElse` seul.
- [ ] `Optional.of(null)` et `orElseThrow()` nu sont reproduits **et** expliqués.
- [ ] Aucun `Optional` en champ, en paramètre, ni dans une collection.

## Conseils pour progresser

1. **Faites l'aller-retour `null` ↔ `Optional` volontairement.** Reprenez votre `findById` d'origine (avec `null`) et écrivez côte à côte les trois usages : repli, exception, action. Vous verrez que la version `Optional` fait **disparaître** les `if` — c'est le vrai bénéfice, plus que la « sécurité ».
2. **Questionnez chaque `null` de votre code** : « est-ce une absence **attendue** (→ `Optional` en retour) ou une **erreur de programmation** (→ `Objects.requireNonNull`, exception) ? ». La confusion entre les deux est la source de la plupart des NPE.
3. **Anticipez la partie 7** : quand Spring vous demandera de gérer un `404`, vous écrirez `orElseThrow(() -> new ReclamationIntrouvableException(id))` et un `@ControllerAdvice` la transformera en réponse HTTP. Vous venez d'écrire le cœur de ce mécanisme.
4. **Anticipez la partie 9** : un exercice de test à faire plus tard — `findById(42).isEmpty()` et `premiereUrgente()` sur un registre **vide**. Les chemins « rien trouvé » sont ceux qu'on oublie de tester… et ceux qui cassent en production.
5. **Retenez le couple `map` + `orElse`** comme le « bonjour le monde » d'`Optional`. Si vous ne devez retenir qu'une ligne de cette leçon, c'est celle-ci : `registre.findById(id).map(r -> r.getDescription()).orElse("inconnue")`.

➡️ **Suite de votre parcours** : votre registre sait maintenant dire « je n'ai pas trouvé » sans mentir. La **leçon 04 — Date and Time API** ajoute la dimension manquante à SignalCUA : **quand** la réclamation est arrivée, **quand** elle doit être traitée, et **comment** mesurer un retard — avec `java.time`, la seule API de dates à utiliser aujourd'hui.

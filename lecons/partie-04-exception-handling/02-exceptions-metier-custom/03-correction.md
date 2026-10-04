# Correction détaillée — Exercice 02 « Robustifier SignalCUA avec des exceptions métier »

> 🧭 **Comment ce fichier s'articule** : vous venez de tenter `02-exercice.md`. Voici la solution complète, les choix expliqués, la **sortie réellement obtenue** (Java 21), les erreurs fréquentes, la checklist et des conseils. Si votre programme produit la même sortie, l'objectif est atteint.

## Correction pas a pas

### Les fichiers du projet

Neuf fichiers, dans le même dossier :

| Fichier | Rôle |
|---|---|
| `SignalcuaException.java` | la **racine** de toutes les erreurs métier |
| `ReclamationNotFoundException.java` | identifiant inconnu (porte un `id`) |
| `ReclamationInvalideException.java` | donnée invalide |
| `TransitionStatutInterditeException.java` | changement d'état interdit |
| `StatutReclamation.java` | l'`enum` des statuts (rappel partie 2) |
| `Reclamation.java` | la donnée + ses transitions |
| `RegistreReclamations.java` | le stockage qui **lève** |
| `ServiceReclamations.java` | le service qui **propage** |
| `Main.java` | la démonstration exécutable |

### Étape 1 et 2 — La hiérarchie d'exceptions

```java
// SignalcuaException.java — racine commune de TOUTES les erreurs métier.
// "unchecked" : etend RuntimeException, donc AUCUN `throws` n'est impose.
public class SignalcuaException extends RuntimeException {

    public SignalcuaException(String message) {
        super(message);
    }

    public SignalcuaException(String message, Throwable cause) {
        super(message, cause); // on conserve la cause d'origine
    }
}
```

```java
// ReclamationNotFoundException.java — l'absence ATTENDUE devient une ERREUR nommee.
public class ReclamationNotFoundException extends SignalcuaException {

    private final int id; // le CONTEXTE : quelle reclamation ?

    public ReclamationNotFoundException(int id) {
        super("Réclamation " + id + " introuvable");
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
```

```java
// ReclamationInvalideException.java — une donnée fournie ne respecte pas une regle.
public class ReclamationInvalideException extends SignalcuaException {

    public ReclamationInvalideException(String message) {
        super(message);
    }

    public ReclamationInvalideException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

```java
// TransitionStatutInterditeException.java — on tente un changement d'etat interdit.
public class TransitionStatutInterditeException extends SignalcuaException {

    private final StatutReclamation de;
    private final StatutReclamation vers;

    public TransitionStatutInterditeException(StatutReclamation de, StatutReclamation vers) {
        super("Transition interdite : " + de + " -> " + vers);
        this.de = de;
        this.vers = vers;
    }

    public StatutReclamation getDe() { return de; }
    public StatutReclamation getVers() { return vers; }
}
```

**Les choix techniques de la hiérarchie :**

1. **Une seule racine (`SignalcuaException`).** Elle permet un `catch (SignalcuaException e)` qui attrape **toutes** les erreurs métier — le point d'entrée unique de l'Étape 4.
2. **Toutes *unchecked*.** Aucune signature `demarrer()`/`resoudre()` ne porte de `throws` : le code reste lisible, et la décision de réaction est **centralisée** à la frontière.
3. **Le contexte enrichit le message.** `ReclamationNotFoundException` **construit** son message à partir de l'`id` (`"Réclamation 42 introuvable"`) : impossible d'oublier de le fournir.

### Étape 3 — La `Reclamation` valide ses données et ses transitions

```java
// StatutReclamation.java — enum borne les trois etats possibles (rappel partie 2).
public enum StatutReclamation {
    NOUVELLE, EN_COURS, RESOLUE
}
```

```java
// Reclamation.java
public class Reclamation {

    private final int id;
    private final String quartier;
    private final String description;
    private StatutReclamation statut = StatutReclamation.NOUVELLE; // etat initial

    public Reclamation(int id, String quartier, String description) {
        if (id <= 0) {
            throw new ReclamationInvalideException("Identifiant invalide : " + id);
        }
        if (description == null || description.isBlank()) {
            throw new ReclamationInvalideException("Description obligatoire");
        }
        this.id = id;
        this.quartier = quartier;
        this.description = description;
    }

    public void demarrerTraitement() {
        if (statut != StatutReclamation.NOUVELLE) {
            throw new TransitionStatutInterditeException(statut, StatutReclamation.EN_COURS);
        }
        statut = StatutReclamation.EN_COURS;
    }

    public void marquerResolue() {
        if (statut != StatutReclamation.EN_COURS) {
            throw new TransitionStatutInterditeException(statut, StatutReclamation.RESOLUE);
        }
        statut = StatutReclamation.RESOLUE;
    }

    public int getId() { return id; }
    public String getQuartier() { return quartier; }
    public String getDescription() { return description; }
    public StatutReclamation getStatut() { return statut; }
}
```

**Pourquoi lever ici plutôt que retourner un code d'erreur ?** Parce qu'une transition interdite est un **fait impossible à ignorer** : le code appelant n'a **rien** de sensé à faire ensuite. Lever (`fail-fast`) empêche d'enchaîner sur un état incohérent.

### Étape 4 et 5 — Le registre qui lève, le service qui propage

```java
// RegistreReclamations.java
import java.util.HashMap;
import java.util.Map;

public class RegistreReclamations {

    private final Map<Integer, Reclamation> parId = new HashMap<>();
    private int prochainId = 1;

    public Reclamation ajouter(String quartier, String description) {
        Reclamation r = new Reclamation(prochainId++, quartier, description);
        parId.put(r.getId(), r);
        return r;
    }

    // Variante QUI LEVE : l'appelant n'a pas a gerer l'absence lui-meme.
    public Reclamation findById(int id) {
        Reclamation r = parId.get(id);
        if (r == null) {
            throw new ReclamationNotFoundException(id);
        }
        return r;
    }
}
```

```java
// ServiceReclamations.java — le service PROPAGE : aucun try/catch ici.
public class ServiceReclamations {

    private final RegistreReclamations registre;

    public ServiceReclamations(RegistreReclamations registre) {
        this.registre = registre;
    }

    public void demarrer(int id) {
        registre.findById(id).demarrerTraitement();
    }

    public void resoudre(int id) {
        registre.findById(id).marquerResolue();
    }
}
```

**Le point clé** : `findById` **lève**, le service **n'attrape pas** → l'exception remonte jusqu'au `main` (la frontière). C'est exactement ce que fera un contrôleur Spring en partie 7.

### Étape 6 — Le `main` qui attrape par type et par famille

```java
// Main.java
public class Main {

    public static void main(String[] args) {
        RegistreReclamations registre = new RegistreReclamations();
        ServiceReclamations service = new ServiceReclamations(registre);

        Reclamation r1 = registre.ajouter("Medina", "Nid de poule");
        registre.ajouter("Plateau", "Poubelles non ramassees");

        // 1) Cas nominal
        service.demarrer(r1.getId());
        service.resoudre(r1.getId());
        System.out.println("1) #" + r1.getId() + " -> " + r1.getStatut());

        // 2) Identifiant inconnu : exception PRÉCISE
        try {
            service.demarrer(99);
        } catch (ReclamationNotFoundException e) {
            System.out.println("2) " + e.getClass().getSimpleName() + " : " + e.getMessage()
                    + " (id=" + e.getId() + ")");
        }

        // 3) Transition invalide
        Reclamation r3 = registre.ajouter("Fann", "Fuite d'eau");
        try {
            service.resoudre(r3.getId());
        } catch (TransitionStatutInterditeException e) {
            System.out.println("3) " + e.getMessage());
        }

        // 4) Donnée invalide à la création
        try {
            new Reclamation(10, "Medina", "   ");
        } catch (ReclamationInvalideException e) {
            System.out.println("4) " + e.getMessage());
        }

        // 5) Attraper la FAMILLE entiere (la classe de base)
        try {
            registre.findById(42);
        } catch (SignalcuaException e) { // attrape TOUTES les exceptions métier
            System.out.println("5) attrape comme SignalcuaException : " + e.getMessage());
        }

        // 6) Chainer une cause
        try {
            try {
                Integer.parseInt("abc");
            } catch (NumberFormatException cause) {
                throw new ReclamationInvalideException("Délai invalide : abc", cause);
            }
        } catch (SignalcuaException e) {
            System.out.println("6) " + e.getMessage() + " | cause = " + e.getCause().getClass().getSimpleName());
        }
    }
}
```

## Verification par exécution

```bash
javac -encoding UTF-8 *.java      # les 9 fichiers
java Main
```

**Résultat obtenu (Java 21.0.7), aucune erreur à la compilation :**

```text
1) #1 -> RESOLUE
2) ReclamationNotFoundException : Réclamation 99 introuvable (id=99)
3) Transition interdite : NOUVELLE -> RESOLUE
4) Description obligatoire
5) attrape comme SignalcuaException : Réclamation 42 introuvable
6) Délai invalide : abc | cause = NumberFormatException
```

**Lecture ligne par ligne :**

| Ligne | Ce qui est prouve |
|---|---|
| `1) #1 -> RESOLUE` | Le chemin nominal `demarrer` puis `resoudre` fonctionne : les transitions valides passent. |
| `2) ReclamationNotFoundException : Réclamation 99 introuvable (id=99)` | L'exception est **précise** (son nom), **lisible** (son message) et **exploitable** (son `id` via `getId()`). |
| `3) Transition interdite : NOUVELLE -> RESOLUE` | On a refusé de **résoudre** une réclamation encore `NOUVELLE` : la règle métier est **protégée** par le code, pas seulement par la discipline du développeur. |
| `4) Description obligatoire` | La `Reclamation` **refuse** une donnée invalide dès sa construction (`fail-fast`). |
| `5) attrape comme SignalcuaException : ...` | Un **seul** `catch (SignalcuaException e)` a attrapé une `ReclamationNotFoundException` : la **racine commune** joue bien son rôle de famille. |
| `6) ... cause = NumberFormatException` | La cause d'origine est **conservée** : le message est métier, mais la cause technique reste disponible pour le diagnostic. |

> ℹ️ **Remarque sur l'ordre (pas 2 et 5)** : dans les deux cas, la `ReclamationNotFoundException` est levée par `findById`. Au point 2, on la capture **par son type précis** ; au point 5, **par la famille**. Si les deux `catch` étaient dans le **même** bloc, il faudrait placer le type précis **avant** la famille, sinon le compilateur refuserait (`has already been caught`).

## Erreurs frequentes et comment les reconnaitre

| Message / symptôme | Cause | Correction |
|---|---|---|
| `has already been caught` | le `catch (SignalcuaException e)` est placé **avant** un `catch` de fille | ordonner du **précis** vers la **famille** |
| `cannot find symbol: class SignalcuaException` | typo dans le nom, ou fichier non compilé avec les autres | vérifier l'orthographe exacte et compiler `*.java` ensemble |
| `Réclamation null introuvable` | vous avez passé un `Integer` `null` au constructeur | passer un `int` primitif ; l'autoboxing de `null` lève un NPE avant |
| L'exception ne remonte pas / le programme s'arrête | vous avez capturé `SignalcuaException` trop haut et ne l'avez pas relancée | décider : soit transformer, soit relancer, soit journaliser |
| `getCause()` renvoie `null` | vous avez construit l'exception **sans** le 2e argument | `super(message, cause)` et `new ...Exception(msg, e)` |
| `unreported exception ...` | une de vos exceptions **étend `Exception`** au lieu de `RuntimeException` | la faire étendre `SignalcuaException` (donc `RuntimeException`) |
| Le statut ne change jamais, aucune erreur | vous avez modifié l'`enum` ou la condition de garde (`!=` au lieu de `==`) | revérifier les conditions des transitions |

## Checklist de validation

Reprenez chaque point **sur votre code** :

- [ ] `SignalcuaException extends RuntimeException` ; **aucun `throws`** dans les signatures du service.
- [ ] Les trois filles étendent `SignalcuaException` (vérifiable dans les `extends`).
- [ ] `ReclamationNotFoundException` expose `getId()` et le message contient l'`id`.
- [ ] `Reclamation` refuse un `id <= 0` et une `description` vide (prouvé par les points 4 et 3).
- [ ] `findById` lève quand l'identifiant est absent.
- [ ] `ServiceReclamations` **ne contient aucun `try/catch`**.
- [ ] Le `main` attrape au moins une fois `SignalcuaException` (point 5).
- [ ] Le point 6 affiche `cause = NumberFormatException` (cause conservée).
- [ ] L'ordre des `catch` respecte précis → famille.

## Conseils pour progresser

1. **Ajoutez une exception** `QuartierInconnuException extends SignalcuaException` et levez-la quand le quartier est vide. Vous sentirez la différence entre « enrichir la famille » et « tout mettre dans une exception fourre-tout ».
2. **Anticipez la partie 7** : un `@ControllerAdvice` fera exactement `catch (ReclamationNotFoundException e) → 404` et `catch (ReclamationInvalideException e) → 400`. Vous avez déjà écrit **les types** qu'il utilisera : c'est le cœur du mécanisme.
3. **Comparez avec la partie 3** : réécrivez `findById` en `Optional` **et** en « qui lève », côte à côte. La règle de décision (absence normale vs erreur) deviendra un réflexe.
4. **Anticipez la partie 9** : un test `assertThrows(ReclamationNotFoundException.class, () -> registre.findById(99))` vérifiera exactement les points 2 et 5. Les exceptions **nommées** rendent les tests lisibles.
5. **Notez dans votre carnet** la liste de vos exceptions métier. En grandissant, ce « vocabulaire d'erreurs » devient une **documentation vivante** de vos règles de gestion.

➡️ **Fin de la partie 4** : SignalCUA ne se contente plus de « ne pas planter » — il **nomme** ses échecs. Prochaine étape, la **partie 5 — Programmation fonctionnelle & Stream API** transformera vos collections de réclamations sans boucles explicites : le prolongement naturel de tout ce que vous venez d'apprendre.

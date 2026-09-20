# Leçon 04 — Java moderne : records, sealed, pattern matching

> 🧭 **Pont depuis la leçon 03** : vous savez désormais signer des contrats (`Traitable`) et forcer des familles à les respecter. Mais regardez le prix en lignes : la classe `Reclamation` de la leçon 01, c'est ~60 lignes pour porter **quatre données et deux comportements** (champs, constructeur, validation, getters, equals absent…). Et le `String statut` accepte encore `"PLATANE"` dès qu'un setter bâclé traîne. Cette leçon présente les trois outils « Java moderne » (16/17/21) qui règlent ça : le **`record`** (données immuables en une ligne), le **`sealed`** (famille de types fermée) et le **pattern matching** (un `switch` qui comprend les types).

---

## 1. Objectifs d'apprentissage

À la fin de cette leçon, vous saurez :

- Écrire un **`record`** et expliquer ce qu'il génère automatiquement (constructeur, accesseurs, `equals`/`hashCode`/`toString`).
- Ajouter une **validation** dans le constructeur compact d'un record.
- Définir une **interface scellée** (`sealed ... permits ...`) : une liste fermée de sous-types.
- Utiliser le **pattern matching** : `switch` sur les types, exhaustif, vérifié par le compilateur.
- Savoir quand utiliser ces outils (DTOs, états fermés) et quand ils ne conviennent PAS (entités JPA).
- Réécrire l'Étape 2 du fil rouge en version moderne : `Reclamation` en record, états scellés, affichage par pattern matching.

---

## 2. Explication simple

### 2.1 Le `record` : la classe de données sans la paperasse

**Pourquoi ?** 90 % des classes que vous écrivez en backend ne sont que des **données** : un DTO (objet qui transporte des données entre l'API et le client), une ligne de résultat, un événement. Les écrire « à l'ancienne » (champs private + constructeur + getters + `equals` + `hashCode` + `toString`) coûte des dizaines de lignes répétitives — et une erreur dedans (`equals` oublié ou bâclé) crée des bugs sournois dans les collections et les comparaisons.

**Analogie** : le `record` est un formulaire pré-imprimé : les cases sont toutes là (constructeur, lectures, comparaison, affichage), vous n'avez qu'à remplir les champs qui vous sont propres.

**Comment ?** Une ligne :

```java
public record PointInteret(String nom, String quartier) { }
```

Cette seule ligne crée, **générés par le compilateur** :
- deux **champs `private final`** (donc immuables : rien n'est modifiable après création) ;
- un **constructeur** `new PointInteret("Place Dakar", "Plateau")` ;
- des **accesseurs sans préfixe `get`** : `p.nom()`, `p.quartier()` (et non `getNom()`) ;
- **`equals()`** et **`hashCode()`** corrects : deux records avec les mêmes valeurs sont égaux (comparaison valeur par valeur) ;
- un **`toString()`** lisible : `PointInteret[nom=Place Dakar, quartier=Plateau]`.

**Quand ?** Pour toute donnée immuable : DTO d'API, résultat de calcul, ligne de rapport. Quand vous avez besoin de MUTER un objet, le record n'est pas l'outil — c'est même un bon signal de design : demandez-vous si la mutation est vraiment nécessaire (la leçon 01 vous avait prévenus).

> 📖 **Vocabulaire** : **DTO** (*Data Transfer Object*) = objet qui transporte des données entre deux couches (typiquement l'API et le client) sans logique métier. **Accesseur** = méthode qui lit une donnée (le « getter »). **`equals`/`hashCode`** = le couple standard Java de comparaison d'objets (vu en partie 3 avec les collections).

### 2.2 La validation : le constructeur compact

**Pourquoi ?** Un record génère son constructeur tout seul — mais alors où mettre la validation de la leçon 01 ? Java prévoit le **constructeur compact** : une forme où l'on écrit uniquement les vérifications, sans répéter les affectations.

**Comment ?** Le constructeur compact s'écrit SANS liste de paramètres (le compilateur sait déjà) :

```java
public record PointInteret(String nom, String quartier) {

    public PointInteret {                 // constructeur compact : pas de (String nom, ...)
        if (nom == null || nom.isBlank()) {
            throw new IllegalArgumentException("Le nom est obligatoire");
        }
        // PAS besoin d'écrire this.nom = nom : c'est fait automatiquement APRÈS le if
    }
}
```

Le corps du constructeur compact s'exécute **avant** les affectations automatiques — vous validez, le compilateur assigne. Le meilleur des deux mondes : une ligne de déclaration, la validation en bonus.

### 2.3 Le `sealed` : une famille de types fermée

**Pourquoi ?** La hiérarchie de la leçon 02 est **ouverte** : n'importe qui, demain, peut écrire `extends Reclamation` et créer une quatrième, cinquième… dixième fille. Parfois c'est voulu (extensibilité) ; parfois NON : le statut d'une réclamation, par exemple, n'a que **trois valeurs possibles et connues** (`NOUVELLE`, `EN_COURS`, `RESOLUE`). On veut le dire au compilateur : « cette famille est fermée, la voici en entier ».

**Analogie** : les boutons d'un ascenseur. Il y a exactement RDC, 1er, 2e — personne ne peut ajouter un bouton « sous-sol secret ». Une liste fermée, visible, exhaustive.

**Comment ?** `sealed` (« scellé ») sur l'interface/mère + `permits` (« autorise ») pour lister les signataires, et chaque signataire doit choisir son propre niveau d'ouverture :

```java
// L'interface scellée : la liste fermée de tous les états possibles
public sealed interface EtatReclamation permits Nouvelle, EnCours, Resolue { }

// Chaque état : un record qui implémente l'interface scellée
public record Nouvelle(String dateSignalement) implements EtatReclamation { }
public record EnCours(String agentAssigne)   implements EtatReclamation { }
public record Resolue(String dateResolution) implements EtatReclamation { }
```

Trois mots pour l'ouverture des filles : **`final`** (plus de filles en dessous), **`sealed`** (la fermeture continue avec une autre liste), ou **`non-sealed`** (ré-ouvert). Le record est implicitement final.

**Quand ?** Pour modéliser un ensemble **fini et connu** d'états ou de types : les statuts, les types de réclamation, les résultats possibles d'une opération. La roadmap le compare justement à l'« union type discriminé » de TypeScript : un type qui ne peut valoir que ces valeurs-là, et rien d'autre.

> 📖 **Vocabulaire** : **sceller** (`sealed`) = fermer une hiérarchie à une liste explicite de sous-types. **`permits`** = la liste des sous-types autorisés. **`non-sealed`** = rouvrir une branche de la hiérarchie.

### 2.4 Le pattern matching : un `switch` qui comprend les types

**Pourquoi ?** Avec une famille scellée, la question naturelle devient : « quel état cet objet porte-t-il, et que faire selon le cas ? ». L'outil historique serait une cascade de `if (x instanceof Nouvelle) ...`. Le **pattern matching** (*filtrage par motif*) modernise ça : le `switch` teste des **types**, extrait la donnée dans une variable, et — avec une famille scellée — le compilateur **vérifie l'exhaustivité** (si vous oubliez un cas, il refuse de compiler).

**Comment ?**

```java
// "etat" est un EtatReclamation (donc : Nouvelle, EnCours OU Resolue — rien d'autre)
String message = switch (etat) {
    case Nouvelle n  -> "Reçue le " + n.dateSignalement();   // n est un Nouvelle, extrait
    case EnCours  e  -> "Prise en charge par " + e.agentAssigne();
    case Resolue  r  -> "Résolue le " + r.dateResolution();
};   // PAS de case default : inutile — le compilateur SAIT que les 3 cas suffisent
```

Deux nouveautés, décomposées :
- `case Nouvelle n -> ...` : teste le type ET nomme la variable `n` (typée `Nouvelle`) prête à l'emploi — fini le cast manuel (conversion de type forcée, vue en partie 1).
- L'absence de `default` est **voulue** : si demain vous ajoutez un état `Archivee` à la liste des `permits`, la compilation de ce switch ÉCHOUE et vous indique le cas manquant. Le compilateur vous guide vers le code à mettre à jour.

**Quand ?** Chaque fois que le code doit réagir différemment selon le type d'un objet d'une famille fermée — exactement le cas du fil rouge : afficher un message différent selon l'état.

**Remarque complémentaire** : le pattern matching existe aussi pour `instanceof` (test de type avec variable extraite, sans switch) — même mécanisme, version courte.

### 2.5 Quand ces outils — et surtout quand PAS

La roadmap est formelle et c'est LA règle à retenir de cette leçon :

- **Oui** : records pour toute donnée immuable (DTO, value object, état scellé) ; sealed pour un ensemble fini de types/états ; pattern matching pour traiter ces familles.
- **Non** : les **entités JPA** (partie 8 : objets mappés vers une base de données) ne peuvent pas être des records — le framework Hibernate crée des « proxies » (doublons générés) qui exigent un constructeur vide et des champs modifiables. Gardez des classes classiques pour les entités, des records pour le transport de données.

Autrement dit : **records/sealed pour ce qui circule et se décide, classes pour ce qui vit et se modifie**.

---

## 📖 Vocabulaire / Abréviations

| Terme | Définition en une ligne |
|---|---|
| **`record`** | Classe de données immuable : champs final + constructeur + accesseurs + equals/hashCode/toString générés. |
| **Composant** | Chaque élément déclaré dans l'en-tête d'un record (`nom`, `quartier`...). |
| **Accesseur** | Méthode qui lit un composant : `p.nom()` (sans préfixe `get`). |
| **Constructeur compact** | Forme de constructeur de record où l'on écrit seulement la validation. |
| **DTO** | *Data Transfer Object* : objet qui transporte des données entre couches, sans logique. |
| **Value object** | Objet défini par ses VALEURS (deux objets aux valeurs égales sont interchangeables). |
| **`sealed`** | Hiérarchie fermée : seuls les sous-types listés par `permits` sont autorisés. |
| **`permits`** | La liste explicite des sous-types autorisés d'une interface/classe scellée. |
| **`non-sealed`** | Ré-ouvre une branche d'une hiérarchie scellée. |
| **Pattern matching** | `switch`/`instanceof` qui teste le type ET extrait une variable typée. |
| **Exhaustivité** | Garantie du compilateur : un switch sur famille scellée couvre TOUS les cas. |
| **Cast** | Conversion de type explicite `(Nouvelle) x` — évitée par le pattern matching. |
| **Entité JPA** | Objet mappé vers une table de base de données (partie 8) — jamais un record. |
| **Proxy** | Doublon généré par Hibernate autour d'une entité (raison de l'interdit record). |

---

## 3. Exemples concrets : l'Étape 2 du fil rouge, version moderne

Le bonus de la roadmap : « transformez `Reclamation` en `record` immuable, et le statut en `sealed interface` avec pattern matching pour afficher un message différent selon l'état ». Le voici, fichier par fichier.

**Fichier 1 — l'interface scellée des états** (`EtatReclamation.java`) :

```java
// La famille fermée des états : la liste complète, connue du compilateur
public sealed interface EtatReclamation permits Nouvelle, EnCours, Resolue { }
```

**Fichiers 2-4 — chaque état est un record** (immuable, equals/toString gratuits) :

```java
// Fichier : Nouvelle.java
public record Nouvelle(String dateSignalement) implements EtatReclamation { }

// Fichier : EnCours.java
public record EnCours(String agentAssigne) implements EtatReclamation { }

// Fichier : Resolue.java
public record Resolue(String dateResolution) implements EtatReclamation { }
```

**Fichier 5 — la réclamation elle-même, en record avec validation** (`Reclamation.java`) :

```java
public record Reclamation(int id, String description, String quartier, EtatReclamation etat) {

    public Reclamation {                                   // constructeur compact
        if (id <= 0) {
            throw new IllegalArgumentException("L'id doit être positif");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("La description est obligatoire");
        }
        // this.id = ... etc. : inutile à écrire, le compilateur le fait
    }

    // Une réclamation neuve : état de départ, factory lisible (rappel static, partie 1)
    public static Reclamation neuve(int id, String description, String quartier) {
        return new Reclamation(id, description, quartier,
            new Nouvelle("aujourd'hui"));   // (une vraie date viendrait en partie 4)
    }

    // "Changer d'état" sans muter : on RETOURNE une nouvelle réclamation
    public Reclamation demarrer(String agent) {
        return new Reclamation(id, description, quartier, new EnCours(agent));
    }

    public Reclamation resoudre(String date) {
        return new Reclamation(id, description, quartier, new Resolue(date));
    }
}
```

> 📖 **Vocabulaire** : **factory** (méthode de fabrique) = méthode `static` qui construit et renvoie un objet (`Reclamation.neuve(...)`), pour un code appelant plus lisible qu'un `new` brut.

**Fichier 6 — l'affichage par pattern matching** (`MainModerne.java`) :

```java
public class MainModerne {
    public static void main(String[] args) {
        // Création → démarrage → résolution : trois OBJETS immuables distincts
        Reclamation r1 = Reclamation.neuve(1, "Nid de poule", "Medina");
        Reclamation r2 = r1.demarrer("Fatou");
        Reclamation r3 = r2.resoudre("hier soir");

        // Le switch exhaustif sur la famille scellée :
        for (Reclamation r : new Reclamation[]{ r1, r2, r3 }) {
            String message = switch (r.etat()) {
                case Nouvelle n -> "Nouvelle (signalée " + n.dateSignalement() + ")";
                case EnCours  e -> "En cours, agent " + e.agentAssigne();
                case Resolue  s -> "Résolue le " + s.dateResolution();
            };
            System.out.println("#" + r.id() + " " + r.description() + " → " + message);
        }

        // Bonus equals : deux records aux mêmes valeurs sont ÉGAUX
        Reclamation a = Reclamation.neuve(1, "Nid de poule", "Medina");
        System.out.println("a.equals(r1) ? " + a.equals(r1));   // true — gratuit !
    }
}
```

Sortie attendue :

```text
#1 Nid de poule → Nouvelle (signalée aujourd'hui)
#1 Nid de poule → En cours, agent Fatou
#1 Nid de poule → Résolue le hier soir
a.equals(r1) ? true
```

Compiler et exécuter :

```bash
javac EtatReclamation.java Nouvelle.java EnCours.java Resolue.java Reclamation.java MainModerne.java
java MainModerne
```

---

## 4. Bonnes pratiques modernes (2025-2026)

1. **Records pour TOUS les DTOs** : objets d'échange d'API, événements, résultats de requêtes — c'est la façon standard d'écrire du Java en 2025-2026 (l'équivalent le plus proche d'un `type`/`interface` TypeScript).
2. **Immutabilité par défaut, version record** : le record ne peut pas être mutable — et c'est une *feature* : si vous voulez muter, repensez le design (ou revenez à une classe, avec justification).
3. **Sealed pour les ensembles fermés du métier** : états, types de réclamations, résultats d'opération. Le compilateur devient votre filet : ajouter un cas casse les switches incomplets, jamais en silence.
4. **Switch sans `default` sur famille scellée** : l'absence de default est un choix de sûreté — la compilation échouera à chaque nouveau cas non traité.
5. **Constructeur compact = validation obligatoire** : un record se valide dans son constructeur compact, comme une classe (leçon 01) — les mauvaises données meurent à la création.
6. **Entités JPA ≠ records** : en partie 8, les objets persistés en base resteront des classes classiques (constructeur vide + setters pour Hibernate). Records = transport ; classes = persistance.

---

## 5. Pièges à éviter

### Piège 1 — Vouloir muter un record

```java
// ❌ IMPOSSIBLE (et c'est voulu) :
var r = Reclamation.neuve(1, "Nid de poule", "Medina");
r.setEtat(new EnCours("Fatou"));   // n'existe PAS : aucun setter généré

// ✅ BON : "changer" = produire une NOUVELLE réclamation
r = r.demarrer("Fatou");           // réassigne la VARIABLE (le record, lui, est intact)
```

*Pourquoi c'est une force* : aucune autre partie du code ne peut modifier votre objet sous votre nez — les bugs « qui a changé le statut ? » disparaissent.

### Piège 2 — Un sealed sans tous les cas dans le switch

```java
// ❌ MAUVAIS : ignorer l'avertissement du compilateur (cas manquant)
String message = switch (etat) {
    case Nouvelle n -> "Reçue";
    case EnCours  e -> "En cours";
    // case Resolue manquant → erreur de compilation, et c'est VEULU
};

// ✅ BON : couvrir tous les cas listés par "permits" — ou ajouter default
//    UNIQUEMENT si la hiérarchie n'est pas scellée
```

*Pourquoi c'est grave* : le compilateur vous dit exactement quel cas ajouter — ignorer ce signal, c'est refuser un correctif guidé.

### Piège 3 — Des DTO « à l'ancienne » avec getters/setters manuels

```java
// ❌ ANTI-PATTERN (2025-2026) : 40 lignes pour déclarer des données
public class EtatDto {
    private String statut;
    public String getStatut() { return statut; }
    public void setStatut(String s) { this.statut = s; }
    // + equals() à la main, + hashCode() à la main, + toString() à la main...
}

// ✅ BON : une ligne, et tout est là (et immuable)
public record EtatDto(String statut) { }
```

### Piège 4 — Un record pour une entité JPA (anticipation partie 8)

```java
// ❌ NE FONCTIONNERA PAS : Hibernate (le moteur JPA) exige un constructeur vide
//    et des champs modifiables — le record n'a ni l'un ni l'autre
public record ReclamationEntity(...) { }   // en partie 8 : erreur au runtime

// ✅ BON : classe classique pour ce qui est persisté en base
public class ReclamationEntity { ... }     // voir partie 8 pour les annotations
```

---

## Checklist de validation

Avant de passer à la leçon 05, vérifiez que vous savez :

- [ ] Écrire un record et lister ce qu'il génère (constructeur, accesseurs, equals/hashCode/toString).
- [ ] Ajouter une validation dans le constructeur compact.
- [ ] Définir une interface scellée avec `permits` et choisir final/sealed/non-sealed pour les filles.
- [ ] Écrire un switch à pattern matching exhaustif sur une famille scellée.
- [ ] Expliquer pourquoi l'absence de `default` est un choix de sûreté.
- [ ] Citer le cas où le record est interdit (entités JPA, partie 8).
- [ ] Avoir réécrit l'Étape 2 du fil rouge en version moderne (record + sealed + pattern matching).

➡️ **Prochaine étape** : un détail vous a peut-être chatouillé — pour des états SIMPLES (`NOUVELLE`, `EN_COURS`, `RESOLUE`, sans donnée portée), trois records séparés font beaucoup. Java a un outil encore plus adapté aux **ensembles finis de constantes** : l'**énumération (`enum`)** — dernière leçon de la partie, et la plus utilisée du quotidien.




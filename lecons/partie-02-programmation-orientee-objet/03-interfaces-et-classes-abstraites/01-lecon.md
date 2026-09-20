# Leçon 03 — Interfaces et classes abstraites

> 🧭 **Pont depuis la leçon 02** : votre hiérarchie SignalCUA fonctionne — trois filles redéfinissent `traiter()` et une seule boucle polymorphe les traite. Mais remarquez le « prix » payé : la mère `Reclamation` a dû écrire un corps de `traiter()` **générique** (qui s'exécute pour tout le monde) uniquement pour que le polymorphisme compile. Et le contrat « je sais traiter » reste enfermé dans la famille des réclamations : un `Agent` ou une `Intervention` qui savent aussi « traiter » ne peuvent pas signer le même contrat (ils ne peuvent pas étendre `Reclamation` !). Cette leçon introduit les deux outils qui corrigent ça : l'**interface** (contrat pur) et la **classe abstraite** (base partielle).

---

## 1. Objectifs d'apprentissage

À la fin de cette leçon, vous saurez :

- Écrire une **interface** (contrat pur) et la **implémenter** avec `implements`.
- Expliquer le rôle des méthodes `default` et `static` d'une interface (Java 8 et après).
- Écrire une **classe abstraite** avec méthodes concrètes et **méthode abstraite**.
- Choisir entre interface et classe abstraite (et savoir quand n'utiliser AUCUNE des deux).
- Éviter les pièges : interface à une seule implémentation « au cas où », classe abstraite trop lourde, interface « marker » vide.
- Appliquer le tout à SignalCUA : extraire le contrat `Traitable` de la famille des réclamations.

---

## 2. Explication simple

### 2.1 L'interface : un contrat, pas du code

**Pourquoi ?** Dans la leçon 02, `traiter()` existe chez la mère mais son corps générique est un compromis : aucune réclamation réelle ne l'exécute vraiment. Ce que le monde extérieur a besoin de savoir, c'est uniquement **« cet objet sait se traiter »** — pas comment. C'est exactement une **interface** : une liste de promesses **sans implémentation** (sans corps de code).

**Analogie** : la plaquette d'un restaurant affichée en vitrine : « ouvre à 11h, prend les cartes bancaires, fait à emporter ». Elle promet des services ; le menu détaillé (l'implémentation) est dans chaque cuisine, différent selon le restaurant. L'interface = la plaquette ; chaque classe qui signe = un restaurant qui garantit ces services à SA manière.

**Comment ?** Le mot-clé `interface` définit le contrat ; `implements` (« implémente ») le fait signer par une classe :

```java
// Le contrat : ce que "savoir traiter" signifie, sans dire comment
public interface Traitable {
    void traiter();   // promesse SANS corps : chaque signataire devra la tenir
}

// Une classe signe avec "implements" :
public class ReclamationVoirie extends Reclamation implements Traitable {
    @Override
    public void traiter() {
        // ... le corps obligatoire, sinon erreur de compilation
    }
}
```

**La grande différence avec l'héritage** : une classe n'hérite que d'**une** classe mère (`extends` une seule fois), mais peut signer **plusieurs interfaces** (`implements A, B, C`). Pourquoi cette règle ? L'héritage transfère de l'état et du code (des ambiguïtés seraient possibles avec deux mères) ; une interface ne transfère **rien**, donc signer 10 contrats ne pose aucun conflit.

**Quand ?** Pour un contrat entre couches (un service demande « quelque chose qui sait traiter », sans connaître les classes concrètes), pour plusieurs implémentations possibles, et pour rendre un code testable (leçon 09 : on remplacera l'implémentation par une fausse, un « mock »).

### 2.2 `default` et `static` : quand l'interface a un peu de code

Depuis Java 8, une interface peut contenir deux formes limitées de code :

```java
public interface Traitable {
    void traiter();                          // la promesse abstraite

    default boolean estTraitable() {         // "default" = code PAR DÉFAUT partagé
        return true;                         // chaque signataire peut le garder ou le redéfinir
    }

    static Traitable nimporteLequel() {      // "static" = fonction utilitaire du contrat
        return null; // (exemple pédagogique : une interface n'a pas d'état à utiliser)
    }
}
```

**Pourquoi `default` ?** Pour étendre un contrat existant sans casser les classes qui le signent déjà : elles héritent du comportement par défaut et ne le redéfinissent que si besoin.

**Pourquoi pas plus de code ?** Une interface n'a **pas d'état** (pas de champs modifiables) : elle ne peut donc pas porter une vraie logique qui dépend de données. Dès que vous voulez état + code commun, c'est la classe abstraite qu'il faut — sujet suivant. (Et notez : une interface « marker » vide, sans AUCUNE méthode, n'a presque plus d'usage en Java moderne — évitez de signer des contrats vides.)

### 2.3 La classe abstraite : la base partielle

**Pourquoi ?** Parfois on veut **partager de l'état et du code** (comme une classe mère) MAIS interdire l'instanciation directe (« créer une `Reclamation` générique » n'a pas de sens métier). C'est la **classe abstraite** : la moitié d'un objet, volontairement incomplète.

**Analogie** : le plan d'une maison « type » fourni par l'architecte : il définit les fondations et la plomberie (code commun), mais reste *non constructible en l'état* — il faut choisir une variante. Et il impose un emplacement « cuisine » (méthode abstraite) que chaque variante aménage à sa façon.

**Comment ?** Deux usages du même mot-clé `abstract` :

```java
public abstract class TraitementDeBase {      // "abstract" sur la classe :
                                              // new TraitementDeBase() est INTERDIT
    protected String agent;                   // de l'ÉTAT : les sous-classes en hériteront

    public TraitementDeBase(String agent) {   // un constructeur : appelé par super(...)
        this.agent = agent;                   // des filles, comme en leçon 02
    }

    public void journaliser(String action) {  // du CODE commun, concret
        System.out.println("[" + agent + "] " + action);
    }

    public abstract void traiter();           // méthode abstraite : PAS de corps ;
}                                             // chaque fille CONCRÈTE doit l'implémenter
```

> 📖 **Vocabulaire** : **méthode abstraite** = méthode déclarée sans corps, obligatoirement implémentée par chaque sous-classe concrète. **Classe concrète** = classe qu'on peut instancier avec `new`. Une fille peut aussi rester `abstract` si elle ne veut pas encore tout implémenter.

**Le mécanisme clé** : la méthode abstraite est le « cuisine obligatoire » du plan. Le compilateur refuse qu'une classe concrète hérite d'une méthode abstraite sans la remplir — le contrat est forcé par le compilateur lui-même.

### 2.4 Interface ou classe abstraite ? Le tableau de décision

La règle 2025-2026 est simple : **interface par défaut**, classe abstraite seulement pour partager de l'état :

| Critère | Interface | Classe abstraite |
|---|---|---|
| État (champs modifiables) | ❌ aucun | ✅ oui |
| Code partagé | limité (`default`/`static`) | ✅ illimité |
| Combien par classe | plusieurs | une seule |
| `new` direct | ❌ | ❌ |
| Impact sur la hiérarchie | aucun (indépendante) | occupe le slot unique de `extends` |

**Citation de la roadmap à retenir** : les interfaces Java jouent le rôle de contrat, comme vos interfaces TypeScript — mais en Spring (partie 7) elles servent aussi de **point d'injection** : vous injectez une interface (`ReclamationRepository`), le framework fournit l'implémentation concrète. Les interfaces sont donc l'investissement le plus rentable de toute la partie 2.

**Et quand n'utiliser ni l'une ni l'autre ?** L'anti-pattern signalé par la roadmap : créer une interface **pour une seule implémentation** « juste au cas où ». Une interface se justifie quand il y a (a) plusieurs implémentations réelles ou prévisibles, ou (b) un besoin d'abstraction pour les tests/injection. Sinon, une classe concrète suffit — ajoutez l'interface le jour où le besoin apparaît.

### 2.5 La composition, promise par la leçon 02

La leçon 02 avait promis un exemple de **composition** (rappel : donner à une classe un objet qu'elle *utilise*, plutôt que d'hériter). Le voici : plutôt que d'hériter d'un `TraitementDeBase` pour réutiliser son journal, on lui donne un champ :

```java
public class TraitementDeBase {
    private final String agent;
    public TraitementDeBase(String agent) { this.agent = agent; }
    public void journaliser(String action) { System.out.println("[" + agent + "] " + action); }
}

// Composition : "A un" traitement de base, au lieu d'"est un" traitement de base
public class TraitementVoirie implements Traitable {
    private final TraitementDeBase base;             // "A un" → champ
    public TraitementVoirie(String agent) { this.base = new TraitementDeBase(agent); }
    @Override
    public void traiter() {
        base.journaliser("équipe envoyée");          // on UTILISE, on n'hérite pas
    }
}
```

Pourquoi c'est mieux : pas de hiérarchie qui s'allonge, et le `TraitementDeBase` est échangeable sans toucher à la famille. C'est l'esprit de « favorise la composition sur l'héritage », appliqué.

---

## 📖 Vocabulaire / Abréviations

| Terme | Définition en une ligne |
|---|---|
| **Interface** | Contrat : liste de méthodes promises, sans état ; une classe la signe avec `implements`. |
| **`implements`** | Mot-clé : « cette classe respecte ce contrat » (plusieurs possibles). |
| **`default`** | Méthode d'interface avec un corps par défaut, héritable par les signataires. |
| **`static` (interface)** | Fonction utilitaire rattachée au contrat, appelée `NomInterface.methode()`. |
| **Classe abstraite** | Classe non instanciable (`abstract`), partage état + code entre sous-classes. |
| **`abstract`** | Mot-clé : sur une classe (« non instanciable ») ou une méthode (« sans corps, à implémenter »). |
| **Méthode abstraite** | Déclarée sans corps ; chaque sous-classe concrète DOIT la remplir. |
| **Classe concrète** | Classe instanciable avec `new` (implémente toutes les promesses). |
| **Contrat** | Métaaphore : l'ensemble des méthodes qu'une classe promet de fournir. |
| **Composition** | Construire une classe en lui donnant des champs objets qu'elle utilise (vs hériter). |
| **DI** | *Dependency Injection* (injection de dépendances) : le framework fournit les objets dont une classe a besoin — via des interfaces (partie 7). |
| **Mock** | Fausse implémentation d'une interface, utilisée pour tester sans le vrai code (leçon 09). |
| **Interface marker** | Interface vide sans méthode — anti-pattern en Java moderne. |

---

## 3. Exemples concrets : le contrat `Traitable` pour SignalCUA

**Étape A — le contrat** (un fichier `Traitable.java`) :

```java
// Fichier : Traitable.java
public interface Traitable {
    void traiter();   // la promesse : « je sais me faire traiter »
}
```

**Étape B — la famille des réclamations signe le contrat** (on reprend les classes de la leçon 02 ; seules les déclarations changent) :

```java
// Fichier : ReclamationVoirie.java — la mère reste Reclamation, ET on signe Traitable
public class ReclamationVoirie extends Reclamation implements Traitable {
    private final String gravite;

    public ReclamationVoirie(int id, String description, String quartier, String gravite) {
        super(id, description, quartier);
        this.gravite = gravite;
    }

    @Override
    public void traiter() {   // implémentation OBLIGATOIRE du contrat (et de la mère)
        System.out.println("#" + getId() + " [VOIRIE/" + gravite + "] équipe envoyée : " + getDescription());
    }
}

// Fichier : ReclamationProprete.java
public class ReclamationProprete extends Reclamation implements Traitable {
    private final boolean recyclable;

    public ReclamationProprete(int id, String description, String quartier, boolean recyclable) {
        super(id, description, quartier);
        this.recyclable = recyclable;
    }

    @Override
    public void traiter() {
        String tri = recyclable ? "tri à faire" : "collecte standard";
        System.out.println("#" + getId() + " [PROPRETE] " + tri + " : " + getDescription());
    }
}
```

**Étape C — un HORS-famille signe le même contrat** : c'est TOUT le bénéfice. Un `Agent` n'est pas une `Reclamation` (pas d'héritage possible), mais il sait « traiter » :

```java
// Fichier : Agent.java
public class Agent implements Traitable {
    private final String nom;

    public Agent(String nom) { this.nom = nom; }

    @Override
    public void traiter() {
        System.out.println(nom + " prend en charge la file de réclamations");
    }
}
```

**Étape D — la boucle polymorphe, désormais ouverte** : le tableau n'est plus typé `Reclamation` mais `Traitable` — tout signataire y entre, de n'importe quelle famille :

```java
// Fichier : MainTraitable.java
public class MainTraitable {
    public static void main(String[] args) {
        Traitable[] travail = {
            new ReclamationVoirie(1, "Nid de poule", "Medina", "majeure"),
            new Agent("Fatou")
        };
        for (Traitable t : travail) {
            t.traiter();   // la version de chaque objet s'exécute : la boucle ignore tout du reste
        }
    }
}
```

Sortie attendue :

```text
#1 [VOIRIE/majeure] équipe envoyée : Nid de poule
Fatou prend en charge la file de réclamations
```

---

## 4. Bonnes pratiques modernes (2025-2026)

1. **Interface par défaut** : préférez toujours une interface à une classe abstraite quand vous n'avez pas d'état à partager — plus flexible et combinable.
2. **Pas d'interface « au cas où »** : une seule implémentation concrète et pas de besoin de test/injection ? Une classe concrète suffit. L'interface s'ajoutera facilement plus tard.
3. **Programmez contre le contrat** : les boucles, services et paramètres sont typés `Traitable` (l'interface), jamais par une classe concrète quand on peut faire autrement — c'est ce qui rend le code remplaçable.
4. **Une classe abstraite, si elle existe, reste légère** : seulement ce que TOUTES les filles partagent vraiment. Sinon elle impose une structure que les filles subissent.
5. **Pour partager du comportement sans hériter** : la composition (un champ qui « a un » helper) plutôt qu'une classe abstraite — vous venez de le voir au 2.5.
6. **La passerelle Spring** : en partie 7, vos services seront typés par interface pour que Spring puisse injecter l'implémentation (DI). Ce que vous écrivez ici, vous le réutiliserez tel quel.

---

## 5. Pièges à éviter

### Piège 1 — L'interface pour une seule implémentation « au cas où »

```java
// ❌ MAUVAIS : un contrat, un seul signataire, aucun second en vue
public interface ReclamationService { ... }
public class ReclamationServiceImpl implements ReclamationService { ... }  // et c'est tout

// ✅ BON : classe concrète simple ; l'interface naîtra avec le 2e besoin
// (ou au moment de mocker les tests / injecter en Spring)
public class ReclamationService { ... }
```

*Pourquoi c'est grave* : deux classes à maintenir pour zéro bénéfice immédiat, et un bruit de lecture en plus (`Impl` est le suffixe classique dont l'existence même signale le doute).

### Piège 2 — La classe abstraite qui impose trop

```java
// ❌ MAUVAIS : toutes les filles sont FORCÉES d'hériter de méthodes dont elles n'ont que faire
public abstract class TraitementDeBase {
    public abstract void traiter();
    public abstract void archiver();     // et si l'Agent n'archive rien ?
}

// ✅ BON : le contrat minimal ; ce qui est optionnel devient une méthode default,
//    ou part dans un AUTRE contrat (une interface Archivable)
public interface Traitable {
    void traiter();
}
```

### Piège 3 — L'interface marker vide

```java
// ❌ MAUVAIS : un contrat qui ne promet rien — personne ne peut s'y fier
public interface TraitableMark { }          // zéro méthode, zéro valeur

// ✅ BON : soit le contrat décrit un vrai service, soit on n'en écrit pas
public interface Traitable { void traiter(); }
```

### Piège 4 — Confondre « abstract » et « vide »

```java
// ❌ MAUVAIS : une méthode SANS corps dans une classe NON abstraite → la compilation refuse
public class TraitementVoirie {
    public void traiter();      // erreur : "missing method body"
}

// ✅ BON : soit la méthode a un corps, soit la classe est abstract
public abstract class TraitementDeBase {
    public abstract void traiter();
}
```

---

## Checklist de validation

Avant de passer à la leçon 04, vérifiez que vous savez :

- [ ] Écrire une interface et l'implémenter avec `implements` (plusieurs à la fois).
- [ ] Expliquer à quoi servent `default` et `static` dans une interface.
- [ ] Écrire une classe abstraite avec état + code commun + méthode abstraite.
- [ ] Choisir entre interface et classe abstraite avec le tableau de décision.
- [ ] Citer les deux anti-patterns : interface à une implémentation « au cas où », interface marker vide.
- [ ] Expliquer pourquoi les interfaces sont le point d'injection de Spring (partie 7).
- [ ] Avoir extrait le contrat `Traitable` et fait entrer `Agent` dans la boucle polymorphe.

➡️ **Prochaine étape** : le contrat `Traitable` liste ses promesses « à l'ancienne ». Or depuis 2021 (Java 16/17), Java sait faire bien mieux : des données immuables en une ligne (le `record`), des familles de types fermées (les `sealed`), et un `switch` qui vérifie les types (le *pattern matching*) — c'est la leçon 04, la plus « moderne » de la partie.





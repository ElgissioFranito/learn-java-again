# Leçon 02 — Héritage et polymorphisme

> 🧭 **Pont depuis la leçon 01** : votre `Reclamation` est désormais une forteresse — champs `private`, portes contrôlées, immuabilité par défaut. Mais SignalCUA reçoit des réclamations de **trois types différents** : voirie (nid de poule), propreté (ordures), éclairage public (lampadaire). Chacune partage l'essentiel (un id, une description, un quartier, un statut) mais a son propre traitement. Recopier la classe trois fois ? Absurde et dangereux (trois copies à maintenir). La solution de cette leçon : l'**héritage** — une classe mère qui porte le commun, et des filles qui ajoutent leur spécificité.

---

## 1. Objectifs d'apprentissage

À la fin de cette leçon, vous saurez :

- Créer une hiérarchie avec `extends` et invoquer la classe mère avec `super(...)`.
- **Redéfinir** (*overrider*) une méthode avec `@Override` — et comprendre pourquoi l'annotation est vitale.
- Utiliser le **polymorphisme** : une variable du type mère pointant vers un objet enfant.
- Choisir entre `private`, `protected`, `public` et package-private en connaissance de cause.
- Appliquer le test « est-un » pour décider d'un héritage — et connaître la règle « favorise la composition ».
- Construire la hiérarchie SignalCUA : `Reclamation` → `ReclamationVoirie`, `ReclamationProprete`, `ReclamationEclairage`.

---

## 2. Explication simple

### 2.1 L'héritage : la classe mère et les filles

**Pourquoi ?** Trois classes copiées-collées à 90 % identiques, c'est trois fois les mêmes bugs à corriger trois fois. L'héritage résout ça : on écrit **une fois** le commun dans une classe mère, et les classes filles héritent automatiquement de ses champs et méthodes.

**Analogie** : un formulaire administratif. La mairie a un formulaire générique (identité, adresse, signature) et trois variantes qui ne diffèrent que par un encart spécial. Personne ne réimprime tout : la variante dit seulement « formulaire standard **+ encart voirie** ». La classe mère = formulaire standard ; `extends` = « + encart ».

**Comment ?** Le mot-clé `extends` (« étend ») :

```java
public class ReclamationVoirie extends Reclamation {
    // Cette classe REÇOIT gratuitement tous les champs et méthodes
    // publics (et protected) de Reclamation. Elle ajoute sa spécificité.
}
```

**Quand ?** Uniquement quand la relation **« est-un »** est vraie et stable dans le temps : une `ReclamationVoirie` **est une** `Reclamation` — oui. À l'inverse, un `Moteur` n'est pas une `Voiture` (la voiture *a* un moteur) → pas d'héritage (on y revient au 2.6 avec la composition).

### 2.2 `super(...)` : appeler le constructeur de la mère

**Pourquoi ?** La fille n'hérite PAS des constructeurs de la mère (des portails, pas des clefs : la mère a peut-être une validation que la fille doit aussi exécuter). Pour créer une `ReclamationVoirie`, il faut d'abord construire la partie « Reclamation » — c'est le rôle de `super(...)`.

**Comment ?** Comme `this(...)`, l'appel à `super(...)` doit être **la première instruction** du constructeur enfant (et si vous ne l'écrivez pas, Java appelle automatiquement `super()` sans arguments — ce qui plante si la mère n'a pas de constructeur sans paramètres) :

```java
public class ReclamationVoirie extends Reclamation {

    private String gravite; // spécificité de la voirie : degré d'urgence technique

    public ReclamationVoirie(int id, String description, String quartier, String gravite) {
        super(id, description, quartier); // 1ère instruction : construit la partie "mère"
        this.gravite = gravite;           // ensuite seulement, la partie "fille"
    }
}
```

**Lecture** : `new ReclamationVoirie(1, "Nid de poule", "Medina", "majeure")` construit d'abord la `Reclamation` (validation de la description incluse — l'encapsulation de la leçon 01 travaille toujours pour nous !), puis la spécificité voirie.

### 2.3 Redéfinir avec `@Override` : même méthode, autre comportement

**Pourquoi ?** Les trois types de réclamation ne se traitent pas pareil. On garde **le même nom de méthode** (`traiter()`) mais chaque fille **redéfinit** (*override* = réécriture) le contenu. Attention à ne pas confondre avec la **surcharge** (*overloading*, partie 1 leçon 04) : même nom, paramètres différents. La **redéfinition** (*overriding*), c'est même signature, contenu remplacé dans la fille.

**Comment ?** Toujours avec l'annotation **`@Override`** posée au-dessus de la méthode :

```java
// Dans Reclamation (la mère) :
public void traiter() {
    statut = "EN_COURS";
    System.out.println("Traitement générique de #" + id);
}

// Dans ReclamationVoirie (la fille) :
@Override
public void traiter() {
    statut = "EN_COURS";
    System.out.println("#" + id + " : équipe voirie envoyée pour " + description);
}
```

**Pourquoi `@Override` est vital** : sans elle, une faute de frappe (`traiter()` devenu `treater()`) ne déclenche **aucune erreur** — Java croit que vous créez une nouvelle méthode, et la vraie n'est jamais redéfinie. Bug silencieux. Avec `@Override`, le compilateur vérifie qu'une méthode de la mère existe bien avec cette signature et vous crie dessus si non. Un contrôle gratuit, en plus, du compilateur — prenez le réflexe.

> 📖 **Vocabulaire** : **redéfinition** (*overriding*) = réécrire dans la fille une méthode héritée de la mère. **Surcharge** (*overloading*) = plusieurs méthodes de même nom avec des paramètres différents (rappel partie 1). 
> **Annotation** = mot commençant par `@` que le compilateur lit pour vérifier quelque chose (ici, `@Override`).

### 2.4 Le polymorphisme : une variable, plusieurs visages

**Pourquoi ?** Le but ultime : traiter un tableau de réclamations **sans se demander quel type chacune est**. C'est le **polymorphisme** (littéralement « plusieurs formes ») : une variable déclarée avec le type de la mère peut référencer un objet de n'importe quelle fille, et **c'est toujours la méthode de l'objet réel qui est appelée** (décision à l'exécution, « dynamiquement »).

**Comment ?**

```java
Reclamation[] recs = {
    new ReclamationVoirie(1, "Nid de poule", "Medina", "majeure"),
    new ReclamationProprete(2, "Dépôt sauvage", "Fass", false)
};

for (Reclamation r : recs) {  // chaque r est déclarée "Reclamation" (type mère)
    r.traiter();              // ...mais exécute le traiter() de l'objet RÉEL :
                              // 1ère itération → version Voirie ; 2ème → version Propreté
}
```

**Analogie** : un chef de service qui crie « traitez la réclamation suivante ! » sans connaître le détail. Chaque équipe (voirie, propreté) réagit à sa façon, parce que chacune **sait** faire son travail. L'appelant ne sait pas comment — il n'a pas besoin de le savoir.

**Quand ?** Dès que vous traitez une collection d'objets de types voisins. C'est un pilier de Java — et vous le retrouverez tel quel en partie 7 : Spring appelle le traitement sans connaître les implémentations concrètes.

### 2.5 Les modificateurs d'accès, revus en famille

La leçon 01 a posé `private` et `public`. L'héritage ajoute un cas : les champs que la fille doit voir mais pas le reste du monde. **`protected`** = accessible dans la classe, dans les **sous-classes** (les filles), et dans le même **package** (le « dossier » de classes, rappel partie 1 leçon 06). Et le « package-private » (aucun modificateur) = accessible seulement dans le même package :

| Modificateur | La classe | Les filles | Même package | Tout le monde |
|---|---|---|---|---|
| `private` | ✅ | ❌ | ❌ | ❌ |
| *(rien)* | ✅ | ❌ | ✅ | ❌ |
| `protected` | ✅ | ✅ | ✅ | ❌ |
| `public` | ✅ | ✅ | ✅ | ✅ |

Règle pratique : restez sur `private` par défaut ; `protected` seulement quand une fille a réellement besoin de toucher à un champ de la mère.

### 2.6 La règle d'or : « favorise la composition sur l'héritage »

La roadmap est claire : l'héritage libre est **rare** dans le Java professionnel moderne. Quatre garde-fous avant de taper `extends` :

1. **Test « est-un »** : héritage seulement pour une relation « est-un » stable (une `ReclamationVoirie` **est** une `Reclamation`). Pour « a un comportement de », préférez une **interface** — c'est exactement le sujet de la leçon 03.
2. **Profondeur** : au-delà de 2-3 niveaux (`Reclamation` → `ReclamationVoirie` → `ReclamationUrgenteVoirie`…), une hiérarchie devient ingérable. Ne descendez pas.
3. **Composition** : plutôt que d'hériter pour réutiliser du code, donner à la classe un objet qu'elle *utilise* — un champ de la classe utile, construit dans le constructeur. La leçon 03 y reviendra avec des exemples.
4. **Liskov** (principe de substitution de Liskov) : une fille ne doit jamais casser le contrat de sa mère (ex. une fille qui lèverait une exception là où la mère promet de réussir). Si la fille contredit la mère, ce n'est pas une fille.

> 📖 **Vocabulaire** : **composition** = construire une classe en lui donnant des champs objets qu'elle utilise, plutôt que d'hériter. **Principe de substitution de Liskov** : tout code qui fonctionne avec la mère doit fonctionner avec n'importe quelle fille, sans surprise.

---

## 📖 Vocabulaire / Abréviations

| Terme | Définition en une ligne |
|---|---|
| **Héritage** | Mécanisme où une classe fille reçoit les membres d'une classe mère (`extends`). |
| **Classe mère / parente** | Classe dont on hérite (`Reclamation` dans SignalCUA). |
| **Classe fille / enfant** | Classe qui hérite (`ReclamationVoirie`, etc.). |
| **`extends`** | Mot-clé : « cette classe hérite de celle-ci ». |
| **`super(...)`** | Appel du constructeur de la mère — toujours en 1ère instruction du constructeur fils. |
| **`super.methode()`** | Appel d'une méthode de la mère depuis la fille (pour la compléter sans la remplacer). |
| **Redéfinition (overriding)** | Réécrire dans la fille une méthode héritée de la mère. |
| **Surcharge (overloading)** | Plusieurs méthodes de même nom, paramètres différents (rappel partie 1). |
| **`@Override`** | Annotation qui force le compilateur à vérifier qu'on redéfinit bien une méthode de la mère. |
| **Polymorphisme** | Une variable du type mère peut référencer une fille ; c'est la méthode de l'objet réel qui s'exécute. |
| **`protected`** | Accès réservé à la classe, ses filles et son package. |
| **Package-private** | Aucun modificateur : accessible seulement dans le même package. |
| **Composition** | Construire une classe en lui donnant des champs objets qu'elle utilise, au lieu d'hériter. |
| **Liskov (substitution)** | Une fille doit pouvoir remplacer sa mère partout sans casser le comportement. |
| **Classe de base / sous-classe** | Autres noms pour classe mère / classe fille. |

---

## 3. Exemples concrets : la hiérarchie SignalCUA (Étape 2 du fil rouge)

La classe mère : une `traiter()` de base que chaque fille va redéfinir (l'encapsulation de la leçon 01 est conservée — seul `statut` reste mutable, via des méthodes contrôlées) :

```java
// Fichier : Reclamation.java
public class Reclamation {

    private final int id;
    private final String description;
    private final String quartier;
    private String statut;

    public Reclamation(int id, String description, String quartier) {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("La description est obligatoire");
        }
        this.id = id;
        this.description = description;
        this.quartier = quartier;
        this.statut = "NOUVELLE";
    }

    // La version "générique" — chaque fille la spécialisera avec @Override
    public void traiter() {
        this.statut = "EN_COURS";
        System.out.println("Traitement générique de #" + id);
    }

    // méthode utilisable par les filles pour changer le statut proprement
    protected void changerStatut(String nouveau) {
        this.statut = nouveau;
    }

    public String getStatut() { return statut; }
    public int getId()        { return id; }
    public String getDescription() { return description; }
    public String getQuartier()    { return quartier; }
}
```

> 📖 **Vocabulaire** : **`protected`** explique ici son intérêt : `changerStatut` est un outil interne de la famille, invisible du reste du monde.

Les trois filles, chacune avec sa spécificité et son `@Override` :

```java
// Fichier : ReclamationVoirie.java
public class ReclamationVoirie extends Reclamation {

    private final String gravite; // spécificité voirie : "majeure", "mineure"...

    public ReclamationVoirie(int id, String description, String quartier, String gravite) {
        super(id, description, quartier);   // construit la partie "mère" (1ère ligne !)
        this.gravite = gravite;
    }

    @Override
    public void traiter() {
        changerStatut("EN_COURS");          // la méthode protected de la mère
        System.out.println("#" + getId() + " [VOIRIE/" + gravite + "] équipe envoyée : " + getDescription());
    }
}
```

```java
// Fichier : ReclamationProprete.java
public class ReclamationProprete extends Reclamation {

    private final boolean recyclable; // spécificité propreté

    public ReclamationProprete(int id, String description, String quartier, boolean recyclable) {
        super(id, description, quartier);
        this.recyclable = recyclable;
    }

    @Override
    public void traiter() {
        changerStatut("EN_COURS");
        String tri = recyclable ? "tri à faire" : "collecte standard";
        System.out.println("#" + getId() + " [PROPRETE] " + tri + " : " + getDescription());
    }
}
```

```java
// Fichier : ReclamationEclairage.java
public class ReclamationEclairage extends Reclamation {

    private final String numeroLampadaire; // spécificité éclairage

    public ReclamationEclairage(int id, String description, String quartier, String numeroLampadaire) {
        super(id, description, quartier);
        this.numeroLampadaire = numeroLampadaire;
    }

    @Override
    public void traiter() {
        changerStatut("EN_COURS");
        System.out.println("#" + getId() + " [ECLAIRAGE] intervention au lampadaire "
            + numeroLampadaire + " (" + getQuartier() + ")");
    }
}
```

Le programme principal — la seule boucle où le polymorphisme fait tout le travail :

```java
// Fichier : MainSignal.java
public class MainSignal {
    public static void main(String[] args) {
        // Un tableau de type MÈRE, rempli d'objets FILLES :
        Reclamation[] recs = {
            new ReclamationVoirie(1, "Nid de poule", "Medina", "majeure"),
            new ReclamationProprete(2, "Dépôt sauvage", "Fass", true),
            new ReclamationEclairage(3, "Lampadaire éteint", "Sicap", "L-42")
        };

        for (Reclamation r : recs) {
            r.traiter();   // ← POLYMORPHISME : chaque objet exécute SA version de traiter()
        }
    }
}
```

Sortie attendue :

```text
#1 [VOIRIE/majeure] équipe envoyée : Nid de poule
#2 [PROPRETE] tri à faire : Dépôt sauvage
#3 [ECLAIRAGE] intervention au lampadaire L-42 (Sicap)
```

---

## 4. Bonnes pratiques modernes (2025-2026)

1. **Test « est-un » systématique** : pas de `extends` tant que la phrase « X est un Y » n'est pas naturellement vraie et stable.
2. **Toujours `@Override`** : jamais une redéfinition sans l'annotation — le compilateur devient votre filet de sécurité.
3. **Hiérarchies courtes** : 2 niveaux suffisent presque toujours. Au-delà, découpez autrement (interfaces, composition).
4. **`protected` avec parcimonie** : c'est une fenêtre ouverte aux filles — on l'utilise pour des outils de la famille (`changerStatut`), pas par facilité.
5. **En 2025-2026** : les **sealed classes** (leçon 04) remplacent souvent l'héritage libre quand on veut un ensemble **fermé** de sous-types — gardez l'héritage pour les hiérarchies réellement ouvertes.
6. **Liskov dans le sang** : une fille ne rétrécit jamais les promesses de la mère (elle peut faire au moins pareil).

---

## 5. Pièges à éviter

### Piège 1 — Oublier `@Override` (la redéfinition silencieuse)

```java
// ❌ MAUVAIS : faute de frappe → Java crée une NOUVELLE méthode, sans erreur !
public void treater() {   // personne n'est averti ; la mère n'est jamais redéfinie
    ...
}

// ✅ BON : le compilateur vérifie que la mère possède bien "traiter"
@Override
public void traiter() { ... }
```

### Piège 2 — Héritage juste pour « ne pas réécrire le code »

```java
// ❌ MAUVAIS : "EstimationInsee" hérite de Reclamation pour récupérer l'id...
// c'est une ESTIMATION, pas une réclamation : la relation "est-un" est fausse.
class EstimationInsee extends Reclamation { ... }

// ✅ BON : la composition — la classe A une réclamation, ou une classe de base commune
class EstimationInsee {
    private final Reclamation source; // "a une" réclamation comme donnée
}
```

*Pourquoi c'est grave* : tout code qui manipulait des `Reclamation` recevra soudain des `EstimationInsee` polymorphes — comportements absurdes garantis (Liskov bafoué).

### Piège 3 — Appeler une méthode redéfinissable depuis le constructeur de la mère

```java
// ❌ DANGEREUX : le constructeur de la mère appelle afficher()...
public Reclamation(...) {
    ...
    afficher(); // si une fille redéfinit afficher(), elle s'exécute AVANT
}               // que ses champs soient initialisés → comportement surprenant

// ✅ BON : le constructeur construit ; l'affichage est appelé APRÈS le new()
```

### Piège 4 — Hiérarchie qui viole Liskov

```java
// ❌ MAUVAIS : la mère promet que traiter() passe l'objet en EN_COURS,
// mais cette fille refuse de le faire → le polymorphisme devient piégeux
@Override
public void traiter() { throw new IllegalStateException("Non géré"); }

// ✅ BON : si la fille ne peut pas honorer le contrat, elle ne devrait pas
//    hériter de cette mère — repensez la hiérarchie ou utilisez une interface
```

---

## Checklist de validation

Avant de passer à la leçon 03, vérifiez que vous savez :

- [ ] Créer une hiérarchie `extends` et expliquer qui hérite de quoi.
- [ ] Écrire un constructeur fils avec `super(...)` en première instruction.
- [ ] Redéfinir une méthode avec `@Override` et expliquer pourquoi l'annotation est vitale.
- [ ] Démontrer le polymorphisme avec un tableau du type mère rempli d'objets filles.
- [ ] Réciter le tableau des modificateurs d'accès (private / rien / protected / public).
- [ ] Appliquer le test « est-un » et réciter la règle « favorise la composition sur l'héritage ».
- [ ] Avoir construit la hiérarchie SignalCUA (Étape 2 du fil rouge) : trois filles, trois traitements différents, une seule boucle.

➡️ **Prochaine étape** : remarquez un détail dans le code de cette leçon — le polymorphisme exige que la **mère** possède déjà un corps de `traiter()` (même générique). Or il existe des cas où la mère **n'a rien de sensé à écrire** : elle ne fait que *promettre* « je sais traiter ». C'est exactement le rôle de l'**interface** (contrat pur) et de la **classe abstraite** (base partielle) — sujet de la leçon 03.





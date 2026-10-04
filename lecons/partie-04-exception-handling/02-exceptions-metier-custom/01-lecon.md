# Leçon 02 — Exceptions métier custom : nommer les problèmes de SignalCUA

> 🧭 **Pont depuis la leçon 01** : vous savez maintenant **lever** une exception (`throw`), la **déclarer** (`throws`) et l'**attraper** (`try/catch`). Mais jusqu'ici, vous utilisez des exceptions **génériques** du langage : `IllegalArgumentException`, `IllegalStateException`. Le problème : ces types décrivent *la mécanique* (« l'argument est mauvais », « l'état est mauvais »), **pas le métier**. Dans les logs, `IllegalArgumentException` et `NullPointerException` se ressemblent ; impossible de savoir qu'il s'agit, par exemple, d'une **réclamation introuvable**. Cette leçon crée nos propres exceptions **métier**, nommées et porteuses de contexte — celles que la partie 7 transformera en réponses HTTP précises.

---

## 1. Objectifs d'apprentissage

À la fin de cette leçon, vous saurez :

- Distinguer une exception **technique** (bug / infrastructure) d'une exception **métier** (règle de gestion violée).
- Créer une exception personnalisée qui **étend `RuntimeException`**, avec un **message** et un **contexte** (par exemple l'identifiant concerné).
- Construire une **hiérarchie** d'exceptions métier et expliquer **pourquoi** (attraper toute la famille d'un coup).
- **Chaîner une cause** (`new MonException("msg", cause)`) pour ne jamais perdre l'erreur d'origine.
- Choisir entre **checked** et **unchecked** pour une exception métier, et savoir **pourquoi** on préfère *unchecked*.
- Faire remonter l'exception jusqu'à une **frontière** (le point unique où on la traite) — la base de la partie 7 (`@ControllerAdvice`).

---

## 2. Explication simple

### 2.1 Deux natures d'erreur : technique et métier

Toutes les erreurs d'un programme ne se valent pas. Il faut distinguer :

| Type d'erreur | Exemples | Qui peut la corriger | Comment la signaler |
|---|---|---|---|
| **Technique** | base de données injoignable, fichier corrompu, `NullPointerException` | l'infrastructure / le développeur | exception standard (`IOException`, `NullPointerException`) |
| **Métier** | réclamation inexistante, transition de statut interdite, valeur hors bornes | l'**utilisateur** ou la **règle de gestion** | exception **métier custom** |

**Analogie** : imaginez un guichet. Si l'**ascenseur** est en panne, c'est un problème **technique** (on répare l'immeuble). Si le **billet présenté est invalide**, c'est un problème **métier** (on explique au client, on ne répare rien). Confondre les deux revient à dire au client « ascenseur en panne » alors que son billet est périmé : le message ne **désigne pas le bon problème**.

**Pourquoi créer nos propres exceptions ?** Parce que **le type d'une exception est un contrat** : il dit **ce qui s'est passé**, de façon exploitable par le code appelant. Avec `IllegalStateException`, l'appelant ne sait pas *quel* problème traiter. Avec `ReclamationNotFoundException`, il sait **exactement** quoi faire (renvoyer un 404, par exemple).

### 2.2 Créer une exception métier : deux ingrédients

Une exception custom est une **classe** qui **hérite** de `RuntimeException` (pour être *unchecked*, voir 2.4). Elle a presque toujours **deux éléments** :

1. un **message** clair (transmis au constructeur parent via `super(...)`) ;
2. un **contexte** : les données qui expliquent *quelle* entité, *quelle* valeur… (stockées dans un champ, par exemple `id`).

```java
// Une exception métier = une classe qui hérite de RuntimeException.
public class ReclamationNotFoundException extends RuntimeException {

    private final int id;               // le CONTEXTE : quelle réclamation est absente ?

    public ReclamationNotFoundException(int id) {
        super("Réclamation " + id + " introuvable");  // le MESSAGE (lisible)
        this.id = id;                                  // on garde l'id pour l'appelant
    }

    public int getId() {                // permet au code appelant de LIRE le contexte
        return id;
    }
}
```

**Comment se lit le code** : `extends RuntimeException` = « je suis une exception *unchecked* » (section 2.4). `super("...")` = « je transmets mon message à la classe mère `RuntimeException` », qui sait déjà le stocker et l'afficher.

**À quoi sert le champ `id` ?** Le message est destiné à être **lu** (par un humain). Le champ `id` est destiné à être **utilisé** par le code : `catch (ReclamationNotFoundException e) { return problem(Type.NOT_FOUND, e.getId()); }`. Message et contexte ont donc deux rôles différents et complémentaires.

> ⚠️ **À ne pas confondre avec ce que vous connaissez déjà** : on a vu en partie 3 que `Optional` sert à signaler une absence **attendue**. Ici, on fait l'inverse : on **décide** que l'absence est une **erreur**. Les deux sont légitimes, mais pas au même niveau (voir 2.7).

### 2.3 Une hiérarchie d'exceptions métier

SignalCUA a **plusieurs** règles métier qui peuvent échouer. Plutôt que des exceptions isolées, on construit une **famille**, avec une **racine commune** :

```text
SignalcuaException (extends RuntimeException)   <- la racine de TOUTES les erreurs métier
├── ReclamationNotFoundException               <- identifiant inconnu
├── ReclamationInvalideException               <- donnée invalide
└── TransitionStatutInterditeException         <- NOUVELLE -> RESOLUE impossible
```

**Pourquoi une racine commune ?** Pour offrir **deux niveaux de capture** :

```java
try {
    service.demarrer(id);
} catch (ReclamationNotFoundException e) {     // capture PRÉCISE (un seul cas)
    // je traite ce cas particulier
}
```

ou

```java
try {
    service.demarrer(id);
} catch (SignalcuaException e) {               // capture toute la FAMILLE métier
    // je traite TOUTES mes erreurs métier d'un seul bloc (par ex. en noter une trace)
}
```

**Analogie** : `SignalcuaException` est le nom de **famille** (« incident SignalCUA ») ; ses filles sont les **types précis** (« réclamation absente », « donnée invalide »). Un tableau de bord peut compter « tous les incidents SignalCUA », ou détailler par type. C'est exactement la même hiérarchie que celle vue en partie 2 (`Reclamation` → ses sous-types).

**Deux règles de conception :**
- **La racine hérite de `RuntimeException`** → toutes les filles sont *unchecked* (aucun `throws` à écrire partout).
- **Une exception = une seule situation.** `ReclamationNotFoundException` ne doit **jamais** servir pour une donnée invalide : le type doit rester **sans ambiguïté**.

### 2.4 Checked ou unchecked pour une exception métier ?

C'est **le** choix de conception à faire pour chaque exception métier. Rappel de la leçon 01 : *checked* = le compilateur impose de la déclarer ; *unchecked* = non.

| Choix | Ce que ça implique | Quand le choisir |
|---|---|---|
| **unchecked** (`extends RuntimeException`) | aucun `throws` dans les signatures ; l'exception remonte jusque la **frontière** sans laisser de trace dans le code | **règle métier** (identifiant inconnu, transition invalide…) |
| **checked** (`extends Exception`) | chaque méthode traversée doit **déclarer** `throws MonException` | **I/O bas niveau réellement récupérable** |

**La recommandation 2025-2026** (et celle de la roadmap) : pour une règle **métier**, **unchecked**. La raison est pratique : une exception checked **pollue les signatures**. Si `demarrer()` peut lever `ReclamationNotFoundException`, alors **toute** méthode qui l'appelle doit `throws ReclamationNotFoundException`, et ainsi de suite — jusqu'au `main`. Au bout de trois couches, vos signatures deviennent illisibles pour un bénéfice nul : personne ne sait quoi faire d'une « réclamation introuvable » **en plein milieu du code**. En revanche, à la **frontière** (le contrôleur web), on sait exactement quoi faire : renvoyer 404.

> 🧠 **À retenir** : *unchecked* pour le métier, *checked* pour l'I/O bas niveau récupérable. C'est ce que fait tout l'écosystème Spring.

### 2.5 Ne jamais perdre la cause (le chaînage)

Quand une exception en **provoque** une autre, il faut **transmettre la cause**. Sinon, l'information d'origine disparaît.

```java
// ❌ MAUVAIS : la cause réelle (NumberFormatException) est PERDUE
try {
    int delai = Integer.parseInt(texte);
} catch (NumberFormatException e) {
    throw new ReclamationInvalideException("Délai invalide : " + texte);  // sans cause !
}

// ✅ BON : on passe la cause en 2e argument
try {
    int delai = Integer.parseInt(texte);
} catch (NumberFormatException e) {
    throw new ReclamationInvalideException("Délai invalide : " + texte, e);  // cause conservée
}
```

**Comment le vérifier ?** La méthode `getCause()` renvoie l'exception d'origine, et la stack trace affiche alors une ligne **`Caused by: java.lang.NumberFormatException …`**. C'est **la** ligne qu'on cherche en production pour comprendre la vraie raison.

**Analogie** : transmettre la cause, c'est **transmettre le dossier médical complet** au médecin suivant, pas seulement dire « le patient ne va pas bien ».

### 2.6 Propager jusqu'à la « frontière »

Une exception métier ne doit **pas** être attrapée n'importe où. On la **laisse remonter** jusqu'à une **frontière** : le point d'entrée de l'application (un `main`, un contrôleur REST, une route…). Là, **et seulement là**, on la traduit vers le monde extérieur.

```text
[ Controller ]  <- FRONTIÈRE : traduit l'exception en réponse HTTP (404, 409...)
      |
[ Service ]     <- leve ReclamationNotFoundException (ne l'attrape PAS)
      |
[ Repository ]  <- constate l'absence (Optional) ou leve l'exception
```

**Pourquoi ne pas attraper « au milieu » ?** Parce qu'une couche intermédiaire ne sait pas **quoi faire** d'une règle métier violée : elle n'a ni le contexte HTTP, ni la décision à prendre. En laissant remonter, on centralise le traitement en **un seul endroit** — exactement le motif `@ControllerAdvice` de la partie 7.

> 📖 **Vocabulaire** : **frontière** (*boundary*) = le point où votre code parle au monde extérieur (utilisateur, autre système). C'est là qu'on traduit les exceptions internes en messages externes.

### 2.7 Rappel de la partie 3 : `Optional` ou exception ?

Les deux approches coexistent. Règle simple :

- **Absence « normale »** (un `findById` public qui *peut* ne rien trouver, sans que ce soit un drame) → renvoyez `Optional<Reclamation>` (partie 3).
- **Absence « anormale »** (on attend une réclamation précise, son absence est une **erreur**) → **levez** `ReclamationNotFoundException`.

*Exemple concret* : une page « liste des réclamations » peut afficher une liste vide ; une page « détail de la réclamation n° 42 » **lève** une exception si elle manque — car il n'y a rien à afficher.

La roadmap (Étape 4) demandera **les deux variantes** pour comparer : `findById` qui renvoie `Optional`, et une variante qui **lève**. C'est un bon exercice de jugement, pas un concours de « bonne façon ».

---

## 📖 Vocabulaire / Abréviations

| Terme | Définition d'une ligne |
|---|---|
| **Exception métier** | Exception qui signale une **règle de gestion** violée (identifiant inconnu, transition interdite), par opposition à une erreur technique. |
| **Exception technique** | Exception liée à l'infrastructure ou à un bug (`IOException`, `NullPointerException`) : ni l'utilisateur ni la règle métier ne la « corrigent ». |
| **Exception custom** | Exception que **vous** écrivez pour nommer un problème de votre domaine. |
| **Hiérarchie d'exceptions** | Ensemble d'exceptions reliées par héritage, avec une **racine commune** (`SignalcuaException`). |
| **Cause** | L'exception d'origine à l'origine d'une autre ; transmise via `super(message, cause)`. |
| **Chaînage** | Fait de relier une nouvelle exception à sa cause (visible par `getCause()` / `Caused by:`). |
| **Contexte** | Données métier portées par l'exception (par ex. `id`) pour permettre une réaction précise. |
| **Frontière** (*boundary*) | Le point d'entrée où l'on traduit les exceptions internes vers l'extérieur (contrôleur, `main`). |
| **Propager** | Laisser une exception remonter sans l'attraper. |
| **`RuntimeException`** | La classe mère des exceptions *unchecked* (aucun `throws` imposé). |
| **`getMessage()` / `getCause()`** | Méthodes de `Throwable` : lire le message / la cause d'une exception. |
| **`super(...)`** | Appel au constructeur de la classe mère ; transmet message (et cause) à `RuntimeException`. |
| **`@ControllerAdvice`** | Mécanisme Spring (partie 7) qui centralise la traduction des exceptions en réponses HTTP. |
| **`Caused by:`** | Ligne d'une stack trace qui affiche la cause d'origine. |

---

## 3. Exemples concrets

> 🔗 **Comment cette section s'articule** : la section 2 a *expliqué* la théorie (`SignalcuaException`, hiérarchie, cause, frontière). Ici, on l'applique au fil rouge, en **code exécuté**. Ces fichiers sont exactement ceux de `02-exercice.md`.

### 3.1 La racine commune

```java
// SignalcuaException.java — la racine de TOUTES les erreurs métier de SignalCUA.
// "unchecked" : elle etend RuntimeException, donc aucun `throws` n'est impose.
public class SignalcuaException extends RuntimeException {

    public SignalcuaException(String message) {
        super(message);
    }

    public SignalcuaException(String message, Throwable cause) {
        super(message, cause); // on conserve la cause d'origine
    }
}
```

**Pourquoi deux constructeurs ?** Le premier sert quand il n'y a rien à chaîner ; le second quand une autre exception **cause** la nôtre. Avoir les deux évite de « perdre » une cause par facilité.

### 3.2 Les trois exceptions métier

```java
// ReclamationNotFoundException.java — un identifiant demande n'existe pas.
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
// ReclamationInvalideException.java — une donnée fournie est invalide.
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

**Ce que ces trois classes montrent** : chacune **nomme** un problème précis. On peut les attraper **individuellement** (traitement spécifique) ou **par famille** via `SignalcuaException` (traitement global). C'est tout l'intérêt de la racine commune.

### 3.3 Le registre qui lève (au lieu de renvoyer `Optional`)

```java
// RegistreReclamations.java (extrait)
// Variante QUI LEVE : l'appelant n'a pas a gerer l'absence lui-meme.
public Reclamation findById(int id) {
    Reclamation r = parId.get(id);
    if (r == null) {
        throw new ReclamationNotFoundException(id);  // absence = erreur, pas un resultat
    }
    return r;
}
```

**À comparer avec la partie 3** : la même méthode renvoyait `Optional<Reclamation>`, et l'appelant décidait (`orElse`, `orElseThrow`…). Ici, on a **déplacé la décision** dans le registre : « pas trouvé = erreur ». Les deux sont valides — le bon choix dépend de **qui** doit décider (voir 2.7).

### 3.4 Capturer par type précis, ou par famille

```java
// Par FAMILLE : un seul bloc pour toutes les erreurs métier.
try {
    service.demarrer(99);
} catch (SignalcuaException e) {          // attrape N'IMPORTE QUELLE erreur métier
    System.out.println("Incident SignalCUA : " + e.getMessage());
}

// Par TYPE PRÉCIS : un traitement par cas.
try {
    service.demarrer(99);
} catch (ReclamationNotFoundException e) {
    System.out.println("Introuvable (id=" + e.getId() + ")"); // on utilise le CONTEXTE
} catch (TransitionStatutInterditeException e) {
    System.out.println("Transition refusée : " + e.getMessage());
}
```

**Rappel (leçon 01)** : la capture **précise** se place **avant** la capture **générale**, sinon le code ne compile pas (`has already been caught`).

---

## 4. Bonnes pratiques modernes (2025-2026)

1. **Nommez l'erreur par le métier, pas par la technique.** `ReclamationNotFoundException` (métier) plutôt que `DataNotFoundException` (technique vague). Le nom doit se lire comme une **phrase du domaine**.
2. **`extends RuntimeException` pour les exceptions métier.** C'est le choix de tout l'écosystème Spring : on ne veut pas polluer les signatures avec des `throws` en cascade.
3. **Une exception = une situation.** Ne réutilisez pas `ReclamationNotFoundException` pour deux causes différentes. Si vous hésitez sur le nom, c'est souvent qu'il y a **deux** exceptions à créer.
4. **Transmettez toujours la cause** quand vous transformez une exception (`super(message, cause)`). Sinon, la stack trace devient un cul-de-sac.
5. **Le message est destiné à l'humain ; le contexte au code.** Message court et clair + champ (`id`) exploitable programmatiquement.
6. **Pas de données sensibles dans le message.** Un message d'exception peut finir dans des logs ou une réponse HTTP : ne mettez ni mot de passe, ni donnée personnelle brute.
7. **Faites remonter jusqu'à la frontière.** Le service **lève** ; le contrôleur (partie 7) **traduit** en HTTP. Ne mélangez pas les responsabilités.
8. **Documentez la hiérarchie.** Un petit schéma (comme en 2.3) dans le code ou le README aide toute l'équipe : « quelle erreur pour quelle situation ? ».

---

## 5. Pièges à éviter

> 🎯 **Comment lire les pièges** : pour chacun, un **❌ MAUVAIS**, **pourquoi** c'est dangereux, puis la **✅ version correcte** juste à côté.

### Piège 1 — Faire de ses exceptions métier des exceptions *checked*

```java
// ❌ MAUVAIS : chaque méthode traversee doit déclarer `throws ReclamationNotFoundException`
public class ReclamationNotFoundException extends Exception { ... }

// -> desormais : demarrer() throws ..., resoudre() throws ..., main() throws ...
//    et ÇA ne s'arrête jamais : la signature devient illisible partout.

// ✅ BON : unchecked -> aucune signature a polluer
public class ReclamationNotFoundException extends RuntimeException { ... }
```

**Pourquoi** : une exception métier *checked* impose un `throws` **à toute la chaîne d'appels**, pour un bénéfice nul. Réservez les *checked* à l'I/O bas niveau.

### Piège 2 — Re-emballer SANS transmettre la cause

```java
// ❌ MAUVAIS : la cause réelle disparait
try {
    int delai = Integer.parseInt(texte);
} catch (NumberFormatException e) {
    throw new ReclamationInvalideException("Délai invalide : " + texte);
}

// ✅ BON : la cause est conservee (2e argument)
} catch (NumberFormatException e) {
    throw new ReclamationInvalideException("Délai invalide : " + texte, e);
}
```

**Pourquoi** : sans la cause, impossible de savoir *pourquoi* le délai était invalide. En production, on cherche **toujours** la ligne `Caused by:`.

### Piège 3 — Une exception sans message

```java
// ❌ MAUVAIS : la stack trace dit "ReclamationNotFoundException" mais RIEN de plus
throw new ReclamationNotFoundException(id);

// ✅ BON : le message donne le CONTEXTE directement dans le log
throw new ReclamationNotFoundException(id); // constructeur => "Réclamation 42 introuvable"
```

**Pourquoi** : un message vide oblige à remonter le code pour comprendre. Le constructeur de `ReclamationNotFoundException` **construit** déjà un message riche : c'est le bon réflexe.

### Piège 4 — Utiliser une exception générique quand une précise existe

```java
// ❌ MAUVAIS : pourquoi "illegal state" ? quel etat ?
if (r == null) {
    throw new IllegalStateException("probleme");
}

// ✅ BON : le type dit exactement ce qui s'est passe
if (r == null) {
    throw new ReclamationNotFoundException(id);
}
```

**Pourquoi** : une exception générique pour un cas **prévu par le métier** empêche l'appelant de réagir (renvoyer 404) et rend les logs ambigus.

### Piège 5 — Trop d'exceptions (ou une seule pour tout)

```java
// ❌ MAUVAIS A : une exception par message -> impossible a attraper proprement
throw new RuntimeException("Reclamation 42 introuvable");
throw new RuntimeException("Description obligatoire");
// ❌ MAUVAIS B : une seule exception fourre-tout pour tout le domaine
throw new ReclamationException("n'importe quoi");

// ✅ BON : une petite famille, un type par SITUATION
// ReclamationNotFoundException | ReclamationInvalideException | TransitionStatutInterditeException
```

**Pourquoi** : trop d'exceptions noient l'appelant ; une seule ne dit rien. Visez **une exception par situation métier distincte**.

### Piège 6 — Utiliser les exceptions métier pour le contrôle de flux

```java
// ❌ MAUVAIS : on utilise une exception pour "savoir si ça existe"
boolean existe;
try {
    registre.findById(id);
    existe = true;
} catch (ReclamationNotFoundException e) {
    existe = false;
}

// ✅ BON : une recherche qui peut legitimement rater renvoie Optional
boolean existe = registre.findByIdOptional(id).isPresent();
```

**Pourquoi** : si l'absence est un **résultat normal** de la question, c'est `Optional` (partie 3). Si c'est une **erreur** attendue par le métier, c'est une exception. Ne détournez pas l'outil.

### Piège 7 — Avaler une exception métier dans une couche intermédiaire

```java
// ❌ MAUVAIS : la couche service cache l'erreur -> le contrôleur renverra 200 par erreur
public void demarrer(int id) {
    try {
        registre.findById(id).demarrerTraitement();
    } catch (SignalcuaException e) {
        // ignoree...
    }
}

// ✅ BON : on laisse remonter jusqu'à la frontière
public void demarrer(int id) {
    registre.findById(id).demarrerTraitement();
}
```

**Pourquoi** : avaler l'exception au milieu trompe l'utilisateur (l'API dira « succès » alors que rien ne s'est passé). Laissez la frontière (partie 7) décider du code HTTP.

---

## Checklist de validation

Avant de quitter la partie 4, vérifiez que vous savez faire **chacun** de ces points :

- [ ] Donner un exemple d'erreur **technique** et un exemple d'erreur **métier**, et expliquer la différence.
- [ ] Créer une exception custom qui `extends RuntimeException`, avec un `super("message")`.
- [ ] Ajouter un **contexte** (`private final int id`) et son getter, et dire à quoi il sert par rapport au message.
- [ ] Construire une **hiérarchie** avec une racine commune, et expliquer les **deux niveaux de capture** (précise / famille).
- [ ] Choisir **unchecked** pour une exception métier et justifier (pas de pollution des signatures).
- [ ] **Chaîner une cause** (`super(message, cause)`) et montrer la ligne `Caused by:` dans une stack trace.
- [ ] Expliquer le rôle d'une **frontière** et citer `@ControllerAdvice` comme traduction future en HTTP.
- [ ] Décider, pour une absence, entre **`Optional`** et **exception**, avec un exemple de chaque.
- [ ] Écrire `throw new MaExceptionMetier(...)` là où une règle métier est violée.

---

## 🔴 Fil rouge — ou en est SignalCUA ?

SignalCUA dispose désormais d'une **hiérarchie d'exceptions métier** : `SignalcuaException` (racine) → `ReclamationNotFoundException`, `ReclamationInvalideException`, `TransitionStatutInterditeException`. Le registre **lève** au lieu de renvoyer `null`, la `Reclamation` **valide** ses données et ses transitions, et le service **propage** jusqu'à une frontière. C'est l'**Étape 4 — robustifier SignalCUA** (voir `lecons/fil-rouge-signalcua.md`).

---

➡️ **Prochaine étape** : la partie 4 est terminée. SignalCUA « échoue proprement », avec des erreurs **nommées**. La **partie 5 — Programmation fonctionnelle & Stream API** change de sujet, mais pas de projet : au lieu d'écrire des boucles `for`, on va **transformer et agréger** les collections de réclamations (`filter`, `map`, `Collectors`) — exactement le vocabulaire que vous avez déjà croisé avec `Optional` et la lecture de fichier. Le sujet suivant n'est pas un hasard : la partie 5 s'appuie sur tout ce que vous venez de consolider (objets, collections, `Optional`, exceptions).

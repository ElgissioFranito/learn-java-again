# Leçon 03 — Optional : dire proprement « il n'y a rien »

> 🧭 **Pont depuis la leçon 02** : vous savez maintenant écrire `Optional<T>`… pardon : vous savez écrire des types génériques comme `<T>`, et vous avez vu que `Optional<T>` apparaît partout dans les bibliothèques (Spring, JPA). Cette leçon lui est consacrée, car elle répond au **problème laissé ouvert depuis la leçon 01** : `registre.findById(99)` renvoie `null`, et rien n'oblige l'appelant à s'en occuper. Vous avez d'ailleurs écrit ce `null` vous-même dans la correction de l'exercice 01, avec un commentaire « la leçon 03 remplacera ce null par Optional ». C'est exactement ce que nous faisons ici.

---

## 1. Objectifs d'apprentissage

À la fin de cette leçon, vous saurez :

- Expliquer **pourquoi** `null` est un problème de conception (et pas seulement une valeur pratique).
- Créer un `Optional` avec `of`, `ofNullable` et `empty`, et dire quand utiliser lequel.
- **Extraire** une valeur proprement : `orElse`, `orElseGet`, `orElseThrow`, `ifPresent`, `ifPresentOrElse`.
- **Transformer** un `Optional` avec `map`, `filter` et `flatMap`.
- Expliquer la différence **`orElse` (avide) / `orElseGet` (paresseux)** — démontrée par l'exécution.
- Identifier les **anti-patterns** : `get()` nu, `Optional` dans un champ ou un paramètre, `Optional<List<T>>`.
- Reconnaître le motif `findById` de Spring Boot et l'écrire vous-même dans SignalCUA.

---

## 2. Explication simple

### 2.1 Le problème : `null`, l'information manquante

**Pourquoi ?** Une collection, une base de données, une API ont tous un point commun : **on ne trouve pas toujours ce qu'on cherche**. La réclamation n° 99 n'existe pas. Un `Map`, par exemple, répond alors `null`… c'est-à-dire « rien », mais un « rien » que **rien ne signale** dans le type de retour.

Regardez la conséquence, telle qu'elle se produit réellement :

```java
public Reclamation findById(int id) {
    return parId.get(id);      // renvoie null si inconnu : RIEN ne le dit dans la signature
}

// ... ailleurs, 200 lignes plus loin, dans une autre classe :
String description = registre.findById(99).getDescription();
// ⛔ NullPointerException ICI, alors que la CAUSE est 200 lignes plus haut
```

**Analogie** : `null`, c'est **une boîte scellée sans étiquette**. Elle peut contenir une pièce… ou être vide. Vous ne le savez qu'en l'ouvrant — et si vous ne vous y attendiez pas, vous vous coupez. Le compilateur ne vous a rien dit, la documentation ne vous a rien dit, le nom de la méthode ne vous a rien dit.

**Le coût réel** : le message d'erreur désigne la **victime**, jamais **le coupable**. C'est le débuggage le plus coûteux qui existe — celui où l'on remonte tout un chemin d'appels à la main.

**Comment ?** `Optional<T>` change la donne en **mettant l'absence dans le type de retour** :

```java
public Optional<Reclamation> findById(int id) {
    return Optional.ofNullable(parId.get(id));   // "il y a peut-être une réclamation"
}

// ... ailleurs :
String description = registre.findById(99).getDescription();
// ⛔ ERREUR DE COMPILATION : Optional<Reclamation> ne possède pas de méthode getDescription()
```

Le programme ne compile plus ! Voilà toute la différence : le problème n'est plus **découvert à l'exécution**, il est **imposé par le type**. L'appelant est obligé de décider quoi faire quand il n'y a rien.

> 📖 **Vocabulaire** : **`null`** = référence vers aucune valeur (leçon « objets » de la partie 1). **`NullPointerException` (NPE)** = l'exception levée quand on appelle une méthode sur `null`. **Type de retour** = le type annoncé par la méthode : c'est le seul endroit où le compilateur peut nous avertir. **Contrat d'API** = ce que la méthode promet à l'appelant (ici : « peut-être rien »).

### 2.2 Qu'est-ce qu'un `Optional`, exactement ?

**Analogie** : un `Optional<Reclamation>`, c'est **une enveloppe** qui contient soit **une** réclamation, soit **rien**. Une enveloppe ne peut pas contenir deux lettres (ce n'est pas une collection) et, une fois fermée, elle ne change plus (elle est **immuable**).

**Comment ?** Techniquement, c'est une **classe générique** (leçon 02 !) avec exactement deux situations :

| Situation | Vocabulaire | Comment on la crée |
|---|---|---|
| L'enveloppe contient une valeur | **présent** (*present*) | `Optional.of(valeur)` |
| L'enveloppe est vide | **absent** ou **vide** (*empty*) | `Optional.empty()` |
| On ne sait pas encore (la valeur peut être `null`) | — | `Optional.ofNullable(valeur)` |

```java
Optional<Reclamation> a = Optional.of(r1);                  // r1 ne doit PAS être null (sinon exception)
Optional<Reclamation> b = Optional.ofNullable(peutEtreNull); // accepte null -> Optional.empty()
Optional<Reclamation> c = Optional.empty();                 // l'absence explicite
```

**Quand ?** `Optional` a **une** vocation principale : être le **type de retour** d'une méthode qui peut légitimement ne rien trouver (recherche, recherche par clé, « le premier élément qui… »). Nous verrons en section 2.7 tout ce qu'il **ne faut pas** en faire.

> 📖 **Vocabulaire** : **immuable** = qu'on ne peut pas modifier après création (les `record`s et les dates `java.time` le sont aussi). **Classe *value-based*** = classe dont les objets ne portent que des valeurs et ne doivent jamais être comparés avec `==` (utilisez `equals`) ; `Optional` en est une.

### 2.3 Extraire la valeur : les cinq bons gestes

**Pourquoi ?** Un `Optional` ne sert à rien si on l'ouvre à la hache (`get()`) — on aurait simplement déplacé le problème. Il faut des **gestes** qui expriment l'intention : « prends-la ou prends autre chose », « prends-la sinon préviens-moi », « si elle est là, fais ceci ».

**Comment ?** Les cinq méthodes à connaître, du cas le plus courant au plus rare :

| Geste | Ce qu'il exprime | Exemple |
|---|---|---|
| `orElse(valeur)` | « ou bien cette valeur de repli » | `findById(id).orElse(inconnue)` |
| `orElseGet(() -> valeur)` | « ou bien **calcule** une valeur de repli » (paresseux) | `findById(id).orElseGet(() -> creer())` |
| `orElseThrow(() -> exception)` | « ou bien lève une exception **expliquée** » | `.orElseThrow(() -> new IllegalStateException("…"))` |
| `ifPresent(valeur -> bloc)` | « s'il y a une valeur, agis avec elle » | `findById(id).ifPresent(r -> r.demarrer())` |
| `ifPresentOrElse(bloc, bloc)` | « agis dans les **deux** cas » (Java 9) | `.ifPresentOrElse(r -> traiter(r), () -> alerter())` |

```java
// Avant (le style "null") : obligé de tester, et d'expliquer en commentaire ce que fait le else
Reclamation r = registre.findById(id);
String description = (r != null) ? r.getDescription() : "inconnue";

// Après (le style Optional) : l'intention est dans le code
String description = registre.findById(id).map(x -> x.getDescription()).orElse("inconnue");
```

> ⚠️ **Ne confondez pas `orElseThrow()` (Java 10, sans argument) et `orElseThrow(supplier)`.** Le premier lève un `NoSuchElementException` **technique** ; le second vous laisse écrire un message **métier**. Dans du code professionnel, on choisit presque toujours le second.

> 📖 **Vocabulaire** : **supplier** (*fournisseur*) = une fonction sans argument qui **produit** une valeur (`() -> new IllegalStateException("…")`) — la syntaxe `(…) -> …` s'appelle une **lambda** et sera détaillée en partie 5. **Paresseux (*lazy*)** = qui ne s'exécute que si c'est nécessaire. **Avide (*eager*)** = qui s'exécute **avant** même de savoir s'il servira.

### 2.4 Transformer : `map`, `filter`, `flatMap`

**Pourquoi ?** Le principal bénéfice d'`Optional` est de pouvoir **enchaîner les opérations** sans jamais écrire un `if (x != null)`. On travaille « dans l'enveloppe » : si elle est vide, la chaîne s'arrête d'elle-même, **sans exception**.

**Comment ?**

```java
// map : transformer la valeur SI elle existe (sinon on reste vide)
registre.findById(99).map(r -> r.getDescription()).orElse("aucune");

// filter : ne garder la valeur que si elle passe un test (sinon on devient vide)
registre.findById(2).filter(r -> r.getPriorite() == Priorite.URGENTE).isPresent();

// flatMap : quand la transformation rend ELLE-MÊME un Optional (pour éviter Optional<Optional<…>>)
registre.findById(1).flatMap(r -> registre.findById(r.getId() + 1));
```

**Analogie** : `map` est un **tapis roulant** qui transforme le contenu de l'enveloppe quand il y en a un ; `filter` est un **portier** qui refuse le contenu s'il ne remplit pas la condition ; `flatMap` est le même tapis roulant mais qui **retire une enveloppe en trop** (sinon on obtiendrait une enveloppe dans une enveloppe).

**La différence `map` / `flatMap` en une image** : si votre transformation rend une valeur « nue », `map` suffit ; si elle rend **déjà** un `Optional` (comme une nouvelle recherche), utilisez `flatMap` — exactement comme pour les `Stream` (partie 5).

### 2.5 `orElse` contre `orElseGet` : la preuve par l'exécution

**Pourquoi ?** Ces deux méthodes se ressemblent tellement qu'on les confond — avec un coût bien réel : `orElse` **évalue son argument avant l'appel**, qu'il serve ou non.

**Comment ?** Regardez le code de la section 3 (programme réellement exécuté) : la méthode `descriptionParDefaut()` **affiche** un message, ce qui rend son appel visible.

```java
// AVEC orElse : la méthode est appelée MÊME quand la valeur existe
findById(1).map(r -> r.getDescription()).orElse(descriptionParDefaut());

// AVEC orElseGet : la méthode n'est appelée QUE si l'enveloppe est vide
findById(1).map(r -> r.getDescription()).orElseGet(() -> descriptionParDefaut());
```

**Ce que la sortie du programme montre :**

```text
      >> calcul de la description par defaut (elle a bien ete appelee)   ← orElse : appelé pour rien
6) orElse(...)    -> Nid de poule
   orElseGet(...) -> Nid de poule  (aucun calcul ci-dessus : la methode n'a pas ete appelee)
```

La valeur affichée est **la même** dans les deux cas (`Nid de poule`) — mais `orElse` a **travaillé pour rien**. Sur une méthode coûteuse (requête en base, appel réseau, calcul lourd), ce n'est plus un détail : c'est une perte de performance.

**Règle simple** : valeur déjà prête (`"inconnue"`, un `enum`) → `orElse` ; **appel de méthode** ou calcul → `orElseGet`.

### 2.6 Le motif `findById` : celui que vous verrez partout

**Pourquoi ?** `Optional` n'a pas été inventé pour faire joli : c'est **la** convention des bibliothèques Java modernes pour une recherche par identifiant.

**Comment ?** Comparez les deux écritures — la seconde est celle de Spring Data JPA et de toute API bien conçue :

```java
// ❌ API fragile : l'appelant DOIT se souvenir du contrat non écrit
Reclamation findById(int id);                 // peut renvoyer null (mais rien ne le dit)

// ✅ API explicite : le type annonce l'incertitude
Optional<Reclamation> findById(int id);

// Et côté appelant, les trois usages qui couvrent 95 % des cas :
registre.findById(99).map(r -> r.getDescription()).orElse("inconnue");      // repli
registre.findById(99).orElseThrow(() -> new IllegalStateException("…"));   // erreur métier
registre.findById(99).ifPresent(r -> r.demarrerTraitement());              // action
```

**Quand ?** Dès qu'une méthode **cherche** quelque chose : par identifiant, par critère, « le premier qui… ». En partie 8, votre dépôt Spring Data écrira exactement cela :

```java
public interface ReclamationRepository extends JpaRepository<Reclamation, Long> {
    Optional<Reclamation> findById(Long id);      // c'est déjà fourni par le framework !
}
```

### 2.7 Quand NE PAS utiliser `Optional`

**Pourquoi ?** Ailleurs que dans un **type de retour**, `Optional` ajoute de la complexité (emballage/déballage) et parfois des ennuis techniques (sérialisation, mapping base de données). Voici les quatre interdits, chacun justifié.

**Comment ?** Le tableau suivant est la règle de référence (elle vient de la pratique : *Effective Java*, conventions Spring) :

| Interdit | Pourquoi | Que faire à la place |
|---|---|---|
| **Un champ d'objet** (`private Optional<X> champ`) | un champ peut simplement être `null` en interne, c'est l'API qui décide d'exposer une absence ; de plus, ni les ORM (Hibernate) ni les sérialiseurs (Jackson) ne savent le gérer proprement | champ de type `X` (éventuellement `null` en interne), et une **méthode** qui renvoie `Optional<X>` |
| **Un paramètre de méthode** (`void traiter(Optional<X> x)`) | l'appelant est forcé de fabriquer une enveloppe pour… rien ; surchargez ou acceptez `null` avec documentation | deux méthodes (`traiter(X)`, `traiterSansValeur()`) ou un paramètre `@Nullable` |
| **Un champ de collection** (`List<Optional<X>>`) | illisible et coûteux | une `List<X>` et, à la rigueur, `Optional.ofNullable(liste)` |
| **Autour d'une collection** (`Optional<List<X>>`) | une liste **vide** dit déjà « aucun élément » | renvoyez `List.of()` (jamais `null`) — comme dans `parQuartier` de l'exercice 01 |

**Et les performances ?** `Optional` crée un petit objet supplémentaire. C'est **négligeable** pour un type de retour classique et **interdit d'usage** dans une boucle sur des millions d'éléments : la règle « type de retour seulement » protège aussi de ce travers.

---

## 📖 Vocabulaire / Abréviations

| Terme | Définition en une ligne |
|---|---|
| **`Optional<T>`** | Classe générique contenant **soit** une valeur de type `T`, **soit** rien — l'absence est dans le type. |
| **Présent / présent (*present*)** | État d'un `Optional` qui contient une valeur. |
| **Vide / absent (*empty*)** | État d'un `Optional` sans valeur. |
| **`Optional.of(v)`** | Crée un `Optional` présent — **lève une exception si `v` est `null`**. |
| **`Optional.ofNullable(v)`** | Crée un `Optional` présent si `v` n'est pas `null`, vide sinon. |
| **`Optional.empty()`** | Crée un `Optional` vide. |
| **`isPresent()` / `isEmpty()`** | Teste s'il y a une valeur / s'il n'y en a pas (Java 11). |
| **`get()`** | Extrait la valeur, **lève `NoSuchElementException` si vide** → à éviter. |
| **`orElse(v)`** | Renvoie la valeur, ou `v` si vide — **`v` est calculé d'avance (avide)**. |
| **`orElseGet(supplier)`** | Idem, mais la valeur de repli n'est **calculée que si nécessaire** (paresseux). |
| **`orElseThrow(supplier)`** | Renvoie la valeur ou **lève l'exception fournie** (message métier). |
| **`ifPresent(consommateur)`** | Exécute l'action si une valeur est là. |
| **`ifPresentOrElse(a, b)`** | `a` si présent, `b` si vide (Java 9). |
| **`map(fonction)`** | Transforme la valeur si présente ; sinon reste vide. |
| **`filter(test)`** | Garde la valeur si le test passe ; sinon devient vide. |
| **`flatMap(fonction)`** | Comme `map`, mais **aplatit** un résultat déjà `Optional`. |
| **`OptionalInt/Long/Double`** | Variantes pour les primitifs (`int`, `long`, `double`) — évitent l'emballage `Integer`. |
| **`NullPointerException` (NPE)** | Exception levée à l'appel d'une méthode sur `null`. |
| **Null-safety** | Ensemble des pratiques qui évitent les NPE (`Optional`, validations d'entrée, `Objects.requireNonNull`). |
| **Avide (*eager*) / Paresseux (*lazy*)** | Exécuté tout de suite / exécuté seulement si nécessaire. |
| **Lambda** | Fonction écrite en place (`r -> r.getDescription()`), détaillée en partie 5. |
| **Supplier / Consumer / Function** | Interfaces fonctionnelles : produit une valeur / consomme une valeur / transforme une valeur (partie 5). |
| **Classe *value-based*** | Classe dont l'identité ne compte pas : à comparer par `equals`, jamais par `==` (`Optional`, `LocalDate`…). |
| **`NoSuchElementException`** | Exception levée par `get()`/`orElseThrow()` sur un `Optional` vide. |
| **Contrat d'API** | Ce qu'une méthode promet à ses appelants (types, exceptions, cas limites). |
| **ORM / Hibernate** | Outil qui fait le pont objets ↔ base de données (partie 8) : il ne gère pas les `Optional` en champ. |
| **Jackson** | Bibliothèque de conversion objets ↔ JSON (partie 7) : elle sérialise mal les `Optional` en champ. |

---

## 3. Exemples concrets

Nous repartons de SignalCUA. Deux fichiers suffisent : le registre (dont `findById` change de type) et le programme de démonstration. Le code est exactement celui **réellement compilé et exécuté** pour écrire cette leçon.

**Fichier 1 — `RegistreOptional.java` : le retour de `findById` devient honnête.**

```java
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

// Version "Optional" du registre : findById ne renvoie plus null mais Optional<Reclamation>
public class RegistreOptional {

    private final List<Reclamation> toutes = new ArrayList<>();
    private final Map<Integer, Reclamation> parId = new HashMap<>();
    private int prochainId = 1;

    public Reclamation ajouter(String description, String quartier, Priorite priorite) {
        Reclamation r = new Reclamation(prochainId, description, quartier, priorite);
        prochainId++;
        toutes.add(r);
        parId.put(r.getId(), r);
        return r;
    }

    // LA signature qui change tout : l'absence est DANS le type de retour
    public Optional<Reclamation> findById(int id) {
        return Optional.ofNullable(parId.get(id));
    }

    public Optional<Reclamation> premiereUrgente() {
        for (Reclamation r : toutes) {
            if (r.getPriorite() == Priorite.URGENTE) {
                return Optional.of(r);           // trouvé : on emballe
            }
        }
        return Optional.empty();                  // rien trouvé : absence explicite
    }

    public long compterResolues() {
        long total = 0;
        for (Reclamation r : toutes) {
            if (r.estResolue()) {
                total++;
            }
        }
        return total;
    }

    public List<Reclamation> toutes() {
        return List.copyOf(toutes);
    }
}
```

**Trois commentaires de conception :**

- `findById` utilise `ofNullable` : c'est **obligatoire** ici, car `parId.get(id)` renvoie `null` quand la clé est absente. Un `Optional.of(...)` provoquerait une exception immédiate.
- `premiereUrgente` illustre le cas « **on cherche, on renvoie `Optional.empty()` si rien** » — c'est la même mécanique que `findById`, mais sans `Map`.
- `toutes()` continue de renvoyer une copie immuable (leçon 01) : **`Optional` ne remplace pas** les bonnes pratiques de collection ; il traite un autre problème.

**Fichier 2 — `MainOptional.java` : les onze situations à connaître.** (Le fichier est long ; il est présenté en deux extraits, du même fichier.)

```java
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

public class MainOptional {

    // L'ancienne facon (celle de l'exercice 01) : renvoyer null quand rien n'est trouve
    private static Reclamation findByIdSansOptional(RegistreOptional registre, int id) {
        for (Reclamation r : registre.toutes()) {
            if (r.getId() == id) {
                return r;
            }
        }
        return null;                    // rien a signaler a l'appelant... sinon une convention orale
    }

    // Une methode "avec effet de bord visible" : elle affiche quand elle est appelee
    private static String descriptionParDefaut() {
        System.out.println("      >> calcul de la description par defaut (elle a bien ete appelee)");
        return "Reclamation inconnue";
    }

    public static void main(String[] args) {

        RegistreOptional registre = new RegistreOptional();
        registre.ajouter("Nid de poule", "Medina", Priorite.NORMALE);          // #1
        registre.ajouter("Lampadaire éteint", "Plateau", Priorite.URGENTE);    // #2
        registre.ajouter("Poubelles non ramassées", "Medina", Priorite.BASSE); // #3

        // ============ 1. Le probleme d'avant : le null qui explose loin de sa source ============
        try {
            String description = findByIdSansOptional(registre, 99).getDescription();
            System.out.println("1) " + description);
        } catch (NullPointerException e) {
            System.out.println("1) AVANT : NullPointerException (le null vient du findById, "
                    + "l'erreur explose ici)");
        }

        // ============ 2. Les trois facons de creer un Optional ============
        Optional<Reclamation> present = Optional.of(registre.toutes().get(0));
        Optional<Reclamation> peutEtre = Optional.ofNullable(findByIdSansOptional(registre, 99));
        Optional<Reclamation> vide = Optional.empty();
        System.out.println("2) of -> present=" + present.isPresent()
                + " | ofNullable(null) -> present=" + peutEtre.isPresent()
                + " | empty -> present=" + vide.isPresent());
        try {
            Optional.of(null);           // LE piege de creation
        } catch (NullPointerException e) {
            System.out.println("   PIEGE : Optional.of(null) leve immediatement un NullPointerException");
        }

        // ============ 3. Lire : isPresent/get (a eviter) puis orElse ============
        Optional<Reclamation> trouvee1 = registre.findById(1);
        if (trouvee1.isPresent()) {
            System.out.println("3) isPresent + get -> " + trouvee1.get().getDescription());
        }
        System.out.println("   orElse -> " + registre.findById(99).map(r -> r.getDescription())
                .orElse("aucune reclamation"));

        // ============ 4. map : transformer la valeur SEULEMENT si elle est la ============
        System.out.println("4) map sur #2 -> " + registre.findById(2)
                .map(r -> r.getDescription()).orElse("aucune"));
        System.out.println("   map sur #99 -> " + registre.findById(99)
                .map(r -> r.getDescription()).orElse("aucune"));

        // ============ 5. filter : ne garder la valeur que si elle passe un test ============
        System.out.println("5) #2 est URGENTE ? " + registre.findById(2)
                .filter(r -> r.getPriorite() == Priorite.URGENTE).isPresent());
        System.out.println("   #1 est URGENTE ? " + registre.findById(1)
                .filter(r -> r.getPriorite() == Priorite.URGENTE).isPresent());
```

        // ============ 6. orElse (avide) contre orElseGet (paresseux) ============
        String avecOrElse = registre.findById(1)
                .map(r -> r.getDescription())
                .orElse(descriptionParDefaut());               // appelee MEME si present
        System.out.println("6) orElse(...)    -> " + avecOrElse);

        String avecOrElseGet = registre.findById(1)
                .map(r -> r.getDescription())
                .orElseGet(() -> descriptionParDefaut());      // appelee SEULEMENT si vide
        System.out.println("   orElseGet(...) -> " + avecOrElseGet
                + "  (aucun calcul ci-dessus : la methode n'a pas ete appelee)");

        // ============ 7. orElseThrow : transformer l'absence en exception explicite ============
        try {
            Reclamation introuvable = registre.findById(99)
                    .orElseThrow(() -> new IllegalStateException("Reclamation 99 introuvable"));
            System.out.println("7) " + introuvable.getDescription());
        } catch (IllegalStateException e) {
            System.out.println("7) orElseThrow(() -> ...) -> " + e.getClass().getSimpleName()
                    + " : " + e.getMessage());
        }
        try {
            registre.findById(99).orElseThrow();              // variante Java 10, sans argument
        } catch (NoSuchElementException e) {
            System.out.println("   orElseThrow() sans argument -> "
                    + e.getClass().getSimpleName() + " (message technique, sans explication metier)");
        }

        // ============ 8. ifPresent / ifPresentOrElse : agir sans extraire la valeur ============
        registre.findById(2).ifPresent(r -> System.out.println("8) ifPresent -> #" + r.getId()
                + " " + r.getBadge()));
        registre.findById(99).ifPresentOrElse(
                r -> System.out.println("   ifPresentOrElse -> present"),
                () -> System.out.println("8) ifPresentOrElse -> absent : on journalise une alerte"));

        // ============ 9. Une methode du registre qui renvoie Optional ============
        System.out.println("9) premiereUrgente -> " + registre.premiereUrgente()
                .map(r -> r.getDescription()).orElse("aucune urgence"));
        System.out.println("   compterResolues() -> " + registre.compterResolues());

        // ============ 10. map contre flatMap (eviter Optional<Optional<...>>) ============
        Optional<Optional<Reclamation>> imbrique = registre.findById(1)
                .map(r -> registre.findById(r.getId() + 1));        // la lambda rend un Optional !
        System.out.println("10) map     -> type Optional<Optional<Reclamation>>, present="
                + imbrique.isPresent());

        Optional<Reclamation> aplati = registre.findById(1)
                .flatMap(r -> registre.findById(r.getId() + 1));    // flatMap APLANIT
        System.out.println("    flatMap -> " + aplati.map(r -> r.getDescription()).orElse("aucune"));

        // ============ 11. Optional<List<T>> est un anti-pattern ============
        List<Reclamation> duQuartier = List.of();      // une liste vide dit deja "aucune"
        System.out.println("11) liste vide -> isEmpty()=" + duQuartier.isEmpty()
                + " : inutile d'emballer ce cas dans un Optional");
    }
}
```

Compilation et exécution :

```bash
javac -encoding UTF-8 *.java      # StatutReclamation, Priorite, Reclamation, RegistreOptional, MainOptional
java MainOptional
```

**Sortie réellement obtenue (Java 21.0.7) :**

```text
1) AVANT : NullPointerException (le null vient du findById, l'erreur explose ici)
2) of -> present=true | ofNullable(null) -> present=false | empty -> present=false
   PIEGE : Optional.of(null) leve immediatement un NullPointerException
3) isPresent + get -> Nid de poule
   orElse -> aucune reclamation
4) map sur #2 -> Lampadaire éteint
   map sur #99 -> aucune
5) #2 est URGENTE ? true
   #1 est URGENTE ? false
      >> calcul de la description par defaut (elle a bien ete appelee)
6) orElse(...)    -> Nid de poule
   orElseGet(...) -> Nid de poule  (aucun calcul ci-dessus : la methode n'a pas ete appelee)
7) orElseThrow(() -> ...) -> IllegalStateException : Reclamation 99 introuvable
   orElseThrow() sans argument -> NoSuchElementException (message technique, sans explication metier)
8) ifPresent -> #2 NOUVELLE
8) ifPresentOrElse -> absent : on journalise une alerte
9) premiereUrgente -> Lampadaire éteint
   compterResolues() -> 0
10) map     -> type Optional<Optional<Reclamation>>, present=true
    flatMap -> Lampadaire éteint
11) liste vide -> isEmpty()=true : inutile d'emballer ce cas dans un Optional
```

**Lisons cette sortie ensemble :**

1. **Ligne 1** : voilà le problème d'origine, matérialisé. Le `null` a été produit par `findByIdSansOptional` et l'exception éclate **sur la ligne qui appelle `getDescription()`** — pas là où se trouve la cause. C'est cette distance qui rend les NPE si coûteux à diagnostiquer.
2. **Ligne 2** : les trois créations. `of(...)` → présent, `ofNullable(null)` → **vide** (aucune exception), `empty()` → vide. Et le piège juste après : `Optional.of(null)` lève **immédiatement** un `NullPointerException` — `of` **interdit** `null`, c'est sa raison d'être.
3. **Ligne 3** : `isPresent()` + `get()` fonctionne… mais c'est du code « à l'ancienne déguisé » : vous testez puis vous extrayez. À réserver aux rares cas où `orElse`/`map` ne conviennent pas.
4. **Ligne 4** : `map` transforme `Optional<Reclamation>` en `Optional<String>`, **et le `orElse` de fin traite le cas vide**. Sur un identifiant absent (`#99`), aucune exception : la chaîne renvoie simplement `aucune`.
5. **Ligne 5** : `filter` joue le rôle du portier. `#2` passe (`true`), `#1` ne passe pas (`false`) — sans `if`.
6. **Lignes 6** : LA démonstration. Le message `>> calcul … (elle a bien ete appelee)` apparaît **avant** la ligne du `orElse`, pour une valeur qui existait déjà. C'est la preuve que `orElse` est **avide** ; `orElseGet` n'a rien déclenché.
7. **Ligne 7** : `orElseThrow(() -> new IllegalStateException("…"))` produit une exception **avec un message métier** utilisable tel quel. La variante sans argument produit un `NoSuchElementException` **technique** : préférez la première.
8. **Ligne 8** : `ifPresent` agit **sans extraire** la valeur (le `#2` est traité), `ifPresentOrElse` couvre les deux branches — utile pour journaliser une alerte quand il n'y a rien.
9. **Ligne 9** : `premiereUrgente()` illustre le `Optional` produit par **votre** code métier, pas seulement par une `Map`.
10. **Ligne 10** : la différence `map` / `flatMap` **en une ligne de sortie**. Avec `map`, le type est `Optional<Optional<Reclamation>>` (deux enveloppes !) ; avec `flatMap`, l'enveloppe en trop est retirée et le résultat reste `Optional<Reclamation>`.
11. **Ligne 11** : un `Optional` autour d'une liste serait un contresens — une liste `isEmpty()` porte déjà l'information.

---

## 4. Bonnes pratiques modernes (2025-2026)

1. **`Optional` uniquement comme type de retour.** Jamais en champ, jamais en paramètre, jamais dans une collection (section 2.7). C'est la règle que suivent toutes les API modernes.
2. **Préférez toujours `orElseThrow(() -> …)` avec un message métier** à un `null` renvoyé plus haut ou à un `get()` nu. En partie 4, vous remplacerez `IllegalStateException` par vos propres exceptions et un gestionnaire global (Spring).
3. **Un chaînage `map`/`filter` plutôt qu'un `if`** : `findById(id).map(r -> r.getDescription()).orElse("inconnue")` dit exactement ce qu'il veut faire.
4. **`orElseGet` dès que la valeur de repli se calcule ou s'appelle** (requête, méthode, construction d'objet) : c'est la protection contre le travail inutile démontré en section 2.5.
5. **Renvoyez `Optional.empty()`, jamais `null`** depuis une méthode qui renvoie un `Optional`. Un `Optional` `null` cumule les deux problèmes (NPE **et** absence non signalée).
6. **Une collection vide, pas un `Optional` de collection** : `List.of()` porte déjà l'information « aucun élément ».
7. **Nommez la méthode pour dire la recherche** : `findById`, `findByQuartier`, `rechercherPremiereUrgente`. Le nom + le type `Optional` forment un contrat lisible.
8. **Alimentez vos entrées avec `Objects.requireNonNull(champ, "message")`** plutôt qu'avec `Optional` : la validation d'un paramètre se fait par exception immédiate, pas par enveloppe.
9. **Spring/JPA : suivez la convention du framework.** `Optional<T> findById(ID)` est fourni ; dans un contrôleur, `orElseThrow(() -> new ReclamationIntrouvableException(id))` (partie 4/7). Ne « déballez » jamais avec `get()` dans un service web.
10. **Testez vos chemins « vide » autant que vos chemins « présent »** : `findById(identifiantInconnu)` doit être un test explicite (partie 9). C'est là que se cachent les NPE.

---

## 5. Pièges à éviter

### Piège 1 — `Optional.of(null)`

```java
// ❌ MAUVAIS : double faute - la ligne leve une NPE ET l'intention est fausse
Optional<Reclamation> o = Optional.of(parId.get(99));    // NullPointerException immédiate
//    error à l'exécution : java.lang.NullPointerException

// ✅ BON : quand la source peut renvoyer null, c'est ofNullable
Optional<Reclamation> o = Optional.ofNullable(parId.get(99));   // vide, sans exception
```

**Pourquoi c'est dangereux** : l'erreur est immédiate, mais elle se produit **avant** le code qui devait gérer l'absence — donc au pire endroit. `of` = « je **garantis** une valeur non nulle » ; si vous n'êtes pas sûr, ce n'est pas `of`.

### Piège 2 — Le `get()` nu

```java
// ❌ MAUVAIS : retour au point de départ
Reclamation r = registre.findById(99).get();      // NoSuchElementException si vide

// ✅ BON : dire ce qui se passe quand c'est vide
Reclamation r = registre.findById(99)
        .orElseThrow(() -> new IllegalStateException("Réclamation 99 introuvable"));

// ✅ AUTRE BON CHOIX : agir dans les deux cas
registre.findById(99).ifPresentOrElse(
        x -> System.out.println("trouvée"),
        () -> System.out.println("absente"));
```

**Pourquoi** : `get()` traduit une absence **attendue** en exception **non expliquée**. Vous avez remplacé un NPE par un `NoSuchElementException` — le gain est nul.

### Piège 3 — `Optional` dans un champ (et dans une entité de base de données)

```java
// ❌ MAUVAIS : ni Hibernate ni Jackson ne gèrent bien ce champ
public class ReclamationEntity {
    private Optional<String> commentaire;     // partie 8 : le mapping va échouer ou être bancal
}

// ✅ BON : un champ simple + une méthode qui expose l'absence
public class Reclamation {
    private String commentaire;               // peut être null en interne

    public Optional<String> commentaire() {   // l'API, elle, est explicite
        return Optional.ofNullable(commentaire);
    }
}
```

### Piège 4 — `orElse` avec un appel de méthode (le travail inutile)

```java
// ❌ MAUVAIS : la requête est exécutée même quand la réclamation existe
Reclamation r = registre.findById(1).orElse(chargerDepuisLaBase());

// ✅ BON : la requête n'est exécutée que si nécessaire
Reclamation r = registre.findById(1).orElseGet(() -> chargerDepuisLaBase());
```

**Pourquoi** : la sortie exécutée de la section 3 a montré l'appel pour rien. Sur une méthode coûteuse, c'est un ralentissement invisible… et un piège classique en revue de code.

### Piège 5 — Renvoyer `null` depuis une méthode qui renvoie un `Optional`

```java
// ❌ MAUVAIS : pire des deux mondes
public Optional<Reclamation> chercher(int id) {
    if (id < 0) {
        return null;             // l'appelant fera .orElse(...) sur null => NPE !
    }
    return Optional.ofNullable(parId.get(id));
}

// ✅ BON : l'absence a une seule représentation
public Optional<Reclamation> chercher(int id) {
    if (id < 0) {
        return Optional.empty();
    }
    return Optional.ofNullable(parId.get(id));
}
```

### Piège 6 — `Optional<Optional<T>>` (oublier `flatMap`)

```java
// ❌ MAUVAIS : deux enveloppes empilées, illisible
Optional<Optional<Reclamation>> double = registre.findById(1).map(r -> registre.findById(2));

// ✅ BON : flatMap retire l'enveloppe en trop
Optional<Reclamation> simple = registre.findById(1).flatMap(r -> registre.findById(2));
```

### Piège 7 — Envelopper une collection

```java
// ❌ MAUVAIS : trois états pour dire deux choses (vide / absent / présent)
Optional<List<Reclamation>> resultat = Optional.ofNullable(map.get(quartier));

// ✅ BON : un seul type de retour, une seule façon de dire "aucune"
List<Reclamation> resultat = map.getOrDefault(quartier, List.of());
```

### Anti-pattern — L'`Optional` décoratif

```java
// ❌ MAUVAIS : envelopper pour immédiatement déballer
String description = Optional.ofNullable(registre.findById(1)).get().getDescription();

// ✅ BON : Optional n'a de sens que si l'ABSENCE est possible
String description = registre.findById(1).map(r -> r.getDescription()).orElse("inconnue");
```

---

## Checklist de validation

Avant de passer à la leçon 04, vérifiez que vous savez faire **chacun** de ces points :

- [ ] Expliquer, avec un exemple de code, pourquoi `null` cache la **cause** d'une erreur loin de son point d'explosion.
- [ ] Créer un `Optional` avec `of`, `ofNullable` et `empty`, et dire pourquoi `of(null)` échoue.
- [ ] Extraire une valeur avec `orElse`, `orElseGet`, `orElseThrow` en choisissant le bon selon le contexte.
- [ ] Utiliser `ifPresent` et `ifPresentOrElse` pour agir sans extraire la valeur.
- [ ] Enchaîner `map` et `filter` pour éviter un `if (x != null)`.
- [ ] Expliquer et **prouver** la différence avide/paresseux entre `orElse` et `orElseGet`.
- [ ] Utiliser `flatMap` pour ne pas produire un `Optional<Optional<T>>`.
- [ ] Citer les **quatre interdits** d'`Optional` (champ, paramètre, `List<Optional<T>>`, `Optional<List<T>>`).
- [ ] Transformer mon `findById` de l'exercice 01 en `Optional<Reclamation> findById(int)`.
- [ ] Reconnaître le motif `findById` de Spring Data et dire ce qu'il renverra en partie 8.
- [ ] Renvoyer `Optional.empty()` (et jamais `null`) depuis une méthode `Optional`.

---

➡️ **Prochaine étape** : SignalCUA sait maintenant dire « cette réclamation n'existe pas » **sans mentir**. Mais une réclamation porte une notion de temps : elle est déclarée **à une date**, et doit être traitée **dans un délai** (4 h pour une urgence, 48 h pour une normale — souvenez-vous du `Priorite.getDelaiHeuresMax()` de la partie 2). Or jusqu'ici, aucune de nos classes ne manipule vraiment le temps… alors que Java traîne depuis 1995 une API de dates notoirement piégeuse (`Date`, `Calendar`). La **leçon 04 — Date and Time API** installe `java.time` et fait de SignalCUA une application qui **mesure ses délais**.

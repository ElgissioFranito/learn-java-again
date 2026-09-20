# Leçon 01 — List, Set, Map, Queue : ranger plusieurs objets

> 🧭 **Pont depuis la partie 2** : à la fin de la leçon 05, votre `Reclamation` est exemplaire — champs protégés, statut fermé par un `enum StatutReclamation`, priorités typées. Mais où *rangez*-vous ces objets ? Jusqu'ici, dans des **tableaux** (`Reclamation[]`), comme à la leçon 03 de la partie 1 : une taille fixe décidée à la création (`new Reclamation[10]`), aucun ajout possible, et « retrouver la réclamation n°3 » oblige à balayer tout le tableau à la main. Dès la 11ᵉ réclamation, il faut tout recopier dans un tableau plus grand. Cette leçon ouvre la partie 3 en remplaçant le tableau par l'outil prévu pour ça : les **collections**.
>
> ⚠️ **À lire dans l'ordre** : ce fichier est la leçon (explications + exemples + bonnes pratiques + pièges + checklist). Ensuite `02-exercice.md` vous fait construire le registre de SignalCUA, puis `03-correction.md` corrige — avec le code réellement exécuté.

---

## 1. Objectifs d'apprentissage

À la fin de cette leçon, vous saurez :

- Expliquer pourquoi un **tableau** de taille fixe ne suffit plus, et ce qu'apporte une **collection**.
- Choisir entre `List`, `Set`, `Map` et `Queue` **en fonction du besoin**, pas par habitude.
- Choisir l'**implémentation** adaptée : `ArrayList`/`LinkedList`, `HashSet`/`LinkedHashSet`/`TreeSet`, `HashMap`/`LinkedHashMap`/`TreeMap`, `ArrayDeque`, `PriorityQueue`.
- Utiliser les opérations essentielles : `add`, `get`, `put`, `getOrDefault`, `containsKey`, `entrySet`, `offer`, `poll`, `push`, `pop`.
- Reconnaître et éviter les pièges classiques : `ConcurrentModificationException`, clé mutable dans une `Map`, collection interne exposée, classes historiques `Vector`/`Stack`.
- Créer des collections **immuables** (`List.of`, `Set.of`, `Map.of`) et des **copies défensives** (`List.copyOf`).
- Utiliser les nouveautés **Java 21** (`getFirst()`, `getLast()`, `reversed()`).

---

## 2. Explication simple

### 2.1 Le problème : le tableau est un casier soudé

**Pourquoi ?** Un tableau Java a deux limites structurelles : sa **taille est fixée à la création** (`new Reclamation[10]`) et il n'offre **aucune méthode** (`taille.length`, `tableau[3]`, c'est tout). Chercher, ajouter, supprimer, trier : tout est à écrire à la main.

**Analogie** : un tableau, c'est une **consigne avec dix casiers soudés**. Impossible d'en ajouter un onzième, impossible d'en retirer un au milieu sans déplacer les valises une par une. Une **collection**, c'est une armoire modulable : on ajoute un tiroir, on en retire un, on demande « donne-moi le tiroir n°3 » ou « combien de tiroirs contiennent des chaussures ? ».

**Comment ?** Java fournit le **Collection Framework** (mot à mot : « cadre de travail des collections »), dans le paquet `java.util`. Il est bâti sur un principe en **deux étages** :

| Étage | Rôle | Exemple |
|---|---|---|
| **L'interface** | le **contrat** : ce que je veux faire | `List`, `Set`, `Map`, `Queue` |
| **L'implémentation** | le **moteur** : comment c'est fait dedans | `ArrayList`, `HashSet`, `HashMap`, `ArrayDeque` |

C'est pour cela qu'on écrit **toujours** :

```java
List<Reclamation> reclamations = new ArrayList<>();   // contrat = List, moteur = ArrayList
```

et pratiquement jamais :

```java
ArrayList<Reclamation> reclamations = new ArrayList<>();  // ❌ inutilement rigide
```

> 📖 **Vocabulaire** : **interface** = liste de méthodes promises (leçon 03 de la partie 2). **Implémentation** = la classe concrète qui tient ces promesses. **`java.util`** = le paquet (dossier) de la bibliothèque standard où vivent les collections.

**Quand ?** Dès que vous manipulez **plusieurs objets de même nature** : les réclamations reçues, les quartiers touchés, les agents disponibles. Le tableau reste utile pour des cas très précis (données de taille connue, performance brute, `byte[]` d'un fichier) — sinon, collections.

### 2.2 `List` : la liste ordonnée

**Pourquoi ?** Parce que 90 % du temps, vous voulez « une liste de choses, dans un ordre, avec des doublons possibles » : les réclamations dans leur ordre d'arrivée.

**Comment ?** `List` garantit trois choses : un **ordre** (celui d'insertion), un **accès par index** (`get(0)`), et l'acceptation des **doublons** (deux réclamations identiques peuvent coexister — ce sont deux objets distincts).

Les deux implémentations à connaître :

| Implémentation | Comment c'est fait dedans | Fort | Faible |
|---|---|---|---|
| `ArrayList` | un tableau qui grandit automatiquement | accès par index **très rapide** ; le choix par défaut | ajout/retrait **au milieu** coûteux (tout se décale) |
| `LinkedList` | une chaîne de maillons, chacun pointant vers le suivant | ajout/retrait au **milieu** très rapide | accès par index **lent** (il faut suivre la chaîne) |

Et deux classes **historiques** à connaître de nom seulement : `Vector` et `Stack` (Java 1.0). Elles étaient « synchronisées » (protégées pour plusieurs fils d'exécution) mais payantes en performance et bancales à l'usage : le code moderne utilise `ArrayList` et `ArrayDeque`. Vous les croiserez dans du vieux code — ne les utilisez pas dans du neuf.

Les opérations essentielles :

```java
List<String> quartiers = new ArrayList<>();
quartiers.add("Medina");            // ajoute à la fin
quartiers.add(0, "Plateau");        // insère à l'index 0 (le reste se décale)
String premier = quartiers.get(0);  // lit par index
quartiers.set(0, "Fann");           // remplace
quartiers.remove("Medina");         // retire la 1re occurrence ÉGALE (via .equals)
quartiers.remove(0);                // ou par index (attention : deux méthodes homonymes !)
boolean present = quartiers.contains("Fann");   // test d'appartenance
int taille = quartiers.size();      // nombre d'éléments
boolean vide = quartiers.isEmpty();
```

> 📖 **Vocabulaire** : **index** = position numérotée, qui commence à **0** (leçon 03 de la partie 1). **Occurrence** = une apparition d'une valeur dans la liste.

### 2.3 `Set` : l'ensemble sans doublon

**Pourquoi ?** Pour répondre à « quels quartiers ont été touchés **au moins une fois** ? ». Si Medina apparaît 40 fois, vous voulez « Medina » **une seule fois**.

**Analogie** : un `Set`, c'est le **tampon d'un videur** : peu importe combien de fois la même personne se présente, elle n'entre qu'une fois.

**Comment ?** Un `Set` interdit les doublons. Mais « doublon » selon **quelle** règle ? Selon la méthode **`equals()`** — et c'est là qu'un rappel de la partie 2 est indispensable : les `record` (leçon 04) génèrent `equals()` **et** `hashCode()` automatiquement, alors que notre classe `Reclamation` (écrite à la leçon 01 de la partie 2) ne les a **pas**. Conséquence concrète : mettre deux `Reclamation` « identiques » dans un `HashSet` n'en gardera **aucune** pour l'autre — elles seront considérées comme différentes, parce que la comparaison par défaut compare les **références** (est-ce le même objet en mémoire ?). Retenez-le : **un `Set` ou une `Map` de vos propres objets exige `equals`/`hashCode`** — d'où l'usage massif des `record` dans ce rôle.

Les trois implémentations :

| Implémentation | Ordre | Quand l'utiliser |
|---|---|---|
| `HashSet` | aucun ordre garanti | le plus rapide, cas par défaut |
| `LinkedHashSet` | ordre d'insertion conservé | quand l'ordre d'arrivée compte (affichage) |
| `TreeSet` | trié automatiquement | afficher « toujours trié » (quartiers A→Z) |

Le `TreeSet` a un prérequis : les éléments doivent être **comparables** entre eux (`String`, `Integer`, votre propre classe qui implémente `Comparable`), sinon le tri est impossible.

### 2.4 `Map` : le dictionnaire clé → valeur

**Pourquoi ?** Pour retrouver **vite** une information **par une clé** : « donne-moi la réclamation n° 42 » sans balayer toute la liste. C'est l'index/cache du programmeur.

**Analogie** : une `Map`, c'est le **tableau d'affichage d'un hôtel** : chaque **numéro de chambre** (la clé) donne **un client** (la valeur). Deux chambres ne peuvent pas avoir le même numéro, mais deux chambres peuvent loger deux personnes du même nom.

**Comment ?** `Map` **n'est pas** une `Collection` (elle n'a pas « des éléments » mais « des paires ») :

```java
Map<Integer, Reclamation> parId = new HashMap<>();   // clé Integer, valeur Reclamation
parId.put(1, r1);                       // range (clé → valeur) ; écrase si la clé existe déjà
Reclamation r = parId.get(1);           // lit ; renvoie null si la clé est absente
boolean connue = parId.containsKey(1);  // la clé existe-t-elle ? (get == null ne suffit pas)
parId.remove(1);                        // retire la paire
int nombre = parId.size();
```

Les trois implémentations suivent exactement la même logique que les `Set` (souvenez-vous : un `Set` n'est d'ailleurs « qu'une `Map` dont on ignore les valeurs ») :

| Implémentation | Ordre | Quand l'utiliser |
|---|---|---|
| `HashMap` | aucun | cas par défaut, le plus rapide |
| `LinkedHashMap` | ordre d'insertion | reproduire l'ordre, base d'un cache LRU |
| `TreeMap` | trié par clé | parcourir les clés A→Z, trouver des « clés proches » |

Trois méthodes indispensables au quotidien :

```java
// 1. getOrDefault : lire avec une valeur de repli (évite le test null)
List<Reclamation> liste = parQuartier.getOrDefault("Fann", List.of());

// 2. computeIfAbsent : créer la valeur à la demande (le motif "Map de List" en 1 ligne)
parQuartier.computeIfAbsent("Fann", q -> new ArrayList<>()).add(r);
//    ↳ la syntaxe « q -> new ArrayList<>() » est une lambda : vous la verrez en partie 5.
//      Ici, lisez-la comme « comment fabriquer la valeur si la clé manque ».

// 3. entrySet : parcourir clés ET valeurs ensemble
for (Map.Entry<String, List<Reclamation>> entree : parQuartier.entrySet()) {
    System.out.println(entree.getKey() + " -> " + entree.getValue().size());
}
```

> 📖 **Vocabulaire** : **clé (key)** = l'identifiant unique servant à retrouver **la valeur (value)**. **Paires clé-valeur** = le contenu d'une `Map` (`Map.Entry` = une paire). **Cache LRU** = *Least Recently Used* : mémoire temporaire qui oublie le moins récemment utilisé.

### 2.5 `Queue` et `Deque` : l'ordre de sortie compte

**Pourquoi ?** Ranger ne suffit pas : il faut parfois **traiter dans un ordre précis**. Une file d'attente au guichet (premier arrivé, premier servi) n'a pas le même ordre qu'une pile d'assiettes (la dernière posée est reprise en premier).

**Comment ?** Deux familles de structures :

| Structure | Règle de sortie | Nom anglais | Exemple réel |
|---|---|---|---|
| **File** | premier entré, premier sorti | **FIFO** (*First In, First Out*) | les réclamations traitées par ordre d'arrivée |
| **Pile** | dernier entré, premier sorti | **LIFO** (*Last In, First Out*) | l'historique des actions pour faire « Ctrl+Z » |

`Queue` (la file) et `Deque` (« *double ended queue* » : file à **deux** bouts, donc utilisable comme file **ou** comme pile) sont les deux interfaces. L'implémentation moderne de référence est **`ArrayDeque`** — c'est elle qui remplace l'historique `Stack` (LIFO) et fait aussi très bien la file (FIFO).

Deux jeux de méthodes se répondent. Le second est **préférable** car il ne lève pas d'exception en cas d'échec :

| Rôle | Méthode qui **lève une exception** | Méthode qui **renvoie une valeur** (`null`/`false`) |
|---|---|---|
| Ajouter | `add(e)` | `offer(e)` |
| Retirer et renvoyer | `remove()` | `poll()` |
| Regarder sans retirer | `element()` | `peek()` |

```java
Deque<Reclamation> file = new ArrayDeque<>();
file.offer(r1);                 // entre par la fin
file.offer(r2);
Reclamation premier = file.poll();   // sort par le début → r1

Deque<Reclamation> pile = new ArrayDeque<>();
pile.push(r1);                  // entre par le début (le haut de la pile)
pile.push(r2);                  // r2 est maintenant au-dessus
Reclamation haut = pile.pop();  // sort par le haut → r2
```

**Et `PriorityQueue` ?** C'est une file qui **n'est pas FIFO** : elle sort toujours l'élément **le plus prioritaire**, selon une règle que vous fournissez (un `Comparator`). Parfaite pour notre fil rouge : traiter d'abord les réclamations dont le **délai maximal est le plus court**.

```java
PriorityQueue<Reclamation> urgences = new PriorityQueue<>(new Comparator<Reclamation>() {
    @Override                                   // « classe anonyme » : une règle écrite sur place
    public int compare(Reclamation a, Reclamation b) {
        return Integer.compare(a.getPriorite().getDelaiHeuresMax(),
                               b.getPriorite().getDelaiHeuresMax());
    }
});
urgences.add(rNormale); urgences.add(rUrgente); urgences.add(rBasse);
urgences.poll();     // → rUrgente (4 h) d'abord, puis rNormale (48 h), puis rBasse (168 h)
```

> ⚠️ **Deux précisions honnêtes sur `PriorityQueue`** (vérifiées à l'exécution dans la section 3) :
> 1. `PriorityQueue` ne **trie pas** la collection : seule la **sortie** (`poll()`) respecte l'ordre. Parcourir la file avec un `for-each` donne un ordre **quelconque** (c'est un « tas », structure interne optimisée pour sortir le meilleur élément).
> 2. Si deux éléments ont **exactement** la même priorité, leur ordre relatif n'est **pas garanti** : ne comptez jamais sur « le premier arrivé des deux urgents ».
>
> 📖 **Vocabulaire** : **`Comparator`** = objet qui contient la règle de comparaison entre deux éléments (« a est-il avant b ? »). **Classe anonyme** = classe écrite sur place, sans nom, pour un usage unique ; la partie 5 montrera la version courte (`(a, b) -> ...`). **Tas (*heap*)** = structure d'arbre où le plus prioritaire est toujours en tête.

### 2.6 Quel conteneur pour quel besoin ? (le tableau de décision)

C'est LA question à se poser avant d'écrire une ligne :

| Le besoin | Le conteneur | L'implémentation par défaut |
|---|---|---|
| « une liste de choses, dans l'ordre, parcourue souvent » | `List` | `ArrayList` |
| « ajouts/retraits fréquents **au milieu** d'une longue liste » | `List` | `LinkedList` |
| « chaque chose **une seule fois** » (unicité) | `Set` | `HashSet` |
| « chaque chose une seule fois, **triée** » | `Set` | `TreeSet` |
| « retrouver une valeur **par clé** » | `Map` | `HashMap` |
| « retrouver par clé, **dans l'ordre d'arrivée** » | `Map` | `LinkedHashMap` |
| « traiter **dans l'ordre d'arrivée** » | `Queue` | `ArrayDeque` |
| « traiter **du plus prioritaire au moins prioritaire** » | `Queue` | `PriorityQueue` |
| « revenir toujours au **dernier** » (pile) | `Deque` | `ArrayDeque` |

### 2.7 Un mot sur la « complexité » : O(1) et O(n)

Vous lirez partout que `ArrayList` a un accès « O(1) » et une insertion en milieu « O(n) ». Décodons, sans mathématiques.

**Analogie** : chercher le 3ᵉ livre d'une étagère rangée **par cote** prend le même temps, que l'étagère contienne 10 ou 1 000 livres (on va directement à la bonne place) : c'est **O(1)**, un coût **constant**. Dans une **pile** de livres en désordre, retrouver un titre oblige à fouiller : deux fois plus de livres ⇒ à peu près deux fois plus de temps : c'est **O(n)**, un coût **proportionnel au nombre d'éléments** (`n`).

Retenez simplement : « O(1) » = direct, « O(n) » = balayage. C'est ce qui explique les colonnes « Fort / Faible » des tableaux ci-dessus, et ce qui vous fera choisir `HashMap` plutôt qu'une `List` quand vous cherchez « par identifiant » des milliers de fois.

---

## 📖 Vocabulaire / Abréviations

Tous les termes techniques de cette leçon, en une ligne chacun (à relire avant l'exercice) :

| Terme | Définition en une ligne |
|---|---|
| **Collection** | Structure qui contient **plusieurs** éléments, de taille variable (contrairement au tableau). |
| **Collection Framework** | L'ensemble d'interfaces et de classes de `java.util` qui fournit toutes ces structures. |
| **`List`** | Collection **ordonnée** avec **index** et doublons autorisés. |
| **`Set`** | Collection **sans doublon** (défini par `equals`/`hashCode`). |
| **`Map`** | Ensemble de **paires clé → valeur**, clés uniques. |
| **`Queue`** | File : on entre d'un côté, on sort de l'autre. |
| **`Deque`** | *Double ended queue* : file à deux bouts → sert de **file** (FIFO) ou de **pile** (LIFO). |
| **FIFO** | *First In, First Out* : premier entré, premier sorti. |
| **LIFO** | *Last In, First Out* : dernier entré, premier sorti. |
| **`ArrayDeque`** | L'implémentation moderne de `Deque` ; remplace `Stack` et fait aussi office de file. |
| **`PriorityQueue`** | File qui sort l'élément **le plus prioritaire** selon un `Comparator` (pas FIFO). |
| **`Comparator`** | Objet contenant la règle de comparaison entre deux éléments. |
| **`Comparable`** | Interface qu'une classe implémente pour définir son **ordre naturel** (`compareTo`). |
| **Classe anonyme** | Classe sans nom, écrite sur place pour un usage unique. |
| **`equals` / `hashCode`** | Les deux méthodes qui définissent l'**égalité** d'objets (les `record` les génèrent). |
| **Index** | Position d'un élément, **à partir de 0**. |
| **Itérateur (`Iterator`)** | Objet de parcours manuel d'une collection (`hasNext()`, `next()`, `remove()`). |
| **`for-each`** | Écriture `for (Type x : collection)` qui parcourt tous les éléments. |
| **`ConcurrentModificationException`** | Exception levée quand on modifie une collection **pendant** son parcours. |
| **Collection immuable** | Collection qu'on ne peut plus modifier (`List.of`, `Set.of`, `Map.of`). |
| **Copie défensive** | Copie renvoyée à l'appelant pour qu'il ne puisse pas modifier l'intérieur de l'objet. |
| **Capacité initiale** | Taille interne prévue à la création (`new ArrayList<>(100)`) — évite des recopies. |
| **`SequencedCollection`** | Nouveauté **Java 21** : méthodes uniformes `getFirst()`, `getLast()`, `reversed()`. |
| **Complexité O(1) / O(n)** | Coût d'une opération : **constant** (direct) ou **proportionnel à la taille** (balayage). |
| **Thread-safe** | Qui supporte d'être utilisé par plusieurs fils d'exécution en même temps (partie 10). |
| **Classe historique (*legacy*)** | Classe ancienne gardée pour compatibilité (`Vector`, `Stack`, `Hashtable`) — à ne pas utiliser dans du code neuf. |
| **Cache LRU** | Mémoire temporaire qui supprime l'élément le moins récemment utilisé. |

---

## 3. Exemples concrets

Passons à la pratique avec SignalCUA. Comme la leçon doit rester reproductible, on repart des **trois fichiers de la partie 2** (`StatutReclamation.java`, `Priorite.java`, `Reclamation.java`) auxquels on ajoute **un champ `priorite`** à `Reclamation` (utile pour illustrer `PriorityQueue`) :

```java
// Fichier : Reclamation.java  (extrait — la partie 2 est reprise telle quelle)
public class Reclamation {

    private final int id;
    private final String description;
    private final String quartier;
    private final Priorite priorite;          // ✚ ajout de la partie 3
    private StatutReclamation statut;

    public Reclamation(int id, String description, String quartier, Priorite priorite) {
        // ... validations de la leçon 01 de la partie 2 ...
        this.priorite = priorite;
        this.statut = StatutReclamation.NOUVELLE;
    }

    public Priorite getPriorite() { return priorite; }
    // ... demarrerTraitement(), marquerResolue(), getLigneAffichage() inchangés ...
}
```

Créons maintenant un dossier de travail et écrivons le programme de démonstration **`MainCollections.java`** — il visite tout ce que la section 2 a expliqué, dans l'ordre :

```java
import java.util.*;

public class MainCollections {

    public static void main(String[] args) {

        // ================= 1. LIST : un tableau qui grandit tout seul =================
        List<Reclamation> reclamations = new ArrayList<>();   // <> = type déduit (leçon 02)
        Reclamation r1 = new Reclamation(1, "Nid de poule", "Medina", Priorite.NORMALE);
        Reclamation r2 = new Reclamation(2, "Lampadaire éteint", "Plateau", Priorite.URGENTE);
        Reclamation r3 = new Reclamation(3, "Poubelles non ramassées", "Medina", Priorite.BASSE);
        reclamations.add(r1);                                 // add() : ajoute en fin de liste
        reclamations.add(r2);
        reclamations.add(r3);

        System.out.println("1) List -> " + reclamations.size() + " reclamations");
        System.out.println("   index 0 : " + reclamations.get(0).getLigneAffichage());
        for (Reclamation r : reclamations) {                  // for-each : parcours sans index
            System.out.println("   - " + r.getLigneAffichage());
        }
```

        // 1.b LE PIÈGE : modifier la liste PENDANT un for-each
        try {
            for (Reclamation r : reclamations) {
                if (r.getQuartier().equals("Medina")) {
                    reclamations.remove(r);                   // interdit pendant le parcours
                }
            }
        } catch (ConcurrentModificationException e) {
            System.out.println("   [PIÈGE] ConcurrentModificationException attrapée !");
        }

        // 1.c LA BONNE FAÇON : on itère sur une COPIE, et l'Iterator enlève proprement
        reclamations = new ArrayList<>(List.of(r1, r2, r3));   // on repart d'une liste propre
        List<Reclamation> copie = new ArrayList<>(reclamations);   // la copie que l'on va modifier
        Iterator<Reclamation> it = copie.iterator();                // l'outil de parcours "manuel"
        while (it.hasNext()) {                                      // reste-t-il un élément ?
            Reclamation r = it.next();                              // on lit le suivant
            if (r.getQuartier().equals("Medina")) {
                it.remove();                                        // retire ET prévient l'iterator
            }
        }
        System.out.println("   après nettoyage de la copie : " + copie.size()
                + " réclamation(s) restante(s) sur " + reclamations.size());

        // ================= 2. SET : aucun doublon possible =================
        Set<String> quartiersHash = new HashSet<>();
        quartiersHash.add("Medina");
        quartiersHash.add("Plateau");
        quartiersHash.add("Medina");                          // doublon : ignoré
        System.out.println("2) HashSet -> " + quartiersHash.size() + " quartier(s) unique(s)");

        Set<String> quartiersLinked = new LinkedHashSet<>();   // ordre d'insertion conservé
        quartiersLinked.add("Plateau");
        quartiersLinked.add("Medina");
        quartiersLinked.add("Fann");
        System.out.println("   LinkedHashSet (ordre d'insertion) : " + quartiersLinked);

        Set<String> quartiersTries = new TreeSet<>(quartiersLinked);   // tri alphabétique
        System.out.println("   TreeSet (trié) : " + quartiersTries);

        // ================= 3. MAP : retrouver un objet par sa CLÉ =================
        Map<Integer, Reclamation> parId = new HashMap<>();      // clé = id, valeur = l'objet
        for (Reclamation r : reclamations) {
            parId.put(r.getId(), r);
        }
        System.out.println("3) Map<Integer, Reclamation> -> " + parId.size() + " entrées");
        System.out.println("   get(2) : " + parId.get(2).getLigneAffichage());
        System.out.println("   get(99) (absent) : " + parId.get(99));

        // 3.b plusieurs valeurs par clé : une Map de List
        Map<String, List<Reclamation>> parQuartier = new HashMap<>();
        for (Reclamation r : reclamations) {
            List<Reclamation> liste = parQuartier.get(r.getQuartier());   // version classique
            if (liste == null) {                                          // la clé est nouvelle
                liste = new ArrayList<>();
                parQuartier.put(r.getQuartier(), liste);
            }
            liste.add(r);
        }
        for (Map.Entry<String, List<Reclamation>> entree : parQuartier.entrySet()) {
            System.out.println("   " + entree.getKey() + " -> "
                    + entree.getValue().size() + " réclamation(s)");
        }
        System.out.println("   getOrDefault(\"Fann\", List.of()).size() = "
                + parQuartier.getOrDefault("Fann", List.of()).size());

        // ================= 4. QUEUE / DEQUE : l'ordre de sortie =================
        Deque<Reclamation> fileAttente = new ArrayDeque<>();   // FIFO : 1er entré, 1er sorti
        fileAttente.offer(r1);
        fileAttente.offer(r2);
        fileAttente.offer(r3);
        System.out.println("4) FIFO -> on sort d'abord : #" + fileAttente.poll().getId());

        Deque<Reclamation> historiquePile = new ArrayDeque<>(); // LIFO : dernier entré, 1er sorti
        historiquePile.push(r1);
        historiquePile.push(r2);
        System.out.println("   LIFO -> on sort d'abord : #" + historiquePile.pop().getId());
```

        // 4.b PriorityQueue : l'ordre suit une RÈGLE fournie, pas l'ordre d'arrivée
        PriorityQueue<Reclamation> urgences = new PriorityQueue<>(new Comparator<Reclamation>() {
            @Override                                          // "classe anonyme" = la règle de tri
            public int compare(Reclamation a, Reclamation b) {
                return Integer.compare(a.getPriorite().getDelaiHeuresMax(),
                                       b.getPriorite().getDelaiHeuresMax());
            }
        });
        urgences.add(r1);   // NORMALE : 48 h
        urgences.add(r2);   // URGENTE : 4 h
        urgences.add(r3);   // BASSE   : 168 h
        System.out.print("   PriorityQueue -> ordre de traitement par délai croissant :");
        while (!urgences.isEmpty()) {
            System.out.print(" #" + urgences.poll().getId());
        }
        System.out.println();

        // ================= 5. Collections IMMUABLES (List.of / Set.of / Map.of) =================
        List<String> quartiersFiges = List.of("Medina", "Plateau");   // immuable, taille fixe
        try {
            quartiersFiges.add("Fann");                                // interdit
        } catch (UnsupportedOperationException e) {
            System.out.println("5) List.of(...).add(...) refusé : " + e.getClass().getSimpleName());
        }

        // ================= 6. NOUVEAUTÉ JAVA 21 : SequencedCollection =================
        List<Reclamation> sequence = new ArrayList<>(parId.values());
        System.out.println("6) getFirst() : #" + sequence.getFirst().getId()
                + " | getLast() : #" + sequence.getLast().getId());
        System.out.print("   reversed() :");
        for (Reclamation r : sequence.reversed()) {        // vue renversée, sans copie
            System.out.print(" #" + r.getId());
        }
        System.out.println();
    }
}
```

Compilation et exécution (dans le dossier des fichiers `.java`) :

```bash
javac -encoding UTF-8 *.java      # -encoding UTF-8 : indispensable pour nos accents
java MainCollections
```

**Sortie réellement obtenue** (Java 21, copiée depuis le terminal) :

```text
1) List -> 3 reclamations
   index 0 : #1 [NOUVELLE] Nid de poule (Medina, NORMALE) 🆕
   - #1 [NOUVELLE] Nid de poule (Medina, NORMALE) 🆕
   - #2 [NOUVELLE] Lampadaire éteint (Plateau, URGENTE) 🆕
   - #3 [NOUVELLE] Poubelles non ramassées (Medina, BASSE) 🆕
   [PIÈGE] ConcurrentModificationException attrapée !
   après nettoyage de la copie : 1 réclamation(s) restante(s) sur 3
2) HashSet -> 2 quartier(s) unique(s)
   LinkedHashSet (ordre d'insertion) : [Plateau, Medina, Fann]
   TreeSet (trié) : [Fann, Medina, Plateau]
3) Map<Integer, Reclamation> -> 3 entrées
   get(2) : #2 [NOUVELLE] Lampadaire éteint (Plateau, URGENTE) 🆕
   get(99) (absent) : null
   Plateau -> 1 réclamation(s)
   Medina -> 2 réclamation(s)
   getOrDefault("Fann", List.of()).size() = 0
4) FIFO -> on sort d'abord : #1
   LIFO -> on sort d'abord : #2
   PriorityQueue -> ordre de traitement par délai croissant : #2 #1 #3
5) List.of(...).add(...) refusé : UnsupportedOperationException
6) getFirst() : #1 | getLast() : #3
   reversed() : #3 #2 #1
```

**Lisons cette sortie ensemble** — chaque ligne est un enseignement :

1. **La liste grandit toute seule** : 3 objets, puis `size()` s'adapte (aucun tableau à redimensionner à la main).
2. **`ConcurrentModificationException`** : la modification pendant le `for-each` échoue vraiment. Le message est **volontairement attrapé** ici pour que le programme continue : en vrai code, on corrige la façon de faire (section 1.c) au lieu de masquer l'erreur.
3. **`HashSet` → 2** alors qu'on a ajouté « Medina » deux fois : la déduplication fonctionne.
4. **`LinkedHashSet` → `[Plateau, Medina, Fann]`** (ordre d'arrivée) contre **`TreeSet` → `[Fann, Medina, Plateau]`** (ordre alphabétique) : même contenu, deux ordres — c'est exactement le critère de choix.
5. **`get(99)` → `null`** : c'est LA limite de `Map` que la leçon 03 (`Optional`) va traiter proprement.
6. **`PriorityQueue` → `#2 #1 #3`** : malgré l'ordre d'insertion (1, 2, 3), la sortie suit les délais (4 h, 48 h, 168 h). C'est **la règle fournie dans le `Comparator`** qui décide, pas l'ordre d'arrivée.
7. **`List.of(...).add(...)` refusé** : une collection immuable protège ses données (elle est aussi **plus légère** en mémoire).
8. **`reversed()` → `#3 #2 #1`** : le renversement s'obtient sans écrire de boucle à l'envers.

---

## 4. Bonnes pratiques modernes (2025-2026)

1. **Déclarez toujours l'interface, instanciez l'implémentation** : `List<Reclamation> l = new ArrayList<>();`. Le jour où il faut passer à `LinkedList` ou à une liste immuable, une seule ligne change.
2. **Choisissez l'implémentation selon le besoin réel** (tableau de la section 2.6), pas par réflexe. En pratique : `ArrayList`, `HashMap`, `HashSet`, `ArrayDeque` couvrent 95 % des cas ; `TreeSet`/`TreeMap`/`LinkedHashMap` répondent aux besoins d'ordre ; `PriorityQueue` aux traitements priorisés.
3. **Prévoyez la capacité quand vous la connaissez** : `new ArrayList<>(10_000)` évite une dizaine de redimensionnements internes. Détail de performance gratuit.
4. **Utilisez `List.of` / `Set.of` / `Map.of` pour les données fixes** (l'équivalent Java de `as const` / `Object.freeze` de TypeScript) et **`List.copyOf`** pour renvoyer une copie depuis une classe. Attention à une subtilité confirmée par l'exécution : **`Set.copyOf` ne garantit pas l'ordre** — pour un ensemble trié, copiez dans un `TreeSet` puis protégez avec `Collections.unmodifiableSet(...)`.
5. **Ne renvoyez jamais l'objet interne** d'une classe (`return this.liste;`). Un appelant pourrait le vider. Renvoyez `List.copyOf(this.liste)` : la classe reste maîtresse de son contenu.
6. **Exploitez `getOrDefault` et `computeIfAbsent`** : elles suppriment les `if (x == null)` répétés dans le motif « Map de quelque chose ».
7. **Java 21 : adoptez `SequencedCollection`** : `getFirst()`, `getLast()`, `reversed()`, et côté `Map` les `SequencedMap` (`firstEntry()`, `pollFirstEntry()`…). Fini les `liste.get(liste.size() - 1)`.
8. **Un `record` pour les éléments d'un `Set` ou les clés d'une `Map`** : il fournit `equals`/`hashCode` garantis. Sinon, implémentez-les vous-même — ou n'utilisez pas ces collections pour ces objets.
9. **`Iterator.remove()` ou `removeIf(...)`** pour supprimer pendant un parcours — jamais `collection.remove(...)` dans un `for-each`.
10. **Pour du multi-thread (partie 10), changez d'implémentation, pas de code** : `ConcurrentHashMap`, `CopyOnWriteArrayList`. Ne bricolez pas à la main autour des collections classiques.

---

## 5. Pièges à éviter

### Piège 1 — Modifier une collection pendant son parcours

```java
// ❌ MAUVAIS : ConcurrentModificationException garantie (vérifié à l'exécution)
for (Reclamation r : reclamations) {
    if (r.getQuartier().equals("Medina")) {
        reclamations.remove(r);          // la liste change sous les pieds du parcours
    }
}

// ✅ BON : l'iterator sait ce qu'il fait
Iterator<Reclamation> it = reclamations.iterator();
while (it.hasNext()) {
    if (it.next().getQuartier().equals("Medina")) {
        it.remove();                     // retrait par l'iterator = autorisé
    }
}

// ✅ ENCORE MIEUX depuis Java 8 (la partie 5 détaillera la syntaxe) :
reclamations.removeIf(r -> r.getQuartier().equals("Medina"));
```

**Pourquoi c'est dangereux** : l'exception est une **protection**. Sans elle, on obtiendrait des résultats faux (éléments sautés, parcours incohérent) — bien pire qu'un plantage.

### Piège 2 — Une clé de `Map` qui change après insertion

```java
// ❌ MAUVAIS : la clé doit être IMMUABLE ; ici on modifie l'objet après l'avoir utilisé comme clé
Map<Reclamation, String> notes = new HashMap<>();
Reclamation cle = new Reclamation(1, "Nid de poule", "Medina", Priorite.NORMALE);
notes.put(cle, "à traiter");
cle.marquerResolue();                       // l'état change → le "casier" de stockage peut changer
System.out.println(notes.get(cle));         // → risque de null : la clé s'est "perdue"

// ✅ BON : une clé stable et immuable — un identifiant, une String, ou un record (données figées)
Map<Integer, Reclamation> parId = new HashMap<>();
parId.put(1, r1);
```

**Pourquoi** : `HashMap` range la valeur dans un « casier » calculé à partir de la clé (`hashCode`). Si la clé change, on cherche dans un autre casier : l'entrée existe toujours mais devient introuvable. Bug silencieux classique.

### Piège 3 — Exposer sa collection interne

```java
// ❌ MAUVAIS : l'appelant peut modifier votre intérieur
public List<Reclamation> getReclamations() { return this.reclamations; }
// ... ailleurs : registre.getReclamations().clear();   ← votre objet est vidé de l'extérieur

// ✅ BON : une copie immuable (même logique que l'encapsulation, leçon 01 de la partie 2)
public List<Reclamation> getReclamations() { return List.copyOf(this.reclamations); }
```

### Piège 4 — Croire que `PriorityQueue` est triée

```java
// ❌ FAUX : le for-each n'a AUCUN ordre garanti (vérifié : seule la sortie poll() est ordonnée)
for (Reclamation r : urgences) { System.out.println(r); }

// ✅ BON : on consomme dans l'ordre en vidant la file
while (!urgences.isEmpty()) {
    System.out.println(urgences.poll());
}
```

### Piège 5 — Utiliser `Vector`, `Stack` ou `Hashtable` dans du code neuf

```java
// ❌ MAUVAIS : classes de Java 1.0, synchronisées (lentes) et à l'API maladroite
Stack<Reclamation> pile = new Stack<>();
Vector<Reclamation> liste = new Vector<>();

// ✅ BON : les équivalents modernes
Deque<Reclamation> pile = new ArrayDeque<>();   // pile (LIFO)
List<Reclamation> liste = new ArrayList<>();    // liste
```

### Piège 6 — Le tableau à taille fixe plus un compteur manuel

```java
// ❌ MAUVAIS : le réflexe hérité d'autres langages
Reclamation[] tableau = new Reclamation[10];
int taille = 0;
tableau[taille++] = r1;              // et à la 11e : ArrayIndexOutOfBoundsException

// ✅ BON : la liste grandit toute seule
List<Reclamation> liste = new ArrayList<>();
liste.add(r1);
```

### Piège 7 — `Map<String, Object>` comme fourre-tout

```java
// ❌ MAUVAIS : typage fort perdu, casts partout, fautes de frappe invisibles
Map<String, Object> donnees = new HashMap<>();
donnees.put("quartier", "Medina");
String quartier = (String) donnees.get("quartie");   // faute ⇒ null ⇒ ClassCastException

// ✅ BON : un type dédié (classe ou record — leçon 04 de la partie 2)
record Lieu(String quartier, String commune) { }
```

### Anti-pattern — `LinkedList` « parce que c'est un mot plus savant »

```java
// ❌ MAUVAIS : lecture par index sur une liste chaînée → O(n) à CHAQUE get(i)
List<Reclamation> liste = new LinkedList<>();
for (int i = 0; i < liste.size(); i++) {
    liste.get(i);                    // catastrophique dès quelques milliers d'éléments
}

// ✅ BON : ArrayList pour lire/parcourir ; LinkedList seulement pour insérer/retirer au milieu
List<Reclamation> liste = new ArrayList<>();
```

---

## Checklist de validation

Avant de passer à la leçon 02, vérifiez que vous savez faire **chacun** de ces points (cochez au fur et à mesure) :

- [ ] Expliquer la différence entre un **tableau** et une **collection**, et dire pourquoi on préfère la collection par défaut.
- [ ] Écrire `List<Reclamation> l = new ArrayList<>();` et expliquer pourquoi on déclare l'**interface**.
- [ ] Choisir entre `ArrayList` et `LinkedList` selon le besoin (lecture vs insertion au milieu).
- [ ] Choisir entre `HashSet`, `LinkedHashSet` et `TreeSet`, et dire lequel conserve l'ordre d'insertion.
- [ ] Expliquer pourquoi un `Set` ou une `Map` de vos propres objets exige `equals`/`hashCode` (et pourquoi un `record` règle le problème).
- [ ] Utiliser `put`, `get`, `getOrDefault`, `containsKey`, `entrySet` sur une `Map`.
- [ ] Construire une `Map<String, List<Reclamation>>` (regroupement) avec la boucle classique `get`/`put`.
- [ ] Expliquer FIFO et LIFO, et écrire une file et une pile avec `ArrayDeque` (`offer`/`poll`, `push`/`pop`).
- [ ] Écrire un `Comparator` (en classe anonyme) pour une `PriorityQueue`, et dire pourquoi sa sortie est ordonnée mais pas son parcours.
- [ ] Créer une collection immuable (`List.of`) et expliquer le bénéfice d'une copie défensive (`List.copyOf`).
- [ ] Utiliser `getFirst()`, `getLast()` et `reversed()` (Java 21).
- [ ] Éviter la `ConcurrentModificationException` (iterator ou copie).

---

➡️ **Prochaine étape** : vous savez maintenant ranger et retrouver vos objets. Mais vous avez écrit partout `List<Reclamation>`, `Map<Integer, Reclamation>`, `Boite<…>` : d'où vient ce **`<...>`** ? Pourquoi le compilateur refuse-t-il `List<int>` ? Et pourquoi un `Set<String>` ne peut-il pas contenir d'`Integer` ? La **leçon 02 — Generics** ouvre la boîte : écrire du code **générique** (une seule fois) plutôt que le recopier pour chaque type d'objet.

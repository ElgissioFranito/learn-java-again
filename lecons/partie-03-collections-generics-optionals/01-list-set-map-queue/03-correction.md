# Correction détaillée — Exercice 01 « Le registre de réclamations de SignalCUA »

> 🧭 **Comment ce fichier s'articule** : vous venez de tenter `02-exercice.md`. Voici la solution complète, les choix techniques expliqués, la **sortie réellement obtenue** après compilation/exécution, puis la checklist et des conseils. Si votre code compile et donne la même sortie, vous avez tout compris — peu importe que le vôtre soit écrit autrement.

## Correction pas à pas

### Étape 1 — Les champs du registre : une structure par besoin

```java
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.TreeSet;

public class RegistreReclamations {

    // 1. Un objet = une structure choisie POUR UN BESOIN precis
    private final List<Reclamation> toutes = new ArrayList<>();          // parcours par index/ordre
    private final Map<Integer, Reclamation> parId = new HashMap<>();     // retrouver par identifiant
    private final Map<String, List<Reclamation>> parQuartier = new HashMap<>();  // regrouper
    private final Set<String> quartiers = new TreeSet<>();               // unique + trie
    private final Deque<Reclamation> fileAttente = new ArrayDeque<>();   // FIFO des non urgentes
    private final PriorityQueue<Reclamation> urgences = new PriorityQueue<>(
            new Comparator<Reclamation>() {
                @Override
                public int compare(Reclamation a, Reclamation b) {
                    return Integer.compare(a.getPriorite().getDelaiHeuresMax(),
                                           b.getPriorite().getDelaiHeuresMax());
                }
            });
    private int prochainId = 1;
```

**Pourquoi ces choix ?**

- **`toutes` en `ArrayList`** : c'est le « carnet » du registre — on le parcourt souvent, on lit par index, on n'insère jamais au milieu. L'implémentation par défaut, pour la raison exacte de la section 2.2 de la leçon.
- **`parId` en `HashMap`** : `findById` doit être « direct » (O(1)) et non un balayage de liste (O(n)). La clé est un `Integer` (immuable) — le piège 2 de la leçon est évité.
- **`parQuartier` en `HashMap` de `List`** : une clé `String`, plusieurs valeurs. C'est le motif « Map de List » vu en section 3.b.
- **`quartiers` en `TreeSet`** : le `Set` interdit les doublons, le `Tree` ajoute le tri alphabétique. C'est le seul `Set` qui répond aux deux exigences à la fois.
- **`fileAttente` en `ArrayDeque`** : `Deque` offre FIFO (`offer`/`poll`) et LIFO (`push`/`pop`) ; on n'utilise que le premier aspect ici.
- **`urgences` en `PriorityQueue`** : les urgentes doivent sortir avant tout le reste ; l'ordre de sortie vient du `Comparator` (délai le plus court d'abord = le plus urgent).
- **`prochainId`** : c'est le registre qui **possède** la numérotation. L'appelant n'a pas à connaître les identifiants (encapsulation, leçon 01 de la partie 2) — et c'est indispensable pour une future base de données (partie 8).

> 📌 **Note honnête sur la lisibilité** : la création du `PriorityQueue` avec une classe anonyme prend 8 lignes au milieu des champs. C'est du code **correct mais verbeux** ; la partie 5 vous montrera l'écriture en une ligne (`Comparator.comparingInt(r -> r.getPriorite().getDelaiHeuresMax())`), et la partie 7 sortira même cette classe du registre. Ici, la forme longue a l'avantage d'être explicite.

### Étape 2 — `ajouter` : écrire dans les cinq structures

```java
    public Reclamation ajouter(String description, String quartier, Priorite priorite) {
        Reclamation r = new Reclamation(prochainId, description, quartier, priorite);
        prochainId++;

        toutes.add(r);                                   // 1) la liste globale
        parId.put(r.getId(), r);                         // 2) l'index par id

        List<Reclamation> duQuartier = parQuartier.get(quartier);   // 3) le regroupement
        if (duQuartier == null) {                        //    la clé est nouvelle
            duQuartier = new ArrayList<>();
            parQuartier.put(quartier, duQuartier);
        }
        duQuartier.add(r);

        quartiers.add(quartier);                         // 4) l'ensemble des quartiers

        if (priorite == Priorite.URGENTE) {              // 5) la file de traitement
            urgences.add(r);
        } else {
            fileAttente.offer(r);
        }
        return r;
    }
```

**Point clé** : `parQuartier.get(quartier)` renvoie `null` quand la clé n'existe pas encore — c'est le comportement normal d'une `Map`. D'où le test, puis la création de la liste **et** son rangement dans la `Map` (`put`) : **oublier le `put` est l'erreur la plus fréquente de cet exercice** (la liste créée serait perdue et les réclamations suivantes du même quartier repartiraient de zéro).

⚠️ **Deux compagnons de route à ne jamais oublier** : quand on aiguille vers `urgences`, on n'ajoute **pas** dans `fileAttente` (le `else` est indispensable), sinon une urgence serait traitée deux fois.

### Étape 3 — Les lectures : renvoyer des copies protégées

```java
    public Reclamation findById(int id) {
        return parId.get(id);        // null si absent : la leçon 03 remplacera ce null par Optional
    }

    public List<Reclamation> toutes() {
        return List.copyOf(toutes);  // copie immuable : l'interieur reste protege
    }

    public List<Reclamation> parQuartier(String quartier) {
        List<Reclamation> liste = parQuartier.get(quartier);
        return liste == null ? List.of() : List.copyOf(liste);   // jamais null pour l'appelant
    }

    public Set<String> quartiers() {
        // Copie TRIÉE et immuable : on ne renvoie pas la référence interne (qui reste modifiable)
        // Remarque : Set.copyOf(...) ne garantit PAS l'ordre — vérifié à l'exécution !
        return Collections.unmodifiableSet(new TreeSet<>(quartiers));
    }

    public int taille() { return toutes.size(); }

    public int enAttente() { return urgences.size() + fileAttente.size(); }
```

**Trois décisions à comprendre, car elles reviennent dans tout code professionnel :**

1. **`List.copyOf(valeur)`** fabrique une liste **immuable** contenant une copie. Un appelant qui fait `registre.toutes().add(...)` reçoit une `UnsupportedOperationException` : impossible de corrompre le registre depuis l'extérieur. C'est la version « collections » de l'encapsulation (leçon 01, partie 2).
2. **`parQuartier` renvoie `List.of()` et non `null`** : une méthode ne doit pas **obliger** l'appelant à se protéger d'un `null`. Le choix « vide » (aucune réclamation) est ici naturel — la leçon 03 approfondira ce débat avec `Optional`.
3. **`quartiers()` : `Collections.unmodifiableSet(new TreeSet<>(quartiers))` et non `Set.copyOf(...)`.** C'est le détail découvert en exécutant le code : `Set.copyOf` crée bien un ensemble immuable, mais **ne garantit pas l'ordre** — la sortie affichait `[Plateau, Fann, Medina]` au lieu du tri attendu. En copiant d'abord dans un `TreeSet` (qui, lui, garantit le tri), puis en l'enveloppant avec `Collections.unmodifiableSet`, on obtient **les deux** : trié **et** non modifiable. Leçon générale : *« immuable » et « ordonné » sont deux propriétés indépendantes ; quand vous en voulez deux, assurez-vous de choisir un outil qui fournit les deux.*

### Étape 4 — La file de traitement priorisée

```java
    public Reclamation prochainATraiter() {              // les urgences passent d'abord
        if (!urgences.isEmpty()) {
            return urgences.poll();
        }
        return fileAttente.poll();
    }
```

**Pourquoi ça marche** : `urgences` est une `PriorityQueue` — `poll()` sort toujours l'élément de plus petit « délai maximal » (donc le plus urgent) grâce au `Comparator` de l'étape 1. Quand elle est vide, on retombe sur la file FIFO classique. Remarquez qu'on **ne parcourt jamais** la `PriorityQueue` : on la **consomme**.

### Étape 5 — Le programme de démonstration

```java
import java.util.List;
import java.util.Set;

public class MainRegistre {

    public static void main(String[] args) {

        RegistreReclamations registre = new RegistreReclamations();

        // 1. On alimente le registre (Medina apparait 2 fois : le Set doit dedupliquer)
        registre.ajouter("Nid de poule", "Medina", Priorite.NORMALE);
        registre.ajouter("Lampadaire éteint", "Plateau", Priorite.URGENTE);
        registre.ajouter("Poubelles non ramassées", "Medina", Priorite.BASSE);
        registre.ajouter("Câble arraché", "Medina", Priorite.URGENTE);
        registre.ajouter("Banc cassé", "Fann", Priorite.NORMALE);

        System.out.println("1) Registre -> " + registre.taille() + " réclamations, "
                + registre.enAttente() + " en attente");

        // 2. Le Set des quartiers : unique ET trié
        Set<String> quartiers = registre.quartiers();
        System.out.println("2) quartiers() -> " + quartiers + " (" + quartiers.size() + ")");
```

        // 3. La Map de regroupement
        List<Reclamation> medina = registre.parQuartier("Medina");
        System.out.println("3) parQuartier(\"Medina\") -> " + medina.size() + " réclamation(s)");
        for (Reclamation r : medina) {
            System.out.println("   - " + r.getLigneAffichage());
        }
        System.out.println("   parQuartier(\"Inconnu\") -> " + registre.parQuartier("Inconnu").size());

        // 4. La recherche par id : present / absent
        System.out.println("4) findById(3) : " + registre.findById(3).getDescription());
        System.out.println("   findById(99) : " + registre.findById(99) + "  (absent -> null)");

        // 5. L'ordre de traitement : les URGENTE sortent d'abord (PriorityQueue)
        System.out.print("5) ordre de traitement :");
        while (registre.enAttente() > 0) {
            Reclamation suivante = registre.prochainATraiter();
            System.out.print(" #" + suivante.getId()
                    + "(" + suivante.getPriorite() + ")");
        }
        System.out.println();

        // 6. La protection de l'interieur : les copies renvoyees sont immuables
        try {
            registre.toutes().add(new Reclamation(99, "Intruse", "Medina", Priorite.BASSE));
        } catch (UnsupportedOperationException e) {
            System.out.println("6) toutes().add(...) refusé : "
                    + e.getClass().getSimpleName() + " (l'interne est protégé)");
        }
    }
}
```

## Vérification par exécution (obligatoire avant de conclure)

```bash
# 1. compiler tous les fichiers du dossier
javac -encoding UTF-8 StatutReclamation.java Priorite.java Reclamation.java \
      RegistreReclamations.java MainRegistre.java

# 2. exécuter la démonstration
java MainRegistre
```

**Résultat obtenu sur cette machine (Java 21.0.7)** — aucune erreur de compilation :

```text
1) Registre -> 5 réclamations, 5 en attente
2) quartiers() -> [Fann, Medina, Plateau] (3)
3) parQuartier("Medina") -> 3 réclamation(s)
   - #1 [NOUVELLE] Nid de poule (Medina, NORMALE) 🆕
   - #3 [NOUVELLE] Poubelles non ramassées (Medina, BASSE) 🆕
   - #4 [NOUVELLE] Câble arraché (Medina, URGENTE) 🆕
   parQuartier("Inconnu") -> 0
4) findById(3) : Poubelles non ramassées
   findById(99) : null  (absent -> null)
5) ordre de traitement : #2(URGENTE) #4(URGENTE) #1(NORMALE) #3(BASSE) #5(NORMALE)
6) toutes().add(...) refusé : UnsupportedOperationException (l'interne est protégé)
```

**Lecture ligne par ligne :**

| Ligne | Ce qui est prouvé |
|---|---|
| 1 | `prochainId` a bien attribué 1→5, et les 5 réclamations sont « en attente » (aucune n'a encore été pollée). |
| 2 | **Trié** (`Fann, Medina, Plateau`) **et** dédupliqué : Medina n'apparaît qu'une fois malgré 3 réclamations. |
| 3 | Le regroupement fonctionne : 3 réclamations à Medina, dans l'ordre d'arrivée (1, 3, 4). |
| 3 bis | Quartier inconnu ⇒ `0` (grâce au `List.of()` de secours), **pas** d'exception. |
| 4 | Recherche par clé : présent ⇒ l'objet ; absent ⇒ `null` (limite que `Optional` corrigera). |
| 5 | **Les deux `URGENTE` sortent en premier**, puis `NORMALE`, `BASSE`, `NORMALE` dans l'ordre d'arrivée de la file. |
| 6 | Les copies renvoyées sont **immuables** : l'extérieur ne peut pas casser le registre. |

> ℹ️ **Détail observé à l'exécution** : `#2` puis `#4` sortent dans cet ordre, alors que les deux sont `URGENTE` (même délai de 4 h). L'ordre entre **priorités égales** n'est **pas garanti** par une `PriorityQueue` (voir la leçon, section 2.5). Si votre programme affiche `#4` avant `#2`, ce n'est **pas** un bug : c'est le comportement normal d'un tas. Une solution professionnelle rendrait l'ordre total déterministe en ajoutant un second critère (l'identifiant) au `Comparator`.

## Erreurs fréquentes et comment les reconnaître

| Message / symptôme | Cause | Correction |
|---|---|---|
| `ConcurrentModificationException` | vous modifiez une structure pendant son parcours | construire une copie, ou utiliser l'`Iterator` |
| `NullPointerException` dans `parQuartier` | vous avez renvoyé `null` au lieu de `List.of()` | `return liste == null ? List.of() : List.copyOf(liste);` |
| Un quartier perd ses réclamations | le `put(quartier, liste)` a été oublié après la création de la liste | faire le `put` **dans** le `if` |
| `UnsupportedOperationException` sur `add` | vous copiez dans `List.of()`/`Arrays.asList()` puis tentez d'ajouter | copier dans un `ArrayList` |
| Les urgentes ne sortent pas en premier | la `PriorityQueue` a été créée **sans `Comparator`** | fournir la règle de comparaison |
| `[Plateau, Fann, Medina]` au lieu du tri | `Set.copyOf` / `HashSet` ne garantissent pas l'ordre | `Collections.unmodifiableSet(new TreeSet<>(...))` |
| `cannot find symbol : method getPriorite()` | le champ `priorite` n'a pas été ajouté à `Reclamation` | l'ajouter avec son getter (section 3 de la leçon) |

## Checklist de validation

Reprenez la liste et vérifiez **sur votre propre code** (pas seulement en lisant) :

- [ ] Les **six** structures du registre sont déclarées, `private final`, avec un commentaire expliquant le besoin de chacune.
- [ ] `ajouter` écrit dans **toutes** les structures, et une urgence ne va **pas** dans `fileAttente`.
- [ ] `parQuartier` crée la liste à la demande **et** la range dans la `Map` (`put`).
- [ ] `toutes()` et `parQuartier(...)` sont protégées par `List.copyOf` : `add` dessus lève `UnsupportedOperationException`.
- [ ] `quartiers()` renvoie un ensemble **trié et immuable**.
- [ ] `prochainATraiter()` sort les `URGENTE` d'abord, puis la file FIFO.
- [ ] Le programme compile **et** affiche la séquence `#2(URGENTE) #4(URGENTE) #1(NORMALE) #3(BASSE) #5(NORMALE)` — ou `#4` avant `#2`, ce qui reste correct (priorités égales).
- [ ] Je sais expliquer, pour chaque structure, **pourquoi ce type plutôt qu'un autre**.

## Conseils pour progresser

1. **Ne supprimez pas `findById` en renvoyant `null` « parce que ça marche ».** Notez-le : la leçon 03 vous montrera pourquoi et comment renvoyer une « absence » explicite avec `Optional`. Vous verrez alors que `findById` est **le** motif le plus utilisé en Spring Boot (`Optional<User> findById(Long id)`).
2. **Refactorez pour comparer.** Réécrivez `parQuartier` avec `computeIfAbsent` (vue en section 2.4 de la leçon) et vérifiez que le programme produit **exactement** la même sortie. C'est la meilleure façon de comprendre que `computeIfAbsent` n'est qu'un raccourci du `get`/`if null`/`put`.
3. **Ajoutez volontairement un second critère de tri** au `Comparator` (par exemple `Integer.compare(a.getId(), b.getId())` quand les délais sont égaux) : la file devient **déterministe**, ce qui est exigé dans un vrai test automatisé (partie 9).
4. **Mesurez-vous au piège de la clé mutable** : remplacez `Map<Integer, Reclamation>` par une `Map` dont la clé serait une `Reclamation`, appelez `marquerResolue()` après l'insertion et observez. Vous aurez vécu le piège 2 de la leçon.
5. **Gardez ce registre sous la main** : il est le point de départ d'Étape 3 du fil rouge SignalCUA, et il deviendra la classe « dépôt » du chapitre Spring Data JPA (partie 8). Le renommage final y sera `ReclamationRepository`.

➡️ **Suite de votre parcours** : avec ce registre, vous avez maintenant de vrais `<...>` partout (`List<Reclamation>`, `Map<Integer, Reclamation>`, `Deque<Reclamation>`). La **leçon 02 — Generics** vous apprend à les écrire vous-même : créer une `Boite<T>`, un `Historique<T>`, et comprendre le fameux `? extends` des bibliothèques.

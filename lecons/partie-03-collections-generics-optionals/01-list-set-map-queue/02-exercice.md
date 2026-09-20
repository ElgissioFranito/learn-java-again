# Exercice 01 — Le registre de réclamations de SignalCUA

> 🧭 **Comment ce fichier s'articule** : la leçon (`01-lecon.md`) a présenté `List`, `Set`, `Map`, `Queue` et leurs implémentations, avec un exemple complet. Ici, vous **construisez vous-même** la classe qui range les réclamations — le squelette qui servira de dépôt de données dans les parties 7 et 8. Faites l'exercice **avant** d'ouvrir `03-correction.md`.

---

## 🎯 Objectif de l'exercice

Écrire une classe `RegistreReclamations` qui range des `Reclamation` dans **plusieurs structures à la fois** (chacune répondant à un besoin différent), puis la tester dans un `main`.

**Fichiers de départ** (repris de la partie 2, aucun changement) : `StatutReclamation.java`, `Priorite.java`, `Reclamation.java`. ⚠️ Pour cet exercice, `Reclamation` doit avoir **un champ `priorite`** (type `Priorite`) en plus des champs de la partie 2 — ajoutez-le avec son getter, exactement comme dans l'extrait de la section 3 de la leçon.

## 📋 Énoncé

### Étape 1 — Les champs du registre
Dans une nouvelle classe `RegistreReclamations`, déclarez **six structures** (champs `private final`, tous instanciés dès la déclaration) et expliquez en commentaire **pourquoi** chacune :

| Champ | Type | Le besoin qu'il sert |
|---|---|---|
| `toutes` | `List<Reclamation>` (`ArrayList`) | parcourir toutes les réclamations dans l'ordre d'arrivée |
| `parId` | `Map<Integer, Reclamation>` (`HashMap`) | retrouver une réclamation par son identifiant |
| `parQuartier` | `Map<String, List<Reclamation>>` (`HashMap`) | regrouper les réclamations par quartier |
| `quartiers` | `Set<String>` | connaître la liste des quartiers touchés, **sans doublon et triée** |
| `fileAttente` | `Deque<Reclamation>` (`ArrayDeque`) | les réclamations non urgentes, traitées en FIFO |
| `urgences` | `PriorityQueue<Reclamation>` | les réclamations urgentes, triées par délai croissant |

Ajoutez un `private int prochainId = 1;` (le registre attribue lui-même les identifiants).

### Étape 2 — La méthode `ajouter`
Écrivez `public Reclamation ajouter(String description, String quartier, Priorite priorite)` qui :
1. crée la `Reclamation` (id = `prochainId`, puis `prochainId++`) ;
2. l'ajoute à **chacune** des cinq structures (⚠️ dans `parQuartier`, la liste du quartier doit être **créée à la demande** si la clé est nouvelle) ;
3. place la réclamation dans `urgences` **si** la priorité est `URGENTE`, sinon dans `fileAttente` ;
4. renvoie la réclamation créée.

### Étape 3 — Les lectures
Ajoutez :
- `public Reclamation findById(int id)` → `null` si l'identifiant est inconnu (la leçon 03 lui donnera mieux) ;
- `public List<Reclamation> toutes()` → une **copie immuable** (`List.copyOf`) ;
- `public List<Reclamation> parQuartier(String quartier)` → une copie immuable, **`List.of()`** si le quartier est inconnu ;
- `public Set<String> quartiers()` → une copie **triée et immuable** de l'ensemble ;
- `public int taille()`, `public int enAttente()` (urgences + file).

### Étape 4 — La file de traitement
`public Reclamation prochainATraiter()` : renvoie et retire **une urgence en priorité** (`urgences.poll()`), sinon l'élément de tête de la file (`fileAttente.poll()`).

### Étape 5 — La démonstration (`MainRegistre.java`)
1. Ajoutez **5 réclamations** dans **3 quartiers**, dont **2 en `Priorite.URGENTE`** et **3 réclamations à Medina** (pour voir le regroupement) ;
2. affichez `taille()` et `enAttente()` ;
3. affichez `quartiers()` (le résultat doit être **trié** et sans doublon) ;
4. affichez le nombre et le détail des réclamations de `parQuartier("Medina")`, puis le cas d'un quartier inconnu ;
5. affichez `findById(3)` puis `findById(99)` ;
6. videz la file de traitement en affichant `#id(PRIORITE)` pour chaque appel à `prochainATraiter()` rendant la file vide ;
7. tentez `registre.toutes().add(...)` dans un `try/catch` et affichez le refus.

## ✅ Critères de réussite

- [ ] Les cinq structures sont présentes, chacune avec le commentaire du besoin qu'elle sert.
- [ ] `parQuartier` ne lève **jamais** de `NullPointerException` sur un quartier inconnu.
- [ ] `toutes()` et `parQuartier(...)` renvoient des copies **immuables** : `add` dessus échoue.
- [ ] `quartiers()` renvoie un ensemble **trié** et sans doublon.
- [ ] `prochainATraiter()` sort **les urgentes d'abord**, puis les autres dans l'ordre d'arrivée.
- [ ] L'identifiant est attribué par le registre (pas par l'appelant).

## 💡 Indications (lisez seulement si bloqué)

- Pour créer la liste d'un quartier à la demande, réutilisez la **boucle classique** de la section 3.b de la leçon : `get`, test de `null`, `put`, puis `add`.
- Pour un `Set` **trié**, l'implémentation est `TreeSet`. Pour protéger la copie tout en gardant le tri, utilisez `Collections.unmodifiableSet(new TreeSet<>(quartiers))` (et non `Set.copyOf`, qui ne garantit pas l'ordre).
- Pour la priorité : `if (priorite == Priorite.URGENTE)` — le `==` entre valeurs d'`enum` est la comparaison correcte (leçon 05 de la partie 2).
- Le `Comparator` de `urgences` compare `getPriorite().getDelaiHeuresMax()` avec `Integer.compare(...)`.

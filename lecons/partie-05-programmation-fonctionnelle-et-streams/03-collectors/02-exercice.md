# Exercice 03 — Le rapport de statistiques SignalCUA

> 🧭 **Comment ce fichier s'articule** : la leçon (`01-lecon.md`) a expliqué les collecteurs (`toMap`, `groupingBy` + aval, `partitioningBy`, `joining`, `counting`), la fusion des doublons et le choix immuable/modifiable. Ici, vous imprimez le **rapport de pilotage** de SignalCUA : c'est l'**Étape 5 du fil rouge**, mot pour mot les trois puces de la roadmap (§5). La correction (`03-correction.md`) suit votre tentative.

---

## 🎯 Objectif de l'exercice

Produire un **rapport console** à partir des 6 réclamations de démonstration (mêmes données que les exercices 01-02) : comptes par statut, index par id, doyenne et dernière par quartier, quartiers urgents, coupe urgent/non-urgent, ligne de quartiers — chaque résultat obtenu par **un seul** `collect(...)`, sans boucle d'accumulation.

**Fichiers de départ** : `Priorite`, `StatutReclamation`, `Reclamation` (inchangés). Aucune classe à créer : que des pipelines dans le `main`.

## 📋 Enoncé

### Étape 1 — Compter par statut (le motif n°1 du fil rouge)

```java
Map<StatutReclamation, Long> comptes = toutes.stream()
        .collect(Collectors.groupingBy(Reclamation::getStatut, Collectors.counting()));
```

1. Affichez `comptes` (attendez `NOUVELLE=3, EN_COURS=2, RESOLUE=1`, **ordre variable**).
2. Refaites-le en version **stable** avec `TreeMap::new` en 2e position : l'affichage devient `{NOUVELLE=3, EN_COURS=2, RESOLUE=1}` **toujours dans cet ordre**. Expliquez en commentaire pourquoi la première version varie et pas la seconde.

### Étape 2 — Indexer par id (`toMap`)

```java
Map<Integer, Reclamation> parId = toutes.stream()
        .collect(Collectors.toMap(Reclamation::getId, Function.identity()));
```

1. Affichez la description de l'id `3` via `parId.get(3).getDescription()` (attendez `Lampadaire eteint`).
2. Provoquez **volontairement** l'erreur des doublons : ajoutez une 7e réclamation avec un id **déjà pris** (`1`), re-collectez **sans fusion** dans un `try/catch (IllegalStateException e)`, et affichez `"doublon refusé : " + e.getMessage()`. Retirez ensuite la 7e réclamation (ou travaillez sur une copie).

### Étape 3 — Doyenne et dernière par quartier (les deux motifs miroirs)

1. **Doyenne** (plus ancienne) par quartier : `groupingBy(getQuartier, minBy(comparing(getDateDeclaration)))`. Affichez, pour chaque quartier, l'id retenu (attendez Medina→6, Plateau→5, Fann→4).
2. **Dernière** (plus récente) par quartier : `toMap(getQuartier, r -> r, fusion « la plus récente gagne »)`. Affichez les id (attendez Medina→3, Plateau→2, Fann→4).
3. En commentaire, dites en **une phrase** la différence entre les deux outils (fusion *pendant* le remplissage vs réduction *après* le paquet).

### Étape 4 — Quartiers urgents + coupe binaire (les deux autres puces du fil rouge)

1. **Quartiers ayant au moins une `URGENTE`** : `filter` (urgentes) + `map` (quartier) + `distinct` + `toList` (attendez `[Medina, Fann]`).
2. **Coupe urgent / non-urgent** : `partitioningBy(priorité == URGENTE)`, puis affichez `urgentes : 2` et `autres : 4` via `.get(true).size()` / `.get(false).size()`.

### Étape 5 — La ligne d'en-tête (`joining`) + la preuve d'immuabilité

1. Affichez `Quartiers : Fann, Medina, Plateau.` via `map` + `distinct` + `sorted` + `joining(", ", "Quartiers : ", ".")`.
2. Prouvez que `Stream.toList()` est immuable : appelez `.add("Ouakam")` sur le résultat de l'étape 4.1 **dans un `try/catch (UnsupportedOperationException e)`**, et affichez `"liste figée : " + e` (le programme **ne doit pas planter**).

### Étape 6 (bonus) — Le combiné : descriptions des urgentes par statut

En **une seule** phrase `collect`, produisez `Map<StatutReclamation, List<String>>` : groupez par statut, et dans chaque paquet filtrez les urgentes puis mappez vers la description (`groupingBy` + `filtering` + `mapping`). Attendez `{NOUVELLE=[Nid de poule dangereux, Fuite d'eau], EN_COURS=[], RESOLUE=[]}` (ordre variable sans `TreeMap`).

## ✅ Criteres de reussite

- [ ] L'étape 1 affiche les bons comptes, puis la version `TreeMap` **stable** + le commentaire d'explication.
- [ ] L'étape 2 affiche `Lampadaire eteint` et capture l'`IllegalStateException` des doublons (message affiché).
- [ ] L'étape 3 affiche doyenne `{1, 5, 4}` et dernière `{3, 2, 4}` par quartier + la phrase de différence.
- [ ] L'étape 4 affiche `[Medina, Fann]`, `urgentes : 2`, `autres : 4`.
- [ ] L'étape 5 affiche exactement `Quartiers : Fann, Medina, Plateau.` et capture l'`UnsupportedOperationException`.
- [ ] Le bonus (si tenté) affiche les descriptions urgentes par statut.
- [ ] Aucune boucle d'accumulation : que des `collect(...)` (les `for` d'affichage restent autorisés).

## 💡 Indications (lisez seulement si bloqué)

- `Collectors.groupingBy(critère, TreeMap::new, aval)` : pensez à `import java.util.TreeMap` (la Map triée).
- `Function.identity()` : pensez à `import java.util.function.Function` (équivaut à `r -> r`).
- `minBy` rend un `Optional<Reclamation>` par paquet : `entry.getValue().map(Reclamation::getId)` pour afficher l'id (rappelez-vous `Optional.map`, partie 3).
- Pour itérer une Map : `for (var entry : map.entrySet())` (`entry.getKey()`, `entry.getValue()` — `var` = le compilateur devine le type, Java 10+).
- `partitioningBy` se lit avec `.get(true)` / `.get(false)` : jamais `null`, même paquet vide.

# Exercice 02 — Interroger SignalCUA en pipelines

> 🧭 **Comment ce fichier s'articule** : la leçon (`01-lecon.md`) a expliqué le modèle source → intermédiaires → terminale, la paresse, l'usage unique et le catalogue d'opérations. Ici, vous remplacez les boucles du registre SignalCUA par des **pipelines déclaratifs** : requêtes de pilotage, pagination, questions oui/non, agrégats numériques. La correction (`03-correction.md`) suit votre tentative.

---

## 🎯 Objectif de l'exercice

Écrire un **tableau de bord console** de SignalCUA entièrement en Streams : filtrer les dossiers ouverts, les trier, les paginer, répondre à des questions métier (`anyMatch`, `findFirst`, `min`), agréger des nombres (`mapToInt`, `reduce`, `summaryStatistics`), et **prouver** la paresse et l'usage unique par l'exécution.

**Fichiers de départ** : le modèle de l'exercice 01 (`Priorite`, `StatutReclamation`, `Reclamation` avec `dateDeclaration`, mêmes 6 réclamations de démonstration). Ajoutez un `record ReclamationLite(int id, String quartier)` (rappel partie 2 : un `record` est une petite classe immuable qui génère constructeur, accesseurs `id()`/`quartier()`, `equals`, `hashCode`, `toString`) pour l'étape 3.

## 📋 Enoncé

### Étape 1 — Les dossiers ouverts, triés (le pipeline « phrase »)

Dans un `main`, écrivez et affichez :

1. les **identifiants** des réclamations **non résolues**, triées par date **croissante** (`filter` + `sorted` + `map` vers l'id + `toList`), affichés comme `[5...]` — attendez-vous à `[5, 2, 6, 1, 4, 3]` ? Non : la 5 est résolue, donc `[2, 6, 1, 4, 3]`. Vérifiez !
2. les **quartiers distincts** des réclamations `URGENTE` (`filter` + `map` + `distinct` + `toList`) : `["Medina", "Fann"]` — l'ordre suit la **première apparition** dans le flux.

### Étape 2 — Pagination : `skip` + `limit`

Toujours sur les non-résolues triées par date **décroissante** (plus récentes d'abord) :

1. la « page 1 » : les **2 premières** (`limit(2)`) → `[3, 4]` ;
2. la « page 2 » : on **saute** les 2 premières puis on prend les **2 suivantes** (`skip(2)` + `limit(2)`) → `[1, 6]`.

Affichez les identifiants de chaque page.

### Étape 3 — Transformer vers un DTO léger (`map` vers un `record`)

Créez `record ReclamationLite(int id, String quartier)`, puis produisez la liste des « fiches légères » des réclamations `EN_COURS`, triées par id :

```java
List<ReclamationLite> fiches = toutes.stream()
        .filter(r -> r.getStatut() == StatutReclamation.EN_COURS)
        .sorted(Comparator.comparingInt(Reclamation::getId))
        .map(r -> new ReclamationLite(r.getId(), r.getQuartier()))
        .toList();
```

Affichez-les (le `toString` du `record` suffit : `ReclamationLite[id=2, quartier=Plateau]`).

### Étape 4 — Questions métier (`anyMatch`, `findFirst`, `min`, `count`)

Affichez :

1. `alerte` : existe-t-il **au moins une** `URGENTE` non résolue ? (`anyMatch`, attendez `true`).
2. `toutResolu` : **toutes** sont-elles `RESOLUE` ? (`allMatch`, attendez `false`).
3. la **plus ancienne** non résolue (`min` sur la date + `ifPresent`, attendez l'id `2`).
4. le **nombre** de réclamations de `Medina` (`filter` + `count`, attendez `3` — notez le type `long`).

### Étape 5 — Agréger des nombres (`mapToInt`, `reduce`, `summaryStatistics`)

1. La **longueur totale** des descriptions, de deux façons : par `mapToInt(...).sum()`, puis par `map(...).reduce(0, (a, b) -> a + b)`. Les deux doivent donner le même nombre.
2. Les **statistiques** des longueurs (`summaryStatistics`) affichées comme `n=6, somme=..., min=..., moyenne=..., max=...`.
3. La **moyenne** seule (`average().orElse(0.0)`).

### Étape 6 — Prouver la paresse et l'usage unique (le cœur de la leçon)

1. **Paresse** : construisez un pipeline avec un `filter` qui affiche `"examen de " + r.getId()`, **sans terminale**. Exécutez : **rien** ne doit s'afficher. Ajoutez ensuite `.count()` : les 6 examens apparaissent **d'un coup**, puis le nombre `2`.
2. **Usage unique** : stockez `toutes.stream()` dans une variable, appelez `.count()`, puis réutilisez la **même** variable avec `.toList()` **dans un `try/catch (IllegalStateException e)`** : affichez `"réutilisation refusée : " + e.getMessage()` (le programme **ne doit pas planter**).

## ✅ Criteres de reussite

- [ ] L'étape 1 affiche `[2, 6, 1, 4, 3]` puis `[Medina, Fann]`.
- [ ] L'étape 2 affiche les pages `[3, 4]` et `[1, 6]`.
- [ ] L'étape 3 affiche deux `ReclamationLite` (id 2 et 6) via le `toString` du record.
- [ ] L'étape 4 affiche `alerte=true`, `toutResolu=false`, l'id `2`, et `medina=3` (type `long`).
- [ ] L'étape 5 donne deux totaux **égaux**, des statistiques `n=6`, et la moyenne.
- [ ] L'étape 6.1 prouve le **silence avant terminale** puis les 6 examens d'un coup.
- [ ] L'étape 6.2 capture l'`IllegalStateException` et affiche son message (pas de plantage).
- [ ] Aucune boucle `for` de parcours : que des pipelines (sauf l'aide `afficherIds`).

## 💡 Indications (lisez seulement si bloqué)

- `sorted(Comparator.comparing(...))` : pensez à importer `java.util.Comparator` (le comparateur de la leçon 01 se branche tel quel).
- `min(...)` rend un `Optional<Reclamation>` : `ifPresent(r -> System.out.println("doyenne=" + r.getId()))`.
- `count()` rend un `long` : affichez avec `"medina=" + n` (pas de conversion nécessaire).
- `mapToInt(r -> ...)` donne un `IntStream` : `.sum()` rend un `int`, `.average()` un `OptionalDouble`.
- Pour l'étape 6.2 : `try { ... } catch (IllegalStateException e) { System.out.println("réutilisation refusée : " + e.getMessage()); }`.

# Correction détaillée — Exercice 03 « Le rapport de statistiques SignalCUA »

> 🧭 **Comment ce fichier s'articule** : vous venez de tenter `02-exercice.md`. Voici la solution complète, les choix expliqués, la **sortie réellement obtenue** (Java 21), les erreurs fréquentes, la checklist et des conseils. Si votre programme produit la même sortie (à l'ordre des Maps près, voir étape 1), l'objectif est atteint. Sorties **réellement obtenues** avec `javac`/`java` 21 (`recentes=[3, 4]`, `ouverts=[2, 6, 1, 4, 3]`, `comptes={NOUVELLE=3, EN_COURS=2, RESOLUE=1}`, `id3=Lampadaire eteint`, `urgentes=2 autres=4`, `doublon refusé OK`).

## Correction pas a pas

### Les fichiers du projet

Quatre fichiers, dans le même dossier :

| Fichier | Rôle |
|---|---|
| `Priorite.java` | l'`enum` des urgences (inchangé) |
| `StatutReclamation.java` | l'`enum` des statuts (inchangé) |
| `Reclamation.java` | la donnée + `dateDeclaration` (inchangé) |
| `Main.java` | le rapport, 6 étapes |

### Le socle commun : imports + jeu de données

Toutes les étapes ci-dessous partagent ce point de départ (à écrire **une fois** en haut de `Main.java`) :

```java
import java.time.LocalDateTime; // rappel partie 3 : des dates fixes pour un résultat déterministe
import java.util.ArrayList; // une liste modifiable (source des streams)
import java.util.Comparator; // le comparateur branché dans sorted/minBy
import java.util.List; // l'interface des listes
import java.util.Map; // l'interface des Maps (résultats des collecteurs)
import java.util.Optional; // l'absence dans le type (paquets minBy)
import java.util.TreeMap; // la Map TRIÉE par clés (rapport stable)
import java.util.function.Function; // Function.identity() = « rend son entrée »
import java.util.stream.Collectors; // LA fabrique de collecteurs (avec un « s »)

public class Main {
    public static void main(String[] args) {
        // Le même jeu de 6 réclamations que les exercices 01-02 (dates FIXES).
        List<Reclamation> toutes = new ArrayList<>(List.of(
                new Reclamation(1, "Medina", Priorite.URGENTE, StatutReclamation.NOUVELLE,
                        "Nid de poule dangereux", LocalDateTime.of(2026, 10, 1, 9, 0)),
                new Reclamation(2, "Plateau", Priorite.NORMALE, StatutReclamation.EN_COURS,
                        "Poubelles non ramassees", LocalDateTime.of(2026, 9, 28, 14, 30)),
                new Reclamation(3, "Medina", Priorite.HAUTE, StatutReclamation.NOUVELLE,
                        "Lampadaire eteint", LocalDateTime.of(2026, 10, 3, 8, 15)),
                new Reclamation(4, "Fann", Priorite.URGENTE, StatutReclamation.NOUVELLE,
                        "Fuite d'eau", LocalDateTime.of(2026, 10, 2, 18, 45)),
                new Reclamation(5, "Plateau", Priorite.BASSE, StatutReclamation.RESOLUE,
                        "Graffiti", LocalDateTime.of(2026, 9, 20, 10, 0)),
                new Reclamation(6, "Medina", Priorite.NORMALE, StatutReclamation.EN_COURS,
                        "Trottoir encombre", LocalDateTime.of(2026, 9, 30, 11, 20))));
        // ... les étapes 1 à 6 s'écrivent ici, dans l'ordre ...
    }
}
```

**Nouveaux imports, en une phrase chacun :** `Map` (l'interface des dictionnaires clé → valeur, partie 3 — ici les résultats) ; `TreeMap` (la Map **triée** par clés, partie 3 — ici le rapport stable) ; `Function` (pour `identity()`, leçon 01) ; `Collectors` (la **fabrique** `toMap`/`groupingBy`/`partitioningBy`/`joining`, leçon 03 — avec un « s », à ne pas confondre avec `Collector`).

### Étape 1 — Compter par statut

```java
System.out.println("== Comptes par statut ==");
Map<StatutReclamation, Long> comptes = toutes.stream()
        .collect(Collectors.groupingBy(Reclamation::getStatut, Collectors.counting()));
System.out.println(comptes); // {NOUVELLE=3, EN_COURS=2, RESOLUE=1} — ordre NON garanti
// Version STABLE : on impose une TreeMap (triée par clés = ordre de déclaration de l'enum).
Map<StatutReclamation, Long> comptesTries = toutes.stream()
        .collect(Collectors.groupingBy(Reclamation::getStatut, TreeMap::new, Collectors.counting()));
System.out.println(comptesTries); // -> {NOUVELLE=3, EN_COURS=2, RESOLUE=1} TOUJOURS dans cet ordre
// Pourquoi la 1re varie et pas la 2e ? groupingBy à 2 args range dans une HashMap (ordre
// de hachage, imprévisible) ; la forme à 3 args range dans LA MAP QU'ON FOURNIT (TreeMap = triée).
```

**Le choix technique :** la forme à 3 arguments de `groupingBy` (critère, **fournisseur de Map**, aval) est le seul moyen de contrôler le contenant. `TreeMap::new` est un `Supplier` (leçon 01 !) : le collecteur l'appelle pour fabriquer la Map vide, puis la remplit. Notez que les deux affichages coïncident **ici** (`{NOUVELLE=3, EN_COURS=2, RESOLUE=1}`) : c'est un hasard d'exécution, pas une garantie — d'où la version `TreeMap`.

### Étape 2 — Indexer par id

```java
System.out.println("== Index par id ==");
Map<Integer, Reclamation> parId = toutes.stream()
        .collect(Collectors.toMap(Reclamation::getId, Function.identity())); // clé = id, valeur = elle-même
System.out.println("id 3 : " + parId.get(3).getDescription()); // -> Lampadaire eteint
// On PROVOQUE le doublon : une 7e réclamation avec un id déjà pris (1), sur une COPIE.
List<Reclamation> avecDoublon = new ArrayList<>(toutes); // on ne touche pas à l'original
avecDoublon.add(new Reclamation(1, "Ouakam", Priorite.HAUTE, StatutReclamation.NOUVELLE,
        "Doublon volontaire", LocalDateTime.of(2026, 10, 4, 10, 0)));
try {
    avecDoublon.stream()
            .collect(Collectors.toMap(Reclamation::getId, Function.identity())); // SANS fusion
    System.out.println("ERREUR : aurait dû lever IllegalStateException");
} catch (IllegalStateException e) { // on capture : le programme NE PLANTE PAS
    System.out.println("doublon refusé : " + e.getMessage());
    // -> doublon refusé : Duplicate key 1 (attempted merging values Reclamation{...} and Reclamation{...})
}
```

**Les choix techniques :** `Function.identity()` plutôt que `r -> r` (lisible, standard) ; la 7e réclamation sur une **copie** (`new ArrayList<>(toutes)`) pour ne pas fausser les étapes suivantes ; le message du JDK (`Duplicate key 1 (attempted merging values ... and ...)`) cite les **deux valeurs en conflit** — précieux pour diagnostiquer en production.

### Étape 3 — Doyenne et dernière par quartier

```java
System.out.println("== Doyenne par quartier (id) ==");
Map<String, Optional<Reclamation>> doyenneParQuartier = toutes.stream()
        .collect(Collectors.groupingBy(Reclamation::getQuartier,
                Collectors.minBy(Comparator.comparing(Reclamation::getDateDeclaration))));
for (var entry : doyenneParQuartier.entrySet()) { // on parcourt les paquets (var = type deviné)
    System.out.println(entry.getKey() + " -> " // le quartier (la clé)
            + entry.getValue().map(Reclamation::getId).orElse(-1)); // l'id de la doyenne
}
// -> Plateau -> 5 (20/09), Medina -> 6 (30/09), Fann -> 4 (02/10) — ordre des lignes variable !

System.out.println("== Dernière par quartier (id) ==");
Map<String, Reclamation> derniereParQuartier = toutes.stream()
        .collect(Collectors.toMap(Reclamation::getQuartier, r -> r,
                (existante, candidate) -> candidate.getDateDeclaration()
                        .isAfter(existante.getDateDeclaration()) ? candidate : existante));
for (var entry : derniereParQuartier.entrySet()) {
    System.out.println(entry.getKey() + " -> " + entry.getValue().getId());
}
// -> Plateau -> 2 (28/09), Medina -> 3 (03/10), Fann -> 4 (02/10)
// Différence en une phrase : la FUSION tranche pendant le remplissage (au moment du conflit),
// tandis que minBy réduit après coup (une fois le paquet complet) — même question miroir, deux moments.
```

**Les choix techniques :** `entry.getValue().map(Reclamation::getId).orElse(-1)` — `Optional.map` (partie 3) transforme l'éventuelle réclamation en id, `orElse(-1)` donne une sentinelle si le paquet était vide (jamais ici, mais le code reste total). `var` évite d'écrire `Map.Entry<String, Optional<Reclamation>>` (Java 10+, inférence locale). Et **Medina → 6** : la doyenne de Medina est bien l'id 6 (30/09), **antérieure** à l'id 1 (01/10) — relisez les dates avant d'accuser le programme !

### Étape 4 — Quartiers urgents + coupe binaire

```java
System.out.println("== Quartiers avec au moins une URGENTE ==");
List<String> quartiersUrgents = toutes.stream()
        .filter(r -> r.getPriorite() == Priorite.URGENTE) // les urgentes (1 et 4)
        .map(Reclamation::getQuartier) // leurs quartiers
        .distinct() // sans doublons
        .toList();
System.out.println(quartiersUrgents); // -> [Medina, Fann]
System.out.println("== Coupe urgent / non-urgent ==");
Map<Boolean, List<Reclamation>> parUrgence = toutes.stream()
        .collect(Collectors.partitioningBy(r -> r.getPriorite() == Priorite.URGENTE));
System.out.println("urgentes : " + parUrgence.get(true).size()); // -> 2 (jamais null)
System.out.println("autres : " + parUrgence.get(false).size()); // -> 4 (jamais null)
```

**Le choix technique :** ici pas de `collect` exotique pour les quartiers urgents : `filter` + `map` + `distinct` + `toList()` suffit — c'est exactement la 2e puce du fil rouge Étape 5. Les Collectors servent quand on **range** ; pour une simple liste plate, `toList()` reste le roi.

### Étape 5 — La ligne d'en-tête + la preuve d'immuabilité

```java
System.out.println("== En-tête ==");
String ligne = toutes.stream()
        .map(Reclamation::getQuartier)
        .distinct()
        .sorted() // trié : affichage STABLE (Fann, Medina, Plateau — pas de hasard)
        .collect(Collectors.joining(", ", "Quartiers : ", "."));
System.out.println(ligne); // -> Quartiers : Fann, Medina, Plateau.
try {
    quartiersUrgents.add("Ouakam"); // la liste vient de Stream.toList() : IMMUABLE
    System.out.println("ERREUR : aurait dû lever UnsupportedOperationException");
} catch (UnsupportedOperationException e) { // on capture : le programme NE PLANTE PAS
    System.out.println("liste figée : " + e); // -> liste figée : java.lang.UnsupportedOperationException
}
```

**Le choix technique :** `sorted()` avant `joining` rend l'en-tête **déterministe** (sans tri, l'ordre dépendrait du flux). Et `"liste figée : " + e` affiche `e.toString()` (`java.lang.UnsupportedOperationException`, sans message — normal : l'exception n'en porte pas, son **type** est l'information).

### Étape 6 (bonus) — Le combiné

```java
System.out.println("== Descriptions des urgentes par statut ==");
Map<StatutReclamation, List<String>> descriptionsUrgentes = toutes.stream()
        .collect(Collectors.groupingBy(Reclamation::getStatut,
                Collectors.filtering(r -> r.getPriorite() == Priorite.URGENTE,
                        Collectors.mapping(Reclamation::getDescription, Collectors.toList()))));
System.out.println(descriptionsUrgentes); // {NOUVELLE=[Nid de poule dangereux, Fuite d'eau], EN_COURS=[], RESOLUE=[]}
```

**Le choix technique :** `filtering` **en aval** (et pas `filter` en amont) : les paquets `EN_COURS` et `RESOLUE` **existent** (vides) au lieu de disparaître. C'est toute la différence : un `filter` amont aurait supprimé les éléments du flux (plus de paquets vides), tandis que `filtering` aval ne filtre **qu'à l'intérieur** de chaque paquet. Pour un rapport, les paquets vides visibles valent mieux que des clés manquantes.

## Verification par exécution

Compilation et exécution réelles (Java 21) :

```text
javac -d out Priorite.java StatutReclamation.java Reclamation.java Main.java
java -cp out Main
```

Sortie obtenue (votre programme doit afficher la même chose, **sauf** l'ordre des lignes des étapes 1/version non triée, 3 et 6 : les `HashMap` n'offrent aucune garantie d'ordre — seule la version `TreeMap` est stable) :

```text
== Comptes par statut ==
{NOUVELLE=3, EN_COURS=2, RESOLUE=1}
{NOUVELLE=3, EN_COURS=2, RESOLUE=1}
== Index par id ==
id 3 : Lampadaire eteint
doublon refusé : Duplicate key 1 (attempted merging values Reclamation{id=1, quartier='Medina', priorite=URGENTE, statut=NOUVELLE, description='Nid de poule dangereux', dateDeclaration=2026-10-01T09:00} and Reclamation{id=1, quartier='Ouakam', priorite=HAUTE, statut=NOUVELLE, description='Doublon volontaire', dateDeclaration=2026-10-04T10:00})
== Doyenne par quartier (id) ==
Plateau -> 5
Medina -> 6
Fann -> 4
== Dernière par quartier (id) ==
Plateau -> 2
Medina -> 3
Fann -> 4
== Quartiers avec au moins une URGENTE ==
[Medina, Fann]
== Coupe urgent / non-urgent ==
urgentes : 2
autres : 4
== En-tête ==
Quartiers : Fann, Medina, Plateau.
liste figée : java.lang.UnsupportedOperationException
== Descriptions des urgentes par statut ==
{NOUVELLE=[Nid de poule dangereux, Fuite d'eau], EN_COURS=[], RESOLUE=[]}
```

Lisez la sortie comme une preuve : les **comptes** (3/2/1) valident `groupingBy` + `counting` ; le **message de doublon** cite les deux valeurs en conflit ; **Medina → 6** (et non 1) valide le `minBy` sur les vraies dates ; `[Medina, Fann]` et `2/4` valident le fil rouge ; `liste figée` valide l'immuabilité de `Stream.toList()`.

## Erreurs frequentes et comment les reconnaitre

- **`IllegalStateException: Duplicate key ...`** hors du `try/catch` → vous avez oublié la fusion sur un `toMap` à clés répétées (piège 1 de la leçon). Ajoutez la fonction de fusion.
- **`NullPointerException`** dans un `toMap`/`groupingBy` → une clé ou valeur `null` (pièges 2-3). Filtrez avant (`filter(r -> r.getX() != null)`).
- **`UnsupportedOperationException`** hors du `try/catch` → `.add` sur une liste de `Stream.toList()` (piège 4). Utilisez `Collectors.toList()` si vous devez encore remplir.
- **Ordre des Maps différent de la correction** → **normal** (sauf version `TreeMap`) : comparez le **contenu**, pas l'ordre. Si votre test exige l'ordre, passez en `TreeMap::new`.
- **Medina → 1 au lieu de 6** → vous avez pris la plus **récente** au lieu de la doyenne (ou comparé à l'envers) : `minBy` + date **croissante** donne la plus ancienne (id 6, 30/09 < 01/10).
- **Paquets `EN_COURS`/`RESOLUE` absents du bonus** → vous avez mis un `filter` **en amont** au lieu de `filtering` **en aval** : le flux a perdu les éléments avant le groupement.

## Checklist de validation

- [ ] Vous comptez par critère avec `groupingBy` + `counting`, en version simple puis `TreeMap` stable.
- [ ] Vous indexez avec `toMap`, lisez avec `Function.identity()`, et capturez le doublon avec son message.
- [ ] Vous produisez doyenne (`groupingBy` + `minBy`) et dernière (`toMap` + fusion) par quartier, et expliquez la différence en une phrase.
- [ ] Vous sortez les quartiers urgents (`filter` + `map` + `distinct`) et la coupe (`partitioningBy` + `.get(true/false)`).
- [ ] Vous collez avec `joining(délimiteur, préfixe, suffixe)` après `sorted` pour un affichage stable.
- [ ] Vous prouvez l'immuabilité de `Stream.toList()` par l'`UnsupportedOperationException` capturée.
- [ ] Vous combinez `filtering` + `mapping` en aval et expliquez pourquoi les paquets vides subsistent.

## Conseils pour progresser

1. **Relisez vos trois rapports** (comptes, doyenne/dernière, coupe) : chacun correspond **mot pour mot** à une puce du fil rouge Étape 5. Cochez-les dans `lecons/fil-rouge-signalcua.md` quand l'Étape 5 y sera écrite.
2. **Ajoutez une 7e réclamation** (un nouveau quartier) et prédisez **chaque ligne** avant d'exécuter : c'est l'exercice mental qui transforme la syntaxe en réflexe.
3. **Préparez la partie 6** : tout ce code vit encore « à la main » dans un dossier. Maven va lui donner une **vraie structure** (dépendances, arborescence, build reproductible) — sans changer une ligne de logique.
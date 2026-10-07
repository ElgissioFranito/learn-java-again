# Correction détaillée — Exercice 02 « Interroger SignalCUA en pipelines »

> 🧭 **Comment ce fichier s'articule** : vous venez de tenter `02-exercice.md`. Voici la solution complète, les choix expliqués, la **sortie réellement obtenue** (Java 21), les erreurs fréquentes, la checklist et des conseils. Si votre programme produit la même sortie, l'objectif est atteint.

## Correction pas a pas

### Les fichiers du projet

Cinq fichiers, dans le même dossier :

| Fichier | Rôle |
|---|---|
| `Priorite.java` | l'`enum` des urgences (inchangé, exercice 01) |
| `StatutReclamation.java` | l'`enum` des statuts (inchangé) |
| `Reclamation.java` | la donnée + `dateDeclaration` (inchangé) |
| `ReclamationLite.java` | le `record` « fiche légère » (`int id`, `String quartier`) |
| `Main.java` | le tableau de bord, 6 étapes |

### Le socle commun : imports + jeu de données

Toutes les étapes ci-dessous partagent ce point de départ (à écrire **une fois** en haut de `Main.java`) :

```java
import java.time.LocalDateTime; // rappel partie 3 : des dates fixes pour un résultat déterministe
import java.util.ArrayList; // une liste modifiable (source des streams)
import java.util.Comparator; // le comparateur de la leçon 01, branché dans sorted(...)
import java.util.IntSummaryStatistics; // accumulateur compte/somme/min/moyenne/max en une passe
import java.util.List; // l'interface des listes
import java.util.Optional; // l'absence dans le type (rappel partie 3)
import java.util.stream.Stream; // le type « traitement », pour la preuve d'usage unique

public class Main {
    public static void main(String[] args) {
        // Le même jeu de 6 réclamations que l'exercice 01 (dates FIXES : résultat déterministe).
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

**Nouveaux imports, en une phrase chacun :** `IntSummaryStatistics` (« statistiques résumées d'entiers » : compte/somme/min/moyenne/max en une passe, leçon 02) ; `Optional` (l'absence dans le type, partie 3, rendu par `min`/`findFirst`) ; `Stream` (le type « traitement », leçon 02, nécessaire pour stocker un pipeline dans une variable à l'étape 6).

### Étape 1 — Les dossiers ouverts, triés

```java
System.out.println("== Ouvertes par date croissante (ids) ==");
List<Integer> ouvertesTriees = toutes.stream() // SOURCE : le flux des 6
        .filter(r -> r.getStatut() != StatutReclamation.RESOLUE) // on écarte la résolue (id 5)
        .sorted(Comparator.comparing(Reclamation::getDateDeclaration)) // date CROISSANTE
        .map(Reclamation::getId) // on transforme en identifiant (le tri est déjà fait : l'ordre est conservé)
        .toList(); // TERMINALE : on rassemble -> [2, 6, 1, 4, 3]
System.out.println(ouvertesTriees);

System.out.println("== Quartiers des urgentes ==");
List<String> quartiersUrgents = toutes.stream()
        .filter(r -> r.getPriorite() == Priorite.URGENTE) // les urgentes (id 1 puis 4)
        .map(Reclamation::getQuartier) // leurs quartiers : ["Medina", "Fann"]
        .distinct() // sans doublons (ici il n'y en a pas, mais la garde est gratuite)
        .toList();
System.out.println(quartiersUrgents); // -> [Medina, Fann]
```

**Les choix techniques :** `map` **après** `sorted` : le tri porte sur les réclamations (la clé date n'existe que là), puis on ne garde que l'id. L'ordre du flux est **préservé** par `map` : trier puis transformer donne un résultat trié. Et `distinct` garde l'ordre de **première apparition** : `[Medina, Fann]`, pas un ordre alphabétique.

### Étape 2 — Pagination : `skip` + `limit`

```java
System.out.println("== Page 1 (2 plus recentes ouvertes) ==");
List<Integer> page1 = toutes.stream()
        .filter(r -> r.getStatut() != StatutReclamation.RESOLUE)
        .sorted(Comparator.comparing(Reclamation::getDateDeclaration).reversed()) // DÉCROISSANTE
        .limit(2) // les 2 premières -> [3, 4]
        .map(Reclamation::getId)
        .toList();
System.out.println(page1);

System.out.println("== Page 2 ==");
List<Integer> page2 = toutes.stream()
        .filter(r -> r.getStatut() != StatutReclamation.RESOLUE)
        .sorted(Comparator.comparing(Reclamation::getDateDeclaration).reversed())
        .skip(2) // on SAUTE la page 1...
        .limit(2) // ... puis on prend la suivante -> [1, 6]
        .map(Reclamation::getId)
        .toList();
System.out.println(page2);
```

**Le choix technique :** on **reconstruit** le pipeline pour la page 2 (usage unique oblige) au lieu de « réutiliser » quoi que ce soit. `skip` + `limit` = la pagination la plus simple ; en vrai projet, la base de données fera ce travail (partie 8), avec exactement la même logique.

### Étape 3 — Transformer vers un DTO léger

```java
// ReclamationLite.java - un record = une fiche immuable (constructeur + id()/quartier() + toString générés).
record ReclamationLite(int id, String quartier) {
}

System.out.println("== Fiches EN_COURS ==");
List<ReclamationLite> fiches = toutes.stream()
        .filter(r -> r.getStatut() == StatutReclamation.EN_COURS) // id 2 et 6
        .sorted(Comparator.comparingInt(Reclamation::getId)) // triés par id
        .map(r -> new ReclamationLite(r.getId(), r.getQuartier())) // Reclamation -> fiche légère
        .toList();
fiches.forEach(System.out::println); // ReclamationLite[id=2, quartier=Plateau] / [id=6, quartier=Medina]
```

**Le choix technique :** la `map` fabrique un **nouveau** type allégé : c'est le motif **DTO** (« Data Transfer Object », objet de transfert : on n'expose que le nécessaire), que la partie 7 utilisera pour les API REST. Le `record` donne un `toString` lisible **gratuitement**.

### Étape 4 — Questions métier

```java
System.out.println("== Questions métier ==");
boolean alerte = toutes.stream()
        .filter(r -> r.getStatut() != StatutReclamation.RESOLUE)
        .anyMatch(r -> r.getPriorite() == Priorite.URGENTE); // au moins une ? -> true
System.out.println("alerte=" + alerte);
boolean toutResolu = toutes.stream()
        .allMatch(r -> r.getStatut() == StatutReclamation.RESOLUE); // toutes ? -> false
System.out.println("toutResolu=" + toutResolu);
Optional<Reclamation> doyenne = toutes.stream()
        .filter(r -> r.getStatut() != StatutReclamation.RESOLUE)
        .min(Comparator.comparing(Reclamation::getDateDeclaration)); // la plus ancienne ouverte
doyenne.ifPresent(r -> System.out.println("doyenne=" + r.getId())); // -> doyenne=2
long medina = toutes.stream()
        .filter(r -> "Medina".equals(r.getQuartier()))
        .count(); // count rend un long (pas un int !)
System.out.println("medina=" + medina); // -> medina=3
```

**Les choix techniques :** `anyMatch` **court-circuite** (s'arrête à l'id 1, sans examiner la suite) ; `min` rend un `Optional` qu'on lit avec `ifPresent` (jamais `.get()` aveugle, rappel partie 3) ; `count` rend un `long` car un flux peut dépasser 2 milliards d'éléments en théorie (le type `int` ne suffirait pas).

### Étape 5 — Agréger des nombres

```java
System.out.println("== Longueurs des descriptions ==");
int totalInt = toutes.stream()
        .mapToInt(r -> r.getDescription().length()) // IntStream : des int, pas des Integer
        .sum(); // terminale numérique -> 98
System.out.println("total(mapToInt+sum)=" + totalInt);
int totalReduce = toutes.stream()
        .map(r -> r.getDescription().length()) // Stream<Integer> : avec boxing
        .reduce(0, (accumule, longueur) -> accumule + longueur); // on plie depuis 0 -> 98
System.out.println("total(reduce)=" + totalReduce); // le MÊME nombre : les deux voies concordent
IntSummaryStatistics stats = toutes.stream()
        .mapToInt(r -> r.getDescription().length())
        .summaryStatistics(); // compte, somme, min, moyenne, max en UNE passe
System.out.println("n=" + stats.getCount() + ", somme=" + stats.getSum()
        + ", min=" + stats.getMin() + ", moyenne=" + stats.getAverage()
        + ", max=" + stats.getMax()); // -> n=6, somme=98, min=8, moyenne=16.333..., max=23
double moyenne = toutes.stream()
        .mapToInt(r -> r.getDescription().length())
        .average() // OptionalDouble : pas de moyenne si le flux est vide !
        .orElse(0.0); // valeur de repli
System.out.println("moyenne=" + moyenne);
```

**Les choix techniques :** les deux totaux (**98**) prouvent l'équivalence `mapToInt+sum` / `map+reduce` — préférez la première (pas de boxing, intention lisible). `summaryStatistics` remplace 5 parcours par **un seul** : sur des millions de lignes, c'est 5× moins de travail.

### Étape 6 — Prouver la paresse et l'usage unique

```java
System.out.println("== Paresse ==");
Stream<Reclamation> prevision = toutes.stream()
        .filter(r -> {
            System.out.println("examen de " + r.getId()); // trace d'exécution
            return r.getPriorite() == Priorite.URGENTE;
        });
System.out.println("(rien ne s'est affiché ci-dessus : le filtre attend) ");
System.out.println("--- déclenchement ---");
long nombre = prevision.count(); // TERMINALE : LÀ, les 6 examens se produisent d'un coup
System.out.println("urgentes=" + nombre); // -> urgentes=2

System.out.println("== Usage unique ==");
Stream<Reclamation> flux = toutes.stream();
System.out.println("premier usage (count) : " + flux.count()); // consomme le stream -> 6
try {
    flux.toList(); // réutilisation du MÊME stream : refusée
    System.out.println("ERREUR : aurait dû lever IllegalStateException");
} catch (IllegalStateException e) { // on capture : le programme NE PLANTE PAS
    System.out.println("réutilisation refusée : " + e.getMessage());
    // -> réutilisation refusée : stream has already been operated upon or closed
}
```

**Le choix technique :** le `try/catch` autour de la réutilisation n'est **pas** du code de production (on ne « teste » pas une erreur qu'on provoque volontairement en vrai projet) : c'est une **preuve pédagogique**, qui montre le message exact du JDK (`stream has already been operated upon or closed` = « ce flux a déjà été utilisé ou fermé »).

## Verification par exécution

Compilation et exécution réelles (Java 21) :

```text
javac -d out Priorite.java StatutReclamation.java Reclamation.java ReclamationLite.java Main.java
java -cp out Main
```

Sortie obtenue (identique à ce que votre programme doit afficher) :

```text
== Ouvertes par date croissante (ids) ==
[2, 6, 1, 4, 3]
== Quartiers des urgentes ==
[Medina, Fann]
== Page 1 (2 plus recentes ouvertes) ==
[3, 4]
== Page 2 ==
[1, 6]
== Fiches EN_COURS ==
ReclamationLite[id=2, quartier=Plateau]
ReclamationLite[id=6, quartier=Medina]
== Questions métier ==
alerte=true
toutResolu=false
doyenne=2
medina=3
== Longueurs des descriptions ==
total(mapToInt+sum)=98
total(reduce)=98
n=6, somme=98, min=8, moyenne=16.333333333333332, max=23
moyenne=16.333333333333332
== Paresse ==
(rien ne s'est affiché ci-dessus : le filtre attend) 
--- déclenchement ---
examen de 1
examen de 2
examen de 3
examen de 4
examen de 5
examen de 6
urgentes=2
== Usage unique ==
premier usage (count) : 6
réutilisation refusée : stream has already been operated upon or closed
```

Lisez la sortie comme une preuve : les **pages** (`[3,4]` / `[1,6]`) valident `sorted`+`skip`+`limit` ; les **deux totaux 98** valident l'équivalence des deux voies d'agrégation ; le **silence avant `--- déclenchement ---`** prouve la paresse ; le **message anglais du JDK** prouve l'usage unique.

## Erreurs frequentes et comment les reconnaitre

- **`IllegalStateException: stream has already been operated upon or closed`** hors du `try/catch` → vous avez réutilisé un Stream consommé ailleurs dans le programme. Recréez-le (`toutes.stream()`).
- **Rien ne s'affiche alors que le pipeline semble bon** → il manque la **terminale** (piège 2 de la leçon). Ajoutez `.toList()`, `.count()`, `.forEach(...)`.
- **`[1, 6]` au lieu de `[3, 4]` pour la page 1** → vous avez trié **croissant** au lieu de décroissant : il manque `.reversed()`.
- **`doyenne` vide ou absent** → vous avez filtré `== RESOLUE` au lieu de `!= RESOLUE` : le `min` porte alors sur la seule id 5 (ou sur rien).
- **`medina` est un `long`** : si vous l'avez stocké dans un `int`, le compilateur refuse (`incompatible types: possible lossy conversion from long to int`). Gardez `long` ou convertissez explicitement.
- **`average()` sans `orElse`** → vous manipulez un `OptionalDouble`, pas un `double` : `.orElse(0.0)` (ou `orElseThrow`) est obligatoire pour extraire la valeur.

## Checklist de validation

- [ ] Vous écrivez un pipeline complet source → intermédiaires → terminale sans aide.
- [ ] Vous paginez avec `sorted` + `skip` + `limit` et prévoyez les bons identifiants **avant** d'exécuter.
- [ ] Vous transformez vers un `record` DTO avec `map` et lisez son `toString`.
- [ ] Vous répondez à une question métier avec `anyMatch`/`allMatch`/`findFirst`/`min` et lisez l'`Optional` avec `ifPresent`.
- [ ] Vous agrégez en `IntStream` (`sum`, `average`, `summaryStatistics`) et expliquez pourquoi pas de boxing.
- [ ] Vous prouvez la paresse (silence avant terminale) et l'usage unique (`IllegalStateException` capturée).
- [ ] Vous expliquez pourquoi `count()` rend un `long` et `average()` un `OptionalDouble`.

## Conseils pour progresser

1. **Changez le jeu de données** (ajoutez une 7e réclamation, videz la liste) et prédisez la sortie **avant** d'exécuter : que devient `doyenne` sur liste vide ? (`Optional` vide → rien ne s'affiche.) Que devient la moyenne ? (`0.0` via `orElse`.)
2. **Réécrivez l'étape 1 avec une boucle `for`** et comparez les deux versions ligne à ligne : où est l'index ? où est l'accumulation ? C'est ce que le Stream vous épargne.
3. **Préparez la leçon 03** : au lieu d'afficher les quartiers urgents en liste plate, imaginez les **ranger par statut** (`{NOUVELLE=[...], EN_COURS=[...]}`) — c'est le travail de `groupingBy`.
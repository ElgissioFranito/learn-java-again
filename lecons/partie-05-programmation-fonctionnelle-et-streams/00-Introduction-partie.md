# 00 — Introduction de la partie 5 : la programmation fonctionnelle et les Streams

> 🧭 **Pourquoi ce fichier existe, et pourquoi il se lit APRÈS les leçons ?** Les 3 leçons de cette partie définissent chaque terme nouveau dans leur **mini-glossaire** (`📖 Vocabulaire`). Mais quelques notions **transversales** apparaissent dans le **code** des exercices et corrections **sans y être définies** « à part entière » : parcourir une `Map`, fabriquer une liste d'un coup, l'ordre naturel, un test « flaky »… Ce fichier les **rassemble**. Lisez-le en **dernier**, comme un **filet de rattrapage** : si un mot du code vous a échappé, il est probablement ici.

---

## 🧭 Le pont d'entrée : de la partie 4 à la partie 5

À la fin de la partie 4, SignalCUA savait **stocker** ses données (collections, partie 3) et **échouer proprement** (exceptions métier, partie 4). Mais son code de traitement ressemblait encore à ceci : des boucles `for`, des listes temporaires, des `if` imbriqués — la **mécanique** du parcours noyait l'**intention** métier.

| Leçon | Ce qu'elle ajoute | Le problème qu'elle règle |
|---|---|---|
| 01 — Lambdas et interfaces fonctionnelles | passer du **comportement** en paramètre (`Predicate`, `Function`, method references, `Comparator.comparing`) | « chaque critère de filtre/tri exige une nouvelle méthode » |
| 02 — Stream API | enchaîner les traitements en **pipeline** (`filter`, `map`, `sorted`, `reduce`…), paresseux et à usage unique | « les boucles `for` mélangent parcours et intention » |
| 03 — Collectors | **ranger** les résultats (`toMap`, `groupingBy`, `partitioningBy`, `joining`) sans accumulation manuelle | « fabriquer une Map à la main ramène les effets de bord » |

Chaque leçon **s'appuie sur la précédente** : la leçon 02 configure chaque étape de pipeline avec les lambdas de la leçon 01, et la leçon 03 fournit la terminale « intelligente » (`collect`) aux pipelines de la leçon 02. L'**Étape 5** du fil rouge SignalCUA — **statistiques** — se construit au fil de ces 3 leçons (voir `lecons/fil-rouge-signalcua.md`).

---

## 🛠️ Le vocabulaire transversal de la partie 5 (à lire si un mot vous échappe)

> Ces termes ne sont **pas** définis dans les mini-glossaires des leçons 01 à 03 (ou seulement en passant). Tous sont **utilisés** dans le code des exercices ou des corrections.

### A. Parcourir et fabriquer des collections (utilisés dans les 3 corrections)

| Terme | Définition complète |
|---|---|
| **`entrySet()`** | La méthode d'une `Map` qui rend l'**ensemble de ses paires clé-valeur**, pour les parcourir avec un `for` (`for (var entry : map.entrySet())`). |
| **`Map.Entry` / `getKey()` / `getValue()`** | Une **paire** clé-valeur : `getKey()` rend la clé (le critère), `getValue()` rend la valeur (le paquet). |
| **`var`** | Depuis Java 10, demande au compilateur de **deviner le type** d'une variable locale (`var entry = ...` au lieu de `Map.Entry<String, Optional<Reclamation>>`). Uniquement pour les variables **locales**. |
| **`List.of(...)`** | Fabrique **d'un coup** une liste **immuable** (`List.of(a, b, c)`). Dans les corrections, on l'enveloppe (`new ArrayList<>(List.of(...))`) pour obtenir une liste **modifiable** (triable). |
| **`System.out.print(...)`** | Comme `println`, mais **sans** retour à la ligne à la fin (pratique pour construire une ligne petit à petit). |
| **`getOrDefault(clé, repli)`** | Lit une `Map` en rendant une **valeur de repli** si la clé est absente (évite le `null`) — cité en leçon 03 comme ce que `partitioningBy` vous épargne.

### B. Ordre, comparaison, stabilité (utilisés leçons 02 et 03)

| Terme | Définition complète |
|---|---|
| **Ordre naturel** | L'ordre « par défaut » d'un type (`String` : alphabétique, `Integer` : croissant, `LocalDateTime` : chronologique). C'est celui qu'utilisent `sorted()` **sans argument**, `TreeSet` et `TreeMap`. |
| **`Comparable` / `compareTo`** | Le mécanisme derrière l'ordre naturel : un objet `Comparable` sait se comparer (`a.compareTo(b)` : négatif / 0 / positif). `String`, `Integer`, `LocalDateTime` et les `enum` le sont ; vos classes ne le sont que si vous l'écrivez. |
| **`forEachOrdered`** | La variante de `forEach` qui **garantit l'ordre** même en `parallelStream` (citée dans le piège 5 de la leçon 02). |
| **Test « flaky »** | De l'anglais *flaky* (« floconneux », instable) : un test qui **passe ou échoue au hasard** (ici : parce que l'ordre d'une `HashMap` varie). La parade : `TreeMap` (ordre garanti). Vous écrirez ces tests en partie 9. |
| **Débordement (*overflow*)** | Quand un calcul dépasse le max du type (`int` : ~2,1 milliards), le résultat **reboucle** (souvent en négatif) **sans erreur** — d'où `summingLong` pour les montants (piège 5, leçon 03). |

### C. Ce vers quoi la partie 5 pointe (pour ne pas être surpris plus tard)

| Terme | Définition complète |
|---|---|
| **RxJS / `Observable`** | Côté Angular, la bibliothèque et le type des flux **réactifs et asynchrones**. Un `Stream` Java **n'est pas** un `Observable` : il est **synchrone** (tout se calcule dans le fil courant) — ne cherchez pas d'asynchronisme ici. |
| **Project Reactor / Spring WebFlux** | L'équivalent Java du réactif asynchrone (partie 13, « à connaître de nom ») : inutile pour une application administrative classique, où les Streams synchrones + Virtual Threads (partie 10) suffisent. |
| **`BigDecimal`** | Le type des montants **exacts** (monnaie) : ni `double` (approximatif) ni `long` (sans décimales). Cité dans le piège 5 de la leçon 03 ; détaillé en partie 13. |
| **Base de données (partie 8)** | Vos `groupingBy` actuels travaillent sur des listes en mémoire ; en partie 8, les données viendront d'une base PostgreSQL — et `groupingBy` servira à mettre en forme les résultats « plats » des requêtes. |
| **Maven (partie 6)** | Tout ce code vit encore « à la main » dans un dossier ; Maven lui donnera une structure de **vrai projet** (dépendances, arborescence, build reproductible) sans changer une ligne de logique. |

---

## 🧩 Ce que la partie 5 vous a fait construire

À la fin de la leçon 03, vous disposez d'une **Étape 5 du fil rouge complète** :

- une **boîte à outils comportementale** (exercice 01) : interface `FiltreReclamation`, `Predicate`/`Function` composés, tris `Comparator.comparing` ;
- un **tableau de bord en pipelines** (exercice 02) : requêtes triées, pagination `skip`/`limit`, DTO `record`, questions `anyMatch`/`min`, agrégats `mapToInt` ;
- un **rapport de statistiques** (exercice 03) : comptes par statut (`groupingBy` + `counting`), doyenne/dernière par quartier (`minBy` / fusion), quartiers urgents (`filter` + `map` + `distinct`), coupe (`partitioningBy`), en-tête (`joining`) ;
- un réflexe de fond : **déclarer l'intention** (`filter`, `group`, `compte`) au lieu d'écrire la mécanique (boucle, index, accumulation).

---

➡️ **Prochaine étape** : la partie 5 clôt la **phase console** (parties 1-5) : SignalCUA sait représenter, échouer proprement et traiter ses données — tout en mémoire, sans framework. La **partie 6 — Build Tools (Maven / Gradle)** ouvre la phase 2 : structurer ce même code en projet buildable, avec des dépendances gérées — le socle sans lequel Spring Boot (partie 7) serait inutilisable. |
# 🔴 Fil rouge — SignalCUA

> **SignalCUA** est le projet fil rouge qui accompagne toutes les leçons : une mini-application de gestion des réclamations citoyennes (voirie, propreté, éclairage public...) pour la commune. Un citoyen signale un problème, un agent le traite, un statut évolue (`NOUVELLE` → `EN_COURS` → `RESOLUE`).

## Les 4 phases

| Phase | Parties | Ce qu'on construit |
|-------|---------|--------------------|
| 1. Console | 1 → 5 | Classe `Reclamation`, affichage console, repository en mémoire, exceptions, streams |
| 2. Maven | 6 | Projet structuré et buildable, packages, premier test JUnit |
| 3. API | 7 → 9 | API REST Spring Boot + PostgreSQL + migrations Flyway + tests |
| 4. Production | 10 → 12 | Traitements asynchrones, logging structuré, sécurité JWT |

## Règle d'or

Chaque partie de la roadmap se termine par un bloc **🔴 Fil rouge** : une évolution concrète du projet qui applique ce qui vient d'être appris. Le projet évolue chapitre après chapitre — ne sautez pas d'étape.

## 📌 Étape 1 — Partie 1 : la première version console

Une classe `Reclamation` fonctionnelle mais fragile (champs publics, statut en `String` libre), des tableaux de taille fixe, une interface console. Sert de point de comparaison : chaque partie suivante corrigera une de ces faiblesses.

## 📌 Étape 2 — Partie 2 : des objets solides (POO)

La classe `Reclamation` est reconstruite : champs `private`/`final`, transitions de statut contrôlées (`demarrerTraitement()`, `marquerResolue()`), famille `ReclamationVoirie`/`ReclamationProprete`/`ReclamationEclairage` traitée par polymorphisme, contrat `Traitable` (interfaces), versions modernes (`record`, `sealed`, pattern matching) et un `enum StatutReclamation` qui ferme définitivement le statut. Détails : `lecons/partie-02-programmation-orientee-objet/00-Introduction-partie.md`.

## 📌 Étape 3 — Partie 3 : les données en collections, typées, mesurées

1. **`RegistreReclamations` multi-structures** (leçon 01) : `List` pour le parcours, `Map<Integer, Reclamation>` pour l'accès par identifiant, `Map<String, List<Reclamation>>` pour le regroupement par quartier, `Set<String>` des quartiers (uniques et triés), `ArrayDeque` (file FIFO des non-urgentes) et `PriorityQueue` (urgences triées par délai). Toutes les lectures renvoient des copies immuables.
2. **Outillage générique** (leçon 02) : `Boite<T>`, `Paire<K, V>`, `Historique<T>`, méthodes `premier`/`maximum`/`copierTout`/`compter` — écrits une seule fois, utilisés pour plusieurs types.
3. **Absence explicite** (leçon 03) : `Optional<Reclamation> findById(int)`, `premiereUrgente()`, `premiereDuQuartier(String)` ; un `ServiceReclamations` transforme l'absence en exception métier (`orElseThrow`) ou en valeur de repli (`orElse`) — le motif exact des dépôts Spring Data (partie 8).
4. **Le temps entre dans le modèle** (leçon 04) : `Reclamation.dateDeclaration` (`LocalDateTime`), échéance **calculée** `dateDeclaration.plus(priorite.delaiMax())` (`Duration`), `ServiceDelais.estEnRetard`/`ecart`/`resumeSla` avec une **horloge injectée** (`Clock`) pour des tests déterministes (partie 9).

À retenir pour la suite : ce registre deviendra le dépôt de données de la partie 8 (où il prendra le nom `ReclamationRepository`), et le service de SLA deviendra un `@Service` de la partie 7.

## 📌 Étape 4 — Partie 4 : robustifier SignalCUA

1. **Importer un fichier sans se planter** (leçon 01) : `importerDepuisFichier(String chemin)` lit un fichier texte en **`try-with-resources`** (la ressource est toujours fermée, même en cas d'erreur), ignore les lignes vides, et signale chaque **ligne mal formée** par une exception *unchecked* (`IllegalArgumentException`) **sans interrompre** l'import des lignes suivantes. Le fichier **absent** est une exception *checked* (`IOException` / `NoSuchFileException`) gérée explicitement.
2. **Hiérarchie d'exceptions métier** (leçon 02) : racine `SignalcuaException extends RuntimeException`, avec `ReclamationNotFoundException` (porte l'`id`), `ReclamationInvalideException` et `TransitionStatutInterditeException`. La classe `Reclamation` **valide** ses données (id, description) et ses transitions de statut ; `findById()` **lève** au lieu de renvoyer `null` ; le service **propage** jusqu'à la frontière.

À retenir pour la suite : ces exceptions métier deviendront, en partie 7, des réponses HTTP précises (`@ControllerAdvice` → 404, 400, 409), et en partie 9 des cas de tests (`assertThrows(...)`).

## 📌 Étape 5 — Partie 5 : statistiques SignalCUA

1. **Compter par statut** (leçon 03) : `toutes.stream().collect(groupingBy(Reclamation::getStatut, counting()))` → `{NOUVELLE=3, EN_COURS=2, RESOLUE=1}` (version stable avec `TreeMap::new`).
2. **Quartiers avec au moins une `URGENTE`** (leçons 02-03) : `filter` (urgentes) + `map` (quartier) + `distinct` + `toList()` → `[Medina, Fann]`.
3. **Doyenne et dernière par quartier** (leçon 03) : `groupingBy(getQuartier, minBy(comparing(getDateDeclaration)))` (doyenne) et `toMap(getQuartier, r -> r, fusion « la plus récente gagne »)` (dernière).
4. **En bonus** : coupe urgent/non-urgent (`partitioningBy`), en-tête collé (`joining`), index par id (`toMap`).

À retenir pour la suite : la partie 5 clôt la **phase console** — tout ce qui précède (représenter, échouer, traiter) reste valable, mais le code vit encore « à la main » dans un dossier. La partie 6 lui donne une structure Maven ; la partie 8 remplacera les listes par une base PostgreSQL, où `groupingBy` servira à mettre en forme les résultats des requêtes.

## 📌 Étape 6 — Partie 6 : SignalCUA devient un vrai projet Maven

1. **Projet structuré** (leçon 01) : coordonnées `fr.cua.signalcua:signalcua:0.0.1-SNAPSHOT`, Java 21, arborescence `src/main/java` / `src/test/java`, packages `model` (`Reclamation`, `Priorite`, `StatutReclamation`), `repository` (`RegistreReclamations`), `exception` (`SignalcuaException`, `ReclamationNotFoundException`) — classes **déplacées sans réécriture** (seuls `package` + `import` changent), `target/` ignoré.
2. **Premier test vert** (leçon 01) : `RegistreReclamationsTest` (JUnit 5, scope `test`) — `creerPuisRetrouver` + `introuvableLeve` → `Tests run: 2, Failures: 0` ; `./mvnw package` produit `target/signalcua-0.0.1-SNAPSHOT.jar`.
3. **Lecture Gradle** (leçon 02, sans migration) : le jumeau `build.gradle.kts` décodé bloc par bloc ; choix acté — SignalCUA **reste Maven** (solo, build de secondes), Gradle seulement si un besoin futur l'exige.

À retenir pour la suite : la partie 6 clôt la **phase Maven** — sans changer une ligne de logique, le code est désormais buildable en une commande (`./mvnw clean package`) sur toute machine. La partie 7 prend ce projet tel quel : le `pom.xml` y gagnera le parent Spring Boot et le registre deviendra un `@Service` exposé en HTTP.

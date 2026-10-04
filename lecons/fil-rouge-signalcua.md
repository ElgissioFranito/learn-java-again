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

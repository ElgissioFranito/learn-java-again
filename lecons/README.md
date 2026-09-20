# 📚 Le livre de leçons — Java → Spring Boot

> Architecture générée à partir de `00-notes/roadmap-java-springboot-detaillee.md`.
> Fil rouge : **SignalCUA** — mini-application de gestion des réclamations citoyennes (voir `fil-rouge-signalcua.md`).

## 🗺️ Organisation

- `partie-XX-nom/` = une partie de la roadmap.
- `partie-XX-nom/NN-lecon/` = une leçon (un sous-chapitre de la roadmap).
- Chaque dossier de leçon contient (à générer) :
  - `01-lecon.md` : la leçon complète (objectifs, explication, exemples, bonnes pratiques, pièges, checklist, vocabulaire)
  - `02-exercice.md` : l'exercice pratique
  - `03-correction.md` : la correction détaillée + checklist de validation + conseils
  - éventuellement des fichiers supplémentaires selon la complexité.

## 📑 Index des parties

| # | Dossier | Thème | Leçons |
|---|---------|-------|--------|
| 1 | `partie-01-bases-du-langage` | Syntaxe, types, variables, structures de contrôle | 7 |
| 2 | `partie-02-programmation-orientee-objet` | Encapsulation, héritage, interfaces, records, enum | 5 |
| 3 | `partie-03-collections-generics-optionals` | Collections, generics, Optional, java.time | 4 |
| 4 | `partie-04-exception-handling` | Exceptions checked/unchecked, try-with-resources | 3 |
| 5 | `partie-05-programmation-fonctionnelle-et-streams` | Lambdas, Stream API, Collectors | 3 |
| 6 | `partie-06-build-tools-maven-gradle` | Maven, aperçu Gradle | 2 |
| 7 | `partie-07-spring-boot-et-injection-de-dependances` | IoC/DI, contrôleurs REST, services, configuration | 4 |
| 8 | `partie-08-acces-aux-donnees-spring-data-jpa` | JDBC, JPA/Hibernate, repositories, Flyway | 5 |
| 9 | `partie-09-tests-junit-mockito` | JUnit, Mockito, Testcontainers | 3 |
| 10 | `partie-10-concurrence` | Threads, @Async, Virtual Threads | 3 |
| 11 | `partie-11-logging` | SLF4J/Logback, logging structuré | 2 |
| 12 | `partie-12-securite` | Spring Security, JWT/OAuth2 | 2 |
| 13 | `partie-13-monde-au-dela-de-la-roadmap` | Carte des sujets à connaître de nom | 1 (synthèse) |
| 14 | `partie-14-pont-angular-springboot` | Récapitulatif Angular ↔ Spring Boot | 1 (synthèse) |

## 🔴 Les 4 phases du fil rouge SignalCUA

1. **Phase console** (parties 1-5) : Java pur, tout en mémoire.
2. **Phase Maven** (partie 6) : structuration en vrai projet buildable.
3. **Phase API** (parties 7-9) : Spring Boot + base de données + tests.
4. **Phase production** (parties 10-12) : concurrence, logs, sécurité.

## ✅ Suivi de progression

- [x] Partie 1 — Bases du langage
- [x] Partie 2 — POO
- [x] Partie 3 — Collections, Generics, Optionals
- [ ] Partie 4 — Exception Handling
- [ ] Partie 5 — Fonctionnel & Streams
- [ ] Partie 6 — Maven / Gradle
- [ ] Partie 7 — Spring Boot & DI
- [ ] Partie 8 — Spring Data JPA
- [ ] Partie 9 — Tests
- [ ] Partie 10 — Concurrence
- [ ] Partie 11 — Logging
- [ ] Partie 12 — Sécurité
- [ ] Partie 13 — Au-delà de la roadmap
- [ ] Partie 14 — Pont Angular ↔ Spring Boot

## 📖 Règles appliquées

- Nommage des fichiers/dossiers **sans accents ni apostrophes**.
- Chaque leçon est **autonome** : au moins 3 fichiers (leçon, exercice, correction).
- Chaque leçon contient un glossaire « 📖 Vocabulaire / Abréviations » et des encarts de transition (« 🧭 Pont depuis… », « Prochaine étape »).
- Détails dans `.clinerules/gererate-rule.md`.

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

# Optional

> Leçon 03 de la partie 3 — ✅ **générée**.

## Fichiers

- `01-lecon.md` — la leçon complète (le problème de `null`, créer/lire/transformer un `Optional` : `of`/`ofNullable`/`empty`, `orElse`/`orElseGet`/`orElseThrow`, `ifPresent(OrElse)`, `map`/`filter`/`flatMap`, preuve par l'exécution de l'évaluation avide de `orElse`, motif `findById` de Spring, les 4 interdits, vocabulaire, exemples exécutés, bonnes pratiques, 8 pièges, checklist)
- `02-exercice.md` — l'exercice « Le registre passe à `Optional` » (`findById` → `Optional<Reclamation>`, `premiereUrgente`, `premiereDuQuartier`, `ServiceReclamations` avec `orElseThrow`, manipulation sans `if`, bonus : provoquer les deux erreurs)
- `03-correction.md` — la correction détaillée + sortie réellement exécutée (Java 21) + erreurs fréquentes + checklist + conseils

## 🔗 Fil rouge

Le registre SignalCUA ne renvoie plus `null` : l'absence est dans le type (`Optional<Reclamation> findById`) — voir `lecons/fil-rouge-signalcua.md`.

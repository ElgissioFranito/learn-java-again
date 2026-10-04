# Exceptions checked vs unchecked et try-with-resources

> Leçon 01 de la partie 4 — ✅ **générée**.

## Fichiers

- `01-lecon.md` — la leçon complète (qu'est-ce qu'une exception et d'où ça vient, hiérarchie `Throwable`/`Error`/`Exception`/`RuntimeException`, **checked vs unchecked** et pourquoi, `throw`/`throws`, propagation, `try/catch/finally` + multi-catch + ordre des `catch`, **`try-with-resources`**, lecture d'une stack trace, vocabulaire, exemples exécutés, bonnes pratiques 2025-2026, 8 pièges, checklist)
- `02-exercice.md` — l'exercice « Importer des réclamations depuis un fichier » (fil rouge : `try-with-resources`, `catch` précis par cas, fichier absent en checked, lignes invalides en unchecked, bonus ordre des `catch`)
- `03-correction.md` — la correction détaillée + sortie réellement exécutée (Java 21) + erreurs fréquentes + checklist + conseils

## 🔗 Fil rouge

SignalCUA sait **importer des réclamations depuis un fichier sans se planter** : ressource toujours fermée (`try-with-resources`), erreurs de données signalées par une exception *unchecked*, fichier absent géré comme une *checked* — voir `lecons/fil-rouge-signalcua.md`.

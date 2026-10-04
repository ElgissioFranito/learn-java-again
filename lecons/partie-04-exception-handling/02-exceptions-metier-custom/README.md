# Exceptions métier custom

> Leçon 02 de la partie 4 — ✅ **générée**.

## Fichiers

- `01-lecon.md` — la leçon complète (erreur technique vs métier, créer une exception `extends RuntimeException` avec message + contexte, hiérarchie avec racine `SignalcuaException`, *unchecked* pour le métier, chaînage de la cause, propagation jusqu'à la frontière, `Optional` vs exception, vocabulaire, exemples exécutés, bonnes pratiques 2025-2026, 7 pièges, checklist)
- `02-exercice.md` — l'exercice « Robustifier SignalCUA » (fil rouge Étape 4 : `SignalcuaException` + 3 filles, `Reclamation` qui valide, `findById` qui lève, service qui propage, `main` qui capture par type et par famille, chaînage de cause)
- `03-correction.md` — la correction détaillée + sortie réellement exécutée (Java 21) + erreurs fréquentes + checklist + conseils

## 🔗 Fil rouge

SignalCUA dispose d'une **hiérarchie d'exceptions métier** : le registre **lève**, la `Reclamation` **valide** ses données et transitions, le service **propage** — voir `lecons/fil-rouge-signalcua.md` (Étape 4).

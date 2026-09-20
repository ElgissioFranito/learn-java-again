# Date and time api

> Leçon 04 de la partie 3 — ✅ **générée**.

## Fichiers

- `01-lecon.md` — la leçon complète (pourquoi quitter `Date`/`Calendar`, les 5 types de `java.time`, immuabilité, `Duration` vs `Period` (démonstration au changement d'heure), `ChronoUnit`, `DateTimeFormatter`, `Clock` injecté, échéance calculée vs stockée, vocabulaire, exemples exécutés, bonnes pratiques, 7 pièges, checklist)
- `02-exercice.md` — l'exercice « Le SLA des réclamations » (`Priorite.delaiMax()`, `dateDeclaration`, `dateEcheance()` calculée, `ServiceDelais` avec `Clock`, horloge figée, `Period`/`Duration`, formatage, bonus fuseaux et heure d'été)
- `03-correction.md` — la correction détaillée + sortie réellement exécutée (Java 21) + erreurs fréquentes + checklist + conseils

## 🔗 Fil rouge

SignalCUA mesure désormais ses engagements : échéance calculée depuis la priorité, retards détectés avec une horloge **injectée** (`Clock`) — voir `lecons/fil-rouge-signalcua.md`.

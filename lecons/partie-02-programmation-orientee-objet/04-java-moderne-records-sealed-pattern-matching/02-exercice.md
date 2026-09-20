# Exercice 04 — SignalCUA version moderne

> 🧭 **Comment ce fichier s'articule** : la leçon (`01-lecon.md`) a présenté records, sealed et pattern matching sur l'exemple complet. Ici, vous les appliquez à une **autre** famille scellée de SignalCUA : les types de réclamations. La correction (`03-correction.md`) vous attend après votre tentative.

---

## 🎯 Objectif de l'exercice

Modéliser les **types de réclamations** (voirie, propreté, éclairage) en famille scellée de records avec données propres, et les consommer par un switch à pattern matching exhaustif.

## 📋 Énoncé

### Étape 1 — La famille scellée
Créez l'interface scellée `TypeReclamation permits Voirie, Proprete, Eclairage`.

### Étape 2 — Les trois types, en records
- `Voirie(String gravite)` — la gravité du problème (ex. « majeure »).
- `Proprete(boolean recyclable)` — si les déchets sont recyclables.
- `Eclairage(String numeroLampadaire)` — le repère du lampadaire.

Chaque record implémente `TypeReclamation`.

### Étape 3 — Le DTO réclamation
Créez le record `Signalement(int id, String description, String quartier, TypeReclamation type)` avec un constructeur compact validant : `id > 0` et description non vide.

### Étape 4 — Le pattern matching
Dans `MainTypes.java`, créez trois `Signalement` (un de chaque type), puis pour chacun produisez une ligne via un **switch sans `default`** :
- `Voirie v` → `"[VOIRIE] gravité " + v.gravite()`
- `Proprete p` → `"[PROPRETE] tri à faire"` ou `"[PROPRETE] collecte standard"` selon `recyclable()`
- `Eclairage e` → `"[ECLAIRAGE] lampadaire " + e.numeroLampadaire()`

Affichez pour chaque signalement : `#id [TYPE] ...` en combinant le résultat du switch.

### Étape 5 (bonus) — Le compilateur comme filet
Ajoutez un quatrième type `Archive(String motif)` à la liste des `permits` (sans toucher au switch). **Observez l'erreur de compilation** et notez le message exact. Ajoutez ensuite le `case` manquant pour réparer. C'est la démonstration du filet de sécurité.

## ✅ Critères de réussite

- [ ] `TypeReclamation` est scellée et liste exactement ses trois (puis quatre) types.
- [ ] Les records portent leur donnée spécifique (gravite/recyclable/numeroLampadaire).
- [ ] Le constructeur compact de `Signalement` refuse id ≤ 0 et description vide.
- [ ] Le switch est exhaustif, sans `default`, et compile.
- [ ] Le bonus : erreur observée avant, compilation OK après ajout du case.

## 💡 Indications (lisez seulement si bloqué)

- Un record peut contenir un `switch` conditionnel dans son accès... mais ici, faites le switch DANS le `main` (ou une méthode `static` de `MainTypes`), plus simple à lire.
- Le switch retourne une `String` : c'est un « switch expression » (vu en partie 1, leçon 02 — ici avec des patterns de type).

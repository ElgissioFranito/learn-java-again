# Exercice 01 — Importer des réclamations depuis un fichier (sans se planter)

> 🧭 **Comment ce fichier s'articule** : la leçon (`01-lecon.md`) a expliqué les exceptions checked/unchecked, `try/catch/finally`, `try-with-resources` et la lecture d'une stack trace. Ici, vous appliquez tout cela au fil rouge **SignalCUA** : importer des réclamations depuis un fichier texte. La correction (`03-correction.md`) vous attend après votre tentative.

---

## 🎯 Objectif de l'exercice

Écrire un **importateur** de réclamations qui lit un fichier ligne par ligne, **ferme le fichier proprement**, **ne plante jamais** sur une ligne mal formée, et **distingue** clairement :
- le fichier **absent** (erreur *checked*, extérieure au programme) ;
- les lignes **invalides** (erreur *unchecked*, donnée métier).

## 📋 Enoncé

Le fichier contient une réclamation par ligne, au format `quartier;priorite;description`, par exemple :

```text
Medina;URGENTE;Nid de poule dangereux
Plateau;normale;Poubelles non ramassees
Medina;INCONNUE;Lampadaire eteint
Fann;HAUTE;Fuite d'eau
ligne mal formee sans separateur
```

### Étape 1 — Le modèle (déjà vu en partie 2)

Créez, si vous ne les avez plus :
- un `enum Priorite { URGENTE, HAUTE, NORMALE, BASSE }` ;
- une classe `Reclamation` immuable avec `int id`, `String quartier`, `Priorite priorite`, `String description`, et un `toString()` lisible.

### Étape 2 — Un rapport d'import

Créez un `record RapportImport(List<Reclamation> valides, List<String> erreurs)`.
*(Rappel partie 2 : un `record` génère automatiquement le constructeur, les accesseurs, `equals`, `hashCode` et `toString`.)*

### Étape 3 — La méthode d'import (le cœur)

Créez une classe `ImportateurReclamations` avec :

```java
public RapportImport importer(Path chemin) throws IOException
```

Contraintes :
1. Utilisez un **`try-with-resources`** pour lire le fichier (`Files.newBufferedReader`). Le fichier doit être fermé **même si une erreur survient**.
2. **Ignorez** les lignes vides (ce n'est pas une erreur).
3. Pour chaque ligne **non vide**, appelez une méthode privée `analyser(...)` :
   - si la ligne n'a **pas exactement 3 champs** (`split(";")`), levez une `IllegalArgumentException` avec un message **précis** ;
   - si la priorité est **inconnue** (`Priorite.valueOf` échoue), levez une `IllegalArgumentException` **en conservant la cause** (2e argument) ;
   - sinon, créez la `Reclamation`.
4. Dans la boucle, **attrapez uniquement** `IllegalArgumentException` : ajoutez son message à la liste `erreurs` et **continuez** (n'interrompez pas tout l'import).
5. Renvoyez un `RapportImport`.

### Étape 4 — Le `main` de démonstration

1. Écrivez le fichier d'exemple (ci-dessus) avec `Files.writeString` (encodage UTF-8).
2. Importez-le et **affichez** valides et erreurs.
3. Essayez d'importer un fichier **inexistant** (`inexistant.txt`) et attrapez l'erreur en affichant un message lisible.

### Étape 5 (bonus) — Le piège de l'ordre des `catch`

En commentaire **expliqué**, écrivez deux `catch` (`FileNotFoundException` puis `IOException`) et dites ce qui se passerait si on **inversait** l'ordre. **Ne laissez pas** le code final en erreur de compilation : mettez l'exemple dans un commentaire.

## ✅ Criteres de reussite

- [ ] `importer` déclare `throws IOException` (le compilateur l'impose).
- [ ] Le fichier est lu dans un `try-with-resources`.
- [ ] Une ligne vide est ignorée (aucune erreur).
- [ ] Une priorité inconnue produit une erreur **avec un message clair**, **sans arrêter** l'import des lignes suivantes.
- [ ] Le fichier absent produit un message lisible (pas de stack trace brute en sortie principale).
- [ ] La sortie de votre programme correspond à celle de la correction.

## 💡 Indications (lisez seulement si bloqué)

- `Files.newBufferedReader(chemin, StandardCharsets.UTF_8)` renvoie un `BufferedReader`.
- `lecteur.readLine()` renvoie `null` à la fin du fichier : c'est la condition d'arrêt de la boucle.
- `Files.newBufferedReader` lève `NoSuchFileException` (fille d'`IOException`) si le fichier n'existe pas.
- Pour arrêter d'analyser **une** ligne mais **continuer** la boucle : `catch` **dans** la boucle, pas autour.
- `Priorite.valueOf("NORMALE")` renvoie la constante ; `Priorite.valueOf(texte.trim().toUpperCase())` est plus tolérant.

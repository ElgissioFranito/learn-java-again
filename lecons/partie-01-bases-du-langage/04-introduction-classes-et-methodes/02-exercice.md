# Exercice 04 — La classe `Reclamation` (Fil rouge — Étape 1)

> 🧭 **Où en sommes-nous ?** La leçon 04 (`01-lecon.md`) vous a donné classes, constructeurs, `this`, surcharge. Il est temps de construire **l'Étape 1 du fil rouge SignalCUA**, exactement comme la roadmap la décrit : une classe `Reclamation` en Java pur, quelques réclamations créées à la main, affichées en boucle, et un compte par quartier. Correction dans `03-correction.md`.

## Étape 0 — Préparer

Dans votre dossier `exercices`, créez DEUX fichiers côte à côte :

```bash
# Les deux fichiers dans le même dossier : le compilateur
# trouvera Reclamation quand il compilera MainSignal
javac Reclamation.java MainSignal.java
java MainSignal
```

## Étape 1 — La classe `Reclamation`

Dans `Reclamation.java`, créez la classe avec :

1. Quatre champs : `int id`, `String description`, `String quartier`, `String statut`.
2. Un constructeur **complet** `(int id, String description, String quartier)` qui :
   - refuse un `id` négatif ou nul (`throw new IllegalArgumentException(...)`),
   - refuse une description `null` ou vide (`.isBlank()`),
   - affecte les champs avec `this.`,
   - met `statut` à `"NOUVELLE"`.
3. Un constructeur **surchargé** `(int id, String description)` qui délègue au complet avec le quartier `"Non précisé"`.
4. Deux méthodes :
   - `boolean estUrgente()` : true si le statut est `"NOUVELLE"`,
   - `void afficher()` : affiche `#1 [NOUVELLE] Nid de poule (Medina)`.

## Étape 2 — Le programme principal

Dans `MainSignal.java` (avec son `main`) :

1. Créez 4 réclamations : au moins une **sans** quartier (test de la surcharge) et une avec description vide (dans un test de refus — voir étape 4).
2. Rangez-les dans une `ArrayList<Reclamation>`.
3. Affichez- toutes avec une boucle for-each.
4. Affichez le **nombre total** et la liste des réclamations **urgentes** (utilisez `estUrgente()`).

## Étape 3 — Le compte par quartier

Avec un `StringBuilder`, construisez :

```text
Reclamations par quartier
- Medina
- Plateau
- Medina
```

Puis (plus costaud, mais faisable avec les leçons 02+03) : un compte **distinct** par quartier. Indice : gardez un tableau `String[] quartiersConnus` et, pour chaque réclamation, vérifiez si son quartier est déjà compté (`.equals()`), sinon ajoutez-le au tableau connu et incrémentez le compte correspondant dans un tableau parallèle `int[] comptes` (oui, vous reconnaissez le tableau parallèle de la leçon 02 — savourez, c'est la dernière fois !).

## Étape 4 — Tester la validation

```java
// Cette ligne doit PLANTER avec "La description est obligatoire" :
Reclamation mauvaise = new Reclamation(99, "   ");
// Lancez le programme, constatez le plantage, lisez le message,
// PUIS mettez cette ligne en commentaire pour le reste de l'exercice.
```

## Critères de réussite

- [ ] `javac` compile les deux fichiers sans erreur.
- [ ] Créer une réclamation sans quartier fonctionne (`"Non précisé"`).
- [ ] La description vide provoque bien le refus avec le bon message.
- [ ] La liste s'affiche avec le bon format, numérotée et avec statut.
- [ ] Le compte par quartier est juste (Medina comptée 2 fois si vous l'avez utilisée deux fois).

Puis comparez avec `03-correction.md`.

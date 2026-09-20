# Exercice 02 — Des outils génériques pour SignalCUA

> 🧭 **Comment ce fichier s'articule** : la leçon (`01-lecon.md`) a expliqué `<T>`, les bornes, les wildcards et l'effacement de type, avec un exemple complet. Ici, vous écrivez **vos propres** types et méthodes génériques, puis vous provoquez volontairement les erreurs de compilation pour vérifier que vous les comprenez. La correction (`03-correction.md`) vous attend après votre tentative.

---

## 🎯 Objectif de l'exercice

Construire une petite boîte à outils générique réutilisable (`Historique<T>`, `Paire<K, V>`, `Boite<T>`, méthodes `premier`/`maximum`/`copierTout`/`compter`) et l'appliquer à des types très différents — `String`, `Integer`, `Reclamation`, `Priorite` — pour constater que **le même code** fonctionne.

**Fichiers de départ** : ceux de l'exercice 01 (`StatutReclamation.java`, `Priorite.java`, `Reclamation.java`). Aucune modification n'est nécessaire.

## 📋 Énoncé

### Étape 1 — Compléter `Boite<T>`
Reprenez la `Boite<T>` de la leçon et ajoutez-lui :
- `public boolean estVide()` → vrai si rien n'a été rangé ;
- `public T prendreOuDefaut(T valeurParDefaut)` → renvoie le contenu, ou la valeur par défaut si la boîte est vide.

Créez une `Boite<String>` vide : affichez `estVide()`, appelez `prendreOuDefaut(...)`, puis rangez une valeur et affichez le contenu.

### Étape 2 — `Paire<K, V>` avec inversion
Ajoutez à `Paire<K, V>` (le `record` de la leçon) la méthode `public Paire<V, K> inverser()`, qui renvoie une **nouvelle** paire aux types croisés.

Démontrez-le : une paire `Paire<String, Integer>` (« Medina », 3) inversée devient `Paire<Integer, String>` — et l'appel de `.cle()` sur l'inversée doit renvoyer un `Integer` (prouvez-le en calculant `cle() + 1`).

### Étape 3 — Les méthodes génériques
Dans une classe `OutilsReclamation`, écrivez :
1. `public static <T> T premier(List<T> elements)` → le premier élément, avec une `IllegalArgumentException` si la liste est vide ;
2. `public static <T extends Comparable<T>> T maximum(List<T> elements)` → le plus grand, même protection.

Testez-les sur `List.of("Plateau", "Medina", "Fann")` **et** sur `List.of(3, 168, 48)`.

### Étape 4 — Les wildcards (PECS)
Toujours dans `OutilsReclamation`, écrivez :
- `public static <T> void copierTout(List<? extends T> source, List<? super T> destination)` qui recopie les éléments de `source` dans `destination` ;

Puis, dans le `main`, copiez **trois types différents** dans un `List<Object>` nommé `journal` : une `List<Reclamation>`, une `List<String>` et une `List<Integer>`. Affichez la taille du journal.

### Étape 5 — Une `Map` de comptage générique
Écrivez `public static <K> Map<K, Integer> compter(List<K> elements, Map<K, Integer> resultat)` qui incrémente le compteur de chaque clé rencontrée (en utilisant `get` puis `put`, ou `getOrDefault`).

Testez sur `List.of("Medina", "Plateau", "Medina", "Medina", "Fann")` et affichez la `Map` obtenue : Medina doit valoir **3**.

### Étape 6 — `Historique<T>`, la classe générique réutilisable
Écrivez une classe `Historique<T>` qui contient une `List<T>` interne et propose :
- `ajouter(T element)` ;
- `dernier()` → le dernier ajouté, avec `IllegalStateException` si l'historique est vide ;
- `taille()`, `tout()` (copie immuable), `contient(T element)`.

Démontrez avec un `Historique<Reclamation>` **et** un `Historique<String>` : le même code, deux types.

### Étape 7 (bonus) — Provoquer les erreurs
Créez un fichier séparé (ou commentez/recréez temporairement) et **observez les trois messages** :
1. `o instanceof List<String>` → que dit `javac` ?
2. `List<int> nombres = new ArrayList<>();` → que dit `javac` ?
3. `List<Object> objets = new ArrayList<String>();` → que dit `javac` ?

Notez pour chacun : **le type d'erreur** (compilation ou exécution ?) et **pourquoi** (reliez à l'effacement de type ou à l'invariance).

## ✅ Critères de réussite

- [ ] `Boite<T>` fonctionne avec `String`, `Integer` **et** `Priorite` (une valeur d'`enum`), sans aucun `cast`.
- [ ] `Paire.inverser()` renvoie bien les types croisés (l'appel `.cle() + 1` compile sur l'inversée).
- [ ] `premier` et `maximum` lèvent une `IllegalArgumentException` sur liste vide.
- [ ] `copierTout` accepte `List<String>` → `List<Object>` (grâce à `? super`).
- [ ] `compter` renvoie `Medina=3` sur la liste de test.
- [ ] `Historique<T>` est utilisé avec deux types différents sans duplication de code.
- [ ] Les trois erreurs de compilation du bonus sont reproduites **et** expliquées en une phrase chacune.
- [ ] Aucun *raw type* dans votre code : la compilation avec `-Xlint:all` ne produit **aucun** avertissement.

## 💡 Indications (lisez seulement si bloqué)

- `<T>` d'une **classe** se déclare après le nom (`public class Historique<T>`), `<T>` d'une **méthode** se déclare avant le type de retour (`public static <T> T premier(...)`).
- Dans `maximum`, la comparaison s'écrit `element.compareTo(meilleur) > 0` (positif = « plus grand »).
- Pour `copierTout`, souvenez-vous de la phrase de la leçon : « **producteur extends, consommateur super** ».
- `List.of(...)` renvoie une liste **immuable** : elle va très bien dans `source` (on ne fait que lire), jamais dans `destination`.
- Pour le bonus, compilez uniquement le fichier fautif (`javac ErreursGenerics.java`) plutôt que tout le dossier.

# Exercice 03 — « Cette réclamation n'existe pas » : le registre passe à `Optional`

> 🧭 **Comment ce fichier s'articule** : la leçon (`01-lecon.md`) a expliqué `Optional` avec un registre d'exemple. Ici, vous reprenez **votre** `RegistreReclamations` de l'exercice 01 pour le transformer : son `findById` ne renverra plus `null`, mais `Optional<Reclamation>`. La correction (`03-correction.md`) vous attend après votre tentative.

---

## 🎯 Objectif de l'exercice

Transformer le registre de l'exercice 01 en API « honnête » (absence exprimée par le type), ajouter des méthodes de recherche qui renvoient `Optional`, et écrire un service qui **traduit une absence en exception métier** — le motif exact que vous retrouverez avec Spring Boot.

**Fichiers de départ** : ceux de l'exercice 01 (`StatutReclamation.java`, `Priorite.java`, `Reclamation.java`, `RegistreReclamations.java`).

## 📋 Énoncé

### Étape 1 — `findById` passe à `Optional`
Changez la signature du registre :

```java
public Reclamation findById(int id)              // avant
public Optional<Reclamation> findById(int id)    // après
```

et adaptez le corps de la méthode (une ligne suffit avec `Optional.ofNullable`).

### Étape 2 — Deux nouvelles recherches qui renvoient `Optional`
Ajoutez au registre :
1. `public Optional<Reclamation> premiereUrgente()` → la première réclamation de priorité `URGENTE`, dans l'ordre d'arrivée ;
2. `public Optional<Reclamation> premiereDuQuartier(String quartier)` → la première réclamation du quartier, ou vide si le quartier est inconnu.

⚠️ Ces deux méthodes ne doivent **jamais** lever d'exception : « rien trouvé » est un cas **normal**, représenté par `Optional.empty()`.

### Étape 3 — Un service qui traduit l'absence en exception métier
Créez une classe `ServiceReclamations` avec :
- un constructeur qui reçoit un `RegistreReclamations` ;
- `public Reclamation recupererObligatoirement(int id)` qui appelle `findById(...).orElseThrow(() -> new IllegalStateException("Réclamation " + id + " introuvable"))`.

### Étape 4 — Des méthodes qui manipulent l'absence sans `if`
Écrivez dans `ServiceReclamations` :
1. `public String descriptionOuInconnue(int id)` → la description, ou `"inconnue"` si l'identifiant est absent (avec `map` + `orElse`) ;
2. `public boolean estUrgente(int id)` → `true` **seulement** si la réclamation existe **et** est `URGENTE` (avec `filter`) ;
3. `public int idSuivant(int id)` → l'identifiant de la réclamation suivante (`id + 1`), ou `-1` s'il n'y en a pas (avec `flatMap`) ;
4. `public void demarrerSiPresente(int id)` → démarre le traitement **si** la réclamation existe (avec `ifPresent`).

### Étape 5 — Démontrer le piège avide/paresseux
Dans le `main` :
1. écrivez une méthode `String descriptionParDefaut()` qui **affiche** « (calcul effectué) » puis renvoie `"inconnue"` ;
2. appelez-la via `orElse(...)` sur un identifiant **présent**, puis via `orElseGet(...)` sur le même identifiant ;
3. concluez en commentaire : laquelle des deux a « travaillé pour rien » ?

### Étape 6 (bonus) — Provoquer les deux erreurs
Montrez, dans un `try/catch` ou en commentaire expliqué :
1. `Optional.of(null)` → quelle exception, et **à quel moment** ?
2. `registre.findById(99).orElseThrow()` **sans argument** → quelle exception et quel message ?

## ✅ Critères de réussite

- [ ] `findById`, `premiereUrgente` et `premiereDuQuartier` renvoient `Optional<Reclamation>` et **jamais** `null`.
- [ ] Aucune des trois méthodes de recherche ne lève d'exception quand rien n'est trouvé.
- [ ] `recupererObligatoirement` fonctionne pour un identifiant présent **et** produit un message clair pour un absent.
- [ ] `descriptionOuInconnue`, `estUrgente`, `idSuivant`, `demarrerSiPresente` n'utilisent **aucun** `if (x != null)` ni `get()`.
- [ ] La démonstration de l'étape 5 prouve, **par la sortie du programme**, que `orElse` a exécuté le calcul pour rien.
- [ ] Aucun `Optional` n'est utilisé comme champ, paramètre, ni dans `List<Optional<...>>`.
- [ ] La sortie du programme est identique à celle de la correction (ou expliquée si vous avez choisi d'autres textes).

## 💡 Indications (lisez seulement si bloqué)

- `Optional.ofNullable(valeur)` : présent si `valeur != null`, vide sinon.
- Pour `premiereUrgente`, la boucle `for` reste parfaitement adaptée : `return Optional.of(r)` dès la trouvaille, puis `return Optional.empty()` après la boucle.
- `flatMap` sert quand la fonction rend **déjà** un `Optional` : `findById(id).flatMap(r -> findById(r.getId() + 1))`.
- `ifPresent(r -> r.demarrerTraitement())` : pas besoin d'extraire la valeur, et **aucun** test préalable.
- Pour `estUrgente` : `findById(id).filter(r -> r.getPriorite() == Priorite.URGENTE).isPresent()`.
- `Optional.of(null)` **lève** au moment de l'appel (c'est sa définition) ; `orElseThrow()` sans argument lève un `NoSuchElementException` avec un message technique.

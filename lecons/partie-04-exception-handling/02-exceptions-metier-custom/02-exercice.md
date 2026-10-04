# Exercice 02 — Robustifier SignalCUA avec des exceptions métier

> 🧭 **Comment ce fichier s'articule** : la leçon (`01-lecon.md`) a expliqué comment **nommer** une erreur métier (exception custom), la structurer en **hiérarchie**, conserver la **cause** et la **propager** jusqu'à une frontière. Ici, vous transformez le `RegistreReclamations`/`ServiceReclamations` du fil rouge pour qu'ils **lèvent** des exceptions métier au lieu de renvoyer `null` ou `Optional`. La correction (`03-correction.md`) suit votre tentative.

---

## 🎯 Objectif de l'exercice

Créer la **hiérarchie d'exceptions métier** de SignalCUA et l'utiliser : `findById` lève une erreur quand la réclamation est absente, une transition de statut invalide lève une erreur, une donnée invalide lève une erreur — et un point d'entrée les attrape **par famille**.

**Fichiers de départ** : vos `StatutReclamation` (enum), `Reclamation`, `RegistreReclamations`, `ServiceReclamations` (issus des parties 2-3).

## 📋 Enoncé

### Étape 1 — La racine commune
Créez `SignalcuaException` qui **étend `RuntimeException`**, avec **deux constructeurs** :
- `SignalcuaException(String message)`
- `SignalcuaException(String message, Throwable cause)`  *(pour chaîner une cause)*

### Étape 2 — Trois exceptions métier
Créez les trois filles de `SignalcuaException` :
1. `ReclamationNotFoundException` : constructeur `(int id)` ; message `"Réclamation " + id + " introuvable"` ; **champ `id`** + getter.
2. `ReclamationInvalideException` : constructeurs `(String)` et `(String, Throwable)`.
3. `TransitionStatutInterditeException` : constructeur `(StatutReclamation de, StatutReclamation vers)` ; message `"Transition interdite : " + de + " -> " + vers` ; champs + getters.

### Étape 3 — La `Reclamation` valide ses données
Dans le constructeur de `Reclamation` :
- si `id <= 0` → `throw new ReclamationInvalideException("Identifiant invalide : " + id)` ;
- si `description` est `null` ou blanche → `throw new ReclamationInvalideException("Description obligatoire")`.

Dans les transitions de statut :
- `demarrerTraitement()` : si le statut n'est pas `NOUVELLE` → `throw new TransitionStatutInterditeException(statut, EN_COURS)` ;
- `marquerResolue()` : si le statut n'est pas `EN_COURS` → `throw new TransitionStatutInterditeException(statut, RESOLUE)`.

### Étape 4 — Le registre qui lève
Ajoutez à `RegistreReclamations` une méthode :

```java
public Reclamation findById(int id)   // leve ReclamationNotFoundException si absent
```

*(Comparez : en partie 3, la même méthode renvoyait `Optional<Reclamation>`. Les deux sont valides — ici, l'appelant veut une valeur ou une erreur.)*

### Étape 5 — Un service qui propage
Créez `ServiceReclamations` avec un `RegistreReclamations` en dépendance et :
- `public void demarrer(int id)` → `registre.findById(id).demarrerTraitement();`
- `public void resoudre(int id)` → `registre.findById(id).marquerResolue();`

⚠️ Le service **n'attrape rien** : il laisse remonter.

### Étape 6 — Le `main` qui attrape par famille
Écrivez un `main` qui :
1. prouve le **cas nominal** (démarrer puis résoudre → `RESOLUE`) ;
2. provoque et attrape `ReclamationNotFoundException` (id inconnu) ;
3. provoque et attrape `TransitionStatutInterditeException` (résoudre une `NOUVELLE`) ;
4. provoque et attrape `ReclamationInvalideException` (description vide) ;
5. attrape **en une seule fois toutes les erreurs métier** via `catch (SignalcuaException e)` ;
6. **chaîne une cause** : attrapez un `NumberFormatException` et relancez une `ReclamationInvalideException` **avec la cause**, puis affichez `e.getCause()`.

## ✅ Criteres de reussite

- [ ] `SignalcuaException extends RuntimeException` ; **aucun `throws`** n'apparaît dans les signatures.
- [ ] Les trois filles étendent `SignalcuaException` (vérifiable dans le code).
- [ ] `ReclamationNotFoundException` porte un **champ `id`** accessible via `getId()`.
- [ ] `Reclamation` refuse un id `<= 0` et une description vide (vérifié à l'exécution).
- [ ] `findById` absent **lève** l'exception (plus de `null`, plus d'`Optional` ici).
- [ ] Le service **ne contient aucun `try/catch`** : il propage.
- [ ] Le `main` attrape **au moins une fois** `SignalcuaException` (la famille).
- [ ] L'étape 6 affiche bien `cause = NumberFormatException`.
- [ ] La sortie correspond à celle de la correction.

## 💡 Indications (lisez seulement si bloqué)

- Un constructeur d'exception **appelle** `super(...)` : c'est tout ce qu'il faut pour le message.
- Pour le contexte, ajoutez un champ `private final int id;` et un getter.
- « Chaîner la cause » = passer l'exception d'origine en **2e argument** au constructeur : `new ReclamationInvalideException("…", e)`.
- `e.getCause()` renvoie l'exception transmise (ou `null`).
- Ordre des `catch` : **précis d'abord**, famille ensuite (`catch (ReclamationNotFoundException e)` **avant** `catch (SignalcuaException e)` — sinon erreur de compilation).

# Correction 06 — SignalCUA prend ses quartiers dans des packages

> 🧭 **Articulation** : solution pas à pas de `02-exercice.md`. Le point sensible est la compilation/exécution depuis la racine (étape 1) et le package-private (étape 2). Checklist finale reprise en bas.

## Étape 1 — Les fichiers packagés

```java
// src/fr/cua/signalcua/model/Reclamation.java
package fr.cua.signalcua.model;   // PREMIÈRE ligne, toujours

public class Reclamation {        // public : visible depuis le package app
    public static final String STATUT_INITIAL = "NOUVELLE";
    public static final String PREFIXE_ID = "REC-"; // nouvelle constante (étape 2.3)

    static int compteurCreees = 0;

    int id;
    String description;
    String quartier;
    String statut;

    // "public" obligatoire : le constructeur est appelé depuis le package app
    public Reclamation(int id, String description, String quartier) {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("La description est obligatoire");
        }
        this.id = id;
        this.description = description;
        this.quartier = quartier;
        this.statut = STATUT_INITIAL;
        compteurCreees++;
    }

    public Reclamation(int id, String description) {
        this(id, description, "Non précisé");
    }

    // estUrgente() reste utilisable dans le package model ;
    // elle sera passée public quand le besoin apparaîtra

    public void afficher() {
        // PREFIXE_ID compose l'identifiant textuel demandé à l'étape 2
        System.out.println(PREFIXE_ID + id + " [" + statut + "] "
                + description + " (" + quartier + ")");
    }

    public static int totalCreees() { return compteurCreees; }
}
```

> ⚠️ **Leçon de la validation** : sans `public` sur le constructeur et `afficher()`, la compilation échoue avec `cannot be accessed from outside package` — la visibilité package-private (étape 2 de l'exercice) bloque aussi les usages légitimes ! Règle : ce qu'un AUTRE package doit utiliser est `public` ; ce qui reste interne au package n'a pas de modificateur.

```java
// src/fr/cua/signalcua/app/MainSignal.java
package fr.cua.signalcua.app;

import fr.cua.signalcua.model.Reclamation; // adresse complète requise

public class MainSignal {
    public static void main(String[] args) {
        Reclamation r1 = new Reclamation(1, "Nid de poule", "Medina");
        r1.afficher();                                              // REC-1 [NOUVELLE] ...
        System.out.println("Total creees : " + Reclamation.totalCreees()); // 1
    }
}
```

```bash
# Depuis la racine exercices/ :
javac -d build src/fr/cua/signalcua/model/Reclamation.java \
             src/fr/cua/signalcua/app/MainSignal.java
java -cp build fr.cua.signalcua.app.MainSignal
```

**Points de vigilance :**
- La ligne de commande peut se couper avec `\` (antislash en fin de ligne = « la commande continue »).
- `javac` résout lui-même l'`import` : compiler `MainSignal.java` suffit parfois à tirer `Reclamation.java` avec lui.

## Étape 2 — Le package-private en action

```java
// src/fr/cua/signalcua/model/OutilInterne.java
package fr.cua.signalcua.model;

// AUCUN "public" devant class : visibilité package-private
class OutilInterne {
    static String prefixe() {
        return "REC-"; // visible UNIQUEMENT depuis fr.cua.signalcua.model
    }
}
```

**Le test qui devait échouer** : depuis `MainSignal.java` (package `app`), écrire `OutilInterne.prefixe()` produit :

```text
erreur: OutilInterne n'est pas public dans fr.cua.signalcua.model ; impossible d'accéder au membre hors du paquet
```

**Pourquoi c'est une bonne nouvelle** : le compilateur a empêché un package de dépendre d'un détail interne d'un autre. C'est le mécanisme de base de l'encapsulation entre modules (approfondi en partie 2 avec `private`).

**L'utilisation légitime** : `prefixe()` est utilisée PAR `Reclamation` (même package) :

```java
// Dans Reclamation.java (même package que OutilInterne) :
void afficher() {
    System.out.println(OutilInterne.prefixe() + id + " [" + statut + "] " + description + " (" + quartier + ")");
}
// Ici j'ai utilisé la constante PREFIXE_ID dans le code ci-dessus pour rester
// simple ; les deux solutions (constante ou OutilInterne.prefixe()) sont acceptées,
// l'exercice validait surtout que vous AVIEZ constaté le blocage depuis app/.
```

## Étape 3 — L'arborescence compilée

```bash
find build -name "*.class"
```

Résultat attendu :

```text
build/fr/cua/signalcua/model/Reclamation.class
build/fr/cua/signalcua/app/MainSignal.class
```

**Lecture** : `javac -d build` a reproduit l'arborescence des packages dans `build/`, et la JVM retrouve chaque classe grâce à son adresse. C'est exactement le mécanisme que Maven (partie 6) automatisera — vous comprenez maintenant ce qu'il fait pour vous.

## Checklist de validation (récapitulatif)

- [ ] Je déclare un package en première ligne et je place le fichier au bon dossier.
- [ ] J'importe une classe d'un autre package avec son adresse complète.
- [ ] J'explique l'import automatique de `java.lang` (String, System…).
- [ ] Je compile avec `javac -d build` et j'exécute avec `java -cp build <adresse.complète>`.
- [ ] J'ai constaté le blocage du package-private depuis un autre package.
- [ ] Mon arborescence compilée reflète mes packages.
- [ ] Je sais proposer une structure `model/`, `app/` (et demain `controller/`, `service/`…).

## 💡 Conseils

1. **En pratique, laissez VS Code créer les packages** (clic droit → New Package) : il crée dossiers ET ligne `package`. L'important est de savoir LIRE la structure, pas de la taper à la main tous les jours.
2. **Ce que vous venez de faire à la main = le quotidien de Maven** (partie 6) : dossiers `src/main/java`, compilation automatique, exécution simplifiée. La douleur actuelle est un investissement.
3. Si un `Could not find or load main class` apparaît : vérifiez dans l'ordre — (a) l'adresse complète avec points ? (b) exécuté depuis la racine ? (c) `-cp` pointe sur le dossier de compilation ?

➡️ **Prochaine étape** : leçon 07 — le **Scanner** : votre programme va enfin dialoguer avec l'utilisateur et clôturer l'Étape 1 du fil rouge SignalCUA.



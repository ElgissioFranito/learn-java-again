# Leçon 06 — Packages et organisation du code

> 🧭 **Pont depuis la leçon 05** : votre projet vit dans un dossier plat : `Reclamation.java`, `MainSignal.java`… à côté de vos fichiers d'exercices. Ça marche avec 2 classes ; imaginez le même dossier avec 50 fichiers mélangés (modèles, affichage, saisie, utilitaires…) : introuvable. Cette leçon introduit les **packages** — les dossiers virtuels de Java, qui donnent une adresse à chaque classe. C'est aussi la dernière brique avant la saisie clavier (leçon 07) qui clôturera la partie 1.

---

## 1. Objectifs d'apprentissage

À la fin de cette leçon, vous saurez :

- Expliquer ce qu'est un **package** et sa correspondance avec les dossiers.
- Déclarer un package (`package fr.cua.signalcua.model;`) et comprendre la convention du nom inversé.
- Importer des classes d'un autre package avec `import`.
- Organiser SignalCUA en packages par **responsabilité** (`model`, `app`…).
- Connaître la visibilité par défaut (package-private) et à quoi elle sert.

---

## 2. Explication simple

### 2.1 Qu'est-ce qu'un package ?

**Pourquoi ?** Deux raisons. (1) **Trouver** : 50 classes à plat, personne ne s'y retrouve ; classées par responsabilité, tout devient prévisible. (2) **Éviter les collisions** : deux bibliothèques pourraient avoir chacune une classe `Reclamation` ; sans adresse, impossible de dire laquelle utiliser.

**Comment ?** Un package est une **adresse** écrite en première ligne du fichier, qui **doit refléter l'arborescence de dossiers** :

```text
Dossier sur le disque :      src/fr/cua/signalcua/model/Reclamation.java
Adresse dans le fichier :    package fr.cua.signalcua.model;
```

Chaque point de l'adresse = un sous-dossier. C'est exactement l'équivalent de vos dossiers Angular `components/`, `services/`, `models/` — les mêmes réflexes de séparation par responsabilité s'appliquent.

**Analogie** : sans package, vos classes sont toutes empilées dans une pièce. Avec des packages, chaque classe a une adresse postale complète (`fr.cua.signalcua.model.Reclamation`) — unique au monde si votre nom de domaine est le vôtre.

### 2.2 La convention du nom de domaine inversé

**Pourquoi `fr.cua.signalcua` et pas juste `signalcua` ?** Le nom de domaine est unique au monde. En le renversant (`cua.fr` → `fr.cua`), on garantit que personne d'autre n'utilisera la même racine : `com.google.xxx`, `org.apache.xxx`, `fr.cua.signalcua.xxx`. C'est la convention universelle en Java — pas une option.

### 2.3 Déclarer et importer

```java
// Fichier : src/fr/cua/signalcua/model/Reclamation.java
package fr.cua.signalcua.model;  // TOUJOURS la première ligne (avant tout code)

public class Reclamation {
    // ... (le contenu de la leçon 05, inchangé)
}
```

```java
// Fichier : src/fr/cua/signalcua/app/MainSignal.java
package fr.cua.signalcua.app;

// Reclamation vit dans un AUTRE package : il faut l'importer
import fr.cua.signalcua.model.Reclamation;

public class MainSignal {
    public static void main(String[] args) {
        Reclamation r = new Reclamation(1, "Nid de poule", "Medina");
        // ...
    }
}
```

**Points de détail qui évitent des surprises :**
- `java.util.ArrayList` ? C'est le même mécanisme : `ArrayList` vit dans le package `java.util`, d'où l'`import` de la leçon 03.
- Le compilateur **auto-importe** deux choses : tout le package `java.lang` (dont `String`, `System`, `Math` — c'est pourquoi vous n'avez jamais eu à les importer) et les classes du MÊME package (avant cette leçon, `Reclamation` et `MainSignal` étaient dans le « package par défaut », sans adresse — pratique interdite en vrai projet).
- Les IDE (VS Code, IntelliJ) créent les dossiers et les imports automatiquement : avec l'extension Java, commencez par créer le package dans l'explorateur, puis le fichier dedans.

### 2.4 La visibilité package-private

**Pourquoi ?** Partager certaines classes/méthodes avec les voisins du même package… sans les exposer au monde entier. En Java, si vous **n'écrivez aucun modificateur** (`public`, `private`…), l'élément est visible **uniquement dans son package** : c'est la visibilité *package-private* (par défaut).

```java
package fr.cua.signalcua.model;

class OutilInterne { }        // visible seulement dans fr.cua.signalcua.model
void calculInterne() { }      // idem pour une méthode
public class Reclamation { }  // visible partout (grâce à l'import)
```

**Quand ?** C'est un excellent outil de design : ce qui est un détail d'implémentation reste caché aux autres packages. On approfondira les visibilités (`private` surtout) en partie 2 — pour l'instant, retenez que l'ABSENCE de modificateur n'est pas un oubli, c'est un choix.

### 2.5 Organiser par responsabilité

La roadmap est claire : ne JAMAIS tout mettre à plat. En vrai projet, la structure ressemble à :

```text
src/fr/cua/signalcua/
├── model/        ← les objets métier (Reclamation, Agent, Quartier...)
├── app/          ← les points d'entrée (MainSignal)
├── controller/   ← (partie 7) les endpoints HTTP
├── service/      ← (partie 7) la logique applicative
├── repository/   ← (partie 8) l'accès aux données
└── exception/    ← (partie 4) les exceptions métier
```

Aujourd'hui on ne crée que `model` et `app` : les autres se rempliront naturellement au fil du parcours. **Anti-piège signalé par la roadmap** : les dépendances circulaires (package A dépend de B qui dépend de A) sont le signe d'un mauvais découpage — une structure par responsabilité claire les évite naturellement.

---

## 3. Exemples concrets

Restructurons SignalCUA (fichiers de la leçon 05) en arborescence packagée :

```text
exercices/
└── src/
    └── fr/
        └── cua/
            └── signalcua/
                ├── model/
                │   └── Reclamation.java
                └── app/
                    └── MainSignal.java
```

```java
// src/fr/cua/signalcua/model/Reclamation.java
package fr.cua.signalcua.model;

public class Reclamation {
    public static final int MAX_URGENCE = 5;
    public static final String STATUT_INITIAL = "NOUVELLE";

    static int compteurCreees = 0;

    int id;
    String description;
    String quartier;
    String statut;

    // ⚠️ "public" est OBLIGATOIRE ici : le constructeur est appelé depuis
    // le package app. Sans "public", visibilité package-private (leçon 06)
    // -> erreur de compilation "cannot be accessed from outside package".
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

    // Idem : afficher() sera appelée depuis app -> public
    public void afficher() {
        System.out.println("#" + id + " [" + statut + "] " + description + " (" + quartier + ")");
    }

    public static int totalCreees() { return compteurCreees; }
}
```

```java
// src/fr/cua/signalcua/app/MainSignal.java
package fr.cua.signalcua.app;

import fr.cua.signalcua.model.Reclamation; // adresse complète de la classe

public class MainSignal {
    public static void main(String[] args) {
        Reclamation r1 = new Reclamation(1, "Nid de poule", "Medina");
        r1.afficher();
        System.out.println("Total creees : " + Reclamation.totalCreees());
    }
}
```

Compilation et exécution **depuis la racine** `exercices/` (le point de départ a changé à cause des sous-dossiers) :

```bash
# -d build : place les .class dans build/, en respectant l'arborescence des packages
javac -d build src/fr/cua/signalcua/model/Reclamation.java src/fr/cua/signalcua/app/MainSignal.java

# On exécute depuis la racine, en donnant l'ADRESSE COMPLÈTE de la classe (avec points, sans .java)
# -cp build : "classpath" = où chercher les classes compilées
java -cp build fr.cua.signalcua.app.MainSignal
```

Résultat :

```text
#1 [NOUVELLE] Nid de poule (Medina)
Total creees : 1
```

> 💡 Si compiler « à la main » avec `-d` et `-cp` vous semble verbeux : c'est normal, et c'est exactement le travail qu'un outil de build (Maven, partie 6) automatisera. Ici, le but est de comprendre ce que l'outil fera à votre place.

---

## 4. Bonnes pratiques modernes (2025-2026)

- **Jamais de package par défaut** (classe sans `package`) dans un vrai projet — interdit par tous les outils professionnels.
- **Organisation par responsabilité** : `model`, `app`, puis plus tard `controller`, `service`, `repository`, `exception`, `dto`. La roadmap insiste : pas de package fourre-tout à 50 classes à plat.
- **Nom de domaine inversé** : `fr.cua.signalcua.model` — universel en Java.
- **Package-private par défaut** : n'exposez (`public`) que ce qui doit être utilisé de l'extérieur ; le reste sans modificateur.
- **Pas de dépendances circulaires** : si A a besoin de B et B de A, le découpage est à revoir.
- **Laissez l'IDE gérer imports et arborescence** (VS Code + Extension Pack for Java le fait très bien) — mais sachez lire la structure à la main, notamment pour déboguer un chemin de classe.

## 5. Pièges à éviter

### Piège 1 — La déclaration `package` ne correspond pas aux dossiers
```java
// Fichier placé dans : src/fr/cua/signalcua/model/Reclamation.java
// ❌ MAUVAIS : adresse qui ne matche pas le dossier -> échec de compilation/exécution
package fr.cua.signalcua;

// ✅ BON : chaque point = un dossier
package fr.cua.signalcua.model;
```

### Piège 2 — Exécuter avec le nom du fichier au lieu de l'adresse
```bash
# ❌ MAUVAIS : depuis build/, "java MainSignal" ne trouvera pas la classe packagée
java MainSignal          # Erreur: Could not find or load main class

# ✅ BON : l'adresse complète, avec des points, sans .java
java -cp build fr.cua.signalcua.app.MainSignal
```

### Piège 3 — Tout mettre `public` « au cas où »
```java
// ❌ MAUVAIS : tout est exposé au monde entier, y compris les détails internes
public class OutilInterne { public void calculSecret() { ... } }

// ✅ BON : sans modificateur, visible seulement du même package
class OutilInterne { void calculSecret() { ... } }
// (On poussera plus loin en partie 2 avec private + getters.)
```

### Anti-pattern — le package fourre-tout
```text
// ❌ MAUVAIS : 50 classes à plat, tout mélangé
src/fr/cua/signalcua/
├── Reclamation.java
├── MainSignal.java
├── Affichage.java
├── Saisie.java
├── Utilitaire.java
└── ... (45 autres)

// ✅ BON : triées par responsabilité, chacune à sa place prévisible
src/fr/cua/signalcua/
├── model/Reclamation.java
├── app/MainSignal.java
└── (plus tard : controller/, service/, repository/, exception/)
```

---

## 📖 Vocabulaire / Abréviations

| Terme | Définition en une ligne |
|---|---|
| **Package** | Espace de nommage Java correspondant à une arborescence de dossiers. |
| **Espace de nommage** | Système d'adresses qui garantit l'unicité des noms (deux `Reclamation` dans deux packages coexistent). |
| **`package` (instruction)** | Première ligne du fichier : déclare l'adresse de la classe. |
| **`import`** | Instruction pour utiliser une classe d'un autre package sans écrire son adresse complète. |
| **Import explicite / étoile** | `import a.b.C` (une classe) vs `import a.b.*` (tout le package — à éviter pour la lisibilité). |
| **`java.lang`** | Package de base (`String`, `System`, `Math`), importé automatiquement. |
| **Package par défaut** | Absence de package : toléré pour des essais, interdit en vrai projet. |
| **Package-private** | Visibilité par défaut (aucun modificateur) : visible uniquement dans le même package. |
| **Modificateur d'accès** | Mot-clé qui règle la visibilité (`public`, `private`, `protected`, ou rien). |
| **Nom de domaine inversé** | Convention `fr.cua.xxx` (depuis `cua.fr`) garantissant l'unicité des racines de packages. |
| **Classpath (`-cp`)** | Liste des endroits où la JVM cherche les classes compilées. |
| **`javac -d build`** | Compilation qui place les `.class` dans `build/` en respectant l'arborescence des packages. |
| **Dépendance circulaire** | Package A dépend de B qui dépend de A : signe d'un mauvais découpage. |

## Checklist de validation

Avant de passer à la leçon 07, vérifiez que vous savez :

- [ ] Expliquer ce qu'est un package et pourquoi le nom de domaine est inversé.
- [ ] Déclarer un package et placer le fichier au bon endroit dans l'arborescence.
- [ ] Importer une classe d'un autre package.
- [ ] Expliquer pourquoi `String` et `System` n'ont jamais besoin d'import (`java.lang`).
- [ ] Décrire la visibilité package-private et son intérêt.
- [ ] Compiler et exécuter un projet packagé avec `javac -d` et `java -cp`.
- [ ] Proposer une organisation par responsabilité pour un projet qui grandit.

➡️ **Prochaine étape** : votre code est bien rangé ; il ne lui manque plus que le **contact avec l'utilisateur** : la leçon 07 introduit le `Scanner` pour lire les saisies clavier et finaliser l'Étape 1 du fil rouge (une réclamation saisie en console, statuts en action).




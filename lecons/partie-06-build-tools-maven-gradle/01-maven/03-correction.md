# Correction détaillée — Exercice 01 « Structurer SignalCUA en projet Maven »

> 🧭 **Comment ce fichier s'articule** : vous venez de tenter `02-exercice.md` (arborescence + `pom.xml` + migration + `./mvnw test` + `./mvnw package`). Voici la solution complète, les choix expliqués, la **sortie réellement obtenue** (code métier compilé et exécuté avec Java 21 : voir § Vérification), les erreurs fréquentes, la checklist et des conseils. Maven/`mvnw` n'étant pas installés sur cette machine, les sorties `mvnw` sont les sorties **attendues** d'un projet correct ; le comportement métier, lui, a été **prouvé par exécution**.

## Correction pas à pas

### Les fichiers du projet

Neuf fichiers, chemins **relatifs** depuis `signalcua/` :

| Fichier | Rôle |
|---|---|
| `pom.xml` | la recette (coordonnées, Java 21, JUnit `test`, compilateur 21) |
| `.gitignore` | contient `target/` : le plan de travail ne se versionne pas |
| `model/Priorite.java` | l'enum des urgences (déplacé, `package` ajouté) |
| `model/StatutReclamation.java` | l'enum des statuts (déplacé) |
| `model/Reclamation.java` | la donnée + transitions (logique **inchangée**) |
| `exception/SignalcuaException.java` | racine unchecked (déplacée) |
| `exception/ReclamationNotFoundException.java` | porte l'`id` (déplacée) |
| `repository/RegistreReclamations.java` | `ajouter` + `findById` qui lève (+ imports) |
| `repository/RegistreReclamationsTest.java` | les 2 tests (voir Étape 3) |

### Étape 1 — Le `pom.xml` (reprendre §3.1 de la leçon, correct tel quel)

Rien à inventer : coordonnées `fr.cua.signalcua:signalcua:0.0.1-SNAPSHOT`, Java 21 + UTF-8, JUnit `junit-jupiter` 5.11.3 en `<scope>test</scope>`, `maven-compiler-plugin` en 21. **Choix** : pas de dépendance Spring — phase Maven pure, Spring arrive en partie 7.

### Étape 2 — Les classes migrées (seuls `package` + `import` changent)

Le point délicat est **toujours** le même : le `package` = le chemin dossier, et tout usage d'un **autre** package = un `import` :

```java
package fr.cua.signalcua.repository; // PREMIÈRE ligne : reflète .../repository/RegistreReclamations.java

import fr.cua.signalcua.exception.ReclamationNotFoundException; // AUTRE package : import obligatoire
import fr.cua.signalcua.model.Reclamation; // AUTRE package : import obligatoire
import java.util.ArrayList; // JDK : import obligatoire
import java.util.List;

public class RegistreReclamations {
    private final List<Reclamation> toutes = new ArrayList<>(); // stockage interne

    public void ajouter(Reclamation r) { // écriture
        toutes.add(r);
    }

    public Reclamation findById(int id) { // lecture : lève au lieu de rendre null (partie 4)
        for (Reclamation r : toutes) {
            if (r.getId() == id) {
                return r; // trouvé
            }
        }
        throw new ReclamationNotFoundException(id); // absent : on LÈVE avec le contexte
    }
}
```

**Pourquoi `Reclamation.java` n'a besoin d'aucun `import` métier ?** Parce que `Priorite` et `StatutReclamation` sont dans le **même** package `model` : même tiroir = pas d'import.


### Étape 3 — Le test (le contrat exécutable du registre)

```java
package fr.cua.signalcua.repository; // MÊME package que la classe testée

import fr.cua.signalcua.exception.ReclamationNotFoundException; // attendue au test 2
import fr.cua.signalcua.model.Priorite; // pour construire une réclamation
import fr.cua.signalcua.model.Reclamation;
import fr.cua.signalcua.model.StatutReclamation;
import org.junit.jupiter.api.Test; // @Test = « cette méthode EST un test »
import static org.junit.jupiter.api.Assertions.assertEquals; // vérifie une égalité
import static org.junit.jupiter.api.Assertions.assertThrows; // vérifie une exception

class RegistreReclamationsTest { // ClasseTestée + Test, dans src/test/java
    @Test
    void creerPuisRetrouver() { // phrase métier : on comprend SANS lire le corps
        RegistreReclamations registre = new RegistreReclamations(); // ARRANGE
        registre.ajouter(new Reclamation(1, "Medina", Priorite.NORMALE, // ACT
                StatutReclamation.NOUVELLE, "Nid de poule"));
        assertEquals(1, registre.findById(1).getId()); // ASSERT (attendu, réel)
    }

    @Test
    void introuvableLeve() {
        RegistreReclamations registre = new RegistreReclamations();
        assertThrows(ReclamationNotFoundException.class, // on EXIGE cette exception...
                () -> registre.findById(999)); // ...pour un id absent (lambda, partie 5)
    }
}
```

Sortie **attendue** de `./mvnw test` (plugin Surefire) :

```text
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

Puis `./mvnw package` ajoute (tests rejoués puis emballage) :

```text
[INFO] Building jar: target/signalcua-0.0.1-SNAPSHOT.jar
[INFO] BUILD SUCCESS
```

## Vérification par exécution

`mvnw` étant absent de cette machine, le comportement a été prouvé en compilant (`javac`, JDK 21) les 6 classes migrées dans leur arborescence `fr/cua/signalcua/...` puis en exécutant un `Main` qui rejoue les 2 assertions. Sortie **réellement obtenue** :

```text
retrouve=1
leve=OK id=999
```

Lisez : `findById(1)` rend `1` (test 1 vert) ; `findById(999)` lève `ReclamationNotFoundException` avec l'id `999` (test 2 vert). Ce même code produira les sorties `mvnw` ci-dessus dès qu'un wrapper est présent : la logique est saine, seul l'outil manque ici.

## Erreurs fréquentes et comment les reconnaître

- Classe introuvable / `package does not match` : le `package` ne correspond pas au dossier. **Remède** : première ligne = chemin dossier.
- `cannot find symbol: Reclamation` dans le repository : `import` oublié. **Remède** : usage hors package = import.
- `Failures: 1` sur `creerPuisRetrouver` : le registre ne retrouve pas. **Remède** : lire la ligne d'assertion, pas Maven.
- `./mvnw : commande introuvable` : pas dans `signalcua/` ou wrapper non généré. **Remède** : `ls` doit montrer `mvnw` + `pom.xml`.
- Premier `./mvnw` très long : normal, il télécharge Maven une seule fois (connexion requise).

## Checklist de validation

- [ ] Chaque classe au bon chemin avec le bon `package`, imports corrects, logique inchangée.
- [ ] `pom.xml` : coordonnées + Java 21 + JUnit en scope `test` + compilateur 21.
- [ ] `.gitignore` contient `target/`.
- [ ] `./mvnw test` : `Tests run: 2, Failures: 0, Errors: 0`, `BUILD SUCCESS`.
- [ ] `./mvnw package` : `target/signalcua-0.0.1-SNAPSHOT.jar` existe.
- [ ] `dependency:tree` montre `junit-jupiter:jar:5.11.3:test` et ses transitives.

## Conseils pour progresser

- Rejouez `./mvnw clean package` les yeux fermés : vous la taperez des centaines de fois en partie 7.
- Cassez volontairement : retirez `<scope>test</scope>`, relancez `dependency:tree`, observez JUnit embarqué — puis remettez le scope. On retient mieux ce qu'on a cassé soi-même.
- Le `pom.xml` d'Initializr en partie 7 reprendra ce squelette + le parent Spring Boot (BOM). Rien ne surgira de nulle part.

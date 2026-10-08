# Lecon 01 — Pourquoi un framework ? (avant meme le « comment »)

> 🧭 **Pont depuis la partie 6** : a la fin de la partie 6, SignalCUA est un vrai projet Maven : structure `src/main/java`, packages `model`, `repository`, `exception`, build en une commande (`./mvnw package`), 1 test vert. Mais il reste un travail manuel invisible : c'est **vous** qui ecrivez chaque `new RegistreReclamations()`, chaque `new ReclamationService(registre)`, dans le bon ordre, au bon endroit. Avec 3 classes ca va. Avec 30 classes qui dependent les unes des autres, cette « plomberie » devient un deuxieme metier. Cette lecon explique **quel probleme** Spring Boot resout, **avant** d'apprendre a s'en servir (lecons 02 et suivantes).

---

## 1. Objectifs d'apprentissage

A la fin de cette lecon, vous saurez :

- Expliquer la difference entre une **bibliotheque** (vous l'appelez) et un **framework** (c'est lui qui appelle votre code), avec un exemple.
- Montrer **pourquoi** le cablage manuel avec `new` devient fragile quand les dependances se multiplient.
- Definir une **dependance** (« de quoi ma classe a besoin pour travailler ») sur l'exemple `ReclamationService` → `RegistreReclamations`.
- Expliquer en une phrase l'**inversion de controle (IoC)** : ce n'est plus vous qui creez les objets, c'est le framework au demarrage.
- Dire **quand** on a besoin de Spring Boot (application avec beaucoup d'objets lies : API web) et quand on n'en a pas besoin (petit programme console).

---

## 2. Explication simple

### 2.1 Le point de depart : ce que vous savez deja faire

Reprenons vos classes de la partie 6, en version tres simplifiee. D'abord, un registre qui stocke les reclamations en memoire (une `List`, partie 3) :

```java
// RegistreReclamations : le tiroir qui garde les reclamations en memoire.
public class RegistreReclamations {
    private final java.util.List<Reclamation> toutes = new java.util.ArrayList<>();

    public void ajouter(Reclamation r) { toutes.add(r); } // ecriture

    public Reclamation findById(int id) { // lecture : leve si absent (partie 4)
        for (Reclamation r : toutes) {
            if (r.getId() == id) return r;
        }
        throw new ReclamationNotFoundException(id);
    }
}
```

Ensuite, un service qui contient les regles metier (les transitions `demarrerTraitement()`, partie 2) et qui **a besoin** du registre pour retrouver les reclamations :

```java
// ReclamationService : les regles metier. Il NE SAIT PAS stocker, il DELEGUE au registre.
public class ReclamationService {
    private final RegistreReclamations registre; // la dependance : « de quoi j'ai besoin »

    // Le service recoit son registre par le constructeur (partie 2 : encapsulation).
    public ReclamationService(RegistreReclamations registre) {
        this.registre = registre;
    }

    public void demarrer(int id) {
        Reclamation r = registre.findById(id); // on demande au registre...
        r.demarrerTraitement();                 // ...puis on applique la regle metier
    }
}
```

> 💡 **Pourquoi on passe du registre au service ?** Parce que le probleme du framework ne se voit que quand **deux objets collaborent**. Un objet seul n'a pas de cablage. Deux objets lies, si.

### 2.2 Le probleme : la « plomberie » des `new`

**Analogie** : imaginez un restaurant. Le cuisinier (le service) a besoin d'un garde-manger (le registre). Sans framework, c'est **vous** le plongeur qui, chaque matin, achetez le garde-manger, le montez, puis le donnez au cuisinier, dans le bon ordre. Avec 1 cuisinier ca va. Avec 20 cuisiniers, 10 garde-mangers, 5 comptables qui dependent des cuisiniers... votre matinee n'est que plomberie.

En code, cette plomberie s'appelle le **cablage** : ecrire les `new` dans le bon ordre :

```java
RegistreReclamations registre = new RegistreReclamations(); // 1) d'abord le garde-manger...
ReclamationService service = new ReclamationService(registre); // 2) ...puis le cuisinier qui en a besoin
service.demarrer(1);
```

Trois douleurs arrivent vite :

1. **L'ordre compte.** Si `ServiceB` a besoin de `ServiceA` qui a besoin du registre, vous devez creer les trois dans le bon ordre, au bon endroit (souvent un `Main` qui gonfle).
2. **Le changement casse tout.** Si demain le registre a lui-meme besoin d'une `Horloge` (partie 3, lecon 04 : `Clock` pour tester les delais), **chaque** `new RegistreReclamations()` du projet doit etre retouche.
3. **Les tests sont penibles.** Pour tester le service avec un faux registre (un mock, partie 9), il faut ruser, car le `new` est ecrit en dur quelque part.

> ⚠️ **On ne parle pas encore de Spring ici.** Retenez seulement : plus il y a d'objets lies, plus les `new` manuels coutent cher. C'est CE probleme que le framework va supprimer.
### 2.3 Bibliotheque vs framework : qui appelle qui ?

**Pourquoi on parle de ca maintenant ?** Parce que « framework » est le mot le plus mal compris du debutant : on croit que Spring Boot est « une grosse bibliotheque ». La difference change tout pour comprendre la suite.

- Une **bibliotheque** (JUnit, par exemple) : c'est **vous** qui l'appelez quand vous voulez. Votre `Main` reste le chef : `assertEquals(...)` quand VOUS decidez.
- Un **framework** (Spring Boot) : c'est **lui** le chef. Au demarrage, il cree les objets, les relie, demarre le serveur web, puis **il appelle votre code** quand une requete HTTP arrive. Vous ne pilotez plus le `main` de A a Z : vous **declarez** vos classes, il s'occupe du reste.

**Analogie** : une bibliotheque = des **ustensiles** que vous prenez quand vous cuisinez (vous restez le chef). Un framework = un **restaurant avec brigade** : vous ecrivez les recettes (vos classes metier), la brigade s'occupe de l'organisation (qui cuisine quoi, dans quel ordre) et vous appelle quand c'est votre tour.

> 💡 **Pourquoi on precise ca avant IoC ?** Parce que l'inversion de controle n'est que le nom savant de ce renversement : « ce n'est plus vous qui appelez, c'est le framework qui vous appelle ».

### 2.4 La solution : declarer, et laisser Spring creer (IoC en une phrase)

**Pourquoi on arrive ici ?** On a le probleme (plomberie, §2.2) et le renversement (framework chef, §2.3). Reste la solution concrete, en une phrase a retenir par coeur :

> **Inversion de controle (IoC)** : au lieu de creer vous-meme vos objets avec `new`, **vous declarez** vos classes et leurs dependances (via le constructeur), et **c'est Spring qui, au demarrage, cree chaque objet et le connecte aux autres automatiquement**.

Concretement, le code de la lecon 02 ressemblera a ceci (apercu, pas a recopier maintenant) :

```java
@Service // « Spring, gere cette classe comme un objet partage » (detail : lecon 04)
public class ReclamationService {
    private final RegistreReclamations registre;

    // Meme constructeur qu'avant. NOUVEAUTE : c'est SPRING qui l'appelle, pas vous.
    public ReclamationService(RegistreReclamations registre) {
        this.registre = registre;
    }
}
```

**Ce qui change pour vous :**

- AVANT : `new RegistreReclamations()` puis `new ReclamationService(registre)` ecrits a la main, dans le bon ordre.
- APRES : vous ecrivez les classes avec leurs constructeurs, Spring fait les `new` tout seul au demarrage, dans le bon ordre, une seule fois.

**Ce qui NE change pas :** votre logique metier (`demarrerTraitement()`, `findById` qui leve, regles de statut). Le framework ne touche jamais a ca : il ne fait que la plomberie.

### 2.5 Quand Spring Boot aide, et quand il est inutile

- ✅ **Utile** : application avec beaucoup d'objets lies + besoin reseau. Exemple : notre API SignalCUA (lecon 05 et suivantes) : controleur → service → registre, requetes HTTP, validation, erreurs JSON. C'est 80 % du metier backend : c'est pour ca que cette partie 7 est la plus longue du guide.
- ❌ **Inutile** : petit programme console autonome (parties 1-5), script de conversion, exercice d'algorithmique. Ajouter Spring Boot la serait comme appeler toute une brigade pour cuire un oeuf.

> 🔧 **Ce que Spring Boot ajoute a Spring** (une phrase, detail en lecon 02) : **Spring** = le systeme qui cree et relie les objets. **Spring Boot** = Spring + un demarrage pre-regle (serveur web inclus, configuration automatique) pour ne pas passer une journee a configurer avant d'ecrire la premiere ligne metier.

---

## 📖 Vocabulaire / Abreviations

> Ces mots reviennent dans TOUTE la partie 7. Chacun est defini ici une fois, puis rappele brievement quand on le revoit.

| Terme | Definition en une ligne |
|---|---|
| **Dependance** | Ce dont une classe a besoin pour travailler (ex. le service *depend* du registre). |
| **Cablage** | Le fait de relier les objets entre eux avec `new` (manuel) ou via Spring (automatique). |
| **Bibliotheque** | Code que VOUS appelez quand vous voulez (ex. JUnit). Vous restez le chef. |
| **Framework** | Code qui pilote l'application et APPELLE votre code (ex. Spring Boot). C'est lui le chef. |
| **Inversion de controle (IoC)** | Renversement : ce n'est plus vous qui creez les objets avec `new`, c'est le framework au demarrage. |
| **Injection de dependances (DI)** | La technique concrete par laquelle Spring fournit a un objet les objets dont il a besoin (detail : lecon 03). |
| **Bean** | Un objet cree et gere par Spring (au lieu d'etre cree par vos `new`). Detail : lecon 03. |
| **API REST** | Facade HTTP de l'application : des adresses (ex. `GET /reclamations`) qui renvoient du JSON. Detail : lecon 05. |

---

## 3. Exemples concrets

> **Raccord avec la section 2** : on vient de comprendre le probleme et la solution en theorie. Voici maintenant le meme probleme **en code executable**, pour le toucher du doigt : d'abord la douleur du `new` manuel, puis ce que Spring va changer (sans ecrire de Spring pour l'instant : juste constater).

### Exemple 1 — La douleur du cablage manuel (a executer tel quel)

Trois petites classes dans UN seul fichier pour l'experience (en vrai projet Maven, une classe = un fichier, partie 6) :
```java
// Fichier : SansFramework.java (experience : un seul fichier, javac direct).
import java.util.ArrayList; // la liste qui stocke (partie 3 : collections)
import java.util.List;

class ReclamationSimple { // version reduite : id + statut texte
    private final int id; // fixe a la creation (final, partie 2)
    private String statut; // modifiable : NOUVELLE puis EN_COURS
    ReclamationSimple(int id) { this.id = id; this.statut = "NOUVELLE"; }
    int getId() { return id; }
    void demarrerTraitement() { // regle metier : une seule transition
        if (!statut.equals("NOUVELLE")) throw new IllegalStateException("Deja demarree");
        statut = "EN_COURS";
    }
    public String toString() { return "Reclamation " + id + " : " + statut; }
}

class RegistreSimple { // le garde-manger : stocke en memoire
    private final List<ReclamationSimple> toutes = new ArrayList<>();
    void ajouter(ReclamationSimple r) { toutes.add(r); }
    ReclamationSimple findById(int id) { // cherche ou leve (partie 4 : pas de null)
        for (ReclamationSimple r : toutes) if (r.getId() == id) return r;
        throw new IllegalArgumentException("Introuvable : " + id);
    }
}

class ServiceSimple { // le cuisinier : a BESOIN du registre (dependance)
    private final RegistreSimple registre; // declaree en champ final...
    ServiceSimple(RegistreSimple registre) { this.registre = registre; } // ...recue par constructeur
    void demarrer(int id) { registre.findById(id).demarrerTraitement(); }
}

public class SansFramework {
    public static void main(String[] args) { // ICI : la plomberie manuelle
        RegistreSimple registre = new RegistreSimple(); // 1) le garde-manger d'abord...
        ServiceSimple service = new ServiceSimple(registre); // 2) ...puis le cuisinier
        registre.ajouter(new ReclamationSimple(1)); // une reclamation deposee
        service.demarrer(1); // le service la fait avancer
        System.out.println(registre.findById(1)); // affiche : Reclamation 1 : EN_COURS
    }
}
```

```bash
javac SansFramework.java # compile : JDK 21 suffit, rien a installer
java SansFramework # execute : affiche « Reclamation 1 : EN_COURS »
```

**Constat a retenir** : ca marche, mais chaque nouvel objet allonge le `main`. Imaginez 30 services : le `main` devient un annuaire de `new`. C'est ce que Spring prendra en charge des la lecon 03.

### Exemple 2 — Ce qui change avec Spring (lecture seule)

```java
// MEME service. Seul changement visible : une annotation + plus aucun new ailleurs.
@Service // « Spring, cree UNE fois cet objet et garde-le » (lecon 04 : stereotypes)
public class ReclamationService {
    private final RegistreReclamations registre; // meme dependance qu'avant
    public ReclamationService(RegistreReclamations registre) { // meme constructeur...
        this.registre = registre; // ...mais c'est SPRING qui l'appelle au demarrage
    }
}
// Dans TOUT le projet : ZERO « new ReclamationService(...) » ecrit a la main.
```

---

## 4. Bonnes pratiques modernes (2025-2026)

- **Penser « declaration, pas creation »** : votre travail = classes propres + constructeurs explicites. Travail du framework = instancier. Jamais de `new` d'un bean dans un `main` Spring.
- **Constructeur = contrat lisible** : dependances visibles dans la signature. Tests faciles en partie 9 (faux registre passe au constructeur, sans Spring).
- **Boot 3.x + Java 21 + `jakarta.*`** : tutoriel pre-2023 avec `javax.*` = perime sur ce point, transposez ou changez de source.
- **Pas de Spring trop tot** : console, script, algo → Java pur + Maven. Spring se justifie quand objets lies + reseau.

---

## 5. Pieges a eviter

### Piege 1 — « Spring = grosse bibliotheque qu'on appelle »

```text
# MAUVAIS modele mental : « j'appelle Spring comme JUnit »
MonControleur c = new MonControleur(); // MANUEL : Spring ne gere rien ici
```

```java
// BON : on DECLARE, Spring CREE et APPELLE. Aucun new manuel d'un bean.
@RestController // « Spring, appelle-moi quand une requete HTTP arrive » (lecon 05)
public class ReclamationController {
    private final ReclamationService service; // declaree...
    public ReclamationController(ReclamationService service) { this.service = service; } // ...Spring l'apporte
}
```

**Pourquoi** : avec le modele « bibliotheque », on ecrit des `new` partout et on perd le benefice (ordre fragile, tests durs). Le framework ne sert que si on lui confie la creation.

### Piege 2 — « Ajouter Spring Boot a tout, au cas ou »

```text
# MAUVAIS : 12 starters coches « au cas ou » -> demarrage lent, tout melange.
# BON : UN starter (web) pour commencer (lecon 02). On ajoute quand la lecon l'exige.
```

**Pourquoi** : meme regle qu'en partie 6 : chaque dependance alourdit le demarrage et la surface a comprendre. Le minimal d'abord.

### Piege 3 — « Le framework gere ma logique metier »

```text
# MAUVAIS : « Spring validera mes transitions de statut tout seul »
# BON : vos regles (demarrerTraitement, findById qui leve) restent VOTRE code, teste en JUnit pur.
# Spring ne fait que : creer, relier, exposer en HTTP, traduire les erreurs en JSON.
```

**Pourquoi** : confondre plomberie et metier mene a mettre des regles dans des annotations au lieu des classes. Le metier reste dans le service, testable sans serveur.

---

## Checklist de validation

Avant de passer a l'exercice, verifiez que vous savez faire **chacun** de ces points :

- [ ] Expliquer : « une bibliotheque, je l'appelle ; un framework, il m'appelle », avec un exemple chacun.
- [ ] Definir « dependance » sur l'exemple service -> registre, sans hesiter.
- [ ] Citer les 3 douleurs du `new` manuel (ordre, changement qui casse tout, tests penibles).
- [ ] Dire la phrase IoC : « je declare, Spring cree et connecte au demarrage ».
- [ ] Decider : « ce programme merite-t-il Spring ? » (console seule : non ; API + objets lies : oui).

---

## Fil rouge — ou en est SignalCUA ?

SignalCUA ne change pas encore de code : il **change de regard**. Le `Main` Maven de la partie 6 cable a la main (`new RegistreReclamations()` puis `new ReclamationService(...)`). L'exercice rejoue ce cablage en reduit, pour sentir la douleur avant le remede. Des la lecon 02, ce meme projet naitra en version Spring Boot (`signalcua-spring-boot`), et des la lecon 03 Spring fera les `new` a votre place.

---

➡️ **Prochaine etape** : l'**exercice 01** — ajouter un guichet et recabler SignalCUA a la main pour mesurer le cout (2 lignes de sortie exacte), puis la **lecon 02 — Premier projet Spring Boot** : generer le projet Initializr, le demarrer (`./mvnw spring-boot:run`), voir `Tomcat started on port 8080`. Le « pourquoi » est acquis ; place au « comment demarrer ».

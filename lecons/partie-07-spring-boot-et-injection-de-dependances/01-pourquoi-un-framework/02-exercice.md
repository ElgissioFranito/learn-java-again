# Exercice 01 — Recabler SignalCUA a la main pour sentir le cout du `new` manuel

> 🧭 **Comment ce fichier s'articule** : la lecon (`01-lecon.md`) a explique le probleme (plomberie des `new`), le renversement (bibliotheque vs framework) et la solution (IoC : je declare, Spring cree). Ici, vous **rejouez** le cablage manuel en reduit, pour **mesurer** ce qu'il coute des qu'un 3e objet s'en mele. La correction (`03-correction.md`) montre la solution complete et la sortie reellement obtenue. Aucun Spring dans cet exercice : c'est volontaire, le remede arrive en lecon 03.

---

## 🎯 Objectif de l'exercice

Obtenir un programme `javac` + `java` qui affiche les 2 lignes attendues, avec **3 objets cables a la main** (registre → service → guichet), puis repondre en 3 phrases : ou est la douleur quand on ajoute un 4e objet.

**Socle de depart rappele** : reprenez l'exemple `SansFramework.java` de la lecon §3 exemple 1 (classes `ReclamationSimple`, `RegistreSimple`, `ServiceSimple` + `main` a 2 objets). Vous y ajoutez un 3e objet : un **guichet d'accueil** (`GuichetSimple`) qui recoit les citoyens et depose leur reclamation via le service. Chaine finale : `GuichetSimple` → `ServiceSimple` → `RegistreSimple` (3 objets, 4 classes metier avec `ReclamationSimple`).

## 📋 Enonce

### Etape 1 — Reprendre la base (5 min)

1. Creez un dossier de travail (chemin relatif, depuis la racine de votre espace) : `atelier-p07-01/`.
2. Recopiez-y le fichier `SansFramework.java` de la lecon (le bloc §3 exemple 1 : 42 lignes avec les fences Markdown, soit 40 lignes de code de `import java.util.ArrayList;` a l'accolade fermante de `SansFramework`).
3. Verifiez : `javac SansFramework.java` puis `java SansFramework` affiche `Reclamation 1 : EN_COURS` (1 seule ligne a ce stade).

### Etape 2 — Ajouter le guichet (15 min)

1. Ajoutez une 5e classe dans le MEME fichier, avant `public class SansFramework` (le fichier contient deja 4 classes : `ReclamationSimple`, `RegistreSimple`, `ServiceSimple`, `SansFramework`) :

```java
// GuichetSimple : l'accueil. Il a BESOIN du service (nouvelle dependance).
// Consigne : champ final + constructeur qui recoit le ServiceSimple + methode deposer(id, ...).
// La methode deposer cree une ReclamationSimple et l'ajoute via... le service ou le registre ?
// Reflechissez : qui sait stocker ? (reponse attendue dans votre code en commentaire).
```

2. Modifiez le `main` pour cabler les 3 objets **dans le bon ordre** (registre → service → guichet), deposer 2 reclamations via le guichet, demarrer la n°1 via le service, afficher les 2.
3. Sortie attendue exacte :

```text
Reclamation 1 : EN_COURS
Reclamation 2 : NOUVELLE
```

### Etape 3 — Constater (5 min, sur papier)

Repondez en 3 phrases maximum **par question** dans un commentaire en haut du fichier :

1. Si le registre avait lui-meme besoin d'une `Horloge`, combien de `new` faudrait-il retoucher ?
2. Pour tester le guichet avec un faux service, que faudrait-il changer ?
3. A partir de combien d'objets ce cablage manuel devient-il ingerable, selon vous ?

## ✅ Criteres de reussite

- [ ] `java SansFramework` affiche exactement les 2 lignes attendues (aucune ligne en plus : ni `Reclamation 1 : EN_COURS` de l'etape 1 — remplacee par les 2 nouvelles lignes — ni message parasite).
- [ ] Zero `null` renvoye : absent = exception (regle partie 4).
- [ ] Le `main` montre l'ordre de creation : registre, puis service, puis guichet.
- [ ] Les 3 reponses figurent en commentaire en haut du fichier (3 phrases max par question, comme dans la correction : 6 lignes de commentaire).
- [ ] Aucune dependance Spring : `import` limites a `java.util` (`ArrayList`, `List`).

## 💡 Indications (lisez seulement si bloque)

- Le guichet ne stocke rien lui-meme : il demande au registre d'ajouter. Mais il recoit le **service** au constructeur (chaine complete guichet → service → registre) : ajoutez au service une methode `deposer(ReclamationSimple r)` qui delegue au registre.
- Ordre du `main` : `new RegistreSimple()` → `new ServiceSimple(registre)` → `new GuichetSimple(service)`.
- `Horloge` : rappelez-vous partie 3 lecon 04 (`Clock` injectee pour tester les delais) — 1 dependance de plus = TOUS les `new` retouches.

# Correction detaillee — Exercice 01 « Recabler SignalCUA a la main »

> 🧭 **Comment ce fichier s'articule** : vous venez de tenter `02-exercice.md` (reprendre `SansFramework.java`, ajouter un guichet, mesurer le cout). Voici la solution complete, les choix expliques, et la **preuve par execution**.

## Correction pas a pas

### Le fichier complet (55 lignes de code Java + 6 lignes de reponses en commentaire)

```java
// REPONSES (etape 3) :
// 1) Avec une Horloge dans le registre, il faudrait retoucher CHAQUE « new RegistreSimple() »
//    du projet (ici 1 seul, mais N en vrai projet) : le changement se propage partout.
// 2) Pour tester le guichet avec un faux service, il faudrait une interface du service
//    (partie 2, lecon 03) ou passer le faux par le constructeur a la main : possible mais manuel.
// 3) Des ~5-6 objets lies, l'ordre des new devient la preoccupation principale : c'est le signal Spring.
import java.util.ArrayList; // la liste qui stocke (partie 3)
import java.util.List;

class ReclamationSimple { // inchangee (lecon §3)
    private final int id;
    private String statut;
    ReclamationSimple(int id) { this.id = id; this.statut = "NOUVELLE"; }
    int getId() { return id; }
    void demarrerTraitement() {
        if (!statut.equals("NOUVELLE")) throw new IllegalStateException("Deja demarree");
        statut = "EN_COURS";
    }
    public String toString() { return "Reclamation " + id + " : " + statut; }
}

class RegistreSimple { // inchange : le seul qui sait stocker
    private final List<ReclamationSimple> toutes = new ArrayList<>();
    void ajouter(ReclamationSimple r) { toutes.add(r); }
    ReclamationSimple findById(int id) {
        for (ReclamationSimple r : toutes) if (r.getId() == id) return r;
        throw new IllegalArgumentException("Introuvable : " + id);
    }
}

class ServiceSimple { // + une methode deposer qui delegue (le guichet ne touche pas au registre)
    private final RegistreSimple registre;
    ServiceSimple(RegistreSimple registre) { this.registre = registre; }
    void deposer(ReclamationSimple r) { registre.ajouter(r); } // delegation, pas de logique
    void demarrer(int id) { registre.findById(id).demarrerTraitement(); }
}

class GuichetSimple { // NOUVEAU : l'accueil, depend du service (chaine complete)
    private final ServiceSimple service; // dependance declaree en final...
    GuichetSimple(ServiceSimple service) { this.service = service; } // ...recue par constructeur
    void deposer(int id) { service.deposer(new ReclamationSimple(id)); } // cree + delegue
}

public class SansFramework {
    public static void main(String[] args) { // plomberie : l'ordre compte
        RegistreSimple registre = new RegistreSimple(); // 1) d'abord le stockage...
        ServiceSimple service = new ServiceSimple(registre); // 2) ...puis le metier...
        GuichetSimple guichet = new GuichetSimple(service); // 3) ...puis l'accueil
        guichet.deposer(1); // via la chaine guichet -> service -> registre
        guichet.deposer(2);
        service.demarrer(1); // la n°1 avance, la n°2 reste NOUVELLE
        System.out.println(registre.findById(1)); // Reclamation 1 : EN_COURS
        System.out.println(registre.findById(2)); // Reclamation 2 : NOUVELLE
    }
}
```

### Choix techniques expliques

- **Le guichet recoit le service, pas le registre** : pour garder la chaine `Guichet → Service → Registre` et montrer qu'une dependance de plus = un `new` de plus dans le bon ordre. Si le guichet recevait le registre direct, on perdrait la demonstration.
- **`deposer` dans le service delegue sans logique** : volontaire. Le service reste le seul interlocuteur metier ; le registre reste le seul stockage. Separation des roles (sera la couche service, lecon 08).
- **Pas d'interface ici** : a 3 classes, l'interface serait du luxe. La question 2 de l'etape 3 la fait deviner : c'est en lecon 08 + partie 9 (mocks) qu'elle deviendra necessaire.

## Verification par execution

Sortie **reellement obtenue** avec JDK 21 (`javac` + `java`, dossier temporaire, supprime apres preuve) :

```text
Reclamation 1 : EN_COURS
Reclamation 2 : NOUVELLE
```

Commandes rejouees : `javac SansFramework.java`, `java SansFramework`. Aucune autre ligne. Si vous avez une 3e ligne ou un ordre different, relisez votre `main` : l'ordre d'affichage suit l'ordre des `println`.

## Erreurs frequentes et comment les reconnaitre

- `cannot find symbol: GuichetSimple` : classe ajoutee apres le `main` ou accolade oubliee. **Remede** : la classe avant `public class SansFramework`, accolades equilibrees.
- `NullPointerException` sur `service` : `new GuichetSimple(null)` ou champ non assigne. **Remede** : constructeur qui assigne (`this.service = service`).
- Ligne manquante : `deposer` oublie d'appeler `registre.ajouter`. **Remede** : delegation en 2 temps (guichet → service → registre), verifier chaque maillon.
- Envie d'ajouter Spring « pour aller plus vite » : **non**, c'est le sujet des lecons 02-03. Ici on mesure la douleur, on ne la soigne pas encore.

## Checklist de validation

- [ ] Le programme affiche exactement les 2 lignes, dans l'ordre.
- [ ] J'explique pourquoi le guichet depend du service (chaine complete) plutot que du registre.
- [ ] Je cite de memoire les 3 reponses (Horloge = N retouches, test = interface ou passage manuel, seuil ~5-6 objets).
- [ ] Je sais dire la phrase IoC en pointant le `main` : « ces 3 new, Spring les fera a ma place ».

## Conseils pour progresser

- Cassez volontairement : inversez 2 `new` dans le `main`, lisez l'erreur du compilateur. On retient mieux l'ordre quand on l'a viole une fois.
- Comptez les `new` de votre vrai projet parties 1-5 : si vous depassez 10, vous venez de prouver a vous-meme que la partie 7 est necessaire.
- Ne passez a la lecon 02 que si la phrase « je declare, Spring cree » est devenue un reflexe. Sinon, relisez le §2.4 de la lecon.

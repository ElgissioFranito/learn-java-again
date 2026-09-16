# Correction 03 — Le registre des réclamations

> 🧭 **Articulation** : solution pas à pas de `02-exercice.md`. Comparez surtout vos étapes 3 et 4 : c'est là que l'immuabilité des `String` et le `StringBuilder` jouent. La checklist finale reprend celle de la leçon.

## Étape 1 — Imports et données

```java
import java.util.ArrayList; // ArrayList vit dans le paquet java.util

public class Registre {
    public static void main(String[] args) {
        ArrayList<String> signalements = new ArrayList<>();
        signalements.add("  Voirie_Nid de poule_Route Dakar  ");
        signalements.add("Eclairage_Lampadaire HS_Rue 12 ");
        signalements.add("  Proprete_Tas d'ordures_Marche Medina");
    }
}
```

## Étape 2 — Afficher numéroté

```java
// Le for CLASSIQUE donne l'index, donc le numéro d'ordre (index + 1
// pour commencer l'affichage à 1, l'humain comptant à partir de 1)
for (int i = 0; i < signalements.size(); i++) {
    System.out.println((i + 1) + ". " + signalements.get(i));
}
// Un for-each marche aussi, mais il faut gérer le compteur à la main :
// int num = 1; for (String s : signalements) { ... num++; }
```

## Étape 3 — Nettoyer et découper (le cœur de l'exercice)

```java
for (int i = 0; i < signalements.size(); i++) {
    // 1. trim() RENVOIE une nouvelle String : il faut la RÉCUPÉRER
    String propre = signalements.get(i).trim();

    // 2. split("_") renvoie un tableau de morceaux (le séparateur choisi
    //    dans les données est le soulignement, car les objets contiennent
    //    des espaces, et le tiret causerait des soucis avec les mots composés)
    String[] morceaux = propre.split("_");

    // 3. morceaux[0] = catégorie, morceaux[1] = objet, morceaux[2] = lieu
    System.out.printf("#%d  Categorie : %-10s | Objet : %-20s | Lieu : %s%n",
            i + 1, morceaux[0], morceaux[1], morceaux[2]);
}
// Note : "%-10s" aligne à gauche sur 10 caractères (fiches lisibles) ;
// si ce format vous semble obscur, la version simple marche aussi :
// System.out.println("#" + (i + 1) + " Categorie : " + morceaux[0] + " ...");
```

**Pourquoi ne pas modifier `signalements` ?** On aurait pu faire `signalements.set(i, propre)`, mais l'exercice demandait de ne pas toucher à l'origine : **l'immuabilité des `String`** nous y oblige de toute façon — `trim()` ne modifie jamais l'objet stocké, il fabrique une nouvelle chaîne qu'on stocke dans une variable locale.

## Étape 4 — Le rapport avec StringBuilder

```java
int nbVoirie = 0, nbEclairage = 0, nbProprete = 0;

// Un seul passage : on compte ET on ne construit pas encore
for (String s : signalements) {
    String categorie = s.trim().split("_")[0]; // nettoyage + découpage à la chaîne
    switch (categorie) {
        case "Voirie"    -> nbVoirie++;
        case "Eclairage" -> nbEclairage++;
        case "Proprete"  -> nbProprete++;
        default -> System.out.println("Categorie inconnue : " + categorie);
    }
}

// PUIS on construit le rapport en une seule gravure
StringBuilder rapport = new StringBuilder();
rapport.append("RAPPORT SIGNALCUA\n");
rapport.append("-----------------\n");
rapport.append("Voirie : ").append(nbVoirie).append(" signalement(s)\n");
rapport.append("Eclairage : ").append(nbEclairage).append(" signalement(s)\n");
rapport.append("Proprete : ").append(nbProprete).append(" signalement(s)\n");
rapport.append("Total : ").append(signalements.size()).append("\n");
System.out.println(rapport);
```

**Points de vigilance :**
- `s.trim().split("_")[0]` : on peut **enchaîner** les appels de gauche à droite — trim d'abord (sinon la première catégorie garderait ses espaces : `" Voirie"` ≠ `"Voirie"`), puis split, puis `[0]` pour le premier morceau.
- Le `switch` ici compare des **valeurs fixes** : c'est LE bon outil (rappel leçon 02). Les tranches de nombres, elles, voulaient des `if`.
- `signalements.size()` pour le total : tous les signalements sont comptés, quelle que soit leur catégorie.

## Étape 5 — Comparaison insensible à la casse

```java
String statut1 = "nouvelle";
String statut2 = "NOUVELLE";

System.out.println(statut1.equals(statut2));             // false : majuscule != minuscule
System.out.println(statut1.equalsIgnoreCase(statut2));   // true  : la casse est ignorée

// Commentaire attendu : equals compare le contenu EXACT (une majuscule change tout,
// "N" et "n" sont des caractères différents) ; equalsIgnoreCase compare le contenu
// en ignorant majuscules/minuscules — parfait pour une saisie humaine ("nouvelle",
// "NOUVELLE", "Nouvelle" doivent désigner le même statut).
```

## Checklist de validation (récapitulatif)

- [ ] Je crée et parcours un tableau, et je sais quand lui préférer une `ArrayList`.
- [ ] J'utilise `.add()`, `.get()`, `.size()`, `.contains()` sur une `ArrayList`.
- [ ] Je nettoie une saisie avec `.trim()` et je RÉCUPÈRE le résultat (immuabilité !).
- [ ] Je découpe avec `.split()` et j'accède aux morceaux par index.
- [ ] Je construis un rapport avec `StringBuilder` et `.append()` (pas de `+` en boucle).
- [ ] Je compare du texte avec `.equals()` / `.equalsIgnoreCase()`.
- [ ] J'enchaîne les appels (`s.trim().split("_")[0]`) en comprenant l'ordre.

## 💡 Conseils

1. **Remarquez la douleur de l'étape 3** : trois morceaux à extraire à la main, par index (`morceaux[0]`, `morceaux[1]`…). Si un jour le format change, tout casse. C'est exactement ce que la **classe `Reclamation`** (leçon suivante) va fiabiliser en donnant un nom à chaque donnée.
2. **Gardez ce fichier `Registre.java`** : la leçon 04 le fera évoluer — les String brutes deviendront de vrais objets `Reclamation`.
3. Si votre fiche affiche ` Voirie` avec un espace devant, c'est que vous avez splitté avant de trimmer : l'ordre des appels compte.

➡️ **Prochaine étape** : leçon 04 — la classe `Reclamation` : regrouper données et comportements dans un modèle unique, l'étape 1 du fil rouge SignalCUA.



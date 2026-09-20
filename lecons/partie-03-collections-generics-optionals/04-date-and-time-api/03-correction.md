# Correction détaillée — Exercice 04 « Le SLA des réclamations »

> 🧭 **Comment ce fichier s'articule** : vous venez de tenter `02-exercice.md`. Voici la solution complète, les choix expliqués, la **sortie réellement obtenue**, les erreurs fréquentes, la checklist et des conseils. Comme toujours : si votre programme produit la même sortie, c'est réussi.

## Correction pas à pas

### Étape 1 — `Priorite` : le délai devient un `Duration`

```java
// Fichier : Priorite.java
import java.time.Duration;

public enum Priorite {

    BASSE(168),     // 1 semaine
    NORMALE(48),    // 2 jours
    URGENTE(4);     // 4 heures

    private final int delaiHeuresMax;

    private Priorite(int delaiHeuresMax) {
        this.delaiHeuresMax = delaiHeuresMax;
    }

    public int getDelaiHeuresMax() { return delaiHeuresMax; }

    public Duration delaiMax() {            // la meme information, en objet Duration
        return Duration.ofHours(delaiHeuresMax);
    }
}
```

**Pourquoi exposer un `Duration` plutôt que l'`int` seul ?** Parce que l'appelant n'a plus à **savoir** que l'unité est l'heure : `dateDeclaration.plus(priorite.delaiMax())` se lit comme une phrase. Si demain un `enum` de priorité doit exprimer « 2 jours ouvrés », il construira un `Duration` différent **sans changer** le code qui l'utilise. C'est l'encapsulation (partie 2) appliquée à une durée.

> ℹ️ Le `getDelaiHeuresMax()` de la partie 2 **reste** : il sert à l'affichage et au `Comparator` de la `PriorityQueue` (leçon 01). On **ajoute** une vue, on ne supprime rien — un principe sûr en refactoring.

### Étape 2 — `Reclamation` : la date de déclaration et l'échéance calculée

```java
import java.time.LocalDateTime;

// Fichier : Reclamation.java  (la version partie 3, avec la date de declaration)
public class Reclamation {

    private final int id;
    private final String description;
    private final String quartier;
    private final Priorite priorite;
    private final LocalDateTime dateDeclaration;      // NOUVEAU : quand la reclamation est arrivee
    private StatutReclamation statut;

    public Reclamation(int id, String description, String quartier, Priorite priorite,
                       LocalDateTime dateDeclaration) {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("La description est obligatoire");
        }
        if (dateDeclaration == null) {
            throw new IllegalArgumentException("La date de declaration est obligatoire");
        }
        this.id = id;
        this.description = description;
        this.quartier = quartier;
        this.priorite = priorite;
        this.dateDeclaration = dateDeclaration;
        this.statut = StatutReclamation.NOUVELLE;
    }

    // L'echeance n'est PAS stockee : elle se CALCULE a partir de la declaration et du delai
    public LocalDateTime dateEcheance() {
        return dateDeclaration.plus(priorite.delaiMax());     // plus(Duration) = ajout de duree
    }

    public void demarrerTraitement() {
        if (statut != StatutReclamation.NOUVELLE) {
            throw new IllegalStateException("Impossible de demarrer une reclamation " + statut);
        }
        this.statut = StatutReclamation.EN_COURS;
    }

    public void marquerResolue() {
        if (statut != StatutReclamation.EN_COURS) {
            throw new IllegalStateException("Impossible de resoudre une reclamation " + statut);
        }
        this.statut = StatutReclamation.RESOLUE;
    }

    public boolean estResolue() {
        return statut == StatutReclamation.RESOLUE;
    }
    // ... getLigneAffichage(), getBadge(), getters ...
}
```

**Les points à retenir :**

- **Deux validations, deux raisons différentes** : une description vide serait une donnée inutilisable, une date absente une donnée incohérente. Les deux sont des **erreurs de programmation** → exception immédiate (leçon 03, section 2.7 : `Optional` n'a rien à faire ici).
- **Aucun champ `dateEcheance`** : elle est **dérivée**. Si la priorité change (ou si la règle du SLA change), l'échéance suit automatiquement — impossible d'avoir deux sources de vérité contradictoires.
- **`estResolue()` est nouvelle** (la version de la leçon 01 ne l'avait pas) : elle rend la règle de SLA lisible côté service, sans que le service ait à connaître le `switch` du `getBadge()`.

### Étape 3 — `ServiceDelais` : une horloge, aucun appel direct à `now()`

```java
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// Le service qui calcule les delais. L'horloge est INJECTEE : c'est ce qui rend le code testable.
public class ServiceDelais {

    // Un formateur CONSTANT (jamais recree a chaque appel) et lisible par un humain francais
    public static final DateTimeFormatter FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final Clock horloge;

    public ServiceDelais(Clock horloge) {
        this.horloge = horloge;
    }

    public LocalDateTime maintenant() {
        return LocalDateTime.now(horloge);       // l'heure vient de l'horloge fournie
    }

    public boolean estEnRetard(Reclamation r) {
        return !r.estResolue() && maintenant().isAfter(r.dateEcheance());
    }

    // Ecart signe : positif = temps restant, negatif = retard
    public Duration ecart(Reclamation r) {
        return Duration.between(maintenant(), r.dateEcheance());
    }

    public String resumeSla(Reclamation r) {
        Duration ecart = ecart(r);
        if (!ecart.isNegative()) {
            return "temps restant : " + ecart.toHours() + " h";
        }
        return "RETARD de " + Math.abs(ecart.toHours()) + " h";
    }

    public String ligneSla(Reclamation r) {
        return "#" + r.getId()
                + " declaree le " + r.getDateDeclaration().format(FORMAT)
                + " | echeance " + r.dateEcheance().format(FORMAT)
                + " | " + resumeSla(r);
    }
}
```

**Analyse de chaque décision :**

| Ligne | Décision | Justification |
|---|---|---|
| `FORMAT` `static final` | Un seul formateur pour toute l'application | `DateTimeFormatter` est immuable et *thread-safe* : le partager est **recommandé** (l'inverse de `SimpleDateFormat`) |
| `Clock horloge` au constructeur | Injection de dépendance | Sans elle, impossible de tester « en retard à 13:00 » (partie 9) |
| `maintenant()` | Un **unique** point d'accès au temps | Deux appels à `now()` dans la même méthode peuvent donner deux instants différents (piège 7) |
| `!r.estResolue() && isAfter(...)` | La règle métier complète | Une réclamation résolue sort du SLA — décision **métier**, donc écrite noir sur blanc |
| `ecart()` renvoie un `Duration` | Renvoyer la **donnée**, pas le texte | L'appelant peut afficher, trier ou agréger ; le formatage reste dans `resumeSla` |
| `Math.abs(ecart.toHours())` | Un seul signe visible | Sans `abs`, on afficherait « RETARD de -73 h » |
| `toHours()` dans le résumé | Troncature assumée | Pour un SLA exprimé en heures, l'arrondi inférieur est le comportement attendu |

> 💡 **Pourquoi ne pas utiliser `.toMinutes()` dans `resumeSla` ?** Parce que le **métier** raisonne en heures ici (4 h, 48 h, 168 h). Afficher « RETARD de 4380 minutes » serait exact mais inutile. C'est un choix de présentation — et le fait que `ecart()` renvoie un `Duration` laisse le choix à l'appelant.

### Étape 4 — La démonstration avec une horloge figée (extrait)

```java
        // ============ 3. L'horloge injectee : une date FIXE pour une demonstration reproductible ============
        Clock horlogeFigee = Clock.fixed(
                LocalDateTime.of(2026, 9, 20, 10, 0).atZone(ZoneId.of("Africa/Dakar")).toInstant(),
                ZoneId.of("Africa/Dakar"));
        ServiceDelais service = new ServiceDelais(horlogeFigee);
        System.out.println("3) maintenant (horloge figee) -> "
                + service.maintenant().format(ServiceDelais.FORMAT));

        // ============ 4. Les reclamations et leur echeance calculee ============
        Reclamation urgente = new Reclamation(1, "Cable arrache", "Medina", Priorite.URGENTE,
                LocalDateTime.of(2026, 9, 20, 8, 0));
        Reclamation normale = new Reclamation(2, "Nid de poule", "Plateau", Priorite.NORMALE,
                LocalDateTime.of(2026, 9, 20, 9, 0));
        Reclamation ancienne = new Reclamation(3, "Poubelles", "Fann", Priorite.BASSE,
                LocalDateTime.of(2026, 9, 10, 9, 0));
        System.out.println("4) " + service.ligneSla(urgente));
        System.out.println("   " + service.ligneSla(normale));
        System.out.println("   " + service.ligneSla(ancienne));

        // ============ 5. En retard ou pas : isAfter / isBefore ============
        System.out.println("5) urgente en retard ? " + service.estEnRetard(urgente)
                + " (echeance 12:00 > maintenant 10:00)");
        System.out.println("   ancienne en retard ? " + service.estEnRetard(ancienne)
                + " (echeance depassee depuis le 17/09)");
        ancienne.demarrerTraitement();
        ancienne.marquerResolue();
        System.out.println("   ancienne resolue -> encore en retard ? " + service.estEnRetard(ancienne)
                + " (une reclamation resolue sort du SLA)");

        // Une autre horloge : 3 heures plus tard, l'urgente est desormais en retard
        ServiceDelais servicePlusTard = new ServiceDelais(Clock.fixed(
                LocalDateTime.of(2026, 9, 20, 13, 0).atZone(ZoneId.of("Africa/Dakar")).toInstant(),
                ZoneId.of("Africa/Dakar")));
        System.out.println("   meme urgente a 13:00 -> en retard ? "
                + servicePlusTard.estEnRetard(urgente)
                + " | " + servicePlusTard.resumeSla(urgente));
```

**Le point central** : `service` et `servicePlusTard` partagent le **même code** et les **mêmes objets** ; seule l'horloge diffère. C'est exactement la manière dont vous écrirez ce test en partie 9, avec un `Clock.fixed` par scénario.

### Étape 5 — `Period` contre `Duration` (et la troncature)

```java
        // ============ 6. Duration : un CHRONOMETRE (heures, minutes, secondes) ============
        LocalDateTime debut = LocalDateTime.of(2026, 9, 20, 8, 0);
        LocalDateTime fin = LocalDateTime.of(2026, 9, 21, 10, 30);
        Duration duree = Duration.between(debut, fin);
        System.out.println("6) Duration.between -> " + duree
                + " | toHours()=" + duree.toHours()
                + " | toMinutes()=" + duree.toMinutes()
                + " | toDays()=" + duree.toDays() + " (TRONQUE : 26 h = 1 jour complet)");
        System.out.println("   plus(Duration.ofHours(4)) -> " + debut.plus(Duration.ofHours(4)));

        // ============ 7. Period : un CALENDRIER (annees, mois, jours) ============
        LocalDate naissance = LocalDate.of(2020, 5, 15);
        Period anciennete = Period.between(naissance, LocalDate.of(2026, 9, 20));
        System.out.println("7) Period.between -> " + anciennete + " soit "
                + anciennete.getYears() + " ans " + anciennete.getMonths() + " mois et "
                + anciennete.getDays() + " jours");
        System.out.println("   plus(Period.ofMonths(3)) -> " + naissance.plus(Period.ofMonths(3)));

        // ============ 8. ChronoUnit : compter une unite precise ============
        System.out.println("8) ChronoUnit.DAYS.between(2026-01-01, 2026-09-20) = "
                + ChronoUnit.DAYS.between(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 9, 20)));
        System.out.println("   ChronoUnit.HOURS.between(debut, fin) = "
                + ChronoUnit.HOURS.between(debut, fin));
```

**Réponse à la question de l'étape 5.3** : `duree.toDays()` vaut **1** parce que la méthode **tronque** la partie entière : 26 h 30 min contient **1** jour complet (24 h) et il reste 2 h 30 min qui sont **ignorées**. Pour un comptage exact, on utilise `ChronoUnit.DAYS.between(debut, fin)` (qui compte les jours **entiers franchis**) ou l'unité réellement utile (`HOURS`, `MINUTES`).

### Étape 6 — Formater, relire, échouer proprement

```java
        // ============ 9. Formater et relire (parse) ============
        System.out.println("9) ISO_LOCAL_DATE -> "
                + jour.format(DateTimeFormatter.ISO_LOCAL_DATE));
        System.out.println("   notre format     -> " + moment.format(ServiceDelais.FORMAT));
        LocalDateTime relue = LocalDateTime.parse("20/09/2026 14:30", ServiceDelais.FORMAT);
        System.out.println("   parse(\"20/09/2026 14:30\") -> " + relue
                + " | identique ? " + relue.equals(moment));
        try {
            LocalDateTime.parse("2026-13-45 14:30", ServiceDelais.FORMAT);
        } catch (DateTimeParseException e) {
            System.out.println("   PIEGE : texte invalide -> " + e.getClass().getSimpleName());
        }
```

**Pourquoi ce `try/catch` est légitime ici** (alors qu'ailleurs on évite d'attraper) : l'erreur vient **de l'extérieur** (un texte fourni), elle est **attendue**, et le programme décide explicitement de la signaler. En partie 4, ce cas deviendra une **exception métier** (`DonneeInvalideException`) pour produire une réponse d'API claire plutôt qu'un message technique.

### Étape 7 (bonus) — Changement d'heure et fuseaux

```java
        // ============ 10. Period contre Duration : le changement d'heure ============
        ZoneId paris = ZoneId.of("Europe/Paris");
        ZonedDateTime avantChangement = ZonedDateTime.of(2025, 3, 30, 1, 30, 0, 0, paris);
        ZonedDateTime avecPeriod = avantChangement.plus(Period.ofDays(1));
        ZonedDateTime avecDuration = avantChangement.plus(Duration.ofDays(1));
        System.out.println("10) depart              -> " + avantChangement);
        System.out.println("    plus(Period.ofDays(1))   -> " + avecPeriod
                + "  (meme heure au mur, 23 h reelles : on a avance les montres)");
        System.out.println("    plus(Duration.ofDays(1)) -> " + avecDuration
                + "  (24 h reelles, donc une heure de plus au mur)");

        // ============ 11. Un MEME instant, deux fuseaux ============
        Instant instant = horlogeFigee.instant();
        System.out.println("11) Instant (UTC)          -> " + instant);
        System.out.println("    vu de Dakar (UTC+0)    -> " + instant.atZone(ZoneId.of("Africa/Dakar")));
        System.out.println("    vu de Paris (UTC+2)    -> " + instant.atZone(paris));
```

**Explication en une phrase (celle à retenir)** : `Period` raisonne en **calendrier** (même heure locale, donc 23 h réelles cette nuit-là à cause du passage à l'heure d'été), tandis que `Duration` raisonne en **temps réel écoulé** (24 h, donc une heure de plus à la même horloge).

## Vérification par exécution

```bash
javac -encoding UTF-8 *.java      # StatutReclamation, Priorite, Reclamation, ServiceDelais, MainDates
java MainDates
```

**Résultat obtenu (Java 21.0.7), compilation sans erreur :**

```text
1) LocalDate      -> 2026-09-20
   LocalTime      -> 14:30
   LocalDateTime  -> 2026-09-20T14:30
   PIEGE : LocalDate.of(2026, 2, 30) -> Invalid date 'FEBRUARY 30'
2) 2026-01-31 + 1 jour = 2026-02-01 (et finMois vaut toujours 2026-01-31)
   2026-01-31 plus 1 mois = 2026-02-28 (le mois se raccourcit a la fin du mois)
3) maintenant (horloge figee) -> 20/09/2026 10:00
4) #1 declaree le 20/09/2026 08:00 | echeance 20/09/2026 12:00 | temps restant : 2 h
   #2 declaree le 20/09/2026 09:00 | echeance 22/09/2026 09:00 | temps restant : 47 h
   #3 declaree le 10/09/2026 09:00 | echeance 17/09/2026 09:00 | RETARD de 73 h
5) urgente en retard ? false (echeance 12:00 > maintenant 10:00)
   ancienne en retard ? true (echeance depassee depuis le 17/09)
   ancienne resolue -> encore en retard ? false (une reclamation resolue sort du SLA)
   meme urgente a 13:00 -> en retard ? true | RETARD de 1 h
6) Duration.between -> PT26H30M | toHours()=26 | toMinutes()=1590 | toDays()=1 (TRONQUE : 26 h = 1 jour complet)
   plus(Duration.ofHours(4)) -> 2026-09-20T12:00
7) Period.between -> P6Y4M5D soit 6 ans 4 mois et 5 jours
   plus(Period.ofMonths(3)) -> 2020-08-15
8) ChronoUnit.DAYS.between(2026-01-01, 2026-09-20) = 262
   ChronoUnit.HOURS.between(debut, fin) = 26
9) ISO_LOCAL_DATE -> 2026-09-20
   notre format     -> 20/09/2026 14:30
   parse("20/09/2026 14:30") -> 2026-09-20T14:30 | identique ? true
   PIEGE : texte invalide -> DateTimeParseException
10) depart              -> 2025-03-30T01:30+01:00[Europe/Paris]
    plus(Period.ofDays(1))   -> 2025-03-31T01:30+02:00[Europe/Paris]  (meme heure au mur, 23 h reelles : on a avance les montres)
    plus(Duration.ofDays(1)) -> 2025-03-31T02:30+02:00[Europe/Paris]  (24 h reelles, donc une heure de plus au mur)
11) Instant (UTC)          -> 2026-09-20T10:00:00Z
    vu de Dakar (UTC+0)    -> 2026-09-20T10:00Z[Africa/Dakar]
    vu de Paris (UTC+2)    -> 2026-09-20T12:00+02:00[Europe/Paris]
```

**Contrôles attendus, points par points :**

| Ce qui est vérifié | Résultat obtenu |
|---|---|
| L'horloge figée donne bien 10:00 | `3) maintenant (horloge figee) -> 20/09/2026 10:00` |
| L'échéance d'une urgence de 4 h est bien calculée | `#1 … echeance 20/09/2026 12:00` |
| 48 h après 09:00 le 20/09 → 09:00 le 22/09 | `#2 … echeance 22/09/2026 09:00` |
| La basse du 10/09 est en retard | `#3 … RETARD de 73 h` |
| À 10:00 l'urgence n'est pas en retard, à 13:00 elle l'est | `false` puis `true` (+ `RETARD de 1 h`) |
| Une réclamation résolue sort du SLA | `false (une reclamation resolue sort du SLA)` |
| L'écart exact en minutes | `toMinutes()=1590` |
| La troncature de `toDays()` | `toDays()=1` |
| Lecture aller-retour d'un texte de date | `identique ? true` |
| Erreur de format attendue | `DateTimeParseException` |
| Changement d'heure : deux résultats différents | `01:30+02:00` contre `02:30+02:00` |
| Même instant, deux fuseaux | `10:00Z[Dakar]` et `12:00+02:00[Paris]` |

> ℹ️ **Une remarque de précision sur la ligne 5** : à 10:00, l'urgence n'est **pas** en retard alors qu'il ne reste que 2 h. C'est un rappel utile pour le métier : « pas encore en retard » ne veut pas dire « confortable ». Une vraie plateforme ajouterait une notion d'**alerte** (par exemple « moins de 2 h restantes ») — un excellent exercice de modélisation, et une règle qui se testerait avec une horloge figée.

## Erreurs fréquentes et comment les reconnaître

| Message / symptôme | Cause | Correction |
|---|---|---|
| `DateTimeException: Invalid date 'FEBRUARY 30'` | date impossible construite avec `of(...)` | utiliser une date valide, ou valider la saisie en amont |
| `DateTimeParseException` | texte non conforme au motif | vérifier le motif (`dd/MM/yyyy` ≠ `MM/dd/yyyy`) et les séparateurs |
| `Method now() ... cannot be resolved` / date figée impossible à tester | vous appelez `LocalDateTime.now()` sans horloge | injecter un `Clock` et écrire `LocalDateTime.now(horloge)` |
| `incompatible types: LocalDate cannot be converted to LocalDateTime` | mélange de types (date vs date-heure, ou date locale vs `ZonedDateTime`) | convertir explicitement : `date.atStartOfDay()`, `dateTime.toLocalDate()` |
| `Duration` et `Period` donnent des résultats différents | confusion chronomètre / calendrier | choisir selon l'unité métier (heures → `Duration`, jours/mois → `Period`) |
| `toDays()` « perd » des heures | la méthode **tronque** | utiliser `ChronoUnit.HOURS`/`MINUTES` ou l'unité adaptée |
| « RETARD de -73 h » (double signe) | signe du texte + signe du nombre | `Math.abs(ecart.toHours())` |
| Une échéance change toute seule après modification de la priorité | échéance **calculée** sur une règle non contractuelle | stocker l'échéance si elle a été **annoncée** (fait historique) |

## Checklist de validation

Reprenez chaque point **sur votre code** :

- [ ] `Priorite.delaiMax()` renvoie un `Duration` et le code appelant se lit comme une phrase (`dateDeclaration.plus(priorite.delaiMax())`).
- [ ] `Reclamation` refuse une `dateDeclaration` nulle par une `IllegalArgumentException` (et non un `Optional`).
- [ ] `dateEcheance()` est une **méthode calculée** : aucun champ `dateEcheance` n'existe dans la classe.
- [ ] Aucun appel à `LocalDateTime.now()` « nu » : tout passe par `maintenant()` du service.
- [ ] `estEnRetard` renvoie `false` pour une réclamation **résolue** (vérifié à l'exécution ligne 5).
- [ ] `#1` affiche « temps restant : 2 h » à 10:00 et « RETARD de 1 h » à 13:00, sans modifier une ligne de code.
- [ ] `#3` affiche « RETARD de 73 h » (échéance du 17/09 09:00).
- [ ] `FORMAT` est `public static final` et je sais dire pourquoi c'est *thread-safe*.
- [ ] L'écart vaut `1590` minutes et j'ai écrit en commentaire pourquoi `toDays()` renvoie `1`.
- [ ] Le bonus démontre les deux heures différentes au changement d'heure, avec une explication exacte.
- [ ] J'ai vu le **même** `Instant` affiché dans deux fuseaux et je sais ce que cela prouve.

## Conseils pour progresser

1. **Refaites l'exercice en changeant l'horloge.** Passez de 10:00 à 12:00 puis à 12:01 : vous verrez la frontière exacte entre « temps restant » et « retard ». C'est exactement le type de vérification que vous écrirez en test unitaire (partie 9) — et le meilleur moyen de comprendre `isAfter` (strictement après : à 12:00 pile, la réclamation n'est **pas** encore en retard).
2. **Ajoutez une notion d'alerte** au service : `public boolean doitEtreAlerte(Reclamation r)` → vrai si le temps restant est inférieur à 25 % du délai. Vous manipulerez `Duration` dans les deux sens (comparaison et pourcentage) — un excellent entraînement.
3. **Essayez le calcul d'un délai « en jours ouvrés »** : c'est le cas typique où `Period` ne suffit pas non plus (il faut sauter les week-ends). Notez-le : les vrais SLA exigent souvent une règle métier explicite, écrite et testée.
4. **Prenez l'habitude de `Clock` dès maintenant.** En partie 7, Spring vous permettra d'injecter une horloge dans vos services ; en partie 9, vos tests de règles de délai seront **déterministes** et ne « casseront » jamais à cause de l'heure d'exécution.
5. **Notez la question « fait historique ou règle actuelle ? »** dans votre carnet : elle reviendra pour les prix, les taux, les barèmes… Tout ce qui doit rester stable dans le temps se **stocke**, tout ce qui se déduit se **calcule**.

➡️ **Fin de la partie 3** : SignalCUA dispose maintenant d'un registre **typé** (generics), **honnête sur l'absence** (`Optional`), ses données sont **rangées** en collections et il **mesure ses engagements** (`java.time`). Il reste à structurer la façon dont il **échoue** : la partie 4 (Gestion des exceptions) posera la hiérarchie d'exceptions métier, les `try/catch/finally`, le `try-with-resources` et les règles de propagation — en réutilisant tout ce que vous venez de construire.





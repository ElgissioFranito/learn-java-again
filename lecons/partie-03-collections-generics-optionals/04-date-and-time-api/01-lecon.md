# Leçon 04 — Date and Time API : mesurer les délais de SignalCUA

> 🧭 **Pont depuis la leçon 03** : votre registre sait dire « je n'ai pas trouvé » (`Optional`). Mais il manque une information essentielle à une plateforme de signalement citoyen : **le temps**. Une réclamation est déclarée à une date, et le `Priorite` de la partie 2 porte déjà un **délai maximal** (`URGENTE` = 4 h, `NORMALE` = 48 h, `BASSE` = 168 h). Jusqu'ici, ce nombre n'était qu'un entier affiché : impossible de savoir si une réclamation est **en retard**, ni de combien. Cette leçon installe `java.time` — l'API moderne de dates, obligatoire depuis Java 8 — et fait de SignalCUA une application qui **mesure ses engagements**.

---

## 1. Objectifs d'apprentissage

À la fin de cette leçon, vous saurez :

- Expliquer pourquoi `Date`, `Calendar` et `SimpleDateFormat` sont des impasses (mutabilité, mois à partir de 0, non *thread-safe*).
- Choisir le bon type : `LocalDate`, `LocalTime`, `LocalDateTime`, `Instant`, `ZonedDateTime`.
- Créer des dates (`of`, `now`, `parse`) et comprendre l'**immuabilité** de `java.time`.
- Mesurer un écart avec **`Duration`** (chronomètre) ou **`Period`** (calendrier), et savoir **pourquoi** on ne les confond pas.
- Comparer des dates (`isBefore`, `isAfter`) et compter une unité précise (`ChronoUnit`).
- Formater et relire des dates avec **`DateTimeFormatter`** (ISO-8601 et motif personnalisé).
- Rendre du code « temps-dépendant » **testable** grâce à **`Clock`**.
- Calculer l'échéance et le retard d'une réclamation (SLA) dans SignalCUA.

---

## 2. Explication simple

### 2.1 Pourquoi une nouvelle API : les trois pièges de `Date` et `Calendar`

**Pourquoi ?** Java 1.0 a livré `java.util.Date`, complété en 1.1 par `Calendar` et `SimpleDateFormat`. Ces classes sont **toujours là** (vous les croiserez), mais elles cumulent trois défauts structurels. Les connaître par cœur vous évitera de les utiliser… et vous fera comprendre les messages des collègues qui ont « toujours fait comme ça ».

| Défaut | Ce qui se passe | Conséquence |
|---|---|---|
| **Mutabilité** | `date.setTime(...)` modifie l'objet reçu | une méthode peut modifier la date de son appelant sans prévenir (bug sournois) |
| **Mois numérotés à partir de 0** | `new GregorianCalendar(2026, 8, 20)` = **septembre**, pas août | erreurs de saisie systématiques |
| **Non *thread-safe*** | `SimpleDateFormat` partagé entre plusieurs fils d'exécution donne des résultats faux | bugs aléatoires et très difficiles à reproduire |

**Analogie** : utiliser `Date`, c'est comme **dater un courrier au crayon** : n'importe qui peut effacer et réécrire la date après l'envoi. `java.time`, c'est la **date tamponnée** : une fois posée, elle ne bouge plus.

**Comment ?** Java 8 (2014) a introduit `java.time` (modèle JSR-310, inspiré de la bibliothèque Joda-Time) : des classes **immuables**, **thread-safe**, **lisibles**, avec des noms qui disent ce qu'elles contiennent. C'est ce paquet que vous utiliserez **toujours** — `Date` est réservé à l'interopérabilité avec du vieux code ou certaines bibliothèques.

> 📖 **Vocabulaire** : **SLA** (*Service Level Agreement*) = engagement de délai de traitement (« toute urgence traitée en 4 h ») ; dans notre fil rouge, c'est le cœur du métier. **Thread-safe** = qui supporte d'être utilisé par plusieurs fils d'exécution simultanément (partie 10). **API dépréciée (*deprecated*)** = marquée « à ne plus utiliser » par les auteurs (les méthodes de `Date` le sont largement).

### 2.2 Les cinq types de `java.time` à connaître

**Pourquoi ?** Une grande partie des bugs de date vient d'un mauvais **choix de type** : on stocke « une date » là où il fallait « un instant précis », ou l'inverse. Choisir le bon type, c'est résoudre le problème avant d'écrire la première ligne.

**Comment ?** Retenez ce tableau comme une carte de décision :

| Type | Ce qu'il représente | Exemple | Usage typique |
|---|---|---|---|
| `LocalDate` | une **date** sans heure ni fuseau | `2026-09-20` | une date de naissance, une échéance « au jour » |
| `LocalTime` | une **heure** seule | `14:30` | un horaire d'ouverture |
| `LocalDateTime` | date **+** heure, sans fuseau | `2026-09-20T14:30` | l'horodatage local d'un événement métier |
| `Instant` | un **point précis sur la ligne du temps**, toujours en **UTC** | `2026-09-20T10:00:00Z` | technique : stockage, journaux, API |
| `ZonedDateTime` | un instant **vu dans un fuseau** | `2026-09-20T12:00+02:00[Europe/Paris]` | affichage à un utilisateur d'un autre pays |

**Analogie** : `LocalDateTime` est **l'heure lue sur votre montre murale** (« il est 10 h ») — elle ne veut rien dire pour quelqu'un d'autre ailleurs dans le monde. `Instant` est **le moment absolu** (le même pour tout le monde, noté en temps universel coordonné). `ZonedDateTime` est **ce moment absolu affiché dans le fuseau de quelqu'un** (« 10 h à Dakar, 12 h à Paris »).

**Quand préférer quoi ?** Deux règles suffisent pour 95 % des cas :
- **Stockage et transmission** (base de données, API, journaux) → `Instant` (ou `LocalDate` si l'heure n'a vraiment aucun sens).
- **Calcul métier et affichage local** → `LocalDateTime` / `ZonedDateTime`.

> 📖 **Vocabulaire** : **UTC** = *Coordinated Universal Time*, l'heure de référence mondiale (successeur de GMT). **Fuseau horaire (*time zone*)** = règle de décalage par rapport à UTC, avec ses exceptions (`ZoneId.of("Africa/Dakar")` = UTC+0 toute l'année). **ISO-8601** = la norme d'écriture des dates (`2026-09-20T14:30:00Z`) — tri alphabétique = tri chronologique, c'est pour cela qu'elle s'impose partout. **Époque (*epoch*)** = le 1ᵉʳ janvier 1970 à 00:00 UTC, origine des comptages en informatique.

### 2.3 Créer des dates… et comprendre l'immuabilité

**Pourquoi ?** En `java.time`, **aucune** méthode ne modifie l'objet sur lequel elle est appelée. `plusDays(1)` ne « vieillit » pas votre date : elle **renvoie une nouvelle date**. C'est la même philosophie que `String` (partie 1) et que les `record` (partie 2).

**Comment ?**

```java
LocalDate jour     = LocalDate.of(2026, 9, 20);          // annee, mois (1-12 !), jour
LocalTime heure    = LocalTime.of(14, 30);
LocalDateTime quand = LocalDateTime.of(jour, heure);
LocalDateTime maintenant = LocalDateTime.now();          // l'horloge de la machine
LocalDate relue    = LocalDate.parse("2026-09-20");      // depuis du texte (format ISO)
LocalDateTime precise = LocalDateTime.of(2026, 9, 20, 14, 30, 15, 250_000_000);  // avec nanos
```

Et l'immuabilité, démontrée :

```java
LocalDate finMois   = LocalDate.of(2026, 1, 31);
LocalDate lendemain = finMois.plusDays(1);      // 2026-02-01 : NOUVELLE date
// finMois vaut TOUJOURS 2026-01-31
```

**Quand ?** `now()` sert à l'exécution réelle ; `of(...)` aux valeurs de référence et **aux tests** ; `parse(...)` quand une date arrive sous forme de texte (base, fichier, API, formulaire web).

**Deux cadeaux de `java.time`** que beaucoup découvrent trop tard :

- **Les dates impossibles sont refusées immédiatement** : `LocalDate.of(2026, 2, 30)` lève une `DateTimeException` (« Invalid date 'FEBRUARY 30' ») au lieu de produire silencieusement une date fausse.
- **Les débordements de calendrier sont gérés** : `LocalDate.of(2026, 1, 31).plusMonths(1)` donne `2026-02-28` (le mois le plus proche valide) — pas d'exception, pas de « 31 février ».

### 2.4 Mesurer : `Duration` (chronomètre) contre `Period` (calendrier)

**Pourquoi ?** « Combien de temps s'est-il écoulé ? » a **deux réponses différentes**, et les confondre produit des bugs que l'on ne voit qu'au changement d'heure ou aux fins de mois.

**Analogie** :
- `Duration` est un **chronomètre** : il compte des **heures, minutes, secondes** réelles. 24 h = 24 h, point.
- `Period` est un **calendrier** : il compte des **années, mois, jours** de calendrier. « Dans 1 jour » signifie « demain, à la même heure », même si demain ne compte que 23 heures.

**Comment ?**

```java
LocalDateTime debut = LocalDateTime.of(2026, 9, 20, 8, 0);
LocalDateTime fin   = LocalDateTime.of(2026, 9, 21, 10, 30);

Duration ecart = Duration.between(debut, fin);      // PT26H30M : 26 h 30 min
ecart.toHours();       // 26   (heures entieres)
ecart.toMinutes();     // 1590 (minutes totales)
ecart.toDays();        // 1    (TRONQUE : il reste 2 h 30 non comptees)

Period periode = Period.between(LocalDate.of(2020, 5, 15), LocalDate.of(2026, 9, 20));
// P6Y4M5D : 6 ans, 4 mois, 5 jours
periode.getYears();  // 6
periode.getMonths(); // 4
periode.getDays();   // 5
```

Et pour compter **une unité précise**, `ChronoUnit` est le couteau suisse :

```java
ChronoUnit.DAYS.between(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 9, 20));   // 262
ChronoUnit.HOURS.between(debut, fin);                                          // 26
```

**La preuve par l'exécution (section 3)** : au passage à l'heure d'été en Europe, « + 1 jour » et « + 24 heures » ne donnent **pas** la même heure. Le programme le montre :

```text
depart                   -> 2025-03-30T01:30+01:00[Europe/Paris]
plus(Period.ofDays(1))   -> 2025-03-31T01:30+02:00[Europe/Paris]   (même heure au mur, 23 h réelles)
plus(Duration.ofDays(1)) -> 2025-03-31T02:30+02:00[Europe/Paris]   (24 h réelles, une heure de plus)
```

**Conclusion opérationnelle** : **délai contractuel en jours** (« sous 3 jours ouvrés ») → `Period` ; **délai en heures** (« sous 4 heures », notre cas) → `Duration`. Et pour une date d'affichage d'échéance, précisez toujours **dans quel fuseau** elle est calculée — sinon, un utilisateur à Paris et un autre à Dakar ne verront pas la même échéance.

### 2.5 Comparer des dates et compter une unité

**Pourquoi ?** Savoir « est-ce en retard ? » suppose de **comparer**. Et en `java.time`, la comparaison ne se fait pas avec `==` (les objets sont distincts) ni avec les opérateurs `<`/`>` (interdits sur les objets) : chaque classe offre ses méthodes et implémente `Comparable<T>` (leçon 02 !).

**Comment ?**

```java
LocalDateTime maintenant   = LocalDateTime.now();
LocalDateTime dateEcheance = LocalDateTime.of(2026, 9, 20, 12, 0);

maintenant.isAfter(dateEcheance);      // true si maintenant est APRES l'echeance
maintenant.isBefore(dateEcheance);     // true si maintenant est AVANT
maintenant.equals(dateEcheance);       // egalite exacte (utile dans les tests)
maintenant.compareTo(dateEcheance);    // < 0, 0, > 0 (tri, Comparable)
```

Pour des **écarts exprimés dans une unité**, on combine `ChronoUnit` et `Duration` :

```java
long heuresDeRetard = ChronoUnit.HOURS.between(dateEcheance, maintenant);   // negatif si on est en avance
Duration ecartSigne = Duration.between(maintenant, dateEcheance);           // signe = sens de l'ecart
```

**Règle pratique** : utilisez `Duration`/`ChronoUnit` pour **mesurer**, `isBefore`/`isAfter` pour **décider**. Et n'oubliez jamais que `LocalDateTime` **ne connaît pas les fuseaux** : deux machines dans deux fuseaux comparant « leurs » `LocalDateTime` ne parlent pas du même instant (l'`Instant`, lui, est comparable partout).

### 2.6 Formater et relire : `DateTimeFormatter`

**Pourquoi ?** Un utilisateur lit `20/09/2026 14:30`, une API échange `2026-09-20T14:30:00Z`, une base stocke un `TIMESTAMP`. Il faut donc **convertir** sans ambiguïté — et surtout **sans utiliser `SimpleDateFormat`** (non *thread-safe*).

**Comment ?**

```java
public static final DateTimeFormatter FORMAT =         // un CONSTANT, cree une seule fois
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

moment.format(FORMAT);                                       // objet -> texte  : "20/09/2026 14:30"
LocalDateTime.parse("20/09/2026 14:30", FORMAT);             // texte -> objet
moment.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);        // "2026-09-20T14:30" (standard)
```

**Les lettres de motif à connaître** (attention : la casse compte !) :

| Lettre | Signification | Exemple |
|---|---|---|
| `dd` | jour sur 2 chiffres | `20` |
| `MM` | mois sur 2 chiffres (`mm` minuscule = **minutes** !) | `09` |
| `yyyy` | année | `2026` |
| `HH` | heure sur 24 h (`hh` = 12 h, `a` = AM/PM) | `14` |
| `mm` | minutes | `30` |
| `ss` | secondes | `05` |

**En cas de texte invalide** (un formulaire mal rempli, une donnée corrompue), la méthode `parse` lève une **`DateTimeParseException`** : c'est une erreur **attendue** à traiter (elle annonce un problème venant de l'extérieur, comme une saisie utilisateur).

**Et pour d'autres langues ?** Ajoutez une `Locale` : `DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.FRENCH)` donnera « dimanche 20 septembre 2026 ». Sans `Locale`, Java utilise celle de la machine — un piège lors d'un déploiement sur un serveur en anglais.

> 📖 **Vocabulaire** : **motif (*pattern*)** = le gabarit de formatage (`dd/MM/yyyy`). **`Locale`** = langue + pays, qui détermine les noms de jours/mois et les conventions. **`DateTimeParseException`** = erreur de lecture d'un texte de date.

### 2.7 Rendre le temps testable : `Clock`

**Pourquoi ?** Un code qui appelle `LocalDateTime.now()` est **impossible à tester de façon fiable** : le résultat change à chaque exécution, et un test qui vérifie « en retard ? » dépendrait de l'heure à laquelle il tourne. C'est le défaut n°1 des applications qui manipulent le temps.

**Comment ?** On **injecte une horloge** (`Clock`) au lieu d'appeler `now()` directement :

```java
public class ServiceDelais {

    private final Clock horloge;                       // la dependance

    public ServiceDelais(Clock horloge) {              // fournie a la construction
        this.horloge = horloge;
    }

    public LocalDateTime maintenant() {
        return LocalDateTime.now(horloge);             // TOUT le code passe par ici
    }
}

// En production (partie 7, Spring fournit une horloge système) :
ServiceDelais service = new ServiceDelais(Clock.systemDefaultZone());

// Dans un test, on FIGE le temps (partie 9) :
Clock fige = Clock.fixed(
        LocalDateTime.of(2026, 9, 20, 10, 0).atZone(ZoneId.of("Africa/Dakar")).toInstant(),
        ZoneId.of("Africa/Dakar"));
ServiceDelais serviceDuTest = new ServiceDelais(fige);   // "maintenant" = toujours 10:00
```

`Clock` offre aussi `Clock.offset(...)` (horloge décalée), `Clock.fixed(...)` (temps arrêté) et `Clock.systemUTC()` (horloge réelle en UTC). Vous verrez en partie 9 que c'est ce qui permet d'écrire un test **déterministe** du type : « à 13:00, une urgence déclarée à 08:00 est en retard ».

> 📖 **Vocabulaire** : **injection de dépendances** = fournir les dépendances de l'extérieur plutôt que de les créer à l'intérieur (le cœur de Spring, partie 7). **Test déterministe** = test dont le résultat est **toujours** le même (indispensable pour une suite de tests fiable). **`Clock`** = abstraction de l'horloge système.

### 2.8 Le cas SignalCUA : échéance **calculée**, jamais recopiée

**Pourquoi ?** Une réclamation a une **date de déclaration**, une **priorité** (donc un délai) et une **échéance**. Le réflexe serait de stocker trois champs… et c'est là qu'apparaît le bug : si quelqu'un modifie la priorité d'une urgence en « normale » (48 h au lieu de 4 h), l'échéance stockée devient **fausse** — elle ne suit pas.

**Comment ?** On **calcule** l'échéance à partir des deux autres informations :

```java
public class Reclamation {

    private final LocalDateTime dateDeclaration;      // donnee fournie
    private final Priorite priorite;                  // donnee fournie

    public LocalDateTime dateEcheance() {             // resultat CALCULE (aucun champ)
        return dateDeclaration.plus(priorite.delaiMax());     // plus(Duration)
    }
}
```

où `Priorite.delaiMax()` (ajouté à l'énumération de la partie 2) transforme le délai en objet `Duration` :

```java
public Duration delaiMax() {
    return Duration.ofHours(delaiHeuresMax);
}
```

> ⚠️ **Nuance professionnelle importante** : « calculé » est le bon choix quand la **règle** est stable. Si l'échéance est **contractuelle** (elle a été annoncée au citoyen, et la règle peut changer plus tard), il faut la **stocker** — sinon vos anciennes réclamations verraient leur échéance se déplacer avec la nouvelle règle. La bonne question n'est donc pas « calculer ou stocker ? » mais « **est-ce un fait historique ou une conséquence de la règle actuelle ?** ».

**Et le retard ?** Le service le calcule à l'instant présent, avec l'horloge injectée :

```java
public boolean estEnRetard(Reclamation r) {
    return !r.estResolue() && maintenant().isAfter(r.dateEcheance());
}
```

Notez la **première condition** : une réclamation **résolue** n'est plus « en retard », même si elle l'a été. Ce genre de règle métier — « quel état compte pour le SLA ? » — est exactement ce qu'il faut clarifier **avant** d'écrire le code (et ce que l'on documente dans un test, partie 9).

---

## 📖 Vocabulaire / Abréviations

| Terme | Définition en une ligne |
|---|---|
| **`java.time`** | Le paquet de l'API de dates moderne (Java 8+) : immuable, lisible, *thread-safe*. |
| **`LocalDate`** | Une **date** sans heure ni fuseau (`2026-09-20`). |
| **`LocalTime`** | Une **heure** sans date (`14:30`). |
| **`LocalDateTime`** | Date **+** heure, **sans fuseau** (« l'heure de la montre murale »). |
| **`Instant`** | Un **point absolu** sur la ligne du temps, en UTC (`2026-09-20T10:00:00Z`). |
| **`ZonedDateTime`** | Un instant **vu dans un fuseau** (`…+02:00[Europe/Paris]`). |
| **`ZoneId`** | Identifiant d'un fuseau horaire (`"Africa/Dakar"`, `"Europe/Paris"`). |
| **UTC** | *Coordinated Universal Time* : l'heure de référence mondiale. |
| **ISO-8601** | Norme d'écriture des dates/heures (`2026-09-20T14:30`) : trier le texte = trier le temps. |
| **Immuable** | Qui ne change jamais après création : `plusX(...)` renvoie **un nouvel objet**. |
| **`Duration`** | Une durée **réelle** (heures/minutes/secondes) — le « chronomètre ». |
| **`Period`** | Une durée **de calendrier** (années/mois/jours) — « dans 3 mois ». |
| **`ChronoUnit`** | Compteur d'une unité précise entre deux dates (`DAYS`, `HOURS`, `MONTHS`…). |
| **`isBefore` / `isAfter`** | Comparaisons de dates (« avant / après »). |
| **`DateTimeFormatter`** | Objet de **formatage** (objet → texte) et de **lecture** (texte → objet) — remplace `SimpleDateFormat`. |
| **Motif (*pattern*)** | Le gabarit de formatage (`dd/MM/yyyy HH:mm`). |
| **`Locale`** | Langue + pays (noms de jours/mois, conventions d'affichage). |
| **`Clock`** | Abstraction de l'horloge : injectable, donc **testable** (`Clock.fixed`, `Clock.systemUTC`). |
| **`DateTimeException`** | Erreur de date invalide (`LocalDate.of(2026, 2, 30)`). |
| **`DateTimeParseException`** | Erreur de **lecture** d'un texte de date. |
| **Heure d'été / d'hiver (*DST*)** | Changement d'offset annuel : une journée de 23 h ou 25 h. |
| **SLA** | *Service Level Agreement* : engagement de délai (« urgences sous 4 h »). |
| **Échéance** | Date limite de traitement, ici **calculée** : `dateDeclaration + délai de la priorité`. |
| **Retard** | Écart positif entre l'instant présent et l'échéance, pour une réclamation non résolue. |
| **`java.util.Date` / `Calendar`** | Anciennes classes **mutables** et piégeuses — à éviter (sauf interopérabilité). |
| **`SimpleDateFormat`** | Ancien formateur **non *thread-safe*** — remplacé par `DateTimeFormatter`. |
| **Époque (*epoch*)** | 1970-01-01T00:00Z, origine des comptages de temps en informatique. |

---

## 3. Exemples concrets

Trois fichiers : `Reclamation` (avec sa date de déclaration), `ServiceDelais` (le calcul du SLA avec horloge injectée) et `MainDates` (la démonstration). Tout a été compilé et exécuté en Java 21.

**Fichier 1 — `Reclamation.java` : la date entre dans le modèle.**

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
    // ... demarrerTraitement(), marquerResolue(), estResolue(), getters ...
}
```

**Trois choix à noter :**

- **`dateDeclaration` est `final`** : la date de déclaration est un **fait historique**, elle ne se modifie pas (immutabilité, partie 2).
- **`dateEcheance()` est une méthode, pas un champ** : aucun risque de désynchronisation avec la priorité (voir section 2.8 et sa nuance).
- **La validation refuse `null`** : ici, une réclamation **sans** date de déclaration est une erreur de programmation, pas une absence légitime — donc exception immédiate, et **pas** d'`Optional` (leçon 03, section 2.7).

**Fichier 2 — `ServiceDelais.java` : le service de SLA, avec horloge injectée.**

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

**Ce que ce service illustre, ligne par ligne :**

- **`FORMAT` est `public static final`** : un formateur est **immutable et *thread-safe***, on le crée **une fois** et on le partage. C'est l'exact inverse du piège `SimpleDateFormat` (section 5).
- **`maintenant()` est le seul point d'accès au temps.** Le jour où il faudra « l'heure de la ville » (avec fuseau), une seule méthode changera.
- **`estEnRetard`** : `!estResolue() && isAfter(echeance)` — la règle métier complète, lisible en une ligne.
- **`ecart` renvoie un `Duration` signé** : positif = marge, négatif = retard. Renvoyer la **durée**, et non un texte, laisse l'appelant libre (afficher, trier, agréger).
- **`resumeSla`** : `toHours()` tronque (section 2.4), ce qui est le comportement souhaité ici (« RETARD de 73 h »). `Math.abs` évite le double signe « --73 h ».

**Fichier 3 — `MainDates.java` (extrait) : la démonstration.**

```java
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

public class MainDates {

    public static void main(String[] args) {

        // ============ 1. Creer des dates : LocalDate, LocalTime, LocalDateTime ============
        LocalDate jour = LocalDate.of(2026, 9, 20);                 // annee, mois (1-12 !), jour
        LocalTime heure = LocalTime.of(14, 30);                     // heures, minutes
        LocalDateTime moment = LocalDateTime.of(jour, heure);
        System.out.println("1) LocalDate      -> " + jour);
        System.out.println("   LocalTime      -> " + heure);
        System.out.println("   LocalDateTime  -> " + moment);

        // Piege : une date impossible est refusee TOUT DE SUITE
        try {
            LocalDate impossible = LocalDate.of(2026, 2, 30);
            System.out.println("   " + impossible);
        } catch (java.time.DateTimeException e) {
            System.out.println("   PIEGE : LocalDate.of(2026, 2, 30) -> " + e.getMessage());
        }

        // ============ 2. L'immuabilite : les methodes RENVOIENT une nouvelle date ============
        LocalDate finMois = LocalDate.of(2026, 1, 31);
        LocalDate lendemain = finMois.plusDays(1);                  // finMois ne change JAMAIS
        System.out.println("2) " + finMois + " + 1 jour = " + lendemain
                + " (et finMois vaut toujours " + finMois + ")");
        System.out.println("   2026-01-31 plus 1 mois = " + LocalDate.of(2026, 1, 31).plusMonths(1)
                + " (le mois se raccourcit a la fin du mois)");

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
```

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
    }
}
```

> 📝 **Note de méthode** : dans le fichier réel, `DateTimeFormatter` est importé (`import java.time.format.DateTimeFormatter;`) pour éviter d'écrire le nom complet. Le code présenté ci-dessus est fonctionnellement identique.

Compilation et exécution :

```bash
javac -encoding UTF-8 *.java      # StatutReclamation, Priorite, Reclamation, ServiceDelais, MainDates
java MainDates
```

**Sortie réellement obtenue (Java 21.0.7) :**

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

**Lisons cette sortie ensemble :**

1. **Lignes 1** : les trois types de base, et le **refus immédiat** d'une date impossible (`Invalid date 'FEBRUARY 30'`) — au lieu d'un 30 février silencieux comme le ferait l'ancien `Calendar` (qui aurait fabriqué le 2 mars).
2. **Lignes 2** : `finMois` **n'a pas changé** (immuabilité), et `plusMonths(1)` sur le 31 janvier donne `2026-02-28` : le calendrier grégorien est respecté sans exception. Notez aussi `+ 1 jour = 2026-02-01` (passage de mois géré).
3. **Ligne 3** : `maintenant()` = 10:00 **exactement** parce que l'horloge est figée. C'est cette prévisibilité qui rendra vos tests fiables (partie 9).
4. **Lignes 4** : les échéances sont **calculées**, pas saisies : `#1` urgente déclarée à 08:00 → échéance **12:00** (4 h) avec « temps restant : 2 h » ; `#2` normale → **22/09 09:00** (48 h) avec 47 h restantes ; `#3` basse déclarée le 10/09 → échéance 17/09 09:00, d'où **« RETARD de 73 h »**.
5. **Lignes 5** : la même urgence est « pas en retard » à 10:00 et « en retard de 1 h » à 13:00 — **avec le même code**, seule l'horloge change. Et une réclamation **résolue** ne compte plus comme en retard : la règle métier est explicite.
6. **Lignes 6** : `Duration` donne `PT26H30M`, avec `toHours()=26`, `toMinutes()=1590` et **`toDays()=1`** : la troncature est visible (26 h = 1 jour *complet* + 2 h 30). Le `plus(Duration.ofHours(4))` ajoute bien 4 h à la déclaration.
7. **Lignes 7** : `Period` affiche `P6Y4M5D` et ses composants `6 ans 4 mois 5 jours` — utile pour une ancienneté, un âge, un délai en mois.
8. **Lignes 8** : `ChronoUnit.DAYS.between` donne `262` (nombre de jours exacts de l'année écoulée) et `HOURS` donne `26` : deux façons de compter la **même** période selon l'unité voulue.
9. **Lignes 9** : formatage ISO et formatage « français », puis `parse` qui **retrouve l'objet d'origine** (`identique ? true`). Un texte invalide produit une `DateTimeParseException` — l'erreur **attendue** quand une donnée vient de l'extérieur.
10. **Lignes 10** : **la démonstration la plus précieuse de cette leçon**. Le 30 mars 2025, la France passe à l'heure d'été : `plus(Period.ofDays(1))` donne `01:30+02:00` (même heure au mur, mais **23 h réelles**), tandis que `plus(Duration.ofDays(1))` donne `02:30+02:00` (**24 h réelles**). Choisir le mauvais outil décale une échéance d'une heure.
11. **Lignes 11** : le **même** `Instant` (`10:00:00Z`) lu à Dakar (`10:00`) et à Paris (`12:00`). Un seul moment, deux affichages — c'est toute la différence entre `Instant` et `LocalDateTime`.

---

## 4. Bonnes pratiques modernes (2025-2026)

1. **`java.time` uniquement.** Pas de `Date`, pas de `Calendar`, pas de `SimpleDateFormat` dans du code neuf. Si une bibliothèque ancienne impose un `Date`, convertissez **aux frontières** : `Date.from(instant)` / `date.toInstant()`.
2. **Choisissez le type selon le sens, pas selon l'habitude** : `LocalDate` (jour), `LocalDateTime` (heure murale métier), `Instant` (technique et universel). Évitez `LocalDateTime` quand vous parlez d'un **instant** partagé entre plusieurs fuseaux.
3. **Injectez `Clock`** au lieu d'appeler `now()` directement. Sans cela, aucune règle de délai n'est testable de façon fiable — et c'est exactement ce que la partie 9 exigera de vous.
4. **`Duration` pour les délais en heures/minutes, `Period` pour les jours/mois/années.** Notre SLA est en heures → `Duration` (via `Priorite.delaiMax()`).
5. **Un `DateTimeFormatter` est `static final`** (immutable, *thread-safe*) — jamais construit dans une boucle ni stocké dans un champ d'instance recréé à chaque usage.
6. **ISO-8601 par défaut aux frontières techniques** (API JSON, fichiers, journaux) : c'est le format compris par tout le monde et triable tel quel. Les formats « à la française » restent pour l'**affichage**.
7. **Calculez ce qui dérive, stockez ce qui est contractuel** : l'échéance dérivée de la priorité est calculée (section 2.8) ; mais une échéance déjà annoncée au citoyen est un **fait historique** à conserver.
8. **`equals` pour comparer dans les tests**, `isBefore`/`isAfter`/`compareTo` pour comparer logiquement. Jamais `==`.
9. **Précisez le fuseau dès qu'un humain d'un autre pays est concerné** : stockez en UTC, convertissez à l'affichage avec `ZoneId`.
10. **Anticipez Spring/JPA (parties 7-8)** : JPA mappe `LocalDate` → `DATE`, `LocalDateTime` → `TIMESTAMP`, `Instant` → `TIMESTAMP WITH TIME ZONE` ; Jackson sérialise ces types en ISO-8601 **à condition** d'avoir le module `jackson-datatype-jsr310` (inclus par défaut dans Spring Boot).

---

## 5. Pièges à éviter

### Piège 1 — L'ancien duo `Date` + `Calendar`

```java
// ❌ MAUVAIS : mois indexe a partir de 0, objet MUTABLE, non thread-safe
Calendar cal = new GregorianCalendar(2026, 8, 20);   // 8 = SEPTEMBRE (pas aout)
cal.add(Calendar.DAY_OF_MONTH, 1);
Date date = cal.getTime();
date.setTime(0);                                     // modifie l'objet recu par l'appelant !

// ✅ BON : lisible, immuable, sur
LocalDate jour = LocalDate.of(2026, 9, 20);          // 9 = septembre, comme on le pense
LocalDate lendemain = jour.plusDays(1);
```

**Pourquoi c'est dangereux** : le mois « 8 = septembre » produit des bugs de décalage d'un mois **silencieux** (ils ne se voient qu'en comparant les données), la mutabilité permet de corrompre l'état d'un appelant, et le partage entre *threads* donne des résultats faux de façon aléatoire.

### Piège 2 — `SimpleDateFormat` partagé entre plusieurs *threads*

```java
// ❌ MAUVAIS : un formateur ANCIEN, non thread-safe, garde un etat interne pendant format()
public class Service {
    private static final SimpleDateFormat FORMAT = new SimpleDateFormat("dd/MM/yyyy");
    // Utilise par plusieurs requetes web en meme temps -> dates melangees ou exception
}

// ✅ BON : DateTimeFormatter est immuable et thread-safe, donc PARFAIT en static final
public class ServiceDelais {
    public static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
}
```

**Pourquoi** : ce piège ne se voit **jamais** en développement (une seule requête à la fois) et apparaît en production sous charge. C'est l'archétype du bug « impossible à reproduire ».

### Piège 3 — Confondre `Duration` et `Period`

```java
// ❌ MAUVAIS : "dans 1 jour" ecrit avec un chronometre
LocalDateTime echeance = declaration.plus(Duration.ofDays(1));
// Le 30/03/2025 (passage a l'heure d'ete), cela donne 02:30 au lieu de 01:30 :
// le citoyen voit une heure de plus que prevu.

// ✅ BON : "dans 1 jour" est une notion de CALENDRIER
LocalDateTime echeance = declaration.plus(Period.ofDays(1));   // meme heure, le lendemain

// ✅ ET pour un delai horaire (notre SLA), c'est Duration qui est correct
LocalDateTime echeanceUrgente = declaration.plus(Duration.ofHours(4));
```

**Pourquoi** : les deux méthodes s'appellent `ofDays` et retournent… un objet de types différents, acceptés tous les deux par `plus(...)`. Le compilateur ne peut pas vous sauver : c'est à vous de savoir **ce que vous mesurez** (vérifié en section 3, ligne 10).

### Piège 4 — `toDays()` / `toHours()` qui tronquent

```java
Duration d = Duration.ofHours(26);
d.toDays();      // 1    ❌ on croit "26 h = 1 jour" alors qu'il reste 2 h non comptees
d.toHours();     // 26   ✅ l'unite reelle

// ✅ BON : choisir l'unite adaptee, ou utiliser ChronoUnit pour compter precisement
ChronoUnit.MINUTES.between(debut, fin);          // 1590 : aucune perte
```

**Pourquoi** : `toX()` **tronque** (partie entière). Un test qui vérifie « il reste 1 jour » avec `toDays()` sur 26 h sera vrai… et faux dès que le délai passe à 25 h 59. Utilisez l'unité de votre besoin, ou `ChronoUnit` pour un comptage exact.

### Piège 5 — Comparer des dates avec `==` ou les opérateurs

```java
// ❌ NE COMPILE PAS ou resultat FAUX
if (date1 == date2) { }                   // compare des references, pas des valeurs
if (date1 < date2) { }                    // interdit sur des objets

// ✅ BON
if (date1.equals(date2)) { }              // egalite de valeur (tests)
if (date1.isBefore(date2)) { }            // comparaison logique
if (date1.compareTo(date2) < 0) { }       // tri (Comparable, lecon 02)
```

### Piège 6 — Une date impossible qui passe inaperçue

```java
// ❌ AVANT java.time : Calendar "arrondissait" en silence
Calendar cal = new GregorianCalendar(2026, 1, 30);   // 30 fevrier -> 2 mars 2026, sans rien dire

// ✅ AVEC java.time : impossible de se tromper
LocalDate.of(2026, 2, 30);   // DateTimeException : Invalid date 'FEBRUARY 30'
```

**Pourquoi c'est une bonne nouvelle** : une exception immédiate vaut mieux qu'une donnée fausse en base, découverte six mois plus tard par un utilisateur.

### Piège 7 — `now()` partout dans le code

```java
// ❌ MAUVAIS : impossible a tester, et le temps change PENDANT le traitement
public boolean estEnRetard(Reclamation r) {
    return LocalDateTime.now().isAfter(r.dateEcheance());
}
public String resume(Reclamation r) {
    return "ecart : " + Duration.between(LocalDateTime.now(), r.dateEcheance()).toHours() + " h";
    // Deux appels a now() : quelques millisecondes d'ecart, parfois un resultat incoherent
}

// ✅ BON : une seule source de temps, injectee
public boolean estEnRetard(Reclamation r) {
    return maintenant().isAfter(r.dateEcheance());          // maintenant() = LocalDateTime.now(horloge)
}
```

**Pourquoi** : deux appels à `now()` dans la même méthode peuvent tomber de part et d'autre d'une minute (ou d'une échéance !). Et sans `Clock`, aucun test ne peut figer « maintenant » pour vérifier une règle de retard.

### Anti-pattern — stocker un texte là où il faut une date

```java
// ❌ MAUVAIS : un String pour une date -> plus de comparaison, plus de calcul, format inconnu
private String dateDeclaration = "20/09/2026";       // et si c'est "09/20/2026" ?

// ✅ BON : le type porte le sens
private LocalDateTime dateDeclaration;               // conversion seulement aux frontieres (parse/format)
```

---

## Checklist de validation

Avant de clore la partie 3, vérifiez que vous savez faire **chacun** de ces points :

- [ ] Expliquer les trois défauts de `Date`/`Calendar`/`SimpleDateFormat` (mutabilité, mois à partir de 0, non *thread-safe*).
- [ ] Choisir entre `LocalDate`, `LocalTime`, `LocalDateTime`, `Instant` et `ZonedDateTime` selon le besoin.
- [ ] Créer une date avec `of`, `parse`, `now`, et expliquer ce que signifie « les objets `java.time` sont immuables ».
- [ ] Dire pourquoi `LocalDate.of(2026, 2, 30)` échoue alors que `plusMonths(1)` sur le 31 janvier réussit.
- [ ] Mesurer un écart avec `Duration` et `Period`, et expliquer la différence **par un exemple de changement d'heure**.
- [ ] Expliquer pourquoi `Duration.ofHours(26).toDays()` renvoie `1` (troncature).
- [ ] Compter une unité précise avec `ChronoUnit`.
- [ ] Comparer deux dates avec `isBefore`/`isAfter`/`equals` (et jamais `==`).
- [ ] Écrire un `DateTimeFormatter` `static final` et expliquer pourquoi il est *thread-safe* contrairement à `SimpleDateFormat`.
- [ ] Utiliser `Locale.FRENCH` pour un affichage en français, et dire ce qui se passe sans `Locale`.
- [ ] Injecter un `Clock` et fabriquer une horloge figée avec `Clock.fixed(...)`.
- [ ] Écrire une méthode `estEnRetard(Reclamation)` qui combine `estResolue()` et `isAfter(dateEcheance())`.
- [ ] Dire quand il faut **stocker** une échéance plutôt que la calculer.

---

➡️ **Prochaine étape** : la partie 3 est close — vos données sont **rangées** (`List`, `Set`, `Map`, `Queue`), **typées** (generics), vos absences **explicites** (`Optional`) et vos délais **mesurés** (`java.time`). SignalCUA sait désormais *quand* il ne respecte pas ses engagements… mais il ne sait pas **quoi faire** quand quelque chose se passe mal : une description vide, un identifiant inconnu, un texte de date illisible. Jusqu'ici, nous avons levé des `IllegalArgumentException` et des `IllegalStateException`, en les attrapant avec des `try/catch` — sans jamais nous demander *qui* doit les traiter, *comment* les structurer, ni ce qu'il advient d'une exception non attrapée. La **partie 4 — Gestion des exceptions** transforme ce bricolage en stratégie : hiérarchie d'exceptions métier, `try/catch/finally`, `try-with-resources`, exceptions personnalisées et bonnes pratiques de propagation.












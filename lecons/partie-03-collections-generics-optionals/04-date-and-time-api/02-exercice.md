# Exercice 04 — Le SLA des réclamations : mesurer les délais de SignalCUA

> 🧭 **Comment ce fichier s'articule** : la leçon (`01-lecon.md`) a présenté `java.time` avec un service de délais complet. Ici, vous construisez **votre** version, avec une horloge injectée — le même schéma que dans les tests de la partie 9. La correction (`03-correction.md`) vous attend après votre tentative.

---

## 🎯 Objectif de l'exercice

Ajouter la dimension temps à SignalCUA : chaque réclamation reçoit une **date de déclaration**, son **échéance** se calcule à partir de la priorité (déjà présente depuis la partie 2), et un **service** décide si elle est en retard — avec une horloge **injectée** pour que tout soit vérifiable.

**Fichiers de départ** : `StatutReclamation.java`, `Priorite.java`, `Reclamation.java` (versions de la partie 3).
**Bonus utile** : `RegistreReclamations.java` de l'exercice 03 (facultatif, pour un affichage groupé).

## 📋 Énoncé

### Étape 1 — Le délai devient un `Duration`
Dans l'énumération `Priorite` (partie 2), ajoutez :

```java
public Duration delaiMax() { return Duration.ofHours(delaiHeuresMax); }
```

⚠️ Cela introduit un import (`java.time.Duration`) dans un `enum` — c'est parfaitement normal et sans conséquence sur son fonctionnement.

### Étape 2 — La date de déclaration entre dans `Reclamation`
Ajoutez un champ `private final LocalDateTime dateDeclaration` :
1. constructeur complété (un paramètre de plus) avec une **validation** : si la date est `null`, lever une `IllegalArgumentException` (rappel : ce n'est pas une absence légitime, donc pas d'`Optional`) ;
2. getter `getDateDeclaration()` ;
3. méthode **calculée** `public LocalDateTime dateEcheance()` — **sans champ supplémentaire** — qui renvoie `dateDeclaration.plus(priorite.delaiMax())`.

### Étape 3 — Le service de délais avec horloge injectée
Créez `ServiceDelais` :
1. un champ `private final Clock horloge` fourni par le constructeur ;
2. `public LocalDateTime maintenant()` → `LocalDateTime.now(horloge)` ;
3. `public boolean estEnRetard(Reclamation r)` → vrai **seulement si** la réclamation n'est pas résolue **et** que l'échéance est dépassée ;
4. `public Duration ecart(Reclamation r)` → l'écart **signé** entre maintenant et l'échéance (positif = temps restant) ;
5. un `DateTimeFormatter` **`public static final`** au motif `dd/MM/yyyy HH:mm` ;
6. `public String resumeSla(Reclamation r)` → `"temps restant : X h"` ou `"RETARD de X h"` ;
7. `public String ligneSla(Reclamation r)` → une ligne complète `#id déclarée le … | échéance … | résumé`.

### Étape 4 — La démonstration avec une horloge **figée**
Dans un `MainDates`, figez l'horloge au **20/09/2026 à 10:00** (fuseau de votre choix, par exemple `Africa/Dakar`) et créez :
- `#1` `URGENTE` déclarée le 20/09 à **08:00** (échéance attendue : 12:00) ;
- `#2` `NORMALE` déclarée le 20/09 à **09:00** (échéance attendue : 22/09 à 09:00) ;
- `#3` `BASSE` déclarée le **10/09** à 09:00 (déjà en retard).

Affichez les trois lignes de SLA, puis :
1. `estEnRetard` pour les trois ;
2. marquez `#3` comme résolue (démarrer puis résoudre) et re-testez `estEnRetard` ;
3. créez un **second** service avec une horloge figée à **13:00** le 20/09 et affichez l'état de `#1` : elle doit désormais être en retard de 1 h.

### Étape 5 — Manipuler les durées de calendrier
1. Affichez l'**ancienneté** (en années, mois, jours) entre le 15/05/2020 et le 20/09/2026 avec `Period` ;
2. calculez l'écart exact en minutes entre le 20/09/2026 08:00 et le 21/09/2026 10:30 (réponse attendue : `1590`) ;
3. affichez `duree.toDays()` pour cette même durée et **expliquez en commentaire** pourquoi le résultat est `1`.

### Étape 6 — Formatage et lecture
1. Affichez le 20/09/2026 14:30 au format ISO (`ISO_LOCAL_DATE_TIME`) **et** au format `dd/MM/yyyy HH:mm` ;
2. relisez `"20/09/2026 14:30"` avec `LocalDateTime.parse(..., FORMAT)` et vérifiez l'égalité avec l'original (`equals`) ;
3. provoquez une **`DateTimeParseException`** avec un texte volontairement invalide et affichez son type.

### Étape 7 (bonus) — Fuseaux et changement d'heure
1. Affichez le **même** `Instant` vu de `Africa/Dakar` (UTC+0) **et** de `Europe/Paris` ;
2. démontrez que, au passage à l'heure d'été du **30/03/2025 à 01:30** (Paris), `plus(Period.ofDays(1))` et `plus(Duration.ofDays(1))` **ne donnent pas la même heure** ; expliquez la différence en une phrase.

## ✅ Critères de réussite

- [ ] `Priorite.delaiMax()` existe et renvoie un `Duration`.
- [ ] `Reclamation` valide `dateDeclaration` (exception si `null`) et **calcule** `dateEcheance()`.
- [ ] `ServiceDelais` n'appelle **jamais** `LocalDateTime.now()` sans horloge.
- [ ] `estEnRetard` renvoie `false` pour une réclamation **résolue**, même en retard.
- [ ] La sortie montre `#1` « 2 h restantes » à 10:00 puis « RETARD de 1 h » à 13:00, **avec le même code**.
- [ ] `#3` affiche « RETARD de 73 h ».
- [ ] L'écart en minutes vaut `1590`, et l'explication de `toDays() == 1` est écrite dans le code.
- [ ] Le bonus montre les deux heures différentes au changement d'heure, avec une explication juste.

## 💡 Indications (lisez seulement si bloqué)

- `Clock.fixed(instant, zone)` : construisez l'`Instant` avec `LocalDateTime.of(...).atZone(ZoneId.of(...)).toInstant()`.
- `Duration.between(debut, fin)` demande deux objets **du même type** (`LocalDateTime` avec `LocalDateTime`).
- Pour l'écart signé, l'ordre des arguments compte : `Duration.between(maintenant, echeance)` est **positif** quand il reste du temps.
- `Math.abs(...)` évite d'afficher « --73 h » (un signe du texte et un signe du nombre).
- Une réclamation résolue : `r.demarrerTraitement(); r.marquerResolue();` (transitions contrôlées, partie 2).
- Pour le bonus, utilisez `ZonedDateTime.of(...)` avec `ZoneId.of("Europe/Paris")` — et non `LocalDateTime`, qui ignore les fuseaux.

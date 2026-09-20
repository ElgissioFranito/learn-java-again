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

        // ============ 9. Formater et relire (parse) ============
        System.out.println("9) ISO_LOCAL_DATE -> " + jour.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE));
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


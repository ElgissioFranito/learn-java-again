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

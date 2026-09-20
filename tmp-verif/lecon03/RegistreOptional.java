import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

// Version "Optional" du registre : findById ne renvoie plus null mais Optional<Reclamation>
public class RegistreOptional {

    private final List<Reclamation> toutes = new ArrayList<>();
    private final Map<Integer, Reclamation> parId = new HashMap<>();
    private int prochainId = 1;

    public Reclamation ajouter(String description, String quartier, Priorite priorite) {
        Reclamation r = new Reclamation(prochainId, description, quartier, priorite);
        prochainId++;
        toutes.add(r);
        parId.put(r.getId(), r);
        return r;
    }

    // LA signature qui change tout : l'absence est DANS le type de retour
    public Optional<Reclamation> findById(int id) {
        return Optional.ofNullable(parId.get(id));
    }

    public Optional<Reclamation> premiereUrgente() {
        for (Reclamation r : toutes) {
            if (r.getPriorite() == Priorite.URGENTE) {
                return Optional.of(r);
            }
        }
        return Optional.empty();
    }

    public long compterResolues() {
        long total = 0;
        for (Reclamation r : toutes) {
            if (r.estResolue()) {
                total++;
            }
        }
        return total;
    }

    public List<Reclamation> toutes() {
        return List.copyOf(toutes);
    }
}

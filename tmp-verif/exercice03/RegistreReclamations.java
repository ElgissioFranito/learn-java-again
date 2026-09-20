import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

// Correction de l'exercice 03 : le registre de l'exercice 01, mais avec Optional
public class RegistreReclamations {

    private final List<Reclamation> toutes = new ArrayList<>();
    private final Map<Integer, Reclamation> parId = new HashMap<>();
    private final Map<String, List<Reclamation>> parQuartier = new HashMap<>();
    private int prochainId = 1;

    public Reclamation ajouter(String description, String quartier, Priorite priorite) {
        Reclamation r = new Reclamation(prochainId, description, quartier, priorite);
        prochainId++;
        toutes.add(r);
        parId.put(r.getId(), r);
        List<Reclamation> duQuartier = parQuartier.get(quartier);
        if (duQuartier == null) {
            duQuartier = new ArrayList<>();
            parQuartier.put(quartier, duQuartier);
        }
        duQuartier.add(r);
        return r;
    }

    // ETAPE 1 : l'absence est desormais dans le type de retour
    public Optional<Reclamation> findById(int id) {
        return Optional.ofNullable(parId.get(id));
    }

    // ETAPE 2a : la premiere urgence, ou rien
    public Optional<Reclamation> premiereUrgente() {
        for (Reclamation r : toutes) {
            if (r.getPriorite() == Priorite.URGENTE) {
                return Optional.of(r);
            }
        }
        return Optional.empty();        // "rien trouve" est un cas NORMAL, pas une erreur
    }

    // ETAPE 2b : la premiere du quartier, ou rien
    public Optional<Reclamation> premiereDuQuartier(String quartier) {
        List<Reclamation> liste = parQuartier.get(quartier);
        if (liste == null || liste.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(liste.get(0));
    }

    public List<Reclamation> toutes() {
        return List.copyOf(toutes);
    }

    public int taille() {
        return toutes.size();
    }
}

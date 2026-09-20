// Correction de l'exercice 03 : le service qui traduit l'absence en exception METIER
public class ServiceReclamations {

    private final RegistreReclamations registre;

    public ServiceReclamations(RegistreReclamations registre) {
        this.registre = registre;
    }

    // ETAPE 3 : l'absence devient une exception expliquee
    public Reclamation recupererObligatoirement(int id) {
        return registre.findById(id)
                .orElseThrow(() -> new IllegalStateException("Reclamation " + id + " introuvable"));
    }

    // ETAPE 4a : map + orElse (aucun if)
    public String descriptionOuInconnue(int id) {
        return registre.findById(id).map(r -> r.getDescription()).orElse("inconnue");
    }

    // ETAPE 4b : filter (existe ET urgente)
    public boolean estUrgente(int id) {
        return registre.findById(id)
                .filter(r -> r.getPriorite() == Priorite.URGENTE)
                .isPresent();
    }

    // ETAPE 4c : flatMap (la recherche suivante rend elle-meme un Optional)
    public int idSuivant(int id) {
        return registre.findById(id)
                .flatMap(r -> registre.findById(r.getId() + 1))
                .map(r -> r.getId())
                .orElse(-1);
    }

    // ETAPE 4d : ifPresent (agir sans extraire)
    public void demarrerSiPresente(int id) {
        registre.findById(id).ifPresent(r -> r.demarrerTraitement());
    }
}

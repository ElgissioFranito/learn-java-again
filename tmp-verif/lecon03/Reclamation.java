// Fichier : Reclamation.java  (version partie 3, sans notion de date pour l'instant)
public class Reclamation {

    private final int id;
    private final String description;
    private final String quartier;
    private final Priorite priorite;
    private StatutReclamation statut;

    public Reclamation(int id, String description, String quartier, Priorite priorite) {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("La description est obligatoire");
        }
        this.id = id;
        this.description = description;
        this.quartier = quartier;
        this.priorite = priorite;
        this.statut = StatutReclamation.NOUVELLE;
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

    public String getBadge() {
        return switch (statut) {
            case NOUVELLE -> "NOUVELLE";
            case EN_COURS -> "EN_COURS";
            case RESOLUE  -> "RESOLUE";
        };
    }

    public String getLigneAffichage() {
        return "#" + id + " [" + statut + "] " + description
                + " (" + quartier + ", " + priorite + ")";
    }

    public int getId()                    { return id; }
    public String getDescription()        { return description; }
    public String getQuartier()           { return quartier; }
    public Priorite getPriorite()         { return priorite; }
    public StatutReclamation getStatut()  { return statut; }
}

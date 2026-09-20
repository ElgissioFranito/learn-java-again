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

    public String getLigneAffichage() {
        return "#" + id + " [" + statut + "] " + description
                + " (" + quartier + ", " + priorite + ")";
    }

    public int getId()                    { return id; }
    public String getDescription()        { return description; }
    public String getQuartier()           { return quartier; }
    public Priorite getPriorite()         { return priorite; }
    public LocalDateTime getDateDeclaration() { return dateDeclaration; }
    public StatutReclamation getStatut()  { return statut; }
}

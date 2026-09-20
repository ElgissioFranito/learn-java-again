// Fichier : StatutReclamation.java  (inchange depuis la partie 2)
public enum StatutReclamation {

    NOUVELLE("Nouvelle demande"),
    EN_COURS("En cours de traitement"),
    RESOLUE("Réclamation résolue");

    private final String libelle;

    private StatutReclamation(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() { return libelle; }
}

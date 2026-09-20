// Fichier : Priorite.java  (repris de la partie 2, enrichi d'une duree pour la lecon 04)
import java.time.Duration;

public enum Priorite {

    BASSE(168),     // 1 semaine
    NORMALE(48),    // 2 jours
    URGENTE(4);     // 4 heures

    private final int delaiHeuresMax;

    private Priorite(int delaiHeuresMax) {
        this.delaiHeuresMax = delaiHeuresMax;
    }

    public int getDelaiHeuresMax() { return delaiHeuresMax; }

    public Duration delaiMax() {            // la meme information, en objet Duration
        return Duration.ofHours(delaiHeuresMax);
    }
}

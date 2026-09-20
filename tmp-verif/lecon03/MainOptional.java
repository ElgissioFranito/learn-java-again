import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

public class MainOptional {

    // L'ancienne facon (celle de l'exercice 01) : renvoyer null quand rien n'est trouve
    private static Reclamation findByIdSansOptional(RegistreOptional registre, int id) {
        for (Reclamation r : registre.toutes()) {
            if (r.getId() == id) {
                return r;
            }
        }
        return null;                    // rien a signaler a l'appelant... sinon une convention orale
    }

    // Une methode "avec effet de bord visible" : elle affiche quand elle est appelee
    private static String descriptionParDefaut() {
        System.out.println("      >> calcul de la description par defaut (elle a bien ete appelee)");
        return "Reclamation inconnue";
    }

    public static void main(String[] args) {

        RegistreOptional registre = new RegistreOptional();
        registre.ajouter("Nid de poule", "Medina", Priorite.NORMALE);        // #1
        registre.ajouter("Lampadaire éteint", "Plateau", Priorite.URGENTE);  // #2
        registre.ajouter("Poubelles non ramassées", "Medina", Priorite.BASSE); // #3

        // ============ 1. Le probleme d'avant : le null qui explose loin de sa source ============
        try {
            String description = findByIdSansOptional(registre, 99).getDescription();
            System.out.println("1) " + description);
        } catch (NullPointerException e) {
            System.out.println("1) AVANT : NullPointerException (le null vient du findById, "
                    + "l'erreur explose ici)");
        }

        // ============ 2. Les trois facons de creer un Optional ============
        Optional<Reclamation> present = Optional.of(registre.toutes().get(0));
        Optional<Reclamation> peutEtre = Optional.ofNullable(findByIdSansOptional(registre, 99));
        Optional<Reclamation> vide = Optional.empty();
        System.out.println("2) of -> present=" + present.isPresent()
                + " | ofNullable(null) -> present=" + peutEtre.isPresent()
                + " | empty -> present=" + vide.isPresent());
        try {
            Optional.of(null);           // LE piege de creation
        } catch (NullPointerException e) {
            System.out.println("   PIEGE : Optional.of(null) leve immediatement un NullPointerException");
        }

        // ============ 3. Lire : isPresent/get (a eviter) puis orElse ============
        Optional<Reclamation> trouvee1 = registre.findById(1);
        if (trouvee1.isPresent()) {
            System.out.println("3) isPresent + get -> " + trouvee1.get().getDescription());
        }
        System.out.println("   orElse -> " + registre.findById(99).map(r -> r.getDescription())
                .orElse("aucune reclamation"));

        // ============ 4. map : transformer la valeur SEULEMENT si elle est la ============
        System.out.println("4) map sur #2 -> " + registre.findById(2)
                .map(r -> r.getDescription()).orElse("aucune"));
        System.out.println("   map sur #99 -> " + registre.findById(99)
                .map(r -> r.getDescription()).orElse("aucune"));

        // ============ 5. filter : ne garder la valeur que si elle passe un test ============
        System.out.println("5) #2 est URGENTE ? " + registre.findById(2)
                .filter(r -> r.getPriorite() == Priorite.URGENTE).isPresent());
        System.out.println("   #1 est URGENTE ? " + registre.findById(1)
                .filter(r -> r.getPriorite() == Priorite.URGENTE).isPresent());

        // ============ 6. orElse (avide) contre orElseGet (paresseux) ============
        String avecOrElse = registre.findById(1)
                .map(r -> r.getDescription())
                .orElse(descriptionParDefaut());               // appelee MEME si present
        System.out.println("6) orElse(...)    -> " + avecOrElse);

        String avecOrElseGet = registre.findById(1)
                .map(r -> r.getDescription())
                .orElseGet(() -> descriptionParDefaut());      // appelee SEULEMENT si vide
        System.out.println("   orElseGet(...) -> " + avecOrElseGet
                + "  (aucun calcul ci-dessus : la methode n'a pas ete appelee)");

        // ============ 7. orElseThrow : transformer l'absence en exception explicite ============
        try {
            Reclamation introuvable = registre.findById(99)
                    .orElseThrow(() -> new IllegalStateException("Reclamation 99 introuvable"));
            System.out.println("7) " + introuvable.getDescription());
        } catch (IllegalStateException e) {
            System.out.println("7) orElseThrow(() -> ...) -> " + e.getClass().getSimpleName()
                    + " : " + e.getMessage());
        }
        try {
            registre.findById(99).orElseThrow();              // variante Java 10, sans argument
        } catch (NoSuchElementException e) {
            System.out.println("   orElseThrow() sans argument -> "
                    + e.getClass().getSimpleName() + " (message technique, sans explication metier)");
        }

        // ============ 8. ifPresent / ifPresentOrElse : agir sans extraire la valeur ============
        registre.findById(2).ifPresent(r -> System.out.println("8) ifPresent -> #" + r.getId()
                + " " + r.getBadge()));
        registre.findById(99).ifPresentOrElse(
                r -> System.out.println("   ifPresentOrElse -> present"),
                () -> System.out.println("8) ifPresentOrElse -> absent : on journalise une alerte"));

        // ============ 9. Une methode du registre qui renvoie Optional ============
        System.out.println("9) premiereUrgente -> " + registre.premiereUrgente()
                .map(r -> r.getDescription()).orElse("aucune urgence"));
        System.out.println("   compterResolues() -> " + registre.compterResolues());

        // ============ 10. map contre flatMap (eviter Optional<Optional<...>>) ============
        Optional<Optional<Reclamation>> imbrique = registre.findById(1)
                .map(r -> registre.findById(r.getId() + 1));        // la lambda rend un Optional !
        System.out.println("10) map     -> type Optional<Optional<Reclamation>>, present="
                + imbrique.isPresent());

        Optional<Reclamation> aplati = registre.findById(1)
                .flatMap(r -> registre.findById(r.getId() + 1));    // flatMap APLANIT
        System.out.println("    flatMap -> " + aplati.map(r -> r.getDescription()).orElse("aucune"));

        // ============ 11. Optional<List<T>> est un anti-pattern ============
        List<Reclamation> duQuartier = List.of();      // une liste vide dit deja "aucune"
        System.out.println("11) liste vide -> isEmpty()=" + duQuartier.isEmpty()
                + " : inutile d'emballer ce cas dans un Optional");
    }
}


import java.util.Optional;

public class MainService {

    // Etape 5 : une methode qui rend son appel VISIBLE
    private static String descriptionParDefaut() {
        System.out.println("      >> (calcul effectue)");
        return "inconnue";
    }

    public static void main(String[] args) {

        RegistreReclamations registre = new RegistreReclamations();
        ServiceReclamations service = new ServiceReclamations(registre);

        registre.ajouter("Nid de poule", "Medina", Priorite.NORMALE);          // #1
        registre.ajouter("Lampadaire éteint", "Plateau", Priorite.URGENTE);    // #2
        registre.ajouter("Poubelles non ramassées", "Medina", Priorite.BASSE); // #3

        // ============ 1. Les trois recherches renvoient Optional ============
        System.out.println("1) findById(2) present ? " + service.recupererObligatoirement(2).getId()
                + " | findById(42) present ? " + registre.findById(42).isPresent());
        System.out.println("   premiereUrgente -> "
                + registre.premiereUrgente().map(r -> r.getDescription()).orElse("aucune"));
        System.out.println("   premiereDuQuartier(\"Medina\") -> "
                + registre.premiereDuQuartier("Medina").map(r -> r.getDescription()).orElse("aucune"));
        System.out.println("   premiereDuQuartier(\"Fann\") -> "
                + registre.premiereDuQuartier("Fann").map(r -> r.getDescription()).orElse("aucune"));

        // ============ 2. L'absence traduite en exception METIER ============
        try {
            service.recupererObligatoirement(42);
        } catch (IllegalStateException e) {
            System.out.println("2) recupererObligatoirement(42) -> " + e.getClass().getSimpleName()
                    + " : " + e.getMessage());
        }

        // ============ 3. Manipuler l'absence sans if ============
        System.out.println("3) descriptionOuInconnue(1)  -> " + service.descriptionOuInconnue(1));
        System.out.println("   descriptionOuInconnue(42) -> " + service.descriptionOuInconnue(42));
        System.out.println("   estUrgente(2) -> " + service.estUrgente(2)
                + " | estUrgente(1) -> " + service.estUrgente(1)
                + " | estUrgente(42) -> " + service.estUrgente(42));
        System.out.println("   idSuivant(1) -> " + service.idSuivant(1)
                + " | idSuivant(3) -> " + service.idSuivant(3));
        service.demarrerSiPresente(3);
        service.demarrerSiPresente(42);          // rien ne se passe : cas normal
        System.out.println("   apres demarrerSiPresente(3) : statut #3 = "
                + registre.findById(3).map(r -> r.getStatut().toString()).orElse("?")
                + " | #1 inchange = " + registre.findById(1).map(r -> r.getStatut().toString()).orElse("?"));

        // ============ 4. Etape 5 : orElse (avide) contre orElseGet (paresseux) ============
        String avecOrElse = registre.findById(1).map(r -> r.getDescription())
                .orElse(descriptionParDefaut());
        System.out.println("4) orElse(...)    -> " + avecOrElse);

        String avecOrElseGet = registre.findById(1).map(r -> r.getDescription())
                .orElseGet(() -> descriptionParDefaut());
        System.out.println("   orElseGet(...) -> " + avecOrElseGet
                + "  (aucun \"(calcul effectue)\" ci-dessus : orElseGet n'a pas appele la methode)");

        // ============ 5. Bonus : les deux erreurs provoquées ============
        try {
            Optional.of(null);
        } catch (NullPointerException e) {
            System.out.println("5) Optional.of(null) -> " + e.getClass().getSimpleName()
                    + " (au moment de la CREATION)");
        }
        try {
            registre.findById(42).orElseThrow();
        } catch (java.util.NoSuchElementException e) {
            System.out.println("   orElseThrow() sans argument -> " + e.getClass().getSimpleName()
                    + " : " + e.getMessage());
        }
    }
}

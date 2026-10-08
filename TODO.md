# TODO — Bloc Partie 07 : Spring Boot et injection de dependances

> Formateur : Java expert, clair, patient et pedagogique. Public : autodidacte francophone debutant qui apprend serieusement.
> Regle d'or : zero etonnement. Chaque terme defini a sa premiere apparition. Chaque transition expliquee. Analogie simple pour chaque concept abstrait. Pourquoi + comment (le plus important) + quand.

## Methode de travail imposee par l'apprenant

- [ ] Avancer **une lecon par une lecon**, dans l'ordre 01 -> 12.
- [ ] Apres chaque lecon terminee, **demander explicitement la validation** avant de passer a la suivante.
- [ ] `00-Introduction-partie.md` ecrit **a la fin**, pour y mettre uniquement le vocabulaire transversal non explique dans les sous-lecons (filet de rattrapage, pas de doublon).
- [ ] Chaque dossier = au moins 3 fichiers : `01-lecon.md`, `02-exercice.md`, `03-correction.md`.
- [ ] Chaque correction **executee en dossier temporaire** et verifiee avant livraison.
- [ ] Chemins **relatifs uniquement**. Nommage sans accents ni apostrophes.
- [ ] Projet fil rouge : creer et faire evoluer `signalcua-spring-boot` (API REST de signalement citoyen : voirie, ordures, eclairage, corruption... geree par agents CUA) des la lecon 02.

## Structure obligatoire de chaque `01-lecon.md`

1. Titre + encart `Pont depuis...` et `Prochaine etape` en fin.
2. `1. Objectifs d'apprentissage` (3 a 6 objectifs concrets).
3. `2. Explication simple` (progressive, analogies, pourquoi/comment/quand).
4. `Vocabulaire / Abreviations` (mini-glossaire avant exemples, 1 ligne par terme).
5. `3. Exemples concrets` (code commente ligne par ligne, copiable, teste).
6. `4. Bonnes pratiques modernes (2025-2026)` (Boot 3.x, Jakarta `jakarta.*`, Java 21).
7. `5. Pieges a eviter` (mauvais exemple + pourquoi + version correcte).
8. `Checklist de validation` + `Fil rouge — ou en est SignalCUA ?`.
9. Liaisons : chaque section se raccroche a la precedente par une phrase. Rien ne surgit sans explication.

## Structure `02-exercice.md` / `03-correction.md`

- `02-exercice.md` : objectif, socle de depart rappele, enonce progressif, criteres cochables, indications repliees.
- `03-correction.md` : articulation avec 01 et 02, pas a pas, choix expliques, preuve d'execution, erreurs frequentes, checklist + conseils.

<!--SUITE-->
## Decoupage et volumetrie (le clair prime sur le constant)
| Ordre | Dossier | Roadmap | Socle essentiel | Secondaire (a voir) | Taille |
|---|---|---|---|---|---|
| 1 | 01-pourquoi-un-framework | 7.1 | plomberie new, declaration + creation auto, IoC en une phrase | aucun comparatif detaille | ~290 lignes (conceptuel, verifie) |
| 2 | 02-premier-projet-springboot | 7.1-bis pont | Initializr Maven/Java 21/Jar/web, SpringBootApplication, spring-boot:run, port 8080, contextLoads | deploy jar, native, Docker | ~500 lignes + init projet |
| 3 | 03-ioc-et-di | 7.2 | bean, conteneur IoC, injection constructeur, singleton par defaut | prototype/request, circulaire = revoir decoupage | ~350 lignes |
| 4 | 04-stereotypes-spring | 7.3 | Component base, Service/Repository/Controller intentions | details AOP/proxy | ~220 lignes |
| 5 | 05-controleurs-rest | 7.4 | RestController, GetMapping/PostMapping, PathVariable/RequestBody, 200/201/404, curl | pagination, HATEOAS | ~550 lignes |
| 6 | 06-validation-des-entrees | 7.5 | NotBlank/Size, Valid, 400 auto | validateurs custom cites | ~350 lignes |
| 7 | 07-gestion-centralisee-des-erreurs | 7.6 | ControllerAdvice, ProblemDetail RFC 7807, mapping 400/404/409 | RFC complete | ~500 lignes |
| 8 | 08-services-et-architecture-en-couches | 7.7 | Controller -> Service -> Repository, Fat Controller interdit | transactions = partie 8 | ~450 lignes |
| 9 | 09-dtos-et-mapping-entity-dto | 7.8 | record DTO entree/sortie, entite jamais exposee, mapping manuel | MapStruct = partie 13 | ~450 lignes |
| 10 | 10-configuration-et-profils | 7.9 | application.properties, Value, profils, secrets hors Git | config server | ~350 lignes |
| 11 | 11-spring-boot-actuator | 7.10 | starter-actuator, /actuator/health | metriques = partie 13 | ~200 lignes |
| 12 | 12-cors-frontend-angular | 7.11 | CORS navigateur, config globale vs proxy, * interdit en prod | OAuth/CSP | ~250 lignes |
| FIN | 00-Introduction-partie.md | transversal | pont partie 6 -> 7, tableau 12 lecons, vocabulaire residuel | — | ~180 lignes |

Total estime : ~4300-4800 lignes Markdown + projet fil rouge.

## Fil rouge SignalCUA — evolution incrementale (jamais de big-bang)

- Lecon 02 : projet signalcua-spring-boot genere, demarre, repond.
- Lecons 03-04 : RegistreReclamations + ServiceDelais deviennent beans.
- Lecon 05 : ReclamationController avec GET /reclamations, GET /reclamations/{id}.
- Lecon 06 : POST /reclamations avec Valid.
- Lecon 07 : exceptions partie 4 -> ProblemDetail JSON.
- Lecon 08 : decoupage final + PUT /reclamations/{id}/statut.
- Lecon 09 : DTO record en entree/sortie.
- Lecons 10-12 : profils dev, health, appel Angular via proxy puis CORS.
- Annonce des lecon 08 : partie 8 remplacera le repository memoire par JPA sans toucher controller/service.

## Vigilances 2025-2026 (a marteler)

- Boot 3.x + Java 21 + jakarta.* (jamais javax.*). Tutoriels pre-2023 perimes sur ce point.
- Injection par constructeur uniquement. Autowired sur champ = anti-pattern.
- record pour DTO, pas pour entite JPA.
- ProblemDetail au lieu de Map maison.
- application.properties par defaut. yml en equivalence seulement en lecon 10.
- Pas de Lombok / MapStruct / JPA / Testcontainers en partie 7 : cites en A voir, non utilises.

## Definition de fini (chaque lecon)

- [ ] 3 fichiers crees avec transitions et glossaire.
- [ ] Code commente ligne par ligne, teste en dossier temporaire.
- [ ] README.md du dossier mis a jour (liens vers les 3 fichiers).
- [ ] Projet signalcua-spring-boot avance d'un pas (depuis lecon 02).
- [ ] Validation de l'apprenant demandee avant lecon suivante.

## Suivi

- [x] TODO.md cree.
- [x] 01-pourquoi-un-framework (terminee, verifiee JDK 21).
- [ ] 02-premier-projet-springboot.
- [ ] 03-ioc-et-di.
- [ ] 04-stereotypes-spring.
- [ ] 05-controleurs-rest.
- [ ] 06-validation-des-entrees.
- [ ] 07-gestion-centralisee-des-erreurs.
- [ ] 08-services-et-architecture-en-couches.
- [ ] 09-dtos-et-mapping-entity-dto.
- [ ] 10-configuration-et-profils.
- [ ] 11-spring-boot-actuator.
- [ ] 12-cors-frontend-angular.
- [ ] 00-Introduction-partie.md (en dernier).
- [ ] Cloture : README partie 07, lecons/README.md, fil-rouge-signalcua.md Etape 7.

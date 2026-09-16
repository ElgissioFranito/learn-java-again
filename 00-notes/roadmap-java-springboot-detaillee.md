# Roadmap détaillée Java → Spring Boot (dev Angular → Java)

> Version enrichie : chapitres → sous-chapitres détaillés, projet **fil rouge** pour pratiquer en continu, et une partie dédiée à ce que la roadmap officielle (roadmap.sh) **ne mentionne pas** mais que vous croiserez presque certainement.

---

## 0. Le projet fil rouge : **SignalCUA**

Pour éviter d'apprendre du Java "hors sol", tout ce guide s'appuie sur **un seul projet que vous faites évoluer chapitre après chapitre** :

> **SignalCUA** — une mini-application de gestion des réclamations citoyennes (voirie, propreté, éclairage public...) pour la commune. Un citoyen signale un problème, un agent le traite, un statut évolue (`NOUVELLE` → `EN_COURS` → `RESOLUE`).

C'est volontairement proche de vos vrais projets CUA (état civil, dashboard administratif) pour que les réflexes que vous prenez ici se transfèrent directement.

Le projet traverse 4 grandes phases au fil des chapitres :
1. **Phase console** (chap. 1-5) : Java pur, pas de framework, tout en mémoire.
2. **Phase Maven** (chap. 6) : structuration en vrai projet buildable.
3. **Phase API** (chap. 7-9) : Spring Boot + base de données + tests.
4. **Phase production** (chap. 10-12) : async, logs, sécurité.

Chaque partie ci-dessous se termine par un bloc **🔴 Fil rouge** qui vous dit exactement quoi coder à cette étape.

---

## Partie 1 — Bases du langage

### 1.1 Syntaxe, types, variables, opérateurs

**Essentiels**
- Types primitifs (`int`, `long`, `double`, `boolean`, `char`) vs types objets (`Integer`, `Long`, `Boolean`...) et l'auto-boxing/unboxing.
- Déclaration/initialisation, `final` (référence immuable, pas l'objet).
- Opérateurs arithmétiques, logiques, ternaire (`? :`).
- Conversion de types (`casting`) implicite (élargissement) vs explicite (rétrécissement, perte de précision possible).

**Erreurs à éviter**
- Division entière qui tronque (`5 / 2 == 2`, pas `2.5`).
- Comparer `Integer`/`String` avec `==` au lieu de `.equals()`.
- Overflow silencieux sur `int` (pas d'exception, juste un résultat faux) → utilisez `long` si le volume peut dépasser ~2 milliards.
- Utiliser `double`/`float` pour de l'argent → `BigDecimal`.

**Anti-patterns**
- "Stringly typed" : coder un statut, une devise, un rôle en `String` libre au lieu d'un `enum`.
- Abus de `var` qui rend le code illisible (contrairement à TS, le typage explicite est très valorisé en Java d'entreprise).

**Conseils de pro**
- Activez les warnings du compilateur dès le premier jour, ne les ignorez jamais.
- Prenez l'habitude native de vous demander "est-ce que cette valeur peut être `null` ?" à chaque déclaration — c'est LE réflexe n°1 à construire en Java (bien plus critique qu'en TS avec `strictNullChecks`).

**Quand l'utiliser** : base obligatoire, partout, tout le temps.

**Bonnes pratiques 2025-2026**
- Java 21/25 LTS : `var` accepté pour les variables locales évidentes (`var list = new ArrayList<String>()`) mais pas pour les paramètres/retours de méthode publics.
- Text blocks (`"""`) pour toute chaîne multi-lignes (requêtes SQL, JSON de test).

---

### 1.2 Structures de contrôle (conditions, boucles)

**Essentiels**
- `if/else`, `switch` classique et `switch` expression moderne (`->`, Java 14+).
- `for`, `for-each`, `while`, `do-while`.
- `break`/`continue`, labels (rarement utilisés mais bon à reconnaître).

**Erreurs à éviter**
- `switch` sans `break` (fallthrough non voulu) — utilisez la syntaxe moderne `switch (x) { case A -> ...; }` qui élimine ce piège.
- Boucles `for` à index avec conditions limites mal calculées (`<=` au lieu de `<`).
- Modifier la collection qu'on est en train de parcourir avec un `for-each` classique.

**Anti-patterns**
- Imbrication de `if` sur plus de 3 niveaux → extrayez des méthodes ou utilisez des **early returns** (`if (!valide) return;` en début de méthode).

**Conseils de pro**
- Préférez le `switch` expression moderne : plus sûr (le compilateur vérifie l'exhaustivité avec les `enum`/`sealed`), plus concis.

**Quand l'utiliser** : base obligatoire.

**Bonnes pratiques 2025-2026**
- Pattern matching dans `switch` (Java 21+) : `switch (obj) { case Reclamation r when r.urgente() -> ...; }` — très proche d'un `match` façon Kotlin/Rust.

**🔍 À voir, pas à apprendre maintenant** : rien à ce niveau, c'est la base pure.

---

### 1.3 Tableaux et Strings

**Essentiels**
- Tableaux à taille fixe (`int[] tab = new int[5]`) vs `ArrayList` (dynamique) — voir partie 3.
- `String` immuable : chaque `+` crée une nouvelle instance.
- Méthodes courantes : `.substring()`, `.split()`, `.trim()`, `.format()`, `.contains()`.
- `StringBuilder` pour construire une chaîne en boucle.

**Erreurs à éviter**
- Concaténer avec `+` dans une boucle (complexité O(n²)) → `StringBuilder`.
- Oublier que `.split()` avec une regex peut avoir des comportements surprenants (chaînes vides en fin de résultat par exemple).
- `String.equals()` sensible à la casse — pensez `.equalsIgnoreCase()` quand pertinent (ex: comparaison d'email).

**Anti-patterns**
- Parser des données structurées (CSV, JSON) "à la main" avec `.split()` au lieu d'utiliser une lib dédiée (OpenCSV, Jackson).

**Conseils de pro**
- Pour du texte multi-lignes ou du templating simple, les **text blocks** remplacent avantageusement la concaténation.

**Quand l'utiliser** : tableaux → tailles fixes connues (rare en pratique moderne) ; `ArrayList` → 95% des cas réels.

**Bonnes pratiques 2025-2026**
- `String.formatted()` (Java 15+) plus lisible que `String.format()` classique.

---

### 1.4 Introduction aux classes et méthodes

**Essentiels**
- Déclarer une classe, un constructeur, des champs, des méthodes.
- `this`, surcharge de méthode (overloading).
- Cycle de vie basique d'un objet (création, garbage collection — pas de `delete` manuel comme en C++).

**Erreurs à éviter**
- Constructeur qui fait trop (validation complexe, appels réseau) → gardez les constructeurs simples.
- Oublier de valider les paramètres du constructeur (accepter un `id` négatif, un `nom` vide...).

**Anti-patterns**
- Classes "boîte à outils" statiques abusives (`Utils`, `Helper` fourre-tout) — souvent signe qu'un concept métier n'a pas été bien modélisé en objet.

**Conseils de pro**
- Donnez à vos classes des noms métier clairs (`Reclamation`, pas `Data` ou `Item`) — ça paraît évident mais c'est la base d'un code lisible en équipe (ou pour vous-même 6 mois plus tard, vu que vous travaillez souvent seul).

**Quand l'utiliser** : base obligatoire.

**Bonnes pratiques 2025-2026** : voir 2.4 (records) — pour beaucoup de classes simples, un record remplace avantageusement une classe classique.

---

### 1.5 Variables de classe (static) & constantes

**Essentiels**
- `static` sur un champ = **variable de classe** : une seule copie partagée par toutes les instances (contrairement aux champs d'instance, propres à chaque objet).
- `static final` = constante de classe (ex: `public static final int MAX_URGENCE = 5;`).
- Méthodes `static` : appelables sans instance (`Reclamation.compter()`), pas d'accès à `this`.
- Bloc d'initialisation statique (`static { ... }`) exécuté une seule fois au chargement de la classe.

**Erreurs à éviter**
- Utiliser des champs `static` mutables pour du "state" partagé (ex: un compteur global) → source de bugs difficiles en environnement multi-thread (voir partie 10) et casse la testabilité.
- Confondre variable de classe et variable d'instance dans une même classe sans s'en rendre compte (piège classique pour un débutant venant de JS où les champs sont d'instance par défaut).

**Anti-patterns**
- Classes "singleton maison" avec des champs `static` partout pour simuler un état global — en Spring Boot, un bean `@Component` en scope singleton fait déjà ce travail proprement (voir partie 7).

**Conseils de pro**
- Réservez `static` aux vraies constantes et aux méthodes utilitaires pures (sans état), pas au partage d'état applicatif.

**Quand l'utiliser** : constantes (`MAX_URGENCE`), compteurs de classe, méthodes utilitaires pures (`Math.max()` est l'exemple canonique du JDK).

---

### 1.6 Packages & organisation du code

**Essentiels**
- `package fr.cua.signalcua.model;` en première ligne du fichier — reflète l'arborescence de dossiers.
- `import` pour utiliser des classes d'un autre package.
- Visibilité package-private (aucun modificateur) : visible seulement dans le même package.
- Convention de nommage inversée du nom de domaine (`fr.cua.xxx`, comme `com.google.xxx`).

**Erreurs à éviter**
- Tout mettre dans un seul package fourre-tout (`fr.cua.signalcua` avec 50 classes à plat) → organisez par couche (`.controller`, `.service`, `.repository`, `.model`/`.entity`, `.dto`, `.exception`).
- Dépendances circulaires entre packages (package A qui dépend de B qui dépend de A) — signe d'un mauvais découpage.

**Conseils de pro**
- Pensez-y comme l'équivalent de l'organisation en dossiers `modules/`, `services/`, `components/` en Angular — les mêmes réflexes de séparation par responsabilité s'appliquent.

**Quand l'utiliser** : dès que le projet dépasse une poignée de classes — donc quasiment tout de suite en pratique.

---

### 1.7 Lecture des entrées utilisateur (Scanner)

**Essentiels**
- `Scanner sc = new Scanner(System.in);` puis `.nextLine()`, `.nextInt()`, `.nextDouble()`.
- Toujours fermer le `Scanner` (ou le réutiliser, ne pas en recréer plusieurs sur `System.in`).

**Erreurs à éviter**
- Mélanger `.nextInt()` et `.nextLine()` sans comprendre que `.nextInt()` ne consomme pas le retour à la ligne → la ligne suivante lue est vide (piège très classique pour les débutants).
- Ne pas valider/gérer les erreurs de saisie (`InputMismatchException` si l'utilisateur tape du texte au lieu d'un nombre).

**Conseils de pro**
- Utile uniquement pour des programmes console d'apprentissage ou des petits scripts/CLI internes — une vraie application Spring Boot ne lit jamais l'entrée clavier, elle reçoit des requêtes HTTP (partie 7). Ne passez pas trop de temps dessus, c'est une étape transitoire.

**Quand l'utiliser** : exercices d'apprentissage en console, petits outils CLI internes (ex: un script d'import ponctuel).

**🔍 À voir, pas à approfondir** : au-delà de `Scanner`, il existe `BufferedReader`/`System.console()` pour des besoins plus avancés (mot de passe masqué, performance) — pas nécessaire pour débuter.

---

### 🔴 Fil rouge — Étape 1 : la classe `Reclamation` en Java pur

Objectif : un petit programme `main()` qui, sans aucun framework :
- Définit une classe `Reclamation` (id, description, quartier, statut en `String` pour l'instant — on corrigera avec un `enum` en partie 2).
- Crée quelques réclamations "à la main" dans un tableau.
- Les affiche avec une boucle `for-each`.
- Calcule et affiche le nombre de réclamations par quartier avec une concaténation via `StringBuilder`.

Ce que ça vous fait pratiquer : syntaxe, classes basiques, tableaux, boucles, strings — sans le vertige de tout apprendre en même temps.

---

## Partie 2 — Programmation orientée objet

### 2.1 Encapsulation & constructeurs

**Essentiels**
- Champs `private`, accès via getters/setters (ou records — voir 2.4).
- Constructeurs multiples (overloading), constructeur par défaut.
- `this()` pour chaîner les constructeurs entre eux.

**Erreurs à éviter**
- Générer des getters/setters pour absolument tous les champs par réflexe (IDE) sans se demander si le champ doit vraiment être modifiable de l'extérieur.
- Setters qui ne valident rien (permettent de mettre l'objet dans un état incohérent).

**Anti-patterns**
- **Anemic Domain Model** : classes qui ne sont que des sacs de getters/setters sans aucune règle métier — très fréquent en Spring Boot mal conçu, où toute la logique finit "en vrac" dans les services.

**Conseils de pro**
- Posez-vous la question à chaque champ : "quelqu'un a-t-il vraiment besoin de le modifier après création ?" Si non → pas de setter, champ `final`.

**Quand l'utiliser** : base obligatoire.

**Bonnes pratiques 2025-2026** : privilégiez l'immutabilité par défaut (champs `final`, pas de setters) sauf besoin explicite de mutation.

---

### 2.2 Héritage & polymorphisme

**Essentiels**
- `extends`, `super()`, override de méthode, `@Override`.
- Polymorphisme : une variable de type parent peut référencer un objet enfant, et la bonne méthode est appelée dynamiquement.
- `protected` vs `private` vs `public` vs package-private (aucun modificateur).

**Erreurs à éviter**
- Héritage sur plus de 2-3 niveaux → devient vite ingérable.
- Oublier `@Override` (aucune erreur de compilation si vous faites une faute de frappe dans le nom de méthode — la "surcharge" silencieuse est un piège classique).
- Appeler une méthode overridable depuis le constructeur du parent (comportement surprenant si la classe enfant n'est pas encore totalement initialisée).

**Anti-patterns**
- Héritage utilisé uniquement pour réutiliser du code (au lieu de composition — "favor composition over inheritance" est un principe central en Java d'entreprise).
- Hiérarchies qui ne respectent pas le principe de substitution de Liskov (une classe enfant qui casse le contrat de la classe parent).

**Conseils de pro**
- Règle simple : héritage seulement pour une vraie relation "est-un" stable dans le temps (`ReclamationVoirie` **est une** `Reclamation`). Pour "a un comportement de", préférez une interface.

**Quand l'utiliser** : hiérarchies de types métier réellement stables. Rare en pratique moderne — la composition + interfaces couvre la majorité des besoins.

**Bonnes pratiques 2025-2026** : les **sealed classes** (2.4) remplacent souvent avantageusement l'héritage libre pour modéliser un ensemble fermé de sous-types.

---

### 2.3 Interfaces & classes abstraites

**Essentiels**
- Interface = contrat pur (+ méthodes `default`/`static` depuis Java 8).
- Classe abstraite = base partielle avec état + comportement, ne peut pas être instanciée.
- **Méthode abstraite** (`abstract void traiter();`) : déclarée sans corps dans une classe abstraite, **obligatoirement** implémentée par chaque sous-classe concrète — c'est le mécanisme qui force une hiérarchie à respecter un contrat commun tout en laissant chaque enfant définir sa propre logique.
- Une classe implémente plusieurs interfaces mais n'étend qu'une seule classe.

**Erreurs à éviter**
- Créer une interface pour une seule implémentation "juste au cas où" — n'ajoutez une interface que quand vous avez un vrai besoin (plusieurs implémentations, ou abstraction pour les tests/DI).
- Classes abstraites trop lourdes qui imposent trop de structure aux enfants.

**Anti-patterns**
- Interfaces "marker" vides sans aucune méthode (rarement utile en Java moderne, contrairement à certains langages).

**Conseils de pro**
- Pensez TypeScript : les interfaces Java jouent exactement le même rôle de contrat que vos interfaces TS, mais en Spring elles servent aussi de **point d'injection** — vous injectez une interface (`ReclamationRepository`), Spring fournit l'implémentation concrète.

**Quand l'utiliser**
- Interfaces : contrats entre couches (service/repository), abstraction pour mocker en test, plusieurs implémentations possibles.
- Classes abstraites : partage de code commun entre sous-types très proches.

**Bonnes pratiques 2025-2026** : préférez toujours une interface à une classe abstraite quand vous n'avez pas besoin de partager de l'état — plus flexible.

---

### 2.4 Java moderne : records, sealed classes, pattern matching

**Essentiels**
- `record` : classe immuable avec constructeur, getters, `equals()`/`hashCode()`/`toString()` générés automatiquement.
- `sealed class`/`sealed interface` : liste fermée et connue des sous-types autorisés.
- Pattern matching `instanceof` et `switch` : extraction de type + variable en une seule expression.

**Erreurs à éviter**
- Vouloir faire un `record` mutable (impossible par design — si vous avez besoin de mutation, c'est signe que ce n'est pas le bon outil).
- Utiliser `sealed` sans lister tous les cas dans un `switch` → le compilateur vous avertit si vous oubliez un cas, ne l'ignorez pas.

**Anti-patterns**
- Continuer à écrire des classes DTO "à l'ancienne" (getters/setters manuels + `equals`/`hashCode` à la main) alors qu'un `record` fait tout ça en une ligne.

**Conseils de pro**
- Utilisez des `record` pour **tous vos DTOs** (objets d'échange API, événements, résultats de requêtes) — c'est l'équivalent le plus proche d'un `type`/`interface` TypeScript.
- `sealed interface EtatReclamation permits Nouvelle, EnCours, Resolue {}` = équivalent d'un union type discriminé TypeScript.

**Quand l'utiliser** : records pour toute donnée immuable (DTO, value object) ; sealed pour modéliser un ensemble fini et fermé d'états/types métier.

**Bonnes pratiques 2025-2026** : c'est **la** manière moderne d'écrire du Java — préférez-la systématiquement aux classes verbeuses d'avant Java 16, sauf entités JPA (voir partie 8, les entités JPA ne peuvent pas être des records à cause du proxying Hibernate).

---

### 2.5 Énumérations (enum)

**Essentiels**
- `enum StatutReclamation { NOUVELLE, EN_COURS, RESOLUE }` — un type fermé et fini de valeurs, bien plus sûr qu'un `String` libre.
- Un `enum` peut avoir des champs, constructeurs et méthodes (ex: chaque statut peut porter un libellé affichable).
- `values()`, `valueOf()`, `.ordinal()` (position, à éviter comme identifiant stable), `.name()`.
- `switch` sur un `enum` (le compilateur peut vérifier l'exhaustivité).

**Erreurs à éviter**
- Utiliser `.ordinal()` comme valeur stockée en base ou transmise à l'API → si vous réordonnez/insérez une valeur dans l'`enum`, tout se décale silencieusement. Stockez toujours le `.name()` (texte) ou une valeur explicite.
- Recréer "à la main" ce qu'un `enum` fait déjà (constantes `String`/`int` éparpillées pour représenter un ensemble fini de valeurs — l'anti-pattern "stringly typed" vu en 1.1).

**Anti-patterns**
- `enum` avec de la grosse logique métier complexe dedans (ex: calculs, appels externes) — un `enum` reste avant tout une donnée, pas un service.

**Conseils de pro**
- Utilisez un `enum` dès qu'un champ ne peut prendre qu'un nombre limité et connu de valeurs (statuts, rôles, catégories, priorités) — c'est l'équivalent le plus proche d'un union type littéral TypeScript (`'NOUVELLE' | 'EN_COURS' | 'RESOLUE'`), mais avec la sécurité du typage fort à la compilation.

**Quand l'utiliser** : statuts, rôles utilisateurs, catégories fixes, jours de la semaine, niveaux de priorité — partout où l'ensemble de valeurs possibles est connu à l'avance et stable.

**Bonnes pratiques 2025-2026** : combinez `enum` + `switch` expression moderne (partie 1.2) pour un code exhaustif et vérifié par le compilateur.

---

### 🔴 Fil rouge — Étape 2 : hiérarchie de types pour SignalCUA

- Remplacez le statut `String` par un `enum StatutReclamation { NOUVELLE, EN_COURS, RESOLUE }`.
- Créez une `interface Traitable` avec une méthode `void traiter()`.
- Créez une hiérarchie : `Reclamation` (classe de base) → `ReclamationVoirie`, `ReclamationProprete`, `ReclamationEclairage` (chacune implémente `traiter()` différemment).
- Bonus moderne : transformez `Reclamation` en `record` immuable, et le statut en `sealed interface` avec pattern matching pour afficher un message différent selon l'état.

---

## Partie 3 — Collections, Generics, Optionals

### 3.1 List, Set, Map, Queue

**Essentiels — List**
- `ArrayList` : tableau redimensionnable, accès par index rapide (O(1)), le choix par défaut dans 90% des cas.
- `LinkedList` : liste doublement chaînée, insertions/suppressions rapides en milieu de liste mais accès par index lent — implémente aussi `Deque` (utilisable comme file/pile).
- `Vector` et `Stack` : versions **synchronisées** historiques (pré-Java 5), quasi jamais utilisées en code moderne — remplacées par `ArrayList`/`Deque` + gestion explicite de la concurrence si besoin (partie 10). À reconnaître si vous les croisez dans du vieux code, pas à utiliser dans du code neuf.

**Essentiels — Queue/Deque**
- `Queue` : structure FIFO (premier entré, premier sorti) — `.offer()`/`.poll()`.
- `PriorityQueue` : file où les éléments sortent triés selon un ordre naturel ou un `Comparator` (pas du pur FIFO) — utile pour traiter d'abord les réclamations les plus urgentes.
- `Deque`/`ArrayDeque` : file **double** (ajout/retrait aux deux extrémités), utilisable comme pile (LIFO) ou file (FIFO) — c'est le remplaçant moderne recommandé de `Stack`.

**Essentiels — Set**
- `HashSet` : pas d'ordre garanti, le plus rapide.
- `LinkedHashSet` : conserve l'ordre d'insertion.
- `TreeSet` : trié automatiquement (ordre naturel ou `Comparator`), utile pour garder une liste de quartiers toujours triée alphabétiquement.

**Essentiels — Map**
- `HashMap` : pas d'ordre garanti, le plus courant.
- `LinkedHashMap` : conserve l'ordre d'insertion (utile pour un cache LRU par exemple).
- `TreeMap` : trié automatiquement par clé.

**Erreurs à éviter**
- `ArrayList` pour des insertions/suppressions fréquentes en milieu de liste (coût O(n)) — repensez la structure ou utilisez `LinkedList`/`ArrayDeque`.
- Utiliser `Stack`/`Vector` dans du code neuf par habitude venue d'anciens tutoriels — préférez `ArrayDeque` (pile) et `ArrayList` (liste).
- Clé de `HashMap` mutable (si elle change après insertion, vous ne la retrouvez plus — `hashCode()` change).
- `ConcurrentModificationException` en modifiant une collection pendant un `for-each`.
- Exposer une collection interne mutable directement (`return this.liste;`) au lieu d'une copie/vue immuable.

**Anti-patterns**
- Utiliser `HashMap<String, Object>` comme structure fourre-tout au lieu de modéliser un vrai objet métier — perd tout l'intérêt du typage fort de Java.

**Conseils de pro**
- `List.of()`, `Map.of()`, `Set.of()` pour des collections immuables rapides (équivalent `as const`/`Object.freeze` en TS).

**Quand l'utiliser**
- `List` : cas par défaut le plus fréquent.
- `Set` : besoin d'unicité garantie (ex: liste de quartiers uniques touchés).
- `Map` : index/cache par clé (ex: réclamations par citoyen).
- `Queue` : traitement séquentiel (file de réclamations à traiter par ordre d'arrivée).

**Bonnes pratiques 2025-2026** : `SequencedCollection` (Java 21) apporte des méthodes uniformes `getFirst()`/`getLast()`/`reversed()` sur `List`, `Deque`, `LinkedHashSet`.

---

### 3.2 Generics

**Essentiels**
- `<T>` sur une classe/méthode, bornes (`<T extends Comparable<T>>`).
- Wildcards `<? extends T>` (lecture) / `<? super T>` (écriture).
- Type erasure : les generics disparaissent au runtime (contrairement à certaines idées reçues venant d'autres langages).

**Erreurs à éviter**
- Vouloir utiliser `instanceof List<String>` (impossible à cause du type erasure).
- Créer un tableau générique (`new T[10]`) — interdit directement, contournements maladroits fréquents chez les débutants.

**Anti-patterns**
- Generics à outrance sur du code simple qui n'en a pas besoin — nuit à la lisibilité sans bénéfice réel.

**Conseils de pro**
- Créez un `Repository<T, ID>` générique maison en partie 3 pour comprendre le principe, **avant** de découvrir que Spring Data JPA fait exactement ça (`JpaRepository<T, ID>`) en partie 8 — ça démystifie complètement le framework.

**Quand l'utiliser** : classes utilitaires réutilisables, repositories génériques, collections typées.

**Bonnes pratiques 2025-2026** : rien de nouveau majeur récemment sur les generics eux-mêmes — la tendance est plutôt de préférer des types concrets/sealed pour la lisibilité quand c'est possible.

---

### 3.3 Optional

**Essentiels**
- `Optional<T>` pour représenter explicitement "peut ne rien retourner", en valeur de retour de méthode.
- `.map()`, `.filter()`, `.orElse()`, `.orElseThrow()`, `.ifPresent()`.

**Erreurs à éviter**
- Appeler `.get()` sans vérifier `.isPresent()` → annule tout l'intérêt d'`Optional` (autant retourner `null`).
- Utiliser `Optional` comme type de **champ de classe** ou de **paramètre de méthode** — ce n'est pas son rôle (il n'est pas sérialisable proprement, alourdit inutilement).

**Anti-patterns**
- `if (opt.isPresent()) { opt.get()... }` partout au lieu d'un style fonctionnel (`.map().orElse()`).

**Conseils de pro**
- `.orElseThrow(() -> new ReclamationNotFoundException(id))` est le pattern standard pour un `findById` en couche service.

**Quand l'utiliser** : retour de méthode "peut ne rien trouver" (recherche par id, recherche dans une liste).

**Bonnes pratiques 2025-2026** : combinez avec les Streams (partie 5) pour des pipelines de recherche/transformation très lisibles.

---

### 3.4 Date and Time API (`java.time`)

**Essentiels**
- `LocalDate` (date seule), `LocalTime` (heure seule), `LocalDateTime` (date + heure, sans fuseau) — les trois classes que vous utiliserez le plus souvent.
- `ZonedDateTime`/`ZoneId` : date-heure avec fuseau horaire explicite, nécessaire dès que vous communiquez avec des systèmes dans d'autres fuseaux.
- `Period` (durée en années/mois/jours, ex: "3 mois") vs `Duration` (durée en heures/minutes/secondes, ex: "2h30") — ne les confondez pas, elles ne s'appliquent pas aux mêmes types.
- `DateTimeFormatter` pour parser/formater (`DateTimeFormatter.ofPattern("dd/MM/yyyy")`).
- Toutes les classes `java.time` sont **immuables** (comme `String`) — chaque opération (`.plusDays(1)`) retourne une nouvelle instance.

**Erreurs à éviter**
- Utiliser les anciennes classes `Date`/`Calendar` (pré-Java 8) — dépréciées en pratique, mutables, sources de bugs classiques. Toujours `java.time` en code neuf.
- Oublier qu'une opération comme `.plusDays(1)` ne modifie pas l'objet original (immuabilité) : `date.plusDays(1);` seul ne fait rien, il faut `date = date.plusDays(1);`.
- Confondre `LocalDateTime` (pas de fuseau, "heure locale abstraite") et `ZonedDateTime` (fuseau explicite) — piège fréquent dès que l'app doit gérer plusieurs zones ou l'heure d'été.

**Conseils de pro**
- Stockez en base et échangez via l'API en `LocalDate`/`LocalDateTime` (Jackson les sérialise nativement en ISO-8601), sauf besoin explicite de gestion multi-fuseaux.
- `Period.between(date1, date2)` pour un écart en jours/mois/années lisible métier (ex: ancienneté d'une réclamation), `Duration.between(instant1, instant2)` pour des mesures techniques précises (temps de traitement).

**Quand l'utiliser** : absolument partout où une date/heure intervient — création d'entité, calculs de délais, rapports, planification (`@Scheduled`, partie 10).

**Bonnes pratiques 2025-2026** : `java.time` est stable depuis Java 8, aucun changement majeur récent — c'est déjà la bonne pratique de référence.

---

### 🔴 Fil rouge — Étape 3 : le "repository" en mémoire

- Créez une classe `ReclamationRepositoryMemoire` avec une `Map<Long, Reclamation>` en interne.
- Méthodes : `save()`, `findById()` (retourne `Optional<Reclamation>`), `findAll()`, `findByQuartier(String quartier)` (retourne une `List`).
- Ajoutez un `Set<String>` pour lister les quartiers uniques déjà touchés par une réclamation.

---

## Partie 4 — Exception Handling

### 4.1 Checked vs unchecked

**Essentiels**
- Checked (`IOException`...) : doivent être déclarées (`throws`) ou catchées.
- Unchecked (`RuntimeException` et filles, ex: `NullPointerException`, `IllegalArgumentException`) : pas d'obligation de déclaration.
- `finally` s'exécute toujours (sauf `System.exit()`).

**Erreurs à éviter**
- `catch (Exception e) {}` vide (exception "avalée" silencieusement) — un des pires anti-patterns Java, très difficile à débugger en prod.
- Utiliser les exceptions pour du contrôle de flux normal (coûteux, illisible).
- Trop de checked exceptions custom qui polluent toutes les signatures de méthode en cascade.

**Anti-patterns**
- **Exception swallowing** : `catch (Exception e) { e.printStackTrace(); }` en production (part dans les logs console, personne ne le voit jamais).
- Catch générique `Exception` au lieu de catcher l'exception précise attendue.

**Conseils de pro**
- Créez une hiérarchie d'exceptions métier claire dès le départ (`ReclamationNotFoundException`, `ReclamationInvalideException`) plutôt que de tout faire remonter en `RuntimeException` générique.

**Quand l'utiliser**
- Unchecked : erreurs de programmation, données invalides (préféré dans la majorité du code métier moderne, y compris dans l'écosystème Spring).
- Checked : uniquement pour des erreurs récupérables où l'appelant DOIT explicitement réagir (I/O bas niveau).

**Bonnes pratiques 2025-2026** : en Spring Boot 3, `ProblemDetail` (RFC 7807) standardise les réponses d'erreur JSON — voir partie 7.

---

### 4.2 try-with-resources

**Essentiels**
- Fermeture automatique des ressources (`AutoCloseable`) : fichiers, connexions, flux.
- Syntaxe `try (Resource r = ...) { ... }`.

**Erreurs à éviter**
- Fermer une ressource "à la main" dans un `finally` (verbeux, source d'oublis) au lieu du try-with-resources.

**Conseils de pro**
- Réflexe systématique dès que vous ouvrez un fichier, un flux réseau ou (plus rarement en Spring, géré par le framework) une connexion DB.

**Quand l'utiliser** : lecture/écriture de fichiers (ex: import CSV de réclamations), flux réseau bas niveau.

---

### 4.3 Exceptions métier custom

**Essentiels**
- Créer ses propres exceptions en étendant `RuntimeException`.
- Ajouter du contexte utile dans le message et éventuellement des champs (id concerné, code d'erreur).

**Conseils de pro**
- Une exception métier = un signal clair pour la couche au-dessus (ex: le contrôleur REST sait mapper `ReclamationNotFoundException` → HTTP 404).

**Quand l'utiliser** : dès que vous avez une règle métier qui peut échouer de façon prévisible (id inconnu, transition d'état invalide, doublon).

---

### 🔴 Fil rouge — Étape 4 : robustifier SignalCUA

- Créez `ReclamationNotFoundException` et `ReclamationInvalideException`.
- `findById()` du repository lève l'exception si absent (au lieu de retourner `Optional` — comparez les deux approches).
- Ajoutez une méthode `importerDepuisFichier(String chemin)` qui lit un fichier texte avec try-with-resources et gère proprement les lignes mal formées (catch précis, pas générique).

---

## Partie 5 — Programmation fonctionnelle & Stream API

### 5.1 Lambdas & interfaces fonctionnelles

**Essentiels**
- `(x, y) -> x + y` — syntaxe proche des arrow functions JS/TS.
- Interfaces fonctionnelles standard : `Function<T,R>`, `Predicate<T>`, `Consumer<T>`, `Supplier<T>`, `BiFunction<T,U,R>`.
- Method references (`Classe::methode`).

**Erreurs à éviter**
- Variables capturées dans une lambda doivent être **effectively final** (pas réassignées) — piège fréquent en venant de JS où les closures sont plus permissives.

**Anti-patterns**
- Lambdas avec effets de bord (modifier une variable externe) — casse le paradigme fonctionnel, dangereux en parallèle.

**Conseils de pro**
- Préférez les method references (`Reclamation::getStatut`) aux lambdas verbeuses (`r -> r.getStatut()`) quand c'est direct.

**Quand l'utiliser** : callbacks, comparateurs (`Comparator.comparing(...)`), pipelines de streams.

---

### 5.2 Stream API

**Essentiels**
- `.filter()`, `.map()`, `.sorted()`, `.limit()`, `.reduce()`, `.forEach()`.
- Un Stream ne se parcourt qu'**une seule fois** (contrairement à un tableau JS).
- `.stream()` vs `.parallelStream()`.

**Erreurs à éviter**
- Réutiliser un Stream déjà consommé → `IllegalStateException`.
- Utiliser `.parallelStream()` par réflexe sans avoir mesuré le gain réel (souvent contre-productif sur petites collections, ou dangereux avec des effets de bord).

**Anti-patterns**
- Chaînes de streams de 10+ opérations illisibles ("stream spaghetti") — parfois une boucle `for` classique reste plus claire, ce n'est pas un aveu de faiblesse.

**Conseils de pro**
- Pensez RxJS : `Stream` = pipeline de transformation synchrone, **pas réactif/asynchrone** par défaut. Pour du réactif async en Java, c'est Project Reactor/Spring WebFlux (voir partie 13).

**Quand l'utiliser** : transformation/filtrage/agrégation de collections en lecture (rapports, mapping DTO, agrégations).

---

### 5.3 Collectors

**Essentiels**
- `Collectors.toList()`, `.toMap()`, `.groupingBy()`, `.partitioningBy()`, `.joining()`, `.counting()`.

**Erreurs à éviter**
- `Collectors.toMap()` qui lève une exception si des clés sont dupliquées sans fonction de fusion précisée.

**Conseils de pro**
- `Collectors.groupingBy()` est l'outil n°1 pour transformer des résultats plats (ex: issus d'une requête JPA) en structures groupées pour un rapport ou un dashboard.

**Quand l'utiliser** : agrégations (compter par statut, grouper par quartier), transformation de listes d'entités en `Map` indexées.

---

### 🔴 Fil rouge — Étape 5 : statistiques SignalCUA

- Avec Streams : nombre de réclamations par statut (`Collectors.groupingBy(Reclamation::getStatut, Collectors.counting())`).
- Liste des quartiers avec au moins une réclamation `URGENTE` (filter + map + distinct).
- Réclamation la plus ancienne par quartier (`groupingBy` + `Collectors.minBy`).

---

## Partie 6 — Build Tools (Maven / Gradle)

### 6.1 Maven

**Essentiels**
- `pom.xml` : `<dependencies>`, `<properties>`, `<build>`.
- Cycle de vie : `compile` → `test` → `package` → `install`.
- Scopes : `compile` (défaut), `test`, `provided`, `runtime`.

**Erreurs à éviter**
- Copier-coller des dépendances sans comprendre les conflits de versions → `mvn dependency:tree` pour visualiser.
- Committer `target/` dans Git (ajoutez-le au `.gitignore`).
- Ne pas utiliser de BOM (`spring-boot-dependencies`) → versions incohérentes entre libs liées.

**Anti-patterns**
- Dépendances "au cas où" jamais utilisées → gonfle le build, augmente la surface d'attaque (CVE).

**Conseils de pro**
- **Spring Initializr** (start.spring.io) pour démarrer proprement avec les bonnes dépendances dès le départ — ne partez jamais d'un `pom.xml` vide à la main pour un projet Spring Boot.

**Quand l'utiliser** : le standard en entreprise, particulièrement adapté à votre contexte (lisibilité, prévisibilité, moins de "magie" que Gradle pour une équipe qui travaille souvent seule).

**Bonnes pratiques 2025-2026** : `mvn versions:display-dependency-updates` régulièrement pour la maintenance de sécurité.

---

### 6.2 Gradle (aperçu — à connaître, pas forcément à maîtriser)

**Essentiels**
- `build.gradle`/`build.gradle.kts`, plus rapide grâce au cache incrémental.
- Syntaxe plus flexible (Groovy/Kotlin DSL) mais plus complexe à débugger que Maven.

**Quand l'utiliser** : gros monorepos multi-modules, écosystème Kotlin, équipes qui privilégient la vitesse de build. Peu probable que ce soit prioritaire pour vos projets CUA — **à voir si vous y êtes confronté**, pas à apprendre en profondeur maintenant.

---

### 🔴 Fil rouge — Étape 6 : structurer SignalCUA

- Créez un vrai projet Maven (via Spring Initializr, même sans encore utiliser Spring) avec l'arborescence standard `src/main/java`, `src/test/java`.
- Migrez toutes vos classes des étapes précédentes dans des packages cohérents (`fr.cua.signalcua.model`, `.repository`, `.exception`).
- Ajoutez JUnit comme dépendance de test et écrivez un premier test simple sur le repository en mémoire (avant-goût de la partie 9).

---

## Partie 7 — Spring Boot & Dependency Injection

### 7.1 IoC & DI

**Essentiels**
- Inversion de contrôle : le framework instancie et connecte vos objets (`@Component`, `@Service`, `@Repository`) au lieu que vous fassiez `new` partout.
- Injection par constructeur (recommandée), par setter, par champ (`@Autowired`).
- Cycle de vie d'un bean, scopes (`singleton` par défaut, `prototype`, `request`...).

**Erreurs à éviter**
- Injection par champ (`@Autowired private XxxService service;`) au lieu du constructeur → rend les tests difficiles (pas de constructeur pour injecter des mocks facilement) et cache les dépendances circulaires.
- Dépendances circulaires entre beans (`ServiceA` ↔ `ServiceB`) — signe d'un mauvais découpage à corriger, pas à contourner avec `@Lazy`.

**Anti-patterns**
- `@Autowired` sur les champs partout sans jamais définir d'interfaces → couplage fort, tests difficiles à mocker.

**Conseils de pro**
- Injection par constructeur = le standard reconnu depuis des années dans l'écosystème Spring ; avec Lombok `@RequiredArgsConstructor` (ou nativement avec les records pour certains beans), c'est aussi concis que `@Autowired` sur champ, sans les inconvénients.

**Quand l'utiliser** : partout, c'est le cœur de Spring.

**Bonnes pratiques 2025-2026** : Spring Boot 3.x tourne sur Jakarta EE (`jakarta.*`, pas `javax.*` — piège fréquent avec les vieux tutos pré-2023).

---

### 7.2 Contrôleurs REST

**Essentiels**
- `@RestController`, `@RequestMapping`, `@GetMapping`/`@PostMapping`/`@PutMapping`/`@DeleteMapping`.
- `@PathVariable`, `@RequestParam`, `@RequestBody`.
- Codes HTTP corrects (`ResponseEntity<T>`).

**Erreurs à éviter**
- Mettre de la logique métier dans le contrôleur (le contrôleur route + valide + délègue au service, rien de plus).
- Retourner directement des entités JPA dans les réponses REST (fuite d'implémentation, boucles infinies de sérialisation sur relations bidirectionnelles).
- Ignorer les codes HTTP (tout retourner en 200, même les erreurs).

**Anti-patterns**
- **Fat Controller** : logique métier, requêtes SQL, et validation directement dans le contrôleur.

**Conseils de pro**
- `@Valid` + Bean Validation (`@NotNull`, `@Email`, `@Size`) sur les DTOs d'entrée — miroir de vos validators Angular Reactive Forms, mais côté serveur (jamais faire confiance uniquement au frontend).
- `springdoc-openapi` génère automatiquement la doc Swagger/OpenAPI depuis vos contrôleurs — très utile pour synchroniser avec votre frontend Angular.

**Quand l'utiliser** : toute exposition d'API REST.

**Bonnes pratiques 2025-2026** : `ProblemDetail` (RFC 7807, natif Spring Boot 3) pour standardiser les réponses d'erreur JSON consommées proprement par Angular.

---

### 7.3 Services & architecture en couches

**Essentiels**
- Architecture classique : `Controller` → `Service` (logique métier) → `Repository` (accès données) → `Entity`/`DTO`.
- `@Service` pour marquer la couche métier, `@Transactional` pour les opérations DB liées.

**Erreurs à éviter**
- Un seul `@Service` gigantesque qui fait tout (encore l'anti-pattern "God Service") — découpez par domaine métier.
- Oublier `@Transactional` sur une méthode qui enchaîne plusieurs écritures DB liées (risque d'état incohérent si une opération échoue au milieu).

**Anti-patterns**
- Logique métier dupliquée entre contrôleur et service par manque de discipline sur "qui fait quoi".

**Conseils de pro**
- DTOs distincts des entités JPA, systématiquement — jamais d'entité exposée telle quelle dans l'API (sécurité + découplage + évite les soucis de sérialisation).

**Quand l'utiliser** : partout, c'est l'architecture standard d'une API Spring Boot.

---

### 7.4 Configuration & profils

**Essentiels**
- `application.properties`/`application.yml`.
- Profils (`dev`, `staging`, `prod`) — équivalent des `environment.ts` Angular.
- `@ConfigurationProperties` pour typer proprement les configs plutôt que des `@Value` dispersés.

**Erreurs à éviter**
- Configuration en dur dans le code au lieu de fichiers de config + variables d'environnement.
- Secrets (mots de passe DB) committés dans le repo Git.

**Conseils de pro**
- Un fichier `application-{profil}.yml` par environnement, activé via `SPRING_PROFILES_ACTIVE` — cohérent avec votre serveur de staging WSL existant.

**Quand l'utiliser** : dès le premier projet Spring Boot, ne remettez jamais ça à plus tard.

**Bonnes pratiques 2025-2026** : CORS configuré explicitement (jamais `*` en prod) pour votre frontend Angular ; Spring Boot Actuator (`/actuator/health`) pour le monitoring de base.

---

### 🔴 Fil rouge — Étape 7 : SignalCUA devient une API

- Transformez le projet en app Spring Boot (via Spring Initializr).
- `ReclamationController` (REST) → `ReclamationService` (logique, reprend vos règles métier des parties précédentes) → `ReclamationRepository` (interface, implémentation en mémoire pour l'instant, JPA à l'étape suivante).
- Endpoints : `GET /reclamations`, `GET /reclamations/{id}`, `POST /reclamations`, `PUT /reclamations/{id}/statut`.
- DTOs (records) pour la création et la réponse, distincts du modèle interne.
- Testez avec Postman/curl, puis avec un vrai appel Angular via `HttpClient` (proxy `proxy.conf.json`).

---

## Partie 8 — Accès aux données : Spring Data JPA / Hibernate

### 8.0 JDBC — ce qui se cache sous Spring Data JPA

**Essentiels**
- JDBC (Java Database Connectivity) = l'API bas niveau standard pour parler à une base relationnelle : `DriverManager`/`DataSource`, `Connection`, `PreparedStatement`, `ResultSet`.
- Chaque SGBD a son propre driver JDBC : SQLite (léger, fichier local, pratique pour prototyper sans installer de serveur), MySQL, **PostgreSQL** (le plus utilisé en entreprise moderne, et le SGBD recommandé pour vos projets CUA vu son écosystème et sa robustesse).
- `PreparedStatement` avec paramètres liés (`?`) — protège nativement contre les injections SQL, contrairement à la concaténation de chaînes.

**Erreurs à éviter**
- Concaténer des valeurs utilisateur directement dans une requête SQL (`"SELECT * WHERE nom = '" + nom + "'"`) → injection SQL, faille de sécurité critique. Toujours des paramètres liés.
- Oublier de fermer `Connection`/`Statement`/`ResultSet` → fuite de ressources (utilisez try-with-resources, partie 4.2).
- Écrire du JDBC brut partout dans un projet Spring Boot moderne — c'est un piège pour un débutant qui ne connaît pas encore Spring Data JPA et réinvente manuellement ce que l'ORM fait déjà proprement.

**Conseils de pro**
- Il est utile de comprendre JDBC **une fois**, à la main, pour démystifier ce qui se passe "sous" Spring Data JPA/Hibernate (partie 8.1-8.3) — Hibernate génère et exécute du JDBC pour vous. Comprendre la couche du dessous rend le débogage de problèmes de performance (comme le N+1) beaucoup plus intuitif.
- SQLite est un excellent outil pour apprendre/prototyper en local sans serveur de base à installer ; passez à PostgreSQL dès que le projet devient réel (concurrence, volumétrie, fonctionnalités avancées).

**Quand l'utiliser** : directement, de moins en moins en pratique une fois Spring Data JPA maîtrisé — réservé à des cas très spécifiques (requêtes ultra-optimisées, outils d'administration/migration bas niveau). Dans 95% de vos projets Spring Boot, vous passerez par Spring Data JPA (ci-dessous), qui utilise JDBC en interne.

**🔍 À voir, pas à approfondir** : écrire un petit programme JDBC pur (connexion + `SELECT` + affichage) une seule fois pour comprendre le mécanisme suffit largement ; pas besoin de devenir expert JDBC bas niveau si vous utilisez Spring Data JPA au quotidien.

---

### 8.1 Entities & mapping

**Essentiels**
- `@Entity`, `@Id`, `@GeneratedValue`, `@Column`, `@Table`.
- Mapping des types Java ↔ colonnes SQL (`LocalDate`, `LocalDateTime`, `enum` via `@Enumerated`).

**Erreurs à éviter**
- Entité JPA = `record` : **impossible**, Hibernate a besoin d'un constructeur vide et de proxying — gardez une classe classique (éventuellement avec Lombok pour réduire le boilerplate).
- `@Enumerated(EnumType.ORDINAL)` (stocke un entier basé sur l'ordre de déclaration) → piège si vous réordonnez l'enum plus tard. Préférez `EnumType.STRING`.

**Conseils de pro**
- Séparez toujours Entité (mapping DB) et DTO (contrat API) même si ça semble redondant au début — ça vous sauve la mise dès que le modèle DB et le modèle API divergent (presque toujours, tôt ou tard).

**Quand l'utiliser** : toute donnée persistée en base relationnelle.

---

### 8.2 Repositories

**Essentiels**
- `interface ReclamationRepository extends JpaRepository<Reclamation, Long>` → CRUD généré automatiquement.
- Query methods dérivées du nom (`findByStatut`, `findByQuartierAndStatut`).
- `@Query` en JPQL ou SQL natif pour les cas complexes.

**Erreurs à éviter**
- Requêtes SQL natives partout "parce que c'est plus simple" → perd la portabilité et l'intérêt de l'ORM.

**Conseils de pro**
- Comparez mentalement avec votre repository en mémoire maison de la partie 3 : Spring Data JPA fait la même chose, en génial l'implémentation via un proxy dynamique à partir de l'interface.

**Quand l'utiliser** : la grande majorité des cas CRUD classiques. Pour des rapports complexes, envisagez des projections DTO en JPQL plutôt que de charger des entités complètes.

---

### 8.3 Relations & le problème N+1

**Essentiels**
- `@OneToMany`, `@ManyToOne`, `@ManyToMany`, `FetchType.LAZY`/`EAGER`.
- Le **problème N+1** : boucler sur une liste d'entités et déclencher une requête SQL par élément pour charger une relation lazy — le piège de performance n°1 en JPA.

**Erreurs à éviter**
- `FetchType.EAGER` par défaut sur toutes les relations → charge des données inutiles partout.
- Accéder à une relation lazy en dehors de la session Hibernate (hors `@Transactional`) → `LazyInitializationException`.

**Conseils de pro**
- Activez les logs SQL en dev (`spring.jpa.show-sql=true`, `logging.level.org.hibernate.SQL=DEBUG`) dès le début pour repérer les N+1 tôt, pas en prod sous charge.
- `JOIN FETCH` en JPQL ou `@EntityGraph` pour charger explicitement les relations nécessaires en une seule requête.

**Quand l'utiliser** : chaque fois que vous mappez une relation, demandez-vous "cette donnée sera-t-elle systématiquement affichée avec le parent, ou seulement parfois ?" → EAGER/JOIN FETCH pour le premier cas, LAZY pour le second.

---

### 8.4 Migrations (Flyway)

**Essentiels**
- Scripts SQL versionnés (`V1__init.sql`, `V2__add_urgence.sql`) exécutés automatiquement au démarrage.
- Jamais de `spring.jpa.hibernate.ddl-auto=update` en production.

**Erreurs à éviter**
- Modifier un script de migration déjà appliqué en prod (Flyway détecte le checksum modifié et bloque le démarrage — c'est voulu, ne contournez pas).

**Conseils de pro**
- Directement pertinent pour vous : c'est l'outil qui structure proprement ce que vous faites déjà "à la main" en migration de données à la CUA.

**Quand l'utiliser** : dès le premier projet avec une vraie base de données, sans exception.

---

### 🔴 Fil rouge — Étape 8 : SignalCUA persiste en PostgreSQL

- `Reclamation` devient une `@Entity`, relation `@ManyToOne` vers une entité `Citoyen`.
- Remplacez le repository mémoire par `JpaRepository`.
- Ajoutez Flyway avec un script de migration initial + un script d'ajout de colonne `urgente`.
- Repérez volontairement un problème N+1 (listez les réclamations avec leur citoyen sans `JOIN FETCH`, observez les logs SQL), puis corrigez-le.

---

## Partie 9 — Tests (JUnit, Mockito)

### 9.1 JUnit

**Essentiels**
- `@Test`, `@BeforeEach`/`@AfterEach`, `@ParameterizedTest`, assertions (`assertEquals`, `assertThrows`, `assertTrue`).

**Erreurs à éviter**
- Ne tester que le "happy path", jamais les cas d'erreur/limites.
- Assertions vagues (`assertTrue(result != null)`) au lieu de vérifier le contenu réel.

**Conseils de pro**
- Structure AAA (Arrange, Act, Assert) systématique pour la lisibilité.

**Quand l'utiliser** : logique métier dans les services, mapping, validations — écrivez-les en même temps que le code, pas après.

---

### 9.2 Mockito

**Essentiels**
- `@Mock`, `@InjectMocks`, `when(...).thenReturn(...)`, `verify(...)`.

**Erreurs à éviter**
- Over-mocking : mocker au point que le test ne vérifie plus rien de réellement utile.
- Tests fragiles couplés à l'implémentation interne plutôt qu'au comportement observable.

**Conseils de pro**
- Mockez les dépendances externes du service testé (repository, autre service), jamais la classe testée elle-même.

**Quand l'utiliser** : tests unitaires de services qui dépendent de repositories/autres services.

---

### 9.3 Tests d'intégration (Testcontainers)

**Essentiels**
- `@SpringBootTest` pour démarrer le contexte Spring complet.
- **Testcontainers** : vrai PostgreSQL/MySQL dans un conteneur Docker pour les tests — bien plus fiable qu'une base H2 en mémoire, qui a des différences de comportement SQL subtiles.

**Erreurs à éviter**
- Tests d'intégration lents pour tester une simple méthode utilitaire → réservez `@SpringBootTest` aux vrais flux d'intégration (repository, contrôleur complet).
- Dépendance entre tests (ordre d'exécution qui compte) — chaque test doit être indépendant et rejouable seul.

**Conseils de pro**
- Particulièrement précieux pour vous, qui travaillez souvent seul sans relecture d'équipe : les tests deviennent votre filet de sécurité principal contre les régressions.

**Quand l'utiliser** : repositories JPA, contrôleurs REST complets, flux métier critiques (transitions de statut d'une réclamation).

---

### 🔴 Fil rouge — Étape 9 : SignalCUA testé

- Tests unitaires du `ReclamationService` avec Mockito (mock du repository).
- Test d'intégration du `ReclamationRepository` avec Testcontainers PostgreSQL, incluant un test qui vérifie qu'il n'y a **pas** de problème N+1 (compte les requêtes SQL générées).
- Test d'intégration du contrôleur (`@SpringBootTest` + `MockMvc` ou `WebTestClient`) sur le flux complet de création d'une réclamation.

---

## Partie 10 — Concurrency (à voir plus tard, sauf besoin spécifique)

### 10.1 Threads & ExecutorService

**Essentiels**
- `Runnable`/`Callable`, `ExecutorService` pour gérer un pool de threads plutôt que créer des `Thread` à la main.
- `synchronized`, locks — protection d'un état partagé.

**Erreurs à éviter**
- `new Thread()` manuel en production au lieu d'un `ExecutorService`.
- Accès concurrent à une collection non thread-safe sans synchronisation.

**Quand l'utiliser** : traitement de gros volumes en parallèle (import/export de données massif).

---

### 10.2 `@Async`

**Essentiels**
- `@EnableAsync` + `@Async` pour exécuter une méthode Spring de façon asynchrone (retour `void` ou `CompletableFuture<T>`).
- `@Scheduled` pour les tâches planifiées (nettoyage, rapports quotidiens).

**Erreurs à éviter**
- `@Async` sur une méthode appelée depuis la même classe (le proxy Spring ne s'applique pas — piège classique du "self-invocation").

**Conseils de pro**
- Cas d'usage concret pour vous : envoi de notification (email/SMS) au citoyen quand sa réclamation change de statut, sans bloquer la réponse HTTP.

**Quand l'utiliser** : notifications, tâches planifiées, traitement différé non bloquant.

---

### 10.3 Virtual Threads

**Essentiels**
- Threads légers gérés par la JVM (Java 21+), activables en Spring Boot via `spring.threads.virtual.enabled=true`.

**Conseils de pro**
- Gain de performance important pour les apps I/O-bound (typiquement une API REST qui fait beaucoup d'appels DB) sans changer votre code métier.

**Quand l'utiliser** : quasi par défaut sur toute nouvelle app Spring Boot 3.2+ sur Java 21+, sans contre-indication connue pour un usage classique.

---

### 🔴 Fil rouge — Étape 10 : notifications asynchrones

- Ajoutez une méthode `@Async notifierCitoyen(Reclamation r)` appelée quand le statut change (simulez l'envoi, pas besoin d'un vrai serveur mail à ce stade).
- Ajoutez une tâche `@Scheduled` qui génère un petit rapport quotidien du nombre de réclamations résolues.

---

## Partie 11 — Logging

### 11.1 SLF4J / Logback

**Essentiels**
- SLF4J comme **façade** de logging standard (l'API que vous utilisez dans le code) + une implémentation en dessous qui fait le vrai travail : **Logback** par défaut dans Spring Boot, ou **Log4j2** (successeur du très répandu Log4j historique) si vous l'ajoutez explicitement — les deux sont interchangeables sans changer votre code applicatif tant que vous codez contre l'API SLF4J.
- Niveaux : `TRACE < DEBUG < INFO < WARN < ERROR`.

**Erreurs à éviter**
- `System.out.println()` au lieu d'un vrai logger.
- Logger des données sensibles (mots de passe, données personnelles — particulièrement important vu que vous gérez de l'état civil).
- Coder directement contre l'API Log4j2/Logback plutôt que contre la façade SLF4J — vous perdez la portabilité entre implémentations.

**Conseils de pro**
- `private static final Logger log = LoggerFactory.getLogger(MaClasse.class);` — ou `@Slf4j` (Lombok) pour éviter le boilerplate.
- Spring Boot embarque Logback par défaut et c'est largement suffisant pour la majorité des projets ; ne migrez vers Log4j2 que si vous avez un besoin précis (performance de logging asynchrone très élevée, existant legacy déjà en Log4j).

---

### 11.2 Logging structuré

**Essentiels**
- MDC (Mapped Diagnostic Context) pour enrichir automatiquement chaque log avec le contexte de la requête (id de corrélation, utilisateur).
- Logs au format JSON en production pour faciliter l'agrégation/recherche.

**Quand l'utiliser** : dès qu'une app tourne en production avec plusieurs utilisateurs simultanés — indispensable pour débugger un incident sans pouvoir reproduire en local.

---

### 🔴 Fil rouge — Étape 11 : traçabilité SignalCUA

- Ajoutez des logs `INFO` sur les créations/changements de statut, `WARN` sur les tentatives invalides, `ERROR` sur les échecs inattendus.
- Ajoutez un identifiant de corrélation (request ID) via un filtre servlet ou un intercepteur Spring, propagé dans le MDC.

---

## Partie 12 — Sécurité (absente de la roadmap.sh, mais indispensable en pratique)

> ⚠️ Ce chapitre n'existe pas dans le PDF que vous m'avez envoyé, mais toute API exposée sur Internet ou même en intranet municipal en a besoin. Je le place ici car c'est le bon moment pour l'introduire, une fois l'API de base fonctionnelle.

### 12.1 Spring Security — les bases

**Essentiels**
- `SecurityFilterChain` (config moderne, remplace les vieilles classes `WebSecurityConfigurerAdapter` dépréciées).
- Authentification (qui êtes-vous) vs autorisation (qu'avez-vous le droit de faire).
- `@PreAuthorize("hasRole('AGENT')")` pour protéger des méthodes.

**Erreurs à éviter**
- Désactiver CSRF/CORS "pour que ça marche" sans comprendre pourquoi c'était activé.
- Stocker des mots de passe en clair ou avec un hash faible (MD5/SHA1) → `BCryptPasswordEncoder`.

**Conseils de pro**
- Commencez simple : une seule règle "tout est protégé sauf `/auth/login`" plutôt que des règles fines partout dès le départ.

**Quand l'utiliser** : dès qu'une API expose des données non publiques — donc quasiment toujours dans votre contexte (données citoyennes).

---

### 12.2 JWT / OAuth2

**Essentiels**
- JWT : token signé contenant les infos d'identité/rôles, stateless (pas de session serveur).
- OAuth2 : protocole standard pour délégation d'autorisation (ex: connexion via un fournisseur d'identité externe).

**Erreurs à éviter**
- Mettre des données sensibles dans le payload JWT (il est signé, pas chiffré — lisible par n'importe qui).
- Token JWT sans expiration, ou expiration trop longue.

**Quand l'utiliser** : API stateless consommée par un frontend Angular (votre cas standard) → JWT est le choix par défaut le plus courant. OAuth2 devient pertinent si vous devez déléguer l'authentification à un système externe (SSO gouvernemental par exemple).

---

### 🔴 Fil rouge — Étape 12 : sécuriser SignalCUA

- Ajoutez un endpoint `/auth/login` qui retourne un JWT.
- Protégez les endpoints de modification (`POST`/`PUT`) pour les agents authentifiés uniquement ; laissez la consultation publique.
- Testez le flux complet depuis Angular : login → stockage du token → `HttpInterceptor` qui l'ajoute automatiquement aux requêtes.

---

## Partie 13 — Le monde au-delà de la roadmap : à voir, pas à apprendre maintenant

La roadmap.sh Java se concentre sur le **langage et son écosystème direct**. Elle passe volontairement sous silence tout un ensemble de sujets que vous croiserez presque certainement en devenant développeur backend d'entreprise. Voici une carte pour ne pas être surpris — **sans obligation de les apprendre maintenant**.

| Sujet | C'est quoi, en une phrase | Priorité pour vous | Quand vous y serez probablement confronté |
|---|---|---|---|
| **Docker** | Conteneuriser l'app pour un déploiement reproductible | 🟠 À voir bientôt | Vous avez déjà un serveur staging WSL — la suite logique est de conteneuriser vos apps Spring Boot dessus |
| **Spring Batch** | Framework dédié aux traitements de données volumineux par lots | 🟠 À voir bientôt | Directement pertinent pour vos migrations de données CUA (état civil) |
| **Spring Mail / notifications** | Envoi d'emails depuis l'app | 🟠 À voir bientôt | Notifications citoyens, alertes internes |
| **i18n (internationalisation)** | Gérer plusieurs langues dans une même app | 🟢 Pertinent mais pas urgent | Contexte malgache : français/malgache dans les interfaces citoyennes |
| **Génération de PDF/rapports** (JasperReports, iText, Apache PDFBox) | Générer des documents officiels (actes, rapports) | 🟢 Pertinent mais pas urgent | Très probable pour l'état civil ou des rapports administratifs |
| **Kafka / RabbitMQ** (message queues) | Communication asynchrone entre services découplés | 🔵 À connaître de nom | Pertinent seulement si vous évoluez vers une architecture microservices — peu probable à court terme pour la CUA |
| **Spring WebFlux / programmation réactive** | Modèle non-bloquant pour très haute charge concurrente | 🔵 À connaître de nom | Rarement nécessaire pour une app administrative classique ; Spring MVC classique + Virtual Threads suffit dans l'immense majorité des cas |
| **Microservices (patterns : circuit breaker, service discovery...)** | Découper une app en plusieurs services indépendants | 🔵 À connaître de nom | Seulement si la CUA grandit vers plusieurs équipes/services séparés — un monolithe bien structuré est souvent le bon choix pour votre taille d'organisation |
| **Kubernetes** | Orchestration de conteneurs à grande échelle | 🔵 À connaître de nom | Pertinent seulement à partir d'une certaine échelle d'infrastructure ; Docker seul (voire Docker Compose) suffit largement pour l'instant |
| **GraphQL** | Alternative à REST pour des requêtes de données flexibles | 🔵 À connaître de nom | Rarement nécessaire sauf besoins de requêtage très complexes côté frontend |
| **gRPC** | Communication binaire performante entre services (souvent en microservices) | 🔵 À connaître de nom | Peu probable dans votre contexte actuel |
| **Redis / caching** | Cache mémoire distribué pour accélérer les lectures fréquentes | 🟢 Pertinent mais pas urgent | Utile si un dashboard/rapport devient lent à cause de requêtes répétées coûteuses |
| **CI/CD (GitHub Actions, Jenkins, GitLab CI)** | Automatiser build/tests/déploiement | 🟠 À voir bientôt | Directement applicable dès que vous avez des tests (partie 9) — gain de fiabilité énorme pour du travail en solo |
| **Observabilité (Micrometer, Prometheus, Grafana, OpenTelemetry)** | Monitoring et traçage détaillé en production | 🔵 À connaître de nom | Pertinent à partir du moment où une app critique tourne en prod sans supervision manuelle possible |
| **Design patterns (GoF) & architecture (Hexagonal, Clean Architecture, DDD)** | Vocabulaire et structures de conception avancées | 🟢 Pertinent mais pas urgent | Utile une fois les bases Spring Boot solides, pour structurer de plus gros projets |
| **WebSockets / STOMP** | Communication temps réel bidirectionnelle | 🔵 À connaître de nom | Utile si un jour vous voulez un dashboard CUA avec mises à jour en temps réel (ex: nombre de réclamations en direct) |
| **Multi-tenancy** | Une même app servant plusieurs organisations isolées | 🔵 À connaître de nom | Peu probable dans votre contexte (une seule commune) |
| **Lombok** | Génère automatiquement getters/setters/constructeurs par annotations | 🟢 Pertinent, facile à adopter | Utile dès maintenant pour réduire le boilerplate sur les entités JPA (qui ne peuvent pas être des records) |
| **MapStruct** | Génère automatiquement le mapping Entity ↔ DTO | 🟢 Pertinent, facile à adopter | Dès que vous avez beaucoup de conversions Entity/DTO répétitives (partie 7-8) |

**Légende des priorités**
- 🟠 À voir bientôt : probablement utile dans les 3-6 prochains mois de vos projets CUA actuels.
- 🟢 Pertinent mais pas urgent : utile un jour, pas de raison de se précipiter.
- 🔵 À connaître de nom seulement : comprendre le principe en une phrase pour ne pas être perdu si le sujet apparaît en réunion ou dans un article, sans l'apprendre en profondeur maintenant.

---

## Partie 14 — Pont Angular ↔ Spring Boot (récapitulatif enrichi)

- **DTOs partagés en esprit** : records Java en miroir de vos interfaces TypeScript ; `springdoc-openapi` peut générer un client TS depuis votre spec OpenAPI pour rester synchronisé automatiquement.
- **Validation** : `@Valid` + Bean Validation côté Spring, en miroir de vos validators Angular Reactive Forms — jamais confiance uniquement au frontend.
- **Dates** : `LocalDate`/`LocalDateTime` sérialisés en ISO-8601 par Jackson, compatibles nativement avec `Date`/`date-fns` côté Angular.
- **Pagination** : `Page<T>` de Spring Data (`content`, `totalElements`, `totalPages`) → prévoyez le typage correspondant côté Angular.
- **CORS** : à configurer explicitement dès le début (proxy `proxy.conf.json` en dev local).
- **Auth** : JWT stocké côté Angular + `HttpInterceptor` pour l'ajouter automatiquement aux requêtes (voir partie 12).

---

## Résumé — les 5 pièges n°1 pour un débutant Java/Spring Boot

1. Injection par champ (`@Autowired` sur les champs) au lieu du constructeur.
2. Exposer les entités JPA directement dans l'API REST au lieu de DTOs.
3. Le problème N+1 en JPA/Hibernate (relations lazy mal gérées).
4. Avaler les exceptions silencieusement (`catch (Exception e) {}`).
5. Logique métier dans les contrôleurs au lieu des services.

Si vous évitez seulement ces 5 pièges dès le départ, vous serez déjà devant une bonne partie des développeurs Spring Boot auto-formés — et le projet **SignalCUA** construit chapitre après chapitre vous donne un terrain concret pour ancrer chaque notion avant de passer à la suivante.

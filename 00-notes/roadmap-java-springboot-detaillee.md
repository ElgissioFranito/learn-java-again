# Roadmap détaillée Java → Spring Boot (dev Angular → Java)

> Ce document est pensé comme un parcours guidé, pas comme une encyclopédie à parcourir dans le désordre. Chaque partie explique, avant d'entrer dans le détail, **pourquoi elle arrive à ce moment précis** — pour qu'aucune notion ne vous tombe dessus sans que vous compreniez d'où elle vient. Les parties n'ont volontairement pas toutes la même taille : certaines (Exception Handling) tiennent en trois sous-chapitres parce que le sujet est simple ; d'autres (Spring Boot) en comptent onze parce que c'est là que se joue la majorité de votre futur travail quotidien.

---

## 0. Le projet fil rouge : **SignalCUA**

Pour éviter d'apprendre du Java "hors sol", tout ce guide s'appuie sur **un seul projet que vous faites évoluer chapitre après chapitre** :

> **SignalCUA** — une mini-application de gestion des réclamations citoyennes (voirie, propreté, éclairage public...) pour la commune. Un citoyen signale un problème, un agent le traite, un statut évolue (`NOUVELLE` → `EN_COURS` → `RESOLUE`).

C'est volontairement proche de vos vrais projets CUA (état civil, dashboard administratif) pour que les réflexes pris ici se transfèrent directement dans votre travail.

Le projet traverse 4 grandes phases, qui suivent exactement la structure du guide :

1. **Phase console** (parties 1-5) : Java pur, aucun framework, tout en mémoire — on apprend le langage sans distraction.
2. **Phase Maven** (partie 6) : le même code, mais structuré comme un vrai projet buildable.
3. **Phase API** (parties 7-9) : Spring Boot + base de données + tests — on devient une vraie application serveur.
4. **Phase production** (parties 10-12) : async, logs, sécurité — on rend l'application robuste face au monde réel.

Chaque partie se termine par un bloc **🔴 Fil rouge** qui vous dit exactement quoi coder à cette étape, en réutilisant ce qui vient d'être appris — jamais autre chose de nouveau.

---

## Partie 1 — Bases du langage

**Pourquoi commencer ici ?** Avant même de parler d'objets, de classes ou de frameworks, il faut le vocabulaire de base : comment on stocke une donnée, comment on répète une action, comment on écrit du texte. C'est le socle sans lequel tout le reste serait incompréhensible — exactement comme on n'apprend pas la grammaire d'une langue avant de connaître ses mots.

### 1.1 Variables, types, opérateurs

**Essentiels**

- Java est un langage **fortement typé** : chaque variable a un type déclaré une fois pour toutes, contrairement à JavaScript/TypeScript où `let x = 5` peut ensuite recevoir autre chose en JS pur (TypeScript vous a déjà habitué à cette rigueur, donc le concept ne devrait pas vous surprendre).
- **Types primitifs** (écrits en minuscule) : `int`, `long`, `double`, `boolean`, `char` — stockent directement une valeur, pas une référence.
- **Types objets associés** ("wrapper classes", en majuscule) : `Integer`, `Long`, `Double`, `Boolean`, `Character` — enveloppent un primitif dans un objet. Java convertit automatiquement de l'un à l'autre (**auto-boxing**/**auto-unboxing**) : `Integer i = 5;` fonctionne sans conversion explicite.
- Pourquoi deux versions du même concept existent : les primitifs sont rapides et légers (utilisés par défaut) ; les objets sont nécessaires quand une valeur peut être absente (`null`), ou dans les collections (partie 3), qui ne savent manipuler que des objets.
- Opérateurs arithmétiques (`+ - * / %`), logiques (`&& || !`), ternaire (`condition ? valeurA : valeurB`).
- Conversion de types (`casting`) : implicite quand on "élargit" (`int` → `double`, aucune perte), explicite quand on "rétrécit" (`(int) 3.9` → perte de précision, il faut l'écrire volontairement).

**Erreurs à éviter**

- La division entre deux `int` tronque le résultat : `5 / 2` vaut `2`, pas `2.5`. Pour un résultat décimal, il faut qu'au moins un des deux opérandes soit `double` (`5.0 / 2`).
- Comparer deux `Integer` (ou deux `String`) avec `==` : cela compare les **références mémoire**, pas le contenu. `Integer a = 200; Integer b = 200; a == b` peut retourner `false` de façon contre-intuitive (Java met en cache les petits entiers entre -128 et 127, donc `==` "marche par hasard" pour les petites valeurs et casse pour les grandes — d'où l'importance de toujours utiliser `.equals()` pour comparer du contenu).
- `int` déborde silencieusement (pas d'erreur) au-delà d'environ 2 milliards. Si un volume peut dépasser ce seuil, utilisez `long` dès le départ.
- Ne jamais utiliser `double`/`float` pour représenter de l'argent (erreurs d'arrondi binaire) : la classe dédiée est `BigDecimal`.

**Anti-patterns**

- Le "stringly typed code" : représenter un statut, un rôle ou une devise avec un `String` libre au lieu d'un type dédié (on verra le bon outil pour ça, l'`enum`, en partie 2.5).
- Abuser de l'inférence `var` au point de rendre le type d'une variable illisible sans l'aide de l'IDE — contrairement à TypeScript où l'inférence est très valorisée, la culture Java d'entreprise préfère la lisibilité explicite, surtout pour un débutant.

**Conseils de pro**

- Prenez dès maintenant le réflexe : à chaque variable objet que vous déclarez, demandez-vous "est-ce que cette valeur peut légitimement être `null` ?". C'est LE réflexe mental n°1 à construire en Java — plus encore qu'en TypeScript, où `strictNullChecks` vous alerte automatiquement ; en Java, rien ne vous protège par défaut contre un `NullPointerException`, il faut l'anticiper vous-même.

**Quand l'utiliser** : partout, sans exception — c'est le vocabulaire de base du langage.

**Bonnes pratiques 2025-2026**

- `var` est accepté pour les variables locales dont le type est évident au premier coup d'œil (`var liste = new ArrayList<String>();`), mais jamais pour les paramètres ou les valeurs de retour de méthode publique, où la clarté du contrat prime.
- Les *text blocks* (`"""`) remplacent la concaténation pour tout texte multi-lignes (SQL, JSON, HTML) — plus lisible, moins d'erreurs d'échappement.

---

### 1.2 Structures de contrôle : if, switch, while, for

**Pourquoi juste après les variables ?** Une fois qu'on sait stocker une valeur, l'étape naturelle suivante est de décider quoi faire selon cette valeur (condition) et de répéter une action (boucle) — les deux briques qui transforment une suite de variables en un vrai programme.

**Essentiels**

- `if / else if / else` : classique, identique en logique à ce que vous connaissez en TypeScript.
- `switch` classique (avec `case` et `break`) et `switch` **expression** moderne (`case X -> ...`, Java 14+), qui retourne directement une valeur et ne nécessite pas de `break`.
- `for` (compteur), `for-each` (parcours de collection, le plus utilisé en pratique), `while`, `do-while`.
- `break` (sortir d'une boucle), `continue` (passer à l'itération suivante).

**Erreurs à éviter**

- Le `switch` classique "tombe" au `case` suivant si vous oubliez `break` (comportement hérité du C, source d'un grand nombre de bugs historiques en Java) — c'est justement ce que corrige le `switch` expression moderne, à préférer systématiquement pour du code neuf.
- Erreurs de bornes dans les boucles `for` à index (`<=` au lieu de `<`, qui fait déborder d'un élément — l'erreur "off-by-one" classique dans tous les langages).
- Modifier une collection pendant qu'on la parcourt avec un `for-each` classique (`ConcurrentModificationException`) — on reverra ce piège en détail en partie 3.

**Anti-patterns**

- Empiler des `if` imbriqués sur plus de 2-3 niveaux : préférez des **retours anticipés** (*early return* — `if (!valide) return;` tout en haut de la méthode) qui aplatissent la logique et la rendent bien plus lisible.

**Conseils de pro**

- Préférez systématiquement le `switch` expression moderne dès que vous le pouvez : en plus d'être plus concis, le compilateur peut vérifier que vous avez couvert tous les cas possibles (utile dès la partie 2.5 avec les `enum`, et essentiel en partie 2.4 avec les `sealed` types).

**Quand l'utiliser** : base obligatoire, omniprésente.

**Bonnes pratiques 2025-2026**

- Le *pattern matching* dans `switch` (Java 21+) permet d'écrire `switch (obj) { case Reclamation r when r.urgente() -> ...; }` — extraction de type et condition en une seule expression. On y reviendra concrètement en 2.4, une fois que vous aurez des hiérarchies de types à filtrer ainsi.

---

### 1.3 Tableaux et Strings

**Pourquoi ici ?** Les tableaux sont la première structure qui regroupe plusieurs valeurs — un prérequis conceptuel avant les vraies collections dynamiques de la partie 3. Les `String`, elles, sont en réalité un cas particulier de tableau (de caractères) sous le capot, d'où leur proximité pédagogique.

**Essentiels**

- Tableau à **taille fixe** : `int[] tab = new int[5];` — la taille ne change jamais après création (contrairement à un array JavaScript).
- `String` est **immuable** : une fois créée, une chaîne ne change jamais ; chaque opération (`+`, `.substring()`) produit une **nouvelle** chaîne.
- Méthodes courantes : `.length()`, `.substring()`, `.split()`, `.trim()`, `.contains()`, `.equals()`/`.equalsIgnoreCase()`, `.format()`.
- `StringBuilder` : la classe dédiée pour construire une chaîne progressivement (notamment en boucle) sans payer le coût de l'immuabilité à chaque étape.

**Erreurs à éviter**

- Concaténer des `String` avec `+` à l'intérieur d'une boucle : chaque `+` recrée une chaîne entière en mémoire, ce qui coûte de plus en plus cher à mesure que la boucle avance (complexité quadratique). Utilisez `StringBuilder` dès qu'il y a une boucle.
- `.equals()` est sensible à la casse par défaut — pensez `.equalsIgnoreCase()` quand c'est pertinent (comparaison d'un email saisi par un utilisateur, par exemple).
- `.split()` avec une expression régulière peut produire des résultats surprenants sur des cas limites (chaînes vides en fin de tableau) — testez toujours sur vos vraies données.

**Anti-patterns**

- Parser "à la main" un format structuré (CSV, JSON) avec des `.split()` en chaîne — fragile dès que les données contiennent le séparateur lui-même. Une bibliothèque dédiée (vue en partie 4, pour l'import de fichiers) fait ce travail correctement.

**Conseils de pro**

- Les tableaux à taille fixe sont rares en pratique réelle une fois que vous connaîtrez `ArrayList` (partie 3) — considérez-les surtout comme une étape d'apprentissage vers les vraies collections dynamiques que vous utiliserez au quotidien.

**Quand l'utiliser**

- Tableau : uniquement quand la taille est connue et fixe à l'avance (rare en pratique métier).
- `ArrayList` (partie 3) : le choix par défaut dans l'immense majorité des cas réels.

**Bonnes pratiques 2025-2026**

- `String.formatted(...)` (Java 15+) est une syntaxe plus lisible que l'ancien `String.format(...)` pour insérer des valeurs dans un texte.

---

### 1.4 Introduction aux classes et méthodes

**Pourquoi maintenant ?** Vous savez désormais manipuler des valeurs isolées (variables, tableaux). L'étape suivante logique est de regrouper des valeurs **liées entre elles** dans une structure cohérente qui porte un nom métier — c'est exactement le rôle d'une classe. C'est la porte d'entrée vers la programmation orientée objet, développée en profondeur dans la partie 2.

**Essentiels**

- Déclarer une classe (`public class Reclamation { ... }`), des **champs** (variables internes à la classe), un **constructeur** (méthode spéciale appelée à la création d'un objet via `new`), des méthodes.
- `this` : référence à l'objet courant, utile notamment pour distinguer un paramètre de constructeur d'un champ portant le même nom (`this.id = id;`).
- **Surcharge de méthode** (*overloading*) : plusieurs méthodes portant le même nom mais des paramètres différents — le compilateur choisit la bonne version selon les arguments fournis à l'appel.
- Cycle de vie basique d'un objet : création via `new`, puis nettoyage automatique par le **ramasse-miettes** (*garbage collector*) quand plus personne ne référence l'objet — contrairement à des langages comme C++, vous n'avez jamais à libérer la mémoire manuellement.

**Erreurs à éviter**

- Un constructeur qui fait "trop" (validation complexe, appel réseau, calculs lourds) : gardez les constructeurs simples, ils ne devraient faire qu'initialiser l'état de l'objet.
- Oublier de valider les paramètres reçus par le constructeur (accepter un `id` négatif, un `nom` vide sans réagir) — un objet devrait toujours naître dans un état cohérent.

**Anti-patterns**

- Les classes "boîte à outils" fourre-tout (`Utils`, `Helper`, `Divers`) qui accumulent des méthodes sans lien logique entre elles — souvent le signe qu'un concept métier aurait dû être modélisé comme sa propre classe.

**Conseils de pro**

- Donnez à vos classes des noms **métier** explicites (`Reclamation`, pas `Data` ou `Item`) : ça paraît évident, mais c'est la base d'un code compréhensible six mois plus tard — particulièrement important pour vous qui travaillez souvent seul, sans collègue pour vous rappeler le contexte.

**Quand l'utiliser** : base obligatoire, dès qu'une donnée a plusieurs attributs liés entre eux.

**Bonnes pratiques 2025-2026** : pour beaucoup de classes simples qui ne font que porter des données, il existe un outil bien plus concis que la classe classique décrite ici — le `record`, présenté en 2.4 une fois que vous aurez compris les bases de la classe "à l'ancienne" qu'il remplace avantageusement.

---

### 1.5 Variables de classe (static) & constantes

**Pourquoi juste après les classes de base ?** Vous venez d'apprendre que chaque objet créé avec `new` a ses propres champs, indépendants des autres objets. Il existe un cas particulier important à connaître dès maintenant, avant d'aller plus loin : une donnée **partagée par toutes les instances** d'une classe plutôt que dupliquée dans chacune.

**Essentiels**

- `static` sur un champ = **variable de classe** : une seule copie existe, partagée entre toutes les instances — à l'inverse d'un champ normal ("variable d'instance"), propre à chaque objet créé.
- `static final` = constante de classe, par convention nommée en majuscules (`public static final int MAX_URGENCE = 5;`).
- Une méthode `static` s'appelle directement sur la classe, sans créer d'objet (`Reclamation.compter()`), et n'a pas accès à `this` (logique : elle n'est liée à aucune instance précise).

**Erreurs à éviter**

- Confondre variable de classe et variable d'instance dans une même classe sans s'en apercevoir — piège fréquent pour un débutant venant de JavaScript, où les champs sont par défaut propres à chaque objet (pas de notion native de "variable de classe" en JS classique).
- Utiliser un champ `static` **mutable** pour stocker un état applicatif partagé (ex : un compteur global modifié depuis plusieurs endroits) : source de bugs difficiles à tracer, et dangereux dès que plusieurs utilisateurs agissent en même temps (vous comprendrez pourquoi précisément en partie 10, sur la concurrence).

**Anti-patterns**

- Des classes "singleton maison" bricolées avec des champs `static` partout pour simuler un état global unique — en Spring Boot (partie 7), un composant déclaré en singleton fait ce travail proprement et sans ces risques.

**Conseils de pro**

- Réservez `static` aux vraies constantes et aux méthodes utilitaires **sans état** (qui ne font que calculer à partir de leurs paramètres, comme `Math.max(a, b)`) — jamais au partage d'état applicatif mutable.

**Quand l'utiliser** : constantes métier (`MAX_URGENCE`), méthodes utilitaires pures.

---

### 1.6 Packages & organisation du code

**Pourquoi ici ?** Vous savez maintenant créer des classes. Dès que leur nombre dépasse quelques unités (ce qui arrivera très vite, dès le fil rouge de la partie 2), il faut un système pour les ranger — exactement comme vous organisez déjà vos fichiers Angular en dossiers `services/`, `components/`, `models/`.

**Essentiels**

- Chaque fichier Java commence par une déclaration `package fr.cua.signalcua.model;`, qui doit correspondre à l'arborescence réelle de dossiers sur le disque.
- `import` pour utiliser une classe définie dans un autre package.
- Convention de nommage : le nom de domaine inversé (`fr.cua.xxx`, comme `com.google.xxx`) — garantit l'absence de collision de noms entre bibliothèques différentes.
- Visibilité **package-private** (aucun modificateur devant un champ/méthode) : visible uniquement depuis le même package — un niveau de visibilité intermédiaire entre `private` et `public` que TypeScript ne possède pas nativement.

**Erreurs à éviter**

- Tout regrouper dans un seul package fourre-tout (`fr.cua.signalcua` avec cinquante classes à plat) : organisez plutôt par **couche technique** dès le départ (`.controller`, `.service`, `.repository`, `.model`, `.dto`, `.exception`) — on retrouvera exactement cette organisation en partie 7, quand Spring Boot structurera votre application en couches.
- Dépendances circulaires entre packages (A dépend de B qui dépend de A) : signe d'un mauvais découpage à corriger, pas à contourner.

**Conseils de pro**

- Pensez-y comme l'équivalent direct de l'organisation en dossiers `services/`, `components/`, `models/` en Angular — les mêmes réflexes de séparation par responsabilité s'appliquent d'un langage à l'autre.

**Quand l'utiliser** : dès que le projet dépasse une poignée de classes — donc pratiquement tout de suite.

---

### 1.7 Lecture des entrées utilisateur (Scanner)

**Pourquoi cette étape, et pourquoi elle sera vite dépassée ?** Pour l'instant, votre programme ne fait qu'afficher des résultats fixés à l'avance. Avant de passer aux structures plus riches (partie 2), il est utile de savoir faire réagir votre programme à une saisie — ne serait-ce que pour des exercices interactifs. Gardez toutefois en tête que cette technique est transitoire : une vraie application Spring Boot (partie 7) ne lit jamais le clavier, elle répond à des requêtes HTTP envoyées par un navigateur ou une app Angular.

**Essentiels**

- `Scanner sc = new Scanner(System.in);` puis `.nextLine()` (texte), `.nextInt()` (entier), `.nextDouble()` (décimal).

**Erreurs à éviter**

- Mélanger `.nextInt()` puis `.nextLine()` sans précaution : `.nextInt()` ne consomme pas le retour à la ligne tapé par l'utilisateur, donc le `.nextLine()` suivant lit une chaîne vide — un piège extrêmement classique et déroutant pour un débutant qui ne s'y attend pas.
- Ne pas gérer les erreurs de saisie : si l'utilisateur tape du texte alors que vous attendez un nombre, `.nextInt()` lève une `InputMismatchException` (voir partie 4 pour la gestion d'exceptions).

**Conseils de pro**

- N'investissez pas trop de temps ici : c'est un outil d'apprentissage en console, pas une compétence professionnelle centrale pour un développeur backend orienté API.

**Quand l'utiliser** : exercices console, petits scripts CLI internes ponctuels (ex : un outil d'import lancé manuellement).

---

### 🔴 Fil rouge — Étape 1 : la classe `Reclamation` en Java pur

Objectif, sans aucun framework :

- Une classe `Reclamation` (id, description, quartier, statut en `String` — corrigé par un `enum` en partie 2.5).
- Quelques réclamations créées "à la main" dans un tableau.
- Affichage via une boucle `for-each`.
- Comptage des réclamations par quartier avec `StringBuilder`.
- (Optionnel) Un petit menu interactif avec `Scanner` pour saisir une nouvelle réclamation au clavier.

Ce que ça vous fait pratiquer, sans vertige de tout apprendre en même temps : syntaxe, classes basiques, tableaux, boucles, strings, organisation minimale en package.

---

## Partie 2 — Programmation orientée objet

**Pourquoi juste après les bases du langage ?** Vous savez créer une classe simple (1.4). La question naturelle suivante est : comment des classes **différentes mais liées** (une réclamation de voirie, une de propreté...) peuvent-elles partager un comportement commun sans dupliquer le code ? C'est précisément le problème que résout la programmation orientée objet — et c'est le paradigme central autour duquel tout Java (et donc tout Spring Boot) est construit.

### 2.1 Encapsulation & constructeurs

**Essentiels**

- Champs `private` par défaut, accès contrôlé via des méthodes publiques **getters**/**setters** (ou, mieux, via un `record` — voir 2.4).
- Constructeurs multiples (surcharge, comme en 1.4), et `this(...)` pour qu'un constructeur en appelle un autre de la même classe, évitant la duplication.

**Erreurs à éviter**

- Générer des getters/setters pour absolument tous les champs par réflexe de l'IDE, sans se demander si le champ doit vraiment être modifiable depuis l'extérieur de la classe.
- Des setters qui ne valident rien, permettant à l'objet de se retrouver dans un état incohérent (ex : un statut réglé sur une valeur qui n'existe pas métier-parlant).

**Anti-patterns**

- L'**Anemic Domain Model** : des classes qui ne sont que des sacs de getters/setters, sans aucune règle métier à l'intérieur. C'est très fréquent dans les applications Spring Boot mal conçues, où toute la logique finit "en vrac" dans les services (partie 7.8) au lieu d'être portée par les objets eux-mêmes.

**Conseils de pro**

- À chaque champ, posez-vous la question : "quelqu'un a-t-il vraiment besoin de le modifier après la création de l'objet ?" Si la réponse est non, pas de setter, champ `final`. Cette discipline vous prépare directement à l'immuabilité par défaut des `record` (2.4).

**Quand l'utiliser** : base obligatoire.

---

### 2.2 Héritage & polymorphisme

**Pourquoi après l'encapsulation ?** Une fois qu'on sait construire une classe autonome et bien protégée, la question suivante est : que faire quand plusieurs classes se ressemblent fortement ? L'héritage propose une réponse — mais comme vous allez le voir, ce n'est presque jamais la meilleure.

**Essentiels**

- `extends` pour hériter d'une classe parent, `super()` pour appeler son constructeur.
- **Redéfinition de méthode** (*override*) avec l'annotation `@Override`, qui déclenche une vérification du compilateur.
- **Polymorphisme** : une variable déclarée du type parent peut référencer un objet d'un type enfant, et c'est la méthode réellement redéfinie dans l'enfant qui s'exécute à l'appel — même si le code appelant "ne sait" que manipuler le type parent.
- Niveaux de visibilité : `private` (classe seule) \< *package-private* (1.6) \< `protected` (classe + sous-classes) \< `public` (partout).

**Erreurs à éviter**

- Une hiérarchie sur plus de 2-3 niveaux devient vite ingérable à faire évoluer.
- Oublier `@Override` : sans cette annotation, une faute de frappe dans le nom de la méthode redéfinie ne provoque **aucune erreur de compilation** — vous créez silencieusement une nouvelle méthode au lieu de redéfinir l'existante, un piège très difficile à repérer sans l'annotation.
- Appeler une méthode redéfinissable depuis le constructeur de la classe parent : au moment de l'appel, l'objet enfant n'est pas encore totalement initialisé, ce qui peut produire des résultats surprenants.

**Anti-patterns**

- Utiliser l'héritage uniquement pour réutiliser du code, sans qu'il existe une vraie relation "est-un" stable dans le temps. Le principe central de la conception objet en Java d'entreprise est de **préférer la composition à l'héritage** ("favor composition over inheritance") : au lieu de faire hériter `ReclamationUrgente` de `Reclamation`, on préfère souvent qu'une classe **contienne** une autre plutôt que d'en hériter.

**Conseils de pro**

- Règle simple pour trancher : héritage seulement pour une relation "est-un" vraiment stable et naturelle (`ReclamationVoirie` **est une** `Reclamation`). Pour "se comporte comme" ou "peut faire", préférez une interface (2.3).

**Quand l'utiliser** : hiérarchies de types métier réellement stables — rare en pratique. La composition + interfaces couvre la majorité des besoins réels.

**Bonnes pratiques 2025-2026** : les *sealed classes* (2.4) remplacent avantageusement l'héritage libre quand vous voulez fermer explicitement l'ensemble des sous-types possibles.

---

### 2.3 Interfaces & classes abstraites

**Pourquoi après l'héritage, et pas avant ?** Vous venez de voir les limites de l'héritage classique. L'interface est l'outil qui répond au vrai besoin sous-jacent — définir un contrat commun — sans les inconvénients de l'héritage rigide. La comprendre juste après rend ce contraste évident au lieu d'abstrait.

**Essentiels**

- Une **interface** est un contrat pur : elle décrit des méthodes que toute classe qui l'implémente doit fournir, sans en imposer l'implémentation (depuis Java 8, elle peut aussi fournir des méthodes `default` toutes faites, et des méthodes `static`).
- Une **classe abstraite** est une base partielle : elle peut contenir à la fois de l'état (des champs) et du comportement partagé, mais ne peut jamais être instanciée directement.
- Une **méthode abstraite** (`abstract void traiter();`, déclarée sans corps dans une classe abstraite) oblige chaque sous-classe concrète à fournir sa propre implémentation — c'est le mécanisme qui garantit qu'une hiérarchie respecte un contrat commun tout en laissant chaque enfant définir sa logique propre.
- Une classe peut implémenter **plusieurs** interfaces, mais n'étend **qu'une seule** classe (abstraite ou non) — une limitation volontaire du langage pour éviter la complexité de l'héritage multiple.

**Erreurs à éviter**

- Créer une interface pour une seule implémentation existante "juste au cas où" : n'en créez une que face à un vrai besoin (plusieurs implémentations réelles, ou nécessité de remplacer facilement l'implémentation dans les tests — vous verrez ce cas très concrètement en partie 9.2).
- Des classes abstraites trop lourdes, qui imposent trop de structure à leurs enfants et limitent leur flexibilité.

**Conseils de pro**

- Faites le pont avec ce que vous connaissez déjà : les interfaces Java jouent exactement le même rôle de contrat que vos interfaces TypeScript. La différence importante viendra en partie 7 : en Spring, les interfaces servent aussi de **point d'injection** — vous manipulez une interface (`ReclamationRepository`), et c'est le framework qui vous fournit l'implémentation concrète correspondante au moment de l'exécution.

**Quand l'utiliser**

- Interface : contrat entre couches d'une application (service ↔ repository), abstraction nécessaire pour remplacer une implémentation en test, plusieurs implémentations réellement envisagées.
- Classe abstraite : partage de code commun entre sous-types très proches, avec un état partagé.

**Bonnes pratiques 2025-2026** : à besoin équivalent, préférez toujours une interface à une classe abstraite quand vous n'avez pas besoin de partager d'état — c'est plus flexible pour la suite.

---

### 2.4 Java moderne : records, sealed classes, pattern matching

**Pourquoi maintenant, et pas dès la partie 1 ?** Ces trois outils modernes ne prennent tout leur sens qu'une fois que vous avez compris ce qu'ils remplacent : le `record` remplace la classe verbeuse de 1.4/2.1 (getters/setters/`equals`/`hashCode` manuels), et `sealed` + le *pattern matching* étendent directement l'héritage (2.2) et le `switch` (1.2) que vous venez d'apprendre. Les introduire avant aurait été un raccourci qui masque ce qu'ils simplifient réellement.

**Essentiels**

- Un `record` est une classe **immuable** : `record Reclamation(Long id, String description, String quartier) {}` génère automatiquement le constructeur, les accesseurs, `equals()`, `hashCode()` et `toString()` — tout ce que vous écriviez à la main en 2.1, en une seule ligne.
- `sealed class`/`sealed interface` : liste explicite et fermée des sous-types autorisés (`sealed interface EtatReclamation permits Nouvelle, EnCours, Resolue {}`) — le compilateur connaît alors l'ensemble complet des possibilités.
- *Pattern matching* pour `instanceof` et `switch` : extraction du type **et** de la variable en une seule expression, notamment très puissant combiné à `sealed` puisque le compilateur peut vérifier que tous les cas sont couverts.

**Erreurs à éviter**

- Vouloir rendre un `record` mutable : c'est impossible par conception. Si vous ressentez ce besoin, c'est le signe que l'outil ne convient pas à ce cas précis (utilisez alors une classe classique de 2.1).
- Utiliser `sealed` sans traiter tous les cas dans un `switch` : le compilateur vous avertit si un cas manque — ne contournez jamais cet avertissement avec un `default` fourre-tout, c'est justement la protection que `sealed` vous offre.

**Anti-patterns**

- Continuer à écrire des classes DTO "à l'ancienne" (getters/setters manuels, `equals`/`hashCode` copiés-collés) alors qu'un `record` fait tout cela en une ligne, sans risque d'oubli.

**Conseils de pro**

- Utilisez des `record` pour **tous vos DTOs** (objets d'échange d'API, événements, résultats de requête) — c'est l'équivalent le plus proche d'une `interface`/`type` TypeScript, avec en plus l'égalité de contenu automatique.
- `sealed interface EtatReclamation permits Nouvelle, EnCours, Resolue {}` correspond très précisément à un union type discriminé en TypeScript (`'NOUVELLE' | 'EN_COURS' | 'RESOLUE'`), mais avec la sécurité de la vérification d'exhaustivité par le compilateur.

**Quand l'utiliser** : `record` pour toute donnée immuable (DTO, objet de valeur) ; `sealed` pour modéliser un ensemble fini et fermé d'états ou de types métier.

**Bonnes pratiques 2025-2026** : c'est la manière **actuelle** d'écrire du Java — à préférer systématiquement aux classes verbeuses d'avant Java 16. Exception notable, à retenir dès maintenant pour ne pas être surpris en partie 8 : les **entités JPA ne peuvent pas être des `record`**, à cause d'une contrainte technique d'Hibernate (besoin d'un constructeur vide et de la possibilité de créer des objets "proxy" — vous verrez ce détail concrètement en 8.1).

---

### 2.5 Énumérations (enum)

**Pourquoi juste après les records/sealed, et pas avant ?** Un `enum` est en réalité un cas particulier très courant de "type fermé à un nombre connu de valeurs" — exactement l'idée derrière `sealed` que vous venez de voir, mais pour de simples constantes plutôt que des types porteurs de données. Le voir juste après rend cette parenté évidente.

**Essentiels**

- `enum StatutReclamation { NOUVELLE, EN_COURS, RESOLUE }` : un ensemble fermé et fini de valeurs nommées, bien plus sûr qu'un `String` libre (rappel de l'anti-pattern "stringly typed" vu en 1.1).
- Un `enum` peut porter des champs, un constructeur et des méthodes — chaque valeur peut par exemple avoir un libellé lisible associé.
- `values()` (toutes les valeurs), `valueOf("NOUVELLE")` (conversion texte → enum), `.name()` (nom textuel stable), `.ordinal()` (position dans la déclaration — à éviter comme identifiant, voir ci-dessous).
- `switch` sur un `enum` : le compilateur peut vérifier que tous les cas sont couverts, exactement comme avec `sealed`.

**Erreurs à éviter**

- Stocker en base de données ou transmettre à une API la valeur `.ordinal()` : si vous insérez ou réordonnez une valeur dans la déclaration de l'`enum` plus tard, tous les `ordinal()` se décalent silencieusement, corrompant les données déjà enregistrées. Utilisez toujours `.name()` (texte stable) ou une valeur explicite associée.
- Recréer "à la main" ce qu'un `enum` fait déjà nativement (constantes `String`/`int` éparpillées dans le code pour représenter un ensemble fini de valeurs).

**Anti-patterns**

- Mettre de la logique métier complexe (calculs lourds, appels externes) directement dans un `enum` — il reste avant tout une donnée, pas un service.

**Conseils de pro**

- Utilisez un `enum` dès qu'un champ ne peut prendre qu'un nombre limité et connu de valeurs (statuts, rôles, catégories, priorités) — l'équivalent le plus proche d'un union type littéral TypeScript, avec la sécurité du typage fort vérifié à la compilation.

**Quand l'utiliser** : statuts, rôles utilisateurs, catégories fixes, niveaux de priorité — partout où l'ensemble des valeurs possibles est connu à l'avance et stable dans le temps.

---

### 🔴 Fil rouge — Étape 2 : hiérarchie de types pour SignalCUA

- Remplacez le statut `String` par un `enum StatutReclamation { NOUVELLE, EN_COURS, RESOLUE }`.
- Créez une `interface Traitable` avec une méthode `void traiter()`.
- Créez une hiérarchie : `Reclamation` (classe de base) → `ReclamationVoirie`, `ReclamationProprete`, `ReclamationEclairage`, chacune implémentant `traiter()` différemment.
- Bonus moderne : transformez `Reclamation` en `record` immuable, et le statut en `sealed interface` avec *pattern matching* pour afficher un message différent selon l'état.

---

## Partie 3 — Collections, Generics, Optionals, Dates

**Pourquoi cette partie maintenant ?** En partie 1.3, vous avez découvert le tableau — une première structure regroupant plusieurs valeurs, mais rigide (taille fixe). Maintenant que vous savez modéliser des objets riches (partie 2), le besoin naturel devient : stocker un **nombre variable** de ces objets, les rechercher, les trier, les regrouper. C'est exactement ce que couvre le *Collection Framework*. Les `Generics` sont présentés juste après parce qu'ils sont le mécanisme qui rend les collections typées (`List<Reclamation>` plutôt qu'une liste "de n'importe quoi") ; `Optional` et les dates ferment cette partie car ce sont deux autres façons, très concrètes, de représenter une donnée qui "peut manquer" ou "évolue dans le temps" — des compléments naturels une fois qu'on manipule beaucoup de données réelles.

### 3.1 List, Set, Map, Queue

**Essentiels — List**

- `ArrayList` : tableau redimensionnable automatiquement, accès par position très rapide — le choix par défaut dans l'immense majorité des cas.
- `LinkedList` : liste doublement chaînée, rapide pour insérer/supprimer au milieu, mais lente pour accéder par position — implémente aussi `Deque` (donc utilisable comme file ou pile, voir ci-dessous).
- `Vector` et `Stack` : versions historiques **synchronisées** (datant d'avant Java 5), quasi jamais utilisées en code moderne, remplacées par `ArrayList`/`ArrayDeque` avec une gestion explicite de la concurrence si nécessaire (partie 10). Il est utile de les reconnaître si vous les croisez dans du code ancien, mais ne les utilisez jamais dans du code neuf.

**Essentiels — Queue/Deque**

- `Queue` : structure **FIFO** (premier entré, premier sorti — comme une file d'attente réelle), via `.offer()`/`.poll()`.
- `PriorityQueue` : les éléments en sortent triés selon un ordre naturel ou un `Comparator` (pas un pur FIFO) — utile par exemple pour toujours traiter en premier la réclamation la plus urgente, indépendamment de son ordre d'arrivée.
- `Deque`/`ArrayDeque` : file **double** (ajout/retrait aux deux extrémités), utilisable à la fois comme pile (LIFO — dernier entré, premier sorti) ou comme file (FIFO). C'est le remplaçant moderne recommandé de l'ancien `Stack`.

**Essentiels — Set**

- `HashSet` : pas d'ordre garanti, le plus rapide en général.
- `LinkedHashSet` : conserve l'ordre d'insertion.
- `TreeSet` : trié automatiquement (ordre naturel ou `Comparator`) — utile pour garder par exemple une liste de quartiers toujours triée alphabétiquement.

**Essentiels — Map**

- `HashMap` : pas d'ordre garanti, le plus courant.
- `LinkedHashMap` : conserve l'ordre d'insertion (utile pour un cache à éviction simple).
- `TreeMap` : trié automatiquement par clé.

**Erreurs à éviter**

- `ArrayList` pour des insertions/suppressions fréquentes en milieu de liste (coût proportionnel à la taille de la liste) — repensez la structure ou utilisez `LinkedList`/`ArrayDeque` selon le besoin exact.
- Une clé de `HashMap` **mutable** : si son contenu change après insertion, son "empreinte" (`hashCode()`) change aussi, et vous ne retrouvez plus l'entrée — préférez des clés immuables (`String`, `Long`, ou un `record`).
- `ConcurrentModificationException` en modifiant une collection pendant qu'on la parcourt avec un `for-each` classique (piège déjà signalé en 1.2, particulièrement fréquent avec les collections).
- Exposer directement une collection interne mutable depuis un getter (`return this.liste;`) plutôt qu'une copie ou une vue immuable — un appelant extérieur pourrait alors modifier l'état interne de votre objet sans passer par ses méthodes prévues.
- Utiliser `Stack`/`Vector` dans du code neuf par habitude issue d'anciens tutoriels.

**Anti-patterns**

- Utiliser une `HashMap<String, Object>` comme structure fourre-tout au lieu de modéliser un vrai objet métier (partie 2) — cela perd tout l'intérêt du typage fort de Java.

**Conseils de pro**

- `List.of()`, `Map.of()`, `Set.of()` (Java 9+) créent des collections **immuables** rapidement — l'équivalent d'un `as const`/`Object.freeze` en TypeScript.

**Quand l'utiliser**

- `List` : cas par défaut, le plus fréquent.
- `Set` : besoin d'unicité garantie.
- `Map` : index ou cache accessible par clé.
- `Queue`/`Deque` : traitement séquentiel ou par ordre de priorité.

**Bonnes pratiques 2025-2026** : `SequencedCollection` (Java 21) apporte des méthodes uniformes `getFirst()`/`getLast()`/`reversed()` sur `List`, `Deque` et `LinkedHashSet`.

---

### 3.2 Generics

**Pourquoi juste après les collections ?** Vous venez d'écrire `List<Reclamation>` sans vous poser de question — mais ce `<Reclamation>` **est** un generic. Le comprendre maintenant, juste après en avoir vu un usage concret, est bien plus efficace que de l'apprendre dans l'abstrait avant d'avoir manipulé la moindre collection.

**Essentiels**

- `<T>` sur une classe ou une méthode : un "type paramétré", décidé au moment de l'utilisation (`List<Reclamation>`, `List<String>`...).
- Bornes : `<T extends Comparable<T>>` restreint `T` aux types qui respectent un contrat donné.
- *Wildcards* : `<? extends T>` (lecture seule d'une collection de sous-types de `T`), `<? super T>` (écriture).
- *Type erasure* : à l'exécution, les informations de type générique disparaissent (contrairement à ce qu'on pourrait imaginer par analogie avec TypeScript, où l'effacement des types au runtime est en fait le même principe — donc ce concept ne devrait pas être surprenant).

**Erreurs à éviter**

- Vouloir tester `instanceof List<String>` : impossible à cause du *type erasure* — on ne peut vérifier que `instanceof List<?>`.
- Créer un tableau générique (`new T[10]`) : interdit directement par le langage, source de contournements maladroits chez les débutants qui ne connaissent pas encore les collections dynamiques comme alternative.

**Anti-patterns**

- Des generics à outrance sur du code simple qui n'en a pas réellement besoin — nuit à la lisibilité sans bénéfice réel.

**Conseils de pro**

- Un excellent exercice pédagogique : créez en fil rouge un `Repository<T, ID>` générique maison **avant** de découvrir, en partie 8, que Spring Data JPA fait exactement la même chose avec `JpaRepository<T, ID>`. Comprendre le mécanisme à la main démystifie complètement ce que le framework fera "par magie" plus tard.

**Quand l'utiliser** : classes utilitaires réutilisables, repositories génériques, toute collection typée.

---

### 3.3 Optional

**Pourquoi ici ?** Vous savez chercher un élément dans une `Map`/`List` (3.1) — mais que retourner quand l'élément n'existe pas ? `null` est la réponse historique, mais elle est dangereuse (rappel du réflexe de 1.1 : "est-ce que cette valeur peut être `null` ?"). `Optional` est la réponse moderne à ce problème précis, d'où sa place ici, juste après avoir manipulé des recherches dans des collections.

**Essentiels**

- `Optional<T>` représente explicitement, dans la signature même d'une méthode, "cette méthode peut ne rien retourner" — la valeur de retour d'une méthode, pas un type de champ ou de paramètre (voir plus bas).
- `.map()`, `.filter()`, `.orElse()`, `.orElseThrow()`, `.ifPresent()` — un style fonctionnel qui évite les vérifications manuelles répétitives.

**Erreurs à éviter**

- Appeler `.get()` sans avoir vérifié `.isPresent()` au préalable : cela annule complètement l'intérêt d'`Optional` (autant retourner `null` directement).
- Utiliser `Optional` comme type d'un **champ de classe** ou d'un **paramètre de méthode** : ce n'est pas son rôle prévu (il n'est pas conçu pour être sérialisé proprement, et alourdit inutilement le code) — il est réservé aux valeurs de retour.

**Anti-patterns**

- Écrire `if (opt.isPresent()) { opt.get()... }` partout au lieu du style fonctionnel (`.map().orElse()`), qui est à la fois plus concis et moins sujet à erreur.

**Conseils de pro**

- `.orElseThrow(() -> new ReclamationNotFoundException(id))` est le motif standard pour un `findById` en couche service — vous le retrouverez tel quel en partie 8.

**Quand l'utiliser** : retour de méthode "peut légitimement ne rien trouver" (recherche par identifiant, recherche dans une liste).

---

### 3.4 Date and Time API (`java.time`)

**Pourquoi en dernier dans cette partie ?** Les dates sont, comme `Optional`, un autre cas de donnée qui a longtemps été mal représentée par les outils historiques du langage (l'ancienne classe `Date`, mutable et peu pratique). `java.time` est la réponse moderne, tout comme `Optional` l'est pour l'absence de valeur — les deux sujets se complètent naturellement en clôture de cette partie consacrée à bien représenter les données réelles de votre application.

**Essentiels**

- `LocalDate` (date seule), `LocalTime` (heure seule), `LocalDateTime` (date + heure, **sans** fuseau horaire) — les trois classes que vous utiliserez le plus souvent au quotidien.
- `ZonedDateTime`/`ZoneId` : date-heure avec un fuseau horaire explicite, nécessaire dès que votre application communique avec des systèmes situés dans d'autres fuseaux.
- `Period` (durée exprimée en années/mois/jours, ex : "3 mois") **vs** `Duration` (durée exprimée en heures/minutes/secondes, ex : "2h30") — les deux ne s'appliquent pas aux mêmes types et ne sont pas interchangeables.
- `DateTimeFormatter` pour convertir entre texte et date (`DateTimeFormatter.ofPattern("dd/MM/yyyy")`).
- Toutes les classes de `java.time` sont **immuables**, exactement comme `String` (1.3) : chaque opération (`.plusDays(1)`) retourne un **nouvel** objet, l'original n'est jamais modifié.

**Erreurs à éviter**

- Utiliser les anciennes classes `Date`/`Calendar` (héritées des tout débuts de Java, avant 2014) : mutables et sources historiques de nombreux bugs. Utilisez toujours `java.time` dans du code neuf.
- Oublier l'immuabilité : écrire `date.plusDays(1);` seul ne fait rien du tout — il faut récupérer le résultat (`date = date.plusDays(1);`), exactement comme pour une opération sur une `String`.
- Confondre `LocalDateTime` (pas de fuseau, "heure abstraite") et `ZonedDateTime` (fuseau explicite) : un piège fréquent dès que l'application doit gérer plusieurs zones géographiques ou l'heure d'été.

**Conseils de pro**

- Stockez en base et échangez via l'API en `LocalDate`/`LocalDateTime` (le format JSON standard, ISO-8601, les gère nativement), sauf besoin explicite de gestion multi-fuseaux.
- `Period.between(date1, date2)` donne un écart lisible métier (ex : ancienneté d'une réclamation en jours), `Duration.between(instant1, instant2)` donne une mesure technique précise (temps de traitement).

**Quand l'utiliser** : partout où une date ou une heure intervient — création d'un enregistrement, calcul de délai, rapport, planification de tâche (vous retrouverez ce dernier usage concrètement en partie 10).

---

### 🔴 Fil rouge — Étape 3 : le "repository" en mémoire, enrichi

- Une classe `ReclamationRepositoryMemoire` avec une `Map<Long, Reclamation>` en interne.
- Méthodes : `save()`, `findById()` (retourne un `Optional<Reclamation>`), `findAll()`, `findByQuartier(String quartier)` (retourne une `List`).
- Un `Set<String>` pour lister les quartiers uniques déjà touchés.
- Ajoutez un champ `LocalDateTime dateCreation` sur `Reclamation`, et une méthode `findAnciennesDe(Period seuil)` qui filtre les réclamations dépassant une certaine ancienneté.

---

## Partie 4 — Exception Handling

**Pourquoi cette partie est courte, et pourquoi maintenant ?** Vous manipulez désormais de vraies données (collections, recherches, dates) — et de vraies données, ça veut dire des recherches qui échouent, des fichiers mal formés, des valeurs invalides. Le sujet des exceptions est **volontairement traité en peu de sous-parties** ici : contrairement à l'orienté objet ou à Spring Boot, il s'agit d'un mécanisme simple une fois compris, pas d'un vaste écosystème à explorer.

### 4.1 Checked vs unchecked, et try-with-resources

**Essentiels**

- Les exceptions **checked** (ex : `IOException`) doivent être explicitement déclarées (`throws`) ou capturées (`catch`) par le compilateur — une contrainte propre à Java, sans équivalent direct en TypeScript.
- Les exceptions **unchecked** (`RuntimeException` et ses descendantes, ex : `NullPointerException`, `IllegalArgumentException`) n'imposent aucune déclaration.
- `finally` s'exécute toujours, qu'une exception ait été levée ou non (sauf arrêt brutal via `System.exit()`).
- `try-with-resources` : ferme automatiquement une ressource (fichier, flux) implémentant `AutoCloseable` à la fin du bloc, même en cas d'exception — à préférer systématiquement à une fermeture manuelle dans un `finally`.

**Erreurs à éviter**

- `catch (Exception e) {}` vide : l'exception est "avalée" silencieusement, ce qui rend un bug quasiment impossible à diagnostiquer en production. C'est l'un des pires réflexes possibles en Java.
- Utiliser les exceptions pour du contrôle de flux normal (par exemple, lever une exception juste pour sortir d'une boucle) : coûteux en performance et illisible.
- Créer trop d'exceptions checked personnalisées, qui obligent à polluer en cascade toutes les signatures de méthode qui les traversent.

**Anti-patterns**

- L'*exception swallowing* : `catch (Exception e) { e.printStackTrace(); }` en production — l'information part dans la console du serveur, que personne ne consulte jamais en pratique. On verra la bonne alternative (le *logging*) en partie 11.

**Conseils de pro**

- Dans la majorité du code métier moderne (y compris tout l'écosystème Spring), on privilégie les exceptions **unchecked** pour signaler les erreurs de données ou de logique — les checked exceptions sont réservées à des cas d'I/O bas niveau réellement récupérables.

**Quand l'utiliser** : chaque fois qu'une opération peut échouer de façon prévisible (recherche sans résultat, fichier absent, donnée invalide).

---

### 4.2 Exceptions métier custom

**Essentiels**

- Créer ses propres exceptions en étendant `RuntimeException`, avec un message et éventuellement des champs de contexte (l'identifiant concerné, par exemple).

**Conseils de pro**

- Une exception métier bien nommée (`ReclamationNotFoundException`) est un signal clair pour la couche qui l'attrapera plus haut — vous verrez concrètement en partie 7 comment un contrôleur REST transforme automatiquement ce type d'exception en un code d'erreur HTTP approprié (404, par exemple).

**Quand l'utiliser** : dès qu'une règle métier peut échouer de façon identifiable (identifiant inconnu, transition de statut invalide, doublon).

---

### 🔴 Fil rouge — Étape 4 : robustifier SignalCUA

- Créez `ReclamationNotFoundException` et `ReclamationInvalideException`.
- `findById()` du repository lève l'exception si l'élément est absent (comparez cette approche à celle avec `Optional` de la partie 3 — les deux sont légitimes selon le contexte).
- Ajoutez une méthode `importerDepuisFichier(String chemin)` qui lit un fichier texte avec `try-with-resources` et gère proprement les lignes mal formées (un `catch` précis pour chaque cas identifié, jamais un `catch (Exception e)` générique).

---

## Partie 5 — Programmation fonctionnelle & Stream API

**Pourquoi ici, et pas plus tôt ?** Les *Streams* servent à transformer et agréger des **collections** (partie 3) — les introduire avant les collections aurait été prématuré, il n'y aurait rien de concret à leur faire traiter. De même, les lambdas s'appuient sur les **interfaces** (2.3) : une lambda n'est, au fond, qu'une façon très concise d'implémenter une interface à une seule méthode. Cette partie arrive donc logiquement après avoir posé ces deux fondations.

### 5.1 Lambdas & interfaces fonctionnelles

**Essentiels**

- Syntaxe `(x, y) -> x + y`, proche des *arrow functions* JavaScript/TypeScript que vous connaissez déjà.
- Interfaces fonctionnelles standard fournies par Java : `Function<T,R>` (transforme une valeur), `Predicate<T>` (teste une condition, retourne `boolean`), `Consumer<T>` (consomme une valeur sans rien retourner), `Supplier<T>` (fournit une valeur sans paramètre).
- *Method references* (`Classe::methode`) : une syntaxe encore plus concise qu'une lambda, quand la lambda ne fait que déléguer directement à une méthode existante.

**Erreurs à éviter**

- Une variable capturée par une lambda doit être **effectively final** (jamais réassignée après sa première affectation) — un piège fréquent en venant de JavaScript, où les *closures* sont plus permissives sur ce point.

**Anti-patterns**

- Des lambdas avec effets de bord (modifier une variable extérieure à la lambda) : cela casse le paradigme fonctionnel et devient dangereux en cas d'exécution parallèle (voir 5.2).

**Conseils de pro**

- Préférez une *method reference* (`Reclamation::getStatut`) à une lambda équivalente mais plus verbeuse (`r -> r.getStatut()`) quand c'est direct.

**Quand l'utiliser** : callbacks, comparateurs personnalisés (`Comparator.comparing(...)`), et bien sûr les pipelines de Streams ci-dessous.

---

### 5.2 Stream API

**Pourquoi juste après les lambdas ?** Un Stream n'est en pratique qu'une chaîne d'opérations, chacune configurée par une lambda ou une *method reference* — sans avoir vu 5.1 d'abord, chaque exemple de Stream serait incompréhensible.

**Essentiels**

- `.filter()` (garder certains éléments), `.map()` (transformer chaque élément), `.sorted()`, `.limit()`, `.reduce()` (agréger en une seule valeur), `.forEach()`.
- Un Stream ne se parcourt qu'**une seule fois** : contrairement à un tableau JavaScript, on ne peut pas réutiliser le même Stream après l'avoir "consommé" par une opération finale.
- `.stream()` (séquentiel) vs `.parallelStream()` (parallélisé sur plusieurs threads).

**Erreurs à éviter**

- Réutiliser un Stream déjà consommé : lève une `IllegalStateException`.
- Utiliser `.parallelStream()` par réflexe, sans avoir mesuré le gain réel — souvent contre-productif sur de petites collections, et potentiellement dangereux en présence d'effets de bord (rappel de 5.1).

**Anti-patterns**

- Des chaînes de dix opérations ou plus, devenues illisibles ("*stream spaghetti*") : parfois, une simple boucle `for` reste plus claire, ce n'est pas un aveu de faiblesse mais un vrai choix de lisibilité.

**Conseils de pro**

- Si vous connaissez RxJS côté Angular : un `Stream` Java est un pipeline de transformation, mais **synchrone** par défaut, pas réactif/asynchrone comme un `Observable`. Pour du réactif asynchrone en Java, l'outil équivalent est Project Reactor (Spring WebFlux) — un sujet volontairement classé "à connaître de nom seulement" en partie 13, car rarement nécessaire pour une application administrative classique.

**Quand l'utiliser** : transformation, filtrage, agrégation de collections en lecture (rapports, transformation en DTO, statistiques).

---

### 5.3 Collectors

**Essentiels**

- `Collectors.toList()`, `.toMap()`, `.groupingBy()` (regrouper par critère), `.partitioningBy()` (diviser en deux groupes selon une condition), `.joining()`, `.counting()`.

**Erreurs à éviter**

- `Collectors.toMap()` lève une exception si deux éléments produisent la même clé, sans qu'une fonction de fusion n'ait été précisée pour gérer ce cas.

**Conseils de pro**

- `Collectors.groupingBy()` est l'outil n°1 pour transformer des résultats "plats" (par exemple issus d'une requête en base de données, partie 8) en structures groupées prêtes pour un rapport ou un tableau de bord.

**Quand l'utiliser** : agrégations (compter par statut, grouper par quartier), transformation d'une liste d'entités en `Map` indexée par une clé.

---

### 🔴 Fil rouge — Étape 5 : statistiques SignalCUA

- Nombre de réclamations par statut : `Collectors.groupingBy(Reclamation::getStatut, Collectors.counting())`.
- Liste des quartiers ayant au moins une réclamation `URGENTE` (`filter` + `map` + `distinct`).
- Réclamation la plus ancienne par quartier (`groupingBy` + `Collectors.minBy`).

---

## Partie 6 — Build Tools (Maven / Gradle)

**Pourquoi cette étape charnière, entre le langage pur et Spring Boot ?** Jusqu'ici, vous avez écrit du Java "à la main", probablement dans un seul dossier. Avant d'introduire Spring Boot (partie 7) — qui repose entièrement sur des dépendances externes téléchargées automatiquement — il faut un outil pour gérer ces dépendances et structurer un projet de façon standardisée. C'est le rôle de Maven (ou Gradle). Cette partie est volontairement courte : l'objectif n'est pas de maîtriser Maven en profondeur, seulement d'être autonome avec l'outil pour pouvoir vous concentrer sur Spring Boot ensuite.

### 6.1 Maven

**Essentiels**

- `pom.xml` : fichier central du projet, déclarant les `<dependencies>` (bibliothèques utilisées), les `<properties>` et la configuration de `<build>`.
- Cycle de vie standard : `compile` (compiler le code) → `test` (exécuter les tests) → `package` (produire un `.jar`) → `install`.
- Scopes de dépendance : `compile` (par défaut, disponible partout), `test` (uniquement pour les tests), `provided` (fourni par l'environnement d'exécution, pas embarqué).

**Erreurs à éviter**

- Copier-coller des dépendances trouvées en ligne sans comprendre les conflits de versions qu'elles peuvent introduire — `mvn dependency:tree` permet de visualiser l'arbre complet des dépendances et leurs versions résolues.
- Committer le dossier `target/` (contenant les fichiers compilés) dans Git — ajoutez-le systématiquement au `.gitignore`.
- Ne pas utiliser de **BOM** (*Bill of Materials*, ex : `spring-boot-dependencies`) : sans lui, les versions des différentes bibliothèques liées entre elles peuvent devenir incohérentes.

**Anti-patterns**

- Ajouter des dépendances "au cas où", jamais réellement utilisées : cela alourdit inutilement le build et augmente la surface d'exposition à des failles de sécurité connues (CVE) dans ces bibliothèques inutiles.

**Conseils de pro**

- Démarrez toujours un projet Spring Boot via **Spring Initializr** (start.spring.io), qui génère un `pom.xml` propre avec les bonnes dépendances de départ — n'écrivez jamais un `pom.xml` Spring Boot entièrement à la main.

**Quand l'utiliser** : Maven est le standard le plus répandu en entreprise, particulièrement adapté à votre contexte — plus lisible et prévisible que Gradle pour quelqu'un qui travaille souvent seul, sans l'aide d'une équipe pour déchiffrer une configuration complexe.

**Bonnes pratiques 2025-2026** : exécutez `mvn versions:display-dependency-updates` régulièrement, pour repérer les mises à jour de sécurité disponibles.

---

### 6.2 Gradle (aperçu)

**Essentiels**

- `build.gradle`/`build.gradle.kts` : plus rapide que Maven grâce à un système de cache incrémental, mais avec une syntaxe plus flexible donc potentiellement plus complexe à déboguer.

**Quand l'utiliser** : gros monorepos multi-modules, écosystème Kotlin, équipes qui privilégient la vitesse de build au-dessus de tout. Peu probable que ce soit une priorité pour vos projets CUA à court terme.

**🔍 À voir, pas à approfondir maintenant** : sachez que Gradle existe et à quoi il sert en une phrase ; vous l'apprendrez en profondeur seulement si un projet concret vous y oblige.

---

### 🔴 Fil rouge — Étape 6 : structurer SignalCUA

- Créez un vrai projet Maven (via Spring Initializr, même sans encore utiliser Spring) avec l'arborescence standard `src/main/java`, `src/test/java`.
- Migrez toutes les classes des étapes précédentes dans des packages cohérents (`fr.cua.signalcua.model`, `.repository`, `.exception`) — en réutilisant directement l'organisation apprise en 1.6.
- Ajoutez JUnit comme dépendance de test et écrivez un premier test simple sur le repository en mémoire (avant-goût de la partie 9).

---

## Partie 7 — Spring Boot & Dependency Injection

**Pourquoi cette partie est, de loin, la plus détaillée du guide ?** Tout ce que vous avez appris jusqu'ici — objets, collections, exceptions, build — était un prérequis pour arriver ici : c'est Spring Boot qui structurera concrètement 80% de votre travail quotidien de développeur backend. Une partie courte ici donnerait une fausse impression de simplicité sur ce qui est en réalité le cœur de votre objectif professionnel — d'où ses **onze sous-chapitres**, contre trois pour les exceptions.

### 7.1 Pourquoi un framework ? (avant même le "comment")

**Pourquoi cette sous-partie zéro, avant même de parler de code ?** Passer directement à `@Autowired` sans expliquer le problème que ça résout serait la source d'étonnement n°1 de tout ce guide : "pourquoi mes objets sont-ils créés tout seuls, sans que j'écrive `new` nulle part ?"

**Le problème concret** : dans SignalCUA (partie 6), votre `ReclamationService` a besoin d'un `ReclamationRepository`. Sans framework, vous devriez écrire vous-même, quelque part, `new ReclamationRepositoryMemoire()` puis le passer à `new ReclamationService(repository)`. Multipliez ça par des dizaines de classes qui dépendent les unes des autres, et cette "plomberie" devient vite un fardeau à maintenir manuellement.

**La solution Spring** : vous **déclarez** vos classes (via des annotations) et leurs dépendances (via leur constructeur), et c'est Spring qui se charge, au démarrage de l'application, de créer chaque objet et de le connecter aux autres automatiquement. C'est ce qu'on appelle l'**inversion de contrôle** (IoC) : ce n'est plus vous qui contrôlez la création des objets, c'est le framework — d'où son nom.

---

### 7.2 IoC & Dependency Injection

**Essentiels**

- Un objet géré par Spring s'appelle un **bean**.
- L'**injection de dépendances** (DI) est la technique concrète par laquelle Spring fournit à un bean les autres beans dont il a besoin — trois façons existent : par constructeur (recommandée), par *setter*, ou par champ (`@Autowired` directement sur un champ).
- Par défaut, chaque bean est un **singleton** : une seule instance partagée dans toute l'application (d'autres *scopes* existent, comme `prototype` — une nouvelle instance à chaque demande — ou `request`, une instance par requête HTTP, mais le singleton couvre la grande majorité des cas).

**Erreurs à éviter**

- L'injection **par champ** (`@Autowired private XxxService service;`) : cela semble plus court, mais rend les tests difficiles (impossible de fournir facilement un mock sans passer par Spring, contrairement à l'injection par constructeur — vous verrez la différence concrètement en partie 9.2) et masque les dépendances circulaires (voir ci-dessous) jusqu'à ce qu'elles causent un problème en production.
- Une dépendance **circulaire** entre deux beans (`ServiceA` a besoin de `ServiceB`, qui a lui-même besoin de `ServiceA`) : c'est un signal que le découpage entre les deux classes est à revoir, pas un problème à contourner avec une annotation spéciale.

**Anti-patterns**

- `@Autowired` sur des champs partout, sans jamais définir d'interfaces entre les couches (2.3) : cela crée un couplage fort qui rend les tests difficiles à mocker plus tard.

**Conseils de pro**

- L'injection **par constructeur** est le standard largement reconnu dans l'écosystème Spring depuis des années. Avec l'outil Lombok (mentionné en partie 13), l'annotation `@RequiredArgsConstructor` génère ce constructeur automatiquement, rendant cette approche aussi concise que l'injection par champ, sans ses inconvénients.

**Quand l'utiliser** : partout — c'est le mécanisme fondamental sur lequel repose tout le reste de Spring Boot.

**Bonnes pratiques 2025-2026** : Spring Boot 3.x repose sur Jakarta EE (annotations `jakarta.*`, et non plus `javax.*`) — un point de vigilance si vous suivez un tutoriel écrit avant 2023, qui utilisera probablement l'ancien nom de package.

---

### 7.3 Les stéréotypes : @Component, @Service, @Repository, @Controller

**Pourquoi une sous-partie dédiée, plutôt qu'une simple mention ?** Ces quatre annotations reviennent constamment dans tout code Spring Boot, et un débutant se demande souvent "quelle est la différence, techniquement elles font toutes la même chose ?" — méritant une clarification explicite pour éviter cet étonnement.

**Essentiels**

- `@Component` : l'annotation générique de base, qui dit à Spring "gère cette classe comme un bean".
- `@Service`, `@Repository`, `@Controller`/`@RestController` sont en réalité des **spécialisations** de `@Component` : techniquement quasi-identiques, mais chacune signale l'intention et le rôle de la classe dans l'architecture (7.5, 7.8), et `@Repository` ajoute en plus une conversion automatique de certaines erreurs techniques de base de données en exceptions Spring plus lisibles.

**Erreurs à éviter**

- Utiliser `@Component` partout par flemme, au lieu de la spécialisation adaptée : cela nuit à la lisibilité du projet pour vous-même et pour quiconque reprendra le code plus tard.

**Conseils de pro**

- Retenez cette règle simple : `@RestController` pour recevoir les requêtes (7.5), `@Service` pour la logique métier (7.8), `@Repository` pour l'accès aux données (partie 8) — cette correspondance annotation ↔ couche architecturale est la structure même de Spring Boot.

**Quand l'utiliser** : chaque nouvelle classe métier de votre application doit porter l'une de ces annotations, choisie selon son rôle réel.

---

### 7.4 Contrôleurs REST

**Pourquoi juste après les stéréotypes ?** Vous savez désormais qu'un `@RestController` est un `@Component` spécialisé pour "recevoir les requêtes" — voyons concrètement comment.

**Essentiels**

- `@RestController` : marque une classe comme point d'entrée HTTP, dont les méthodes retournent directement des données (converties automatiquement en JSON), pas des pages web.
- `@RequestMapping`, ou ses raccourcis `@GetMapping`/`@PostMapping`/`@PutMapping`/`@DeleteMapping` : associent une méthode Java à une route HTTP et un verbe précis.
- `@PathVariable` (valeur dans l'URL, ex : `/reclamations/{id}`), `@RequestParam` (paramètre de requête, ex : `?statut=NOUVELLE`), `@RequestBody` (corps JSON de la requête, désérialisé automatiquement en objet Java).
- `ResponseEntity<T>` : permet de contrôler explicitement le code de statut HTTP retourné (200, 201, 404...), plutôt que de laisser Spring en choisir un par défaut.

**Erreurs à éviter**

- Retourner directement des entités JPA (partie 8) dans les réponses REST plutôt que des DTOs (voir 7.9) : cela expose les détails internes de votre base de données à l'extérieur, et peut provoquer des boucles infinies de sérialisation sur des relations bidirectionnelles.
- Ignorer les codes HTTP, en retournant systématiquement un code 200 même pour signaler une erreur — le code HTTP fait partie du contrat de votre API, au même titre que les données elles-mêmes.

**Anti-patterns**

- Le **Fat Controller** : mettre de la logique métier, des requêtes de base de données, ou des règles de validation complexes directement dans le contrôleur. Le rôle du contrôleur se limite strictement à recevoir la requête, la valider superficiellement, et déléguer au service (7.8) — jamais davantage.

**Conseils de pro**

- La bibliothèque `springdoc-openapi` génère automatiquement une documentation Swagger/OpenAPI à partir de vos contrôleurs — très utile pour synchroniser le contrat d'API avec votre frontend Angular, y compris pour générer un client TypeScript.

**Quand l'utiliser** : toute exposition d'une API REST consommée par votre application Angular.

---

### 7.5 Validation des entrées

**Pourquoi une sous-partie séparée, et pas fondue dans les contrôleurs ?** La validation est un sujet suffisamment important et distinct (elle a son propre écosystème d'annotations) pour mériter sa place propre, plutôt que d'être noyée dans la présentation générale des contrôleurs.

**Essentiels**

- `@Valid` sur le paramètre `@RequestBody` d'un contrôleur, combiné à des annotations de *Bean Validation* sur les champs du DTO : `@NotNull`, `@NotBlank`, `@Size(min=..., max=...)`, `@Email`, `@Min`/`@Max`.
- Si la validation échoue, Spring lève automatiquement une exception (`MethodArgumentNotValidException`), qu'on gère proprement grâce au mécanisme de la sous-partie suivante.

**Erreurs à éviter**

- Valider uniquement côté Angular (formulaires réactifs) sans jamais revalider côté serveur : un appel direct à l'API (via un outil comme Postman, ou un client malveillant) contournerait totalement votre frontend — ne faites **jamais** confiance uniquement à la validation côté client.

**Conseils de pro**

- Pensez cette validation comme le miroir serveur de vos validators Angular Reactive Forms : les deux sont complémentaires, pas redondants — Angular offre une expérience utilisateur immédiate, Spring garantit l'intégrité réelle des données.

**Quand l'utiliser** : sur chaque DTO reçu en entrée d'un contrôleur, systématiquement.

---

### 7.6 Gestion centralisée des erreurs

**Pourquoi juste après la validation ?** La validation (7.5) est justement l'un des cas les plus fréquents où une exception doit être transformée en réponse HTTP compréhensible — ce mécanisme de transformation est donc la suite naturelle et indispensable de ce que vous venez de voir.

**Essentiels**

- `@ControllerAdvice` : une classe spéciale qui intercepte les exceptions levées par **n'importe quel** contrôleur de l'application, en un seul endroit centralisé.
- `@ExceptionHandler(MaException.class)` : à l'intérieur d'une classe `@ControllerAdvice`, associe un type d'exception précis à la réponse HTTP à produire.
- `ProblemDetail` (norme RFC 7807, native depuis Spring Boot 3) : un format JSON standardisé pour représenter une erreur (type, titre, détail, statut) — pratique et prévisible à consommer côté Angular.

**Erreurs à éviter**

- Gérer les erreurs contrôleur par contrôleur avec des blocs `try/catch` répétés partout : c'est exactement le problème que résout `@ControllerAdvice`, en centralisant cette logique une seule fois pour toute l'application.

**Conseils de pro**

- Faites le lien direct avec la partie 4 : chaque exception métier custom que vous créez (`ReclamationNotFoundException`) trouve ici sa traduction précise en réponse HTTP (`@ExceptionHandler(ReclamationNotFoundException.class)` → 404).

**Quand l'utiliser** : dès le premier contrôleur de votre application, sans exception — ne remettez jamais cette étape à plus tard.

---

### 7.7 Services & architecture en couches

**Pourquoi seulement maintenant, après les contrôleurs ?** Vous savez désormais ce qu'un contrôleur ne doit **pas** faire (7.4, le *Fat Controller*) — la question naturelle devient alors : où va vraiment la logique métier ? C'est le rôle de la couche service, présentée ici en réaction directe à ce que vous venez d'apprendre à éviter.

**Essentiels**

- Architecture classique en couches : `Controller` (7.4, reçoit/route) → `Service` (logique métier) → `Repository` (accès aux données, partie 8) → `Entity`/`DTO`.
- `@Transactional` : annotation qui garantit que plusieurs opérations de base de données au sein d'une même méthode réussissent **toutes ensemble**, ou échouent **toutes ensemble** (voir en détail en partie 8.5).

**Erreurs à éviter**

- Un seul `@Service` gigantesque qui gère tout le domaine métier ("God Service") : découpez par sous-domaine métier cohérent dès que la classe grossit.
- Oublier `@Transactional` sur une méthode qui enchaîne plusieurs écritures liées en base : sans elle, un échec au milieu de l'opération peut laisser les données dans un état incohérent (une écriture réussie, l'autre non).

**Anti-patterns**

- De la logique métier dupliquée entre le contrôleur et le service, par manque de discipline claire sur "qui fait quoi" dans l'équipe (ou, pour vous, dans vos propres habitudes de code).

**Quand l'utiliser** : c'est l'architecture standard de toute API Spring Boot, sans exception notable.

---

### 7.8 DTOs et mapping Entity ↔ DTO

**Pourquoi cette sous-partie distincte ?** Ce point a été mentionné plusieurs fois en passant (7.4, 7.5) — il mérite ici sa place propre pour en expliquer clairement le "pourquoi", avant que vous ne le pratiquiez concrètement en partie 8.

**Essentiels**

- Un **DTO** (*Data Transfer Object*) est un objet dédié uniquement au transport de données entre les couches — typiquement, entre votre API et le frontend Angular. Un **record** (2.4) est l'outil idéal pour l'écrire.
- Une **entité** (partie 8) est un objet dédié au mapping avec la base de données — ce sont deux responsabilités différentes, même si elles se ressemblent souvent en apparence.

**Erreurs à éviter**

- Fusionner entité et DTO "pour aller plus vite" : cela fonctionne au début, mais devient rapidement un piège dès que le modèle de données interne et le contrat d'API externe doivent évoluer différemment (ce qui arrive presque toujours, tôt ou tard).

**Conseils de pro**

- Une bibliothèque comme MapStruct (mentionnée en partie 13) automatise ce mapping répétitif Entity ↔ DTO une fois que vous en avez beaucoup — inutile au tout début, très utile dès que le projet grossit.

**Quand l'utiliser** : systématiquement, dès la première entité créée en partie 8 — ne repoussez jamais cette séparation "à plus tard".

---

### 7.9 Configuration & profils

**Pourquoi vers la fin de cette partie ?** La configuration devient concrètement utile une fois que vous avez quelque chose à configurer (une base de données, une URL externe) — la placer avant les sous-parties précédentes aurait été abstrait sans exemple réel à s'y raccrocher.

**Essentiels**

- `application.properties` ou `application.yml` : fichier de configuration centralisé (port, connexion base de données...).
- Les **profils** Spring (`dev`, `staging`, `prod`) permettent d'avoir une configuration différente selon l'environnement — l'équivalent direct des fichiers `environment.ts`/`environment.prod.ts` que vous connaissez déjà côté Angular.
- `@ConfigurationProperties` : permet de regrouper et typer proprement un ensemble de propriétés de configuration liées, plutôt que de disperser des `@Value("${...}")` un peu partout dans le code.

**Erreurs à éviter**

- Écrire des valeurs de configuration en dur directement dans le code Java, au lieu de les externaliser dans les fichiers de configuration.
- Committer des secrets (mot de passe de base de données, clé d'API) directement dans le dépôt Git — utilisez des variables d'environnement pour tout ce qui est sensible.

**Conseils de pro**

- Créez un fichier `application-{profil}.yml` par environnement, activé via la variable `SPRING_PROFILES_ACTIVE` — cohérent avec votre serveur de staging WSL existant, où vous pourrez ainsi distinguer facilement la configuration de test de celle de production.

**Quand l'utiliser** : dès le tout premier projet Spring Boot, sans exception.

**Bonnes pratiques 2025-2026** : configurez CORS explicitement (jamais `*` en production) pour autoriser précisément votre frontend Angular à consommer l'API ; activez Spring Boot Actuator (`/actuator/health`) pour un monitoring minimal dès le départ.

---

### 7.10 Spring Boot Actuator (observabilité minimale)

**Pourquoi une brève mention à ce stade, avant même d'avoir une vraie base de données ?** Une fois une API en état de fonctionner (ce qui sera le cas à l'issue du fil rouge de cette partie), il devient utile de savoir "est-ce que mon application tourne correctement ?" — un besoin simple qui mérite d'être planté dès maintenant, avant d'être développé plus largement en partie 13 (observabilité avancée).

**Essentiels**

- La dépendance `spring-boot-starter-actuator` ajoute des points de terminaison prêts à l'emploi, notamment `/actuator/health` (l'application répond-elle correctement ?).

**Quand l'utiliser** : dès qu'une application tourne sur un serveur, même de test — pour pouvoir vérifier rapidement qu'elle fonctionne sans avoir à tester manuellement chaque fonctionnalité.

**🔍 À voir, pas à approfondir maintenant** : Actuator propose bien d'autres points de terminaison (métriques détaillées, informations sur l'environnement...) — inutile de tous les explorer avant d'en avoir un besoin concret.

---

### 7.11 CORS pour un frontend Angular

**Pourquoi en toute fin de partie ?** C'est le tout dernier maillon avant que votre API Spring Boot puisse réellement être appelée depuis votre application Angular — la conclusion logique et pratique de cette longue partie.

**Essentiels**

- CORS (*Cross-Origin Resource Sharing*) est un mécanisme de sécurité du navigateur qui bloque par défaut les requêtes faites depuis une origine (ex : `localhost:4200`, votre Angular en dev) vers une autre origine (ex : `localhost:8080`, votre Spring Boot) — sauf autorisation explicite du serveur.
- `@CrossOrigin` sur un contrôleur, ou une configuration CORS globale, pour autoriser explicitement votre frontend.

**Erreurs à éviter**

- Autoriser `*` (toutes origines) en production : acceptable en développement local, jamais en production.

**Conseils de pro**

- En développement local, utilisez plutôt un proxy Angular (`proxy.conf.json`) qui redirige les appels API vers votre backend Spring Boot local, évitant ainsi de devoir gérer CORS à chaque test.

**Quand l'utiliser** : dès le premier appel réel entre votre frontend Angular et votre backend Spring Boot.

---

### 🔴 Fil rouge — Étape 7 : SignalCUA devient une API

- Transformez le projet en application Spring Boot (via Spring Initializr).
- `ReclamationController` (`@RestController`) → `ReclamationService` (`@Service`, reprend vos règles métier des parties précédentes) → `ReclamationRepository` (interface `@Repository`, implémentation en mémoire pour l'instant — la vraie base de données arrive en partie 8).
- Endpoints : `GET /reclamations`, `GET /reclamations/{id}`, `POST /reclamations`, `PUT /reclamations/{id}/statut`.
- DTOs (`record`) pour la création et la réponse, distincts du modèle interne, avec validation `@Valid`.
- Un `@ControllerAdvice` qui transforme vos exceptions métier de la partie 4 en réponses `ProblemDetail`.
- Testez avec Postman/curl, puis avec un vrai appel Angular via `HttpClient` (proxy `proxy.conf.json`).

---

## Partie 8 — Accès aux données : Spring Data JPA / Hibernate

**Pourquoi cette partie suit directement Spring Boot ?** Votre API de la partie 7 fonctionne, mais tout disparaît à chaque redémarrage puisque les données restent en mémoire. La suite logique et attendue est de les faire persister réellement — c'est le rôle de cette partie, la deuxième plus dense du guide après Spring Boot, car la persistance des données est le second pilier (avec les API) de presque toute application d'entreprise.

### 8.0 JDBC — ce qui se cache sous Spring Data JPA

**Pourquoi commencer par ce niveau bas, alors qu'on ne l'utilisera presque jamais directement ?** Pour éviter que Spring Data JPA ne ressemble à de la "magie" incompréhensible : comprendre une fois ce qui se passe en dessous rend tout ce qui suit (notamment le problème N+1 en 8.3) bien plus intuitif à diagnostiquer.

**Essentiels**

- JDBC (*Java Database Connectivity*) est l'API bas niveau standard pour dialoguer avec une base relationnelle : `DataSource`, `Connection`, `PreparedStatement`, `ResultSet`.
- Chaque système de gestion de base de données a son propre pilote JDBC : SQLite (léger, fichier local, pratique pour prototyper sans installer de serveur), MySQL, et **PostgreSQL** — le plus utilisé dans l'entreprise moderne, et le choix recommandé pour vos projets CUA pour sa robustesse et son écosystème.
- `PreparedStatement` avec des paramètres liés (`?`) protège nativement contre l'injection SQL, contrairement à la concaténation de chaînes.

**Erreurs à éviter**

- Concaténer une valeur saisie par un utilisateur directement dans une requête SQL (`"SELECT * WHERE nom = '" + nom + "'"`) : c'est une faille de sécurité critique, l'**injection SQL** — toujours utiliser des paramètres liés.
- Oublier de fermer `Connection`/`Statement`/`ResultSet` : fuite de ressources (utilisez `try-with-resources`, vu en 4.1).
- Écrire du JDBC brut partout dans un projet Spring Boot moderne : un piège pour un débutant qui ne connaît pas encore Spring Data JPA (8.1) et réinvente manuellement ce que l'ORM fait déjà correctement.

**Conseils de pro**

- SQLite est un excellent outil pour apprendre et prototyper localement sans serveur à installer ; passez à PostgreSQL dès que le projet devient réel (accès concurrents, volumétrie, fonctionnalités avancées).

**Quand l'utiliser** : directement, de moins en moins une fois Spring Data JPA maîtrisé — réservé à des cas très spécifiques (requêtes ultra-optimisées, outils d'administration bas niveau).

**🔍 À voir, pas à approfondir** : écrire un petit programme JDBC pur (connexion, `SELECT`, affichage) une seule fois pour comprendre le mécanisme suffit largement — inutile de devenir expert JDBC bas niveau si vous utilisez Spring Data JPA au quotidien, ce qui sera votre cas dans l'immense majorité de vos projets.

---

### 8.1 Entities & mapping

**Essentiels**

- `@Entity` marque une classe comme correspondant à une table en base ; `@Id`/`@GeneratedValue` désignent la clé primaire ; `@Column`/`@Table` précisent le mapping vers les colonnes/table si besoin.
- Mapping des types Java vers les colonnes SQL : `LocalDate`/`LocalDateTime` (partie 3.4) mappent naturellement vers des colonnes date/heure ; les `enum` (partie 2.5) via `@Enumerated`.

**Erreurs à éviter**

- Vouloir faire d'une entité JPA un `record` (2.4) : **techniquement impossible**, car Hibernate a besoin d'un constructeur vide et de la capacité de créer des objets "proxy" pour gérer le chargement différé (voir 8.3) — pour une entité, restez sur une classe classique, éventuellement allégée avec Lombok (partie 13).
- `@Enumerated(EnumType.ORDINAL)` : stocke la **position** de la valeur dans l'`enum`, exactement le piège déjà signalé en 2.5 — préférez systématiquement `EnumType.STRING`.

**Conseils de pro**

- Séparez toujours l'entité (mapping base de données) du DTO (contrat d'API, vu en 7.8), même si cela semble redondant au début — c'est ce choix qui vous protège le jour où le modèle de base de données et le modèle exposé à l'API doivent diverger, ce qui arrive presque toujours avec le temps.

**Quand l'utiliser** : toute donnée destinée à être persistée en base relationnelle.

---

### 8.2 Repositories

**Pourquoi juste après les entités ?** Une entité seule ne sert à rien sans un moyen de la lire/écrire en base — le repository est cette interface concrète, et fait directement écho au `Repository<T, ID>` générique maison que vous avez construit en partie 3.2 : ici, Spring en génère l'implémentation à votre place.

**Essentiels**

- `interface ReclamationRepository extends JpaRepository<Reclamation, Long> {}` : Spring génère automatiquement toutes les opérations CRUD de base, sans une seule ligne d'implémentation à écrire.
- Les *query methods* dérivées du nom de méthode (`findByStatut`, `findByQuartierAndStatut`) génèrent automatiquement la requête correspondante — Spring analyse le nom de la méthode pour en déduire la logique.
- `@Query` (en JPQL, un langage de requête proche de SQL mais orienté objet, ou en SQL natif) pour les cas trop complexes pour une *query method* dérivée.

**Erreurs à éviter**

- Écrire des requêtes SQL natives partout "parce que c'est plus simple à comprendre au début" : cela fait perdre la portabilité entre bases de données et une grande partie de l'intérêt de l'ORM.

**Quand l'utiliser** : la grande majorité des cas CRUD classiques. Pour des rapports complexes en lecture seule, une projection DTO directement en JPQL (plutôt que charger des entités complètes) est souvent plus performante.

---

### 8.3 Relations & le problème N+1

**Pourquoi cette sous-partie est cruciale, et placée ici précisément ?** C'est le piège de performance n°1 rencontré par tout débutant Spring Data JPA — il ne peut être compris qu'une fois les entités et repositories posés (8.1, 8.2), mais doit impérativement être vu **avant** que vous n'écriviez du code de production qui le déclenche sans le savoir.

**Essentiels**

- `@OneToMany`, `@ManyToOne`, `@ManyToMany` : les annotations qui décrivent les relations entre entités, en miroir des relations entre tables en base.
- `FetchType.LAZY` (la relation n'est chargée que si on y accède explicitement) vs `FetchType.EAGER` (chargée systématiquement en même temps que l'entité).
- Le **problème N+1** : boucler sur une liste de N entités et déclencher, pour chacune, une requête SQL séparée pour charger une relation *lazy* — au lieu d'une seule requête bien conçue, on en exécute N+1 (une pour la liste, une par élément).

**Erreurs à éviter**

- `FetchType.EAGER` par défaut sur toutes les relations : charge systématiquement des données potentiellement inutiles, dégradant les performances partout dans l'application.
- Accéder à une relation *lazy* en dehors d'une session Hibernate active (donc hors d'une méthode `@Transactional`, vue en 8.5) : provoque une `LazyInitializationException`.

**Conseils de pro**

- Activez les logs SQL dès le développement (`spring.jpa.show-sql=true`, `logging.level.org.hibernate.SQL=DEBUG`) pour repérer un problème N+1 tôt, pas une fois en production sous charge réelle.
- `JOIN FETCH` en JPQL, ou l'annotation `@EntityGraph`, permettent de charger explicitement les relations nécessaires en une seule requête bien construite.

**Quand l'utiliser** : à chaque relation que vous mappez, demandez-vous systématiquement "cette donnée liée sera-t-elle affichée en même temps que le parent, presque toujours ?" → alors `EAGER`/`JOIN FETCH`. Sinon → `LAZY`.

---

### 8.4 Migrations avec Flyway

**Pourquoi après les relations, et pas avant ?** Une fois que votre modèle d'entités (8.1) et leurs relations (8.3) commencent à se stabiliser, la question suivante devient : comment faire évoluer le schéma de la base de façon fiable et traçable, plutôt que de le modifier "à la main" à chaque changement ?

**Essentiels**

- Des scripts SQL **versionnés** (`V1__init.sql`, `V2__add_urgence.sql`), exécutés automatiquement et dans l'ordre au démarrage de l'application.
- Ne **jamais** utiliser `spring.jpa.hibernate.ddl-auto=update` en production : cette option laisse Hibernate modifier le schéma automatiquement selon vos entités, sans aucune traçabilité ni possibilité de revenir en arrière proprement.

**Erreurs à éviter**

- Modifier un script de migration déjà appliqué en production : Flyway détecte que son empreinte (*checksum*) a changé et bloque volontairement le démarrage — c'est une protection voulue, ne cherchez jamais à la contourner, créez plutôt un nouveau script de correction.

**Conseils de pro**

- C'est directement l'outil qui structure proprement ce que vous faites déjà "à la main" lors de vos migrations de données à la CUA — un des points de ce guide les plus transférables tels quels à votre travail actuel.

**Quand l'utiliser** : dès le premier projet avec une vraie base de données, sans aucune exception.

---

### 8.5 Transactions (`@Transactional`)

**Pourquoi une sous-partie dédiée, après avoir vu l'annotation en passant en 7.7 ?** `@Transactional` a été mentionnée rapidement en partie 7, mais son fonctionnement réel ne devient compréhensible qu'une fois que vous savez ce qu'est une relation (8.3) et une migration (8.4) — les cas concrets où elle s'applique.

**Essentiels**

- Une **transaction** garantit que plusieurs opérations de base de données réussissent **toutes ensemble**, ou sont **toutes annulées** ensemble en cas d'échec au milieu (propriété dite "tout ou rien").
- `@Transactional` sur une méthode de service (7.7) délimite cette frontière : toutes les opérations de base de données effectuées à l'intérieur de la méthode font partie de la même transaction.

**Erreurs à éviter**

- Oublier `@Transactional` sur une méthode qui enchaîne, par exemple, la mise à jour d'une réclamation **et** l'enregistrement d'un historique lié : un échec au milieu laisserait la réclamation modifiée sans son historique correspondant, un état incohérent.
- Placer `@Transactional` sur une méthode du contrôleur plutôt que du service : la transaction doit encadrer la logique métier (couche service), pas la couche de réception des requêtes.

**Quand l'utiliser** : toute méthode de service qui effectue plusieurs écritures liées en base de données.

---

### 8.6 Projections DTO pour les rapports

**Pourquoi cette dernière sous-partie de la partie 8 ?** Elle relie directement deux notions déjà vues séparément — les DTOs (7.8) et JPQL (8.2) — pour un cas d'usage très concret que vous rencontrerez vite : générer des statistiques/rapports sans charger inutilement des entités complètes.

**Essentiels**

- Une requête JPQL peut construire directement un `record` (DTO) plutôt qu'une entité complète, en ne sélectionnant que les colonnes réellement nécessaires au rapport.

**Conseils de pro**

- Réservez cette technique aux rapports/vues en lecture seule sur de gros volumes, où charger des entités complètes (avec toutes leurs relations potentielles) serait un gaspillage de performance inutile.

**Quand l'utiliser** : tableaux de bord, exports, statistiques — tout affichage agrégé qui ne nécessite pas la richesse complète d'une entité.

---

### 🔴 Fil rouge — Étape 8 : SignalCUA persiste en PostgreSQL

- `Reclamation` devient une `@Entity`, avec une relation `@ManyToOne` vers une nouvelle entité `Citoyen`.
- Remplacez le repository mémoire par un vrai `JpaRepository`.
- Ajoutez Flyway avec un script de migration initial, puis un second script ajoutant une colonne `urgente`.
- Repérez volontairement un problème N+1 (listez les réclamations avec leur citoyen sans `JOIN FETCH`, observez le nombre de requêtes SQL dans les logs), puis corrigez-le.
- Ajoutez `@Transactional` sur la méthode de service qui change le statut d'une réclamation **et** enregistre une ligne d'historique.

---

## Partie 9 — Tests (JUnit, Mockito, Testcontainers)

**Pourquoi cette partie vient-elle après Spring Boot et JPA, plutôt qu'en partie 6 ?** On pourrait tester du Java pur bien plus tôt — mais les techniques les plus utiles au quotidien (mocker un service, tester un contrôleur, tester un vrai repository) n'ont de sens concret qu'une fois que ces éléments existent réellement dans votre projet, ce qui n'est le cas qu'à partir d'ici.

### 9.1 JUnit

**Essentiels**

- `@Test` marque une méthode comme un test exécutable ; `@BeforeEach`/`@AfterEach` s'exécutent avant/après chaque test ; `@ParameterizedTest` exécute le même test avec plusieurs jeux de données.
- Assertions : `assertEquals(attendu, obtenu)`, `assertTrue(...)`, `assertThrows(MonException.class, () -> ...)`.

**Erreurs à éviter**

- Ne tester que le "chemin heureux" (*happy path*), jamais les cas d'erreur ou les valeurs limites.
- Des assertions vagues (`assertTrue(resultat != null)`) au lieu de vérifier réellement le contenu attendu.

**Conseils de pro**

- Structure **AAA** (*Arrange, Act, Assert*) systématique dans chaque test : préparer les données, exécuter l'action testée, vérifier le résultat — cette discipline rend chaque test immédiatement lisible par vous-même des mois plus tard.

**Quand l'utiliser** : toute logique métier dans vos services (7.7), mapping, validations — écrits **en même temps** que le code, pas après coup.

---

### 9.2 Mockito

**Pourquoi juste après JUnit ?** JUnit seul suffit pour tester une méthode isolée sans dépendance — mais votre `ReclamationService` (7.7) dépend d'un `ReclamationRepository` (8.2). Pour le tester **sans** avoir besoin d'une vraie base de données, il faut un outil capable de simuler cette dépendance : c'est exactement le rôle de Mockito, et c'est précisément l'intérêt d'avoir injecté cette dépendance par interface et par constructeur (7.2) plutôt que de l'avoir codée en dur.

**Essentiels**

- `@Mock` crée une version simulée d'une dépendance (par exemple, le repository) ; `@InjectMocks` crée l'objet réellement testé (le service) en lui injectant automatiquement les mocks déclarés.
- `when(mock.methode(...)).thenReturn(valeur)` définit le comportement simulé ; `verify(mock).methode(...)` vérifie qu'une méthode a bien été appelée.

**Erreurs à éviter**

- Le *over-mocking* : simuler à l'excès, au point que le test ne vérifie plus rien de réellement significatif sur le comportement du service.
- Des tests fragiles, couplés aux détails d'implémentation interne plutôt qu'au comportement observable de l'extérieur — un test devrait survivre à un refactoring qui ne change pas le comportement.

**Conseils de pro**

- Mockez uniquement les dépendances **externes** à la classe testée (le repository, un autre service) — jamais la classe que vous êtes en train de tester elle-même.

**Quand l'utiliser** : tests unitaires de services qui dépendent de repositories ou d'autres services.

---

### 9.3 Tests d'intégration avec Testcontainers

**Pourquoi en dernier, après les tests unitaires ?** Les tests unitaires (avec des mocks) vérifient la logique métier isolément, mais jamais que votre application fonctionne réellement **avec une vraie base de données** ou un **vrai flux HTTP complet** — c'est précisément la limite que ce dernier niveau de test vient combler.

**Essentiels**

- `@SpringBootTest` démarre le contexte Spring complet pour le test, en conditions proches du réel.
- **Testcontainers** démarre un vrai conteneur Docker PostgreSQL (ou MySQL) le temps du test, plutôt que d'utiliser une base H2 en mémoire — H2 a en effet des différences de comportement SQL subtiles par rapport à une vraie base PostgreSQL, ce qui peut cacher des bugs qui n'apparaîtront qu'en production.

**Erreurs à éviter**

- Utiliser `@SpringBootTest` pour tester une simple méthode utilitaire sans dépendance : c'est lent et disproportionné — réservez ce niveau de test aux vrais flux d'intégration.
- Une dépendance entre deux tests (l'un doit s'exécuter après l'autre pour fonctionner) : chaque test doit rester indépendant et rejouable seul, dans n'importe quel ordre.

**Conseils de pro**

- Particulièrement précieux pour vous, qui travaillez souvent seul sans relecture d'équipe possible : les tests deviennent votre principal filet de sécurité contre les régressions, remplaçant en partie le regard d'un collègue.

**Quand l'utiliser** : repositories JPA (8.2), contrôleurs REST complets (7.4), flux métier critiques (une transition de statut de réclamation, par exemple).

---

### 🔴 Fil rouge — Étape 9 : SignalCUA testé

- Tests unitaires du `ReclamationService` avec Mockito (mock du repository).
- Test d'intégration du `ReclamationRepository` avec Testcontainers PostgreSQL, incluant une vérification qu'il n'y a **pas** de problème N+1 (comptez les requêtes SQL générées dans les logs).
- Test d'intégration du contrôleur (`@SpringBootTest` + `MockMvc`) sur le flux complet de création d'une réclamation, de la requête HTTP jusqu'à la base de données.

---

## Partie 10 — Concurrency

**Pourquoi cette partie reste volontairement courte, et vient après les tests plutôt qu'avant ?** Dans une API Spring Boot classique orientée traitement de requêtes, Spring/Tomcat gère déjà l'exécution concurrente pour vous, requête par requête — vous n'avez presque jamais besoin de manipuler des threads manuellement. Ce sujet ne devient pertinent que pour des besoins précis (traitement asynchrone, tâches planifiées), d'où sa place après avoir consolidé le cœur de l'application (Spring Boot, JPA, tests).

### 10.1 Threads & ExecutorService

**Essentiels**

- `Runnable`/`Callable` représentent une tâche à exécuter ; `ExecutorService` gère un pool de threads réutilisables, plutôt que de créer un nouveau `Thread` à chaque besoin.
- `synchronized` et les verrous (*locks*) protègent un état partagé contre les accès simultanés incohérents.

**Erreurs à éviter**

- Créer des `new Thread()` manuellement en production, au lieu de passer par un `ExecutorService` qui gère proprement le nombre de threads actifs.
- Accéder à une collection non conçue pour la concurrence (`ArrayList`, `HashMap` — partie 3.1) depuis plusieurs threads simultanément, sans synchronisation : produit des bugs difficiles à reproduire, car dépendants du timing exact d'exécution.

**Quand l'utiliser** : traitement de gros volumes en parallèle (import/export massif de données).

---

### 10.2 `@Async` et `@Scheduled`

**Pourquoi juste après les threads bruts ?** Spring propose une façon bien plus simple d'obtenir de l'asynchrone dans une application Spring Boot, sans manipuler directement les outils bas niveau de 10.1 — la suite logique une fois le principe général compris.

**Essentiels**

- `@EnableAsync` (à activer une fois dans la configuration) + `@Async` sur une méthode : l'exécute de façon asynchrone, sans bloquer l'appelant.
- `@Scheduled` : exécute une méthode automatiquement selon un planning donné (par exemple, une fois par jour).

**Erreurs à éviter**

- Appeler une méthode `@Async` depuis une autre méthode **de la même classe** : le mécanisme Spring qui rend l'appel asynchrone repose sur un "proxy" externe à la classe, qui ne s'active pas pour un appel interne — un piège classique connu sous le nom d'*auto-invocation*.

**Conseils de pro**

- Cas d'usage concret pour vous : envoyer une notification (email/SMS) au citoyen quand le statut de sa réclamation change, sans faire attendre la réponse HTTP de la requête qui a déclenché ce changement.

**Quand l'utiliser** : notifications, tâches planifiées (rapports quotidiens), traitement différé non bloquant.

---

### 10.3 Virtual Threads

**Pourquoi en dernier dans cette partie ?** C'est une évolution récente qui bénéficie à tout ce qui précède, sans changer votre code — une conclusion naturelle plutôt qu'un nouveau concept à apprendre en profondeur.

**Essentiels**

- Les *Virtual Threads* (Java 21+) sont des threads très légers, gérés directement par la JVM, activables en Spring Boot via `spring.threads.virtual.enabled=true`.

**Conseils de pro**

- Gain de performance important pour les applications dont l'activité principale consiste à attendre des réponses (base de données, appels réseau — typiquement une API REST) sans que vous ayez à modifier votre code métier.

**Quand l'utiliser** : quasiment par défaut sur toute nouvelle application Spring Boot 3.2+ tournant sur Java 21+, sans contre-indication connue pour un usage classique.

---

### 🔴 Fil rouge — Étape 10 : notifications asynchrones

- Ajoutez une méthode `@Async notifierCitoyen(Reclamation r)`, appelée automatiquement quand le statut change (simulez l'envoi, pas besoin d'un vrai serveur mail à ce stade — voir partie 13 pour Spring Mail).
- Ajoutez une tâche `@Scheduled` qui génère un petit rapport quotidien du nombre de réclamations résolues.

---

## Partie 11 — Logging

**Pourquoi cette partie, courte, arrive-t-elle ici ?** Votre application fait désormais des choses en arrière-plan (partie 10) et manipule une vraie base de données (partie 8) — deux sources fréquentes de comportements difficiles à observer sans un vrai système de journalisation. Le sujet est volontairement bref : contrairement à Spring Boot, c'est un mécanisme simple, avec peu de pièges une fois compris.

### 11.1 SLF4J / Logback

**Essentiels**

- SLF4J est une **façade** de journalisation standard (l'interface que vous utilisez dans votre code), tandis que **Logback** (l'implémentation par défaut de Spring Boot) fait le travail réel en arrière-plan. Une alternative existe, **Log4j2** (successeur du très répandu Log4j historique), interchangeable sans changer votre code tant que vous programmez contre l'interface SLF4J.
- Niveaux de gravité, du moins au plus critique : `TRACE < DEBUG < INFO < WARN < ERROR`.

**Erreurs à éviter**

- `System.out.println()` au lieu d'un vrai logger : aucun contrôle du niveau de gravité, aucune rotation automatique de fichiers, aucun format structuré exploitable.
- Journaliser des données sensibles (mots de passe, données personnelles) — un point de vigilance particulièrement important vu que vous gérez des données d'état civil.

**Conseils de pro**

- `private static final Logger log = LoggerFactory.getLogger(MaClasse.class);` dans chaque classe, ou l'annotation `@Slf4j` (Lombok, partie 13) pour éviter cette ligne répétitive.
- Spring Boot embarque Logback par défaut, largement suffisant pour la majorité des projets — ne migrez vers Log4j2 que face à un besoin précis (performance de journalisation asynchrone très élevée, ou un existant déjà en Log4j).

---

### 11.2 Logging structuré et corrélation

**Pourquoi cette sous-partie ferme la partie logging ?** Une fois les bases posées, la vraie difficulté en production n'est pas d'écrire des logs, mais de **retrouver** les bons logs pour un incident précis, parmi des milliers de lignes générées par de multiples utilisateurs simultanés — le sujet naturel de conclusion.

**Essentiels**

- Le **MDC** (*Mapped Diagnostic Context*) permet d'enrichir automatiquement chaque ligne de log avec un contexte partagé pour toute la durée d'une requête (par exemple, un identifiant de corrélation unique).
- En production, un format de log **structuré** (JSON) facilite grandement la recherche et l'agrégation, comparé à du texte libre.

**Quand l'utiliser** : dès qu'une application tourne en production avec plusieurs utilisateurs simultanés — indispensable pour diagnostiquer un incident sans pouvoir le reproduire soi-même en local.

---

### 🔴 Fil rouge — Étape 11 : traçabilité SignalCUA

- Ajoutez des logs `INFO` sur les créations/changements de statut, `WARN` sur les tentatives invalides, `ERROR` sur les échecs inattendus.
- Ajoutez un identifiant de corrélation (*request ID*) via un filtre servlet, propagé automatiquement dans le MDC pour chaque requête.

---

## Partie 12 — Sécurité (Spring Security, JWT)

**Pourquoi cette partie arrive-t-elle en avant-dernière position, et pas plus tôt ?** Il aurait été prématuré de sécuriser une API avant qu'elle n'existe réellement (partie 7) et fonctionne de bout en bout avec de vraies données (partie 8). Maintenant que SignalCUA est fonctionnelle, robuste et observable, il est temps de la protéger — c'est l'étape logique qui précède un vrai déploiement.

> **Remarque sur cette partie** : elle n'apparaît pas dans la roadmap.sh d'origine, mais toute API exposée — même sur un réseau interne municipal — en a besoin dès qu'elle manipule des données non publiques, ce qui est votre cas.

### 12.1 Spring Security — les bases

**Essentiels**

- `SecurityFilterChain` : la configuration moderne de Spring Security (elle remplace l'ancienne classe `WebSecurityConfigurerAdapter`, aujourd'hui dépréciée — un point de vigilance si vous suivez un tutoriel un peu ancien).
- **Authentification** (qui êtes-vous ?) et **autorisation** (qu'avez-vous le droit de faire ?) sont deux étapes distinctes et successives.
- `@PreAuthorize("hasRole('AGENT')")` protège une méthode selon le rôle de l'utilisateur authentifié.

**Erreurs à éviter**

- Désactiver CSRF ou CORS "pour que ça marche" sans comprendre pourquoi c'était activé par défaut — ces protections existent pour de vraies raisons de sécurité, ne les retirez jamais sans en comprendre précisément l'implication.
- Stocker des mots de passe en clair, ou avec un algorithme de hachage faible (MD5, SHA1) : utilisez `BCryptPasswordEncoder`, l'algorithme recommandé par défaut dans Spring Security.

**Conseils de pro**

- Commencez simple : une seule règle globale ("tout est protégé, sauf `/auth/login`") plutôt que des règles fines réparties partout dès le départ — vous affinerez au fur et à mesure des besoins réels.

**Quand l'utiliser** : dès qu'une API expose des données non publiques — donc quasiment toujours dans votre contexte de données citoyennes.

---

### 12.2 JWT / OAuth2

**Pourquoi juste après les bases de Spring Security ?** Une fois qu'on sait **protéger** un endpoint (12.1), la question suivante est concrète : comment l'utilisateur prouve-t-il son identité à chaque requête ? JWT est la réponse la plus courante pour une API stateless consommée par un frontend comme Angular.

**Essentiels**

- Un **JWT** (*JSON Web Token*) est un jeton signé contenant des informations d'identité et de rôles, sans nécessiter de session conservée côté serveur (*stateless*).
- **OAuth2** est un protocole standard de délégation d'autorisation, utile quand on veut déléguer l'authentification à un système externe (par exemple, un fournisseur d'identité gouvernemental).

**Erreurs à éviter**

- Mettre des données sensibles dans le contenu (*payload*) d'un JWT : il est **signé** (garantissant qu'il n'a pas été altéré), mais pas **chiffré** — n'importe qui peut le décoder et le lire.
- Un token JWT sans expiration, ou avec une durée de validité trop longue : augmente le risque en cas de vol du jeton.

**Quand l'utiliser** : JWT est le choix par défaut le plus courant pour une API stateless consommée par un frontend Angular — votre cas typique. OAuth2 devient pertinent seulement si vous devez déléguer l'authentification à un système externe (par exemple un SSO gouvernemental).

---

### 🔴 Fil rouge — Étape 12 : sécuriser SignalCUA

- Ajoutez un endpoint `/auth/login` qui retourne un JWT.
- Protégez les endpoints de modification (`POST`/`PUT`) pour les agents authentifiés uniquement ; laissez la consultation publique.
- Testez le flux complet depuis Angular : connexion → stockage du token → `HttpInterceptor` qui l'ajoute automatiquement à chaque requête sortante.

---

## Partie 13 — Le monde au-delà de la roadmap : à voir, pas à apprendre maintenant

**Pourquoi cette partie ferme le guide, plutôt que d'être dispersée dans chaque chapitre ?** Vous avez désormais une application complète, testée et sécurisée — le bon moment pour prendre du recul et voir la carte complète de l'écosystème, sans risquer de vous éparpiller pendant l'apprentissage des fondations. La roadmap.sh se concentre volontairement sur le langage et son écosystème direct ; voici ce qu'elle passe sous silence, avec une priorité réaliste pour votre contexte.

| Sujet | En une phrase | Priorité pour vous | Quand vous y serez probablement confronté |
| --- | --- | --- | --- |
| **Docker** | Conteneuriser l'application pour un déploiement reproductible | 🟠 À voir bientôt | Vous avez déjà un serveur staging WSL — la suite logique est de conteneuriser vos apps Spring Boot dessus |
| **Spring Batch** | Framework dédié aux traitements de données volumineux par lots | 🟠 À voir bientôt | Directement pertinent pour vos migrations de données CUA (état civil) |
| **Spring Mail** | Envoi d'emails depuis l'application | 🟠 À voir bientôt | Notifications citoyennes, alertes internes — évoqué en fil rouge de la partie 10 |
| **Lombok** | Génère automatiquement getters/setters/constructeurs par annotations | 🟢 Pertinent, facile à adopter | Utile dès maintenant pour réduire le code répétitif sur les entités JPA (qui ne peuvent pas être des records) |
| **MapStruct** | Génère automatiquement le mapping Entity ↔ DTO | 🟢 Pertinent, facile à adopter | Dès que les conversions Entity/DTO (partie 7.8, 8.1) deviennent répétitives |
| **CI/CD** (GitHub Actions, GitLab CI, Jenkins) | Automatiser build/tests/déploiement | 🟠 À voir bientôt | Directement applicable dès que vous avez des tests (partie 9) — gain de fiabilité important pour du travail en solo |
| **i18n (internationalisation)** | Gérer plusieurs langues dans une même application | 🟢 Pertinent mais pas urgent | Contexte malgache : français/malgache dans les interfaces citoyennes |
| **Génération de PDF/rapports** (JasperReports, iText, Apache PDFBox) | Générer des documents officiels (actes, rapports) | 🟢 Pertinent mais pas urgent | Très probable pour l'état civil ou des rapports administratifs |
| **Redis / caching** | Cache mémoire distribué pour accélérer les lectures fréquentes | 🟢 Pertinent mais pas urgent | Utile si un dashboard/rapport devient lent à cause de requêtes répétées coûteuses |
| **Observabilité avancée** (Micrometer, Prometheus, Grafana, OpenTelemetry) | Monitoring et traçage détaillé en production, au-delà de l'Actuator de base (7.10) | 🔵 À connaître de nom | Pertinent une fois qu'une application critique tourne en production sans supervision manuelle possible |
| **Design patterns (GoF) & architecture avancée** (Hexagonale, Clean Architecture, DDD) | Vocabulaire et structures de conception pour de plus gros projets | 🟢 Pertinent mais pas urgent | Utile une fois les bases Spring Boot bien solides |
| **WebSockets / STOMP** | Communication temps réel bidirectionnelle | 🔵 À connaître de nom | Utile si un jour un dashboard CUA doit afficher des mises à jour en temps réel |
| **Kafka / RabbitMQ** (files de messages) | Communication asynchrone entre services découplés | 🔵 À connaître de nom | Pertinent seulement en architecture microservices — peu probable à court terme pour la CUA |
| **Spring WebFlux / programmation réactive** | Modèle non-bloquant pour très haute charge concurrente | 🔵 À connaître de nom | Rarement nécessaire pour une application administrative classique ; Spring MVC + Virtual Threads (10.3) suffit dans l'immense majorité des cas |
| **Microservices** (patterns : *circuit breaker*, *service discovery*...) | Découper une application en plusieurs services indépendants | 🔵 À connaître de nom | Seulement si la CUA grandit vers plusieurs équipes/services séparés — un monolithe bien structuré (comme SignalCUA) est souvent le bon choix à votre échelle |
| **Kubernetes** | Orchestration de conteneurs à grande échelle | 🔵 À connaître de nom | Pertinent seulement à partir d'une certaine échelle d'infrastructure ; Docker (voire Docker Compose) suffit largement pour l'instant |
| **GraphQL** | Alternative à REST pour des requêtes de données flexibles | 🔵 À connaître de nom | Rarement nécessaire sauf besoins de requêtage très complexes côté frontend |
| **gRPC** | Communication binaire performante entre services | 🔵 À connaître de nom | Peu probable dans votre contexte actuel |
| **Multi-tenancy** | Une même application servant plusieurs organisations isolées | 🔵 À connaître de nom | Peu probable (une seule commune) |

**Légende des priorités**

- 🟠 **À voir bientôt** : probablement utile dans les 3-6 prochains mois de vos projets CUA actuels.
- 🟢 **Pertinent mais pas urgent** : utile un jour, sans raison de se précipiter.
- 🔵 **À connaître de nom seulement** : comprendre le principe en une phrase, pour ne pas être perdu si le sujet apparaît en réunion ou dans un article — sans l'approfondir maintenant.

---

## Partie 14 — Pont Angular ↔ Spring Boot (synthèse)

**Pourquoi cette dernière partie, alors que les ponts Angular ont déjà été mentionnés tout au long du guide ?** Chaque pont a été introduit au moment précis où il devenait pertinent (DTOs en 7.8, CORS en 7.11, JWT en 12.2...) — plutôt que de vous les faire retenir hors contexte. Cette partie les rassemble en un seul endroit consultable, maintenant que vous avez tout le contexte nécessaire pour chacun.

### 14.1 Contrats de données

- Les DTOs Java en `record` (2.4, 7.8) sont l'équivalent le plus direct de vos interfaces TypeScript.
- `springdoc-openapi` (7.4) génère une spécification OpenAPI depuis vos contrôleurs, à partir de laquelle un client TypeScript peut être généré automatiquement — évite les désynchronisations manuelles entre backend et frontend.

### 14.2 Dates, pagination, erreurs

- Les dates `java.time` (3.4) se sérialisent nativement en ISO-8601, compatible directement avec `Date`/`date-fns` côté Angular.
- La pagination Spring Data (`Page<T>`, partie 8.2) se sérialise avec `content`, `totalElements`, `totalPages` — prévoyez le typage correspondant côté Angular.
- Les erreurs au format `ProblemDetail` (7.6) sont prévisibles et faciles à intercepter uniformément dans un `HttpInterceptor` Angular.

### 14.3 Authentification

- Le JWT (12.2) émis par le backend est stocké côté Angular, puis ajouté automatiquement à chaque requête sortante via un `HttpInterceptor`.

### 14.4 Environnement de développement

- CORS (7.11) configuré côté Spring Boot, complété par un proxy Angular (`proxy.conf.json`) en développement local pour éviter d'avoir à gérer CORS à chaque test.

---

## Résumé — les 5 pièges n°1 pour un débutant Java/Spring Boot

1. Injection par champ (`@Autowired` sur les champs) au lieu du constructeur (7.2).
2. Exposer les entités JPA directement dans l'API REST au lieu de DTOs (7.8, 8.1).
3. Le problème N+1 en JPA/Hibernate, relations *lazy* mal gérées (8.3).
4. Avaler les exceptions silencieusement, `catch (Exception e) {}` (4.1).
5. Logique métier dans les contrôleurs au lieu des services (7.4, 7.7).

Si vous évitez seulement ces 5 pièges dès le départ, vous serez déjà devant une bonne partie des développeurs Spring Boot auto-formés — et le projet **SignalCUA**, construit chapitre après chapitre, vous donne un terrain concret pour ancrer chaque notion avant de passer à la suivante.
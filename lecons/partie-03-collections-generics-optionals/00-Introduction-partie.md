# 00 — Introduction de la partie 3 : Collections, Generics, Optionals

> 🧭 **Pourquoi ce fichier ?** Les 4 leçons de cette partie définissent chaque terme nouveau dans leur mini-glossaire. Mais quelques notions **transversales** — le vocabulaire commun aux 4 leçons — apparaissent partout sans y être définies « à part entière ». Lisez ce fichier en premier, et revenez-y quand un mot vous échappe.

---

## 🧭 Le pont d'entrée : de la partie 2 à la partie 3

À la fin de la partie 2, SignalCUA possédait des objets solides : une `Reclamation` encapsulée, des familles de types (héritage, contrats, `sealed`), un statut fermé par un `enum StatutReclamation` et des priorités typées (`Priorite`). Mais le projet était encore **singulier** : une réclamation à la fois, rangée dans des **tableaux de taille fixe**, sans notion de temps. La partie 3 donne au projet ses « organes » de gestion :

| Leçon | Ce qu'elle ajoute | Le problème qu'elle règle |
|---|---|---|
| 01 — List, Set, Map, Queue | collections qui grandissent, index, unicité, clés, files | le tableau figé : taille fixe, aucune opération, recherche manuelle |
| 02 — Generics | `<T>`, bornes, wildcards, effacement de type | écrire le code **une seule fois** pour tous les types (et lire les bibliothèques) |
| 03 — Optional | l'absence explicite dans le type de retour | le `null` renvoyé silencieusement, dont l'erreur explose ailleurs |
| 04 — Date and Time API | `java.time`, durées, formatage, `Clock` | aucune notion de délai ni d'échéance ; les pièges de `Date`/`Calendar` |

Chaque leçon **s'appuie sur la précédente** : les collections (01) sont toutes génériques (02), `Optional` est un type générique (03) et le service de délais réutilise les recherches du registre (04). L'Étape 3 du fil rouge SignalCUA se construit au fil de ces leçons (voir `lecons/fil-rouge-signalcua.md`).

---

## 🛠️ Le vocabulaire transversal de la partie 3

| Terme | Définition complète |
|---|---|
| **Collection Framework** | L'ensemble d'interfaces (`List`, `Set`, `Map`, `Queue`, `Deque`) et d'implémentations (`ArrayList`, `HashMap`, `HashSet`, `ArrayDeque`…) du paquet `java.util`. Tableau de décision dans la leçon 01, section 2.6. |
| **Interface / implémentation** | Le **contrat** (« ce que je veux ») et le **moteur** (« comment c'est fait »). On déclare toujours l'interface, on instancie l'implémentation : `List<X> l = new ArrayList<>();` |
| **Élément / clé / valeur** | Dans une `List`/`Set` : l'**élément**. Dans une `Map` : la **clé** (identifiant unique) pointe vers la **valeur**. Noms de convention des paramètres de type : `E`, `K`, `V`, `T`. |
| **Ordre** | Trois sens à distinguer : **ordre d'insertion** (`LinkedHashSet`, `LinkedHashMap`), **ordre trié** (`TreeSet`, `TreeMap`), **aucun ordre** (`HashSet`, `HashMap`). Aucune collection « non ordonnée » ne garantit un ordre d'affichage. |
| **Complexité O(1) / O(n)** | Coût d'une opération : **constant** (accès direct, ex. `HashMap.get`) ou **proportionnel à la taille** (balayage, ex. recherche dans une `List`). Défini leçon 01, section 2.7. |
| **Doublon** | Deux éléments « égaux » au sens de `equals()`/`hashCode()`. Un `Set` les refuse ; une `List` les accepte. Les `record` fournissent ces deux méthodes automatiquement. |
| **Mutabilité / immuabilité** | **Mutable** : modifiable après création (`ArrayList`, ancien `Date`). **Immuables** : `List.of(...)`, les `record`, tous les types `java.time`, `Optional`, `String`. Règle par défaut 2025-2026 : préférer l'immuabilité. |
| **Copie défensive** | Renvoyer `List.copyOf(this.liste)` au lieu de la référence interne, pour que l'appelant ne puisse pas modifier l'intérieur d'un objet. Leçon 01, piège 3. |
| **Paramètre / argument de type** | `<T>` déclaré dans la classe ou la méthode = **paramètre** ; `String` dans `Boite<String>` = **argument**. Toute la leçon 02. |

| **Effacement de type (*erasure*)** | Les generics disparaissent après compilation : `List<String>` et `List<Integer>` sont la **même** classe à l'exécution. Explique trois interdits (`new T[]`, `instanceof List<String>`, `List<int>`). |
| **PECS** | *Producer Extends, Consumer Super* : lire d'un `? extends`, écrire dans un `? super`. Leçon 02, section 2.6. |
| **Invariance** | `List<String>` **n'est pas** une `List<Object>`, même si `String` est un `Object` : c'est une protection contre l'introduction d'un mauvais type. |
| **`null` / absence** | `null` = « rien », sans signal dans le type. `Optional<T>` = l'absence **est dans le type de retour** : le compilateur force l'appelant à décider. Toute la leçon 03. |
| **NPE (`NullPointerException`)** | Exception levée à l'appel d'une méthode sur `null` ; sa particularité : elle explose **loin** de sa cause. |
| **Avide / paresseux (*eager*/*lazy*)** | `orElse(x)` évalue `x` **avant** l'appel ; `orElseGet(() -> x)` ne l'évalue **que si nécessaire**. Démontré par l'exécution, leçon 03, section 2.5. |
| **Lambda** | Fonction écrite sur place : `r -> r.getDescription()`. Utilisée dès la partie 3 (`map`, `filter`, `orElseThrow`), **définie formellement** en partie 5. |
| **Horodatage** | L'instant auquel un événement a lieu (`dateDeclaration`). En Java : `LocalDateTime` pour l'heure métier, `Instant` pour le technique (leçon 04, section 2.2). |
| **Échéance / SLA** | L'**échéance** = date limite de traitement ; le **SLA** (*Service Level Agreement*) = engagement de délai (« urgences sous 4 h »). Échéance **calculée** ou **stockée** : leçon 04, section 2.8. |
| **UTC / fuseau horaire** | UTC = l'heure de référence mondiale ; un **fuseau** (`ZoneId`) = le décalage d'une région, avec ses exceptions (heure d'été). Un `Instant` est universel, un `LocalDateTime` est « local ». |
| **`Clock` / test déterministe** | L'horloge **injectée** rend le code temps-dépendant testable (`Clock.fixed` fige « maintenant »). Leçon 04, section 2.7 ; exploité en partie 9. |
| **Thread-safe** | Qui supporte plusieurs fils d'exécution simultanés. Les collections classiques ne le **sont pas** (partie 10) ; `DateTimeFormatter` l'est, `SimpleDateFormat` non. |
| **Classe *value-based*** | Classe dont l'identité mémoire ne compte pas : à comparer par `equals`, jamais par `==` (`Optional`, `LocalDate`, `Duration`…). |
| **Fil rouge** | Le projet **SignalCUA** qui évolue leçon après leçon pour ancrer chaque notion dans du concret. |

---

## 🧩 Ce que la partie 3 vous a fait construire

À la fin de la leçon 04, vous disposez d'une **Étape 3 du fil rouge complète** :

- un **`RegistreReclamations`** multi-structures (exercice 01) : `List` pour le parcours, `Map<Integer, Reclamation>` pour l'accès par identifiant, `Map<String, List<Reclamation>>` pour le regroupement par quartier, `TreeSet` pour les quartiers uniques et triés, `ArrayDeque` pour la file FIFO et `PriorityQueue` pour les urgences — chacune justifiée par un besoin ;
- des **copies défensives** (`List.copyOf`, `Collections.unmodifiableSet`) qui protègent l'intérieur du registre ;
- un outillage **générique** réutilisable (exercice 02) : `Boite<T>`, `Paire<K, V>`, `Historique<T>`, `premier`/`maximum`/`copierTout`/`compter` ;
- un registre **honnête sur l'absence** (leçon 03) : `Optional<Reclamation> findById(int)`, `premiereUrgente()`, `premiereDuQuartier(...)`, plus un `ServiceReclamations` qui traduit l'absence en exception métier (`orElseThrow`) ;
- un service de **SLA** (leçon 04) : `Reclamation.dateDeclaration`, échéance **calculée** (`dateDeclaration.plus(priorite.delaiMax())`), `ServiceDelais.estEnRetard`/`ecart`/`resumeSla` avec **horloge injectée** (`Clock`) pour un code testable.

➡️ **Prochaine étape** : la partie 4 — **Gestion des exceptions**. Jusqu'ici, SignalCUA levait des exceptions standard (`IllegalArgumentException`, `IllegalStateException`) et les attrapait en cas par cas. La partie 4 transforme ce réflexe en **stratégie** : hiérarchie d'exceptions métier, `try/catch/finally`, `try-with-resources`, et règles de propagation — le socle dont dépendent vos futures API REST (partie 7).


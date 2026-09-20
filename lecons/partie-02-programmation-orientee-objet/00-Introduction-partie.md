# 00 — Introduction de la partie 2 : la Programmation Orientée Objet

> 🧭 **Pourquoi ce fichier ?** Les 5 leçons de cette partie définissent chaque terme nouveau dans leur mini-glossaire. Mais quelques notions **transversales** — le vocabulaire commun aux 5 leçons — apparaissent partout sans y être défini « à part entière ». Lisez ce fichier en premier, et revenez-y quand un mot vous échappe.

---

## 🧭 Le pont d'entrée : de la partie 1 à la partie 2

À la fin de la partie 1, SignalCUA vivait en console avec une classe `Reclamation` **fonctionnelle mais fragile** : champs modifiables par n'importe qui, statut en `String` libre, une seule classe pour tout. La partie 2 reconstruit ces fondations en dur :

| Leçon | Ce qu'elle ajoute | Le problème qu'elle règle |
|---|---|---|
| 01 — Encapsulation & constructeurs | champs `private`, setters validés, `final` | l'objet « troué » que n'importe quel code corrompt |
| 02 — Héritage & polymorphisme | `extends`, `@Override`, polymorphisme | trois classes copiées-collées à maintenir |
| 03 — Interfaces & classes abstraites | contrats (`implements`), `abstract` | le contrat enfermé dans une seule famille |
| 04 — Records, sealed, pattern matching | données immuables, familles fermées, switch par type | 60 lignes pour quatre champs ; les cas oubliés en silence |
| 05 — Énumérations | `enum` + switch exhaustif | le statut `String` libre (« PLATANE » accepté) |

Chaque leçon **réutilise** la précédente : la `Reclamation` encapsulée (01) devient mère (02), signe des contrats (03), puis se réécrit en version moderne (04), et son statut devient un `enum` (05). L'Étape 2 du fil rouge SignalCUA se construit au fil de ces leçons (voir `lecons/fil-rouge-signalcua.md`).

---

## 🛠️ Le vocabulaire transversal de la partie 2

| Terme | Définition complète |
|---|---|
| **POO** | *Programmation Orientée Objet* : paradigme où le programme s'organise en **classes** (plans) produisant des **objets** (instances) qui collaborent. |
| **Classe / objet / instance** | Le plan de construction / la maison bâtie avec `new` / un synonyme d'« objet » (détail : leçon 04 de la partie 1). |
| **Membre** | Champ ou méthode d'une classe. |
| **État** | L'ensemble des valeurs des champs d'un objet à un instant donné. Protéger l'état = l'essence de l'encapsulation. |
| **Cohérence / invariant** | Fait qu'un objet reste toujours dans une configuration valide. Un « invariant » est une règle qui doit rester vraie toute la vie de l'objet (ex. « on ne résout jamais sans avoir traité »). |
| **Modificateur d'accès** | Mot-clé (`private`, *(rien)*, `protected`, `public`) définissant QUI peut utiliser un membre. Tableau complet dans la leçon 02 (section 2.5). |
| **Immutabilité / immuable** | Objet qui ne peut plus être modifié après création. Pratique par défaut en 2025-2026 (`final`, records). |
| **Contrat** | Métaphore de l'interface : la liste des méthodes qu'une classe promet de fournir, indépendamment du comment. |
| **Polymorphisme** | Une variable du type mère (ou du contrat) référence un objet fille/concret ; c'est la méthode de l'objet RÉEL qui s'exécute. |
| **Hiérarchie / famille** | L'ensemble d'une classe mère et de ses filles (`extends`). |
| **Famille fermée / scellée** | Hiérarchie (`sealed ... permits`) ou ensemble de valeurs (`enum`) dont la liste complète est connue du compilateur — qui vérifie alors l'exhaustivité des `switch`. |
| **Exhaustivité** | Garantie du compilateur : un `switch` couvre TOUS les cas possibles (toutes les valeurs d'un `enum`, tous les sous-types scellés). |
| **DTO** | *Data Transfer Object* : objet qui transporte des données entre deux couches (typiquement vers/depuis une API), sans logique métier. En 2025-2026 : toujours un `record`. |
| **Value object** | Objet défini par ses VALEURS : deux instances aux valeurs égales sont interchangeables (le `record` en fabrique). |
| **Composition** | Construire une classe en lui donnant des champs objets qu'elle utilise (« a un ») plutôt que d'en hériter (« est un »). |
| **Liskov (substitution)** | Principe : tout code fonctionnant avec la mère doit fonctionner avec n'importe quelle fille, sans surprise. |
| **Anemic Domain Model** | Anti-modèle : classe réduite à des getters/setters, sans logique métier — l'intelligence finit « en vrac » dans les services. |
| **Stringly typed** | Anti-pattern : des `String`/`int` éparpillés pour représenter un ensemble fini de valeurs, au lieu d'un `enum`. |
| **Singleton** | Objet unique partagé dans tout le programme. Chaque valeur d'un `enum` en est un. |
| **Proxy** | Doublon généré par un framework (Hibernate en partie 8) autour d'un objet réel — la raison pour laquelle les entités JPA ne peuvent pas être des `record`s. |
| **DI** | *Dependency Injection* (injection de dépendances) : le framework fournit les objets dont une classe a besoin, typés par interface. Le cœur de Spring (partie 7). |
| **Fil rouge** | Le projet **SignalCUA** qui évolue leçon après leçon pour ancrer chaque notion dans du concret. |

---

## 🧩 Ce que la partie 2 vous a fait construire

À la fin de la leçon 05, vous disposez d'une **Étape 2 du fil rouge complète** :

- une `Reclamation` encapsulée (champs `private`/`final`, transitions de statut contrôlées) ;
- une hiérarchie `Reclamation` → `ReclamationVoirie` / `ReclamationProprete` / `ReclamationEclairage` traitée par polymorphisme ;
- un contrat `Traitable` qui ouvre la boucle hors-famille (`Agent`) ;
- des versions modernes (`record`, `sealed`, pattern matching) pour les données et les états ;
- un `enum StatutReclamation` qui ferme définitivement le statut.

➡️ **Prochaine étape** : la partie 3 — **Collections, Generics, Optionals**. Jusqu'ici, SignalCUA gérait des tableaux de taille fixe ; la partie 3 introduit `List`, `Set`, `Map` (des collections qui grandissent), les **Generics** (`List<Reclamation>`), et `Optional` (représenter proprement une « absence » — ex. une réclamation introuvable).



# 00 — Introduction de la partie 1 : les notions transversales

> 🧭 **Pourquoi ce fichier ?** Les 7 leçons de cette partie définissent chaque terme nouveau dans leur mini-glossaire (« 📖 Vocabulaire »). Mais quelques notions **transversales** — celles de l'environnement de travail lui-même (outils, fichiers, machine) — apparaissent partout sans jamais avoir de « leçon d'accueil ». Ce fichier comble ce trou : lisez-le en premier, et revenez-y quand un mot « d'environnement » vous échappe.

---

## 🛠️ L'environnement : les mots qui entourent votre travail

| Terme | Définition complète |
|---|---|
| **JDK** (*Java Development Kit*) | La trousse à outils du développeur : il contient le **compilateur** (`javac`), la **JVM** (`java`) et la bibliothèque standard du langage. Sans JDK, on peut exécuter des programmes Java mais pas les écrire. Versions LTS recommandées en 2025-2026 : 21 et 25. |
| **JVM** (*Java Virtual Machine*) | La « machine virtuelle Java » : le programme qui **exécute** votre code compilé. C'est elle qui rend Java portable : le même `.class` tourne sur Windows, Linux ou macOS, car la JVM existe pour chaque système. |
| **Bytecode** | Le langage intermédiaire produit par le compilateur : ni du code source (`.java`), ni du code machine natif. La JVM lit le bytecode — c'est le contenu des fichiers `.class`. |
| **Fichier `.java`** | Votre code source, lisible par un humain. Un fichier = une classe publique (même nom). |
| **Fichier `.class`** | Le résultat de la compilation (`javac`) : du bytecode, lisible par la JVM. C'est LUI qu'on exécute réellement (`java MaClasse`). |
| **Compilation** | L'étape de traduction `.java` → `.class`. Le compilateur y vérifie les types (leçon 01) : la majorité des erreurs sont attrapées ICI, avant toute exécution — c'est le grand avantage de Java sur JavaScript. |
| **Terminal / console** | La fenêtre texte où l'on tape des commandes (`javac`, `java`...) et où les programmes console affichent leur sortie (`System.out`). |
| **CLI** (*Command Line Interface*) | Un programme qu'on pilote en tapant des commandes au clavier dans ce terminal — ce que SignalCUA est devenu à la leçon 07. |
| **IDE** (*Integrated Development Environment*) | L'atelier intégré du développeur : éditeur + compilation à la touche + autocomplétion + navigation dans le code. **VS Code** (avec l'« Extension Pack for Java ») ou **IntelliJ IDEA** sont les choix courants ; IntelliJ est le standard en entreprise Java. |
| **Classpath (`-cp`)** | La liste des endroits où la JVM cherche les `.class` au lancement. Leçon 06 : `java -cp build fr.cua.signalcua.app.MainSignal` dit « cherche dans le dossier `build/` ». |
| **Variables d'environnement** | Des réglages globaux du système que les outils lisent. `JAVA_HOME` (chemin du JDK) et `PATH` (où trouver les commandes `java`/`javac`) sont les deux classiques ; si `java -version` échoue, ce sont elles qu'on vérifie en premier. |
| **Extension vs application** | Dans VS Code, une **extension** est un module ajouté à l'éditeur (ex. « Extension Pack for Java ») ; ne pas confondre avec votre application à vous. |

---

## 🧩 Les concepts Java qui traversent toute la partie

| Terme | Rappel en une ligne (détails dans la leçon indiquée) |
|---|---|
| **Typage statique** | Le type de chaque variable est fixé à la compilation et vérifié avant l'exécution — la « strictitude » vue en leçon 01. |
| **Primitif vs objet / référence** | La valeur dans la boîte vs le reçu qui pointe vers le coffre (leçon 01). |
| **Immuabilité** | Ne peut pas être modifié après création (`String`, leçon 03 ; `final`, leçon 01). |
| **Classe / objet / instance** | Le plan vs la maison bâtie avec `new` (leçon 04). |
| **Champ / méthode / constructeur** | Les données / les comportements / la porte contrôlée d'une classe (leçon 04). |
| **`static` vs instance** | Appartient à la classe entière vs à chaque objet (leçon 05). |
| **Package** | L'adresse postale d'une classe, reflétant l'arborescence des dossiers (leçon 06). |
| **Exception** | Une erreur d'exécution signalée par un plantage contrôlé, rattrapable avec `try/catch` (leçon 07, approfondie en partie 4). |
| **Fil rouge** | Le projet **SignalCUA** qui évolue d'une leçon à l'autre pour ancrer chaque notion dans du concret (`lecons/fil-rouge-signalcua.md`). |

---

## 🗺️ Où en êtes-vous à la fin de la partie 1 ?

Vous savez : écrire et exécuter un programme Java ; stocker des données typées ; décider (`if`, `switch`) et répéter (boucles) ; manipuler tableaux, `ArrayList` et `String` ; concevoir une classe avec constructeur validé et méthodes ; utiliser `static` à bon escient ; organiser le code en packages ; dialoguer au clavier avec un `Scanner`. **Et votre fil rouge SignalCUA tourne en console, sans framework** — exactement l'état visé par la phase « console » de la roadmap.

➡️ **La suite** : la partie 2 — Programmation Orientée Objet (encapsulation, héritage, interfaces, records, enum).

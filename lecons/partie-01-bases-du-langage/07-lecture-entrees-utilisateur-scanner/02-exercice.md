# Exercice 07 — SignalCUA interactif (Fil rouge — fin de l'Étape 1)

> 🧭 **Où en sommes-nous ?** La leçon 07 (`01-lecon.md`) a donné la voix à votre programme : le `Scanner`. Cet exercice finalise l'Étape 1 du fil rouge : SignalCUA devient un menu console interactif qui crée de vraies réclamations saisies au clavier. Toutes les briques des leçons 01-06 sont réutilisées. Correction dans `03-correction.md`.

## Étape 0 — Préparer

Reprenez le projet packagé de la leçon 06 :

```text
src/fr/cua/signalcua/model/Reclamation.java   (avec OutilInterne)
src/fr/cua/signalcua/app/MainSignal.java
```

Exécution :

```bash
javac -d build $(find src -name "*.java")   # compile tous les .java trouvés
java -cp build fr.cua.signalcua.app.MainSignal
```

## Étape 1 — Le menu

Dans `MainSignal`, écrivez une boucle `do-while` qui affiche ce menu (text block) et lit le choix :

```text
=== SIGNALCUA ===
1. Signaler un probleme
2. Lister les signalements
0. Quitter
Votre choix :
```

Le programme continue tant que le choix n'est pas `0`. Utilisez un `switch` pour router les cas.

## Étape 2 — Saisir une réclamation (choix 1)

1. Demandez la **description** puis le **quartier** (`nextLine()` + `.trim()`).
2. Créez la `Reclamation` avec un id auto-incrémenté (`prochainId++`).
3. Encadrez la création d'un `try/catch` : si le constructeur refuse (description vide), affichez `"Refus : " + e.getMessage()` au lieu de planter.

## Étape 3 — Lister (choix 2)

Affichez le nombre de réclamations puis chacune via `r.afficher()`. Si la liste est vide, affichez `Aucun signalement`.

## Étape 4 — Blindage du menu (le défi)

Le menu utilise `nextInt()` : que se passe-t-il si l'utilisateur tape `abc` ? **Testez-le** — le programme plante. Corrigez avec la garde `hasNextInt()` (boucle `while` de redemande), de sorte qu'aucune saisie ne fasse plus planter le programme.

## Critères de réussite

- [ ] Le menu se réaffiche après chaque action, jusqu'au choix 0.
- [ ] Une description vide ou « espaces seuls » est refusée proprement (pas de plantage).
- [ ] Taper `abc` au menu ne plante plus : le programme redemande.
- [ ] La liste affiche les réclamations avec leurs ids croissants (REC-1, REC-2…).
- [ ] Vous savez expliquer POURQUOI le `nextLine()` jetable est nécessaire après `nextInt()`.

Puis comparez avec `03-correction.md`.

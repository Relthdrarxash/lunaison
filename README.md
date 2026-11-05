# Lunaison

**Français** · [English](README.en.md)

Suivi du cycle menstruel, libre et sans compte. Pour noter ses règles, ses symptômes et son humeur, comprendre ce qui se passe à chaque phase, savoir quand arrivent les prochaines règles, et prévenir son ou sa partenaire si on le souhaite.

Lunaison est une appli Android gratuite, libre (GPL-3.0), sans publicité, sans traceur et **sans accès à Internet**. Tout reste sur le téléphone.

## Ce que fait l'appli

- **Noter ses journées** : flux, douleur (décrite par son effet), symptômes (y compris douleurs pelviennes, bouffées de chaleur, sueurs nocturnes et brouillard mental), humeurs, prise de contraception ou de traitement, signes d'ovulation facultatifs (glaire cervicale et température basale sans clavier), note personnelle. Tout est facultatif, et on peut remplir un jour oublié.
- **Prévoir les règles et mettre en pause si besoin** : une date estimée et une plage plausible, calculées à partir des derniers cycles, avec option pour afficher la fenêtre fertile estimée, mode **cycles naturellement irréguliers** (périménopause, SOPK, post-partum) et possibilité de mettre les prévisions en pause (grossesse, allaitement, contraception continue).
- **Comprendre la phase en cours et ses quatre phases** : ce qui se passe, ce qui aide, et ce qui revient chez soi pendant chacune des quatre phases d'après les cycles précédents.
- **Voir ce que disent ses cycles et générer un bilan pour un soignant** : durée habituelle, justesse des prévisions, motifs par phase, bilan de chaque cycle terminé et synthèse complète exportable pour une consultation médicale (repères FIGO/ACOG, douleurs, saignements hors règles, traitements).
- **Un conseil sourcé par jour et un exercice de respiration guidée** : le conseil change chaque jour selon la phase et répond en priorité à ce qui a été noté ; un exercice de respiration abdominale (4 s / 6 s) aide à relâcher les crampes ou la tension prémenstruelle.
- **Repérer ce qui mérite un avis médical** : douleurs fortes répétées, règles de plus de 8 jours, règles abondantes répétées, cycles hors des repères, saignements entre les règles, absence de règles, retard prolongé. Jamais un diagnostic.
- **Lire des fiches sourcées** : cycle, douleurs, alimentation, syndrome prémenstruel, quand consulter, choc toxique, limites des prévisions, fiche pour l'entourage.
- **Rappels discrets** : avant les règles, pour noter sa journée, ou chaque jour pour sa contraception ou son traitement, toujours neutres sur l'écran verrouillé (« Lunaison »).
- **Partager avec un ou une partenaire, par message ou par QR Code** (18 ans et plus) : préparation de messages dans la messagerie choisie, repères et conseils de soutien concrets pour le ou la partenaire, et partage décentralisé des estimations par **QR Code** (affiché et scanné directement dans l'appli) ou par le menu de partage d'Android (SMS, Signal, WhatsApp…), sans aucun serveur.
- **Personnalisation, code PIN et widget d'accueil** : 4 palettes de couleurs (clair et sombre), verrouillage facultatif par code à 4 chiffres (avec masquage dans les applications récentes) et widget d'accueil discret.

Lunaison n'est **ni un dispositif médical ni une méthode de contraception**.

## Pourquoi Lunaison est gratuite

Lunaison est née d'un besoin de ma compagne : suivre son cycle plus facilement, et me permettre de l'accompagner au quotidien. Les applications existantes réservent souvent leurs fonctions utiles à un abonnement, ou se financent avec les données de leurs utilisatrices, alors que ce sont parmi les plus intimes qui soient. Lunaison fait l'inverse : elle est gratuite, libre, fonctionne sans Internet et garde tout sur le téléphone. Le partage avec le ou la partenaire passe par la messagerie du téléphone, quand la personne le décide, et ne contient jamais de données de fertilité.

L'appli reste au strict nécessaire. Ce qu'elle ne dit pas elle-même, d'où viennent ses repères et pourquoi elle est faite ainsi, est expliqué dans [Sources et choix de conception](https://relthdrarxash.github.io/lunaison/references.html).

## Vie privée

- Aucune permission Internet, aucun SDK tiers, aucun compte.
- Sauvegarde dans le nuage désactivée : les données de santé ne passent par aucun serveur. Le transfert direct vers un nouveau téléphone (Android 12 et plus) reste possible, ainsi que l'export et l'**import de sauvegarde CSV** dans les réglages.
- Le sélecteur de contacts du système donne accès au seul contact choisi, sans autorisation sur le carnet d'adresses.
- Effacement complet en un geste.

## Technique

Kotlin, Jetpack Compose, Material 3, Room, WorkManager. minSdk 26, targetSdk 36.

L'appli est en français et en anglais, selon la langue du téléphone. Chaque texte est écrit dans les deux langues côte à côte (fonction `tr` de `:core:model`), pour qu'aucune traduction ne puisse manquer ; les fiches ont un fichier par langue, vérifié par les tests.

```
:app                 Écrans, navigation, rappels, partage
:core:model          Journée notée : flux, douleur, symptômes, humeurs (pur Kotlin)
:core:cycle          Moteur : repérage des règles, prévision, phases, signaux, rappels (pur Kotlin, testé)
:core:content        Fiches, conseils par phase, messages de partage, sources (pur Kotlin, testé)
:core:data           Base Room locale et dépôt unique
:core:designsystem   Thème, police Atkinson Hyperlegible, composants accessibles
```

Les règles du moteur et leurs références sont dans [`CycleConfig.kt`](core/cycle/src/main/kotlin/fr/lunaison/core/cycle/CycleConfig.kt). Les sources des contenus et les choix de conception sont dans [documentation/sources.md](documentation/sources.md), publié sur le site : [Sources et choix de conception](https://relthdrarxash.github.io/lunaison/references.html). Après une modification, régénérer la page avec `python3 scripts/site-sources.py`.

Tests : `./gradlew test`. Construction : `./gradlew :app:assembleDebug`.

## Pistes envisagées

Les fonctions étudiées mais pas encore réalisées, comme l'import de l'historique depuis Clue ou Flo, sont décrites dans [documentation/pistes.md](documentation/pistes.md).

## Contact

pilmil.studio@proton.me

## Note sur le développement

L'essentiel du code de Lunaison a été écrit avec Claude, l'assistant IA d'Anthropic. Je l'ai relu, testé et je le maintiens : le moteur du cycle et les contenus sont couverts par des tests automatiques, et je réponds moi-même aux tickets et aux messages.

# Lunaison

Suivi du cycle menstruel, libre et sans compte. Pour noter ses règles, ses symptômes et son humeur, comprendre ce qui se passe à chaque phase, savoir quand arrivent les prochaines règles, et prévenir son ou sa partenaire si on le souhaite.

Lunaison est une appli Android gratuite, libre (GPL-3.0), sans publicité, sans traceur et **sans accès à Internet**. Tout reste sur le téléphone.

## Ce que fait l'appli

- **Noter ses journées** : flux, douleur (décrite par son effet), symptômes, humeurs, note personnelle. Tout est facultatif, et on peut remplir un jour oublié.
- **Prévoir les règles** : une date estimée et une plage plausible, calculées à partir des derniers cycles. La plage se resserre à mesure que l'historique grandit.
- **Comprendre la phase en cours** : ce qui se passe, ce qui aide, et ce qui revient chez soi pendant cette phase d'après les cycles précédents.
- **Repérer ce qui mérite un avis médical** : douleurs fortes répétées, règles de plus de 8 jours, règles abondantes répétées, cycles hors des repères, saignements entre les règles, absence de règles. Jamais un diagnostic.
- **Lire des fiches sourcées** : cycle, douleurs, alimentation, syndrome prémenstruel, quand consulter, choc toxique, limites des prévisions, fiche pour l'entourage.
- **Rappels** : avant les règles et pour noter sa journée, discrets sur l'écran verrouillé.
- **Partager avec un ou une partenaire** (18 ans et plus) : l'appli prépare un message et ouvre SMS, WhatsApp, Signal ou une autre appli. C'est la personne qui appuie sur Envoyer : rien ne part automatiquement.

Lunaison n'est **ni un dispositif médical ni une méthode de contraception**.

## Vie privée

- Aucune permission Internet, aucun SDK tiers, aucun compte.
- Sauvegarde dans le nuage et transfert automatique désactivés : les données de santé ne quittent pas le téléphone. Un export au format tableur est disponible dans les réglages.
- Le sélecteur de contacts du système donne accès au seul contact choisi, sans autorisation sur le carnet d'adresses.
- Effacement complet en un geste.

## Technique

Kotlin, Jetpack Compose, Material 3, Room, WorkManager. minSdk 26, targetSdk 36.

```
:app                 Écrans, navigation, rappels, partage
:core:model          Journée notée : flux, douleur, symptômes, humeurs (pur Kotlin)
:core:cycle          Moteur : repérage des règles, prévision, phases, signaux, rappels (pur Kotlin, testé)
:core:content        Fiches, conseils par phase, messages de partage, sources (pur Kotlin, testé)
:core:data           Base Room locale et dépôt unique
:core:designsystem   Thème, police Atkinson Hyperlegible, composants accessibles
```

Les règles du moteur et leurs références sont dans [`CycleConfig.kt`](core/cycle/src/main/kotlin/fr/lunaison/core/cycle/CycleConfig.kt). Les sources des contenus sont listées dans [documentation/sources.md](documentation/sources.md).

Tests : `./gradlew test`. Construction : `./gradlew :app:assembleDebug`.

## Pistes envisagées

Les fonctions étudiées mais pas encore réalisées, comme l'import de l'historique depuis Clue ou Flo, sont décrites dans [documentation/pistes.md](documentation/pistes.md).

## Contact

escale.apps@proton.me

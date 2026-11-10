# Préparation d'une version iOS

État au 10 novembre 2026. Aucune version iOS n'est publiée ni prévue tant que la question du compte Apple Developer (nom légal affiché, 99 €/an) n'est pas réglée. Cette page décrit ce qui est prêt côté technique.

## Ce qui est fait

Le socle partagé est en Kotlin Multiplatform :

| Module | Contenu | Cibles |
|---|---|---|
| `core:model` | modèle, langue, partage QR et SMS | JVM (Android), iOS |
| `core:cycle` | moteur du cycle, alertes, rappels | JVM (Android), iOS |
| `core:content` | fiches, conseils, textes en six langues | JVM (Android), iOS |

- Les dates passent par `kotlinx-datetime`. `core/model/.../DateCompat.kt` reprend les noms de `java.time` utilisés par le moteur (`plusDays`, `toEpochDay`, `LocalDate.of`…) pour garder le code lisible.
- Le code de partage `LUNA1:...` (Base64 URL, CRC32, UTF-8 strict) est écrit en Kotlin commun dans `Codec.kt`. Le format est inchangé : un code produit sur Android reste lisible sur iOS, et inversement.
- La langue de l'appareil se lit par plateforme (`expect`/`actual` dans `Platform.kt`).
- La CI compile le socle pour le simulateur iOS sur un runner macOS (job `ios` de `ci.yml`). Les tests tournent sur la JVM (`jvmTest`).
- Android reste l'unique appli : `core:data`, `core:designsystem`, `ui` et `app` n'ont pas bougé, hormis la conversion des dates à la frontière.

## Ce qui reste à faire pour une appli iOS

1. **Données** : passer Room en version multiplateforme (2.7 ou plus) et déplacer `core:data` en KMP, ou écrire un stockage propre à iOS.
2. **Interface** : choisir entre Compose Multiplatform (partage des écrans de `ui`) et SwiftUI (écrans réécrits au-dessus du socle).
3. **Projet Xcode** (`iosApp/`) qui héberge l'interface et branche notifications, partage par la messagerie et sauvegarde iCloud (à désactiver, comme la sauvegarde Android).
4. **Tests partagés** : déplacer les tests de `jvmTest` vers `commonTest` quand ils n'utilisent plus `java.util.Locale`, pour les faire tourner aussi sur iOS.
5. **Publication** : compte Apple Developer, puis TestFlight et App Store.

## Points de vigilance

- Les caractères de format Unicode hors plan de base sont listés à la main dans `Codec.kt` (Unicode 15), car Kotlin commun ne les catégorise pas.
- `Math.round` devient `roundToInt` : même arrondi (les moitiés vont vers le haut).
- Sans Mac, la vérification iOS passe uniquement par la CI.

# Préparation d'une version iOS

État au 10 novembre 2026. Aucune version iOS n'est publiée ni prévue tant que la question du compte Apple Developer (nom légal affiché, 99 €/an) n'est pas réglée. Cette page décrit ce qui est prêt côté technique.

## Ce qui est fait

Tout le code de l'appli, hors entrée système, est en Kotlin Multiplatform et compile pour Android et pour iOS (simulateur et appareil) :

| Module | Contenu | Cibles |
|---|---|---|
| `core:model` | modèle, langue, partage QR et SMS | JVM (Android), iOS |
| `core:cycle` | moteur du cycle, alertes, rappels | JVM (Android), iOS |
| `core:content` | fiches, conseils, textes en six langues | JVM (Android), iOS |
| `core:data` | base Room multiplateforme, dépôt, export CSV | Android, iOS |
| `core:designsystem` | thème, composants, police Atkinson (Compose Multiplatform) | Android, iOS |
| `ui` | tous les écrans et la navigation (Compose Multiplatform) | Android, iOS |

- Les dates passent par `kotlinx-datetime`. `core/model/.../DateCompat.kt` reprend les noms de `java.time` utilisés par le moteur (`plusDays`, `toEpochDay`, `LocalDate.of`…) pour garder le code lisible. Les noms de mois et de jours sont écrits en dur dans `Format.kt` (six langues), donc identiques sur les deux plateformes.
- Le code de partage `LUNA1:...` (Base64 URL, CRC32, UTF-8 strict) est écrit en Kotlin commun dans `Codec.kt`. Le format est inchangé : un code produit sur Android reste lisible sur iOS, et inversement.
- Room 2.7 : même fichier `lunaison.db` et même schéma sur Android (SQLite du système). Sur iOS, SQLite embarqué (`BundledSQLiteDriver`), base dans `Application Support/Lunaison`.
- **Sauvegarde iCloud** : incluse par défaut sur iOS, car elle reste sous le contrôle de la personne et de son compte Apple. Un réglage (Réglages, section données) permet de l'exclure : il pose l'attribut « exclu de la sauvegarde » sur le dossier de la base (`DatabaseIos.kt`). La mention de confidentialité du même écran est adaptée sur iOS.
- Tout ce qui dépend du système passe par `ui/src/commonMain/.../Platform.kt` (`expect`), avec une implémentation dans `androidMain` et une dans `iosMain` : ouverture de liens, notifications, export CSV, partage, contacts, NFC, import, PDF, QR Code.
- La CI compile tout pour le simulateur iOS sur un runner macOS (job `ios` de `ci.yml`). Les captures d'écran (Paparazzi) et les tests unitaires restent côté Android.

## Ce qui manque sur iOS (aujourd'hui désactivé ou vide)

- **QR Code** : pas encore d'encodeur ni de lecteur (`encodeQr` renvoie `null`, `rememberScanner` n'a pas de caméra). Le code se partage par message. À faire : encodeur en Kotlin commun (testé contre ZXing sur Android) ou CoreImage, lecteur AVFoundation.
- **Rappels** : l'autorisation de notifications est demandée, mais rien n'est encore planifié (Android passe par WorkManager). À faire : notifications locales `UNCalendarNotificationTrigger`.
- **PDF du bilan** : dessiné avec les API graphiques d'Android, bouton masqué sur iOS. À faire : rendu PDF avec UIKit.
- **Import d'autres applis** (Period Calendar) et **sélecteur de contacts** : masqués ou absents.
- **Verrou par code** : écran commun, mais pas de déverrouillage biométrique.
- **Fiche App Store** : le bouton « Laisser un avis » est masqué tant que l'appli n'a pas d'identifiant App Store.

## Ce qui reste à faire pour une appli iOS

1. **Projet Xcode** (`iosApp/`) qui héberge `ui` (point d'entrée `ComposeUIViewController`), l'icône et les autorisations, avec un job de CI qui construit l'appli sur macOS.
2. **Fonctions manquantes** de la liste ci-dessus, dans l'ordre : rappels, QR Code, PDF.
3. **Accessibilité** : essai VoiceOver et texte dynamique sur un iPhone ou le simulateur dès le premier écran.
4. **Tests partagés** : déplacer les tests de `ui/src/androidUnitTest` et de `jvmTest` vers `commonTest` quand ils n'utilisent plus `java.util.Locale`, pour les faire tourner aussi sur iOS.
5. **Publication** : compte Apple Developer, puis TestFlight et App Store.

## Points de vigilance

- Les caractères de format Unicode hors plan de base sont listés à la main dans `Codec.kt` (Unicode 15), car Kotlin commun ne les catégorise pas.
- `Math.round` devient `roundToInt` : même arrondi (les moitiés vont vers le haut).
- Sans Mac, la vérification iOS passe uniquement par la CI : elle prouve que le code compile, pas que l'interface se comporte bien sur un iPhone.
- La police Atkinson est copiée deux fois (`androidMain/res/font` pour Android et Paparazzi, `commonMain/composeResources/font` pour iOS) ; garder les deux jeux identiques.

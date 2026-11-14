# Feuille de route — Lunaison

> Dernière mise à jour : novembre 2026. Détails iOS : [documentation/ios.md](documentation/ios.md).

## 1. Fonctionnalités à venir
- **v1.x** :
  - Prise en charge de formats d'import complémentaires (Clue, Flo, Drip) lorsque des exports exploitables existent.
  - Suppression définitive du partage NFC HCE au profit de l'échange pair-à-pair / QR / message.
  - Intégration de `pilmil-core` (`BackupVault` `.pilmil` AES-256-GCM) pour mutualiser l'export chiffré.

## 2. Aspect visuel & UX
- Audit visuel des écrans secondaires en mode nuit et avec grandes polices système (TalkBack / contraste AA-AAA).
- Maintien des captures d'écran Paparazzi à jour pour les fiches stores (6 langues).

## 3. Qualité de code & sécurité
- **Prioritaire** : chiffrement complet de la base locale Room au repos (SQLCipher) en complément du verrou PIN (`PinThrottle`) et de la sauvegarde `.pilmil`.
- Sauvegarde système Android configurable par application (`-Ppilmil.allowAutoBackup=false` par défaut, règles `data_extraction_rules.xml` excluant le cloud).
- Renforcement des tests unitaires sur les parcours critiques (verrou PIN, temporisation `PinThrottle`, import/export, calculs de cycle) et suivi `lintDebug` en CI.
- À faire : ajouter `ktlint` / `detekt` et déplacer les tests sans `java.util.Locale` vers `commonTest`.

## 4. Feuille de route iOS
- **État actuel** : tous les modules KMP (`core:model`, `core:cycle`, `core:content`, `core:data` avec Room 2.7, `core:designsystem`, `ui`) compilent pour iOS en CI.
- **Ce qu'il reste** :
  1. Créer le projet Xcode `iosApp/` (`ComposeUIViewController`, icône, autorisations, build CI macOS).
  2. Implémenter les ponts natifs manquants dans `iosMain` : **QR Code** (CoreImage / AVFoundation), **rappels locaux** (`UNCalendarNotificationTrigger`), **export PDF** (UIKit) et déverrouillage biométrique.
  3. Vérifier VoiceOver, texte dynamique et réglage d'exclusion iCloud sur appareil réel, puis publier via TestFlight / App Store.

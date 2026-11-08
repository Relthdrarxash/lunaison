# Kit de distribution éthique (F-Droid, IzzyOnDroid, annuaires libres et soignants)

Ce document regroupe les fiches techniques prêtes à copier-coller pour référencer **Lunaison**, **Sommelle** et **Respire** (Pilmil Studio) dans les dépôts d'applications libres et auprès des réseaux de santé.

---

## 1. Métadonnées F-Droid (`fdroiddata`)

Les trois dépôts incluent déjà l'arborescence `fastlane/metadata/android/` (descriptions, titres, captures et notes de version), que F-Droid et IzzyOnDroid lisent automatiquement.

### `metadata/fr.lunaison.yml`
```yaml
Categories:
  - Science & Education
  - Sports & Health
License: MPL-2.0
AuthorName: Pilmil Studio
AuthorEmail: pilmil.studio@proton.me
WebSite: https://relthdrarxash.github.io/lunaison/
SourceCode: https://github.com/Relthdrarxash/lunaison
IssueTracker: https://github.com/Relthdrarxash/lunaison/issues
Changelog: https://github.com/Relthdrarxash/lunaison/releases

AutoName: Lunaison

RepoType: git
Repo: https://github.com/Relthdrarxash/lunaison.git

Builds:
  - versionName: 1.2.0
    versionCode: 10200
    commit: v1.2.0
    subdir: app
    gradle:
      - yes
    prebuild: sed -i -e 's/versionCode = .*/versionCode = 10200/' -e 's/versionName = .*/versionName = "1.2.0"/' build.gradle.kts

AutoUpdateMode: Version
UpdateCheckMode: Tags
CurrentVersion: 1.2.0
CurrentVersionCode: 10200
```

### `metadata/fr.sommelle.yml`
```yaml
Categories:
  - Science & Education
  - Sports & Health
License: MPL-2.0
AuthorName: Pilmil Studio
AuthorEmail: pilmil.studio@proton.me
WebSite: https://relthdrarxash.github.io/sommelle/
SourceCode: https://github.com/Relthdrarxash/sommelle
IssueTracker: https://github.com/Relthdrarxash/sommelle/issues
Changelog: https://github.com/Relthdrarxash/sommelle/releases

AutoName: Sommelle

RepoType: git
Repo: https://github.com/Relthdrarxash/sommelle.git

Builds:
  - versionName: 1.2.0
    versionCode: 10200
    commit: v1.2.0
    subdir: app
    gradle:
      - yes
    prebuild: sed -i -e 's/versionCode = .*/versionCode = 10200/' -e 's/versionName = .*/versionName = "1.2.0"/' build.gradle.kts

AutoUpdateMode: Version
UpdateCheckMode: Tags
CurrentVersion: 1.2.0
CurrentVersionCode: 10200
```

### `metadata/fr.respire.app.yml`
```yaml
Categories:
  - Time
  - System
License: MPL-2.0
AuthorName: Pilmil Studio
AuthorEmail: pilmil.studio@proton.me
WebSite: https://relthdrarxash.github.io/respire/
SourceCode: https://github.com/Relthdrarxash/respire
IssueTracker: https://github.com/Relthdrarxash/respire/issues
Changelog: https://github.com/Relthdrarxash/respire/releases

AutoName: Respire

RepoType: git
Repo: https://github.com/Relthdrarxash/respire.git

Builds:
  - versionName: 4.3.0
    versionCode: 40300
    commit: v4.3.0
    subdir: app
    gradle:
      - yes
    prebuild: sed -i -e 's/versionCode = .*/versionCode = 40300/' -e 's/versionName = .*/versionName = "4.3.0"/' build.gradle.kts

AutoUpdateMode: Version
UpdateCheckMode: Tags
CurrentVersion: 4.3.0
CurrentVersionCode: 40300
```

---

## 2. Référencement rapide sur IzzyOnDroid et Obtainium

- **IzzyOnDroid** (dépôt F-Droid indexant directement les APK signés des GitHub Releases en 24–48 h) :
  - Ouvrir un ticket sur `https://gitlab.com/IzzyOnDroid/repo/-/issues` avec les 3 URL :
    - `https://github.com/Relthdrarxash/lunaison`
    - `https://github.com/Relthdrarxash/sommelle`
    - `https://github.com/Relthdrarxash/respire`
  - Points forts vérifiés automatiquement par le scanner IzzyOnDroid : **0 bibliothèque propriétaire, 0 pisteur, 0 permission `INTERNET`, structure Fastlane complète**.
- **Obtainium** : les utilisateurs peuvent ajouter directement l'URL GitHub de chaque dépôt pour recevoir les mises à jour automatiques depuis les GitHub Releases.

---

## 3. Fiche de présentation pour les professionnels de santé

- **Lunaison** : page dédiée et imprimable sur `https://relthdrarxash.github.io/lunaison/professionnels.html` (et `/en/professionals.html`).
- **Sommelle** : page dédiée et imprimable sur `https://relthdrarxash.github.io/sommelle/professionnels.html` (et `/en/professionals.html`).

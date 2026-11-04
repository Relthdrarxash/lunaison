pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "lunaison"

include(":app")

// Socle pur Kotlin (sans dépendance Android), testable et relisible indépendamment de l'interface.
include(":core:model")
include(":core:cycle")
include(":core:content")

// Socle Android.
include(":core:data")
include(":core:designsystem")

// Écrans.
include(":ui")

// Captures d'écran de la documentation et de la fiche Play, générées par la CI (non embarqué).
include(":screenshots")

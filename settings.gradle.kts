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

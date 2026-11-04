import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// Module de documentation : il n'est pas embarqué dans l'appli. Paparazzi y affiche les écrans
// sans téléphone ni émulateur, et la CI publie les images.
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.paparazzi)
}

android {
    namespace = "fr.lunaison.screenshots"
    compileSdk = 35
    defaultConfig { minSdk = 26 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures { compose = true }
}

kotlin {
    compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
}

dependencies {
    implementation(project(":ui"))
    implementation(libs.androidx.activity.compose)
    testImplementation(libs.junit)
}

// Trace complète en cas d'échec, lisible directement dans le journal de la CI.
tasks.withType<Test>().configureEach {
    testLogging {
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        events("failed")
    }
}

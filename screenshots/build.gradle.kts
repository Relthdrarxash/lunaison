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
    testImplementation(libs.junit)
}

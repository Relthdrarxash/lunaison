import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// Thème et composants communs à Android et iOS (Compose Multiplatform).
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose.multiplatform)
}

android {
    namespace = "fr.lunaison.core.designsystem"
    compileSdk = 35
    defaultConfig { minSdk = 26 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    androidTarget { compilerOptions { jvmTarget.set(JvmTarget.JVM_17) } }
    iosArm64()
    iosSimulatorArm64()
    iosX64()

    sourceSets {
        commonMain.dependencies {
            api(compose.runtime)
            api(compose.foundation)
            api(compose.ui)
            api(compose.material3)
            api(libs.compose.icons.core)
            implementation(compose.components.resources)
        }
    }
}

// Les polices sont aussi des ressources Android (R.font) : les captures d'écran (Paparazzi) n'ont pas
// accès aux ressources Compose. iOS lit la copie de composeResources.
compose.resources {
    packageOfResClass = "fr.lunaison.core.designsystem.resources"
    publicResClass = false
}

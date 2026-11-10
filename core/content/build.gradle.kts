import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

kotlin {
    jvm {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    // Cibles iOS : compilées seulement sur macOS (ignorées sans avertissement bloquant sous Linux).
    iosArm64()
    iosSimulatorArm64()
    iosX64()

    sourceSets {
        commonMain.dependencies {
            api(project(":core:cycle"))
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}

tasks.named<Test>("jvmTest") {
    useJUnitPlatform()
    // Les tests vérifient les textes français : on fixe la langue par défaut de la JVM.
    systemProperty("user.language", "fr")
    systemProperty("user.country", "FR")
}

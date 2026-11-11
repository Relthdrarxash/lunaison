import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
}

android {
    namespace = "fr.lunaison.core.data"
    compileSdk = 35
    defaultConfig { minSdk = 26 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    androidTarget {
        compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
    }

    // Cibles iOS : compilées seulement sur macOS.
    iosArm64()
    iosSimulatorArm64()
    iosX64()

    sourceSets {
        commonMain.dependencies {
            api(project(":core:content"))
            api(libs.kotlinx.coroutines.core)
            api(libs.androidx.room.runtime)
        }
        androidMain.dependencies {
            api(libs.kotlinx.coroutines.android)
        }
        iosMain.dependencies {
            // Moteur SQLite embarqué : iOS n'a pas d'équivalent de SupportSQLite.
            implementation(libs.androidx.sqlite.bundled)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
        getByName("androidUnitTest").dependencies {
            // Vrai moteur SQLite sur la JVM, pour rejouer les migrations sans émulateur.
            implementation("org.xerial:sqlite-jdbc:3.46.1.3")
        }
    }
}

// Le plugin Room gère l'export du schéma pour chaque cible.
room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    add("kspAndroid", libs.androidx.room.compiler)
    add("kspIosArm64", libs.androidx.room.compiler)
    add("kspIosSimulatorArm64", libs.androidx.room.compiler)
    add("kspIosX64", libs.androidx.room.compiler)
}

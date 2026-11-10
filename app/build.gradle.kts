import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "fr.lunaison"
    compileSdk = 36

    defaultConfig {
        applicationId = "fr.lunaison"
        minSdk = 26
        targetSdk = 36
        // En publication, la CI fournit le numéro et le nom de version.
        versionCode = System.getenv("LUNAISON_VERSION_CODE")?.toIntOrNull() ?: 1
        versionName = System.getenv("LUNAISON_VERSION_NAME")?.removePrefix("v") ?: "0.1.0"
    }

    // La clé de signature n'est jamais dans le dépôt : elle vient des secrets de la CI.
    val keystorePath = System.getenv("LUNAISON_KEYSTORE_PATH")
    signingConfigs {
        if (keystorePath != null) {
            create("release") {
                storeFile = file(keystorePath)
                storePassword = System.getenv("LUNAISON_KEYSTORE_PASSWORD")
                keyAlias = System.getenv("LUNAISON_KEY_ALIAS")
                keyPassword = System.getenv("LUNAISON_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            if (keystorePath != null) signingConfig = signingConfigs.getByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

kotlin {
    compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
}

dependencies {
    implementation(project(":ui"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.work.runtime)
}

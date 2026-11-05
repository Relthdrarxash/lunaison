import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.jvm)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    api(project(":core:cycle"))
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
    // Les tests vérifient les textes français : on fixe la langue par défaut de la JVM.
    systemProperty("user.language", "fr")
    systemProperty("user.country", "FR")
}

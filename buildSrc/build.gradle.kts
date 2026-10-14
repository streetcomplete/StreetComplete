repositories {
    google()
    mavenCentral()
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

dependencies {
    implementation(libs.klaxon)
    implementation(libs.countryboundaries)
    implementation(libs.buildsrc.kotlinxIo)
    implementation(libs.yamlbeans)
    implementation(libs.jsoup)
    implementation(libs.buildsrc.kotlinxSerializationJson)
    implementation(libs.buildsrc.kaml)
    implementation(libs.markdown)
}

plugins {
    `kotlin-dsl`
    alias(libs.plugins.buildsrc.kotlinSerialization)
}

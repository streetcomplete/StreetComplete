repositories {
    google()
    mavenCentral()
}

dependencies {
    implementation("com.beust:klaxon:5.6")
    implementation("de.westnordost:countryboundaries:3.0.0")
    implementation("org.jetbrains.kotlinx:kotlinx-io-core:0.7.0")
    implementation("com.esotericsoftware.yamlbeans:yamlbeans:1.17")
    implementation("org.jsoup:jsoup:1.18.3")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1")
    implementation("com.charleskorn.kaml:kaml:0.77.1")
    implementation("org.jetbrains:markdown:0.7.3")
}

plugins {
    `kotlin-dsl`
    kotlin("plugin.serialization") version "2.0.0"
}

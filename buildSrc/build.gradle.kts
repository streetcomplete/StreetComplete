repositories {
    google()
    mavenCentral()
}

dependencies {
    implementation("com.beust:klaxon:5.5")
    implementation("de.westnordost:countryboundaries:2.1")
    implementation("com.esotericsoftware.yamlbeans:yamlbeans:1.15")
    implementation("org.jsoup:jsoup:1.15.3")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")
    implementation("com.charleskorn.kaml:kaml:0.55.0")
    implementation("org.jetbrains:markdown:0.5.0")
}

plugins {
    `kotlin-dsl`
    kotlin("plugin.serialization") version "1.5.0"
}

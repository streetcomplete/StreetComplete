repositories {
    google()
    mavenCentral()
}

dependencies {
    implementation("com.beust:klaxon:5.5")
    implementation("de.westnordost:countryboundaries:1.5")
    implementation("com.esotericsoftware.yamlbeans:yamlbeans:1.15")
    implementation("org.jsoup:jsoup:1.14.3")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")
    implementation("com.charleskorn.kaml:kaml:0.42.0")
}

plugins {
    `kotlin-dsl`
    kotlin("plugin.serialization") version "1.5.0"
}

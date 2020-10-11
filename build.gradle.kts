buildscript {
    repositories {
        google()
        jcenter()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:4.0.2")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.4.10")
    }
}

allprojects {
    repositories {
        google()
        jcenter()
    }
}


tasks.register<UpdateStoreDescriptionsTask>("updateStoreDescriptions") {
    group = "streetcomplete"
    targetDir = "$projectDir/metadata"
    apiToken = properties["POEditorAPIToken"] as String
}


tasks.register("updateStreetCompleteData") {
    group = "streetcomplete"
    dependsOn(
        "app:updatePresets",
        "app:updateTranslations",
        "app:updateTranslationCompleteness",
        "app:generateMetadataByCountry",
        "updateStoreDescriptions")
}
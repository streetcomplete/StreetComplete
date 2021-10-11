buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        val kotlinVersion = "1.5.30"
        classpath("com.android.tools.build:gradle:4.2.2")
        classpath(kotlin("gradle-plugin", version = kotlinVersion))
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}


tasks.register<UpdateStoreDescriptionsTask>("updateStoreDescriptions") {
    group = "streetcomplete"
    targetDir = "$projectDir/metadata"
    apiToken = properties["POEditorAPIToken"] as String
}

tasks.register<SophoxCountValueByCountryTask>("updateAtmOperators") {
    group = "streetcomplete"
    targetFile = "$projectDir/res/country_metadata/atmOperators.yml"
    osmTag = "operator"
    sparqlQueryPart = "osmt:amenity 'atm';"
    minCount = 2
    minPercent = 0.1
}

tasks.register<SophoxCountValueByCountryTask>("updateClothesContainerOperators") {
    group = "streetcomplete"
    targetFile = "$projectDir/res/country_metadata/clothesContainerOperators.yml"
    osmTag = "operator"
    sparqlQueryPart = "osmt:amenity 'recycling'; osmt:recycling_type 'container'; osmt:recycling:clothes 'yes';"
    minCount = 2
    minPercent = 0.1
}

tasks.register<SophoxCountValueByCountryTask>("updateChargingStationOperators") {
    group = "streetcomplete"
    targetFile = "$projectDir/res/country_metadata/chargingStationOperators.yml"
    osmTag = "operator"
    sparqlQueryPart = "osmt:amenity 'charging_station';"
    minCount = 2
    minPercent = 0.1
}

tasks.register("updateStreetCompleteData") {
    group = "streetcomplete"
    dependsOn(
        "updateStoreDescriptions",
        //"updateChargingStationOperators",
        //"updateClothesContainerOperators",
        //"updateAtmOperators",
        "app:updatePresets",
        "app:updateNsiPresets",
        "app:updateTranslatorCredits",
        "app:updateTranslations",
        "app:updateTranslationCompleteness",
        "app:copyDefaultStringsToEnStrings",
        "app:generateMetadataByCountry",
        "app:updateTranslatorCredits"
        )
}

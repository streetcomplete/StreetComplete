buildscript {
    repositories {
        google()
        jcenter()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:4.1.0")
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
        "updateChargingStationOperators",
        "updateClothesContainerOperators",
        "updateAtmOperators",
        "app:updatePresets",
        "app:updateTranslations",
        "app:updateTranslationCompleteness",
        "app:generateMetadataByCountry"
        )
}

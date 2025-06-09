plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    id("org.jetbrains.kotlin.multiplatform") version "2.1.21" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.21"  apply false
    id("com.android.application") version "8.9.3" apply false
    id("com.android.library") version "8.9.3" apply false
    id("org.jetbrains.compose") version "1.8.1" apply false
    id("org.jetbrains.kotlinx.atomicfu") version "0.27.0" apply false
}

val poEditorProjectId = "97843"

tasks.register<UpdateWebsiteTranslationsTask>("updateWebsiteTranslations") {
    group = "streetcomplete"
    targetDir = "$projectDir/../streetcomplete-website/res"
    projectId = poEditorProjectId
    apiToken = properties["POEditorAPIToken"] as String
}

tasks.register<UpdateStoreDescriptionsTask>("updateStoreDescriptions") {
    group = "streetcomplete"
    targetDir = "$projectDir/metadata"
    projectId = poEditorProjectId
    apiToken = properties["POEditorAPIToken"] as String
}

tasks.register<QLeverCountValueByCountryTask>("updateAtmOperators") {
    group = "streetcomplete"
    targetFile = "$projectDir/res/country_metadata/atmOperators.yml"
    osmTag = "operator"
    sparqlQueryPart = "osmkey:amenity 'atm';"
    minCount = 2
    minPercent = 0.1
}

tasks.register<QLeverCountValueByCountryTask>("updateParcelLockerBrand") {
    group = "streetcomplete"
    targetFile = "$projectDir/res/country_metadata/parcelLockerBrand.yml"
    osmTag = "brand"
    sparqlQueryPart = "osmkey:amenity 'parcel_locker';"
    minCount = 2
    minPercent = 0.1
}

tasks.register<QLeverCountValueByCountryTask>("updateClothesContainerOperators") {
    group = "streetcomplete"
    targetFile = "$projectDir/res/country_metadata/clothesContainerOperators.yml"
    osmTag = "operator"
    sparqlQueryPart = "osmkey:amenity 'recycling'; osmkey:recycling_type 'container'; osmkey:recycling:clothes 'yes';"
    minCount = 2
    minPercent = 0.1
}

tasks.register<QLeverCountValueByCountryTask>("updateChargingStationOperators") {
    group = "streetcomplete"
    targetFile = "$projectDir/res/country_metadata/chargingStationOperators.yml"
    osmTag = "operator"
    sparqlQueryPart = "osmkey:amenity 'charging_station';"
    minCount = 2
    minPercent = 0.1
}

tasks.register<GenerateQuestListTask>("generateQuestList") {
    group = "streetcomplete"
    targetFile = "$projectDir/quest-list.csv"
    projectDirectory = projectDir
    questsDirectory = projectDir.resolve("app/src/androidMain/kotlin/de/westnordost/streetcomplete/quests/")
    iconsDirectory = projectDir.resolve("res/graphics/quest/")
    noteQuestFile = projectDir.resolve("app/src/androidMain/kotlin/de/westnordost/streetcomplete/quests/note_discussion/OsmNoteQuestType.kt")
    questsModuleFile = projectDir.resolve("app/src/androidMain/kotlin/de/westnordost/streetcomplete/quests/QuestsModule.kt")
    stringsFile = projectDir.resolve("app/src/androidMain/res/values/strings.xml")
}

tasks.register("updateStreetCompleteData") {
    group = "streetcomplete"
    dependsOn(
        "updateStoreDescriptions",
        "updateChargingStationOperators",
        "updateClothesContainerOperators",
        "updateAtmOperators",
        "updateParcelLockerBrand",
        "generateQuestList",
        "app:updateContributorStatistics",
        "app:updatePresets",
        "app:updateNsiPresets",
        "app:updateTranslations",
        "app:updateTranslationCompleteness",
        "app:copyDefaultStringsToEnStrings",
        "app:updateMapStyle",
        "app:updateChangelog",
        "app:generateMetadataByCountry",
        "app:updateTranslatorCredits",
        "app:updateAvailableLanguages",
        "app:downloadAndConvertPresetIcons"
    )
}

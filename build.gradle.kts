plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.android.multiplatformLibrary) apply false
    alias(libs.plugins.compose.multiplatform) apply false
    alias(libs.plugins.nucleus) apply false
}

val poEditorProjectId = "97843"

tasks.register<UpdateWebsiteTranslationsTask>("updateWebsiteTranslations") {
    group = "streetcomplete"
    targetDir = projectDir.resolve("../streetcomplete-website/res")
    projectId = poEditorProjectId
    apiToken = properties["app.streetcomplete.POEditorAPIToken"] as String
}

tasks.register<UpdateStoreDescriptionsTask>("updateStoreDescriptions") {
    group = "streetcomplete"
    targetDir = projectDir.resolve("metadata")
    projectId = poEditorProjectId
    apiToken = properties["app.streetcomplete.POEditorAPIToken"] as String
}

tasks.register<QLeverCountValueByCountryTask>("updateAtmOperators") {
    group = "streetcomplete"
    targetFile = projectDir.resolve("res/country_metadata/atmOperators.yml")
    osmTag = "operator"
    sparqlQueryPart = "osmkey:amenity 'atm';"
    minCount = 2
    minPercent = 0.1
}

tasks.register<QLeverCountValueByCountryTask>("updateParcelLockerBrand") {
    group = "streetcomplete"
    targetFile = projectDir.resolve("res/country_metadata/parcelLockerBrand.yml")
    osmTag = "brand"
    sparqlQueryPart = "osmkey:amenity 'parcel_locker';"
    minCount = 2
    minPercent = 0.1
}

tasks.register<QLeverCountValueByCountryTask>("updateClothesContainerOperators") {
    group = "streetcomplete"
    targetFile = projectDir.resolve("res/country_metadata/clothesContainerOperators.yml")
    osmTag = "operator"
    sparqlQueryPart = "osmkey:amenity 'recycling'; osmkey:recycling_type 'container'; osmkey:recycling:clothes 'yes';"
    minCount = 2
    minPercent = 0.1
}

tasks.register<QLeverCountValueByCountryTask>("updateChargingStationOperators") {
    group = "streetcomplete"
    targetFile = projectDir.resolve("res/country_metadata/chargingStationOperators.yml")
    osmTag = "operator"
    sparqlQueryPart = "osmkey:amenity 'charging_station';"
    minCount = 2
    minPercent = 0.1
}

tasks.register<GenerateQuestListTask>("generateQuestList") {
    group = "streetcomplete"
    targetFile = projectDir.resolve("quest-list.csv")
    projectDirectory = projectDir
    questsDirectory = projectDir.resolve("app/src/commonMain/kotlin/de/westnordost/streetcomplete/quests/")
    iconsDirectory = projectDir.resolve("res/graphics/quest/")
    noteQuestFile = projectDir.resolve("app/src/commonMain/kotlin/de/westnordost/streetcomplete/quests/note_comments/OsmNoteQuestType.kt")
    questTypesRegistryFile = projectDir.resolve("app/src/commonMain/kotlin/de/westnordost/streetcomplete/quests/QuestTypesRegistry.kt")
    stringsFile = projectDir.resolve("app/src/commonMain/composeResources/values/strings.xml")
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
        "app:updateIosTranslations",
        "app:updateMacosTranslations",
        "app:updateChangelog",
        "app:generateMetadataByCountry",
        "app:updateTranslatorCredits",
        "app:updateAvailableLanguages",
        "app:downloadAndConvertPresetIcons",
        "app:copyDefaultStringsToEnStrings"
    )
}

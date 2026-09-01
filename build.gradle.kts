plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    id("org.jetbrains.kotlin.multiplatform") version "2.4.0" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.4.0"  apply false
    id("com.android.application") version "9.1.1" apply false
    id("com.android.library") version "9.1.1" apply false
    id("com.android.kotlin.multiplatform.library") version "9.1.1" apply false
    id("org.jetbrains.compose") version "1.12.0" apply false
}

data class PinnedMapLibreFile(
    val module: String,
    val variant: String,
    val logicalName: String,
    val immutablePath: String,
)

val mapLibreComposeVersion = providers.gradleProperty("mapLibreComposeVersion").get()
val pinnedMapLibreFiles = file("gradle/maplibre-compose-snapshot-files.tsv")
    .readLines()
    .filterNot { it.isBlank() || it.startsWith("#") }
    .map { line ->
        val columns = line.split('\t')
        check(columns.size == 4) { "Invalid MapLibre snapshot file entry: $line" }
        PinnedMapLibreFile(columns[0], columns[1], columns[2], columns[3])
    }

subprojects {
    dependencies.components {
        pinnedMapLibreFiles.groupBy(PinnedMapLibreFile::module).forEach { (module, files) ->
            withModule("org.maplibre.compose:$module") {
                if (id.version != mapLibreComposeVersion) return@withModule
                files.groupBy(PinnedMapLibreFile::variant).forEach { (variant, variantFiles) ->
                    withVariant(variant) {
                        withFiles {
                            removeAllFiles()
                            variantFiles.forEach { file ->
                                addFile(file.logicalName, file.immutablePath)
                            }
                        }
                    }
                }
            }
        }
    }
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
        "app:updateChangelog",
        "app:generateMetadataByCountry",
        "app:updateTranslatorCredits",
        "app:updateAvailableLanguages",
        "app:downloadAndConvertPresetIcons",
        "app:copyDefaultStringsToEnStrings"
    )
}

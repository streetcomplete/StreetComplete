buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        val kotlinVersion = "2.0.0"
        classpath("com.android.tools.build:gradle:8.4.1")
        classpath(kotlin("gradle-plugin", version = kotlinVersion))
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://www.jitpack.io") }
    }
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
    sourceDirectory = projectDir.resolve("app/src/main/java/de/westnordost/streetcomplete/")
    iconsDirectory = projectDir.resolve("res/graphics/quest/")
    noteQuestFile = sourceDirectory.resolve("data/osmnotes/notequests/OsmNoteQuestType.kt")
}

tasks.register<UpdateContributorStatisticsTask>("updateContributorStatistics") {
    group = "streetcomplete"
    skipCommits = setOf(
        "ae7a244dd60ccfc91cf2dc01bf9e60c8d6a81616", // some weird force-merge or something
        "f3bc67328c3be989835e44eb33e769f49da479e1", // just a large re-import of orchard-produce images
        "9c6d3e25216d06a2c5afa71086949e1e195de926", // mechanical linting
        "1908fc930397c17739e60c8da67f968361f52e89", // mechanical linting
        "74b6424d3310f62a5c0f7b0071ee81c2308db4f6", // mechanically optimized all graphics in the repo back then
        "4282c1e812764a2bb46c17bbdb0fd98aee598e83", // deletion of adding too many files prior
        "a64d57efc3d8d51c564365088772fdac528ab069", // deletion of adding too many files prior
        "7fb216b8360ee85d84b36ad3fb0b0ea0ebf9977d", // mechanical linting
        "21aa1deabae7a563ba1475094f372590fb33d784", // mechanical linting
        "fef6877852d6a19a7b85e6f3ed3b09ea7c6538ec", // mostly just moving a lot of packages around
        "7a7d725154eb38d53936d154fc8011355679a8ae", // just moving packages around
    )
    val skipWords = listOf("lint", "linter", "reorder imports", "organize imports")
    skipCommitRegex = Regex(".*\\b(${skipWords.joinToString("|")})\\b.*", RegexOption.IGNORE_CASE)
    targetFile = "$projectDir/app/src/main/res/raw/credits_contributors.yml"
    // gradle, py, bat, java and mjs don't exist anymore in this repo but they used to
    codeFileRegex = Regex(".*\\.(java|kt|kts|py|gradle|bat|mjs)$")
    /* photos, illustrations, sounds ... but not yml, json, ... because most of these are updated
       via gradle tasks */
    assetFileRegex = Regex(".*\\.(jpe?g|png|svg|webp|wav)$", RegexOption.IGNORE_CASE)
    /* drawable xmls, layout xmls, animation xmls ... but not strings because they are updated
       via gradle tasks */
    interfaceMarkupRegex = Regex(".*(anim|color|drawable|layout|menu|mipmap).*\\.xml$")
    githubApiToken = properties["GithubApiToken"] as String
}

tasks.register("updateStreetCompleteData") {
    group = "streetcomplete"
    dependsOn(
        "updateStoreDescriptions",
        "updateContributorStatistics",
        "updateChargingStationOperators",
        "updateClothesContainerOperators",
        "updateAtmOperators",
        "updateParcelLockerBrand",
        "generateQuestList",
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

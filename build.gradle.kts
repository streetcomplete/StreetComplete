buildscript {
    repositories {
        google()
        jcenter()
    }
    dependencies {
        val kotlinVersion = "1.4.10"
        classpath("com.android.tools.build:gradle:4.1.3")
        classpath(kotlin("gradle-plugin", version = kotlinVersion))
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
        //"updateChargingStationOperators",
        //"updateClothesContainerOperators",
        //"updateAtmOperators",
        "app:updatePresets",
        "app:updateNsiPresets",
        "app:updateTranslations",
        "app:updateTranslationCompleteness",
        "app:copyDefaultStringsToEnStrings",
        "app:generateMetadataByCountry"
        )
}

tasks.register("gitstats") {
    val countsByName = mutableMapOf<String, Int>()
    val countsByCommit = mutableMapOf<String, Int>()
    Runtime.getRuntime().exec("git log --no-merges --pretty='%an'%n%H --numstat").inputStream.bufferedReader().useLines { lines ->
        var name = ""
        var commit = ""
        var commitNext = false
        var skipNext = false
        for(line in lines) {
            if (line.startsWith('\'')) {
                name = line.trim('\'')
                skipNext = false
                commitNext = true
            } else if (commitNext) {
                commit = line
                if (line.startsWith("ae7a244dd60ccfc91cf2dc01bf9e60c8d6a81616")) {
                  // println("Found commit " + line)
                  skipNext = true
                }
                commitNext = false
            } else {
                val splits = line.split(Regex("\\s+"))
                val additions = splits[0].toIntOrNull() ?: continue
                val deletions = splits[1].toIntOrNull() ?: continue
                if(!splits.last().matches(Regex(".*\\.(java|kt|kts)$"))) continue
                val commitTotal = additions + deletions
                if (!skipNext) {
                    countsByName[name] = commitTotal + countsByName.getOrPut(name, { 0 })
                }
                if (commitTotal > 400) {
                    countsByCommit[commit] = commitTotal
                }
            }
        }
    }
    countsByCommit.entries.sortedByDescending { it.value }.forEach { println("${it.value}\t${it.key}") }
    println("*************************************************************")
    countsByName.entries.sortedByDescending { it.value }.forEach { println("${it.value}\t${it.key}") }
}

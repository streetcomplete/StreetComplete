package de.westnordost.streetcomplete.osm.building

import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.building.BuildingLifecycle.*
import de.westnordost.streetcomplete.osm.building.BuildingType.*
import de.westnordost.streetcomplete.osm.updateCheckDate

fun BuildingTypeAndStatus.applyTo(tags: Tags) {
    // special case for status=CONSTRUCTION, it is tagged like
    // building=construction + construction=actual_building_value
    if (status == CONSTRUCTION) {
        clearBuildingStatus(tags)
        tags["building"] = "construction"
        tags["construction"] = type?.osmValue ?: "yes"
        type?.applyHouseTypeTo(tags)
    } else {
        tags.remove("construction")
        type?.applyTo(tags)
        status?.applyTo(tags)
    }

    // Update check_date only when neither selection changed the tags.
    if (!tags.hasChanges) {
        tags.updateCheckDate()
    }
}

private fun BuildingType.applyTo(tags: Tags, postfix: String = "") {
    require(osmKey != null && osmValue != null)

    // do not change anything if it is considered an alias! (This could destroy information, e.g.
    // building=livestock would be changed to building=farm_auxiliary)
    val alias = BuildingType.aliases.entries.find { tags[it.key.first] == it.key.second }?.value
    if (alias != null) return

    // Switch between man-made and building.
    if (osmKey == "man_made") tags.remove("building" + postfix)
    if (osmKey == "building") tags.remove("man_made" + postfix)

    applyHouseTypeTo(tags)

    // Apply the original building classification.
    tags[osmKey + postfix] = osmValue
}

private fun BuildingStatus.applyTo(tags: Tags) {
    clearBuildingStatus(tags)
    when (this) {
        is BuildingLifecycle -> tags[osmKey] = "yes"
        is BuildingType -> applyTo(tags, ":use")
    }
}

private fun BuildingType?.applyHouseTypeTo(tags: Tags) {
    if (this in BuildingType.houses) {
        if (this == TERRACED_HOUSE) {
            tags["house"] = "terraced"
        }
        // like with aliases, preserve building=house + house=* (etc.) tagging
        else if (tags.containsKey("house")) {
            val houseType = createBuildingTypeFromHouseType(tags["house"])
            if (houseType == this) return
        }
    } else {
        tags.remove("house")
    }
}

private fun BuildingType.getHouseTypeValue(): String? = when (this) {
    HOUSE -> null
    DETACHED -> "detached"
    TERRACE -> "terrace"
    TERRACED_HOUSE -> "terraced"
    SEMI_DETACHED -> "semi-detached"
    BUNGALOW -> "bungalow"
    else -> null
}

private fun clearBuildingStatus(tags: Tags) {
    BuildingLifecycle.entries.forEach {
        // 1. the more common disused = yes etc.
        tags.remove(it.osmKey)
        // 2. the more rare (for buildings) disused:building=*
        tags.remove(it.osmKey + ":building")
    }
    tags.remove("building:use")
    tags.remove("man_made:use")
}


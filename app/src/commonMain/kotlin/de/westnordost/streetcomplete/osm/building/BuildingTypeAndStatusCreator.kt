package de.westnordost.streetcomplete.osm.building

import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.building.BuildingLifecycle.*
import de.westnordost.streetcomplete.osm.building.BuildingType.*
import de.westnordost.streetcomplete.osm.updateCheckDate

fun BuildingTypeAndStatus.applyTo(tags: Tags) {
    // special case for status=CONSTRUCTION, it is tagged like
    // building=construction (+ construction=actual_building_value) but only for building=*
    if (status == CONSTRUCTION && (type == null || type.osmKey == "building")) {
        type.applyTo(tags)
        (null as BuildingStatus?).applyTo(tags)
        // building=construction + construction=yes is documented as discouraged. If the kind of
        // building is not known, construction=yes is simply left out.
        val buildingValue = tags["building"] ?: "yes"
        if (buildingValue != "yes") {
            tags["construction"] = buildingValue
        } else {
            tags.remove("construction")
        }
        tags["building"] = "construction"
    } else {
        tags["construction"]?.let { tags["building"] = it }
        type.applyTo(tags)
        // current use same as building type is redundant
        status.takeIf { it != type }.applyTo(tags)
    }

    // Update check_date only when neither selection changed the tags.
    if (!tags.hasChanges) {
        tags.updateCheckDate()
    }
}

private fun BuildingType?.applyTo(tags: Tags, postfix: String = "") {
    // unsupported: don't change anything
    if (this == UNSUPPORTED) {
        return
    }
    // `null` on the other hand shall set it to building=yes
    if (this == null) {
        tags.remove("man_made" + postfix)
        tags.remove("house")
        tags["building" + postfix] = "yes"
        return
    }

    // do not change anything if it is considered an alias! (This could destroy information, e.g.
    // building=livestock would be changed to building=farm_auxiliary. We don't want that)
    val alias = BuildingType.aliases.entries.find { tags[it.key.first] == it.key.second }?.value
    if (alias != null && alias == this) return

    // Switch between man-made and building.
    if (osmKey != "building") tags.remove("building" + postfix)
    if (osmKey != "man_made") tags.remove("man_made" + postfix)

    if (this in BuildingType.houses) {
        if (this == TERRACED_HOUSE) {
            tags["house"] = "terraced"
        } else {
            // like with aliases, preserve building=house + house=* (etc.) tagging.
            if (this == createBuildingTypeFromHouseType(tags["house"])) return
            tags.remove("house")
        }
    } else {
        tags.remove("house")
    }

    tags[osmKey + postfix] = osmValue!!
}

private fun BuildingStatus?.applyTo(tags: Tags) {
    // clear previous status tagging
    BuildingLifecycle.entries.forEach {
        // 1. the more common disused = yes etc.
        tags.remove(it.osmKey)
        // 2. the more rare (for buildings) disused:building=*
        tags.remove(it.osmKey + ":building")
    }
    tags.remove("building:use")
    tags.remove("man_made:use")
    if (this == null) return
    when (this) {
        is BuildingLifecycle -> tags[osmKey] = "yes"
        is BuildingType -> applyTo(tags, ":use")
    }
}

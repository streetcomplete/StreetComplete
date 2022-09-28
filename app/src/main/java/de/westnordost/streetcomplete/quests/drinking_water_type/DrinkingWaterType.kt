package de.westnordost.streetcomplete.quests.drinking_water_type

import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.hasCheckDate
import de.westnordost.streetcomplete.osm.updateCheckDate
import de.westnordost.streetcomplete.quests.drinking_water_type.DrinkingWaterType.*

enum class DrinkingWaterType {
    WATER_TAP,
    WATER_FOUNTAIN_JET,
    WATER_FOUNTAIN_BOTTLE_REFILL_ONLY,
    WATER_FOUNTAIN_GENERIC,
    HAND_PUMP,
    WATER_WELL_WITHOUT_PUMP,
    SPRING,
    DISUSED_DRINKING_WATER,
}

fun DrinkingWaterType.applyTo(tags: Tags) {
    when (this) {
        WATER_FOUNTAIN_GENERIC -> tags["fountain"] == "drinking"
        WATER_FOUNTAIN_JET -> tags["fountain"] == "bubbler"
        WATER_FOUNTAIN_BOTTLE_REFILL_ONLY -> tags["fountain"] == "bottle_refill"
        WATER_TAP -> tags["man_made"] == "water_tap"
        HAND_PUMP -> {
            tags["man_made"] = "water_well"
            tags["pump"] = "manual"
        }
        WATER_WELL_WITHOUT_PUMP -> {
            tags["man_made"] = "water_well"
            tags["pump"] = "no"
        }
        SPRING -> tags["natural"] == "spring"
        DISUSED_DRINKING_WATER -> {
            var changed = false
            if (tags["amenity"] == "drinking_water") {
                tags.remove("amenity")
                changed = true
            }
            if (tags["amenity"] != "drinking_water") {
                tags["disused:amenity"] = "drinking_water"
                changed = true
            }
            if (!changed || tags.hasCheckDate()) {
                tags.updateCheckDate()
            }
        }
    }

    // handle change from disused:amenity=drinking_water to amenity=drinking_water
    if (this != DISUSED_DRINKING_WATER) {
        if (tags["disused:amenity"] == "drinking_water") {
            tags.remove("disused:amenity")
        }
        if (tags["amenity"] != "drinking_water") {
            tags["amenity"] = "drinking_water"
        }
    }
}

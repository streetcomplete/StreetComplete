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
        WATER_TAP -> tags["man_made"] = "water_tap"
        WATER_FOUNTAIN_JET -> tags["fountain"] = "bubbler"
        WATER_FOUNTAIN_BOTTLE_REFILL_ONLY -> tags["fountain"] = "bottle_refill"
        WATER_FOUNTAIN_GENERIC -> tags["fountain"] = "drinking"
        HAND_PUMP -> {
            tags["man_made"] = "water_well"
            tags["pump"] = "manual"
        }
        WATER_WELL_WITHOUT_PUMP -> {
            tags["man_made"] = "water_well"
            tags["pump"] = "no"
        }
        SPRING -> tags["natural"] = "spring"
        DISUSED_DRINKING_WATER -> {
            tags.remove("amenity")
            if (tags["disused:amenity"] != "drinking_water") {
                tags["disused:amenity"] = "drinking_water"
            }
            if (!tags.hasChanges || tags.hasCheckDate()) {
                // may happen if it was disused and now is still disused
                tags.updateCheckDate()
            }
        }
    }

    // handle change from disused:amenity=drinking_water to amenity=drinking_water
    if (this != DISUSED_DRINKING_WATER) {
        tags.remove("disused:amenity")
        if (tags["amenity"] != "drinking_water") {
            tags["amenity"] = "drinking_water"
        }
    }
}

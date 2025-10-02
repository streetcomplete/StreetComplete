package de.westnordost.streetcomplete.quests.charging_station_capacity

/*
 Returns true if the element can be accessed by other vehicles than specified in the tag field.
 */
fun canBeAccessedByOtherVehicle(tags: Map<String, String>, tag: String): Boolean {
    val accessTags = listOf(
        // All access tags with at least one usage.
        "motorcar", "hgv", "bus", "bicycle", "goods", "scooter", "motorcycle", "electric_bicycle",
        "cargo_bike", "kick_scooter", "caravan", "moped", "speed_pedelec", "mofa",
        "small_electric_vehicle", "motorhome", "goods", "nev", "psv", "bus", "taxi", "disabled",
        "wheelchair", "boat", "ship", "train"

    ).filter { it != tag }
    return accessTags.any { accessTag ->
        tags[accessTag].let { it == "designated" || it == "yes" }
    }
}

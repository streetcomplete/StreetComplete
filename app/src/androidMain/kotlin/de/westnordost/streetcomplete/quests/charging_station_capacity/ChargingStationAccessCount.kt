package de.westnordost.streetcomplete.quests.charging_station_capacity

/*
 Returns true if the element can be accessed by other vehicles than specified in the tag field.
 */
fun canBeAccessedByOtherVehicle(tags: Map<String, String>, tag: String): Boolean {
    val accessTags = listOf(
        "motor_vehicle", "motorcar", "hgv", "bus", "bicycle", "goods", "scooter", "motorcycle",
    ).filter { it != tag }
    return accessTags.any { accessTag ->
        tags[accessTag].let { it == "designated" || it == "yes" }
    }
}

package de.westnordost.streetcomplete.quests.charging_station_capacity

fun otherAccessTag(tags: Map<String, String>, tag: String): Boolean {
    val accessTags = listOf(
        "motor_vehicle", "motorcar", "hgv", "bus", "bicycle", "goods", "scooter", "motorcycle",
    ).filter { it != tag }
    return accessTags.any { accessTag ->
        tags[accessTag].let { it == "designated" || it == "yes" }
    }
}

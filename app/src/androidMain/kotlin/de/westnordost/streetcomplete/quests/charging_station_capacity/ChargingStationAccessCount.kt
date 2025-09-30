package de.westnordost.streetcomplete.quests.charging_station_capacity

fun countAccessTags(tags: Map<String, String>): Int {
    val accessTags = listOf(
        "motor_vehicle", "motorcar", "hgv", "bus", "bicycle", "goods", "scooter", "motorcycle",
    )
    return accessTags.count { accessTag ->
        tags[accessTag].let { it == "designated" || it == "yes" }
    }
}

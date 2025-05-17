package de.westnordost.streetcomplete.osm.lane_narrowing_traffic_calming

import de.westnordost.streetcomplete.osm.lane_narrowing_traffic_calming.LaneNarrowingTrafficCalming.*

/** Parses only the part of traffic_calming that relates to the narrowing of the road */
fun parseNarrowingTrafficCalming(tags: Map<String, String>): LaneNarrowingTrafficCalming? {
    val values = tags["traffic_calming"]
        ?.let { expandTrafficCalmingValue(it) }.orEmpty()
        .toMutableList()

    // `crossing:island=yes` (or deprecated `crossing=island`) implies `traffic_calming=island`
    if (tags["highway"] == "crossing"
        && (tags["crossing:island"] == "yes" || tags["crossing"] == "island")
        && "island" !in values
    ) {
        values.add("island")
    }

    return when {
        "island" in values && "choker" in values -> CHOKED_ISLAND
        "choker" in values -> CHOKER
        "island" in values -> ISLAND
        "chicane" in values -> CHICANE
        else -> null
    }
}

/* according to the wiki documentation, values such as `traffic_calming=rumble_strip;island;choker`
   are fine and in use but at the same time, values such as `traffic_calming=choked_island` are,
   too. So we need to do some normalization
 */
internal fun expandTrafficCalmingValue(values: String): List<String> =
    values
        .split(';') // split e.g. choker;table;island
        .flatMap {
            when {
                // e.g. choked_table, choked_island... anything chocked_* is also a choker
                it.startsWith("choked_") ->
                    listOf("choker", it.substringAfter('_'))
                else ->
                    listOf(it)
            }
        }

package de.westnordost.streetcomplete.osm.lane_narrowing_traffic_calming

import de.westnordost.streetcomplete.osm.lane_narrowing_traffic_calming.LaneNarrowingTrafficCalming.*

/** Parses only the part of traffic_calming that relates to the narrowing of the road */
fun createNarrowingTrafficCalming(tags: Map<String, String>): LaneNarrowingTrafficCalming? {

    val values = tags["traffic_calming"]
        ?.let { expandTrafficCalmingValue(it) }.orEmpty()
        .toMutableList()

    // `crossing:island=yes` implies `traffic_calming=island`
    if (tags["crossing:island"] == "yes" && "island" !in values) {
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

// TODO tests

/* according to the wiki documentation, values such as `traffic_calming=rumble_strip;island;choker`
   are fine and in use but at the same time, values such as `traffic_calming=choked_island` are,
   too. So we need to do some normalization
 */
internal fun expandTrafficCalmingValue(values: String): List<String> =
    values
        .split(';') // split e.g. choker;table;island
        .flatMap {
            when  {
                // only choked_island, not painted_island, the latter is not painted+island but something distinct
                it == "choked_island" ->
                    listOf("choker", "island")
                // e.g. choked_table, ... anything chocked_* is also a choker
                it.startsWith("choked_") ->
                    listOf("choker", it.substringAfter('_'))
                else ->
                    listOf(it)
            }
        }

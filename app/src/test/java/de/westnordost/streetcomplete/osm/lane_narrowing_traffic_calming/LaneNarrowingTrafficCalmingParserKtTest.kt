package de.westnordost.streetcomplete.osm.lane_narrowing_traffic_calming

import de.westnordost.streetcomplete.osm.lane_narrowing_traffic_calming.LaneNarrowingTrafficCalming.*
import kotlin.test.*
import kotlin.test.Test

class LaneNarrowingTrafficCalmingParserKtTest {
    @Test fun none() {
        assertEquals(null, parse())
    }

    @Test fun unknown() {
        assertEquals(null, parse("traffic_calming" to "rumble_strip"))
    }

    @Test fun chicane() {
        assertEquals(CHICANE, parse("traffic_calming" to "chicane"))
        assertEquals(CHICANE, parse("traffic_calming" to "rumble_strip;chicane"))
    }

    @Test fun choker() {
        assertEquals(CHOKER, parse("traffic_calming" to "choker"))
        assertEquals(CHOKER, parse("traffic_calming" to "choked_table"))
        assertEquals(CHOKER, parse("traffic_calming" to "choker;rumble_strip"))
        assertEquals(CHOKER, parse("traffic_calming" to "table;choker;cows"))
        assertEquals(CHOKER, parse("traffic_calming" to "rumble_strip;choked_table"))
    }

    @Test fun island() {
        assertEquals(ISLAND, parse("traffic_calming" to "island"))
        assertEquals(ISLAND, parse("traffic_calming" to "rumble_strip;island"))
        assertEquals(ISLAND, parse("traffic_calming" to "island;cows"))
        assertEquals(ISLAND, parse("highway" to "crossing", "crossing:island" to "yes"))
        assertEquals(ISLAND, parse("highway" to "crossing", "crossing" to "island"))
        // highway=crossing is required
        assertEquals(null, parse( "crossing:island" to "yes"))
    }

    @Test fun choked_island() {
        assertEquals(CHOKED_ISLAND, parse("traffic_calming" to "choker;island"))
        assertEquals(CHOKED_ISLAND, parse("traffic_calming" to "island;choker"))
        assertEquals(CHOKED_ISLAND, parse("traffic_calming" to "choked_island"))
        assertEquals(CHOKED_ISLAND, parse("traffic_calming" to "choked_table;island"))
        assertEquals(CHOKED_ISLAND, parse("traffic_calming" to "island;choked_table"))
        assertEquals(CHOKED_ISLAND, parse(
            "traffic_calming" to "choker",
            "highway" to "crossing",
            "crossing" to "island"
        ))
        assertEquals(CHOKED_ISLAND, parse(
            "traffic_calming" to "choker",
            "highway" to "crossing",
            "crossing:island" to "yes"
        ))
    }

    @Test fun `expand traffic calming value`() {
        assertEquals(
            listOf("choker"),
            expandTrafficCalmingValue("choker")
        )
        assertEquals(
            listOf("choker", "cows"),
            expandTrafficCalmingValue("choker;cows")
        )
        assertEquals(
            listOf("choker", "table", "horny_cows"),
            expandTrafficCalmingValue("choker;table;horny_cows")
        )
        assertEquals(
            listOf("choker", "table", "horny_cows"),
            expandTrafficCalmingValue("choked_table;horny_cows")
        )
        assertEquals(
            listOf("choker", "island"),
            expandTrafficCalmingValue("choked_island")
        )
        assertEquals(
            listOf("painted_island"),
            expandTrafficCalmingValue("painted_island")
        )
    }
}

private fun parse(vararg tags: Pair<String, String>): LaneNarrowingTrafficCalming? =
    createNarrowingTrafficCalming(mapOf(*tags))

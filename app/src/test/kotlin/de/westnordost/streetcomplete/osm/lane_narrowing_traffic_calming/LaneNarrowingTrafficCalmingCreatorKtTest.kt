package de.westnordost.streetcomplete.osm.lane_narrowing_traffic_calming

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryChange
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryDelete
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryModify
import de.westnordost.streetcomplete.osm.lane_narrowing_traffic_calming.LaneNarrowingTrafficCalming.*
import de.westnordost.streetcomplete.osm.nowAsCheckDateString
import kotlin.test.Test
import kotlin.test.assertEquals

class LaneNarrowingTrafficCalmingCreatorKtTest {
    @Test fun `set traffic_calming`() {
        assertEquals(
            setOf(StringMapEntryAdd("traffic_calming", "choker")),
            CHOKER.appliedTo(mapOf())
        )
        assertEquals(
            setOf(StringMapEntryAdd("traffic_calming", "chicane")),
            CHICANE.appliedTo(mapOf())
        )
        assertEquals(
            setOf(StringMapEntryAdd("traffic_calming", "island")),
            ISLAND.appliedTo(mapOf())
        )
        assertEquals(
            setOf(StringMapEntryAdd("traffic_calming", "choker;island")),
            CHOKED_ISLAND.appliedTo(mapOf())
        )

        // nothing
        assertEquals(
            setOf(
                StringMapEntryDelete("traffic_calming", "choker"),
                StringMapEntryDelete("check_date:traffic_calming", "2000-10-10"),
            ),
            (null as LaneNarrowingTrafficCalming?).appliedTo(mapOf(
                "traffic_calming" to "choker",
                "check_date:traffic_calming" to "2000-10-10"
            ))
        )

        // nothing changed
        assertEquals(
            setOf(
                StringMapEntryModify("traffic_calming", "island", "island"),
                StringMapEntryAdd("check_date:traffic_calming", nowAsCheckDateString()),
            ),
            ISLAND.appliedTo(mapOf("traffic_calming" to "island"))
        )

        // something changed & check date set
        assertEquals(
            setOf(
                StringMapEntryModify("traffic_calming", "choker", "island"),
                StringMapEntryModify("check_date:traffic_calming", "2000-10-10", nowAsCheckDateString()),
            ),
            ISLAND.appliedTo(mapOf(
                "traffic_calming" to "choker",
                "check_date:traffic_calming" to "2000-10-10"
            ))
        )
    }

    @Test fun `merge with current traffic_calming`() {
        assertEquals(
            setOf(
                StringMapEntryModify("traffic_calming", "rumble_strip", "choker;rumble_strip")
            ),
            CHOKER.appliedTo(mapOf("traffic_calming" to "rumble_strip"))
        )
        assertEquals(
            setOf(
                StringMapEntryModify("traffic_calming", "cows", "choker;island;cows")
            ),
            CHOKED_ISLAND.appliedTo(mapOf("traffic_calming" to "cows"))
        )
        assertEquals(
            setOf(
                StringMapEntryModify("traffic_calming", "cows;choker", "choker;island;cows")
            ),
            CHOKED_ISLAND.appliedTo(mapOf("traffic_calming" to "cows;choker"))
        )
        assertEquals(
            setOf(
                StringMapEntryModify("traffic_calming", "choker", "chicane")
            ),
            CHICANE.appliedTo(mapOf("traffic_calming" to "choker"))
        )
        assertEquals(
            setOf(
                StringMapEntryModify("traffic_calming", "cows;choker", "chicane;cows")
            ),
            CHICANE.appliedTo(mapOf("traffic_calming" to "cows;choker"))
        )
        assertEquals(
            setOf(
                StringMapEntryModify("traffic_calming", "choked_table", "choker;table")
            ),
            CHOKER.appliedTo(mapOf("traffic_calming" to "choked_table"))
        )

        // nothing
        assertEquals(
            setOf(
                StringMapEntryModify("traffic_calming", "choker;cows;island", "cows")
            ),
            (null as LaneNarrowingTrafficCalming?).appliedTo(mapOf(
                "traffic_calming" to "choker;cows;island"
            )),
        )

        // nothing changed
        assertEquals(
            setOf(
                StringMapEntryModify("traffic_calming", "choker;cows", "choker;cows"),
                StringMapEntryAdd("check_date:traffic_calming", nowAsCheckDateString()),
            ),
            CHOKER.appliedTo(mapOf("traffic_calming" to "choker;cows"))
        )

        // something changed & check date set
        assertEquals(
            setOf(
                StringMapEntryModify("traffic_calming", "choker;cows", "island;cows"),
                StringMapEntryModify("check_date:traffic_calming", "2000-10-10", nowAsCheckDateString()),
            ),
            ISLAND.appliedTo(mapOf(
                "traffic_calming" to "choker;cows",
                "check_date:traffic_calming" to "2000-10-10"
            ))
        )
    }

    @Test fun `updates crossing island`() {
        // island, add
        assertEquals(
            setOf(
                StringMapEntryAdd("crossing:island", "yes"),
                StringMapEntryAdd("traffic_calming", "island")
            ),
            ISLAND.appliedTo(mapOf("highway" to "crossing"))
        )

        // island, modify
        assertEquals(
            setOf(
                StringMapEntryModify("crossing:island", "no", "yes"),
                StringMapEntryAdd("traffic_calming", "island")
            ),
            ISLAND.appliedTo(mapOf(
                "highway" to "crossing",
                "crossing:island" to "no"
            ))
        )

        // no island, add
        assertEquals(
            setOf(
                StringMapEntryAdd("crossing:island", "no"),
                StringMapEntryAdd("traffic_calming", "choker"),
            ),
            CHOKER.appliedTo(mapOf("highway" to "crossing"))
        )

        // no island, modify
        assertEquals(
            setOf(
                StringMapEntryModify("crossing:island", "yes", "no"),
                StringMapEntryAdd("traffic_calming", "choker"),
            ),
            CHOKER.appliedTo(mapOf(
                "highway" to "crossing",
                "crossing:island" to "yes"
            ))
        )

        // nothing changed
        assertEquals(
            setOf(
                StringMapEntryModify("crossing:island", "yes", "yes"),
                StringMapEntryModify("traffic_calming", "island", "island"),
                StringMapEntryAdd("check_date:traffic_calming", nowAsCheckDateString()),
            ),
            ISLAND.appliedTo(mapOf(
                "highway" to "crossing",
                "crossing:island" to "yes",
                "traffic_calming" to "island"
            ))
        )

        // something changed & check date set
        assertEquals(
            setOf(
                StringMapEntryModify("crossing:island", "no", "yes"),
                StringMapEntryModify("traffic_calming", "island", "island"),
                StringMapEntryModify("check_date:traffic_calming", "2000-10-10", nowAsCheckDateString()),
            ),
            ISLAND.appliedTo(mapOf(
                "highway" to "crossing",
                "crossing:island" to "no",
                "traffic_calming" to "island",
                "check_date:traffic_calming" to "2000-10-10"
            ))
        )
    }

    @Test fun `updating crossing island removes deprecated crossing=island`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("crossing:island", "yes"),
                StringMapEntryDelete("crossing", "island"),
                StringMapEntryAdd("traffic_calming", "island")
            ),
            ISLAND.appliedTo(mapOf(
                "highway" to "crossing",
                "crossing" to "island"
            ))
        )
        assertEquals(
            setOf(
                StringMapEntryAdd("crossing:island", "no"),
                StringMapEntryDelete("crossing", "island"),
                StringMapEntryAdd("traffic_calming", "choker"),
            ),
            CHOKER.appliedTo(mapOf(
                "highway" to "crossing",
                "crossing" to "island"
            ))
        )
    }
}

private fun LaneNarrowingTrafficCalming?.appliedTo(tags: Map<String, String>): Set<StringMapEntryChange> {
    val cb = StringMapChangesBuilder(tags)
    applyTo(cb)
    return cb.create().changes
}

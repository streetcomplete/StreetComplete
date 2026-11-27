package de.westnordost.streetcomplete.osm.traffic_calming

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryChange
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryDelete
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryModify
import de.westnordost.streetcomplete.osm.nowAsCheckDateString
import de.westnordost.streetcomplete.osm.traffic_calming.LaneNarrowingTrafficCalming.*
import kotlin.test.Test
import kotlin.test.assertEquals

class LaneNarrowingTrafficCalmingCreatorKtTest {
    @Test fun `set traffic_calming`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("traffic_calming", "choker"),
                StringMapEntryAdd("check_date:traffic_calming", nowAsCheckDateString()),
            ),
            CHOKER.appliedTo(mapOf())
        )
        assertEquals(
            setOf(
                StringMapEntryAdd("traffic_calming", "chicane"),
                StringMapEntryAdd("check_date:traffic_calming", nowAsCheckDateString()),
            ),
            CHICANE.appliedTo(mapOf())
        )
        assertEquals(
            setOf(
                StringMapEntryAdd("traffic_calming", "island"),
                StringMapEntryAdd("check_date:traffic_calming", nowAsCheckDateString()),
            ),
            ISLAND.appliedTo(mapOf())
        )
        assertEquals(
            setOf(
                StringMapEntryAdd("traffic_calming", "choker;island"),
                StringMapEntryAdd("check_date:traffic_calming", nowAsCheckDateString()),
            ),
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
    }

    @Test fun `merge with current traffic_calming`() {
        assertEquals(
            setOf(
                StringMapEntryModify("traffic_calming", "rumble_strip", "choker;rumble_strip"),
                StringMapEntryAdd("check_date:traffic_calming", nowAsCheckDateString()),
            ),
            CHOKER.appliedTo(mapOf("traffic_calming" to "rumble_strip"))
        )
        assertEquals(
            setOf(
                StringMapEntryModify("traffic_calming", "cows", "choker;island;cows"),
                StringMapEntryAdd("check_date:traffic_calming", nowAsCheckDateString()),
            ),
            CHOKED_ISLAND.appliedTo(mapOf("traffic_calming" to "cows"))
        )
        assertEquals(
            setOf(
                StringMapEntryModify("traffic_calming", "cows;choker", "choker;island;cows"),
                StringMapEntryAdd("check_date:traffic_calming", nowAsCheckDateString()),
            ),
            CHOKED_ISLAND.appliedTo(mapOf("traffic_calming" to "cows;choker"))
        )
        assertEquals(
            setOf(
                StringMapEntryModify("traffic_calming", "choker", "chicane"),
                StringMapEntryAdd("check_date:traffic_calming", nowAsCheckDateString()),
            ),
            CHICANE.appliedTo(mapOf("traffic_calming" to "choker"))
        )
        assertEquals(
            setOf(
                StringMapEntryModify("traffic_calming", "cows;choker", "chicane;cows"),
                StringMapEntryAdd("check_date:traffic_calming", nowAsCheckDateString()),
            ),
            CHICANE.appliedTo(mapOf("traffic_calming" to "cows;choker"))
        )
        assertEquals(
            setOf(
                StringMapEntryModify("traffic_calming", "choked_table", "choker;table"),
                StringMapEntryAdd("check_date:traffic_calming", nowAsCheckDateString()),
            ),
            CHOKER.appliedTo(mapOf("traffic_calming" to "choked_table"))
        )

        // nothing
        assertEquals(
            setOf(
                StringMapEntryModify("traffic_calming", "choker;cows;island", "cows"),
                StringMapEntryAdd("check_date:traffic_calming", nowAsCheckDateString()),
            ),
            (null as LaneNarrowingTrafficCalming?).appliedTo(mapOf(
                "traffic_calming" to "choker;cows;island"
            )),
        )
    }

    @Test fun `updates crossing island`() {
        // island, add
        assertEquals(
            setOf(
                StringMapEntryAdd("crossing:island", "yes"),
                StringMapEntryAdd("traffic_calming", "island"),
                StringMapEntryAdd("check_date:traffic_calming", nowAsCheckDateString()),
            ),
            ISLAND.appliedTo(mapOf("highway" to "crossing"))
        )

        // island, modify
        assertEquals(
            setOf(
                StringMapEntryModify("crossing:island", "no", "yes"),
                StringMapEntryAdd("traffic_calming", "island"),
                StringMapEntryAdd("check_date:traffic_calming", nowAsCheckDateString()),
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
                StringMapEntryAdd("check_date:traffic_calming", nowAsCheckDateString()),
            ),
            CHOKER.appliedTo(mapOf("highway" to "crossing"))
        )

        // no island, modify
        assertEquals(
            setOf(
                StringMapEntryModify("crossing:island", "yes", "no"),
                StringMapEntryAdd("traffic_calming", "choker"),
                StringMapEntryAdd("check_date:traffic_calming", nowAsCheckDateString()),
            ),
            CHOKER.appliedTo(mapOf(
                "highway" to "crossing",
                "crossing:island" to "yes"
            ))
        )
    }

    @Test fun `updating crossing island removes deprecated crossing=island`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("crossing:island", "yes"),
                StringMapEntryDelete("crossing", "island"),
                StringMapEntryAdd("traffic_calming", "island"),
                StringMapEntryAdd("check_date:traffic_calming", nowAsCheckDateString()),
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
                StringMapEntryAdd("check_date:traffic_calming", nowAsCheckDateString()),
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

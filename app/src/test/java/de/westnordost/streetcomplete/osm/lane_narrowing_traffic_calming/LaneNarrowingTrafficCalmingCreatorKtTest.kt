package de.westnordost.streetcomplete.osm.lane_narrowing_traffic_calming

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryChange
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryDelete
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryModify
import de.westnordost.streetcomplete.osm.lane_narrowing_traffic_calming.LaneNarrowingTrafficCalming.*
import de.westnordost.streetcomplete.osm.nowAsCheckDateString
import org.assertj.core.api.Assertions
import kotlin.test.Test

class LaneNarrowingTrafficCalmingCreatorKtTest {
    @Test fun `set traffic_calming`() {
        verifyAnswer(
            mapOf(),
            CHOKER,
            arrayOf(
                StringMapEntryAdd("traffic_calming", "choker")
            )
        )
        verifyAnswer(
            mapOf(),
            CHICANE,
            arrayOf(
                StringMapEntryAdd("traffic_calming", "chicane")
            )
        )
        verifyAnswer(
            mapOf(),
            ISLAND,
            arrayOf(
                StringMapEntryAdd("traffic_calming", "island")
            )
        )
        verifyAnswer(
            mapOf(),
            CHOKED_ISLAND,
            arrayOf(
                StringMapEntryAdd("traffic_calming", "choker;island")
            )
        )

        // nothing
        verifyAnswer(
            mapOf("traffic_calming" to "choker", "check_date:traffic_calming" to "2000-10-10"),
            null,
            arrayOf(
                StringMapEntryDelete("traffic_calming", "choker"),
                StringMapEntryDelete("check_date:traffic_calming", "2000-10-10"),
            )
        )

        // nothing changed
        verifyAnswer(
            mapOf("traffic_calming" to "island"),
            ISLAND,
            arrayOf(
                StringMapEntryModify("traffic_calming", "island", "island"),
                StringMapEntryAdd("check_date:traffic_calming", nowAsCheckDateString()),
            )
        )

        // something changed & check date set
        verifyAnswer(
            mapOf("traffic_calming" to "choker", "check_date:traffic_calming" to "2000-10-10"),
            ISLAND,
            arrayOf(
                StringMapEntryModify("traffic_calming", "choker", "island"),
                StringMapEntryModify("check_date:traffic_calming", "2000-10-10", nowAsCheckDateString()),
            )
        )
    }

    @Test fun `merge with current traffic_calming`() {
        verifyAnswer(
            mapOf("traffic_calming" to "rumble_strip"),
            CHOKER,
            arrayOf(
                StringMapEntryModify("traffic_calming", "rumble_strip", "choker;rumble_strip")
            )
        )
        verifyAnswer(
            mapOf("traffic_calming" to "cows"),
            CHOKED_ISLAND,
            arrayOf(
                StringMapEntryModify("traffic_calming", "cows", "choker;island;cows")
            )
        )
        verifyAnswer(
            mapOf("traffic_calming" to "cows;choker"),
            CHOKED_ISLAND,
            arrayOf(
                StringMapEntryModify("traffic_calming", "cows;choker", "choker;island;cows")
            )
        )
        verifyAnswer(
            mapOf("traffic_calming" to "choker"),
            CHICANE,
            arrayOf(
                StringMapEntryModify("traffic_calming", "choker", "chicane")
            )
        )
        verifyAnswer(
            mapOf("traffic_calming" to "cows;choker"),
            CHICANE,
            arrayOf(
                StringMapEntryModify("traffic_calming", "cows;choker", "chicane;cows")
            )
        )
        verifyAnswer(
            mapOf("traffic_calming" to "choked_table"),
            CHOKER,
            arrayOf(
                StringMapEntryModify("traffic_calming", "choked_table", "choker;table")
            )
        )

        // nothing
        verifyAnswer(
            mapOf("traffic_calming" to "choker;cows;island"),
            null,
            arrayOf(
                StringMapEntryModify("traffic_calming", "choker;cows;island", "cows")
            )
        )

        // nothing changed
        verifyAnswer(
            mapOf("traffic_calming" to "choker;cows"),
            CHOKER,
            arrayOf(
                StringMapEntryModify("traffic_calming", "choker;cows", "choker;cows"),
                StringMapEntryAdd("check_date:traffic_calming", nowAsCheckDateString()),
            )
        )

        // something changed & check date set
        verifyAnswer(
            mapOf("traffic_calming" to "choker;cows", "check_date:traffic_calming" to "2000-10-10"),
            ISLAND,
            arrayOf(
                StringMapEntryModify("traffic_calming", "choker;cows", "island;cows"),
                StringMapEntryModify("check_date:traffic_calming", "2000-10-10", nowAsCheckDateString()),
            )
        )
    }

    @Test fun `updates crossing island`() {
        // island, add
        verifyAnswer(
            mapOf("highway" to "crossing"),
            ISLAND,
            arrayOf(
                StringMapEntryAdd("crossing:island", "yes"),
                StringMapEntryAdd("traffic_calming", "island")
            )
        )

        // island, modify
        verifyAnswer(
            mapOf("highway" to "crossing", "crossing:island" to "no"),
            ISLAND,
            arrayOf(
                StringMapEntryModify("crossing:island", "no", "yes"),
                StringMapEntryAdd("traffic_calming", "island")
            )
        )

        // no island, add
        verifyAnswer(
            mapOf("highway" to "crossing"),
            CHOKER,
            arrayOf(
                StringMapEntryAdd("crossing:island", "no"),
                StringMapEntryAdd("traffic_calming", "choker"),
            )
        )

        // no island, modify
        verifyAnswer(
            mapOf("highway" to "crossing", "crossing:island" to "yes"),
            CHOKER,
            arrayOf(
                StringMapEntryModify("crossing:island", "yes", "no"),
                StringMapEntryAdd("traffic_calming", "choker"),
            )
        )

        // nothing changed
        verifyAnswer(
            mapOf(
                "highway" to "crossing",
                "crossing:island" to "yes",
                "traffic_calming" to "island"
            ),
            ISLAND,
            arrayOf(
                StringMapEntryModify("crossing:island", "yes", "yes"),
                StringMapEntryModify("traffic_calming", "island", "island"),
                StringMapEntryAdd("check_date:traffic_calming", nowAsCheckDateString()),
            )
        )

        // something changed & check date set
        verifyAnswer(
            mapOf(
                "highway" to "crossing",
                "crossing:island" to "no",
                "traffic_calming" to "island",
                "check_date:traffic_calming" to "2000-10-10"
            ),
            ISLAND,
            arrayOf(
                StringMapEntryModify("crossing:island", "no", "yes"),
                StringMapEntryModify("traffic_calming", "island", "island"),
                StringMapEntryModify("check_date:traffic_calming", "2000-10-10", nowAsCheckDateString()),
            )
        )
    }

    @Test fun `updating crossing island removes deprecated crossing=island`() {
        verifyAnswer(
            mapOf("highway" to "crossing", "crossing" to "island"),
            ISLAND,
            arrayOf(
                StringMapEntryAdd("crossing:island", "yes"),
                StringMapEntryDelete("crossing", "island"),
                StringMapEntryAdd("traffic_calming", "island")
            )
        )
        verifyAnswer(
            mapOf("highway" to "crossing", "crossing" to "island"),
            CHOKER,
            arrayOf(
                StringMapEntryAdd("crossing:island", "no"),
                StringMapEntryDelete("crossing", "island"),
                StringMapEntryAdd("traffic_calming", "choker"),
            )
        )
    }
}

private fun verifyAnswer(tags: Map<String, String>, answer: LaneNarrowingTrafficCalming?, expectedChanges: Array<StringMapEntryChange>) {
    val cb = StringMapChangesBuilder(tags)
    answer.applyTo(cb)
    val changes = cb.create().changes
    Assertions.assertThat(changes).containsExactlyInAnyOrder(*expectedChanges)
}

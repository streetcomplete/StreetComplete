package de.westnordost.streetcomplete.quests.lanes

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryDelete
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryModify
import de.westnordost.streetcomplete.quests.answerApplied
import de.westnordost.streetcomplete.quests.answerAppliedTo
import kotlin.test.Test
import kotlin.test.assertEquals

class AddLanesTest {

    private val questType = AddLanes()

    @Test fun `answering unmarked lanes`() {
        assertEquals(
            setOf(StringMapEntryAdd("lane_markings", "no")),
            questType.answerApplied(LanesAnswer.IsUnmarked)
        )
    }

    @Test fun `answering unmarked lanes deletes specific lanes`() {
        assertEquals(
            setOf(
                StringMapEntryModify("lane_markings", "yes", "no"),
                StringMapEntryDelete("lanes:forward", "2"),
                StringMapEntryDelete("lanes:backward", "1"),
                StringMapEntryDelete("lanes:both_ways", "1"),
                StringMapEntryDelete("turn:lanes:both_ways", "left"),
            ),
            questType.answerAppliedTo(
                LanesAnswer.IsUnmarked,
                mapOf(
                    "lanes" to "4",
                    "lane_markings" to "yes",
                    "lanes:forward" to "2",
                    "lanes:backward" to "1",
                    "lanes:both_ways" to "1",
                    "turn:lanes:both_ways" to "left"
                )
            )
        )
    }

    @Test fun `answering lanes for each side`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("lanes", "5"),
                StringMapEntryAdd("lanes:forward", "2"),
                StringMapEntryAdd("lanes:backward", "3")
            ),
            questType.answerApplied(Lanes(2, 3, false))
        )
    }

    @Test fun `answering lanes that are the same for each side`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("lanes", "4"),
            ),
            questType.answerApplied(Lanes(2, 2, false))
        )
    }

    @Test fun `answering lanes that are the same for each side with previous for-each-side answer`() {
        assertEquals(
            setOf(
                StringMapEntryModify("lanes", "5", "4"),
                StringMapEntryModify("lanes:forward", "2", "2"),
                StringMapEntryModify("lanes:backward", "3", "2")
            ),
            questType.answerAppliedTo(
                Lanes(2, 2),
                mapOf(
                    "lanes" to "5",
                    "lanes:forward" to "2",
                    "lanes:backward" to "3",
                )
            )
        )
    }

    @Test fun `answering lanes with center left turn lane`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("lanes", "4"),
                StringMapEntryAdd("lanes:forward", "1"),
                StringMapEntryAdd("lanes:backward", "2"),
                StringMapEntryAdd("lanes:both_ways", "1"),
                StringMapEntryAdd("turn:lanes:both_ways", "left")
            ),
            questType.answerApplied(Lanes(1, 2, true))
        )
    }

    @Test fun `answering lanes without center left turn lane deletes previously present center lane`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("lanes", "5"),
                StringMapEntryAdd("lanes:forward", "2"),
                StringMapEntryAdd("lanes:backward", "3"),
                StringMapEntryDelete("lanes:both_ways", "1"),
                StringMapEntryDelete("turn:lanes:both_ways", "left"),
            ),
            questType.answerAppliedTo(
                Lanes(2, 3, false),
                mapOf(
                    "lanes:both_ways" to "1",
                    "turn:lanes:both_ways" to "left"
                )
            )
        )
    }
}

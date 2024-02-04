package de.westnordost.streetcomplete.quests.lanes

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryDelete
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryModify
import de.westnordost.streetcomplete.quests.answerApplied
import de.westnordost.streetcomplete.quests.answerAppliedTo
import de.westnordost.streetcomplete.testutils.way
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AddLanesTest {

    private val questType = AddLanes()

    @Test fun `is applicable to residential roads if speed above 33`() {
        assertTrue(questType.isApplicableTo(way(tags = mapOf(
            "maxspeed" to "DE:urban",
            "highway" to "residential",
            "surface" to "asphalt"
        ))))
        assertTrue(questType.isApplicableTo(way(tags = mapOf(
            "source:maxspeed" to "DE:urban",
            "highway" to "residential",
            "surface" to "asphalt"
        ))))
        assertTrue(questType.isApplicableTo(way(tags = mapOf(
            "maxspeed" to "34",
            "highway" to "residential",
            "surface" to "asphalt"
        ))))
        assertTrue(questType.isApplicableTo(way(tags = mapOf(
            "maxspeed" to "30 mph",
            "highway" to "residential",
            "surface" to "asphalt"
        ))))
    }

    @Test fun `is not applicable to residential roads if speed is below or equal 33`() {
        assertFalse(questType.isApplicableTo(way(tags = mapOf(
            "maxspeed" to "DE:zone30",
            "highway" to "residential",
            "surface" to "asphalt"
        ))))
        assertFalse(questType.isApplicableTo(way(tags = mapOf(
            "source:maxspeed" to "DE:zone:30",
            "highway" to "residential",
            "surface" to "asphalt"
        ))))
        assertFalse(questType.isApplicableTo(way(tags = mapOf(
            "maxspeed:type" to "DE:30",
            "highway" to "residential",
            "surface" to "asphalt"
        ))))
        assertFalse(questType.isApplicableTo(way(tags = mapOf(
            "maxspeed" to "33",
            "highway" to "residential",
            "surface" to "asphalt"
        ))))
        assertFalse(questType.isApplicableTo(way(tags = mapOf(
            "maxspeed" to "20 mph",
            "highway" to "residential",
            "surface" to "asphalt"
        ))))
        assertFalse(questType.isApplicableTo(way(tags = mapOf(
            "maxspeed" to "walk",
            "highway" to "residential",
            "surface" to "asphalt"
        ))))
    }

    @Test fun `answering unmarked lanes`() {
        assertEquals(
            setOf(StringMapEntryAdd("lane_markings", "no")),
            questType.answerApplied(UnmarkedLanes)
        )
    }

    @Test fun `answering unmarked lanes with existing lane marking`() {
        assertEquals(
            setOf(StringMapEntryModify("lane_markings", "yes", "no")),
            questType.answerAppliedTo(
                UnmarkedLanes,
                mapOf("lanes" to "4", "lane_markings" to "yes")
            )
        )
    }

    @Test fun `answering unmarked lanes with existing forward and backward lanes marking`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("lane_markings", "no"),
                StringMapEntryDelete("lanes:forward", "2"),
                StringMapEntryDelete("lanes:backward", "3"),
            ),
            questType.answerAppliedTo(
                UnmarkedLanes,
                mapOf(
                    "lanes" to "5",
                    "lanes:forward" to "2",
                    "lanes:backward" to "3",
                )
            )
        )
    }

    @Test fun `answering marked lanes`() {
        assertEquals(
            setOf(StringMapEntryAdd("lanes", "4")),
            questType.answerApplied(MarkedLanes(4))
        )
    }

    @Test fun `answering marked lanes with previous unmarked answer`() {
        assertEquals(
            setOf(
                StringMapEntryModify("lanes", "5", "4"),
                StringMapEntryModify("lane_markings", "no", "yes"),
            ),
            questType.answerAppliedTo(
                MarkedLanes(4),
                mapOf(
                    "lanes" to "5",
                    "lane_markings" to "no"
                )
            )
        )
    }

    @Test fun `answering marked lanes with previous marked for each side answer`() {
        assertEquals(
            setOf(
                StringMapEntryModify("lanes", "5", "4"),
                StringMapEntryModify("lanes:forward", "2", "2"),
                StringMapEntryModify("lanes:backward", "3", "2")
            ),
            questType.answerAppliedTo(
                MarkedLanes(4),
                mapOf(
                    "lanes" to "5",
                    "lanes:forward" to "2",
                    "lanes:backward" to "3",
                )
            )
        )
    }

    @Test fun `answering marked lanes for each side`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("lanes", "5"),
                StringMapEntryAdd("lanes:forward", "2"),
                StringMapEntryAdd("lanes:backward", "3")
            ),
            questType.answerApplied(MarkedLanesSides(2, 3, false))
        )
    }

    @Test fun `answering marked lanes for each side with previous unmarked answer`() {
        assertEquals(
            setOf(
                StringMapEntryModify("lanes", "4", "5"),
                StringMapEntryModify("lane_markings", "no", "yes"),
                StringMapEntryAdd("lanes:forward", "2"),
                StringMapEntryAdd("lanes:backward", "3")
            ),
            questType.answerAppliedTo(
                MarkedLanesSides(2, 3, false),
                mapOf(
                    "lanes" to "4",
                    "lane_markings" to "no"
                )
            )
        )
    }

    @Test fun `answering marked lanes with previous marked answer`() {
        assertEquals(
            setOf(
                StringMapEntryModify("lanes", "4", "5"),
                StringMapEntryModify("lanes:forward", "2", "2"),
                StringMapEntryModify("lanes:backward", "2", "3")
            ),
            questType.answerAppliedTo(
                MarkedLanesSides(2, 3, false),
                mapOf(
                    "lanes" to "4",
                    "lanes:forward" to "2",
                    "lanes:backward" to "2",
                )
            )
        )
    }

    @Test fun `answering marked lanes for each side deletes center lane tagging`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("lanes", "5"),
                StringMapEntryAdd("lanes:forward", "2"),
                StringMapEntryAdd("lanes:backward", "3"),
                StringMapEntryDelete("lanes:both_ways", "1"),
                StringMapEntryDelete("turn:lanes:both_ways", "left"),
            ),
            questType.answerAppliedTo(
                MarkedLanesSides(2, 3, false),
                mapOf(
                    "lanes:both_ways" to "1",
                    "turn:lanes:both_ways" to "left"
                )
            )
        )
    }

    @Test fun `answering unmarked lanes deletes center lane tagging`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("lane_markings", "no"),
                StringMapEntryDelete("lanes:both_ways", "1"),
                StringMapEntryDelete("turn:lanes:both_ways", "left"),
            ),
            questType.answerAppliedTo(
                UnmarkedLanes,
                mapOf(
                    "lanes:both_ways" to "1",
                    "turn:lanes:both_ways" to "left"
                ),
            )
        )
    }

    @Test fun `answering marked lanes deletes center lane tagging`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("lanes", "2"),
                StringMapEntryDelete("lanes:both_ways", "1"),
                StringMapEntryDelete("turn:lanes:both_ways", "left"),
            ),
            questType.answerAppliedTo(
                MarkedLanes(2),
                mapOf(
                    "lanes:both_ways" to "1",
                    "turn:lanes:both_ways" to "left"
                )
            )
        )
    }

    @Test fun `answering marked lanes with center left turn lane`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("lanes", "3"),
                StringMapEntryAdd("lanes:forward", "1"),
                StringMapEntryAdd("lanes:backward", "1"),
                StringMapEntryAdd("lanes:both_ways", "1"),
                StringMapEntryAdd("turn:lanes:both_ways", "left")
            ),
            questType.answerApplied(MarkedLanesSides(1, 1, true))
        )
    }
}

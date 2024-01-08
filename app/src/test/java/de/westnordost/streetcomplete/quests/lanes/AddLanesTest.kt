package de.westnordost.streetcomplete.quests.lanes

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryDelete
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryModify
import de.westnordost.streetcomplete.quests.verifyAnswer
import de.westnordost.streetcomplete.testutils.way
import kotlin.test.Test
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
        questType.verifyAnswer(
            UnmarkedLanes,
            StringMapEntryAdd("lane_markings", "no"),
        )
    }

    @Test fun `answering unmarked lanes with existing lane marking`() {
        questType.verifyAnswer(
            mapOf(
                "lanes" to "4",
                "lane_markings" to "yes",
            ),
            UnmarkedLanes,
            StringMapEntryModify("lane_markings", "yes", "no"),
        )
    }

    @Test fun `answering unmarked lanes with existing forward and backward lanes marking`() {
        questType.verifyAnswer(
            mapOf(
                "lanes" to "5",
                "lanes:forward" to "2",
                "lanes:backward" to "3",
            ),
            UnmarkedLanes,
            StringMapEntryAdd("lane_markings", "no"),
            StringMapEntryDelete("lanes:forward", "2"),
            StringMapEntryDelete("lanes:backward", "3"),
        )
    }

    @Test fun `answering marked lanes`() {
        questType.verifyAnswer(
            MarkedLanes(4),
            StringMapEntryAdd("lanes", "4")
        )
    }

    @Test fun `answering marked lanes with previous unmarked answer`() {
        questType.verifyAnswer(
            mapOf(
                "lanes" to "5",
                "lane_markings" to "no"
            ),
            MarkedLanes(4),
            StringMapEntryModify("lanes", "5", "4"),
            StringMapEntryModify("lane_markings", "no", "yes"),
        )
    }

    @Test fun `answering marked lanes with previous marked for each side answer`() {
        questType.verifyAnswer(
            mapOf(
                "lanes" to "5",
                "lanes:forward" to "2",
                "lanes:backward" to "3",
            ),
            MarkedLanes(4),
            StringMapEntryModify("lanes", "5", "4"),
            StringMapEntryModify("lanes:forward", "2", "2"),
            StringMapEntryModify("lanes:backward", "3", "2")
        )
    }

    @Test fun `answering marked lanes for each side`() {
        questType.verifyAnswer(
            MarkedLanesSides(2, 3, false),
            StringMapEntryAdd("lanes", "5"),
            StringMapEntryAdd("lanes:forward", "2"),
            StringMapEntryAdd("lanes:backward", "3")
        )
    }

    @Test fun `answering marked lanes for each side with previous unmarked answer`() {
        questType.verifyAnswer(
            mapOf(
                "lanes" to "4",
                "lane_markings" to "no"
            ),
            MarkedLanesSides(2, 3, false),
            StringMapEntryModify("lanes", "4", "5"),
            StringMapEntryModify("lane_markings", "no", "yes"),
            StringMapEntryAdd("lanes:forward", "2"),
            StringMapEntryAdd("lanes:backward", "3")
        )
    }

    @Test fun `answering marked lanes with previous marked answer`() {
        questType.verifyAnswer(
            mapOf(
                "lanes" to "4",
                "lanes:forward" to "2",
                "lanes:backward" to "2",
            ),
            MarkedLanesSides(2, 3, false),
            StringMapEntryModify("lanes", "4", "5"),
            StringMapEntryModify("lanes:forward", "2", "2"),
            StringMapEntryModify("lanes:backward", "2", "3")
        )
    }

    @Test fun `answering marked lanes for each side deletes center lane tagging`() {
        questType.verifyAnswer(
            mapOf(
                "lanes:both_ways" to "1",
                "turn:lanes:both_ways" to "left"
            ),
            MarkedLanesSides(2, 3, false),
            StringMapEntryAdd("lanes", "5"),
            StringMapEntryAdd("lanes:forward", "2"),
            StringMapEntryAdd("lanes:backward", "3"),
            StringMapEntryDelete("lanes:both_ways", "1"),
            StringMapEntryDelete("turn:lanes:both_ways", "left"),
        )
    }

    @Test fun `answering unmarked lanes deletes center lane tagging`() {
        questType.verifyAnswer(
            mapOf(
                "lanes:both_ways" to "1",
                "turn:lanes:both_ways" to "left"
            ),
            UnmarkedLanes,
            StringMapEntryAdd("lane_markings", "no"),
            StringMapEntryDelete("lanes:both_ways", "1"),
            StringMapEntryDelete("turn:lanes:both_ways", "left"),
        )
    }

    @Test fun `answering marked lanes deletes center lane tagging`() {
        questType.verifyAnswer(
            mapOf(
                "lanes:both_ways" to "1",
                "turn:lanes:both_ways" to "left"
            ),
            MarkedLanes(2),
            StringMapEntryAdd("lanes", "2"),
            StringMapEntryDelete("lanes:both_ways", "1"),
            StringMapEntryDelete("turn:lanes:both_ways", "left"),
        )
    }

    @Test fun `answering marked lanes with center left turn lane`() {
        questType.verifyAnswer(
            MarkedLanesSides(1, 1, true),
            StringMapEntryAdd("lanes", "3"),
            StringMapEntryAdd("lanes:forward", "1"),
            StringMapEntryAdd("lanes:backward", "1"),
            StringMapEntryAdd("lanes:both_ways", "1"),
            StringMapEntryAdd("turn:lanes:both_ways", "left")
        )
    }
}

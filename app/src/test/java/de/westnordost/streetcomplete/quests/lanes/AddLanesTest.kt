package de.westnordost.streetcomplete.quests.lanes

import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryDelete
import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryModify
import de.westnordost.streetcomplete.quests.verifyAnswer
import org.junit.Test

class AddLanesTest {

    private val questType = AddLanes()

    @Test fun `answering unmarked lanes`() {
        questType.verifyAnswer(
            UnmarkedLanes(2),
            StringMapEntryAdd("lanes", "2"),
            StringMapEntryAdd("lane_markings", "no"),
        )
    }

    @Test fun `answering unmarked lanes with existing lane marking`() {
        questType.verifyAnswer(
            mapOf(
                "lanes" to "4",
                "lane_markings" to "yes",
            ),
            UnmarkedLanes(2),
            StringMapEntryModify("lanes", "4", "2"),
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
            UnmarkedLanes(2),
            StringMapEntryModify("lanes", "5", "2"),
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
            MarkedLanesSides(2,3),
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
            MarkedLanesSides(2, 3),
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
            MarkedLanesSides(2, 3),
            StringMapEntryModify("lanes", "4", "5"),
            StringMapEntryModify("lanes:forward", "2", "2"),
            StringMapEntryModify("lanes:backward", "2", "3")
        )
    }
}

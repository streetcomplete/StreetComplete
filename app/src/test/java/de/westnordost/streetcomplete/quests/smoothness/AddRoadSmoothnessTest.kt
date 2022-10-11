package de.westnordost.streetcomplete.quests.smoothness

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryDelete
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryModify
import de.westnordost.streetcomplete.osm.toCheckDateString
import de.westnordost.streetcomplete.quests.verifyAnswer
import de.westnordost.streetcomplete.testutils.way
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class AddRoadSmoothnessTest {
    private val questType = AddRoadSmoothness()

    @Test fun `apply smoothness answer`() {
        questType.verifyAnswer(
            SmoothnessValueAnswer(Smoothness.EXCELLENT),
            StringMapEntryAdd("smoothness", "excellent")
        )
    }

    @Test fun `deletes smoothness-date`() {
        questType.verifyAnswer(
            mapOf("smoothness" to "excellent", "smoothness:date" to "2000-10-10"),
            SmoothnessValueAnswer(Smoothness.EXCELLENT),
            StringMapEntryModify("smoothness", "excellent", "excellent"),
            StringMapEntryDelete("smoothness:date", "2000-10-10"),
            StringMapEntryAdd("check_date:smoothness", LocalDate.now().toCheckDateString()),
        )
    }

    @Test fun `deletes surface-grade`() {
        questType.verifyAnswer(
            mapOf("smoothness" to "excellent", "surface:grade" to "3"),
            SmoothnessValueAnswer(Smoothness.HORRIBLE),
            StringMapEntryModify("smoothness", "excellent", "horrible"),
            StringMapEntryDelete("surface:grade", "3"),
        )
    }

    @Test fun `on wrong surface, deletes everything smoothness-related`() {
        questType.verifyAnswer(
            mapOf(
                "smoothness" to "excellent",
                "smoothness:date" to "2000-10-10",
                "surface" to "asphalt",
                "check_date:smoothness" to "2000-10-10",
            ),
            WrongSurfaceAnswer,
            StringMapEntryDelete("smoothness", "excellent"),
            StringMapEntryDelete("smoothness:date", "2000-10-10"),
            StringMapEntryDelete("surface", "asphalt"),
            StringMapEntryDelete("check_date:smoothness", "2000-10-10"),
        )
    }

    @Test fun `applicable to old way tagged with smoothness-date`() {
        assertTrue(questType.isApplicableTo(way(tags = mapOf(
            "highway" to "residential",
            "surface" to "asphalt",
            "smoothness" to "excellent",
            "smoothness:date" to "2014-10-10"
        ))))
    }
}

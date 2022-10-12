package de.westnordost.streetcomplete.quests.width

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryDelete
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryModify
import de.westnordost.streetcomplete.osm.LengthInMeters
import de.westnordost.streetcomplete.quests.verifyAnswer
import de.westnordost.streetcomplete.testutils.mock
import org.junit.Test

class AddRoadWidthTest {
    private val quest = AddRoadWidth(mock())

    @Test fun `apply to street`() {
        quest.verifyAnswer(
            WidthAnswer(LengthInMeters(3.0), false),
            StringMapEntryAdd("width", "3")
        )
    }

    @Test fun `apply to street when measured with AR`() {
        quest.verifyAnswer(
            WidthAnswer(LengthInMeters(3.0), true),
            StringMapEntryAdd("width", "3"),
            StringMapEntryAdd("source:width", "ARCore")
        )
    }

    @Test fun `apply to street when not measured with AR but previously was`() {
        quest.verifyAnswer(
            mapOf("width" to "2", "source:width" to "estimate"),
            WidthAnswer(LengthInMeters(3.0), false),
            StringMapEntryModify("width", "2", "3"),
            StringMapEntryDelete("source:width", "estimate")
        )
    }

    @Test fun `modifies width-carriageway if set`() {
        quest.verifyAnswer(
            mapOf("width:carriageway" to "2"),
            WidthAnswer(LengthInMeters(3.0), false),
            StringMapEntryAdd("width", "3"),
            StringMapEntryModify("width:carriageway", "2", "3"),
        )
    }

    @Test fun `apply to choker`() {
        quest.verifyAnswer(
            mapOf("traffic_calming" to "choker"),
            WidthAnswer(LengthInMeters(3.0), false),
            StringMapEntryAdd("maxwidth", "3")
        )
    }

    @Test fun `apply to choker when measured with AR`() {
        quest.verifyAnswer(
            mapOf("traffic_calming" to "choker"),
            WidthAnswer(LengthInMeters(3.0), true),
            StringMapEntryAdd("maxwidth", "3"),
            StringMapEntryAdd("source:maxwidth", "ARCore")
        )
    }

    @Test fun `apply to choker when not measured with AR but previously was`() {
        quest.verifyAnswer(
            mapOf(
                "traffic_calming" to "choker",
                "maxwidth" to "2",
                "source:maxwidth" to "estimate"
            ),
            WidthAnswer(LengthInMeters(3.0), false),
            StringMapEntryModify("maxwidth", "2", "3"),
            StringMapEntryDelete("source:maxwidth", "estimate")
        )
    }
}

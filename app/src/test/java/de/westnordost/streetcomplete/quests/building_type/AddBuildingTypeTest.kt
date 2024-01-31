package de.westnordost.streetcomplete.quests.building_type

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryDelete
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryModify
import de.westnordost.streetcomplete.quests.answerAppliedTo
import kotlin.test.Test
import kotlin.test.assertEquals

class AddBuildingTypeTest {
    private val questType = AddBuildingType()

    @Test fun `set building as residential`() {
        assertEquals(
            setOf(StringMapEntryModify("building", "yes", "residential")),
            questType.answerAppliedTo(BuildingType.RESIDENTIAL, mapOf("building" to "yes"))
        )
    }

    @Test fun `set building as abandoned`() {
        assertEquals(
            setOf(StringMapEntryAdd("abandoned", "yes")),
            questType.answerAppliedTo(BuildingType.ABANDONED, mapOf("building" to "yes"))
        )
    }

    @Test fun `set building as abandoned and prevent double tagging`() {
        // https://github.com/streetcomplete/StreetComplete/issues/3386
        assertEquals(
            setOf(
                StringMapEntryAdd("abandoned", "yes"),
                StringMapEntryDelete("disused", "yes"),
            ),
            questType.answerAppliedTo(BuildingType.ABANDONED, mapOf(
                "building" to "yes",
                "disused" to "yes",
            ))
        )
    }

    @Test fun `set building as abandoned where it was marked as used`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("abandoned", "yes"),
                StringMapEntryDelete("disused", "no"),
            ),
            questType.answerAppliedTo(BuildingType.ABANDONED, mapOf(
                "building" to "yes",
                "disused" to "no",
            ))
        )
    }
}

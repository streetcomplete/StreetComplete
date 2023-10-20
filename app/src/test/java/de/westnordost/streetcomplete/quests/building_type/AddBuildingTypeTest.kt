package de.westnordost.streetcomplete.quests.building_type

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryDelete
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryModify
import de.westnordost.streetcomplete.quests.verifyAnswer
import kotlin.test.Test

class AddBuildingTypeTest {
    private val questType = AddBuildingType()

    @Test
    fun `set building as residential`() {
        questType.verifyAnswer(
            mapOf(
                "building" to "yes",
            ),
            BuildingType.RESIDENTIAL,
            StringMapEntryModify("building", "yes", "residential"),
        )
    }

    @Test
    fun `set building as abandoned`() {
        questType.verifyAnswer(
            mapOf(
                "building" to "yes",
            ),
            BuildingType.ABANDONED,
            StringMapEntryAdd("abandoned", "yes"),
        )
    }

    @Test
    fun `set building as abandoned and prevent double tagging`() {
        // https://github.com/streetcomplete/StreetComplete/issues/3386
        questType.verifyAnswer(
            mapOf(
                "building" to "yes",
                "disused" to "yes",
            ),
            BuildingType.ABANDONED,
            StringMapEntryAdd("abandoned", "yes"),
            StringMapEntryDelete("disused", "yes"),
        )
    }

    @Test
    fun `set building as abandoned where it was marked as used`() {
        questType.verifyAnswer(
            mapOf(
                "building" to "yes",
                "disused" to "no",
            ),
            BuildingType.ABANDONED,
            StringMapEntryAdd("abandoned", "yes"),
            StringMapEntryDelete("disused", "no"),
        )
    }
}

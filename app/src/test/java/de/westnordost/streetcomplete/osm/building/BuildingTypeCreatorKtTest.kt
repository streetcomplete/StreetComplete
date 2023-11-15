package de.westnordost.streetcomplete.osm.building

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryDelete
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryModify
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryChange
import org.assertj.core.api.Assertions
import kotlin.test.Test

class BuildingTypeCreatorKtTest {

    @Test
    fun `set building as residential`() {
        verifyAnswer(
            mapOf(
                "building" to "yes",
            ),
            BuildingType.RESIDENTIAL,
            arrayOf(
                StringMapEntryModify("building", "yes", "residential"),
            )
        )
    }

    @Test
    fun `set building as abandoned`() {
        verifyAnswer(
            mapOf(
                "building" to "yes",
            ),
            BuildingType.ABANDONED,
            arrayOf(
                StringMapEntryAdd("abandoned", "yes"),
            )
        )
    }

    @Test
    fun `set building as abandoned and prevent double tagging`() {
        // https://github.com/streetcomplete/StreetComplete/issues/3386
        verifyAnswer(
            mapOf(
                "building" to "yes",
                "disused" to "yes",
            ),
            BuildingType.ABANDONED,
            arrayOf(
                StringMapEntryAdd("abandoned", "yes"),
                StringMapEntryDelete("disused", "yes"),
            )
        )
    }

    @Test
    fun `set building as abandoned where it was marked as used`() {
        verifyAnswer(
            mapOf(
                "building" to "yes",
                "disused" to "no",
            ),
            BuildingType.ABANDONED,
            arrayOf(
                StringMapEntryAdd("abandoned", "yes"),
                StringMapEntryDelete("disused", "no"),
            )
        )
    }
}

private fun verifyAnswer(tags: Map<String, String>, answer: BuildingType, expectedChanges: Array<StringMapEntryChange>) {
    val cb = StringMapChangesBuilder(tags)
    answer.applyTo(cb)
    val changes = cb.create().changes
    Assertions.assertThat(changes).containsExactlyInAnyOrder(*expectedChanges)
}

package de.westnordost.streetcomplete.quests.recycling

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.quests.answerApplied
import kotlin.test.Test
import kotlin.test.assertEquals

class AddRecyclingTypeTest {

    private val questType = AddRecyclingType()

    @Test fun `apply recycling centre answer`() {
        assertEquals(
            setOf(StringMapEntryAdd("recycling_type", "centre")),
            questType.answerApplied(RecyclingType.RECYCLING_CENTRE)
        )
    }

    @Test fun `apply underground recycling container answer`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("recycling_type", "container"),
                StringMapEntryAdd("location", "underground")
            ),
            questType.answerApplied(RecyclingType.UNDERGROUND_CONTAINER)
        )
    }

    @Test fun `apply overground recycling container answer`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("recycling_type", "container"),
                StringMapEntryAdd("location", "overground")
            ),
            questType.answerApplied(RecyclingType.OVERGROUND_CONTAINER)
        )
    }
}

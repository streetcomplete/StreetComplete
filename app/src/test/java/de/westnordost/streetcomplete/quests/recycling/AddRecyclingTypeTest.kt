package de.westnordost.streetcomplete.quests.recycling

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.quests.verifyAnswer
import kotlin.test.Test

class AddRecyclingTypeTest {

    private val questType = AddRecyclingType()

    @Test fun `apply recycling centre answer`() {
        questType.verifyAnswer(
            RecyclingType.RECYCLING_CENTRE,
            StringMapEntryAdd("recycling_type", "centre")
        )
    }

    @Test fun `apply underground recycling container answer`() {
        questType.verifyAnswer(
            RecyclingType.UNDERGROUND_CONTAINER,
            StringMapEntryAdd("recycling_type", "container"),
            StringMapEntryAdd("location", "underground")
        )
    }

    @Test fun `apply overground recycling container answer`() {
        questType.verifyAnswer(
            RecyclingType.OVERGROUND_CONTAINER,
            StringMapEntryAdd("recycling_type", "container"),
            StringMapEntryAdd("location", "overground")
        )
    }
}

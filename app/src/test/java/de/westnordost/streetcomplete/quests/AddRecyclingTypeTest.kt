package de.westnordost.streetcomplete.quests


import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryAdd
import de.westnordost.streetcomplete.mock
import de.westnordost.streetcomplete.quests.recycling.AddRecyclingType
import de.westnordost.streetcomplete.quests.recycling.RecyclingType
import org.junit.Test

class AddRecyclingTypeTest  {

    private val questType = AddRecyclingType(mock())

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

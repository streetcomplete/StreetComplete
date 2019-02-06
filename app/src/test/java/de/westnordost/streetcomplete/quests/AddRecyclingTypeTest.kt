package de.westnordost.streetcomplete.quests


import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao
import de.westnordost.streetcomplete.quests.recycling.AddRecyclingType
import de.westnordost.streetcomplete.quests.recycling.RecyclingType
import org.junit.Test

import org.mockito.Mockito.mock

class AddRecyclingTypeTest  {

    private val questType = AddRecyclingType(mock(OverpassMapDataDao::class.java))

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

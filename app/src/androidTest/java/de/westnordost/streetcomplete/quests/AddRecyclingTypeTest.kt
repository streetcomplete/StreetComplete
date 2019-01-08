package de.westnordost.streetcomplete.quests


import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao
import de.westnordost.streetcomplete.quests.recycling.AddRecyclingType
import org.junit.Test

import org.mockito.Mockito.mock

class AddRecyclingTypeTest : AOsmElementQuestTypeTest() {

    override val questType = AddRecyclingType(mock(OverpassMapDataDao::class.java))

    @Test fun recyclingCentre() {
        bundle.putStringArrayList(ImageListQuestAnswerFragment.OSM_VALUES, arrayListOf("centre"))
        verify(StringMapEntryAdd("recycling_type", "centre"))
    }

    @Test fun recyclingUndergroundContainer() {
        bundle.putStringArrayList(ImageListQuestAnswerFragment.OSM_VALUES, arrayListOf("underground"))
        verify(StringMapEntryAdd("recycling_type", "container"))
        verify(StringMapEntryAdd("location", "underground"))
    }

    @Test fun recyclingOvergroundContainer() {
        bundle.putStringArrayList(ImageListQuestAnswerFragment.OSM_VALUES, arrayListOf("overground"))
        verify(StringMapEntryAdd("recycling_type", "container"))
        verify(StringMapEntryAdd("location", "overground"))
    }
}

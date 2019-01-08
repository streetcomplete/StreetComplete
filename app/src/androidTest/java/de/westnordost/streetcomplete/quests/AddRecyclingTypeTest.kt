package de.westnordost.streetcomplete.quests

import java.util.ArrayList

import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao
import de.westnordost.streetcomplete.quests.recycling.AddRecyclingType
import org.junit.Before
import org.junit.Test

import org.mockito.Mockito.mock

class AddRecyclingTypeTest : AOsmElementQuestTypeTest() {

    override val questType = AddRecyclingType(mock(OverpassMapDataDao::class.java))

    @Test fun recyclingCentre() {
        bundle.putStringArrayList(ImageListQuestAnswerFragment.OSM_VALUES, "centre".toArrayList())
        verify(StringMapEntryAdd("recycling_type", "centre"))
    }

    @Test fun recyclingUndergroundContainer() {
        bundle.putStringArrayList(ImageListQuestAnswerFragment.OSM_VALUES, "underground".toArrayList())
        verify(StringMapEntryAdd("recycling_type", "container"))
        verify(StringMapEntryAdd("location", "underground"))
    }

    @Test fun recyclingOvergroundContainer() {
        bundle.putStringArrayList(ImageListQuestAnswerFragment.OSM_VALUES, "overground".toArrayList())
        verify(StringMapEntryAdd("recycling_type", "container"))
        verify(StringMapEntryAdd("location", "overground"))
    }

    private fun String.toArrayList(): ArrayList<String> {
        val values = ArrayList<String>()
        values.add(this)
        return values
    }
}

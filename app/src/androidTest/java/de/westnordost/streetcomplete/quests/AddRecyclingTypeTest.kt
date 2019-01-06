package de.westnordost.streetcomplete.quests

import java.util.ArrayList

import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao
import de.westnordost.streetcomplete.quests.recycling.AddRecyclingType

import org.mockito.Mockito.mock

class AddRecyclingTypeTest : AOsmElementQuestTypeTest() {

    override val questType = AddRecyclingType(mock(OverpassMapDataDao::class.java))

    override fun setUp() {
        super.setUp()
        tags["amenity"] = "recycling"
    }

    fun testRecyclingCentre() {
        bundle.putStringArrayList(ImageListQuestAnswerFragment.OSM_VALUES, "centre".toArrayList())
        verify(StringMapEntryAdd("recycling_type", "centre"))
    }

    fun testRecyclingUndergroundContainer() {
        bundle.putStringArrayList(ImageListQuestAnswerFragment.OSM_VALUES, "underground".toArrayList())
        verify(StringMapEntryAdd("recycling_type", "container"))
        verify(StringMapEntryAdd("location", "underground"))
    }

    fun testRecyclingOvergroundContainer() {
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

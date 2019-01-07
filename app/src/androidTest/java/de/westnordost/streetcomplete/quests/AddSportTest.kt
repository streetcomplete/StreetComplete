package de.westnordost.streetcomplete.quests

import java.util.ArrayList

import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryModify
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao
import de.westnordost.streetcomplete.quests.sport.AddSport

import org.mockito.Mockito.mock

class AddSportTest : AOsmElementQuestTypeTest() {

    override val questType = AddSport(mock(OverpassMapDataDao::class.java))

    override fun setUp() {
        super.setUp()
        tags["leisure"] = "pitch"
    }

    fun testReplaceHockey() {
        tags["sport"] = "hockey"
        bundle.putStringArrayList(ImageListQuestAnswerFragment.OSM_VALUES, "field_hockey".toArrayList())
        verify(StringMapEntryModify("sport", "hockey", "field_hockey"))
    }

    fun testReplaceTeamHandball() {
        tags["sport"] = "team_handball"
        bundle.putStringArrayList(ImageListQuestAnswerFragment.OSM_VALUES, "handball".toArrayList())
        verify(StringMapEntryModify("sport", "team_handball", "handball"))
    }

    fun testAddSport() {
        bundle.putStringArrayList(ImageListQuestAnswerFragment.OSM_VALUES, "soccer".toArrayList())
        verify(StringMapEntryAdd("sport", "soccer"))
    }

    private fun String.toArrayList(): ArrayList<String> {
        val values = ArrayList<String>()
        values.add(this)
        return values
    }
}

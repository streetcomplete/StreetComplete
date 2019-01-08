package de.westnordost.streetcomplete.quests

import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryModify
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao
import de.westnordost.streetcomplete.quests.sport.AddSport
import org.junit.Test

import org.mockito.Mockito.mock

class AddSportTest : AOsmElementQuestTypeTest() {

    override val questType = AddSport(mock(OverpassMapDataDao::class.java))

    @Test fun replaceHockey() {
        tags["sport"] = "hockey"
        bundle.putStringArrayList(ImageListQuestAnswerFragment.OSM_VALUES, arrayListOf("field_hockey"))
        verify(StringMapEntryModify("sport", "hockey", "field_hockey"))
    }

    @Test fun replaceTeamHandball() {
        tags["sport"] = "team_handball"
        bundle.putStringArrayList(ImageListQuestAnswerFragment.OSM_VALUES, arrayListOf("handball"))
        verify(StringMapEntryModify("sport", "team_handball", "handball"))
    }

    @Test fun replaceSkating() {
        tags["sport"] = "skating"
        bundle.putStringArrayList(ImageListQuestAnswerFragment.OSM_VALUES, arrayListOf("ice_skating"))
        verify(StringMapEntryModify("sport", "skating", "ice_skating"))
    }

    @Test fun replaceFootball() {
        tags["sport"] = "football"
        bundle.putStringArrayList(ImageListQuestAnswerFragment.OSM_VALUES, arrayListOf("soccer"))
        verify(StringMapEntryModify("sport", "football", "soccer"))
    }

    @Test fun addSport() {
        bundle.putStringArrayList(ImageListQuestAnswerFragment.OSM_VALUES, arrayListOf("soccer"))
        verify(StringMapEntryAdd("sport", "soccer"))
    }
}

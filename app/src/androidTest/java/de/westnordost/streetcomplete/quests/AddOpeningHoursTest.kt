package de.westnordost.streetcomplete.quests

import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao
import de.westnordost.streetcomplete.quests.opening_hours.AddOpeningHours
import de.westnordost.streetcomplete.quests.opening_hours.AddOpeningHoursForm
import org.junit.Test

import org.mockito.Mockito.mock

class AddOpeningHoursTest : AOsmElementQuestTypeTest() {

    override val questType = AddOpeningHours(mock(OverpassMapDataDao::class.java))

    @Test fun openingHours() {
        bundle.putString(AddOpeningHoursForm.OPENING_HOURS, "my cool opening hours")
        verify(StringMapEntryAdd("opening_hours", "my cool opening hours"))
    }

    @Test fun noOpeningHoursSign() {
        bundle.putBoolean(AddOpeningHoursForm.NO_SIGN, true)
        verify(StringMapEntryAdd("opening_hours:signed", "no"))
    }
}

package de.westnordost.streetcomplete.quests

import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao
import de.westnordost.streetcomplete.quests.opening_hours.AddOpeningHours
import de.westnordost.streetcomplete.quests.opening_hours.AddOpeningHoursForm

import org.mockito.Mockito.mock

class AddOpeningHoursTest : AOsmElementQuestTypeTest() {
    override val questType = AddOpeningHours(mock(OverpassMapDataDao::class.java))

    fun testOpeningHours() {
        bundle.putString(AddOpeningHoursForm.OPENING_HOURS, "my cool opening hours")
        verify(StringMapEntryAdd("opening_hours", "my cool opening hours"))
    }

    fun testNoOpeningHoursSign() {
        bundle.putBoolean(AddOpeningHoursForm.NO_SIGN, true)
        verify(StringMapEntryAdd("opening_hours:signed", "no"))
    }
}

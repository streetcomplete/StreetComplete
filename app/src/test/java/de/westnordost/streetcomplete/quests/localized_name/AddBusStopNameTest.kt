package de.westnordost.streetcomplete.quests.localized_name

import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao
import de.westnordost.streetcomplete.quests.verifyAnswer
import org.junit.Test
import org.mockito.Mockito.mock

class AddBusStopNameTest {

    private val questType = AddBusStopName(mock(OverpassMapDataDao::class.java))

    @Test
    fun `apply no name answer`() {
        questType.verifyAnswer(
            NoBusStopName,
            StringMapEntryAdd("noname", "yes")
        )
    }

    @Test fun `apply name answer with one name`() {
        questType.verifyAnswer(
            BusStopName(listOf(LocalizedName("", "my name"))),
            StringMapEntryAdd("name", "my name")
        )
    }

    @Test fun `apply name answer with multiple names`() {
        questType.verifyAnswer(
            BusStopName(listOf(
                LocalizedName("", "Altona / All-Too-Close"),
                LocalizedName("de", "Altona"),
                LocalizedName("en", "All-Too-Close")
            )),
            StringMapEntryAdd("name", "Altona / All-Too-Close"),
            StringMapEntryAdd("name:en", "All-Too-Close"),
            StringMapEntryAdd("name:de", "Altona")
        )
    }
}

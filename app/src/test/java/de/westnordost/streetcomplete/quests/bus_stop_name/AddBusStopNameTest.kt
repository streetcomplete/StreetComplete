package de.westnordost.streetcomplete.quests.bus_stop_name

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.osm.LocalizedName
import de.westnordost.streetcomplete.quests.answerApplied
import kotlin.test.Test
import kotlin.test.assertEquals

class AddBusStopNameTest {

    private val questType = AddBusStopName()

    @Test fun `apply no name answer`() {
        assertEquals(
            setOf(StringMapEntryAdd("name:signed", "no")),
            questType.answerApplied(NoBusStopName)
        )
    }

    @Test fun `apply name answer with one name`() {
        assertEquals(
            setOf(StringMapEntryAdd("name", "my name")),
            questType.answerApplied(BusStopName(listOf(LocalizedName("", "my name"))))
        )
    }

    @Test fun `apply name answer with multiple names`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("name", "Altona / All-Too-Close"),
                StringMapEntryAdd("name:en", "All-Too-Close"),
                StringMapEntryAdd("name:de", "Altona")
            ),
            questType.answerApplied(BusStopName(listOf(
                LocalizedName("", "Altona / All-Too-Close"),
                LocalizedName("de", "Altona"),
                LocalizedName("en", "All-Too-Close")
            )))
        )
    }
}

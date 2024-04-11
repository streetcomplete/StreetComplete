package de.westnordost.streetcomplete.quests.place_name

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.osm.LocalizedName
import de.westnordost.streetcomplete.quests.answerApplied
import de.westnordost.streetcomplete.testutils.mock
import kotlin.test.Test
import kotlin.test.assertEquals

class AddPlaceNameTest {

    private val questType = AddPlaceName(mock())

    @Test fun `apply no name answer`() {
        assertEquals(
            setOf(StringMapEntryAdd("name:signed", "no")),
            questType.answerApplied(NoPlaceNameSign)
        )
    }

    @Test fun `apply name answer`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("name", "Hey ya!"),
                StringMapEntryAdd("name:de", "He ja!")
            ),
            questType.answerApplied(PlaceName(listOf(
                LocalizedName("", "Hey ya!"),
                LocalizedName("de", "He ja!"),
            )))
        )
    }
}

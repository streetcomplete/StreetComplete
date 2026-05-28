package de.westnordost.streetcomplete.quests.place_name

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.osm.localized_name.LocalizedName
import de.westnordost.streetcomplete.quests.answerApplied
import de.westnordost.streetcomplete.testutils.feature
import kotlin.test.Test
import kotlin.test.assertEquals

class AddPlaceNameTest {

    private val questType = AddPlaceName(
        getFeature = { feature() }
    )

    @Test fun `apply no name answer`() {
        assertEquals(
            setOf(StringMapEntryAdd("name:signed", "no")),
            questType.answerApplied(PlaceNameAnswer.NoNameSign)
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

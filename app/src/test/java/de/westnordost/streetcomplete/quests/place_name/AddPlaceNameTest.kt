package de.westnordost.streetcomplete.quests.place_name

import de.westnordost.osmapi.map.data.Element
import de.westnordost.osmfeatures.Feature
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.osm.LocalizedName
import de.westnordost.streetcomplete.quests.answerApplied
import io.mockative.Mock
import io.mockative.classOf
import io.mockative.mock
import kotlin.test.Test
import kotlin.test.assertEquals

class AddPlaceNameTest {
    @Mock
    private val feature: Feature = mock(classOf<Feature>())

    private val questType = AddPlaceName { _ -> feature }

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

package de.westnordost.streetcomplete.quests.place_name

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.osm.LocalizedName
import de.westnordost.streetcomplete.quests.verifyAnswer
import de.westnordost.streetcomplete.testutils.mock
import kotlin.test.Test

class AddPlaceNameTest {

    private val questType = AddPlaceName(mock())

    @Test fun `apply no name answer`() {
        questType.verifyAnswer(
            NoPlaceNameSign,
            StringMapEntryAdd("name:signed", "no")
        )
    }

    @Test fun `apply name answer`() {
        questType.verifyAnswer(
            PlaceName(listOf(
                LocalizedName("", "Hey ya!"),
                LocalizedName("de", "He ja!"),
            )),
            StringMapEntryAdd("name", "Hey ya!"),
            StringMapEntryAdd("name:de", "He ja!"),
        )
    }
}

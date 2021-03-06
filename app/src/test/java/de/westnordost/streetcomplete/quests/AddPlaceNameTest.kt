package de.westnordost.streetcomplete.quests

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryModify
import de.westnordost.streetcomplete.testutils.mock
import de.westnordost.streetcomplete.quests.place_name.AddPlaceName
import de.westnordost.streetcomplete.quests.place_name.BrandFeature
import de.westnordost.streetcomplete.quests.place_name.NoPlaceNameSign
import de.westnordost.streetcomplete.quests.place_name.PlaceName
import org.junit.Test

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
            PlaceName("Hey ya!"),
            StringMapEntryAdd("name", "Hey ya!")
        )
    }

    @Test fun `apply brand answer`() {
        questType.verifyAnswer(
            mapOf("a" to "b"),
            BrandFeature(mapOf("a" to "b", "c" to "d")),
            StringMapEntryAdd("c", "d"),
            StringMapEntryModify("a", "b", "b"),
        )
    }
}

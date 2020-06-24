package de.westnordost.streetcomplete.quests

import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryAdd
import de.westnordost.streetcomplete.mock
import de.westnordost.streetcomplete.quests.place_name.AddPlaceName
import de.westnordost.streetcomplete.quests.place_name.NoPlaceNameSign
import de.westnordost.streetcomplete.quests.place_name.PlaceName
import org.junit.Test

class AddPlaceNameTest {

    private val questType = AddPlaceName(mock(), mock())

    @Test fun `apply no name answer`() {
        questType.verifyAnswer(
            NoPlaceNameSign,
            StringMapEntryAdd("noname", "yes")
        )
    }

    @Test fun `apply name answer`() {
        questType.verifyAnswer(
            PlaceName("Hey ya!"),
            StringMapEntryAdd("name", "Hey ya!")
        )
    }

}

package de.westnordost.streetcomplete.quests

import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryModify
import de.westnordost.streetcomplete.mock
import de.westnordost.streetcomplete.quests.crossing_type.AddCrossingType
import org.junit.Test

class AddCrossingTypeTest {

    private val questType = AddCrossingType(mock())

    @Test fun `apply normal answer`() {
        questType.verifyAnswer(
            "bla",
            StringMapEntryAdd("crossing", "bla")
        )
    }

    @Test fun `apply answer for crossing = island`() {
        questType.verifyAnswer(
            mapOf("crossing" to "island"),
            "blub",
            StringMapEntryModify("crossing", "island", "blub"),
            StringMapEntryAdd("crossing:island", "yes")
        )
    }

    @Test fun `apply answer for crossing = island and crossing_island set`() {
        questType.verifyAnswer(
            mapOf("crossing" to "island", "crossing:island" to "something"),
            "blub",
            StringMapEntryModify("crossing", "island", "blub"),
            StringMapEntryModify("crossing:island", "something", "yes")
        )
    }
}

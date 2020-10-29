package de.westnordost.streetcomplete.quests

import de.westnordost.streetcomplete.data.meta.toCheckDateString
import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryModify
import de.westnordost.streetcomplete.quests.crossing_type.AddCrossingType
import org.junit.Test
import java.util.*

class AddCrossingTypeTest {

    private val questType = AddCrossingType()

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

    @Test fun `apply marked answer does not change the type of marked value`() {
        questType.verifyAnswer(
            mapOf("crossing" to "zebra"),
            "uncontrolled",
            StringMapEntryAdd("check_date:crossing", Date().toCheckDateString())
        )

        questType.verifyAnswer(
            mapOf("crossing" to "marked"),
            "uncontrolled",
            StringMapEntryAdd("check_date:crossing", Date().toCheckDateString())
        )

        questType.verifyAnswer(
            mapOf("crossing" to "uncontrolled"),
            "uncontrolled",
            StringMapEntryAdd("check_date:crossing", Date().toCheckDateString())
        )

        questType.verifyAnswer(
            mapOf("crossing" to "unmarked"),
            "unmarked",
            StringMapEntryAdd("check_date:crossing", Date().toCheckDateString())
        )
    }
}

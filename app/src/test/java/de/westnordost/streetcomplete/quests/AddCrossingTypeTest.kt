package de.westnordost.streetcomplete.quests

import de.westnordost.streetcomplete.data.meta.toCheckDateString
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryModify
import de.westnordost.streetcomplete.quests.crossing_type.AddCrossingType
import de.westnordost.streetcomplete.quests.crossing_type.CrossingType.*
import org.junit.Test
import java.time.LocalDate

class AddCrossingTypeTest {

    private val questType = AddCrossingType()

    @Test fun `apply normal answer`() {
        questType.verifyAnswer(
            TRAFFIC_SIGNALS,
            StringMapEntryAdd("crossing", "traffic_signals")
        )
    }

    @Test fun `apply answer for crossing = island`() {
        questType.verifyAnswer(
            mapOf("crossing" to "island"),
            TRAFFIC_SIGNALS,
            StringMapEntryModify("crossing", "island", "traffic_signals"),
            StringMapEntryAdd("crossing:island", "yes")
        )
    }

    @Test fun `apply answer for crossing = island and crossing_island set`() {
        questType.verifyAnswer(
            mapOf("crossing" to "island", "crossing:island" to "something"),
            TRAFFIC_SIGNALS,
            StringMapEntryModify("crossing", "island", "traffic_signals"),
            StringMapEntryModify("crossing:island", "something", "yes")
        )
    }

    @Test fun `apply marked answer does not change the type of marked value`() {
        questType.verifyAnswer(
            mapOf("crossing" to "zebra"),
            MARKED,
            StringMapEntryAdd("check_date:crossing", LocalDate.now().toCheckDateString())
        )

        questType.verifyAnswer(
            mapOf("crossing" to "marked"),
            MARKED,
            StringMapEntryAdd("check_date:crossing", LocalDate.now().toCheckDateString())
        )

        questType.verifyAnswer(
            mapOf("crossing" to "uncontrolled"),
            MARKED,
            StringMapEntryAdd("check_date:crossing", LocalDate.now().toCheckDateString())
        )

        questType.verifyAnswer(
            mapOf("crossing" to "unmarked"),
            MARKED,
            StringMapEntryModify("crossing", "unmarked", "marked")
        )
    }
}

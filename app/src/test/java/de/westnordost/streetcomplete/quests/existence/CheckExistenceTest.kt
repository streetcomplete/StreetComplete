package de.westnordost.streetcomplete.quests.existence

import de.westnordost.streetcomplete.data.meta.toCheckDateString
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryDelete
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryModify
import de.westnordost.streetcomplete.testutils.mock
import de.westnordost.streetcomplete.quests.verifyAnswer
import org.junit.Test
import java.time.LocalDate

class CheckExistenceTest {
    private val questType = CheckExistence(mock())

    @Test fun `apply answer adds check date`() {
        questType.verifyAnswer(
            Unit,
            StringMapEntryAdd("check_date", LocalDate.now().toCheckDateString())
        )
    }

    @Test fun `apply answer removes all previous survey keys`() {
        questType.verifyAnswer(
            mapOf(
                "check_date" to "1",
                "lastcheck" to "a",
                "last_checked" to "b",
                "survey:date" to "c",
                "survey_date" to "d"
            ),
            Unit,
            StringMapEntryModify("check_date", "1", LocalDate.now().toCheckDateString()),
            StringMapEntryDelete("lastcheck", "a"),
            StringMapEntryDelete("last_checked", "b"),
            StringMapEntryDelete("survey:date", "c"),
            StringMapEntryDelete("survey_date", "d"),
        )
    }
}

package de.westnordost.streetcomplete.quests.opening_hours_signed

import de.westnordost.streetcomplete.data.meta.toCheckDateString
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryDelete
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryModify
import de.westnordost.streetcomplete.quests.verifyAnswer
import de.westnordost.streetcomplete.testutils.mock
import de.westnordost.streetcomplete.testutils.node
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class CheckOpeningHoursSignedTest {
    private val questType = CheckOpeningHoursSigned(mock())

    @Test fun `is applicable to old place`() {
        assertTrue(questType.isApplicableTo(node(timestamp = 0, tags = mapOf(
            "name" to "XYZ",
            "opening_hours:signed" to "no"
        ))))
    }

    @Test fun `is not applicable to new place`() {
        assertFalse(questType.isApplicableTo(node(tags = mapOf(
            "name" to "XYZ",
            "opening_hours:signed" to "no"
        ))))
    }

    @Test fun `is applicable to place with old check_date`() {
        assertTrue(questType.isApplicableTo(node(tags = mapOf(
            "name" to "XYZ",
            "check_date:opening_hours" to "2020-12-12",
            "opening_hours:signed" to "no"
        ))))
    }

    @Test fun `is not applicable to place with new check_date`() {
        assertFalse(questType.isApplicableTo(node(tags = mapOf(
            "name" to "XYZ",
            "check_date:opening_hours" to LocalDate.now().toCheckDateString(),
            "opening_hours:signed" to "no"
        ))))
    }

    @Test fun `apply yes answer with no prior check date`() {
        questType.verifyAnswer(
            mapOf("opening_hours:signed" to "no"),
            true,
            StringMapEntryDelete("opening_hours:signed", "no"),
            StringMapEntryAdd("check_date:opening_hours", "1970-01-01"),
        )
    }

    @Test fun `apply yes answer with prior check date`() {
        questType.verifyAnswer(
            mapOf(
                "opening_hours:signed" to "no",
                "check_date:opening_hours" to "2020-03-04"
            ),
            true,
            StringMapEntryDelete("opening_hours:signed", "no"),
        )
    }

    @Test fun `apply no answer`() {
        questType.verifyAnswer(
            mapOf("opening_hours:signed" to "no"),
            false,
            StringMapEntryModify("opening_hours:signed", "no", "no"),
            StringMapEntryAdd("check_date:opening_hours", LocalDate.now().toCheckDateString()),
        )
    }

    @Test fun `apply no answer with prior check date`() {
        questType.verifyAnswer(
            mapOf(
                "opening_hours:signed" to "no",
                "check_date:opening_hours" to "2020-03-04"
            ),
            false,
            StringMapEntryModify("opening_hours:signed", "no", "no"),
            StringMapEntryModify("check_date:opening_hours", "2020-03-04", LocalDate.now().toCheckDateString()),
        )
    }
}

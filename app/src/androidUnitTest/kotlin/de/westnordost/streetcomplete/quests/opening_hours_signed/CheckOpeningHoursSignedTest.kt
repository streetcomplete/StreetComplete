package de.westnordost.streetcomplete.quests.opening_hours_signed

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryDelete
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryModify
import de.westnordost.streetcomplete.osm.nowAsCheckDateString
import de.westnordost.streetcomplete.quests.answerAppliedTo
import de.westnordost.streetcomplete.testutils.mock
import de.westnordost.streetcomplete.testutils.node
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

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
            "check_date:opening_hours" to nowAsCheckDateString(),
            "opening_hours:signed" to "no"
        ))))
    }

    @Test fun `is applicable to old place with existing opening hours via other means`() {
        assertTrue(questType.isApplicableTo(node(timestamp = 0, tags = mapOf(
            "name" to "XYZ",
            "opening_hours" to "24/7",
            "opening_hours:signed" to "no"
        ))))
    }

    @Test fun `is not applicable to old place with signed hours`() {
        assertFalse(questType.isApplicableTo(node(timestamp = 0, tags = mapOf(
            "name" to "XYZ",
            "opening_hours:signed" to "yes"
        ))))
    }

    @Test fun `is not applicable to old place with signed hours with hours specified`() {
        assertFalse(questType.isApplicableTo(node(timestamp = 0, tags = mapOf(
            "name" to "XYZ",
            "opening_hours" to "Mo 10:00-12:00",
            "opening_hours:signed" to "yes"
        ))))
    }

    @Test fun `apply yes answer with no prior check date`() {
        assertEquals(
            setOf(
                StringMapEntryDelete("opening_hours:signed", "no"),
                StringMapEntryAdd("check_date:opening_hours", "1970-01-01")
            ),
            questType.answerAppliedTo(true, mapOf("opening_hours:signed" to "no"))
        )
    }

    @Test fun `apply yes answer with prior check date`() {
        assertEquals(
            setOf(StringMapEntryDelete("opening_hours:signed", "no")),
            questType.answerAppliedTo(
                true,
                mapOf(
                    "opening_hours:signed" to "no",
                    "check_date:opening_hours" to "2020-03-04"
                )
            )
        )
    }

    @Test fun `apply yes answer with no prior check date and existing opening hours`() {
        assertEquals(
            setOf(
                StringMapEntryDelete("opening_hours:signed", "no"),
                StringMapEntryAdd("check_date:opening_hours", "1970-01-01"),
            ),
            questType.answerAppliedTo(
                true,
                mapOf(
                    "opening_hours" to "my opening hours",
                    "opening_hours:signed" to "no"
                )
            )
        )
    }

    @Test fun `apply yes answer with prior check date and existing opening hours`() {
        assertEquals(
            setOf(StringMapEntryDelete("opening_hours:signed", "no")),
            questType.answerAppliedTo(
                true,
                mapOf(
                    "opening_hours" to "\"oh\"",
                    "opening_hours:signed" to "no",
                    "check_date:opening_hours" to "2020-03-04"
                ),
            )
        )
    }

    @Test fun `apply no answer`() {
        assertEquals(
            setOf(
                StringMapEntryModify("opening_hours:signed", "no", "no"),
                StringMapEntryAdd("check_date:opening_hours", nowAsCheckDateString()),
            ),
            questType.answerAppliedTo(
                false,
                mapOf("opening_hours:signed" to "no"),
            )
        )
    }

    @Test fun `apply no answer with prior check date`() {
        assertEquals(
            setOf(
                StringMapEntryModify("opening_hours:signed", "no", "no"),
                StringMapEntryModify("check_date:opening_hours", "2020-03-04", nowAsCheckDateString()),
            ),
            questType.answerAppliedTo(
                false,
                mapOf(
                    "opening_hours:signed" to "no",
                    "check_date:opening_hours" to "2020-03-04"
                )
            )
        )
    }

    @Test fun `apply no answer with existing opening hours`() {
        assertEquals(
            setOf(
                StringMapEntryModify("opening_hours:signed", "no", "no"),
                StringMapEntryAdd("check_date:opening_hours", nowAsCheckDateString()),
            ),
            questType.answerAppliedTo(
                false,
                mapOf(
                    "opening_hours" to "24/7",
                    "opening_hours:signed" to "no"
                )
            )
        )
    }

    @Test fun `apply no answer with prior check date and existing opening hours`() {
        assertEquals(
            setOf(
                StringMapEntryModify("opening_hours:signed", "no", "no"),
                StringMapEntryModify("check_date:opening_hours", "2020-03-04", nowAsCheckDateString()),
            ),
            questType.answerAppliedTo(
                false,
                mapOf(
                    "opening_hours" to "Mo 10:00-12:00",
                    "opening_hours:signed" to "no",
                    "check_date:opening_hours" to "2020-03-04"
                )
            )
        )
    }
}

package de.westnordost.streetcomplete.quests.camping

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryModify
import de.westnordost.streetcomplete.osm.nowAsCheckDateString
import de.westnordost.streetcomplete.quests.answerApplied
import de.westnordost.streetcomplete.quests.answerAppliedTo
import de.westnordost.streetcomplete.testutils.node
import de.westnordost.streetcomplete.util.ktx.nowAsEpochMilliseconds
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AddCampSiteFeeTest {
    private val questType = AddCampSiteFee()

    private val millisecondsForFiveYears = 5L * 365 * 24 * 60 * 60 * 1000

    @Test fun `applicable to basic camp site without fee`() {
        assertTrue(questType.isApplicableTo(node(tags = mapOf(
            "tourism" to "camp_site",
            "camp_site" to "basic"
        ))))
    }

    @Test fun `applicable to backcountry camp site without fee`() {
        assertTrue(questType.isApplicableTo(node(tags = mapOf(
            "tourism" to "camp_site",
            "backcountry" to "yes"
        ))))
    }

    @Test fun `not applicable to plain camp site`() {
        assertFalse(questType.isApplicableTo(node(tags = mapOf(
            "tourism" to "camp_site"
        ))))
    }

    @Test fun `not applicable to basic camp site that recently had a fee surveyed`() {
        assertFalse(questType.isApplicableTo(node(
            tags = mapOf(
                "tourism" to "camp_site",
                "camp_site" to "basic",
                "fee" to "yes"
            ),
            timestamp = nowAsEpochMilliseconds()
        )))
    }

    @Test fun `applicable to basic camp site whose fee survey is old enough`() {
        assertTrue(questType.isApplicableTo(node(
            tags = mapOf(
                "tourism" to "camp_site",
                "camp_site" to "basic",
                "fee" to "yes"
            ),
            timestamp = nowAsEpochMilliseconds() - millisecondsForFiveYears
        )))
    }

    @Test fun `not applicable to basic camp site with a conditional fee`() {
        assertFalse(questType.isApplicableTo(node(tags = mapOf(
            "tourism" to "camp_site",
            "camp_site" to "basic",
            "fee:conditional" to "no @ (Nov-Mar)"
        ))))
    }

    @Test fun `apply yes answer`() {
        assertEquals(
            setOf(StringMapEntryAdd("fee", "yes")),
            questType.answerApplied(true)
        )
    }

    @Test fun `apply no answer`() {
        assertEquals(
            setOf(StringMapEntryAdd("fee", "no")),
            questType.answerApplied(false)
        )
    }

    @Test fun `apply same answer again refreshes check date`() {
        assertEquals(
            setOf(
                StringMapEntryModify("fee", "yes", "yes"),
                StringMapEntryAdd("check_date:fee", nowAsCheckDateString())
            ),
            questType.answerAppliedTo(true, mapOf("fee" to "yes"))
        )
    }
}

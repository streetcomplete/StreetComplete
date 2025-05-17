package de.westnordost.streetcomplete.quests.parking_fee

import de.westnordost.osm_opening_hours.model.OpeningHours
import de.westnordost.osm_opening_hours.model.Rule
import de.westnordost.osm_opening_hours.model.TwentyFourSeven
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryChange
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryDelete
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryModify
import de.westnordost.streetcomplete.osm.nowAsCheckDateString
import kotlin.test.Test
import kotlin.test.assertEquals

class MaxStayTest {

    private val oneHour = MaxStayDuration(1.0, MaxStay.Unit.HOURS)
    private val oneHourStr = "1 hour"

    private val oh = OpeningHours(listOf(Rule(TwentyFourSeven)))
    private val ohStr = "24/7"

    @Test fun `MaxStayDuration to OSM value`() {
        assertEquals("1 minute", MaxStayDuration(1.0, MaxStay.Unit.MINUTES).toOsmValue())
        assertEquals("12 minutes", MaxStayDuration(12.0, MaxStay.Unit.MINUTES).toOsmValue())
        assertEquals("1 hour", MaxStayDuration(1.0, MaxStay.Unit.HOURS).toOsmValue())
        assertEquals("12 hours", MaxStayDuration(12.0, MaxStay.Unit.HOURS).toOsmValue())
        assertEquals("1 day", MaxStayDuration(1.0, MaxStay.Unit.DAYS).toOsmValue())
        assertEquals("12 days", MaxStayDuration(12.0, MaxStay.Unit.DAYS).toOsmValue())
    }

    @Test fun `apply NoMaxStay`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("maxstay", "no")
            ),
            NoMaxStay.appliedTo(mapOf())
        )

        assertEquals(
            setOf(
                StringMapEntryModify("maxstay", "no", "no"),
                StringMapEntryAdd("check_date:maxstay", nowAsCheckDateString())
            ),
            NoMaxStay.appliedTo(mapOf("maxstay" to "no"))
        )

        assertEquals(
            setOf(
                StringMapEntryAdd("maxstay", "no"),
                StringMapEntryDelete("maxstay:conditional", "no @ (24/7)")
            ),
            NoMaxStay.appliedTo(mapOf("maxstay:conditional" to "no @ (24/7)"))
        )
    }

    @Test fun `apply MaxStayDuration`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("maxstay", oneHourStr)
            ),
            oneHour.appliedTo(mapOf())
        )

        assertEquals(
            setOf(
                StringMapEntryModify("maxstay", oneHourStr, oneHourStr),
                StringMapEntryAdd("check_date:maxstay", nowAsCheckDateString())
            ),
            oneHour.appliedTo(mapOf("maxstay" to oneHourStr))
        )

        assertEquals(
            setOf(
                StringMapEntryAdd("maxstay", oneHourStr),
                StringMapEntryDelete("maxstay:conditional", "no @ (24/7)")
            ),
            oneHour.appliedTo(mapOf("maxstay:conditional" to "no @ (24/7)"))
        )
    }

    @Test fun `apply MaxStayAtHours`() {
        val maxstayAtHours = MaxStayAtHours(oneHour, oh)

        assertEquals(
            setOf(
                StringMapEntryAdd("maxstay", "no"),
                StringMapEntryAdd("maxstay:conditional", "$oneHourStr @ ($ohStr)"),
            ),
            maxstayAtHours.appliedTo(mapOf())
        )

        assertEquals(
            setOf(
                StringMapEntryModify("maxstay", "no", "no"),
                StringMapEntryModify("maxstay:conditional", "$oneHourStr @ ($ohStr)", "$oneHourStr @ ($ohStr)"),
                StringMapEntryAdd("check_date:maxstay", nowAsCheckDateString())
            ),
            maxstayAtHours.appliedTo(mapOf(
                "maxstay" to "no",
                "maxstay:conditional" to "$oneHourStr @ ($ohStr)"
            ))
        )
    }

    @Test fun `apply MaxStayExceptAtHours`() {
        val maxstayExceptAtHours = MaxStayExceptAtHours(oneHour, oh)

        assertEquals(
            setOf(
                StringMapEntryAdd("maxstay", oneHourStr),
                StringMapEntryAdd("maxstay:conditional", "no @ ($ohStr)"),
            ),
            maxstayExceptAtHours.appliedTo(mapOf())
        )

        assertEquals(
            setOf(
                StringMapEntryModify("maxstay", oneHourStr, oneHourStr),
                StringMapEntryModify("maxstay:conditional", "no @ ($ohStr)", "no @ ($ohStr)"),
                StringMapEntryAdd("check_date:maxstay", nowAsCheckDateString())
            ),
            maxstayExceptAtHours.appliedTo(mapOf(
                "maxstay" to oneHourStr,
                "maxstay:conditional" to "no @ ($ohStr)"
            ))
        )
    }
}

private fun MaxStay.appliedTo(tags: Map<String, String>): Set<StringMapEntryChange> {
    val cb = StringMapChangesBuilder(tags)
    applyTo(cb)
    return cb.create().changes
}

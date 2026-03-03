package de.westnordost.streetcomplete.osm.maxstay

import de.westnordost.osm_opening_hours.parser.toOpeningHours
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryChange
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryDelete
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryModify
import de.westnordost.streetcomplete.osm.duration.Duration
import de.westnordost.streetcomplete.osm.duration.DurationUnit
import de.westnordost.streetcomplete.osm.nowAsCheckDateString
import de.westnordost.streetcomplete.osm.opening_hours.toHierarchicOpeningHours
import de.westnordost.streetcomplete.osm.time_restriction.TimeRestriction
import kotlin.test.Test
import kotlin.test.assertEquals

class MaxStayTest {

    private val oneHour = Duration(1.0, DurationUnit.HOURS)
    private val oneHourStr = "1 hour"

    private val ohStr = "08:00-12:00"
    private val oh = ohStr.toOpeningHours().toHierarchicOpeningHours()!!

    @Test fun `apply max stay duration`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("maxstay", oneHourStr)
            ),
            MaxStay(oneHour).appliedTo(mapOf())
        )

        assertEquals(
            setOf(
                StringMapEntryModify("maxstay", oneHourStr, oneHourStr),
                StringMapEntryAdd("check_date:maxstay", nowAsCheckDateString())
            ),
            MaxStay(oneHour).appliedTo(mapOf("maxstay" to oneHourStr))
        )

        assertEquals(
            setOf(
                StringMapEntryAdd("maxstay", oneHourStr),
                StringMapEntryDelete("maxstay:conditional", "no @ (24/7)")
            ),
            MaxStay(oneHour).appliedTo(mapOf("maxstay:conditional" to "no @ (24/7)"))
        )
    }

    @Test fun `apply max stay duration only at hours`() {
        val maxstayAtHours = MaxStay(oneHour, TimeRestriction(oh, TimeRestriction.Mode.ONLY_AT_HOURS))

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
        val maxstayExceptAtHours = MaxStay(oneHour, TimeRestriction(oh, TimeRestriction.Mode.EXCEPT_AT_HOURS))

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

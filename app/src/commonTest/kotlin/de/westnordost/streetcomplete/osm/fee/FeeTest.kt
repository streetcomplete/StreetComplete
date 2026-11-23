package de.westnordost.streetcomplete.osm.fee

import de.westnordost.osm_opening_hours.parser.toOpeningHours
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryChange
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryDelete
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryModify
import de.westnordost.streetcomplete.osm.nowAsCheckDateString
import de.westnordost.streetcomplete.osm.opening_hours.toHierarchicOpeningHours
import de.westnordost.streetcomplete.osm.time_restriction.TimeRestriction
import kotlin.test.Test
import kotlin.test.assertEquals

class FeeTest {
    private val ohStr = "08:00-12:00"
    private val oh = ohStr.toOpeningHours().toHierarchicOpeningHours()!!

    @Test fun `apply no fee`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("fee", "no"),
                StringMapEntryAdd("check_date:fee", nowAsCheckDateString())
            ),
            Fee.No.appliedTo(mapOf())
        )

        assertEquals(
            setOf(
                StringMapEntryAdd("fee", "no"),
                StringMapEntryAdd("check_date:fee", nowAsCheckDateString()),
                StringMapEntryDelete("fee:conditional", "no @ (24/7)")
            ),
            Fee.No.appliedTo(mapOf("fee:conditional" to "no @ (24/7)"))
        )
    }

    @Test fun `apply with fee`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("fee", "yes"),
                StringMapEntryAdd("check_date:fee", nowAsCheckDateString())
            ),
            Fee.Yes().appliedTo(mapOf())
        )

        assertEquals(
            setOf(
                StringMapEntryAdd("fee", "yes"),
                StringMapEntryAdd("check_date:fee", nowAsCheckDateString()),
                StringMapEntryDelete("fee:conditional", "no @ (24/7)")
            ),
            Fee.Yes().appliedTo(mapOf("fee:conditional" to "no @ (24/7)"))
        )
    }

    @Test fun `apply fee at hours`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("fee", "no"),
                StringMapEntryAdd("fee:conditional", "yes @ ($ohStr)"),
                StringMapEntryAdd("check_date:fee", nowAsCheckDateString())
            ),
            Fee.Yes(TimeRestriction(oh, TimeRestriction.Mode.ONLY_AT_HOURS)).appliedTo(mapOf())
        )
    }

    @Test fun `apply fee except at hours`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("fee", "yes"),
                StringMapEntryAdd("fee:conditional", "no @ ($ohStr)"),
                StringMapEntryAdd("check_date:fee", nowAsCheckDateString())
            ),
            Fee.Yes(TimeRestriction(oh, TimeRestriction.Mode.EXCEPT_AT_HOURS)).appliedTo(mapOf())
        )
    }
}

private fun Fee.appliedTo(tags: Map<String, String>): Set<StringMapEntryChange> {
    val cb = StringMapChangesBuilder(tags)
    applyTo(cb)
    return cb.create().changes
}

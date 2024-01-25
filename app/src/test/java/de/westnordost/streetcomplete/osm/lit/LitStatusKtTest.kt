package de.westnordost.streetcomplete.osm.lit

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryChange
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryModify
import de.westnordost.streetcomplete.osm.lit.LitStatus.*
import de.westnordost.streetcomplete.osm.nowAsCheckDateString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class LitStatusKtTest {

    @Test fun `apply normally`() {
        assertEquals(setOf(StringMapEntryAdd("lit", "yes")), YES.appliedTo(mapOf()))
        assertEquals(setOf(StringMapEntryAdd("lit", "no")), NO.appliedTo(mapOf()))
        assertEquals(setOf(StringMapEntryAdd("lit", "automatic")), AUTOMATIC.appliedTo(mapOf()))
        assertEquals(setOf(StringMapEntryAdd("lit", "24/7")), NIGHT_AND_DAY.appliedTo(mapOf()))
    }

    @Test fun `applying invalid throws exception`() {
        assertFailsWith<IllegalArgumentException> {
            UNSUPPORTED.applyTo(StringMapChangesBuilder(mapOf()))
        }
    }

    @Test fun `apply updates check date`() {
        val today = nowAsCheckDateString()
        assertEquals(
            setOf(
                StringMapEntryModify("lit", "yes", "yes"),
                StringMapEntryAdd("check_date:lit", today)
            ),
            YES.appliedTo(mapOf("lit" to "yes"))
        )
        assertEquals(
            setOf(
                StringMapEntryModify("lit", "no", "no"),
                StringMapEntryAdd("check_date:lit", today)
            ),
            NO.appliedTo(mapOf("lit" to "no"))
        )
        assertEquals(
            setOf(
                StringMapEntryModify("lit", "automatic", "automatic"),
                StringMapEntryAdd("check_date:lit", today)
            ),
            AUTOMATIC.appliedTo(mapOf("lit" to "automatic"))
        )
        assertEquals(
            setOf(
                StringMapEntryModify("lit", "24/7", "24/7"),
                StringMapEntryAdd("check_date:lit", today)
            ),
            NIGHT_AND_DAY.appliedTo(mapOf("lit" to "24/7"))
        )
    }

    @Test fun `apply does not overwrite unsupported value if 'yes'`() {
        assertEquals(
            setOf(StringMapEntryAdd("check_date:lit", nowAsCheckDateString())),
            YES.appliedTo(mapOf("lit" to "limited"))
        )
        assertEquals(
            setOf(StringMapEntryAdd("check_date:lit", nowAsCheckDateString())),
            YES.appliedTo(mapOf("lit" to "22:00-05:00"))
        )
    }

    @Test fun `apply does overwrite unsupported value if not 'yes'`() {
        assertEquals(
            setOf(StringMapEntryModify("lit", "limited", "no")),
            NO.appliedTo(mapOf("lit" to "limited"))
        )
        assertEquals(
            setOf(StringMapEntryModify("lit", "22:00-05:00", "automatic")),
            AUTOMATIC.appliedTo(mapOf("lit" to "22:00-05:00"))
        )
    }
}

private fun LitStatus.appliedTo(tags: Map<String, String>): Set<StringMapEntryChange> {
    val cb = StringMapChangesBuilder(tags)
    applyTo(cb)
    return cb.create().changes
}

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
        assertEquals(
            setOf(
                StringMapEntryAdd("lit", "yes"),
                StringMapEntryAdd("check_date:lit", nowAsCheckDateString())
            ),
            YES.appliedTo(mapOf())
        )
        assertEquals(
            setOf(
                StringMapEntryAdd("lit", "no"),
                StringMapEntryAdd("check_date:lit", nowAsCheckDateString())
            ),
            NO.appliedTo(mapOf())
        )
        assertEquals(
            setOf(
                StringMapEntryAdd("lit", "automatic"),
                StringMapEntryAdd("check_date:lit", nowAsCheckDateString())
            ),
            AUTOMATIC.appliedTo(mapOf())
        )
        assertEquals(
            setOf(
                StringMapEntryAdd("lit", "24/7"),
                StringMapEntryAdd("check_date:lit", nowAsCheckDateString())
            ),
            NIGHT_AND_DAY.appliedTo(mapOf())
        )
    }

    @Test fun `applying invalid throws exception`() {
        assertFailsWith<IllegalArgumentException> {
            UNSUPPORTED.applyTo(StringMapChangesBuilder(mapOf()))
        }
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
            setOf(
                StringMapEntryModify("lit", "limited", "no"),
                StringMapEntryAdd("check_date:lit", nowAsCheckDateString()),
            ),
            NO.appliedTo(mapOf("lit" to "limited"))
        )
        assertEquals(
            setOf(
                StringMapEntryModify("lit", "22:00-05:00", "automatic"),
                StringMapEntryAdd("check_date:lit", nowAsCheckDateString()),
            ),
            AUTOMATIC.appliedTo(mapOf("lit" to "22:00-05:00"))
        )
    }
}

private fun LitStatus.appliedTo(tags: Map<String, String>): Set<StringMapEntryChange> {
    val cb = StringMapChangesBuilder(tags)
    applyTo(cb)
    return cb.create().changes
}

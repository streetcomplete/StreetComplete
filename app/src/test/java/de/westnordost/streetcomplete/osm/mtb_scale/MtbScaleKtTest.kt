package de.westnordost.streetcomplete.osm.mtb_scale

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryChange
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryModify
import de.westnordost.streetcomplete.osm.nowAsCheckDateString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class MtbScaleKtTest {
    @Test fun `parse invalid`() {
        assertNull(parse(""))
        assertNull(parse("-1"))
        assertNull(parse("7"))
        assertNull(parse("grade2"))
        assertNull(parse("S0"))
        assertNull(parse("1-2"))
    }

    @Test fun `parse scale`() {
        for (modifier in MtbScale.Modifier.entries) {
            val mod = modifier.value?.toString().orEmpty()
            for (i in 0..6) {
                assertEquals(MtbScale(i, modifier), parse("$i$mod"))
            }
        }
    }

    @Test fun `applyTo normally`() {
        assertEquals(
            setOf(StringMapEntryAdd("mtb:scale", "6")),
            MtbScale(6).appliedTo(mapOf())
        )
        assertEquals(
            setOf(StringMapEntryAdd("mtb:scale", "3+")),
            MtbScale(3, MtbScale.Modifier.PLUS).appliedTo(mapOf())
        )
        assertEquals(
            setOf(StringMapEntryModify("mtb:scale", "grade2", "2")),
            MtbScale(2).appliedTo(mapOf("mtb:scale" to "grade2"))
        )
        assertEquals(
            setOf(
                StringMapEntryModify("mtb:scale", "2", "2"),
                StringMapEntryAdd("check_date:mtb:scale", nowAsCheckDateString())
            ),
            MtbScale(2).appliedTo(mapOf("mtb:scale" to "2"))
        )
    }

    @Test fun `applyTo with unspecified modifier doesn't overwrite modifier`() {
        assertEquals(
            setOf(
                StringMapEntryModify("mtb:scale", "2+", "2+"),
                StringMapEntryAdd("check_date:mtb:scale", nowAsCheckDateString())
            ),
            MtbScale(2).appliedTo(mapOf("mtb:scale" to "2+"))
        )
    }

    @Test fun `applyTo with specified modifier overwrites modifier`() {
        assertEquals(
            setOf(StringMapEntryModify("mtb:scale", "2+", "2")),
            MtbScale(2, MtbScale.Modifier.NONE).appliedTo(mapOf("mtb:scale" to "2+"))
        )
    }
}

private fun parse(value: String) = parseMtbScale(mapOf("mtb:scale" to value))

private fun MtbScale.appliedTo(tags: Map<String, String>): Set<StringMapEntryChange> {
    val cb = StringMapChangesBuilder(tags)
    applyTo(cb)
    return cb.create().changes
}

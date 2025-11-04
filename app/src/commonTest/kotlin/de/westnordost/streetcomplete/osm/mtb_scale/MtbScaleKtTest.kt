package de.westnordost.streetcomplete.osm.mtb_scale

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryChange
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryModify
import de.westnordost.streetcomplete.osm.nowAsCheckDateString
import de.westnordost.streetcomplete.osm.mtb_scale.MtbScale.Value.*
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
        for (modifier in MtbScale.Mod.entries) {
            val mod = modifier.value?.toString().orEmpty()
            assertEquals(MtbScale(ZERO, modifier), parse("0$mod"))
            assertEquals(MtbScale(ONE, modifier), parse("1$mod"))
            assertEquals(MtbScale(TWO, modifier), parse("2$mod"))
            assertEquals(MtbScale(THREE, modifier), parse("3$mod"))
            assertEquals(MtbScale(FOUR, modifier), parse("4$mod"))
            assertEquals(MtbScale(FIVE, modifier), parse("5$mod"))
            assertEquals(MtbScale(SIX, modifier), parse("6$mod"))
        }
    }

    @Test fun `applyTo normally`() {
        assertEquals(
            setOf(StringMapEntryAdd("mtb:scale", "6")),
            MtbScale(SIX).appliedTo(mapOf())
        )
        assertEquals(
            setOf(StringMapEntryAdd("mtb:scale", "3+")),
            MtbScale(THREE, MtbScale.Mod.PLUS).appliedTo(mapOf())
        )
        assertEquals(
            setOf(StringMapEntryModify("mtb:scale", "grade2", "2")),
            MtbScale(TWO).appliedTo(mapOf("mtb:scale" to "grade2"))
        )
        assertEquals(
            setOf(
                StringMapEntryModify("mtb:scale", "2", "2"),
                StringMapEntryAdd("check_date:mtb:scale", nowAsCheckDateString())
            ),
            MtbScale(TWO).appliedTo(mapOf("mtb:scale" to "2"))
        )
    }

    @Test fun `applyTo with unspecified modifier doesn't overwrite modifier`() {
        assertEquals(
            setOf(
                StringMapEntryModify("mtb:scale", "2+", "2+"),
                StringMapEntryAdd("check_date:mtb:scale", nowAsCheckDateString())
            ),
            MtbScale(TWO).appliedTo(mapOf("mtb:scale" to "2+"))
        )
    }

    @Test fun `applyTo with specified modifier overwrites modifier`() {
        assertEquals(
            setOf(StringMapEntryModify("mtb:scale", "2+", "2")),
            MtbScale(TWO, MtbScale.Mod.NONE).appliedTo(mapOf("mtb:scale" to "2+"))
        )
    }
}

private fun parse(value: String) = parseMtbScale(mapOf("mtb:scale" to value))

private fun MtbScale.appliedTo(tags: Map<String, String>): Set<StringMapEntryChange> {
    val cb = StringMapChangesBuilder(tags)
    applyTo(cb)
    return cb.create().changes
}

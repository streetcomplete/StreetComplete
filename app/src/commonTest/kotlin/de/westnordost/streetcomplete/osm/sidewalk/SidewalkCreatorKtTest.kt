package de.westnordost.streetcomplete.osm.sidewalk

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryChange
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryDelete
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryModify
import de.westnordost.streetcomplete.osm.Sides
import de.westnordost.streetcomplete.osm.nowAsCheckDateString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SidewalkCreatorKtTest {
    @Test fun `apply nothing applies nothing`() {
        assertEquals(
            setOf(),
            Sides<Sidewalk>(null, null).appliedTo(mapOf())
        )
    }

    @Test fun `apply simple values`() {
        assertEquals(
            setOf(StringMapEntryAdd("sidewalk", "both")),
            Sides(Sidewalk.YES, Sidewalk.YES).appliedTo(mapOf()),
        )
        assertEquals(
            setOf(StringMapEntryAdd("sidewalk", "no")),
            Sides(Sidewalk.NO, Sidewalk.NO).appliedTo(mapOf()),
        )
        assertEquals(
            setOf(StringMapEntryAdd("sidewalk", "left")),
            Sides(Sidewalk.YES, Sidewalk.NO).appliedTo(mapOf()),
        )
        assertEquals(
            setOf(StringMapEntryAdd("sidewalk", "right")),
            Sides(Sidewalk.NO, Sidewalk.YES).appliedTo(mapOf()),
        )
    }

    @Test fun `apply value when each side differs`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("sidewalk:left", "yes"),
                StringMapEntryAdd("sidewalk:right", "separate")
            ),
            Sides(Sidewalk.YES, Sidewalk.SEPARATE).appliedTo(mapOf()),
        )
        assertEquals(
            setOf(
                StringMapEntryAdd("sidewalk:left", "separate"),
                StringMapEntryAdd("sidewalk:right", "no")
            ),
            Sides(Sidewalk.SEPARATE, Sidewalk.NO).appliedTo(mapOf()),
        )
    }

    @Test fun `clean up previous tagging`() {
        assertEquals(
            setOf(
                StringMapEntryModify("sidewalk:both", "yes and separate ;-)", "separate"),
                StringMapEntryDelete("sidewalk:left", "yes"),
                StringMapEntryDelete("sidewalk:right", "separate"),
                StringMapEntryDelete("sidewalk", "different"),
            ),
            Sides(Sidewalk.SEPARATE, Sidewalk.SEPARATE).appliedTo(mapOf(
                "sidewalk" to "different",
                "sidewalk:left" to "yes",
                "sidewalk:right" to "separate",
                "sidewalk:both" to "yes and separate ;-)"
            ))
        )
    }

    @Test fun `clean up previous tagging when applying value for each side`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("sidewalk:left", "separate"),
                StringMapEntryAdd("sidewalk:right", "yes"),
                StringMapEntryDelete("sidewalk", "both"),
                StringMapEntryDelete("sidewalk:both", "yes"),
            ),
            Sides(Sidewalk.SEPARATE, Sidewalk.YES).appliedTo(mapOf(
                "sidewalk" to "both",
                "sidewalk:both" to "yes",
            ))
        )
    }

    @Test fun `updates check date`() {
        assertEquals(
            setOf(
                StringMapEntryModify("sidewalk", "both", "both"),
                StringMapEntryAdd("check_date:sidewalk", nowAsCheckDateString())
            ),
            Sides(Sidewalk.YES, Sidewalk.YES).appliedTo(mapOf(
                "sidewalk" to "both"
            ))
        )
        assertEquals(
            setOf(
                StringMapEntryModify("sidewalk:left", "separate", "separate"),
                StringMapEntryModify("sidewalk:right", "no", "no"),
                StringMapEntryAdd("check_date:sidewalk", nowAsCheckDateString())
            ),
            Sides(Sidewalk.SEPARATE, Sidewalk.NO).appliedTo(mapOf(
                "sidewalk:left" to "separate",
                "sidewalk:right" to "no"
            ))
        )
    }

    @Test fun `apply value only for one side`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("sidewalk:left", "yes")
            ),
            Sides(Sidewalk.YES, null).appliedTo(mapOf())
        )
        assertEquals(
            setOf(
                StringMapEntryAdd("sidewalk:right", "no")
            ),
            Sides(null, Sidewalk.NO).appliedTo(mapOf())
        )
    }

    @Test fun `apply for one side does not touch the other side`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("sidewalk:right", "yes")
            ),
            Sides(null, Sidewalk.YES).appliedTo(mapOf(
                "sidewalk:left" to "separate"
            )),
        )
        assertEquals(
            setOf(
                StringMapEntryAdd("sidewalk", "right"),
                StringMapEntryDelete("sidewalk:right", "yes"),
            ),
            Sides(Sidewalk.NO, null).appliedTo(mapOf(
                "sidewalk:right" to "yes"
            )),
        )
    }

    @Test fun `apply for one side does not touch the other side even if it is invalid`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("sidewalk:right", "yes")
            ),
            Sides(null, Sidewalk.YES).appliedTo(mapOf(
                "sidewalk:left" to "some invalid value"
            )),
        )
        assertEquals(
            setOf(
                StringMapEntryAdd("sidewalk:left", "no")
            ),
            Sides(Sidewalk.NO, null).appliedTo(mapOf(
                "sidewalk:right" to "another invalid value"
            )),
        )
    }

    @Test fun `apply for one side does not change values for the other side even if it was defined for both sides before and invalid`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("sidewalk:right", "yes"),
                StringMapEntryDelete("sidewalk:both", "some invalid value"),
                StringMapEntryAdd("sidewalk:left", "some invalid value"),
            ),
            Sides(null, Sidewalk.YES).appliedTo(mapOf(
                "sidewalk:both" to "some invalid value"
            ))
        )
        assertEquals(
            setOf(
                StringMapEntryAdd("sidewalk:left", "yes"),
                StringMapEntryDelete("sidewalk:both", "some invalid value"),
                StringMapEntryAdd("sidewalk:right", "some invalid value"),
            ),
            Sides(Sidewalk.YES, null).appliedTo(mapOf(
                "sidewalk:both" to "some invalid value"
            ))
        )
    }

    @Test fun `apply conflates values`() {
        assertEquals(
            setOf(
                StringMapEntryDelete("sidewalk:left", "yes"),
                StringMapEntryDelete("sidewalk:right", "yes"),
                StringMapEntryAdd("sidewalk", "both"),
            ),
            Sides(Sidewalk.YES, null).appliedTo(mapOf(
                "sidewalk:left" to "yes",
                "sidewalk:right" to "yes",
            ))
        )
        assertEquals(
            setOf(
                StringMapEntryDelete("sidewalk:right", "no"),
                StringMapEntryAdd("sidewalk", "left"),
            ),
            Sides(Sidewalk.YES, null).appliedTo(mapOf(
                "sidewalk:right" to "no",
            ))
        )
        assertEquals(
            setOf(
                StringMapEntryDelete("sidewalk:right", "yes"),
                StringMapEntryAdd("sidewalk", "right"),
            ),
            Sides(Sidewalk.NO, null).appliedTo(mapOf(
                "sidewalk:right" to "yes",
            ))
        )
        assertEquals(
            setOf(
                StringMapEntryDelete("sidewalk:right", "no"),
                StringMapEntryAdd("sidewalk", "no"),
            ),
            Sides(Sidewalk.NO, null).appliedTo(mapOf(
                "sidewalk:right" to "no",
            ))
        )
    }

    @Test fun `apply does not conflate values non-yes-no-values`() {
        assertEquals(
            setOf(
                StringMapEntryDelete("sidewalk:right", "separate"),
                StringMapEntryAdd("sidewalk:both", "separate"),
            ),
            Sides(Sidewalk.SEPARATE, null).appliedTo(mapOf(
                "sidewalk:right" to "separate",
            ))
        )
    }

    @Test fun `applying invalid left throws exception`() {
        assertFailsWith<IllegalArgumentException> {
            Sides(Sidewalk.INVALID, null).applyTo(StringMapChangesBuilder(mapOf()))
        }
    }

    @Test fun `applying invalid right throws exception`() {
        assertFailsWith<IllegalArgumentException> {
            Sides(null, Sidewalk.INVALID).applyTo(StringMapChangesBuilder(mapOf()))
        }
    }
}

private fun Sides<Sidewalk>.appliedTo(tags: Map<String, String>): Set<StringMapEntryChange> {
    val cb = StringMapChangesBuilder(tags)
    applyTo(cb)
    return cb.create().changes
}

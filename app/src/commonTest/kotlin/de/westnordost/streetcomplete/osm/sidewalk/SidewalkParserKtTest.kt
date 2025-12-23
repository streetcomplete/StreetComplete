package de.westnordost.streetcomplete.osm.sidewalk

import de.westnordost.streetcomplete.osm.Sides
import de.westnordost.streetcomplete.osm.sidewalk.Sidewalk.INVALID
import de.westnordost.streetcomplete.osm.sidewalk.Sidewalk.NO
import de.westnordost.streetcomplete.osm.sidewalk.Sidewalk.SEPARATE
import de.westnordost.streetcomplete.osm.sidewalk.Sidewalk.YES
import kotlin.test.Test
import kotlin.test.assertEquals

class SidewalkParserKtTest {
    /* These are a lot of tests because there are many possible permutations and this test does not
     * make (many) assumptions that the code is written in a way that if it is solved for one type,
     * it is solved for all */

    /* ------------------------------------------ invalid --------------------------------------- */

    @Test
    fun `invalid because unknown values`() {
        val invalid = Sides(INVALID, INVALID)
        assertEquals(invalid, parse("sidewalk" to "something"))
        assertEquals(invalid, parse("sidewalk:both" to "something"))
        assertEquals(invalid, parse(
            "sidewalk:left" to "something",
            "sidewalk:right" to "something",
        ))
    }

    @Test
    fun `invalid because contradictory or duplicate values`() {
        val invalid = Sides(INVALID, INVALID)
        assertEquals(invalid, parse(
            "sidewalk:left" to "yes",
            "sidewalk" to "left"))
        assertEquals(invalid, parse(
            "sidewalk:right" to "no",
            "sidewalk" to "both"
        ))
        assertEquals(invalid, parse(
            "sidewalk:both" to "separate",
            "sidewalk" to "left"
        ))
        assertEquals(invalid, parse(
            "sidewalk" to "something",
            "sidewalk:left" to "something"
        ))
        assertEquals(invalid, parse(
            "sidewalk:left" to "yes",
            "sidewalk:right" to "yes",
            "sidewalk" to "something"
        ))
        assertEquals(invalid, parse(
            "sidewalk:left" to "yes",
            "sidewalk:right" to "yes",
            "sidewalk" to "both"
        ))
        assertEquals(invalid, parse(
            "sidewalk:left" to "yes",
            "sidewalk:right" to "no",
            "sidewalk:both" to "yes",
        ))
    }

    @Test fun `left invalid, right yes`() {
        assertEquals(
            Sides(INVALID, YES),
            parse(
                "sidewalk:left" to "something",
                "sidewalk:right" to "yes"
            )
        )
    }

    @Test fun `left invalid, right no`() {
        assertEquals(
            Sides(INVALID, NO),
            parse(
                "sidewalk:left" to "something",
                "sidewalk:right" to "no"
            )
        )
    }

    @Test fun `left invalid, right separate`() {
        assertEquals(
            Sides(INVALID, SEPARATE),
            parse(
                "sidewalk:left" to "something",
                "sidewalk:right" to "separate"
            )
        )
    }

    @Test fun `right invalid, left yes`() {
        assertEquals(
            Sides(YES, INVALID),
            parse(
                "sidewalk:right" to "something",
                "sidewalk:left" to "yes"
            )
        )
    }

    @Test fun `right invalid, left no`() {
        assertEquals(
            Sides(NO, INVALID),
            parse(
                "sidewalk:right" to "something",
                "sidewalk:left" to "no"
            )
        )
    }

    @Test fun `right invalid, left separate`() {
        assertEquals(
            Sides(SEPARATE, INVALID),
            parse(
                "sidewalk:right" to "something",
                "sidewalk:left" to "separate"
            )
        )
    }

    /* ------------------------------------------ both ------------------------------------------ */

    @Test fun `sidewalk both`() {
        assertEquals(
            Sides(YES, YES),
            parse("sidewalk" to "both")
        )
    }

    @Test fun `both sidewalk`() {
        assertEquals(
            Sides(YES, YES),
            parse("sidewalk:both" to "yes")
        )
    }

    @Test fun `left and right yes`() {
        assertEquals(
            Sides(YES, YES),
            parse(
                "sidewalk:left" to "yes",
                "sidewalk:right" to "yes"
            )
        )
    }

    /* ------------------------------------------ none ------------------------------------------ */

    @Test fun `no sidewalk`() {
        assertEquals(
            Sides(NO, NO),
            parse("sidewalk" to "no")
        )
        assertEquals(
            Sides(NO, NO),
            parse("sidewalk" to "none")
        )

        assertEquals(
            Sides(NO, NO),
            parse("sidewalk:both" to "no")
        )
        assertEquals(
            Sides(NO, NO),
            parse("sidewalk:both" to "none")
        )
    }

    @Test fun `left and right no`() {
        assertEquals(
            Sides(NO, NO),
            parse(
                "sidewalk:left" to "no",
                "sidewalk:right" to "no"
            )
        )
        assertEquals(
            Sides(NO, NO),
            parse(
                "sidewalk:left" to "none",
                "sidewalk:right" to "none"
            )
        )
    }

    /* ------------------------------------------ left ------------------------------------------ */

    @Test fun `left sidewalk`() {
        assertEquals(
            Sides(YES, NO),
            parse("sidewalk" to "left")
        )
    }

    @Test fun `left sidewalk, right no`() {
        assertEquals(
            Sides(YES, NO),
            parse(
                "sidewalk:left" to "yes",
                "sidewalk:right" to "no"
            )
        )
    }

    @Test fun `left sidewalk, right separate`() {
        assertEquals(
            Sides(YES, SEPARATE),
            parse(
                "sidewalk:left" to "yes",
                "sidewalk:right" to "separate"
            )
        )
    }

    @Test fun `left sidewalk, right invalid`() {
        assertEquals(
            Sides(YES, INVALID),
            parse(
                "sidewalk:left" to "yes",
                "sidewalk:right" to "something"
            )
        )
    }

    /* ------------------------------------------ right ----------------------------------------- */

    @Test fun `right sidewalk`() {
        assertEquals(
            Sides(NO, YES),
            parse("sidewalk" to "right")
        )
    }

    @Test fun `right sidewalk, left no`() {
        assertEquals(
            Sides(NO, YES),
            parse(
                "sidewalk:right" to "yes",
                "sidewalk:left" to "no"
            )
        )
    }

    @Test fun `right sidewalk, left separate`() {
        assertEquals(
            Sides(SEPARATE, YES),
            parse(
                "sidewalk:right" to "yes",
                "sidewalk:left" to "separate"
            )
        )
    }

    @Test fun `right sidewalk, left invalid`() {
        assertEquals(
            Sides(INVALID, YES),
            parse(
                "sidewalk:right" to "yes",
                "sidewalk:left" to "something"
            )
        )
    }

    /* ------------------------------------- both separate -------------------------------------- */

    @Test fun `both separate`() {
        assertEquals(
            Sides(SEPARATE, SEPARATE),
            parse("sidewalk" to "separate")
        )
        assertEquals(
            Sides(SEPARATE, SEPARATE),
            parse("sidewalk:both" to "separate")
        )
    }

    @Test fun `left and right separate`() {
        assertEquals(
            Sides(SEPARATE, SEPARATE),
            parse(
                "sidewalk:left" to "separate",
                "sidewalk:right" to "separate"
            )
        )
    }

    /* ---------------------------------- only one side defined --------------------------------- */

    @Test fun `left separate, right undefined`() {
        assertEquals(
            Sides(SEPARATE, null),
            parse("sidewalk:left" to "separate")
        )
    }

    @Test fun `left no, right undefined`() {
        assertEquals(
            Sides(NO, null),
            parse("sidewalk:left" to "no")
        )
        assertEquals(
            Sides(NO, null),
            parse("sidewalk:left" to "none")
        )
    }

    @Test fun `left yes, right undefined`() {
        assertEquals(
            Sides(YES, null),
            parse(
                "sidewalk:left" to "yes"
            )
        )
    }

    @Test fun `left invalid, right undefined`() {
        assertEquals(
            Sides(INVALID, null),
            parse(
                "sidewalk:left" to "something"
            )
        )
    }

    @Test fun `left undefined, right separate`() {
        assertEquals(
            Sides(null, SEPARATE),
            parse("sidewalk:right" to "separate")
        )
    }

    @Test fun `left undefined, right no`() {
        assertEquals(
            Sides(null, NO),
            parse("sidewalk:right" to "no")
        )
        assertEquals(
            Sides(null, NO),
            parse("sidewalk:right" to "none")
        )
    }

    @Test fun `left undefined, right yes`() {
        assertEquals(
            Sides(null, YES),
            parse(
                "sidewalk:right" to "yes"
            )
        )
    }

    @Test fun `left undefined, right invalid`() {
        assertEquals(
            Sides(null, INVALID),
            parse(
                "sidewalk:right" to "something"
            )
        )
    }
}

private fun parse(vararg tags: Pair<String, String>) =
    parseSidewalkSides(mapOf(*tags))

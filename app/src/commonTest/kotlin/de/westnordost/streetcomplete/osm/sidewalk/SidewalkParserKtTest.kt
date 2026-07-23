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
    fun `invalid because contradictory values`() {
        val invalid = Sides(INVALID, INVALID)

        // sidewalk=left means that sidewalk:right=no, but sidewalk:left=yes doesn't say anything
        // about the right side. Conflict!
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
            "sidewalk:right" to "no",
            "sidewalk:both" to "yes",
        ))
    }

    @Test fun `duplication is fine when it is not contradictory`() {
        assertEquals(
            Sides(YES, YES),
            parse(
                "sidewalk:left" to "yes",
                "sidewalk:right" to "yes",
                "sidewalk" to "both"
            )
        )
    }

    @Test fun `left invalid and right yes`() {
        assertEquals(
            Sides(INVALID, YES),
            parse(
                "sidewalk:left" to "something",
                "sidewalk:right" to "yes"
            )
        )
    }

    @Test fun `left invalid and right no`() {
        assertEquals(
            Sides(INVALID, NO),
            parse(
                "sidewalk:left" to "something",
                "sidewalk:right" to "no"
            )
        )
    }

    @Test fun `left invalid and right separate`() {
        assertEquals(
            Sides(INVALID, SEPARATE),
            parse(
                "sidewalk:left" to "something",
                "sidewalk:right" to "separate"
            )
        )
    }

    @Test fun `right invalid and left yes`() {
        assertEquals(
            Sides(YES, INVALID),
            parse(
                "sidewalk:right" to "something",
                "sidewalk:left" to "yes"
            )
        )
    }

    @Test fun `right invalid and left no`() {
        assertEquals(
            Sides(NO, INVALID),
            parse(
                "sidewalk:right" to "something",
                "sidewalk:left" to "no"
            )
        )
    }

    @Test fun `right invalid and left separate`() {
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

    /* ------------------------------------------- no ------------------------------------------- */

    @Test fun `no sidewalk`() {
        assertEquals(
            Sides(NO, NO),
            parse("sidewalk" to "no")
        )
        assertEquals(
            Sides(INVALID, INVALID),
            parse("sidewalk" to "none")
        )

        assertEquals(
            Sides(NO, NO),
            parse("sidewalk:both" to "no")
        )
        assertEquals(
            Sides(INVALID, INVALID),
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
            Sides(INVALID, INVALID),
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

    @Test fun `left sidewalk and right no`() {
        assertEquals(
            Sides(YES, NO),
            parse(
                "sidewalk:left" to "yes",
                "sidewalk:right" to "no"
            )
        )
    }

    @Test fun `left sidewalk and right separate`() {
        assertEquals(
            Sides(YES, SEPARATE),
            parse(
                "sidewalk:left" to "yes",
                "sidewalk:right" to "separate"
            )
        )
    }

    @Test fun `left sidewalk and right invalid`() {
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

    @Test fun `right sidewalk and left no`() {
        assertEquals(
            Sides(NO, YES),
            parse(
                "sidewalk:right" to "yes",
                "sidewalk:left" to "no"
            )
        )
    }

    @Test fun `right sidewalk and left separate`() {
        assertEquals(
            Sides(SEPARATE, YES),
            parse(
                "sidewalk:right" to "yes",
                "sidewalk:left" to "separate"
            )
        )
    }

    @Test fun `right sidewalk and left invalid`() {
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

    @Test fun `left separate and right undefined`() {
        assertEquals(
            Sides(SEPARATE, null),
            parse("sidewalk:left" to "separate")
        )
    }

    @Test fun `left no and right undefined`() {
        assertEquals(
            Sides(NO, null),
            parse("sidewalk:left" to "no")
        )
        assertEquals(
            Sides(INVALID, null),
            parse("sidewalk:left" to "none")
        )
    }

    @Test fun `left yes and right undefined`() {
        assertEquals(
            Sides(YES, null),
            parse(
                "sidewalk:left" to "yes"
            )
        )
    }

    @Test fun `left invalid and right undefined`() {
        assertEquals(
            Sides(INVALID, null),
            parse(
                "sidewalk:left" to "something"
            )
        )
    }

    @Test fun `left undefined and right separate`() {
        assertEquals(
            Sides(null, SEPARATE),
            parse("sidewalk:right" to "separate")
        )
    }

    @Test fun `left undefined and right no`() {
        assertEquals(
            Sides(null, NO),
            parse("sidewalk:right" to "no")
        )
        assertEquals(
            Sides(null, INVALID),
            parse("sidewalk:right" to "none")
        )
    }

    @Test fun `left undefined and right yes`() {
        assertEquals(
            Sides(null, YES),
            parse(
                "sidewalk:right" to "yes"
            )
        )
    }

    @Test fun `left undefined and right invalid`() {
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

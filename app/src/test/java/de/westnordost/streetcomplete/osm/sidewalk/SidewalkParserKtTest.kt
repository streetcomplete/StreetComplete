package de.westnordost.streetcomplete.osm.sidewalk

import de.westnordost.streetcomplete.osm.sidewalk.Sidewalk.INVALID
import de.westnordost.streetcomplete.osm.sidewalk.Sidewalk.NO
import de.westnordost.streetcomplete.osm.sidewalk.Sidewalk.SEPARATE
import de.westnordost.streetcomplete.osm.sidewalk.Sidewalk.YES
import org.junit.Assert.assertEquals
import org.junit.Test

class SidewalkParserKtTest {
    /* These are a lot of tests because there are many possible permutations and this test does not
    *  make (many) assumptions that the code is written in a way that if it is solved for one type,
    *  it is solved for all */

    /* ------------------------------------------ invalid --------------------------------------- */

    @Test
    fun `both invalid`() {
        val invalid = LeftAndRightSidewalk(INVALID, INVALID)
        assertEquals(invalid, parse("sidewalk" to "something"))
        assertEquals(invalid, parse("sidewalk:both" to "something"))
        assertEquals(invalid, parse("sidewalk:left" to "something"))
        assertEquals(invalid, parse("sidewalk:right" to "something"))
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

    @Test
    fun `left invalid, right yes`() {
        val leftInvalidRightYes = LeftAndRightSidewalk(INVALID, YES)
        assertEquals(leftInvalidRightYes, parse(
            "sidewalk:left" to "something",
            "sidewalk:right" to "yes"
        ))
        assertEquals(leftInvalidRightYes, parse("sidewalk:right" to "yes")
        )
    }

    @Test
    fun `left invalid, right no`() {
        val leftInvalidRightNo = LeftAndRightSidewalk(INVALID, NO)
        assertEquals(leftInvalidRightNo, parse(
            "sidewalk:left" to "something",
            "sidewalk:right" to "no"
        ))
        assertEquals(leftInvalidRightNo, parse("sidewalk:right" to "no")
        )
    }

    @Test
    fun `left invalid, right separate`() {
        val leftInvalidRightSeparate = LeftAndRightSidewalk(INVALID, SEPARATE)
        assertEquals(leftInvalidRightSeparate, parse(
            "sidewalk:left" to "something",
            "sidewalk:right" to "separate"
        ))
    }

    @Test
    fun `right invalid, left yes`() {
        val rightInvalidLeftYes = LeftAndRightSidewalk(YES, INVALID)
        assertEquals(rightInvalidLeftYes, parse(
            "sidewalk:right" to "something",
            "sidewalk:left" to "yes"
        ))
        assertEquals(rightInvalidLeftYes, parse("sidewalk:left" to "yes"))
    }

    @Test
    fun `right invalid, left no`() {
        val rightInvalidLeftNo = LeftAndRightSidewalk(NO, INVALID)
        assertEquals(rightInvalidLeftNo, parse(
            "sidewalk:right" to "something",
            "sidewalk:left" to "no"
        ))
        assertEquals(rightInvalidLeftNo, parse("sidewalk:left" to "no"))
    }

    @Test
    fun `right invalid, left separate`() {
        val rightInvalidLeftSeparate = LeftAndRightSidewalk(SEPARATE, INVALID)
        assertEquals(rightInvalidLeftSeparate, parse(
            "sidewalk:right" to "something",
            "sidewalk:left" to "separate"
        ))
        assertEquals(rightInvalidLeftSeparate, parse("sidewalk:left" to "separate"))
    }

    /* ------------------------------------------ both ------------------------------------------ */

    @Test
    fun `sidewalk both`() {
        assertEquals(
            LeftAndRightSidewalk(YES, YES),
            parse("sidewalk" to "both")
        )
    }

    @Test
    fun `both sidewalk`() {
        assertEquals(
            LeftAndRightSidewalk(YES, YES),
            parse("sidewalk:both" to "yes")
        )
    }

    @Test
    fun `left and right yes`() {
        assertEquals(
            LeftAndRightSidewalk(YES, YES),
            parse(
                "sidewalk:left" to "yes",
                "sidewalk:right" to "yes"
            )
        )
    }

    /* ------------------------------------------ none ------------------------------------------ */

    @Test
    fun `no sidewalk`() {
        assertEquals(
            LeftAndRightSidewalk(NO, NO),
            parse("sidewalk" to "no")
        )
    }

    @Test
    fun `both no`() {
        assertEquals(
            LeftAndRightSidewalk(NO, NO),
            parse("sidewalk:both" to "no")
        )
    }

    @Test
    fun `left and right no`() {
        assertEquals(
            LeftAndRightSidewalk(NO, NO),
            parse(
                "sidewalk:left" to "no",
                "sidewalk:right" to "no"
            )
        )
    }

    /* ------------------------------------------ left ------------------------------------------ */

    @Test
    fun `left sidewalk`() {
        assertEquals(
            LeftAndRightSidewalk(YES, NO),
            parse("sidewalk" to "left")
        )
    }

    @Test
    fun `left sidewalk, right no`() {
        assertEquals(
            LeftAndRightSidewalk(YES, NO),
            parse(
                "sidewalk:left" to "yes",
                "sidewalk:right" to "no"
            )
        )
    }

    @Test
    fun `left sidewalk, right separate`() {
        assertEquals(
            LeftAndRightSidewalk(YES, SEPARATE),
            parse(
                "sidewalk:left" to "yes",
                "sidewalk:right" to "separate"
            )
        )
    }

    /* ------------------------------------------ right ----------------------------------------- */

    @Test
    fun `right sidewalk`() {
        assertEquals(
            LeftAndRightSidewalk(NO, YES),
            parse("sidewalk" to "right")
        )
    }

    @Test
    fun `right sidewalk, left no`() {
        assertEquals(
            LeftAndRightSidewalk(NO, YES),
            parse(
                "sidewalk:right" to "yes",
                "sidewalk:left" to "no"
            )
        )
    }

    @Test
    fun `right sidewalk, left separate`() {
        assertEquals(
            LeftAndRightSidewalk(SEPARATE, YES),
            parse(
                "sidewalk:right" to "yes",
                "sidewalk:left" to "separate"
            )
        )
    }

    /* ------------------------------------------ separate -------------------------------------- */

    @Test
    fun `separate sidewalk`() {
        assertEquals(
            LeftAndRightSidewalk(SEPARATE, SEPARATE),
            parse("sidewalk" to "separate")
        )
    }

    @Test
    fun `both separate`() {
        assertEquals(
            LeftAndRightSidewalk(SEPARATE, SEPARATE),
            parse("sidewalk" to "separate")
        )
    }

    @Test
    fun `left and right separate`() {
        assertEquals(
            LeftAndRightSidewalk(SEPARATE, SEPARATE),
            parse(
                "sidewalk:left" to "separate",
                "sidewalk:right" to "separate"
            )
        )
    }
}

private fun parse(vararg pairs: Pair<String, String>) =
    createSidewalkSides(mapOf(*pairs))

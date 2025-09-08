package de.westnordost.streetcomplete.osm.address

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class StructuredHouseNumbersTest {

    @Test fun step() {
        assertEquals(simple(2), parseHouseNumbers("1")?.step(1))
        assertEquals(simple(2), parseHouseNumbers("1c")?.step(1))
        assertEquals(simple(2), parseHouseNumbers("1/7")?.step(1))

        assertEquals(simple(2), parseHouseNumbers("1-4")?.step(1))

        assertEquals(simple(7), parseHouseNumbers("2,6,3")?.step(1))
        assertEquals(simple(7), parseHouseNumbers("2,6/3,3")?.step(1))
        assertEquals(simple(7), parseHouseNumbers("2,6-8,3")?.step(1))

        assertEquals(null, parseHouseNumbers("1")?.step(-1))
        assertEquals(null, parseHouseNumbers("1a")?.step(-1))
        assertEquals(null, parseHouseNumbers("1/3")?.step(-1))

        assertEquals(simple(1), parseHouseNumbers("2")?.step(-1))
        assertEquals(simple(1), parseHouseNumbers("2c")?.step(-1))
        assertEquals(simple(1), parseHouseNumbers("2/5")?.step(-1))

        assertEquals(null, parseHouseNumbers("1-4")?.step(-1))
        assertEquals(null, parseHouseNumbers("1f-4")?.step(-1))

        assertEquals(simple(2), parseHouseNumbers("3-7")?.step(-1))

        assertEquals(null, parseHouseNumbers("6,1,3")?.step(-1))
        assertEquals(null, parseHouseNumbers("6,1/4,3")?.step(-1))

        assertEquals(simple(1), parseHouseNumbers("6,2,3")?.step(-1))
        assertEquals(simple(1), parseHouseNumbers("6,2/4,3")?.step(-1))

        assertEquals(null, parseHouseNumbers("6,1-8,3")?.step(-1))
        assertEquals(null, parseHouseNumbers("6,1/4,3")?.step(-1))
    }

    @Test fun minorStep() {
        assertEquals(null, parseHouseNumbers("1")?.minorStep(1))
        assertEquals(null, parseHouseNumbers("2")?.minorStep(-1))

        assertEquals(simple(1), parseHouseNumbers("1a")?.minorStep(-1))
        assertEquals(simple(1), parseHouseNumbers("1/1")?.minorStep(-1))

        assertEquals(withLetter(1, "b"), parseHouseNumbers("1a")?.minorStep(1))
        assertEquals(withNumber(1, 2), parseHouseNumbers("1/1")?.minorStep(1))
        assertEquals(withLetter(1, "a"), parseHouseNumbers("1b")?.minorStep(-1))
        assertEquals(withNumber(1, 1), parseHouseNumbers("1/2")?.minorStep(-1))

        assertEquals(null, parseHouseNumbers("6f,9")?.minorStep(1))
        assertEquals(withLetter(6, "e"), parseHouseNumbers("6f,9")?.minorStep(-1))
    }
}

class StructuredHouseNumberTest {

    @Test fun comparisons() {
        val hs_1 = simple(1)
        val hs_4 = simple(4)
        val hs_6 = simple(6)
        val hs_4c = withLetter(4, "c")
        val hs_4d = withLetter(4, "d")
        val hs_4_4 = withNumber(4, 4)
        val hs_1_4 = withNumber(1, 4)
        val hs_1_6 = withNumber(1, 6)

        // simple
        assertFalse(hs_1 > hs_1)
        assertFalse(hs_1 < hs_1)
        assertTrue(hs_4 > hs_1)
        assertTrue(hs_1 < hs_4)
        assertTrue(hs_6 > hs_4c)
        assertTrue(hs_4c < hs_6)
        assertTrue(hs_6 > hs_4_4)
        assertTrue(hs_4_4 < hs_6)

        // simple vs with letter when same number
        assertTrue(hs_4 < hs_4c)
        assertTrue(hs_4c > hs_4)

        // simple vs with number when same number
        assertTrue(hs_4 < hs_4_4)
        assertTrue(hs_4_4 > hs_4)

        // with number vs with letter when same number
        assertTrue(hs_4_4 < hs_4c)
        assertTrue(hs_4c > hs_4_4)

        // with letter
        assertTrue(hs_4d > hs_4c)
        assertTrue(hs_4c < hs_4d)

        // with number
        assertTrue(hs_1_6 > hs_1_4)
        assertTrue(hs_1_4 < hs_1_6)
    }

    @Test fun step() {
        assertEquals(simple(2), simple(1).step(1))
        assertEquals(null, simple(1).step(-1))
        assertEquals(simple(1), simple(2).step(-1))
    }

    @Test fun minorStep() {
        assertEquals(null, simple(1).minorStep(1))
        assertEquals(null, simple(2).minorStep(-1))

        assertEquals(withLetter(1, "b"), withLetter(1, "a").minorStep(1))
        assertEquals(withLetter(1, "B"), withLetter(1, "A").minorStep(1))
        assertEquals(null, withLetter(1, "z").minorStep(1))
        assertEquals(null, withLetter(1, "Z").minorStep(1))
        assertEquals(simple(1), withLetter(1, "a").minorStep(-1))
        assertEquals(simple(1), withLetter(1, "A").minorStep(-1))

        // and non-latin script, too
        assertEquals(withLetter(1, "α"), withLetter(1, "β").minorStep(-1))
        assertEquals(withLetter(1, "β"), withLetter(1, "α").minorStep(1))

        assertEquals(withNumber(1, 2), withNumber(1, 1).minorStep(1))
        assertEquals(simple(1), withNumber(1, 1).minorStep(-1))
    }
}

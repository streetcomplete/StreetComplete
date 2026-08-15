package de.westnordost.streetcomplete.util.locale

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotSame

class CurrencyAmountTest {

    @Test
    fun testFromDoubleZeroDigits() {
        assertEquals(CurrencyAmount(12, 0),
            CurrencyAmount.fromDouble(12.0, 0))
        assertEquals(CurrencyAmount(12, 0),
            CurrencyAmount.fromDouble(12.201, 0))
    }
    @Test
    fun testFromDoubleSeveralDigits() {
        assertEquals(CurrencyAmount(12, 0),
            CurrencyAmount.fromDouble(12.0, 3))
        assertEquals(CurrencyAmount(12, 305),
            CurrencyAmount.fromDouble(12.305, 3))
        assertEquals(CurrencyAmount(12, 31),
            CurrencyAmount.fromDouble(12.305, 2))
        assertEquals(CurrencyAmount(12, 30),
            CurrencyAmount.fromDouble(12.30, 2))
    }
    @Test
    fun testConvertDoubleBothWays() {
        assertEquals(15.0,
            CurrencyAmount.fromDouble(15.0, 0).toDouble(0))
        assertEquals(15.201,
            CurrencyAmount.fromDouble(15.201, 3).toDouble(3))
        assertEquals(15.261,
            CurrencyAmount.fromDouble(15.2612, 3).toDouble(3))
    }

    @Test
    fun testToDouble() {
        assertEquals(12.0,
            CurrencyAmount(12, 0).toDouble(0))
        assertEquals(12.0,
            CurrencyAmount(12, 50).toDouble(0))
        assertEquals(12.5,
            CurrencyAmount(12, 50).toDouble(2))
        assertEquals(12.51,
            CurrencyAmount(12, 510).toDouble(3),
            "Tests ")
        assertNotSame(12.51,
            CurrencyAmount(12, 51).toDouble(3))
    }

    @Test
    fun testDoubleToString() {
        assertEquals("14.20",
            CurrencyAmount.fromDouble(14.2, 2).toString(2))
        assertEquals("14",
            CurrencyAmount.fromDouble(14.0, 2).toString(2))
    }

    @Test
    fun testToStringZeroSubunits() {
        val amount = CurrencyAmount(12, 50)
        assertEquals("12", amount.toString(0))

        val amount2 = CurrencyAmount(12, 5)
        assertEquals("12", amount2.toString(0))

        val amount3 = CurrencyAmount(12, 0)
        assertEquals("12", amount3.toString(0))
    }
    @Test
    fun testToStringOneSubunit() {
        val amount = CurrencyAmount(12, 53)
        assertEquals("12.5", amount.toString(1))

        val amount2 = CurrencyAmount(12, 5)
        assertEquals("12.5", amount2.toString(1))

        val amount3 = CurrencyAmount(12, 0)
        assertEquals("12", amount3.toString(1))
    }
    @Test
    fun testToStringTwoSubunits() {
        val amount = CurrencyAmount(12, 50)
        assertEquals("12.50", amount.toString(2))

        val amount2 = CurrencyAmount(12, 5)
        assertEquals("12.50", amount2.toString(2))

        val amount3 = CurrencyAmount(12, 0)
        assertEquals("12", amount3.toString(2))
    }
    @Test
    fun testToStringThreeSubunits() {
        val amount = CurrencyAmount(12, 51)
        assertEquals("12.510", amount.toString(3))

        val amount2 = CurrencyAmount(12, 5)
        assertEquals("12.500", amount2.toString(3))

        val amount3 = CurrencyAmount(12, 0)
        assertEquals("12", amount3.toString(3))
    }
    @Test
    fun testToStringFourSubunits() {
        val amount = CurrencyAmount(12, 501)
        assertEquals("12.5010", amount.toString(4))

        val amount2 = CurrencyAmount(12, 5)
        assertEquals("12.5000", amount2.toString(4))

        val amount3 = CurrencyAmount(12, 0)
        assertEquals("12", amount3.toString(4))
    }
}

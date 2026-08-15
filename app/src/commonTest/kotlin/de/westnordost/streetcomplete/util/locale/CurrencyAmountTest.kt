package de.westnordost.streetcomplete.util.locale

import kotlin.test.Test
import kotlin.test.assertEquals

class CurrencyAmountTest {
    // TODO: Test toDouble, fromDouble

    @Test
    fun testToStringZeroSubunits() {
        var amount = CurrencyAmount(12, 50)
        assertEquals("12", amount.toString(0))

        var amount2 = CurrencyAmount(12, 5)
        assertEquals("12", amount2.toString(0))

        var amount3 = CurrencyAmount(12, 0)
        assertEquals("12", amount3.toString(0))
    }
    @Test
    fun testToStringOneSubunit() {
        var amount = CurrencyAmount(12, 53)
        assertEquals("12.5", amount.toString(1))

        var amount2 = CurrencyAmount(12, 5)
        assertEquals("12.5", amount2.toString(1))

        var amount3 = CurrencyAmount(12, 0)
        assertEquals("12", amount3.toString(1))
    }
    @Test
    fun testToStringTwoSubunits() {
        var amount = CurrencyAmount(12, 50)
        assertEquals("12.50", amount.toString(2))

        var amount2 = CurrencyAmount(12, 5)
        assertEquals("12.50", amount2.toString(2))

        var amount3 = CurrencyAmount(12, 0)
        assertEquals("12", amount3.toString(2))
    }
    @Test
    fun testToStringThreeSubunits() {
        var amount = CurrencyAmount(12, 51)
        assertEquals("12.510", amount.toString(3))

        var amount2 = CurrencyAmount(12, 5)
        assertEquals("12.500", amount2.toString(3))

        var amount3 = CurrencyAmount(12, 0)
        assertEquals("12", amount3.toString(3))
    }
    @Test
    fun testToStringFourSubunits() {
        var amount = CurrencyAmount(12, 501)
        assertEquals("12.5010", amount.toString(4))

        var amount2 = CurrencyAmount(12, 5)
        assertEquals("12.5000", amount2.toString(4))

        var amount3 = CurrencyAmount(12, 0)
        assertEquals("12", amount3.toString(4))
    }
}

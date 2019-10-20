package de.westnordost.streetcomplete.data.osm.tql

import org.junit.Test

import org.junit.Assert.*

class BooleanExpressionTest {

    @Test fun `match leaf`() {
        assertTrue(evalExpression("1"))
        assertFalse(evalExpression("0"))
    }

    @Test fun `match or`() {
        assertTrue(evalExpression("1+1"))
        assertTrue(evalExpression("1+0"))
        assertTrue(evalExpression("0+1"))
        assertFalse(evalExpression("0+0"))

        assertTrue(evalExpression("0+0+1"))
    }

    @Test fun `match and`() {
        assertTrue(evalExpression("1*1"))
        assertFalse(evalExpression("1*0"))
        assertFalse(evalExpression("0*1"))
        assertFalse(evalExpression("0*0"))

        assertTrue(evalExpression("1*1*1"))
        assertFalse(evalExpression("1*1*0"))
    }

    @Test fun `match and in or`() {
        assertTrue(evalExpression("(1*0)+1"))
        assertFalse(evalExpression("(1*0)+0"))
        assertTrue(evalExpression("(1*1)+0"))
        assertTrue(evalExpression("(1*1)+1"))
    }

    @Test fun `match or in and`() {
        assertTrue(evalExpression("(1+0)*1"))
        assertFalse(evalExpression("(1+0)*0"))
        assertFalse(evalExpression("(0+0)*0"))
        assertFalse(evalExpression("(0+0)*1"))
    }

    private fun evalExpression(input: String): Boolean {
        val expr = TestBooleanExpressionParser.parse(input)
        return expr!!.matches("1")
    }
}

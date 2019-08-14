package de.westnordost.streetcomplete.data.osm.tql

import org.junit.Test

import org.junit.Assert.*

class BooleanExpressionBuilderTest {
    @Test fun leaf() { check("a") }
    @Test fun and() { check("a*b") }
    @Test fun or() { check("a+b") }

    @Test fun and3() { check("a*b*c") }
    @Test fun or3() { check("a+b+c") }
    @Test fun andOr() { check("a*b+c") }
    @Test fun orAnd() { check("a+b*c") }

    @Test fun andInOr() { check("a+b*c+d") }
    @Test fun andInOr2() { check("a*b+c*d") }

    @Test fun brackets0() { check("(a)", "a") }

    @Test fun brackets1() { check("(a*b)", "a*b") }
    @Test fun brackets2() { check("(a+b)", "a+b") }
    @Test fun brackets3() { check("((a*b))", "a*b") }
    @Test fun brackets4() { check("((a+b))", "a+b") }

    @Test fun brackets5() { check("(a+b)*c") }
    @Test fun brackets6() { check("a*(b+c)") }
    @Test fun brackets7() { check("a*(b+c)*d") }

    @Test fun brackets8() { check("(a*b)+c", "a*b+c") }
    @Test fun brackets9() { check("(a*b)*c", "a*b*c") }
    @Test fun brackets10() { check("(a+b)+c", "a+b+c") }

    @Test fun brackets11() { check("a+(b*c)", "a+b*c") }
    @Test fun brackets12() { check("a*(b*c)", "a*b*c") }
    @Test fun brackets13() { check("a+(b+c)", "a+b+c") }

    @Test fun brackets14() { check("(a*b+c)", "a*b+c") }
    @Test fun brackets15() { check("(a+b*c)", "a+b*c") }
    @Test fun brackets16() { check("(((a+b*c)))", "a+b*c") }

    @Test fun merge1() { check("a+(b+(c+(d)))", "a+b+c+d") }
    @Test fun merge2() { check("a*(b*(c*(d)))", "a*b*c*d") }
    @Test fun merge3() { check("a*(b+(c*(d)))", "a*(b+c*d)") }
    @Test fun merge4() { check("a+(b*(c+(d)))", "a+b*(c+d)") }

    @Test fun merge5() { check("(((a)+b)+c)+d", "a+b+c+d") }
    @Test fun merge6() { check("(((a)*b)*c)*d", "a*b*c*d") }
    @Test fun merge7() { check("(((a)+b)*c)+d", "(a+b)*c+d") }
    @Test fun merge8() { check("(((a)*b)+c)*d", "(a*b+c)*d") }

    @Test fun merge9() { check("(a+b*c)*d", "(a+b*c)*d") }
    @Test fun merge10() { check("(a+b*c)*d*(e+f*g)*h", "(a+b*c)*d*(e+f*g)*h") }

    @Test fun flatten1() { check("((a*b)*c)*d*(e*f)", "a*b*c*d*e*f") }
    @Test fun flatten2() { check("(a+b*(c+d)+e)*f", "(a+b*(c+d)+e)*f") }


    @Test(expected = IllegalStateException::class)
    fun `closed too many brackets 1`() { TestBooleanExpressionParser.parse("a+b)") }

    @Test(expected = IllegalStateException::class)
    fun `closed too many brackets 2`() { TestBooleanExpressionParser.parse("(a+b))") }

    @Test(expected = IllegalStateException::class)
    fun `closed too many brackets 3`() { TestBooleanExpressionParser.parse("((b+c)*a)+d)") }

    @Test(expected = IllegalStateException::class)
    fun `closed too few brackets 1`() { TestBooleanExpressionParser.parse("(a+b") }

    @Test(expected = IllegalStateException::class)
    fun `closed too few brackets 2`() { TestBooleanExpressionParser.parse("((a+b)") }

    @Test(expected = IllegalStateException::class)
    fun `closed too few brackets 3`() { TestBooleanExpressionParser.parse("((a*(b+c))") }

    private fun check(input: String, expected: String = input) {
        val tree = TestBooleanExpressionParser.parse(input)
        assertEquals(expected, translateOutput(tree.toString()))
    }

    private fun translateOutput(output: String) = output.replace(" and ", "*").replace(" or ", "+")
}

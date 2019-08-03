package de.westnordost.streetcomplete.data.osm.tql

import org.junit.Test

import org.junit.Assert.*

class BooleanExpressionTest {

    @Test fun expand00() { checkExpand("a", "a") }
    @Test fun expand01() { checkExpand("a*b*c", "a*b*c") }
    @Test fun expand02() { checkExpand("a+b+c", "a+b+c") }
    @Test fun expand03() { checkExpand("a+b*c+d", "a+b*c+d") }
    @Test fun expand04() { checkExpand("a*b+c*d", "a*b+c*d") }

    @Test fun expand10() { checkExpand("(a+b)*c", "a*c+b*c") }
    @Test fun expand11() { checkExpand("a*(b+c)", "a*b+a*c") }
    @Test fun expand12() { checkExpand("a*(b+c)*d", "a*b*d+a*c*d") }

    @Test fun expand20() { checkExpand("a*(b+c*d)", "a*b+a*c*d") }
    @Test fun expand21() { checkExpand("a+b*(c+d)", "a+b*c+b*d") }
    @Test fun expand22() { checkExpand("(a+b)*c+d", "a*c+b*c+d") }

    @Test fun expand30() { checkExpand("((a+b)*c+d)*e", "a*c*e+b*c*e+d*e") }
    @Test fun expand31() { checkExpand("a*(b+c*(d+e))", "a*b+a*c*d+a*c*e") }
    @Test fun expand32() { checkExpand("z*(y+x*(a+b)*c+d)*e", "z*y*e+z*x*a*c*e+z*x*b*c*e+z*d*e") }

    @Test fun expand40() { checkExpand("(x+y)*z*(a+b)", "x*z*a+x*z*b+y*z*a+y*z*b") }

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

    @Test fun `type not initially set`() {
        val x = BooleanExpression<BooleanExpressionValue<String>, String>()
        assertFalse(x.isAnd)
        assertFalse(x.isOr)
        assertFalse(x.isRoot)
        assertFalse(x.isValue)
    }

    @Test fun `add and`() {
        val x = BooleanExpression<BooleanExpressionValue<String>, String>()
        assertTrue(x.addAnd().isAnd)
    }

    @Test fun `add or`() {
        val x = BooleanExpression<BooleanExpressionValue<String>, String>()
        assertTrue(x.addOr().isOr)
    }

    @Test fun `set as root`() {
        val x = BooleanExpression<BooleanExpressionValue<String>, String>(true)
        assertTrue(x.isRoot)
    }

    @Test fun `set as value`() {
        val x = BooleanExpression<BooleanExpressionValue<String>, String>()
        x.addValue(TestBooleanExpressionValue("jo"))
        assertTrue(x.firstChild!!.isValue)
        assertEquals("jo", (x.firstChild!!.value as TestBooleanExpressionValue).value)
    }

    @Test fun `get parent`() {
        val parent = BooleanExpression<BooleanExpressionValue<String>, String>()
        assertNull(parent.parent)
        assertEquals(parent, parent.addOpenBracket().parent)
    }

    @Test fun `copy string equals`() {
        val tree = TestBooleanExpressionParser.parse("(a+b)*c")
        val treeCopy = tree.copy()

        assertEquals(treeCopy.toString(), tree.toString())
    }

    @Test fun `copy is deep`() {
        val tree = TestBooleanExpressionParser.parse("(a+b)*c")
        val treeCopy = tree.copy()
        checkRecursiveEqualsButNotSame(tree, treeCopy)
    }

    private fun checkRecursiveEqualsButNotSame(
        tree: BooleanExpression<BooleanExpressionValue<String>, String>,
        treeCopy: BooleanExpression<BooleanExpressionValue<String>, String>
    ) {
        assertNotSame(tree, treeCopy)
        assertEquals(tree.toString(), treeCopy.toString())

        val treeIt = tree.children.iterator()
        val treeCopyIt = treeCopy.children.iterator()

        while (treeIt.hasNext()) {
            checkRecursiveEqualsButNotSame(treeIt.next(), treeCopyIt.next())
        }
    }

    private fun checkExpand(input: String, expected: String) {
        val tree = TestBooleanExpressionParser.parse(input)
        tree.expand()
        assertEquals(expected, translateOutput(tree.toString()))
    }

    private fun translateOutput(output: String) = output.replace(" and ", "*").replace(" or ", "+")

    private fun evalExpression(input: String): Boolean {
        val expr = TestBooleanExpressionParser.parse(input)
        return expr.matches("1")
    }
}

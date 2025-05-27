package de.westnordost.streetcomplete.data.elementfilter

import de.westnordost.streetcomplete.data.elementfilter.filters.HasKey
import de.westnordost.streetcomplete.data.elementfilter.filters.NotHasKey
import de.westnordost.streetcomplete.testutils.node
import de.westnordost.streetcomplete.testutils.rel
import de.westnordost.streetcomplete.testutils.way
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ElementFilterExpressionTest {
    private val node = node()
    private val way = way()
    private val relation = rel()

    @Test fun `matches nodes`() {
        val expr = ElementFilterExpression(setOf(ElementsTypeFilter.NODES), null)

        assertTrue(expr.matches(node))
        assertFalse(expr.matches(way))
        assertFalse(expr.matches(relation))
    }

    @Test fun `matches ways`() {
        val expr = ElementFilterExpression(setOf(ElementsTypeFilter.WAYS), null)

        assertFalse(expr.matches(node))
        assertTrue(expr.matches(way))
        assertFalse(expr.matches(relation))
    }

    @Test fun `matches relations`() {
        val expr = ElementFilterExpression(setOf(ElementsTypeFilter.RELATIONS), null)

        assertFalse(expr.matches(node))
        assertFalse(expr.matches(way))
        assertTrue(expr.matches(relation))
    }

    @Test fun `matches nwr`() {
        val expr = ElementFilterExpression(ElementsTypeFilter.entries.toSet(), null)

        assertTrue(expr.matches(node))
        assertTrue(expr.matches(way))
        assertTrue(expr.matches(relation))
    }

    @Test fun `matches nw`() {
        val expr = ElementFilterExpression(setOf(ElementsTypeFilter.WAYS, ElementsTypeFilter.NODES), null)

        assertTrue(expr.matches(node))
        assertTrue(expr.matches(way))
    }

    @Test fun `matches wr`() {
        val expr = ElementFilterExpression(setOf(ElementsTypeFilter.WAYS, ElementsTypeFilter.RELATIONS), null)

        assertTrue(expr.matches(way))
        assertTrue(expr.matches(relation))
    }

    @Test fun `matches filter`() {
        val expr1 = ElementFilterExpression(setOf(ElementsTypeFilter.NODES), Leaf(HasKey("bla")))

        assertTrue(expr1.matches(node(tags = mapOf("bla" to "1"))))
        assertFalse(expr1.matches(node(tags = mapOf("foo" to "1"))))
        assertFalse(expr1.matches(node(tags = mapOf())))

        // to test mayEvaluateToTrueWithNoTags
        val expr2 = ElementFilterExpression(setOf(ElementsTypeFilter.NODES), Leaf(NotHasKey("bla")))
        assertTrue(expr2.matches(node(tags = mapOf())))
        assertFalse(expr2.matches(node(tags = mapOf("bla" to "1"))))
        assertTrue(expr2.matches(node(tags = mapOf("foo" to "1"))))
    }
}

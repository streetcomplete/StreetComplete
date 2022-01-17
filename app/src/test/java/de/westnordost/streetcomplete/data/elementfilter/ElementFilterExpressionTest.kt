package de.westnordost.streetcomplete.data.elementfilter

import de.westnordost.streetcomplete.data.elementfilter.filters.HasKey
import de.westnordost.streetcomplete.data.elementfilter.filters.NotHasKey
import de.westnordost.streetcomplete.testutils.node
import de.westnordost.streetcomplete.testutils.rel
import de.westnordost.streetcomplete.testutils.way
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.EnumSet

class ElementFilterExpressionTest {
    private val node = node()
    private val way = way()
    private val relation = rel()

    @Test fun `matches nodes`() {
        val expr = createMatchExpression(ElementsTypeFilter.NODES)

        assertTrue(expr.matches(node))
        assertFalse(expr.matches(way))
        assertFalse(expr.matches(relation))
    }

    @Test fun `matches ways`() {
        val expr = createMatchExpression(ElementsTypeFilter.WAYS)

        assertFalse(expr.matches(node))
        assertTrue(expr.matches(way))
        assertFalse(expr.matches(relation))
    }

    @Test fun `matches relations`() {
        val expr = createMatchExpression(ElementsTypeFilter.RELATIONS)

        assertFalse(expr.matches(node))
        assertFalse(expr.matches(way))
        assertTrue(expr.matches(relation))
    }

    @Test fun `matches nwr`() {
        val expr = createMatchExpression(*ElementsTypeFilter.values())

        assertTrue(expr.matches(node))
        assertTrue(expr.matches(way))
        assertTrue(expr.matches(relation))
    }

    @Test fun `matches nw`() {
        val expr = createMatchExpression(ElementsTypeFilter.WAYS, ElementsTypeFilter.NODES)

        assertTrue(expr.matches(node))
        assertTrue(expr.matches(way))
    }

    @Test fun `matches wr`() {
        val expr = createMatchExpression(ElementsTypeFilter.WAYS, ElementsTypeFilter.RELATIONS)

        assertTrue(expr.matches(way))
        assertTrue(expr.matches(relation))
    }

    @Test fun `matches filter`() {
        val expr = ElementFilterExpression(EnumSet.of(ElementsTypeFilter.NODES), Leaf(HasKey("bla")))

        assertTrue(expr.matches(node(tags = mapOf("bla" to "1"))))
        assertFalse(expr.matches(node(tags = mapOf("foo" to "1"))))
    }

    private fun createMatchExpression(vararg elementsTypeFilter: ElementsTypeFilter): ElementFilterExpression {
        val tagFilter = NotHasKey("something")
        return ElementFilterExpression(createEnumSet(*elementsTypeFilter), Leaf(tagFilter))
    }

    private fun createEnumSet(vararg filters: ElementsTypeFilter): EnumSet<ElementsTypeFilter> {
        return when (filters.size) {
            1 -> EnumSet.of(filters[0])
            2 -> EnumSet.of(filters[0], filters[1])
            3 -> EnumSet.of(filters[0], filters[1], filters[2])
            else -> throw IllegalStateException()
        }
    }
}

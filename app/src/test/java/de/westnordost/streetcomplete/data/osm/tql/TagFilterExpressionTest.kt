package de.westnordost.streetcomplete.data.osm.tql

import org.junit.Test

import de.westnordost.osmapi.map.data.Element
import de.westnordost.streetcomplete.any
import de.westnordost.streetcomplete.mock
import de.westnordost.streetcomplete.on

import org.junit.Assert.*

class TagFilterExpressionTest {
    // Tests for toOverpassQLString are in FiltersParserTest

    private val node = createElement(Element.Type.NODE)
    private val way = createElement(Element.Type.WAY)
    private val relation = createElement(Element.Type.RELATION)

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

    @Test fun `matches filter`() {
        val tagFilter: TagFilter = mock()
        val expr = TagFilterExpression(listOf(ElementsTypeFilter.NODES), Leaf(tagFilter))

        on(tagFilter.matches(any())).thenReturn(true)
        assertTrue(expr.matches(node))
        on(tagFilter.matches(any())).thenReturn(false)
        assertFalse(expr.matches(node))
    }

    private fun createElement(type: Element.Type): Element {
        val element: Element = mock()
        on(element.type).thenReturn(type)
        return element
    }

    private fun createMatchExpression(vararg elementsTypeFilter: ElementsTypeFilter): TagFilterExpression {
        val tagFilter: TagFilter = mock()
        on(tagFilter.matches(any())).thenReturn(true)
        return TagFilterExpression(elementsTypeFilter.asList(), Leaf(tagFilter))
    }
}

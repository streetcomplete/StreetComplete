package de.westnordost.streetcomplete.data.osm.tql

import org.junit.Test

import de.westnordost.osmapi.map.data.Element
import de.westnordost.streetcomplete.any
import de.westnordost.streetcomplete.on

import org.junit.Assert.*
import org.mockito.Mockito.mock

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

    @Test fun `matches elements`() {
        val booleanExpression = mock(BooleanExpression::class.java)
        on(booleanExpression.matches(any())).thenReturn(true)
        val expr = TagFilterExpression(
            ElementsTypeFilter.values().toList(),
	        booleanExpression as BooleanExpression<OQLExpressionValue>
        )

        assertTrue(expr.matches(node))
        assertTrue(expr.matches(way))
        assertTrue(expr.matches(relation))
    }

    private fun createElement(type: Element.Type): Element {
        val element = mock(Element::class.java)
	    on(element.type).thenReturn(type)
        return element
    }

    private fun createMatchExpression(elementsTypeFilter: ElementsTypeFilter): TagFilterExpression {
        val expr = mock(BooleanExpression::class.java)
	    on(expr.matches(any())).thenReturn(true)
        return TagFilterExpression(listOf(elementsTypeFilter),
	        expr as BooleanExpression<OQLExpressionValue>
        )
    }
}

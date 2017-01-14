package de.westnordost.streetcomplete.data.osm.tql;

import junit.framework.TestCase;

import java.util.Arrays;
import java.util.Collections;

import de.westnordost.osmapi.map.data.Element;

import static org.mockito.Mockito.*;

public class TagFilterExpressionTest extends TestCase
{
	// Tests for toOverpassQLString are in FiltersParserTest

	private Element node = createElement(Element.Type.NODE);
	private Element way = createElement(Element.Type.WAY);
	private Element relation = createElement(Element.Type.RELATION);

	public void testMatchesNodes()
	{
		TagFilterExpression expr = createMatchExpression(ElementsTypeFilter.NODES);

		assertTrue(expr.matches(node));
		assertFalse(expr.matches(way));
		assertFalse(expr.matches(relation));
	}

	public void testMatchesWays()
	{
		TagFilterExpression expr = createMatchExpression(ElementsTypeFilter.WAYS);

		assertFalse(expr.matches(node));
		assertTrue(expr.matches(way));
		assertFalse(expr.matches(relation));
	}

	public void testMatchesRelations()
	{
		TagFilterExpression expr = createMatchExpression(ElementsTypeFilter.RELATIONS);

		assertFalse(expr.matches(node));
		assertFalse(expr.matches(way));
		assertTrue(expr.matches(relation));
	}

	public void testMatchesElements()
	{
		BooleanExpression booleanExpression = mock(BooleanExpression.class);
		when(booleanExpression.matches(anyObject())).thenReturn(true);
		TagFilterExpression expr = new TagFilterExpression(
				Arrays.asList(ElementsTypeFilter.values()),
				booleanExpression);

		assertTrue(expr.matches(node));
		assertTrue(expr.matches(way));
		assertTrue(expr.matches(relation));
	}

	private Element createElement(Element.Type type)
	{
		Element element = mock(Element.class);
		when(element.getType()).thenReturn(type);
		return element;
	}

	private TagFilterExpression createMatchExpression(ElementsTypeFilter elementsTypeFilter)
	{
		BooleanExpression expr = mock(BooleanExpression.class);
		when(expr.matches(anyObject())).thenReturn(true);
		return new TagFilterExpression(Collections.singletonList(elementsTypeFilter), expr);
	}
}

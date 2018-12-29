package de.westnordost.streetcomplete.data.osm.tql;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import de.westnordost.osmapi.map.data.Element;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class TagFilterExpressionTest
{
	// Tests for toOverpassQLString are in FiltersParserTest

	private Element node = createElement(Element.Type.NODE);
	private Element way = createElement(Element.Type.WAY);
	private Element relation = createElement(Element.Type.RELATION);

	@Test public void matchesNodes()
	{
		TagFilterExpression expr = createMatchExpression(ElementsTypeFilter.NODES);

		assertTrue(expr.matches(node));
		assertFalse(expr.matches(way));
		assertFalse(expr.matches(relation));
	}

	@Test public void matchesWays()
	{
		TagFilterExpression expr = createMatchExpression(ElementsTypeFilter.WAYS);

		assertFalse(expr.matches(node));
		assertTrue(expr.matches(way));
		assertFalse(expr.matches(relation));
	}

	@Test public void matchesRelations()
	{
		TagFilterExpression expr = createMatchExpression(ElementsTypeFilter.RELATIONS);

		assertFalse(expr.matches(node));
		assertFalse(expr.matches(way));
		assertTrue(expr.matches(relation));
	}

	@Test public void matchesElements()
	{
		BooleanExpression booleanExpression = mock(BooleanExpression.class);
		when(booleanExpression.matches(any())).thenReturn(true);
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
		when(expr.matches(any())).thenReturn(true);
		return new TagFilterExpression(Collections.singletonList(elementsTypeFilter), expr);
	}
}

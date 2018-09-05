package de.westnordost.streetcomplete.data.osm.tql;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.westnordost.osmapi.map.data.BoundingBox;
import de.westnordost.osmapi.map.data.Element;

/** Represents a parse result of a string in filter syntax, i.e.
 *  <tt>"ways with (highway = residential or highway = tertiary) and !name"</tt> */
public class TagFilterExpression
{
	private final List<ElementsTypeFilter> elementsTypeFilters;
	private final BooleanExpression<OQLExpressionValue> tagExprRoot;

	public TagFilterExpression(List<ElementsTypeFilter> elementsTypeFilters,
							   BooleanExpression<OQLExpressionValue> tagExprRoot)
	{
		this.elementsTypeFilters = elementsTypeFilters;
		this.tagExprRoot = tagExprRoot;
	}

	/** @return whether the given element is found through (=matches) this expression */
	public boolean matches(Element element)
	{
		Element.Type eleType = element.getType();

		boolean elementTypeMatches = false;

		switch(eleType)
		{
			case NODE:
				elementTypeMatches = elementsTypeFilters.contains(ElementsTypeFilter.NODES);
				break;
			case WAY:
				elementTypeMatches = elementsTypeFilters.contains(ElementsTypeFilter.WAYS);
				break;
			case RELATION:
				elementTypeMatches = elementsTypeFilters.contains(ElementsTypeFilter.RELATIONS);
				break;
		}

		return elementTypeMatches && tagExprRoot.matches(element);
	}

	/** @return this expression as a Overpass query string (in a short one-liner form) */
	public String toOverpassQLString(BoundingBox bbox)
	{
		StringBuilder oql = new StringBuilder();
		if(bbox != null)
		{
			oql.append(OverpassQLUtil.getGlobalOverpassBBox(bbox));
		}

		BooleanExpression<OQLExpressionValue> expandedExpression = createExpandedExpression();

		List<String> elements = getTagFiltersOverpassList(expandedExpression);

		final boolean useUnion = elementsTypeFilters.size() > 1 || elements.size() > 1;

		if(useUnion) oql.append("(");
		for(ElementsTypeFilter filter : elementsTypeFilters)
		{
			oql.append(getTagFiltersOverpassString(filter, elements));
		}
		if(useUnion) oql.append(");");

		return oql.toString();
	}

	private BooleanExpression<OQLExpressionValue> createExpandedExpression()
	{
		BooleanExpression<OQLExpressionValue> result = tagExprRoot.copy();
		result.flatten();
		result.expand();
		return result;
	}

	private static String getTagFiltersOverpassString(ElementsTypeFilter elementType, List<String> elements)
	{
		StringBuilder oql = new StringBuilder();
		for(String element : elements)
		{
			oql.append(elementType.oqlName);
			oql.append(element);
		}
		return oql.toString();
	}

	private static List<String> getTagFiltersOverpassList(
			BooleanExpression<OQLExpressionValue> expandedExpression)
	{
		BooleanExpression<OQLExpressionValue> child = expandedExpression.getFirstChild();
		if(child == null) return Collections.singletonList(";");

		if(child.isOr())
		{
			return getUnionOverpassQueryString(child);
		}
		else if(child.isAnd() || child.isValue())
		{
			return Collections.singletonList(getSingleOverpassQueryString(child));
		}

		throw new RuntimeException("The boolean expression is not in the expected format");
	}

	private static List<String> getUnionOverpassQueryString(BooleanExpression<OQLExpressionValue> child)
	{
		List<String> result = new ArrayList<>();
		for(BooleanExpression<OQLExpressionValue> orChild : child.getChildren())
		{
			result.add(getSingleOverpassQueryString(orChild));
		}
		return result;
	}

	private static String getSingleOverpassQueryString(BooleanExpression<OQLExpressionValue> child)
	{
		if(child.isValue()) return child.getValue().toOverpassQLString() + ";";

		if(!child.isAnd())
			throw new RuntimeException("The boolean expression is not in the expected format");

		StringBuilder result = new StringBuilder();
		for(BooleanExpression<OQLExpressionValue> valueChild : child.getChildren())
		{
			result.append(valueChild.getValue().toOverpassQLString());
		}
		result.append(";");

		return result.toString();
	}
}

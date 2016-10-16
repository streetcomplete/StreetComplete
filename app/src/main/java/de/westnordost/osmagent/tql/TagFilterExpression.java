package de.westnordost.osmagent.tql;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.westnordost.osmapi.map.data.BoundingBox;
import de.westnordost.osmapi.map.data.Element;

/** Represents a parse result of a string in filter syntax, i.e.
 *  <tt>"ways with (highway = residential or highway = tertiary) and !name"</tt> */
public class TagFilterExpression
{
	private ElementsTypeFilter elementsTypeFilter;
	private BooleanExpression<OQLExpressionValue> tagExprRoot;

	public TagFilterExpression(ElementsTypeFilter elementsTypeFilter,
							   BooleanExpression<OQLExpressionValue> tagExprRoot)
	{
		this.elementsTypeFilter = elementsTypeFilter;
		this.tagExprRoot = tagExprRoot;
	}

	/** @return whether the given element is found through (=matches) this expression */
	public boolean matches(Element element)
	{
		Element.Type eleType = element.getType();

		if(elementsTypeFilter == ElementsTypeFilter.NODES && eleType != Element.Type.NODE)
			return false;
		if(elementsTypeFilter == ElementsTypeFilter.WAYS && eleType != Element.Type.WAY)
			return false;
		if(elementsTypeFilter == ElementsTypeFilter.RELATIONS && eleType != Element.Type.RELATION)
			return false;

		return tagExprRoot.matches(element);
	}

	/** @return this expression as a Overpass query string (in a short one-liner form) */
	public String toOverpassQLString(BoundingBox bbox)
	{
		StringBuilder oql = new StringBuilder();
		if(bbox != null)
		{
			oql.append(
					"[bbox:" +
							bbox.getMinLatitude() + "," + bbox.getMinLongitude() + "," +
							bbox.getMaxLatitude() + "," + bbox.getMaxLongitude() +
					"];");
		}

		BooleanExpression<OQLExpressionValue> exprWithBBox = createExpandedExpression();

		List<String> elements = getTagFiltersOverpassList(exprWithBBox);

		final boolean useUnion = elementsTypeFilter == ElementsTypeFilter.ELEMENTS || elements.size() > 1;

		if(useUnion) oql.append("(");
		if(elementsTypeFilter == ElementsTypeFilter.ELEMENTS)
		{
			for(ElementsTypeFilter elem : ElementsTypeFilter.OQL_VALUES)
			{
				oql.append(getTagFiltersOverpassString(elem, elements));
			}
		}
		else
		{
			oql.append(getTagFiltersOverpassString(elementsTypeFilter, elements));
		}
		if(useUnion) oql.append(");");

		/* "body" print mode (default) does not include version, but "meta" does. "geom" prints out
		 * geometry for every way and relation */
		oql.append("out meta geom;");
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

	private static List<String> getTagFiltersOverpassList(BooleanExpression<OQLExpressionValue> exprWithBBox)
	{
		BooleanExpression<OQLExpressionValue> child = exprWithBBox.getFirstChild();
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

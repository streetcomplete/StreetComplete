package de.westnordost.osmagent.data.osm;

import de.westnordost.osmagent.data.osm.tql.FiltersParser;
import de.westnordost.osmagent.data.osm.tql.TagFilterExpression;
import de.westnordost.osmapi.map.data.BoundingBox;
import de.westnordost.osmapi.map.data.Element;

public abstract class OverpassQuestType implements OsmElementQuestType
{
	protected TagFilterExpression filter;

	public OverpassQuestType()
	{
		filter = new FiltersParser().parse(getTagFilters());
	}

	/** @return a query string that is accepted by Overpass and does not exceed the given bbox */
	public String getOverpassQuery(BoundingBox bbox)
	{
		return filter.toOverpassQLString(bbox);
	}

	@Override public boolean appliesTo(Element element)
	{
		return filter.matches(element);
	}

	protected abstract String getTagFilters();
}

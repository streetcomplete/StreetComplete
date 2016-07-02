package de.westnordost.osmagent.quests.types;

import de.westnordost.osmagent.tql.FiltersParser;
import de.westnordost.osmagent.tql.TagFilterExpression;
import de.westnordost.osmapi.map.data.BoundingBox;
import de.westnordost.osmapi.map.data.Element;

public abstract class AbstractQuestType implements QuestType
{
	protected TagFilterExpression filter;

	public AbstractQuestType()
	{
		filter = new FiltersParser().parse(getTagFilters());
	}

	@Override
	public String getOverpassQuery(BoundingBox bbox)
	{
		return filter.toOverpassQLString(bbox);
	}

	@Override
	public boolean appliesTo(Element element)
	{
		return filter.matches(element);
	}

	protected abstract String getTagFilters();
}

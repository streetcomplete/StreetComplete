package de.westnordost.streetcomplete.data.complete;

import de.westnordost.osmapi.map.data.BoundingBox;
import de.westnordost.streetcomplete.data.osm.Countries;
import de.westnordost.streetcomplete.data.osm.download.MapDataWithGeometryHandler;
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao;
import de.westnordost.streetcomplete.data.osm.tql.FiltersParser;
import de.westnordost.streetcomplete.data.osm.tql.TagFilterExpression;

public abstract class SimpleOverpassCompleteQuestType extends CompleteQuestType
{
	private final OverpassMapDataDao overpassServer;

	private TagFilterExpression filter;

	public SimpleOverpassCompleteQuestType(OverpassMapDataDao overpassServer)
	{
		this.overpassServer = overpassServer;
		filter = new FiltersParser().parse(getTagFilters());
	}

	/** @return a query string that is accepted by Overpass and does not exceed the given bbox */
	String getOverpassQuery(BoundingBox bbox)
	{
		return filter.toOverpassQLString(bbox);
	}

	protected abstract String getTagFilters();

	public boolean download(BoundingBox bbox, MapDataWithGeometryHandler handler)
	{
		return overpassServer.getAndHandleQuota(getOverpassQuery(bbox), handler);
	}

	@Override public Countries getEnabledForCountries() { return Countries.ALL; }
}

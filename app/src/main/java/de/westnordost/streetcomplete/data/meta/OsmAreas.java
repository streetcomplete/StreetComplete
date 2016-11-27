package de.westnordost.streetcomplete.data.meta;

import java.util.List;

import de.westnordost.streetcomplete.data.osm.tql.FiltersParser;
import de.westnordost.streetcomplete.data.osm.tql.TagFilterExpression;
import de.westnordost.osmapi.map.data.Relation;
import de.westnordost.osmapi.map.data.Way;

/** Definitions which closed ways and which relations are areas */
public class OsmAreas
{
	private static final TagFilterExpression IS_AREA_EXPR = new FiltersParser().parse(
			" ways with area = yes or (" +
			" aeroway or amenity or boundary or building or craft or emergency or historic or" +
			" landuse or leisure or military or office or place or public_transport or shop or" +
			" tourism or" +
			" aerialway = station or railway = station or" +
			" power ~ compensator|converter|generator|plant|substation or" +
			" waterway ~ boatyard|dam|dock|riverbank|fuel" +
			")"
	);

	public static boolean isArea(Way way)
	{
		List<Long> nodeIds = way.getNodeIds();
		long firstNode = nodeIds.get(0);
		long lastNode = nodeIds.get(nodeIds.size() - 1);
		return firstNode == lastNode && IS_AREA_EXPR.matches(way);
	}

	public static boolean isArea(Relation relation)
	{
		if(relation.getTags() == null) return false;
		if(!relation.getTags().containsKey("type")) return false;
		return relation.getTags().get("type").equals("multipolygon");
	}
}

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
			" tourism or building:part or" +
			" aerialway = station or railway = station or" +
			" natural ~ " +
				"wood|scrub|heath|moor|grassland|fell|bare_rock|scree|shingle|sand|mud" +
				"water|wetland|glacier|beach|rock|sinkhole" +
			" or" +
			" man_made ~ " +
				"beacon|bridge|campanile|dolphin|lighthouse|obelisk|observatory|tower|" +
				"bunker_silo|chimney|gasometer|kiln|mineshaft|petroleum_well|silo|storage_tank|watermill|windmill|works|" +
				"communications_tower|monitoring_station|street_cabinet|" +
				"pumping_station|reservoir_covered|wastewater_plant|water_tank|water_tower|water_well|water_works" +
			" or" +
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

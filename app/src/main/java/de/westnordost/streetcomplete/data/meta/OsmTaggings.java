package de.westnordost.streetcomplete.data.meta;

/** Definitions/meanings of certain OSM taggings */
public class OsmTaggings
{
	public static final String[] ANYTHING_UNPAVED = {
			"unpaved","compacted","gravel","fine_gravel","pebblestone","grass_paver",
			"ground","earth","dirt","grass","sand","mud","ice","salt","snow","woodchips"
	};

	public static final String[] ANYTHING_PAVED = {
			"paved", "asphalt", "cobblestone", "cobblestone:flattened", "sett",
			"concrete", "concrete:lanes", "concrete:plates", "paving_stones",
			"metal", "wood", "unhewn_cobblestone"
	};

	public static final String[] ALL_ROADS = {
		"motorway", "motorway_link", "trunk", "trunk_link", "primary", "primary_link",
		"secondary", "secondary_link", "tertiary", "tertiary_link",
		"unclassified", "residential", "living_street", "pedestrian",
		"service", "track", "road",
	};

	public static final String SURVEY_MARK_KEY = "check_date";
}

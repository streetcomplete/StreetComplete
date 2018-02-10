package de.westnordost.streetcomplete.data.meta;

/** Definitions/meanings of certain OSM taggings */
public class OsmTaggings
{
	public static final String[] ANYTHING_UNPAVED = {
			"unpaved","compacted","gravel","fine_gravel","pebblestone","grass_paver",
			"ground","earth","dirt","grass","sand","mud","ice","salt","snow","woodchips"
	};

	public static final String[] ALL_ROADS = {
		"motorway", "motorway_link", "trunk", "trunk_link", "primary", "primary_link",
		"secondary", "secondary_link", "tertiary", "tertiary_link",
		"unclassified", "residential", "living_street", "pedestrian",
		"service", "track", "road",
	};

	// listed on https://wiki.openstreetmap.org/wiki/Key:access and used more than 5k times
	public static final String[] POPULAR_ROAD_ACCESS_TAGS = {
		"access", "foot", "vehicle", "bicycle", "carriage", "motor_vehicle", "motorcycle",
		"mofa", "moped", "motorcar", "tourist_bus", "goods", "hgv", "agricultural", "snowmobile",
		"psv", "hov", "emergency", "hazmat", "horse", "taxi",
	};

	public static final String SURVEY_MARK_KEY = "check_date";
}

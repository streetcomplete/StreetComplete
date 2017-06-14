package de.westnordost.streetcomplete.quests.road_surface;

public class RoadSurfaceConfig {
	// well, all roads have surfaces, what I mean is that not all ways with highway key are
	// "something with a surface"
	static final String[] ROADS_WITH_SURFACES = {
			// "trunk","trunk_link","motorway","motorway_link", // too much, motorways are almost by definition asphalt (or concrete)
			"primary", "primary_link", "secondary", "secondary_link", "tertiary", "tertiary_link",
			"unclassified", "residential", "bicycle_road", "living_street", "pedestrian",
			"track", "road",
			/*"service", */ // this is too much, and the information value is very low
	};
}

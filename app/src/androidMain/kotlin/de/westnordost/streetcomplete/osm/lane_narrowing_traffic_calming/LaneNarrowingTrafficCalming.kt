package de.westnordost.streetcomplete.osm.lane_narrowing_traffic_calming

/** Other than the TrafficCalmingType, this enum just concerns itself with how the road is narrowed,
 *  hence it is somewhat more difficult to parse and put together again into OSM tags */
enum class LaneNarrowingTrafficCalming {
    CHOKER,
    ISLAND,
    CHICANE,
    CHOKED_ISLAND
}

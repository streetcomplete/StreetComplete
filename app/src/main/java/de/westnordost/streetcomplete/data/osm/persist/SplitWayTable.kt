package de.westnordost.streetcomplete.data.osm.persist

object SplitWayTable {
    const val NAME = "osm_split_ways"
    const val INDEX_NAME ="osm_split_ways_index"

    object Columns {
        const val WAY_ID = "id"
        const val NODE1_ID = "node1_id"
        const val NODE1_VERSION = "node1_version"
        const val NODE1_LAT = "node1_lat"
        const val NODE1_LON = "node1_lon"
        const val NODE2_ID = "node2_id"
        const val NODE2_VERSION = "node2_version"
        const val NODE2_LAT = "node2_lat"
        const val NODE2_LON = "node2_lon"
        const val DELTA = "delta"
    }
}

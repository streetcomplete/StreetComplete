package de.westnordost.streetcomplete.data.osm.mapdata

/** Controller to access element data and its geometry and handle updates to it (from OSM API) */
interface MapDataController : MapDataSource {
    /** update element data with [mapData] in the given [bbox] (fresh data from the OSM API has been
     *  downloaded) */
    fun putAllForBBox(bbox: BoundingBox, mapData: MutableMapData)

    /** incorporate the [mapDataUpdates] (data has been updated after upload) */
    fun updateAll(mapDataUpdates: MapDataUpdates)

    fun deleteOlderThan(timestamp: Long, limit: Int? = null): Int

    fun clear()

    fun trimCache()

    fun clearCache()
}

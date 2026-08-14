package de.westnordost.streetcomplete.data.osm.mapdata

import de.westnordost.streetcomplete.data.osm.created_elements.CreatedElementsController
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometryCreator
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometryDao
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometryEntry
import de.westnordost.streetcomplete.util.Listeners
import de.westnordost.streetcomplete.util.ktx.format
import de.westnordost.streetcomplete.util.ktx.nowAsEpochMilliseconds
import de.westnordost.streetcomplete.util.logs.Log
import kotlinx.atomicfu.locks.ReentrantLock
import kotlinx.atomicfu.locks.withLock

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

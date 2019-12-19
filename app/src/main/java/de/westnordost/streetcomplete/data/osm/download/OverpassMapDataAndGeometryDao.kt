package de.westnordost.streetcomplete.data.osm.download

import android.util.Log
import de.westnordost.osmapi.map.data.*
import de.westnordost.osmapi.overpass.MapDataWithGeometryHandler
import de.westnordost.osmapi.overpass.OsmTooManyRequestsException
import de.westnordost.osmapi.overpass.OverpassMapDataDao
import de.westnordost.streetcomplete.data.osm.ElementGeometry
import javax.inject.Inject

class OverpassMapDataAndGeometryDao @Inject constructor(
    private val dao: OverpassMapDataDao,
    private val elementGeometryCreator: ElementGeometryCreator
) {

    /** Get data from Overpass assuming a "out meta geom;" query and handles automatically waiting
     * for the request quota to replenish.
     *
     * @param query Query string. Either Overpass QL or Overpass XML query string
     * @param callback map data callback that is fed the map data and geometry
     * @return false if it was interrupted while waiting for the quota to be replenished
     *
     * @throws OsmBadUserInputException if there is an error if the query
     */
    fun query(query: String, callback: (element: Element, geometry: ElementGeometry?) -> Unit): Boolean {

        val handler = object : MapDataWithGeometryHandler {
            override fun handle(bounds: BoundingBox) {}

            override fun handle(node: Node) {
                callback(node, elementGeometryCreator.create(node))
            }

            override fun handle(way: Way, bounds: BoundingBox, geometry: MutableList<LatLon>) {
                callback(way, elementGeometryCreator.create(way, geometry))
            }

            override fun handle(
                relation: Relation,
                bounds: BoundingBox,
                nodeGeometries: MutableMap<Long, LatLon>,
                wayGeometries: MutableMap<Long, MutableList<LatLon>>
            ) {
                callback(relation, elementGeometryCreator.create(relation, wayGeometries))
            }
        }

        try {
            dao.queryElementsWithGeometry(query, handler)
        } catch (e: OsmTooManyRequestsException) {
            val status = dao.getStatus()
            if (status.availableSlots == 0) {
                // apparently sometimes Overpass does not tell the client when the next slot is
                // available when there is currently no slot available. So let's just wait 60s
                // before trying again
                // also, rather wait 1s longer than required cause we only get the time in seconds
                val nextAvailableSlotIn = status.nextAvailableSlotIn
                val waitInSeconds = if (nextAvailableSlotIn != null) nextAvailableSlotIn + 1 else 60
                Log.i(TAG, "Hit Overpass quota. Waiting ${waitInSeconds}s before continuing")
                try {
                    Thread.sleep(waitInSeconds * 1000L)
                } catch (ie: InterruptedException) {
                    Log.d(TAG, "Thread interrupted while waiting for Overpass quota to be replenished")
                    return false
                }
            }
            return query(query, callback)
        }
        return true
    }

    companion object {
        private const val TAG = "OverpassMapDataGeomDao"
    }
}

package de.westnordost.streetcomplete.data.osm.osmquest

import de.westnordost.osmapi.common.errors.OsmQueryTooBigException
import de.westnordost.osmapi.map.*
import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.Element.Type.*
import de.westnordost.osmapi.map.data.OsmLatLon
import de.westnordost.streetcomplete.data.MapDataApi
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementGeometryCreator
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementPointGeometry
import javax.inject.Inject

// TODO TEST

// TODO thread safety! - should be safe if it is going to be accessed by a thread pool (which makes sense)
/** MapDataWithGeometry that fetches the necessary data from the OSM API itself */
class OsmApiMapData @Inject constructor(
    private val mapDataApi: MapDataApi,
    private val elementGeometryCreator: ElementGeometryCreator
) : MutableMapData(), MapDataWithGeometry {

    private val nodeGeometriesById: MutableMap<Long, ElementPointGeometry?> = mutableMapOf()
    private val wayGeometriesById: MutableMap<Long, ElementGeometry?> = mutableMapOf()
    private val relationGeometriesById: MutableMap<Long, ElementGeometry?> = mutableMapOf()

    /* caching which ways and relations are complete so they do not need to be checked every time
       they are accessed */
    private val completeWays: MutableSet<Long> = mutableSetOf()
    private val completeRelations: MutableSet<Long> = mutableSetOf()

    /* overridden because the publicly accessible bounding box should be the bbox of the initial
       download, not of any consecutive ones */
    override var boundingBox: BoundingBox? = null

    fun initWith(boundingBox: BoundingBox) {
        check(this.boundingBox == null)
        this.boundingBox = boundingBox

        getMapAndHandleTooBigQuery(boundingBox)
        // we know that all ways included in the initial download are complete
        completeWays.addAll(waysById.keys)
    }

    private fun getMapAndHandleTooBigQuery(bounds: BoundingBox) {
        try {
            mapDataApi.getMap(bounds, this)
        } catch (e : OsmQueryTooBigException) {
            for (subBounds in bounds.splitIntoFour()) {
                getMapAndHandleTooBigQuery(subBounds)
            }
        }
    }

    override fun getNodeGeometry(id: Long): ElementPointGeometry? {
        if (!nodesById.containsKey(id)) return null
        return nodeGeometriesById.getOrPut(id) {
            elementGeometryCreator.create(nodesById.getValue(id))
        }
    }

    override fun getWayGeometry(id: Long): ElementGeometry? {
        if (!waysById.containsKey(id)) return null
        return wayGeometriesById.getOrPut(id) {
            ensureWayIsComplete(id)
            elementGeometryCreator.create(waysById.getValue(id), this)
        }
    }

    override fun getRelationGeometry(id: Long): ElementGeometry? {
        if (!relationsById.containsKey(id)) return null
        return relationGeometriesById.getOrPut(id) {
            ensureRelationIsComplete(id)
            elementGeometryCreator.create(relationsById.getValue(id), this)
        }
    }

    private fun ensureRelationIsComplete(id: Long) {
        /* conditionally need to fetch from OSM API here because the nodes and ways of relations
           are not included in the normal map download call if not all are in the bbox */
        if (!completeRelations.contains(id)) {
            if (!isRelationComplete(id)) mapDataApi.getRelationComplete(id, this)
            completeRelations.add(id)
        }
    }

    private fun ensureWayIsComplete(id: Long) {
        /* conditionally need to fetch additional data from OSM API here */
        if (!completeWays.contains(id)) {
            if (!isWayComplete(id)) mapDataApi.getWayComplete(id, this)
            completeWays.add(id)
        }
    }

    private fun isRelationComplete(id: Long): Boolean =
        relationsById.getValue(id).members.all {
            when (it.type!!) {
                NODE -> nodesById.containsKey(it.ref)
                WAY -> waysById.containsKey(it.ref) && isWayComplete(it.ref)
                /* not being recursive here is deliberate. sub-relations are considered not relevant
                   for the element geometry in StreetComplete */
                RELATION -> relationsById.containsKey(it.ref)
            }
        }

    private fun isWayComplete(id: Long): Boolean =
        waysById.getValue(id).nodeIds.all { nodesById.containsKey(it) }

}

private fun BoundingBox.splitIntoFour(): List<BoundingBox> {
    val center = OsmLatLon((maxLatitude + minLatitude) / 2, (maxLongitude + minLongitude) / 2)
    return listOf(
        BoundingBox(minLatitude,     minLongitude,     center.latitude, center.longitude),
        BoundingBox(minLatitude,     center.longitude, center.latitude, maxLongitude),
        BoundingBox(center.latitude, minLongitude,     maxLatitude,     center.longitude),
        BoundingBox(center.latitude, center.longitude, maxLatitude,     maxLongitude)
    )
}
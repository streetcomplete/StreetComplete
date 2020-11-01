package de.westnordost.streetcomplete.data.osm.osmquest

import de.westnordost.osmapi.map.MapDataWithGeometry
import de.westnordost.osmapi.map.MutableMapData
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementGeometryCreator
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementPointGeometry
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

/** MapDataWithGeometry that lazily creates the element geometry. Will create incomplete (relation)
 *  geometry */
class CachingMapDataWithGeometry @Inject constructor(
    private val elementGeometryCreator: ElementGeometryCreator
) : MutableMapData(), MapDataWithGeometry {

    private val nodeGeometriesById: ConcurrentHashMap<Long, ElementPointGeometry?> = ConcurrentHashMap()
    private val wayGeometriesById: ConcurrentHashMap<Long, ElementGeometry?> = ConcurrentHashMap()
    private val relationGeometriesById: ConcurrentHashMap<Long, ElementGeometry?> = ConcurrentHashMap()

    override fun getNodeGeometry(id: Long): ElementPointGeometry? {
        val node = nodesById[id] ?: return null
        return nodeGeometriesById.getOrPut(id, { elementGeometryCreator.create(node) })
    }

    override fun getWayGeometry(id: Long): ElementGeometry? {
        val way = waysById[id] ?: return null
        return wayGeometriesById.getOrPut(id, { elementGeometryCreator.create(way, this, true) })
    }

    override fun getRelationGeometry(id: Long): ElementGeometry? {
        val relation = relationsById[id] ?: return null
        return relationGeometriesById.getOrPut(id, { elementGeometryCreator.create(relation, this, true) })
    }
}
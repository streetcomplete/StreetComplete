package de.westnordost.streetcomplete.data.osm.osmquest

import de.westnordost.osmapi.map.MapDataWithGeometry
import de.westnordost.osmapi.map.MutableMapData
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementGeometryCreator
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementPointGeometry
import javax.inject.Inject

/** MapDataWithGeometry that lazily creates the element geometry. Will create incomplete (relation)
 *  geometry */
class CachingMapDataWithGeometry @Inject constructor(
    private val elementGeometryCreator: ElementGeometryCreator
) : MutableMapData(), MapDataWithGeometry {

    private val nodeGeometriesById: MutableMap<Long, ElementPointGeometry?> = mutableMapOf()
    private val wayGeometriesById: MutableMap<Long, ElementGeometry?> = mutableMapOf()
    private val relationGeometriesById: MutableMap<Long, ElementGeometry?> = mutableMapOf()

    override fun getNodeGeometry(id: Long): ElementPointGeometry? {
        val node = nodesById[id] ?: return null
        if (!nodeGeometriesById.containsKey(id)) {
            synchronized(nodeGeometriesById) {
                if (!nodeGeometriesById.containsKey(id)) {
                    nodeGeometriesById[id] = elementGeometryCreator.create(node)
                }
            }
        }
        return nodeGeometriesById[id]
    }

    override fun getWayGeometry(id: Long): ElementGeometry? {
        val way = waysById[id] ?: return null
        if (!wayGeometriesById.containsKey(id)) {
            synchronized(wayGeometriesById) {
                if (!wayGeometriesById.containsKey(id)) {
                    wayGeometriesById[id] = elementGeometryCreator.create(way, this, true)
                }
            }
        }
        return wayGeometriesById[id]
    }

    override fun getRelationGeometry(id: Long): ElementGeometry? {
        val relation = relationsById[id] ?: return null
        if (!relationGeometriesById.containsKey(id)) {
            synchronized(relationGeometriesById) {
                if (!relationGeometriesById.containsKey(id)) {
                    relationGeometriesById[id] = elementGeometryCreator.create(relation, this, true)
                }
            }
        }
        return relationGeometriesById[id]
    }
}
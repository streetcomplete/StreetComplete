package de.westnordost.streetcomplete.data.osm.download

import android.util.LongSparseArray
import de.westnordost.osmapi.common.errors.OsmNotFoundException

import de.westnordost.osmapi.map.MapDataDao
import de.westnordost.osmapi.map.data.*
import de.westnordost.osmapi.map.handler.MapDataHandler
import de.westnordost.streetcomplete.data.osm.ElementGeometry
import javax.inject.Inject

/** Creates the geometry for an element by fetching the necessary geometry data from the OSM API */
class OsmApiElementGeometryCreator @Inject constructor(
    private val osmDao: MapDataDao,
    private val elementCreator: ElementGeometryCreator) {

    fun create(element: Element): ElementGeometry? {
        when(element) {
            is Node -> {
                return elementCreator.create(element)
            }
            is Way -> {
                val positions = getNodePositions(element.id) ?: return null
                return elementCreator.create(element, positions)
            }
            is Relation -> {
                val wayMembers = element.members.filter { it.type == Element.Type.WAY }
                val wayGeometries = mutableMapOf<Long, List<LatLon>>()
                for (member in wayMembers) {
                    val positions = getNodePositions(member.ref) ?: return null
                    wayGeometries[member.ref] = positions
                }
                return elementCreator.create(element, wayGeometries)
            }
            else -> return null
        }
    }

    private fun getNodePositions(wayId: Long): List<LatLon>? {
        lateinit var way: Way
        val nodes = LongSparseArray<Node>()
        val handler = object : MapDataHandler {
            override fun handle(b: BoundingBox) {}
            override fun handle(n: Node) { nodes.put(n.id, n) }
            override fun handle(w: Way) { way = w }
            override fun handle(r: Relation) {}
        }

        try {
            osmDao.getWayComplete(wayId, handler)
        } catch (e : OsmNotFoundException) {
            return null
        }

        return way.nodeIds.map { nodes[it].position }
    }
}

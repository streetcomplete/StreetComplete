package de.westnordost.streetcomplete.data.osm.download

import de.westnordost.osmapi.common.errors.OsmNotFoundException

import de.westnordost.osmapi.map.MapDataDao
import de.westnordost.osmapi.map.data.*
import de.westnordost.osmapi.map.handler.DefaultMapDataHandler
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
                val positionsByWayId = getWaysNodePositions(element.id) ?: return null
                return elementCreator.create(element, positionsByWayId)
            }
            else -> return null
        }
    }

    private fun getNodePositions(wayId: Long): List<LatLon>? {
        var way: Way? = null
        val nodes = mutableMapOf<Long, Node>()
        val handler = object : DefaultMapDataHandler() {
            override fun handle(n: Node) { nodes[n.id] = n }
            override fun handle(w: Way) { way = w }
        }

        try {
            osmDao.getWayComplete(wayId, handler)
        } catch (e : OsmNotFoundException) {
            return null
        }
        val wayNodeIds = way?.nodeIds ?: return null
        return wayNodeIds.map { nodeId ->
            val node = nodes[nodeId] ?: return null
            node.position
        }
    }

    private fun getWaysNodePositions(relationId: Long): Map<Long, List<LatLon>>? {
        val nodes = mutableMapOf<Long, Node>()
        val ways = mutableMapOf<Long, Way>()
        var relation: Relation? = null

        val handler = object : DefaultMapDataHandler() {
            override fun handle(n: Node) { nodes[n.id] = n }
            override fun handle(w: Way) { ways[w.id] = w }
            override fun handle(r: Relation) { relation = r }
        }

        try {
            osmDao.getRelationComplete(relationId, handler)
        } catch (e : OsmNotFoundException) {
            return null
        }

        val members = relation?.members ?: return null
        val wayMembers = members.filter { it.type == Element.Type.WAY }
        return wayMembers.associate { wayMember ->
            val way = ways[wayMember.ref] ?: return null
            val wayPositions = way.nodeIds.map { nodeId ->
                val node = nodes[nodeId] ?: return null
                node.position
            }
            way.id to wayPositions
        }
    }
}

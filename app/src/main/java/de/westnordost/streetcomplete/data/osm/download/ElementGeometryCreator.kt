package de.westnordost.streetcomplete.data.osm.download

import de.westnordost.osmapi.map.data.*
import de.westnordost.streetcomplete.data.meta.OsmAreas
import de.westnordost.streetcomplete.data.osm.ElementGeometry
import de.westnordost.streetcomplete.data.osm.ElementPointGeometry
import de.westnordost.streetcomplete.data.osm.ElementPolygonsGeometry
import de.westnordost.streetcomplete.data.osm.ElementPolylinesGeometry
import de.westnordost.streetcomplete.util.SphericalEarthMath.*
import kotlin.collections.ArrayList

class ElementGeometryCreator(private val wayGeometrySource: WayGeometrySource) {

    fun create(element: Element): ElementGeometry? = when(element) {
        is Node -> create(element)
        is Way -> create(element)
        is Relation -> create(element)
        else -> throw IllegalArgumentException()
    }

    fun create(node: Node) = ElementPointGeometry(node.position)

    fun create(way: Way): ElementGeometry? {
        val nodePositions = wayGeometrySource.getNodePositions(way.id) ?: return null
        val polyline = ArrayList(nodePositions)
        polyline.eliminateDuplicates()
        if (polyline.size < 2) return null

        return if (OsmAreas.isArea(way)) {
            /* ElementGeometry considers polygons that are defined clockwise holes, so ensure that
               it is defined CCW here. */
            if (isRingDefinedClockwise(polyline)) polyline.reverse()
            ElementPolygonsGeometry(arrayListOf(polyline), centerPointOfPolygon(polyline))
        } else {
            ElementPolylinesGeometry(arrayListOf(polyline), centerPointOfPolyline(polyline))
        }
    }

    fun create(relation: Relation): ElementGeometry? {
        return if (OsmAreas.isArea(relation)) {
            createMultipolygonGeometry(relation)
        } else {
            createPolylinesGeometry(relation)
        }
    }

    private fun createMultipolygonGeometry(relation: Relation): ElementPolygonsGeometry? {
        val outer = createNormalizedRingGeometry(relation, "outer", false)
        val inner = createNormalizedRingGeometry(relation, "inner", true)
        if (outer.isEmpty()) return null

        val rings = ArrayList<ArrayList<LatLon>>()
        rings.addAll(outer)
        rings.addAll(inner)

        /* only use first ring that is not a hole if there are multiple
           this is the same behavior as Leaflet or Tangram */
        return ElementPolygonsGeometry(rings, centerPointOfPolygon(outer.first()))
    }

    private fun createPolylinesGeometry(relation: Relation): ElementPolylinesGeometry? {
        val waysNodePositions = getRelationMemberWaysNodePositions(relation)
        val joined = waysNodePositions.joined()

        val polylines = joined.ways
        polylines.addAll(joined.rings)
        if (polylines.isEmpty()) return null

        /* if there are more than one polylines, these polylines are not connected to each other,
           so there is no way to find a reasonable "center point". In most cases however, there
           is only one polyline, so let's just take the first one...
           This is the same behavior as Leaflet or Tangram */
        return ElementPolylinesGeometry(polylines, centerPointOfPolyline(polylines.first()))
    }

    private fun createNormalizedRingGeometry(relation: Relation, role: String, clockwise: Boolean): ArrayList<ArrayList<LatLon>> {
        val waysNodePositions = getRelationMemberWaysNodePositions(relation, role)
        val ringGeometry = waysNodePositions.joined().rings
        ringGeometry.setOrientation(clockwise)
        return ringGeometry
    }

    private fun getRelationMemberWaysNodePositions(relation: Relation): List<List<LatLon>> {
        return relation.members.filter { it.type == Element.Type.WAY }.mapNotNull {
            getValidNodePositions(it.ref)
        }
    }

    private fun getRelationMemberWaysNodePositions(relation: Relation, withRole: String): List<List<LatLon>> {
        return relation.members.filter { it.type == Element.Type.WAY && it.role == withRole }.mapNotNull {
            getValidNodePositions(it.ref)
        }
    }

    private fun getValidNodePositions(wayId: Long): List<LatLon>? {
        val nodePositions = wayGeometrySource.getNodePositions(wayId) ?: return null
        nodePositions.eliminateDuplicates()
        return if (nodePositions.size >= 2) nodePositions else null
    }
}

/** Ensures that all given rings are defined in clockwise/counter-clockwise direction  */
private fun List<MutableList<LatLon>>.setOrientation(clockwise: Boolean) {
    for (ring in this) {
        if (isRingDefinedClockwise(ring) != clockwise) {
            ring.reverse()
        }
    }
}

private fun List<LatLon>.isRing() = first() == last()

private class ConnectedWays(val rings: ArrayList<ArrayList<LatLon>>, val ways: ArrayList<ArrayList<LatLon>>)

/** Returns a list of polylines joined together at their endpoints into rings and ways */
private fun List<List<LatLon>>.joined(): ConnectedWays {
    val nodeWayMap = NodeWayMap(this)

    val rings: ArrayList<ArrayList<LatLon>> = ArrayList()
    val ways: ArrayList<ArrayList<LatLon>> = ArrayList()

    var currentWay: ArrayList<LatLon> = ArrayList()

    while (nodeWayMap.hasNextNode()) {
        val node: LatLon = if (currentWay.isEmpty()) nodeWayMap.getNextNode() else currentWay.last()

        val waysAtNode = nodeWayMap.getWaysAtNode(node)
        if (waysAtNode == null) {
            ways.add(currentWay)
            currentWay = ArrayList()
        } else {
            val way = waysAtNode.first()

            currentWay.join(way)
            nodeWayMap.removeWay(way)

            // finish ring and start new one
            if (currentWay.isRing()) {
                rings.add(currentWay)
                currentWay = ArrayList()
            }
        }
    }
    if (currentWay.isNotEmpty()) {
        ways.add(currentWay)
    }

    return ConnectedWays(rings, ways)
}

/** Join the given adjacent polyline into this polyline */
private fun MutableList<LatLon>.join(way: List<LatLon>) {
    if (isEmpty()) {
        addAll(way)
    } else {
        when {
            last() == way.last() -> addAll(way.asReversed().subList(1, way.size))
            last() == way.first() -> addAll(way.subList(1, way.size))
            first() == way.last() -> addAll(0, way.subList(0, way.size - 1))
            first() == way.first() -> addAll(0, way.asReversed().subList(0, way.size - 1))
            else -> throw IllegalArgumentException("The ways are not adjacent")
        }
    }
}

private fun MutableList<LatLon>.eliminateDuplicates() {
    val it = iterator()
    var prev: LatLon? = null
    while (it.hasNext()) {
        val line = it.next()
        if (prev == null || line.latitude != prev.latitude || line.longitude != prev.longitude) {
            prev = line
        } else {
            it.remove()
        }
    }
}

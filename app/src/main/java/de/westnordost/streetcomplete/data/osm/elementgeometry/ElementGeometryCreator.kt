package de.westnordost.streetcomplete.data.osm.elementgeometry

import de.westnordost.osmapi.map.data.*
import de.westnordost.streetcomplete.ktx.isArea
import de.westnordost.streetcomplete.util.centerPointOfPolygon
import de.westnordost.streetcomplete.util.centerPointOfPolyline
import de.westnordost.streetcomplete.util.isRingDefinedClockwise
import javax.inject.Inject
import kotlin.collections.ArrayList

/** Creates an ElementGeometry from an element and a collection of positions. */
class ElementGeometryCreator @Inject constructor() {

    /** Create an ElementPointGeometry for a node. */
    fun create(node: Node) = ElementPointGeometry(node.position)

    /**
     * Create an ElementGeometry for a way
     *
     * @param way the way to create the geometry for
     * @param wayGeometry the geometry of the way: A list of positions of its nodes.
     *
     * @return an ElementPolygonsGeometry if the way is an area or an ElementPolylinesGeometry
     *          if the way is a linear feature */
    fun create(way: Way, wayGeometry: List<LatLon>): ElementGeometry? {
        val polyline = ArrayList(wayGeometry)
        polyline.eliminateDuplicates()
        if (wayGeometry.size < 2) return null

        return if (way.isArea()) {
            /* ElementGeometry considers polygons that are defined clockwise holes, so ensure that
               it is defined CCW here. */
            if (polyline.isRingDefinedClockwise()) polyline.reverse()
            ElementPolygonsGeometry(arrayListOf(polyline), polyline.centerPointOfPolygon())
        } else {
            ElementPolylinesGeometry(arrayListOf(polyline), polyline.centerPointOfPolyline())
        }
    }

    /**
     * Create an ElementGeometry for a relation
     *
     * @param relation the relation to create the geometry for
     * @param wayGeometries the geometries of the ways that are members of the relation. It is a
     *                      map of way ids to a list of positions.
     *
     * @return an ElementPolygonsGeometry if the relation describes an area or an
     *         ElementPolylinesGeometry if it describes is a linear feature */
    fun create(relation: Relation, wayGeometries: Map<Long, List<LatLon>>): ElementGeometry? {
        return if (relation.isArea()) {
            createMultipolygonGeometry(relation, wayGeometries)
        } else {
            createPolylinesGeometry(relation, wayGeometries)
        }
    }

    private fun createMultipolygonGeometry(
        relation: Relation,
        wayGeometries: Map<Long, List<LatLon>>
    ): ElementPolygonsGeometry? {

        val outer = createNormalizedRingGeometry(relation, "outer", false, wayGeometries)
        val inner = createNormalizedRingGeometry(relation, "inner", true, wayGeometries)
        if (outer.isEmpty()) return null

        val rings = ArrayList<ArrayList<LatLon>>()
        rings.addAll(outer)
        rings.addAll(inner)

        /* only use first ring that is not a hole if there are multiple
           this is the same behavior as Leaflet or Tangram */
        return ElementPolygonsGeometry(rings, outer.first().centerPointOfPolygon())
    }

    private fun createPolylinesGeometry(
        relation: Relation,
        wayGeometries: Map<Long, List<LatLon>>
    ): ElementPolylinesGeometry? {

        val waysNodePositions = getRelationMemberWaysNodePositions(relation, wayGeometries)
        val joined = waysNodePositions.joined()

        val polylines = joined.ways
        polylines.addAll(joined.rings)
        if (polylines.isEmpty()) return null

        /* if there are more than one polylines, these polylines are not connected to each other,
           so there is no way to find a reasonable "center point". In most cases however, there
           is only one polyline, so let's just take the first one...
           This is the same behavior as Leaflet or Tangram */
        return ElementPolylinesGeometry(polylines, polylines.first().centerPointOfPolyline())
    }

    private fun createNormalizedRingGeometry(
        relation: Relation,
        role: String,
        clockwise: Boolean,
        wayGeometries: Map<Long, List<LatLon>>
    ): ArrayList<ArrayList<LatLon>> {

        val waysNodePositions = getRelationMemberWaysNodePositions(relation, role, wayGeometries)
        val ringGeometry = waysNodePositions.joined().rings
        ringGeometry.setOrientation(clockwise)
        return ringGeometry
    }

    private fun getRelationMemberWaysNodePositions(
        relation: Relation, wayGeometries: Map<Long, List<LatLon>>
    ): List<List<LatLon>> {
        return relation.members.filter { it.type == Element.Type.WAY }.mapNotNull {
            getValidNodePositions(wayGeometries[it.ref])
        }
    }

    private fun getRelationMemberWaysNodePositions(
        relation: Relation, withRole: String, wayGeometries: Map<Long, List<LatLon>>
    ): List<List<LatLon>> {
        return relation.members.filter { it.type == Element.Type.WAY && it.role == withRole }.mapNotNull {
            getValidNodePositions(wayGeometries[it.ref])
        }
    }

    private fun getValidNodePositions(wayGeometry: List<LatLon>?): List<LatLon>? {
        if (wayGeometry == null) return null
        val nodePositions = ArrayList(wayGeometry)
        nodePositions.eliminateDuplicates()
        return if (nodePositions.size >= 2) nodePositions else null
    }
}

/** Ensures that all given rings are defined in clockwise/counter-clockwise direction  */
private fun List<MutableList<LatLon>>.setOrientation(clockwise: Boolean) {
    for (ring in this) {
        if (ring.isRingDefinedClockwise() != clockwise) {
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

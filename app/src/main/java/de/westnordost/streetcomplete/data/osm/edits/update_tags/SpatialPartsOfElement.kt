package de.westnordost.streetcomplete.data.osm.edits.update_tags

import de.westnordost.osmapi.map.data.*
import de.westnordost.streetcomplete.util.distanceTo

/** Only the parts of an element that are used to determine the geometry */
sealed class SpatialPartsOfElement
data class SpatialPartsOfNode(val position: OsmLatLon) : SpatialPartsOfElement()
data class SpatialPartsOfWay(val nodeIds: ArrayList<Long>) : SpatialPartsOfElement()
data class SpatialPartsOfRelation(val members: ArrayList<RelationMember>) : SpatialPartsOfElement()

internal fun isGeometrySubstantiallyDifferent(element: SpatialPartsOfElement, newElement: Element) =
    when (element) {
        is SpatialPartsOfNode -> isNodeGeometrySubstantiallyDifferent(element, newElement as Node)
        is SpatialPartsOfWay -> isWayGeometrySubstantiallyDifferent(element, newElement as Way)
        is SpatialPartsOfRelation -> isRelationGeometrySubstantiallyDifferent(element, newElement as Relation)
    }

private fun isNodeGeometrySubstantiallyDifferent(node: SpatialPartsOfNode, newNode: Node) =
    /* Moving the node a distance beyond what would pass as adjusting the position within a
       building counts as substantial change. Also, the maximum distance should be not (much)
       bigger than the usual GPS inaccuracy in the city. */
    node.position.distanceTo(newNode.position) > 20

private fun isWayGeometrySubstantiallyDifferent(way: SpatialPartsOfWay, newWay: Way) =
    /* if the first or last node is different, it means that the way has either been extended or
       shortened at one end, which is counted as being substantial:
       If for example the surveyor has been asked to determine something for a certain way
       and this way is now longer, his answer does not apply to the whole way anymore, so that
       is an unsolvable conflict. */
    way.nodeIds.firstOrNull() != newWay.nodeIds.firstOrNull() ||
        way.nodeIds.lastOrNull() != newWay.nodeIds.lastOrNull()

private fun isRelationGeometrySubstantiallyDifferent(relation: SpatialPartsOfRelation, newRelation: Relation) =
    /* a relation is counted as substantially different, if any member changed, even if just
       the order changed because for some relations, the order has an important meaning */
    relation.members != newRelation.members



fun Element.getSpatialParts(): SpatialPartsOfElement = when(this) {
    is Node -> SpatialPartsOfNode(OsmLatLon(position.latitude, position.longitude))
    is Way -> SpatialPartsOfWay(ArrayList(nodeIds))
    is Relation -> SpatialPartsOfRelation(ArrayList(members))
    else -> throw IllegalArgumentException()
}

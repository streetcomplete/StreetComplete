package de.westnordost.streetcomplete.data.osm.edits.update_tags

import de.westnordost.streetcomplete.data.osm.mapdata.*
import de.westnordost.streetcomplete.util.distanceTo

internal fun isGeometrySubstantiallyDifferent(element: Element, newElement: Element) =
    when (element) {
        is Node -> isNodeGeometrySubstantiallyDifferent(element, newElement as Node)
        is Way -> isWayGeometrySubstantiallyDifferent(element, newElement as Way)
        is Relation -> isRelationGeometrySubstantiallyDifferent(element, newElement as Relation)
    }

private fun isNodeGeometrySubstantiallyDifferent(node: Node, newNode: Node) =
    /* Moving the node a distance beyond what would pass as adjusting the position within a
       building counts as substantial change. Also, the maximum distance should be not (much)
       bigger than the usual GPS inaccuracy in the city. */
    node.position.distanceTo(newNode.position) > 20

private fun isWayGeometrySubstantiallyDifferent(way: Way, newWay: Way) =
    /* if the first or last node is different, it means that the way has either been extended or
       shortened at one end, which is counted as being substantial:
       If for example the surveyor has been asked to determine something for a certain way
       and this way is now longer, his answer does not apply to the whole way anymore, so that
       is an unsolvable conflict. */
    way.nodeIds.firstOrNull() != newWay.nodeIds.firstOrNull() ||
        way.nodeIds.lastOrNull() != newWay.nodeIds.lastOrNull()

private fun isRelationGeometrySubstantiallyDifferent(relation: Relation, newRelation: Relation) =
    /* a relation is counted as substantially different, if any member changed, even if just
       the order changed because for some relations, the order has an important meaning */
    relation.members != newRelation.members

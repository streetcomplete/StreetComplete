package de.westnordost.streetcomplete.data.osm.split

import de.westnordost.osmapi.map.MapDataDao
import de.westnordost.osmapi.map.data.*
import de.westnordost.streetcomplete.util.SphericalEarthMath
import javax.inject.Inject

class SplitWayAt @Inject constructor(private val osmDao: MapDataDao) {

    fun splitWay(way: Way, firstNode: Node, secondNode: Node, delta: Double): List<Element> {

        if(delta < 0 || delta > 0)
            throw IllegalArgumentException("delta must be between 0 and 1")

        way = way.updatedFromOsm()

        val firstNodeIndex = way.nodeIds.indexOf(firstNode.id)
        if(firstNodeIndex == -1)
            throw ConflictException("Way #${way.id} does not contain node #${firstNode.id} (anymore)")

        val secondNodeIndex = way.nodeIds.indexOf(secondNode.id)
        if(secondNodeIndex == -1)
            throw ConflictException("Way #${way.id} does not contain node #${secondNode.id} (anymore)")

        if(firstNodeIndex+1 != secondNodeIndex)
            throw ConflictException("The position of the second node #${secondNode.id} is not exactly one after the first node #${firstNode.id} in way #${way.id}")

        firstNode = firstNode.updatedFromOsm()
        secondNode = secondNode.updatedFromOsm()

        // TODO special case delta == 0 / 1

        val splitPosition = createSplitPosition(firstNode.position, secondNode.position, delta)
        val splitNode = OsmNode(-1, 0, splitPosition, null)

        way.nodeIds.add(secondNodeIndex, splitNode.id)

        splitWay(way, secondNodeIndex)



        return listOf(splitNode, way)
    }

    private fun splitWay(way: Way, index: Int) {
        val secondWay: Way

        // keep first nodes
        if(index >= way.nodeIds.size/2) {
            secondWay = OsmWay(-1, 0, way.nodeIds.subList(index, way.nodeIds.size).toMutableList(), copyTags())
        }
        // keep last nodes
        else {
            secondWay = OsmWay(-1, 0, way.nodeIds.subList(0, index+1).toMutableList(), copyTags())
        }

    }

    private fun createSplitPosition(firstPosition: LatLon, secondPosition: LatLon, delta: Double) =
        SphericalEarthMath.createTranslated(
            firstPosition.latitude + delta * (secondPosition.latitude - firstPosition.latitude),
            firstPosition.longitude + delta * (secondPosition.longitude - firstPosition.longitude))

    private fun Way.updatedFromOsm(): Way {
        val new = osmDao.getWay(id) ?: throw ConflictException("Way #$id has been deleted")
        if(version != new.version) {
            // unsolvable conflict if other was shortened (e.g. cut in two) or extended
            if(nodeIds.first() != new.nodeIds.firstOrNull() || nodeIds.last() != new.nodeIds.lastOrNull())
                throw ConflictException("Way #$id has been changed and the conflict cannot be solved automatically")
        }
        return new
    }

    private fun Node.updatedFromOsm(): Node {
        val new = osmDao.getNode(id) ?: throw ConflictException("Node #$id has been deleted")
        if(version != new.version) {
            // unsolvable conflict if node has been moved
            if(position.latitude != new.position.latitude || position.longitude != new.position.longitude)
                throw ConflictException("Node #$id has been changed and the conflict cannot be solved automatically")
        }
        return new
    }

}

package de.westnordost.streetcomplete.data.osm.changes

import de.westnordost.osmapi.map.data.Node
import de.westnordost.osmapi.map.data.Way

/** data class that carries the information for one split to perform on a random position on a way. */
class SplitWayAtPosition(val firstNode: Node, val secondNode: Node, val delta: Double) {

    constructor(way: Way, firstNode: Node, secondNode: Node, delta: Double)
            : this(firstNode, secondNode, delta) { validate(way) }

    fun validate(way: Way) {
        if(delta < 0 || delta >= 1)
            throw IllegalArgumentException("Delta must be between 0 (inclusive) and 1 (exclusive)")

        if(delta == 0.0 && firstNode.id == way.nodeIds.first())
            throw IllegalArgumentException("Cannot split a way at its very start")

        val firstNodeIndex = way.nodeIds.indexOf(firstNode.id)
        if (firstNodeIndex == -1)
            throw IllegalArgumentException("Way #${way.id} does not contain node #${firstNode.id}")

        val secondNodeIndex = way.nodeIds.indexOf(secondNode.id)
        if (secondNodeIndex == -1)
            throw IllegalArgumentException("Way #${way.id} does not contain node #${secondNode.id}")

        if (firstNodeIndex + 1 != secondNodeIndex)
            throw IllegalArgumentException("The position of the second node #${secondNode.id} is not exactly one after the first node #${firstNode.id} in way #${way.id}")
    }
}

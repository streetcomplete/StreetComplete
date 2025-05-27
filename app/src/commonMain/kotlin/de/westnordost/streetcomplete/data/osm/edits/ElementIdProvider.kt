package de.westnordost.streetcomplete.data.osm.edits

import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType

/** Provides stable element ids for the creation of new elements */
class ElementIdProvider(elementKeys: Collection<ElementKey>) {
    private val nodeIds: MutableList<Long>
    private val wayIds: MutableList<Long>
    private val relationIds: MutableList<Long>

    init {
        nodeIds = ArrayList(elementKeys.size)
        wayIds = ArrayList(elementKeys.size)
        relationIds = ArrayList(elementKeys.size)

        for (key in elementKeys) {
            when (key.type) {
                ElementType.NODE -> nodeIds.add(key.id)
                ElementType.WAY -> wayIds.add(key.id)
                ElementType.RELATION -> relationIds.add(key.id)
            }
        }
        // they should be sorted -1, -2, -3, ... etc. - it's descending because all ids are negative
        nodeIds.sortDescending()
        wayIds.sortDescending()
        relationIds.sortDescending()
    }

    fun nextNodeId(): Long = nodeIds.removeAt(0)
    fun nextWayId(): Long = wayIds.removeAt(0)
    fun nextRelationId(): Long = relationIds.removeAt(0)

    fun getAll(): List<ElementKey> =
        nodeIds.map { ElementKey(ElementType.NODE, it) } +
        wayIds.map { ElementKey(ElementType.WAY, it) } +
        relationIds.map { ElementKey(ElementType.RELATION, it) }

    fun isEmpty(): Boolean = nodeIds.isEmpty() && wayIds.isEmpty() && relationIds.isEmpty()
}

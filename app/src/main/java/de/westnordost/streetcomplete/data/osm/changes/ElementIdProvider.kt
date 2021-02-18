package de.westnordost.streetcomplete.data.osm.changes

import de.westnordost.osmapi.map.data.Element
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey

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
            when(key.elementType) {
                Element.Type.NODE -> nodeIds.add(key.elementId)
                Element.Type.WAY -> wayIds.add(key.elementId)
                Element.Type.RELATION -> relationIds.add(key.elementId)
            }
        }
        // they should be sorted -1, -2, -3, ... etc. - it's descending because all ids are negative
        nodeIds.sortDescending()
        wayIds.sortDescending()
        relationIds.sortDescending()
    }

    fun nextNodeId(): Long = nodeIds.removeFirst()
    fun nextWayId(): Long = wayIds.removeFirst()
    fun nextRelationId(): Long = relationIds.removeFirst()
}

package de.westnordost.streetcomplete.data.osm.changes

import de.westnordost.osmapi.map.data.Element
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey

class NewOsmElementIdProvider(elementKeys: Collection<ElementKey>) {
    private val nodeIds = mutableListOf<Long>()
    private val wayIds = mutableListOf<Long>()
    private val relationIds = mutableListOf<Long>()

    init {
        for (key in elementKeys) {
            when(key.elementType) {
                Element.Type.NODE -> nodeIds.add(key.elementId)
                Element.Type.WAY -> wayIds.add(key.elementId)
                Element.Type.RELATION -> relationIds.add(key.elementId)
            }
        }
        nodeIds.sort()
        wayIds.sort()
        relationIds.sort()
    }

    fun nextNodeId(): Long = nodeIds.removeFirst()
    fun nextWayId(): Long = wayIds.removeFirst()
    fun nextRelationId(): Long = relationIds.removeFirst()
}

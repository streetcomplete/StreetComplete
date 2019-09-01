package de.westnordost.streetcomplete.ktx

import de.westnordost.osmapi.map.data.*
import java.util.ArrayList

fun Element.copy(newId: Long = id, newVersion: Int = version): Element {
    val tags = tags?.let { HashMap(it) }
    return when (this) {
        is Node -> OsmNode(newId, newVersion, position, tags)
        is Way -> OsmWay(newId, newVersion, ArrayList(nodeIds), tags)
        is Relation -> OsmRelation(newId, newVersion, ArrayList(members), tags)
        else -> throw RuntimeException()
    }
}

fun Way.isClosed() = nodeIds.size >= 3 && nodeIds.first() == nodeIds.last()

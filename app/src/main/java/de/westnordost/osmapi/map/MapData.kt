package de.westnordost.osmapi.map

import de.westnordost.streetcomplete.data.osm.mapdata.*

interface MapData : Iterable<Element> {
    val nodes: Collection<Node>
    val ways: Collection<Way>
    val relations: Collection<Relation>
    val boundingBox: BoundingBox?

    fun getNode(id: Long): Node?
    fun getWay(id: Long): Way?
    fun getRelation(id: Long): Relation?
}

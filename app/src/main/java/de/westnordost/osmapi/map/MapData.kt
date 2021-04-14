package de.westnordost.osmapi.map

import de.westnordost.osmapi.map.data.Element
import de.westnordost.osmapi.map.data.Node
import de.westnordost.osmapi.map.data.Relation
import de.westnordost.osmapi.map.data.Way
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox

interface MapData : Iterable<Element> {
    val nodes: Collection<Node>
    val ways: Collection<Way>
    val relations: Collection<Relation>
    val boundingBox: BoundingBox?

    fun getNode(id: Long): Node?
    fun getWay(id: Long): Way?
    fun getRelation(id: Long): Relation?
}

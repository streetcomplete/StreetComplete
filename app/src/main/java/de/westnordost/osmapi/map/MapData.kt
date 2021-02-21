package de.westnordost.osmapi.map

import de.westnordost.osmapi.map.data.*

interface MapData : Iterable<Element> {
    val nodes: Collection<Node>
    val ways: Collection<Way>
    val relations: Collection<Relation>
    val boundingBox: BoundingBox?

    fun getNode(id: Long): Node?
    fun getWay(id: Long): Way?
    fun getRelation(id: Long): Relation?
}

fun MapData.isRelationComplete(id: Long): Boolean =
    getRelation(id)?.members?.all { member ->
        when (member.type!!) {
            Element.Type.NODE -> getNode(member.ref) != null
            Element.Type.WAY -> getWay(member.ref) != null && isWayComplete(member.ref)
            /* not being recursive here is deliberate. sub-relations are considered not relevant
               for the element geometry in StreetComplete (and OSM API call to get a "complete"
               relation also does not include sub-relations) */
            Element.Type.RELATION -> getRelation(member.ref) != null
        }
    } ?: false

fun MapData.isWayComplete(id: Long): Boolean =
    getWay(id)?.nodeIds?.all { getNode(it) != null } ?: false

package de.westnordost.streetcomplete.data.osm.mapdata

interface MapData : Iterable<Element> {
    val nodes: Collection<Node>
    val ways: Collection<Way>
    val relations: Collection<Relation>
    val boundingBox: BoundingBox?
    val size: Int get() = nodes.size + ways.size + relations.size

    fun getNode(id: Long): Node?
    fun getWay(id: Long): Way?
    fun getRelation(id: Long): Relation?
}

fun MapData.isRelationComplete(id: Long): Boolean =
    getRelation(id)?.members?.all { member ->
        when (member.type) {
            ElementType.NODE -> getNode(member.ref) != null
            ElementType.WAY -> getWay(member.ref) != null && isWayComplete(member.ref)
            /* not being recursive here is deliberate. sub-relations are considered not relevant
               for the element geometry in StreetComplete (and OSM API call to get a "complete"
               relation also does not include sub-relations) */
            ElementType.RELATION -> getRelation(member.ref) != null
        }
    } ?: false

fun MapData.isWayComplete(id: Long): Boolean =
    getWay(id)?.nodeIds?.all { getNode(it) != null } ?: false

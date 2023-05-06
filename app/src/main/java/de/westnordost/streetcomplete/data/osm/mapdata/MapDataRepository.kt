package de.westnordost.streetcomplete.data.osm.mapdata

interface MapDataRepository {
    fun getNode(id: Long): Node?
    fun getWay(id: Long): Way?
    fun getRelation(id: Long): Relation?

    fun getWayComplete(id: Long): MapData?
    fun getRelationComplete(id: Long): MapData?

    fun getWaysForNode(id: Long): Collection<Way>
    fun getRelationsForNode(id: Long): Collection<Relation>
    fun getRelationsForWay(id: Long): Collection<Relation>
    fun getRelationsForRelation(id: Long): Collection<Relation>

    fun get(type: ElementType, id: Long) = when (type) {
        ElementType.NODE     -> getNode(id)
        ElementType.WAY      -> getWay(id)
        ElementType.RELATION -> getRelation(id)
    }
}

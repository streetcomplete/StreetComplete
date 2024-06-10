package de.westnordost.streetcomplete.data.osm.mapdata

interface MapDataRepository {
    suspend fun getNode(id: Long): Node?
    suspend fun getWay(id: Long): Way?
    suspend fun getRelation(id: Long): Relation?

    suspend fun getWayComplete(id: Long): MapData?
    suspend fun getRelationComplete(id: Long): MapData?

    suspend fun getWaysForNode(id: Long): Collection<Way>
    suspend fun getRelationsForNode(id: Long): Collection<Relation>
    suspend fun getRelationsForWay(id: Long): Collection<Relation>
    suspend fun getRelationsForRelation(id: Long): Collection<Relation>

    suspend fun get(type: ElementType, id: Long) = when (type) {
        ElementType.NODE     -> getNode(id)
        ElementType.WAY      -> getWay(id)
        ElementType.RELATION -> getRelation(id)
    }
}

package de.westnordost.streetcomplete.data.osm.mapdata

import de.westnordost.streetcomplete.data.osm.mapdata.ElementType.NODE
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType.RELATION
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType.WAY

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

    fun get(type: ElementType, id: Long): Element? = when (type) {
        ElementType.NODE     -> getNode(id)
        ElementType.WAY      -> getWay(id)
        ElementType.RELATION -> getRelation(id)
    }

    fun getRelationsForElement(type: ElementType, id: Long): Collection<Relation> = when (type) {
        NODE -> getRelationsForNode(id)
        WAY -> getRelationsForWay(id)
        RELATION -> getRelationsForRelation(id)
    }
}

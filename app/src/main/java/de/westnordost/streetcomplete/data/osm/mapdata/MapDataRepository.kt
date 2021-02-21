package de.westnordost.streetcomplete.data.osm.mapdata

import de.westnordost.osmapi.map.MapData
import de.westnordost.osmapi.map.data.Node
import de.westnordost.osmapi.map.data.Relation
import de.westnordost.osmapi.map.data.Way

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
}

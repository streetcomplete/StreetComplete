package de.westnordost.streetcomplete.data.osm.mapdata

import de.westnordost.osmapi.map.MapData
import de.westnordost.osmapi.map.data.Node
import de.westnordost.osmapi.map.data.Relation
import de.westnordost.osmapi.map.data.Way
import de.westnordost.osmapi.map.getWayComplete
import de.westnordost.osmapi.map.getRelationComplete
import de.westnordost.streetcomplete.data.MapDataApi

class ApiMapDataRepository(private val api: MapDataApi) : MapDataRepository {

    override fun getNode(id: Long): Node? = api.getNode(id)
    override fun getWay(id: Long): Way? = api.getWay(id)
    override fun getRelation(id: Long): Relation? = api.getRelation(id)

    override fun getWayComplete(id: Long): MapData? = api.getWayComplete(id)
    override fun getRelationComplete(id: Long): MapData? = api.getRelationComplete(id)

    override fun getWaysForNode(id: Long): List<Way> = api.getWaysForNode(id)
    override fun getRelationsForNode(id: Long): List<Relation> = api.getRelationsForNode(id)
    override fun getRelationsForWay(id: Long): List<Relation> = api.getRelationsForWay(id)
    override fun getRelationsForRelation(id: Long): List<Relation> = api.getRelationsForRelation(id)
}

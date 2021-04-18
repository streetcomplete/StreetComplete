package de.westnordost.streetcomplete.data.osm.mapdata

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

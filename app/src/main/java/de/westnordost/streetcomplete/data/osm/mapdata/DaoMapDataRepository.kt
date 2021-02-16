package de.westnordost.streetcomplete.data.osm.mapdata

import de.westnordost.osmapi.map.MapData
import de.westnordost.osmapi.map.MutableMapData
import de.westnordost.osmapi.map.data.Element
import de.westnordost.osmapi.map.data.Node
import de.westnordost.osmapi.map.data.Relation
import de.westnordost.osmapi.map.data.Way
import javax.inject.Inject

class DaoMapDataRepository @Inject constructor(
    private val nodeDao: NodeDao,
    private val wayDao: WayDao,
    private val relationDao: RelationDao
) : MapDataRepository {

    override fun getNode(id: Long): Node? = nodeDao.get(id)
    override fun getWay(id: Long): Way? = wayDao.get(id)
    override fun getRelation(id: Long): Relation? = relationDao.get(id)

    override fun getWayComplete(id: Long): MapData? {
        val way = wayDao.get(id) ?: return null
        val nodes = nodeDao.getAll(way.nodeIds)
        if (nodes.size < way.nodeIds.size) return null
        return MutableMapData().apply { addAll(nodes + way) }
    }

    override fun getRelationComplete(id: Long): MapData? {
        val relation = relationDao.get(id) ?: return null

        val nodeMemberIds = relation.members.filter { it.type == Element.Type.NODE }.map { it.ref }
        val nodes = nodeDao.getAll(nodeMemberIds)
        if (nodes.size < nodeMemberIds.size) return null

        val wayMemberIds = relation.members.filter { it.type == Element.Type.WAY }.map { it.ref }
        val ways = wayDao.getAll(wayMemberIds)
        if (ways.size < wayMemberIds.size) return null

        val relationMemberIds = relation.members.filter {  it.type == Element.Type.RELATION }.map { it.ref }
        val relations = relationDao.getAll(relationMemberIds)
        if (relations.size < relationMemberIds.size) return null

        return MutableMapData().apply { addAll(nodes + ways + relations) }
    }

    override fun getWaysForNode(id: Long): List<Way> = wayDao.getAllForNode(id)
    override fun getRelationsForNode(id: Long): List<Relation> = relationDao.getAllForNode(id)
    override fun getRelationsForWay(id: Long): List<Relation> = relationDao.getAllForWay(id)
    override fun getRelationsForRelation(id: Long): List<Relation> = relationDao.getAllForRelation(id)
}

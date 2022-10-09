package de.westnordost.streetcomplete.data.osm.created_elements

import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataRepository

class MapDataRepositoryWithUpdatedIds(
    private val createdElementsSource: CreatedElementsSource,
    private val mapDataRepository: MapDataRepository
) : MapDataRepository {

    private fun nodeId(id: Long): Long = createdElementsSource.getId(ElementType.NODE, id) ?: id
    private fun wayId(id: Long): Long = createdElementsSource.getId(ElementType.WAY, id) ?: id
    private fun relId(id: Long): Long = createdElementsSource.getId(ElementType.RELATION, id) ?: id

    override fun getNode(id: Long) = mapDataRepository.getNode(nodeId(id))
    override fun getWay(id: Long) = mapDataRepository.getWay(wayId(id))
    override fun getRelation(id: Long) = mapDataRepository.getRelation(relId(id))
    override fun getWayComplete(id: Long) = mapDataRepository.getWayComplete(wayId(id))
    override fun getRelationComplete(id: Long) = mapDataRepository.getRelationComplete(relId(id))
    override fun getWaysForNode(id: Long) = mapDataRepository.getWaysForNode(nodeId(id))
    override fun getRelationsForNode(id: Long) = mapDataRepository.getRelationsForNode(nodeId(id))
    override fun getRelationsForWay(id: Long) = mapDataRepository.getRelationsForWay(wayId(id))
    override fun getRelationsForRelation(id: Long) = mapDataRepository.getRelationsForRelation(wayId(id))
}

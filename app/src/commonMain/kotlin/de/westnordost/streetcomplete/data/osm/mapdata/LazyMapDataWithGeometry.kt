package de.westnordost.streetcomplete.data.osm.mapdata

import de.westnordost.streetcomplete.data.osm.edits.MapDataWithEditsSource
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPointGeometry

/** A map data with geometry that will only actually query the data once it is first accessed */
class LazyMapDataWithGeometry(
    private val bbox: BoundingBox,
    private val mapDataWithEditsSource: MapDataWithEditsSource
) : MapDataWithGeometry {

    private val data by lazy { mapDataWithEditsSource.getMapDataWithGeometry(bbox) }

    override fun getNodeGeometry(id: Long) = data.getNodeGeometry(id)
    override fun getWayGeometry(id: Long) = data.getWayGeometry(id)
    override fun getRelationGeometry(id: Long) = data.getRelationGeometry(id)

    override val nodes = data.nodes
    override val ways = data.ways
    override val relations = data.relations
    override val boundingBox get() = data.boundingBox

    override fun getNode(id: Long) = data.getNode(id)
    override fun getWay(id: Long) = data.getWay(id)
    override fun getRelation(id: Long) = data.getRelation(id)

    override fun iterator() = data.iterator()
}

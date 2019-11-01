package de.westnordost.streetcomplete.data.osm.download

import android.util.LongSparseArray

import de.westnordost.osmapi.map.MapDataDao
import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.LatLon
import de.westnordost.osmapi.map.data.Node
import de.westnordost.osmapi.map.data.Relation
import de.westnordost.osmapi.map.data.Way
import de.westnordost.osmapi.map.handler.MapDataHandler
import javax.inject.Inject

class OsmApiWayGeometrySource @Inject constructor(private val osmDao: MapDataDao) : WayGeometrySource {

    override fun getNodePositions(wayId: Long): List<LatLon> {
        lateinit var way: Way
        val nodes = LongSparseArray<Node>()

        osmDao.getWayComplete(wayId, object : MapDataHandler {
            override fun handle(b: BoundingBox) {}
            override fun handle(n: Node) { nodes.put(n.id, n) }
            override fun handle(w: Way) { way = w }
            override fun handle(r: Relation) {}
        })

        return way.nodeIds.map { nodes[it].position }
    }
}

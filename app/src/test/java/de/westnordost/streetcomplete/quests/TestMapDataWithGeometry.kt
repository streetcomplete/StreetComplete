package de.westnordost.streetcomplete.quests

import de.westnordost.streetcomplete.testutils.bbox
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPointGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.*

class TestMapDataWithGeometry(elements: Iterable<Element>) : MutableMapData(), MapDataWithGeometry {

    init {
        addAll(elements)
        boundingBox = bbox()
    }

    val nodeGeometriesById: MutableMap<Long, ElementPointGeometry?> = mutableMapOf()
    val wayGeometriesById: MutableMap<Long, ElementGeometry?> = mutableMapOf()
    val relationGeometriesById: MutableMap<Long, ElementGeometry?> = mutableMapOf()

    override fun getNodeGeometry(id: Long): ElementPointGeometry? = nodeGeometriesById[id]
    override fun getWayGeometry(id: Long): ElementGeometry? = wayGeometriesById[id]
    override fun getRelationGeometry(id: Long): ElementGeometry? = relationGeometriesById[id]
}

fun createMapData(elements: Map<Element, ElementGeometry?>): TestMapDataWithGeometry {
    val result = TestMapDataWithGeometry(elements.keys)
    for((element, geometry) in elements) {
        when(element) {
            is Node ->
                result.nodeGeometriesById[element.id] = geometry as ElementPointGeometry
            is Way ->
                result.wayGeometriesById[element.id] = geometry
            is Relation ->
                result.relationGeometriesById[element.id] = geometry
        }
    }
    result.boundingBox = bbox()
    return result
}

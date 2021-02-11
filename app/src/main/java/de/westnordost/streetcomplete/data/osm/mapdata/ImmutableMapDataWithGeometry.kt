package de.westnordost.streetcomplete.data.osm.mapdata

import de.westnordost.osmapi.map.MapData
import de.westnordost.osmapi.map.MapDataWithGeometry
import de.westnordost.osmapi.map.data.Element.Type.*
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometryEntry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPointGeometry

class ImmutableMapDataWithGeometry(
    mapData: MapData,
    geometry: Iterable<ElementGeometryEntry>
) : MapData by mapData, MapDataWithGeometry {

    private val nodeGeometriesById = mutableMapOf<Long, ElementPointGeometry?>()
    private val wayGeometriesById = mutableMapOf<Long, ElementGeometry?>()
    private val relationGeometriesById = mutableMapOf<Long, ElementGeometry?>()

    init {
        for (entry in geometry) {
            when(entry.elementType) {
                NODE -> nodeGeometriesById[entry.elementId] = entry.geometry as? ElementPointGeometry
                WAY -> wayGeometriesById[entry.elementId] = entry.geometry
                RELATION -> relationGeometriesById[entry.elementId] = entry.geometry
            }
        }
    }

    override fun getNodeGeometry(id: Long) = nodeGeometriesById[id]
    override fun getWayGeometry(id: Long) = wayGeometriesById[id]
    override fun getRelationGeometry(id: Long) = relationGeometriesById[id]
}

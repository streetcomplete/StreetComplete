package de.westnordost.streetcomplete.data.osm.mapdata

import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometryEntry

interface MapDataSource : MapDataRepository {
    /** Interface to be notified of new or updated OSM elements */
    interface Listener {
        /** Called when a number of elements have been updated or deleted */
        fun onUpdated(updated: MutableMapDataWithGeometry, deleted: Collection<ElementKey>)

        /** Called when all elements in the given bounding box should be replaced with the elements
         *  in the mapDataWithGeometry */
        fun onReplacedForBBox(bbox: BoundingBox, mapDataWithGeometry: MutableMapDataWithGeometry)

        /** Called when all elements have been cleared */
        fun onCleared()
    }

    fun addListener(listener: Listener)

    fun removeListener(listener: Listener)

    fun getGeometry(type: ElementType, id: Long): ElementGeometry?

    fun getGeometries(keys: Collection<ElementKey>): List<ElementGeometryEntry>

    fun getMapDataWithGeometry(bbox: BoundingBox): MutableMapDataWithGeometry

    data class ElementCounts(val nodes: Int, val ways: Int, val relations: Int)
    fun getElementCounts(bbox: BoundingBox): ElementCounts

    fun getAll(elementKeys: Collection<ElementKey>): List<Element>

    fun getNodes(ids: Collection<Long>): List<Node>
    fun getWays(ids: Collection<Long>): List<Way>
    fun getRelations(ids: Collection<Long>): List<Relation>
}

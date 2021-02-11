package de.westnordost.streetcomplete.data.osm.changes

import de.westnordost.osmapi.map.MapDataWithGeometry
import de.westnordost.osmapi.map.MutableMapData
import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.Element
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.ImmutableMapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.OsmElementController
import de.westnordost.streetcomplete.data.osm.mapdata.OsmElementSource
import java.util.concurrent.CopyOnWriteArrayList
import javax.inject.Inject
import javax.inject.Singleton

@Singleton class OsmElementsWithChangesSource @Inject constructor(
    private val osmElementController: OsmElementController,
    private val osmElementChangesController: OsmElementChangesController
): OsmElementSource {
    /* Must be a singleton because there is a listener that should respond to a change in the
     * database table */

    private val listeners: MutableList<OsmElementSource.Listener> = CopyOnWriteArrayList()

    private val osmElementControllerListener = object : OsmElementSource.Listener {
        override fun onUpdated(updated: MapDataWithGeometry, deleted: Collection<ElementKey>) {
            TODO("Not yet implemented")
        }

        override fun onReplacedForBBox(bbox: BoundingBox, mapDataWithGeometry: MapDataWithGeometry) {
            TODO("Not yet implemented")
        }
    }

    init {
        osmElementController.addListener(osmElementControllerListener)
    }

    override fun get(type: Element.Type, id: Long): Element? {
        val element = osmElementController.get(type, id)
        val mapData = ImmutableMapDataWithGeometry(MutableMapData(), )
        return osmElementChangesController.changesAppliedTo(element)
    }

    override fun getGeometry(type: Element.Type, id: Long): ElementGeometry? {
        val geometry = osmElementController.getGeometry(type, id)

        TODO("Not yet implemented")
    }

    override fun getMapDataWithGeometry(bbox: BoundingBox): MapDataWithGeometry {
        val mapData = osmElementController.getMapDataWithGeometry(bbox)
        return osmElementChangesController.changesAppliedTo(mapData)
    }

    override fun addListener(listener: OsmElementSource.Listener) {
        listeners.add(listener)
    }
    override fun removeListener(listener: OsmElementSource.Listener) {
        listeners.remove(listener)
    }
}

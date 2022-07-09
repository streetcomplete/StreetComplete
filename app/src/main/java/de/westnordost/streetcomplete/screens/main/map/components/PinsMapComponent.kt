package de.westnordost.streetcomplete.screens.main.map.components

import com.mapzen.tangram.MapData
import com.mapzen.tangram.geometry.Point
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.screens.main.map.tangram.KtMapController
import de.westnordost.streetcomplete.screens.main.map.tangram.toLngLat
import de.westnordost.streetcomplete.screens.main.map.tangram.toTangramGeometry

/** Takes care of displaying pins on the map, e.g. quest pins or pins for recent edits */
class PinsMapComponent(ctrl: KtMapController) {

    private val pinsLayer: MapData = ctrl.addDataLayer(PINS_LAYER)
    private val questsGeometryLayer: MapData = ctrl.addDataLayer(QUESTS_GEOMETRY_LAYER)

    /** Shows/hides the pins */
    var isVisible: Boolean
        get() = pinsLayer.visible
        set(value) {
            pinsLayer.visible = value
            questsGeometryLayer.visible = value
        }

    /** Show given pins. Previously shown pins are replaced with these.  */
    fun set(pins: Collection<Pin>) {
        pinsLayer.setFeatures(pins.map { pin ->
            // avoid creation of intermediate HashMaps.
            val tangramProperties = listOf(
                "type" to "point",
                "kind" to pin.iconName,
                "importance" to pin.importance.toString(),
                "poi_color" to pin.color
            )
            val properties = HashMap<String, String>()
            properties.putAll(tangramProperties)
            properties.putAll(pin.properties)
            Point(pin.position.toLngLat(), properties)
        })
        val questGeometries = pins.mapNotNull { it.geometry?.toTangramGeometry() }.flatten()
        questsGeometryLayer.setFeatures(questGeometries)
    }

    /** Clear pins */
    fun clear() {
        pinsLayer.clear()
        questsGeometryLayer.clear()
    }

    companion object {
        // see streetcomplete.yaml for the definitions of the below layers
        private const val PINS_LAYER = "streetcomplete_pins"
        private const val QUESTS_GEOMETRY_LAYER = "streetcomplete_quests_geometry"
    }
}

data class Pin(
    val position: LatLon,
    val iconName: String,
    val properties: Collection<Pair<String, String>> = emptyList(),
    val importance: Int = 0,
    val geometry: ElementGeometry? = null,
    val color: String = "no",
)

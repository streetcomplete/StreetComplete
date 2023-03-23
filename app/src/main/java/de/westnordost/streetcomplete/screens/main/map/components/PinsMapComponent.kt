package de.westnordost.streetcomplete.screens.main.map.components

import com.mapzen.tangram.MapData
import com.mapzen.tangram.geometry.Point
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.screens.main.map.tangram.KtMapController
import de.westnordost.streetcomplete.screens.main.map.tangram.toLngLat

/** Takes care of displaying pins on the map, e.g. quest pins or pins for recent edits */
class PinsMapComponent(private val ctrl: KtMapController) {

    private val pinsLayer: MapData = ctrl.addDataLayer(PINS_LAYER)

    /** Shows/hides the pins */
    var isVisible: Boolean
        get() = pinsLayer.visible
        set(value) {
            pinsLayer.visible = value
            ctrl.requestRender()
        }

    /** Show given pins. Previously shown pins are replaced with these.  */
    fun set(pins: Collection<Pin>) {
        pinsLayer.setFeatures(pins.map { it.tangramPoint })
    }

    /** Clear pins */
    fun clear() {
        pinsLayer.clear()
    }

    companion object {
        // see streetcomplete.yaml for the definitions of the below layers
        private const val PINS_LAYER = "streetcomplete_pins"
    }
}

data class Pin(
    val position: LatLon,
    val iconName: String,
    val properties: Collection<Pair<String, String>> = emptyList(),
    val importance: Int = 0
) {
    val tangramPoint by lazy {
        // avoid creation of intermediate HashMaps.
        val tangramProperties = listOf(
            "type" to "point",
            "kind" to iconName,
            "importance" to importance.toString()
        )
        val props = HashMap<String, String>()
        props.putAll(tangramProperties)
        props.putAll(properties)
        Point(position.toLngLat(), props)
    }
}

package de.westnordost.streetcomplete.screens.main.map.components

import com.google.gson.JsonParser
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions
import com.mapzen.tangram.MapData
import com.mapzen.tangram.geometry.Point
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.screens.main.map.MainMapFragment
import de.westnordost.streetcomplete.screens.main.map.tangram.KtMapController
import de.westnordost.streetcomplete.screens.main.map.tangram.toLngLat
import de.westnordost.streetcomplete.screens.main.map.toQuestKey

/** Takes care of displaying pins on the map, e.g. quest pins or pins for recent edits */
class PinsMapComponent(
    ctrl: KtMapController,
    private val symbolManager: SymbolManager,
    listener: MainMapFragment.Listener?
) {

    private val pinsLayer: MapData = ctrl.addDataLayer(PINS_LAYER)

    /** Shows/hides the pins */
    var isVisible: Boolean
        get() = pinsLayer.visible
        set(value) { pinsLayer.visible = value }

    init {
        symbolManager.addClickListener { u ->
            val questKey = u.data!!.toQuestKey()
            listener?.onClickedQuest(questKey)
            return@addClickListener true
        }
    }


    /** Show given pins. Previously shown pins are replaced with these.  */
    fun set(pins: Collection<Pin>) {
        pinsLayer.setFeatures(pins.map { pin ->
            // avoid creation of intermediate HashMaps.
            val tangramProperties = listOf(
                "type" to "point",
                "kind" to pin.iconName,
                "importance" to pin.importance.toString()
            )
            val properties = HashMap<String, String>()
            properties.putAll(tangramProperties)
            properties.putAll(pin.properties)
            Point(pin.position.toLngLat(), properties)
        })

        symbolManager.deleteAll()
        symbolManager.create(pins.map { pin ->

            SymbolOptions()
                .withLatLng(LatLng(pin.position.latitude, pin.position.longitude))
                .withIconImage(pin.iconName)
                .withIconSize(0.3f)
                .withData(JsonParser().parse(pin.jsonProps))
        })
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
    val jsonProps : String,
    val importance: Int = 0
)

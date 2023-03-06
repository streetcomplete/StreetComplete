package de.westnordost.streetcomplete.screens.main.map.components

import com.google.gson.JsonElement
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions
import com.mapzen.tangram.MapData
import com.mapzen.tangram.geometry.Point
import de.westnordost.streetcomplete.data.maptiles.toLatLng
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.screens.MainActivity
import de.westnordost.streetcomplete.screens.main.map.tangram.KtMapController
import de.westnordost.streetcomplete.screens.main.map.tangram.toLngLat

/** Takes care of displaying pins on the map, e.g. quest pins or pins for recent edits */
class PinsMapComponent(
    ctrl: KtMapController,
    private val symbolManager: SymbolManager,
) {

    private val pinsLayer: MapData = ctrl.addDataLayer(PINS_LAYER)

    /** Shows/hides the pins */
    var isVisible: Boolean
        get() = pinsLayer.visible
        set(value) { pinsLayer.visible = value }

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

        // todo: crash if not on UI thread
        //  for now the runOnUiThread is ok, but actually it should be handled differently...
        MainActivity.activity?.runOnUiThread {
            symbolManager.deleteAll()
            // todo: symbolManager returns symbols created from the pins, which can be used to delete single pins from the map
            //  is the order of the returned list the same as the symbol options list?
            //  this is important for associating symbols with quests
            //  -> yes according to https://github.com/maplibre/maplibre-plugins-android/blob/main/plugin-annotation/src/main/java/com/mapbox/mapboxsdk/plugins/annotation/AnnotationManager.java#L150
            //   but it's not mentioned in documentation... can we assume it will stay that way?
            //   there might be performance issues when adding many pins one by one...
            symbolManager.create(pins.map { pin ->
                SymbolOptions()
                    .withLatLng(pin.position.toLatLng())
                    .withIconImage(pin.iconName)
                    .withIconSize(0.3f)
                    .withData(pin.jsonProps)
            })

        }
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
    val jsonProps: JsonElement,
    val importance: Int = 0
)

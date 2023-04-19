package de.westnordost.streetcomplete.screens.main.map.components

import com.google.gson.JsonObject
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapzen.tangram.MapData
import com.mapzen.tangram.geometry.Point
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.screens.MainActivity
import de.westnordost.streetcomplete.screens.main.map.MainMapFragment
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
        // first create all features, then set them (almost) at the same time, to visually compare which library is faster here
        // impression: on start, MapLibre is faster, but once the app is fully loaded there is no difference
        val tangramFeatures = pins.map { it.tangramPoint }

        // do sorting here, because we can set the symbolZOrder to SYMBOL_Z_ORDER_SOURCE, which
        // is the order in which the source has the features
        val mapLibreFeatures = pins.sortedBy { -it.importance }.map {
                val p = JsonObject()
                p.addProperty("icon-image", it.iconName)
                p.addProperty("symbol-sort-key", -it.importance.toFloat()) // still set sort key, because we may want to try it again
                it.properties.forEach { p.addProperty(it.first, it.second) }
                Feature.fromGeometry(com.mapbox.geojson.Point.fromLngLat(it.position.longitude, it.position.latitude), p)
            }
        // todo: for testing the runOnUiThread is ok, but actually it should be handled differently...
        MainActivity.activity?.runOnUiThread { MainMapFragment.pinsSource?.setGeoJson(FeatureCollection.fromFeatures(mapLibreFeatures)) }
        pinsLayer.setFeatures(tangramFeatures)
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
        val props = HashMap<String, String>(properties.size + tangramProperties.size, 1f)
        props.putAll(tangramProperties)
        props.putAll(properties)
        Point(position.toLngLat(), props)
    }
}

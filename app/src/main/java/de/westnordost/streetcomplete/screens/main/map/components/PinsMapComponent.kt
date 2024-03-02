package de.westnordost.streetcomplete.screens.main.map.components

import androidx.annotation.UiThread
import com.google.gson.JsonObject
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.style.sources.GeoJsonOptions
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.screens.main.map.maplibre.clear
import de.westnordost.streetcomplete.screens.main.map.maplibre.toPoint

/** Takes care of displaying pins on the map, e.g. quest pins or pins for recent edits */
class PinsMapComponent(private val map: MapboxMap) {

    private val pinsSource = GeoJsonSource(
        "pins-source",
        GeoJsonOptions().withBuffer(32)  // is the buffer relevant? default value is 128, so this should load less data fromm adjacent tiles
    )

    /** Shows/hides the pins */
    var isVisible: Boolean
        // add / remove source
        @UiThread get() = map.style?.sources?.find { it.id == "pins-source" } != null
        @UiThread set(value) {
            if (isVisible == value) return
            if (value) {
                map.style?.addSource(pinsSource)
            } else {
                map.style?.removeSource(pinsSource)
            }
        }

    init {
        map.style?.addSource(pinsSource)
    }

    /** Show given pins. Previously shown pins are replaced with these.  */
    @UiThread fun set(pins: Collection<Pin>) {
        // do sorting here, because we can set the symbolZOrder to SYMBOL_Z_ORDER_SOURCE, which
        // is the order in which the source has the features
        val mapLibreFeatures = pins.sortedBy { -it.importance }.map { pin ->
            val p = JsonObject()
            p.addProperty("icon-image", pin.iconName)
            p.addProperty("symbol-sort-key", -pin.importance.toFloat()) // still set sort key, because we may want to try it again
            pin.properties.forEach { p.addProperty(it.first, it.second) }
            Feature.fromGeometry(pin.position.toPoint(), p)
        }
        pinsSource.setGeoJson(FeatureCollection.fromFeatures(mapLibreFeatures))
    }

    /** Clear pins */
    @UiThread fun clear() {
        pinsSource.clear()
    }
}

data class Pin(
    val position: LatLon,
    val iconName: String,
    val properties: Collection<Pair<String, String>> = emptyList(),
    val importance: Int = 0
) {
    // todo: maplibre feature by lazy?
}

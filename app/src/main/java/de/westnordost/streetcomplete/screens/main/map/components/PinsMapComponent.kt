package de.westnordost.streetcomplete.screens.main.map.components

import androidx.annotation.UiThread
import com.google.gson.JsonObject
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.style.expressions.Expression.*
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.Layer
import org.maplibre.android.style.layers.Property
import org.maplibre.android.style.layers.PropertyFactory.*
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonSource
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.screens.main.map.maplibre.clear
import de.westnordost.streetcomplete.screens.main.map.maplibre.toPoint
import de.westnordost.streetcomplete.util.logs.Log
import org.maplibre.android.style.sources.GeoJsonOptions

/** Takes care of displaying pins on the map, e.g. quest pins or pins for recent edits */
class PinsMapComponent(private val map: MapLibreMap) {
    private val pinsSource = GeoJsonSource(SOURCE,
        GeoJsonOptions()
            .withCluster(true)
            .withClusterMaxZoom(17)
            // how does it work? needs other properties, so I guess it's not useful for us...
            // though maybe it could help with the todo below?
//            .withClusterProperty(propertyName = , operatorExpr = , mapExpr = )
    )

    // todo:
    //  avoid clustering for multiple quests on the same element
    //  maybe replace circles with some special pins, would look nicer
    val layers: List<Layer> = listOf(
        CircleLayer("pin-dot-layer", SOURCE)
            .withFilter(all(gte(zoom(), 14f), lte(zoom(), 17f), gt(toNumber(get("point_count")), 1)))
            .withProperties(
                circleColor("white"),
                circleStrokeColor("grey"),
                circleRadius(12f), // is that dp? or should it be scaled with display size or something like that?
                circleStrokeWidth(1f)
            ),
        SymbolLayer("pin-dot-text-layer", SOURCE)
            .withFilter(all(gte(zoom(), 14f), lte(zoom(), 17f), gt(toNumber(get("point_count")), 1)))
            .withProperties(
                textField(get("point_count")),
            ),
        SymbolLayer("pins-layer", SOURCE)
            .withFilter(gte(zoom(), 16f))
            .withProperties(
                iconImage(get("icon-image")),
                iconSize(0.5f),
                iconOffset(listOf(-9f, -69f).toTypedArray()),
                symbolZOrder(Property.SYMBOL_Z_ORDER_SOURCE), // = order in which they were added
            )
    )

    /** Shows/hides the pins */
    var isVisible: Boolean
        @UiThread get() = layers.first().visibility.value != Property.NONE
        @UiThread set(value) {
            if (isVisible == value) return
            if (value) {
                layers.forEach { it.setProperties(visibility(Property.VISIBLE)) }
            } else {
                layers.forEach { it.setProperties(visibility(Property.NONE)) }
            }
        }

    fun getProperties(properties: JsonObject): Map<String, String> =
        properties.getProperties()


    init {
        pinsSource.isVolatile = true
        map.style?.addSource(pinsSource)
    }

    /** Show given pins. Previously shown pins are replaced with these.  */
    @UiThread fun set(pins: Collection<Pin>) {
        val mapLibreFeatures = pins.sortedBy { it.order }.map { it.toFeature() }
        pinsSource.setGeoJson(FeatureCollection.fromFeatures(mapLibreFeatures))
    }

    /** Clear pins */
    @UiThread fun clear() {
        pinsSource.clear()
    }

    companion object {
        private const val SOURCE = "pins-source"
    }
}

data class Pin(
    val position: LatLon,
    val iconName: String,
    val properties: Collection<Pair<String, String>> = emptyList(),
    val order: Int = 0
)

private fun Pin.toFeature(): Feature {
    val p = JsonObject()
    p.addProperty("icon-image", iconName)
    properties.forEach { p.addProperty(it.first, it.second) }
    return Feature.fromGeometry(position.toPoint(), p)
}

private fun JsonObject.getProperties(): Map<String, String> =
    entrySet().associate { it.key to it.value.asString }

package de.westnordost.streetcomplete.screens.main.map.components

import androidx.annotation.UiThread
import com.google.gson.JsonObject
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
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
import de.westnordost.streetcomplete.screens.main.map.maplibre.CameraUpdate
import de.westnordost.streetcomplete.screens.main.map.maplibre.clear
import de.westnordost.streetcomplete.screens.main.map.maplibre.toPoint
import de.westnordost.streetcomplete.util.ktx.nowAsEpochMilliseconds
import de.westnordost.streetcomplete.util.logs.Log
import de.westnordost.streetcomplete.util.math.enclosingBoundingBox
import org.maplibre.android.style.sources.GeoJsonOptions
import org.maplibre.geojson.Point

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

    fun getClusterExpansionZoom(feature: Feature) = pinsSource.getClusterExpansionZoom(feature)
    fun getBboxForCluster(feature: Feature): BoundingBox {
        val leaves = pinsSource.getClusterLeaves(feature, Long.MAX_VALUE, 0L)
        val ll = mutableListOf<LatLon>()
        leaves.features()?.forEach { ll.add((it.geometry()!! as Point).let { LatLon(it.latitude(), it.longitude()) }) }
        return ll.enclosingBoundingBox()
    }
    fun getCamera(feature: Feature): BoundingBox {
        CameraUpdate()
        val leaves = pinsSource.getClusterLeaves(feature, Long.MAX_VALUE, 0L)
        val ll = mutableListOf<LatLon>()
        leaves.features()?.forEach { ll.add((it.geometry()!! as Point).let { LatLon(it.latitude(), it.longitude()) }) }
        return ll.enclosingBoundingBox()
    }

    // todo:
    //  avoid clustering for multiple quests on the same element
    //  maybe replace circles with pins with the number written on them?
    val layers: List<Layer> = listOf(
        CircleLayer("pin-cluster-layer", SOURCE)
            .withFilter(all(gte(zoom(), 14f), lte(zoom(), CLUSTER_START_ZOOM), gt(toNumber(get("point_count")), 1)))
            .withProperties(
                circleColor("white"),
                circleStrokeColor("grey"),
                circleRadius(sum(toNumber(literal(10f)), sqrt(get("point_count")))),
                circleStrokeWidth(1f)
            ),
        SymbolLayer("pin-cluster-text-layer", SOURCE)
            .withFilter(all(gte(zoom(), 14f), lte(zoom(), CLUSTER_START_ZOOM), gt(toNumber(get("point_count")), 1)))
            .withProperties(
                textField(get("point_count")),
                textAllowOverlap(true) // avoid quest pins hiding number
            ),
        CircleLayer("pin-dot-layer", SOURCE)
            .withFilter(gt(zoom(), CLUSTER_START_ZOOM))
            .withProperties(
                circleColor("white"),
                circleStrokeColor("grey"),
                circleRadius(5f),
                circleStrokeWidth(1f)
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
        val mapLibreFeatures = pins.sortedBy { it.order }.distinctBy { it.position }.map { it.toFeature() }
        pinsSource.setGeoJson(FeatureCollection.fromFeatures(mapLibreFeatures))
    }

    /** Clear pins */
    @UiThread fun clear() {
        pinsSource.clear()
    }

    companion object {
        private const val SOURCE = "pins-source"
        private const val CLUSTER_START_ZOOM = 17f
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

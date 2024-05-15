package de.westnordost.streetcomplete.screens.main.map.components

import android.content.ContentResolver
import androidx.annotation.UiThread
import androidx.core.graphics.Insets
import com.google.gson.JsonObject
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.style.expressions.Expression.all
import org.maplibre.android.style.expressions.Expression.division
import org.maplibre.android.style.expressions.Expression.gte
import org.maplibre.android.style.expressions.Expression.lte
import org.maplibre.android.style.expressions.Expression.gt
import org.maplibre.android.style.expressions.Expression.get
import org.maplibre.android.style.expressions.Expression.literal
import org.maplibre.android.style.expressions.Expression.sqrt
import org.maplibre.android.style.expressions.Expression.sum
import org.maplibre.android.style.expressions.Expression.toNumber
import org.maplibre.android.style.expressions.Expression.zoom
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.Layer
import org.maplibre.android.style.layers.Property
import org.maplibre.android.style.layers.PropertyFactory.*
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonSource
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.screens.main.map.maplibre.clear
import de.westnordost.streetcomplete.screens.main.map.maplibre.getEnclosingCamera
import de.westnordost.streetcomplete.screens.main.map.maplibre.toLatLon
import de.westnordost.streetcomplete.screens.main.map.maplibre.toPoint
import de.westnordost.streetcomplete.screens.main.map.maplibre.updateCamera
import de.westnordost.streetcomplete.util.math.enclosingBoundingBox
import org.maplibre.android.style.sources.GeoJsonOptions
import org.maplibre.geojson.Point
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/** Takes care of displaying pins on the map, e.g. quest pins or pins for recent edits */
class PinsMapComponent(
    private val contentResolver: ContentResolver,
    private val map: MapLibreMap
) {
    private val pinsSource = GeoJsonSource(SOURCE,
        GeoJsonOptions()
            .withCluster(true)
            .withClusterMaxZoom(17)
    )

    val layers: List<Layer> = listOf(
        CircleLayer("pin-cluster-layer", SOURCE)
            .withFilter(all(gte(zoom(), 14f), lte(zoom(), CLUSTER_START_ZOOM), gt(toNumber(get("point_count")), 1)))
            .withProperties(
                circleColor("white"),
                circleStrokeColor("#aaaaaa"),
                circleRadius(sum(toNumber(literal(10f)), sqrt(get("point_count")))),
                circleStrokeWidth(1f)
            ),
        SymbolLayer("pin-cluster-text-layer", SOURCE)
            .withFilter(all(gte(zoom(), 14f), lte(zoom(), CLUSTER_START_ZOOM), gt(toNumber(get("point_count")), 1)))
            .withProperties(
                textField(get("point_count")),
                textSize(sum(literal(15f), division(sqrt(get("point_count")), literal(2f)))),
                textAllowOverlap(true) // avoid quest pins hiding number
            ),
        CircleLayer("pin-dot-layer", SOURCE)
            .withFilter(gt(zoom(), CLUSTER_START_ZOOM))
            .withProperties(
                circleColor("white"),
                circleStrokeColor("#aaaaaa"),
                circleRadius(4f),
                circleStrokeWidth(1f),
                circleTranslate(arrayOf(0f, -6f)), // so that it hides behind the pin
                circleTranslateAnchor(Property.CIRCLE_TRANSLATE_ANCHOR_VIEWPORT),
            ),
        SymbolLayer("pins-layer", SOURCE)
            .withFilter(gte(zoom(), 16f))
            .withProperties(
                iconImage(get("icon-image")),
                iconSize(1f),
                // better would be arrayOf(-2.5f, 0f, -7f, 2.5f) or something like that, but setting
                // different paddings per side is not supported by MapLibre Native yet
                iconPadding(-2f),
                iconOffset(listOf(-4.5f, -34.5f).toTypedArray()),
                symbolZOrder(Property.SYMBOL_Z_ORDER_SOURCE), // = order in which they were added
            )
    )

    /** Shows/hides the pins */
    @UiThread fun setVisible(value: Boolean) {
        val visibility = if (value) Property.VISIBLE else Property.NONE
        layers.forEach { it.setProperties(visibility(visibility)) }
    }

    fun getProperties(properties: JsonObject): Map<String, String> =
        properties.getProperties()


    init {
        pinsSource.isVolatile = true
        map.style?.addSource(pinsSource)
    }

    /** Show given pins. Previously shown pins are replaced with these.  */
    @UiThread fun set(pins: Collection<Pin>) {
        val features = pins.sortedBy { it.order }.distinctBy { it.position }.map { it.toFeature() }
        val mapLibreFeatures = FeatureCollection.fromFeatures(features)
        pinsSource.setGeoJson(mapLibreFeatures)
    }

    /** Clear pins */
    @UiThread fun clear() {
        pinsSource.clear()
    }

    @UiThread fun zoomToCluster(feature: Feature) {
        val leaves = pinsSource.getClusterLeaves(feature, Long.MAX_VALUE, 0L)
        val bbox = leaves.features()
            ?.mapNotNull { (it.geometry() as? Point)?.toLatLon() }
            ?.enclosingBoundingBox()
            ?: return
        val targetPos = map.getEnclosingCamera(bbox, Insets.NONE) ?: return

        // don't zoom in fully: leave some space to show the full pins, and limit max zoom
        val targetZoom = min(targetPos.zoom - 0.25, 19.0)

        val zoomDiff = abs(map.cameraPosition.zoom - targetZoom)
        val zoomTime = max(450, (zoomDiff * 450).roundToInt())

        map.updateCamera(zoomTime, contentResolver) {
            position = targetPos.position
            padding = targetPos.padding
            this.zoom = targetZoom
        }
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

package de.westnordost.streetcomplete.screens.main.map.components

import android.content.ContentResolver
import android.content.Context
import androidx.annotation.UiThread
import androidx.core.graphics.Insets
import com.google.gson.JsonObject
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.screens.main.map.createPinBitmap
import de.westnordost.streetcomplete.screens.main.map.maplibre.MapImages
import de.westnordost.streetcomplete.screens.main.map.maplibre.clear
import de.westnordost.streetcomplete.screens.main.map.maplibre.getEnclosingCamera
import de.westnordost.streetcomplete.screens.main.map.maplibre.toLatLon
import de.westnordost.streetcomplete.screens.main.map.maplibre.toPoint
import de.westnordost.streetcomplete.screens.main.map.maplibre.updateCamera
import de.westnordost.streetcomplete.util.math.enclosingBoundingBox
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.style.expressions.Expression.all
import org.maplibre.android.style.expressions.Expression.any
import org.maplibre.android.style.expressions.Expression.division
import org.maplibre.android.style.expressions.Expression.get
import org.maplibre.android.style.expressions.Expression.gt
import org.maplibre.android.style.expressions.Expression.gte
import org.maplibre.android.style.expressions.Expression.literal
import org.maplibre.android.style.expressions.Expression.log2
import org.maplibre.android.style.expressions.Expression.lte
import org.maplibre.android.style.expressions.Expression.sum
import org.maplibre.android.style.expressions.Expression.toNumber
import org.maplibre.android.style.expressions.Expression.zoom
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.Layer
import org.maplibre.android.style.layers.Property
import org.maplibre.android.style.layers.PropertyFactory.*
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonOptions
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.Point
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/** Takes care of displaying pins on the map, e.g. quest pins or pins for recent edits */
class PinsMapComponent(
    private val context: Context,
    private val contentResolver: ContentResolver,
    private val map: MapLibreMap,
    private val mapImages: MapImages,
    private val onClickPin: (properties: Map<String, String>) -> Unit
) {
    private val pinsSource = GeoJsonSource(SOURCE,
        GeoJsonOptions()
            .withCluster(true)
            .withClusterMaxZoom(CLUSTER_MAX_ZOOM)
            .withClusterRadius(55)
    )

    val layers: List<Layer> = listOf(
        SymbolLayer("pin-cluster-layer", SOURCE)
            .withFilter(all(
                gte(zoom(), 13f),
                lte(zoom(), CLUSTER_MAX_ZOOM),
                gt(toNumber(get("point_count")), 1)
            ))
            .withProperties(
                iconImage("cluster-circle"),
                iconSize(sum(literal(0.5f), division(log2(get("point_count")), literal(10f)))),
                textField(get("point_count")),
                textFont(arrayOf("Roboto Regular")),
                textOffset(arrayOf(0f, 0.1f)),
                textSize(sum(literal(15f), division(log2(get("point_count")), literal(1.5f)))),
                iconAllowOverlap(true),
                textAllowOverlap(true),
                iconIgnorePlacement(true),
                textIgnorePlacement(true),
                symbolSortKey(50f)
            ),
        CircleLayer("pin-dot-layer", SOURCE)
            .withFilter(any(
                gt(zoom(), CLUSTER_MAX_ZOOM),
                all(gte(zoom(), 14f), lte(toNumber(get("point_count")), 1))
            ))
            .withProperties(
                circleColor("white"),
                circleStrokeColor("#aaaaaa"),
                circleRadius(5f),
                circleStrokeWidth(1f),
                circleTranslate(arrayOf(0f, -8f)), // so that it hides behind the pin
                circleTranslateAnchor(Property.CIRCLE_TRANSLATE_ANCHOR_VIEWPORT),
                symbolSortKey(40f),
                iconAllowOverlap(true),
                iconIgnorePlacement(true),
            ),
        SymbolLayer("pins-layer", SOURCE)
            .withFilter(gt(zoom(), CLUSTER_MAX_ZOOM))
            .withProperties(
                iconImage(get("icon-image")),
                // constant icon size because click area would become a bit too small and more
                // importantly, dynamic size per zoom + collision doesn't work together well, it
                // results in a lot of flickering.
                iconSize(1f),

                // better would be arrayOf(-2.5f, 0f, -7f, 2.5f) or something like that, but setting
                // different paddings per side is not supported by MapLibre Native yet. See
                // https://github.com/maplibre/maplibre-native/issues/2368
                iconPadding(-2f),
                iconOffset(listOf(-4.5f, -34.5f).toTypedArray()),
                iconAllowOverlap(false),
                iconIgnorePlacement(false),
                symbolSortKey(get("icon-order")),
            )
    )

    /** Shows/hides the pins */
    @UiThread fun setVisible(value: Boolean) {
        val visibility = if (value) Property.VISIBLE else Property.NONE
        layers.forEach { it.setProperties(visibility(visibility)) }
    }

    init {
        pinsSource.isVolatile = true
        map.style?.addImageAsync("cluster-circle", context.getDrawable(R.drawable.pin_circle)!!)
        map.style?.addSource(pinsSource)
        map.addOnMapClickListener(::onClick)
    }

    /** Show given pins. Previously shown pins are replaced with these.  */
    suspend fun set(pins: Collection<Pin>) {
        val icons = pins.map { it.icon }
        mapImages.addOnce(icons) { createPinBitmap(context, it) to false }
        val features = pins.map { it.toFeature() }
        val mapLibreFeatures = FeatureCollection.fromFeatures(features)
        withContext(Dispatchers.Main) { pinsSource.setGeoJson(mapLibreFeatures) }
    }

    /** Clear pins */
    @UiThread fun clear() {
        pinsSource.clear()
    }

    private fun onClick(position: LatLng): Boolean {
        val feature = map.queryRenderedFeatures(
            map.projection.toScreenLocation(position),
            *arrayOf("pins-layer", "pin-cluster-layer")
        ).firstOrNull() ?: return false

        val properties = feature.properties()

        if (properties?.has("point_count") == true) {
            zoomToCluster(feature)
        } else {
            onClickPin(properties?.toMap().orEmpty())
        }
        return true
    }

    private fun zoomToCluster(feature: Feature) {
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

    private fun Pin.toFeature(): Feature {
        val p = JsonObject()
        p.addProperty("icon-image", context.resources.getResourceEntryName(icon))
        p.addProperty("icon-order", order + 50)
        properties.forEach { p.addProperty(it.first, it.second) }
        return Feature.fromGeometry(position.toPoint(), p)
    }

    companion object {
        private const val SOURCE = "pins-source"
        private const val CLUSTER_MAX_ZOOM = 14
    }
}

data class Pin(
    val position: LatLon,
    val icon: Int,
    val properties: Collection<Pair<String, String>> = emptyList(),
    val order: Int = 0
)

private fun JsonObject.toMap(): Map<String, String> =
    entrySet().associate { it.key to it.value.asString }

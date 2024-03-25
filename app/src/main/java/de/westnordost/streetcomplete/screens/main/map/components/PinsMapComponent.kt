package de.westnordost.streetcomplete.screens.main.map.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.RectF
import androidx.annotation.UiThread
import androidx.core.graphics.toRect
import com.google.gson.JsonObject
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.style.expressions.Expression.*
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.Layer
import org.maplibre.android.style.layers.Property
import org.maplibre.android.style.layers.PropertyFactory.*
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonSource
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.overlays.OverlayRegistry
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.screens.main.map.maplibre.clear
import de.westnordost.streetcomplete.screens.main.map.maplibre.toPoint
import de.westnordost.streetcomplete.util.ktx.dpToPx
import kotlin.math.ceil

/** Takes care of displaying pins on the map, e.g. quest pins or pins for recent edits */
class PinsMapComponent(
    private val context: Context,
    private val questTypeRegistry: QuestTypeRegistry,
    private val overlayRegistry: OverlayRegistry,
    private val map: MapLibreMap
) {
    private val pinsSource = GeoJsonSource(SOURCE)

    val layers: List<Layer> = listOf(
        CircleLayer("pin-dot-layer", SOURCE)
            .withFilter(gte(zoom(), 14f))
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
                symbolZOrder(Property.SYMBOL_Z_ORDER_SOURCE),
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

    init {
        pinsSource.isVolatile = true
        map.style?.addImagesAsync(createPinBitmaps())
        map.style?.addSource(pinsSource)
    }

    private fun createPinBitmaps(): HashMap<String, Bitmap> {
        val questIconResIds = (
            questTypeRegistry.map { it.icon } +
            overlayRegistry.map { it.icon }
        ).toSortedSet()

        val result = HashMap<String, Bitmap>(questIconResIds.size)

        val scale = 2f
        val size = context.dpToPx(71 * scale)
        val sizeInt = ceil(size).toInt()
        val iconSize = context.dpToPx(48 * scale)
        val iconPinOffset = context.dpToPx(2 * scale)
        val pinTopRightPadding = context.dpToPx(5 * scale)

        val pin = context.getDrawable(R.drawable.pin)!!
        val pinShadow = context.getDrawable(R.drawable.pin_shadow)!!

        val pinWidth = (size - pinTopRightPadding) * pin.intrinsicWidth / pin.intrinsicHeight
        val pinXOffset = size - pinTopRightPadding - pinWidth

        for (questIconResId in questIconResIds) {
            val bitmap = Bitmap.createBitmap(sizeInt, sizeInt, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            pinShadow.setBounds(0, 0, sizeInt, sizeInt)
            pinShadow.draw(canvas)
            pin.bounds = RectF(
                pinXOffset,
                pinTopRightPadding,
                size - pinTopRightPadding,
                size
            ).toRect()
            pin.draw(canvas)
            val questIcon = context.getDrawable(questIconResId)!!
            questIcon.bounds = RectF(
                pinXOffset + iconPinOffset,
                pinTopRightPadding + iconPinOffset,
                pinXOffset + iconPinOffset + iconSize,
                pinTopRightPadding + iconPinOffset + iconSize
            ).toRect()
            questIcon.draw(canvas)
            val questIconName = context.resources.getResourceEntryName(questIconResId)
            result[questIconName] = bitmap
        }

        return result
    }

    /** Show given pins. Previously shown pins are replaced with these.  */
    @UiThread fun set(pins: Collection<Pin>) {
        // do sorting here, because we can set the symbolZOrder to SYMBOL_Z_ORDER_SOURCE, which
        // is the order in which the source has the features
        val mapLibreFeatures = pins.sortedBy { -it.importance }.map { it.toFeature() }
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
    val importance: Int = 0
)

private fun Pin.toFeature(): Feature {
    val p = JsonObject()
    p.addProperty("icon-image", iconName)
    p.addProperty("symbol-sort-key", -importance.toFloat()) // still set sort key, because we may want to try it again
    properties.forEach { p.addProperty(it.first, it.second) }
    return Feature.fromGeometry(position.toPoint(), p)
}

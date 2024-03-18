package de.westnordost.streetcomplete.screens.main.map.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.annotation.UiThread
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
                iconOffset(listOf(-8f, -33f).toTypedArray()),
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
        map.style?.addImagesAsync(createPinBitmaps())
        map.style?.addSource(pinsSource)
    }

    private fun createPinBitmaps(): HashMap<String, Bitmap> {
        val questIconResIds = (
            questTypeRegistry.map { it.icon } +
            overlayRegistry.map { it.icon }
        ).toSortedSet()

        val result = HashMap<String, Bitmap>(questIconResIds.size)

        val pin = context.getDrawable(R.drawable.pin)!!
        val pinSize = context.dpToPx(66).toInt()
        val iconSize = 2 * pinSize / 3
        val iconOffsetX = 56 * pinSize / 192
        val iconOffsetY = 18 * pinSize / 192

        for (questIconResId in questIconResIds) {
            val bitmap = Bitmap.createBitmap(pinSize, pinSize, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            pin.setBounds(0, 0, pinSize, pinSize)
            pin.draw(canvas)
            val questIcon = context.getDrawable(questIconResId)!!
            questIcon.setBounds(
                iconOffsetX,
                iconOffsetY,
                iconOffsetX + iconSize,
                iconOffsetY + iconSize
            )
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
) {
    // todo: maplibre feature by lazy?
}

private fun Pin.toFeature(): Feature {
    val p = JsonObject()
    p.addProperty("icon-image", iconName)
    p.addProperty("symbol-sort-key", -importance.toFloat()) // still set sort key, because we may want to try it again
    properties.forEach { p.addProperty(it.first, it.second) }
    return Feature.fromGeometry(position.toPoint(), p)
}

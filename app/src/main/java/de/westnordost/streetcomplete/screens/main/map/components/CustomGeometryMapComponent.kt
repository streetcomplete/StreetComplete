package de.westnordost.streetcomplete.screens.main.map.components

import android.content.Context
import androidx.annotation.UiThread
import de.westnordost.streetcomplete.screens.main.map.components.FocusGeometryMapComponent.Companion
import de.westnordost.streetcomplete.screens.main.map.maplibre.clear
import de.westnordost.streetcomplete.screens.main.map.maplibre.isArea
import de.westnordost.streetcomplete.screens.main.map.maplibre.isPoint
import de.westnordost.streetcomplete.util.logs.Log
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.style.expressions.Expression.*
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.FillLayer
import org.maplibre.android.style.layers.Layer
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.Property
import org.maplibre.android.style.layers.PropertyFactory.*
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonSource

/** Allows setting any (User-Provided) Geo-Json. Reads text from "name" property */
class CustomGeometryMapComponent(
    private val context: Context,
    private val map: MapLibreMap,
) {
    private val geometrySource = GeoJsonSource(SOURCE)

    val layers: List<Layer> = listOf(
        FillLayer("custom-geo-fill", SOURCE)
            .withFilter(isArea())
            .withProperties(
                fillColor(COLOR),
                fillOpacity(0.3f)
            ),
        LineLayer("custom-geo-lines", SOURCE)
            // both polygon and line
            .withProperties(
                lineWidth(8f),
                lineColor(COLOR),
                lineOpacity(0.5f),
                lineCap(Property.LINE_CAP_ROUND)
            ),
        CircleLayer("custom-geo-circle", SOURCE)
            .withFilter(isPoint())
            .withProperties(
                circleColor(COLOR),
                circleRadius(8f),
                circleOpacity(0.6f)
            ),
        SymbolLayer("custom-geo-text", SOURCE)
            .withFilter(all(has("name"), gte(zoom(), 14)))
            .withProperties(
                textColor(COLOR),
                textFont(arrayOf("Roboto Regular")),
                textField(get("name")),
                textAllowOverlap(true),
                textIgnorePlacement(true),
                textAnchor(Property.TEXT_ANCHOR_TOP),
                textOffset(arrayOf(0f, 1f)),
                textSize(16 * context.resources.configuration.fontScale),
            ),
    )

    init {
        geometrySource.isVolatile = true
        map.style?.addSource(geometrySource)
    }

    @UiThread fun set(geoJson: String) {
        try {
            geometrySource.setGeoJson(geoJson)
        } catch (e: Exception) {
            Log.e("CustomGeometrySource", "error setting geoJson: $e")
            clear()
        }
    }

    @UiThread fun clear() {
        geometrySource.clear()
    }

    companion object {
        private const val SOURCE = "custom-geo-source"
        private const val COLOR = "#9e319e"
    }
}

package de.westnordost.streetcomplete.screens.main.map.components

import android.content.Context
import android.content.res.Resources
import androidx.annotation.UiThread
import com.google.gson.JsonObject
import de.westnordost.streetcomplete.R
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.style.expressions.Expression.*
import org.maplibre.android.style.layers.FillLayer
import org.maplibre.android.style.layers.Layer
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.Property
import org.maplibre.android.style.layers.PropertyFactory.*
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonSource
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPointGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolygonsGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.screens.main.map.Marker
import de.westnordost.streetcomplete.screens.main.map.createIconBitmap
import de.westnordost.streetcomplete.screens.main.map.maplibre.MapImages
import de.westnordost.streetcomplete.screens.main.map.maplibre.clear
import de.westnordost.streetcomplete.screens.main.map.maplibre.isArea
import de.westnordost.streetcomplete.screens.main.map.maplibre.isPoint
import de.westnordost.streetcomplete.screens.main.map.maplibre.toMapLibreGeometry
import de.westnordost.streetcomplete.screens.main.map.maplibre.toPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Manages putting some generic geometry markers with an optional drawable on the map. I.e. to
 *  show the geometry of elements surrounding the selected quest */
class GeometryMarkersMapComponent(
    private val context: Context,
    private val map: MapLibreMap,
    private val mapImages: MapImages
) {

    private val geometrySource = GeoJsonSource(SOURCE)

    private val featuresByGeometry: MutableMap<ElementGeometry, List<Feature>> = HashMap()

    val layers: List<Layer> = listOf(
        FillLayer("geo-fill", SOURCE)
            .withFilter(isArea())
            .withProperties(
                fillColor("#D140D0"),
                fillOpacity(0.3f)
            ),
        LineLayer("geo-lines", SOURCE)
            // both polygon and line
            .withProperties(
                lineWidth(10f),
                lineColor("#D140D0"),
                lineOpacity(0.5f),
                lineCap(Property.LINE_CAP_ROUND)
            ),
        SymbolLayer("geo-symbols", SOURCE)
            .withFilter(isPoint())
            .withProperties(
                iconColor("#D140D0"),
                iconImage(get("icon")),
                iconAllowOverlap(true),
                textField(get("label")),
                textAnchor(Property.TEXT_ANCHOR_TOP),
                textOffset(arrayOf(0f, 1f)),
                textSize(16 * context.resources.configuration.fontScale),
                textColor("#D140D0"),
                textFont(arrayOf("Roboto Bold")),
                textOptional(true)
            )
    )

    init {
        geometrySource.isVolatile = true
        map.style?.addSource(geometrySource)
    }

    suspend fun putAll(markers: Iterable<Marker>) {
        val icons = markers.map { it.icon ?: R.drawable.ic_preset_maki_circle }
        mapImages.addOnce(icons) {
            val name = context.resources.getResourceEntryName(it)
            val sdf = name.startsWith("ic_preset_")
            createIconBitmap(context, it, sdf) to sdf
        }
        for (marker in markers) {
            featuresByGeometry[marker.geometry] = marker.toFeatures(context.resources)
        }
        withContext(Dispatchers.Main) { update() }
    }

    @UiThread fun delete(geometry: ElementGeometry) {
        featuresByGeometry.remove(geometry)
        update()
    }

    @UiThread fun clear() {
        featuresByGeometry.clear()
        geometrySource.clear()
    }

    private fun update() {
        geometrySource.setGeoJson(FeatureCollection.fromFeatures(featuresByGeometry.values.flatten()))
    }

    companion object {
        private const val SOURCE = "geometry-source"
    }
}

private fun Marker.toFeatures(resources: Resources): List<Feature> {
    val features = ArrayList<Feature>(3)
    // point marker or any marker with title or icon
    if (icon != null || title != null || geometry is ElementPointGeometry) {
        val p = JsonObject()
        val mustHaveIcon = icon ?: R.drawable.ic_preset_maki_circle
        p.addProperty("icon", resources.getResourceEntryName(mustHaveIcon))
        if (title != null) {
            p.addProperty("label", title)
        }
        features.add(Feature.fromGeometry(geometry.center.toPoint(), p))
    }

    // polygon / polylines marker(s)
    if (geometry is ElementPolygonsGeometry || geometry is ElementPolylinesGeometry) {
        features.add(Feature.fromGeometry(geometry.toMapLibreGeometry()))
    }
    return features
}

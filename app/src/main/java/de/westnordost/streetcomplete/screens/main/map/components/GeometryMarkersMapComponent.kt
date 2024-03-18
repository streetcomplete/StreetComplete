package de.westnordost.streetcomplete.screens.main.map.components

import android.content.res.Resources
import androidx.annotation.DrawableRes
import androidx.annotation.UiThread
import com.google.gson.JsonObject
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
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
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPointGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolygonsGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.screens.main.map.maplibre.clear
import de.westnordost.streetcomplete.screens.main.map.maplibre.isArea
import de.westnordost.streetcomplete.screens.main.map.maplibre.isPoint
import de.westnordost.streetcomplete.screens.main.map.maplibre.toMapLibreGeometry
import de.westnordost.streetcomplete.screens.main.map.maplibre.toPoint

/** Manages putting some generic geometry markers with an optional drawable on the map. I.e. to
 *  show the geometry of elements surrounding the selected quest */
class GeometryMarkersMapComponent(private val resources: Resources, private val map: MapLibreMap) {

    private val geometrySource = GeoJsonSource(SOURCE)

    private val featuresByPosition: MutableMap<LatLon, List<Feature>> = HashMap()

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
        CircleLayer("geo-circle", SOURCE)
            .withFilter(all(not(has("icon")), isPoint()))
            .withProperties(
                circleColor("#D140D0"),
                circleOpacity(0.7f),
                circleRadius(12f),
                textField(get("label")),
                textAnchor(Property.TEXT_ANCHOR_LEFT),
                textJustify(Property.TEXT_JUSTIFY_LEFT),
                textOffset(arrayOf(1.5f, 0f)),
            ),
        SymbolLayer("geo-symbols", SOURCE)
            .withFilter(all(has("icon"), isPoint()))
            .withProperties(
                iconColor("#D140D0"),
                iconImage(get("icon")),
                iconAllowOverlap(true),
                textField(get("label")),
                textAnchor(Property.TEXT_ANCHOR_LEFT),
                textJustify(Property.TEXT_JUSTIFY_LEFT),
                textOffset(arrayOf(1.5f, 0f)),
                textHaloColor("white"),
                textHaloWidth(1.5f),
            )
    )

    init {
        map.style?.addSource(geometrySource)
    }

    @UiThread fun put(
        geometry: ElementGeometry,
        @DrawableRes drawableResId: Int? = null,
        title: String? = null
    ) {
        featuresByPosition.remove(geometry.center)

        val features = mutableListOf<Feature>()

        // point marker or any marker with title or icon
        if (drawableResId != null || title != null || geometry is ElementPointGeometry) {
            val p = JsonObject()
            if (drawableResId != null) {
                p.addProperty("icon", resources.getResourceEntryName(drawableResId))
            }
            if (title != null) {
                p.addProperty("label", title)
            }
            features.add(Feature.fromGeometry(geometry.center.toPoint(), p))
        }

        // polygon / polylines marker(s)
        if (geometry is ElementPolygonsGeometry || geometry is ElementPolylinesGeometry) {
            features.add(Feature.fromGeometry(geometry.toMapLibreGeometry()))
        }

        featuresByPosition[geometry.center] = features
        update()
    }

    @UiThread fun delete(geometry: ElementGeometry) {
        featuresByPosition.remove(geometry.center)
        update()
    }

    @UiThread fun clear() {
        featuresByPosition.clear()
        geometrySource.clear()
    }

    private fun update() {
        geometrySource.setGeoJson(FeatureCollection.fromFeatures(featuresByPosition.values.flatten()))
    }

    companion object {
        private const val SOURCE = "geometry-source"
    }
}

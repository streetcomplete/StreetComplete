package de.westnordost.streetcomplete.screens.main.map.components

import android.content.res.Resources
import androidx.annotation.DrawableRes
import androidx.annotation.UiThread
import com.google.gson.JsonObject
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.style.expressions.Expression.*
import com.mapbox.mapboxsdk.style.layers.CircleLayer
import com.mapbox.mapboxsdk.style.layers.FillLayer
import com.mapbox.mapboxsdk.style.layers.Layer
import com.mapbox.mapboxsdk.style.layers.LineLayer
import com.mapbox.mapboxsdk.style.layers.Property
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.*
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPointGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolygonsGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.screens.main.map.maplibre.clear
import de.westnordost.streetcomplete.screens.main.map.maplibre.toMapLibreFeature
import de.westnordost.streetcomplete.screens.main.map.maplibre.toPoint

/** Manages putting some generic geometry markers with an optional drawable on the map. I.e. to
 *  show the geometry of elements surrounding the selected quest */
class GeometryMarkersMapComponent(private val resources: Resources, private val map: MapboxMap) {

    private val geometrySource = GeoJsonSource(SOURCE)

    private val featuresByPosition: MutableMap<LatLon, List<Feature>> = HashMap()

    val layers: List<Layer> = listOf(
        FillLayer("geo-fill", SOURCE)
            .withFilter(eq(get("type"), literal("polygon")))
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
            .withFilter(not(has("icon")))
            .withProperties(
                circleColor("#D140D0"),
                circleOpacity(0.7f),
                textField("{label}"),
                textAnchor(Property.TEXT_ANCHOR_LEFT),
                textOffset(arrayOf(1.5f, 0f)),
                textMaxWidth(5f),
            ),
        SymbolLayer("geo-symbols", SOURCE)
            .withFilter(has("icon"))
            .withProperties(
                iconColor("#D140D0"),
                iconImage("{icon}"),
                textField("{label}"),
                textOffset(arrayOf(1.5f, 0f)),
                textMaxWidth(5f),
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
            features.add(geometry.toMapLibreFeature())
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

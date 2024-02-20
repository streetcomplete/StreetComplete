package de.westnordost.streetcomplete.screens.main.map.components

import android.content.res.Resources
import androidx.annotation.DrawableRes
import com.google.gson.JsonObject
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.geojson.Polygon
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPointGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolygonsGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.screens.MainActivity
import de.westnordost.streetcomplete.screens.main.map.MainMapFragment
import de.westnordost.streetcomplete.screens.main.map.maplibre.pointFromGeometry
import de.westnordost.streetcomplete.screens.main.map.tangram.KtMapController
import de.westnordost.streetcomplete.util.math.centerPointOfPolyline

/** Manages putting some generic geometry markers with an optional drawable on the map. I.e. to
 *  show the geometry of elements surrounding the selected quest */
class GeometryMarkersMapComponent(private val resources: Resources, private val ctrl: KtMapController) {

    private val featuresByPosition: MutableMap<LatLon, List<Feature>> = HashMap()

    @Synchronized fun put(
        geometry: ElementGeometry,
        @DrawableRes drawableResId: Int? = null,
        title: String? = null
    ) {
        val center = geometry.center
        delete(geometry)

        val features = mutableListOf<Feature>()

        // todo: symbols not showing (icon and text)
        //  maybe related to collision?
        //  maybe we need a second symbolManager for this?

        // point / icon marker
        if (drawableResId != null || geometry is ElementPointGeometry) {
            if (drawableResId != null) {
                val p = JsonObject()
                p.addProperty("icon", resources.getResourceEntryName(drawableResId))
                if (title != null) { // with text
                    val escapedTitle = title
                        .replace('\n', ' ')
                        .replace("'", "''")
                        .replace("\"", "\\\"")
                    p.addProperty("label", escapedTitle)
                }
                features.add(Feature.fromGeometry(pointFromGeometry(geometry), p))
            } else {
                features.add(Feature.fromGeometry(pointFromGeometry(geometry)))
            }
        }

        // text only marker
        if (title != null && drawableResId == null) {
            val escapedTitle = title
                .replace('\n', ' ')
                .replace("'", "''")
                .replace("\"", "\\\"")
            val p = JsonObject()
            p.addProperty("label", escapedTitle)
            features.add(Feature.fromGeometry(pointFromGeometry(geometry), p))
        }

        // polygon / polylines marker(s)
        if (geometry is ElementPolygonsGeometry || geometry is ElementPolylinesGeometry) {
            if (geometry is ElementPolygonsGeometry) {
                val points = geometry.polygons.map { it.map { Point.fromLngLat(it.longitude, it.latitude) } }
                features.add(Feature.fromGeometry(Polygon.fromLngLats(points)))
            }

            /* Polygons should be styled to have a more opaque outline. Due to a technical
             *  limitation in tangram-es, these have to be actually two markers then. */
            // todo: still necessary in maplibre?
            val polylines: ElementPolylinesGeometry = when (geometry) {
                is ElementPolygonsGeometry -> ElementPolylinesGeometry(geometry.polygons, geometry.polygons.first().centerPointOfPolyline())
                is ElementPolylinesGeometry -> geometry
                else -> throw IllegalStateException()
            }
            val points = polylines.polylines.map { it.map { Point.fromLngLat(it.longitude, it.latitude) } }
            features.add(Feature.fromGeometry(Polygon.fromLngLats(points)))
        }

        featuresByPosition[center] = features
        // todo: this is resetting the whole source for every single marker, adding single markers is not possible
        //  annotation managers work the same way (internally)
        MainActivity.activity?.runOnUiThread { MainMapFragment.geometrySource?.setGeoJson(FeatureCollection.fromFeatures(featuresByPosition.values.flatten())) }
    }

    @Synchronized fun delete(geometry: ElementGeometry) {
        val pos = geometry.center
        featuresByPosition.remove(pos)
        MainActivity.activity?.runOnUiThread { MainMapFragment.geometrySource?.setGeoJson(FeatureCollection.fromFeatures(featuresByPosition.values.flatten())) }
    }

    @Synchronized fun clear() {
        val fc: FeatureCollection? = null
        MainActivity.activity?.runOnUiThread { MainMapFragment.geometrySource?.setGeoJson(fc) }
        featuresByPosition.clear()
    }

    companion object {
        private const val color = "#D140D0"
        private const val pointOpacity = 0.7f
        private const val lineOpacity = 0.5f
        private const val areaOpacity = 0.3f
        private const val lineWidth = 6f
        private const val pointSize = 16
    }
}

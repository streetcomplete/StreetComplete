package de.westnordost.streetcomplete.map.components

import android.content.res.Resources
import android.graphics.drawable.BitmapDrawable
import androidx.annotation.DrawableRes
import com.mapzen.tangram.geometry.Polygon
import com.mapzen.tangram.geometry.Polyline
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPointGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolygonsGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.ktx.getBitmapDrawable
import de.westnordost.streetcomplete.map.tangram.KtMapController
import de.westnordost.streetcomplete.map.tangram.Marker
import de.westnordost.streetcomplete.map.tangram.toLngLat

/** Manages putting some generic geometry markers with an optional drawable on the map */
class GeometryMarkersMapComponent(private val resources: Resources, private val ctrl: KtMapController) {

    // markers: LatLon -> Marker Id
    private val markerIdsByPosition: MutableMap<LatLon, List<Long>> = HashMap()

    // cache for all drawable res ids supplied so far
    private val drawables: MutableMap<Int, BitmapDrawable> = HashMap()

    @Synchronized fun put(geometry: ElementGeometry, @DrawableRes drawableResId: Int?) {
        val center = geometry.center
        delete(geometry)

        val markers = mutableListOf<Marker>()

        if (drawableResId != null || geometry is ElementPointGeometry) {
            val marker = ctrl.addMarker()
            val size: Int
            val color: String
            if (drawableResId != null) {
                val drawable = getBitmapDrawable(drawableResId)
                marker.setDrawable(drawable)
                size = (drawable.intrinsicWidth / resources.displayMetrics.density).toInt()
                color = "white"
            } else {
                size = pointSize
                color = geometryColor
            }
            marker.setStylingFromString("""
            {
                style: 'geometry-points',
                color: '$color',
                size: ${size}px,
                order: 2000,
                collide: false
            }
            """.trimIndent())
            marker.setPoint(center)
            markers.add(marker)
        }

        if (geometry is ElementPolygonsGeometry) {
            val marker = ctrl.addMarker()
            marker.setStylingFromString("""
            {
                style: 'geometry-polygons',
                color: '$geometryColor',
                order: 1800,
                collide: false
            }
            """.trimIndent())
            marker.setPolygon(Polygon(
                geometry.polygons.map { polygon -> polygon.map { it.toLngLat() }},
                mapOf("type" to "poly")
            ))
            markers.add(marker)
        } else if (geometry is ElementPolylinesGeometry) {
            val marker = ctrl.addMarker()
            marker.setStylingFromString("""
            {
                style: 'geometry-lines',
                width: ${lineWidth}px,
                color: '$geometryColor',
                order: 1800,
                collide: false
            }
            """.trimIndent())
            marker.setPolyline(Polyline(
                geometry.polylines.first().map { it.toLngLat() },
                mapOf("type" to "line")
            ))
            markers.add(marker)
        }

        markerIdsByPosition[center] = markers.map { it.markerId }
    }

    @Synchronized fun delete(geometry: ElementGeometry) {
        val pos = geometry.center
        val markerIds = markerIdsByPosition[pos] ?: return
        markerIdsByPosition.remove(pos)
        markerIds.forEach { ctrl.removeMarker(it) }
    }

    @Synchronized fun clear() {
        markerIdsByPosition.values.forEach { markerIds ->
            markerIds.forEach { ctrl.removeMarker(it) }
        }
        markerIdsByPosition.clear()
    }

    private fun getBitmapDrawable(@DrawableRes drawableResId: Int): BitmapDrawable {
        if (drawables[drawableResId] == null) {
            drawables[drawableResId] = resources.getBitmapDrawable(drawableResId)
        }
        return drawables[drawableResId]!!
    }

    companion object {
        private const val geometryColor = "#66D140D0"
        private const val lineWidth = 8
        private const val pointSize = 16
    }
}

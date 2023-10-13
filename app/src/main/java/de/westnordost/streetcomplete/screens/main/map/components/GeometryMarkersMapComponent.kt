package de.westnordost.streetcomplete.screens.main.map.components

import android.content.res.Resources
import android.graphics.drawable.BitmapDrawable
import androidx.annotation.DrawableRes
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPointGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolygonsGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.screens.main.map.tangram.KtMapController
import de.westnordost.streetcomplete.screens.main.map.tangram.Marker
import de.westnordost.streetcomplete.screens.main.map.tangram.toTangramGeometry
import de.westnordost.streetcomplete.util.ktx.getBitmapDrawable
import de.westnordost.streetcomplete.util.math.centerPointOfPolyline

/** Manages putting some generic geometry markers with an optional drawable on the map. I.e. to
 *  show the geometry of elements surrounding the selected quest */
class GeometryMarkersMapComponent(private val resources: Resources, private val ctrl: KtMapController) {

    // markers: LatLon -> Marker Id
    private val markerIdsByPosition: MutableMap<LatLon, List<Long>> = HashMap()

    // cache for all drawable res ids supplied so far
    private val drawables: MutableMap<Int, BitmapDrawable> = HashMap()

    @Synchronized fun put(
        geometry: ElementGeometry,
        @DrawableRes drawableResId: Int? = null,
        title: String? = null
    ) {
        val center = geometry.center
        delete(geometry)

        val markers = mutableListOf<Marker>()
        var iconSize = 0

        // point / icon marker
        if (drawableResId != null || geometry is ElementPointGeometry) {
            val marker = ctrl.addMarker()
            val color: String
            if (drawableResId != null) {
                val drawable = getBitmapDrawable(drawableResId)
                marker.setDrawable(drawable)
                iconSize = (drawable.bitmap.width / resources.displayMetrics.density).toInt()
                color = pointColorOpaque
            } else {
                iconSize = pointSize
                color = pointColor
            }
            marker.setStylingFromString("""
            {
                style: 'geometry-points',
                color: '$color',
                size: ${iconSize}px,
                collide: false
            }
            """.trimIndent())
            marker.setPoint(geometry.center)
            markers.add(marker)
        }

        // text marker
        if (title != null) {
            val marker = ctrl.addMarker()
            val escapedTitle = title
                .replace('\n', ' ')
                .replace("'", "''")
                .replace("\"", "\\\"")
            marker.setStylingFromString("""
            {
                style: 'text',
                text_source: 'function() { return "$escapedTitle"; }',
                anchor: "bottom",
                priority: 1,
                font: {
                    family: global.text_font_family,
                    fill: global.text_fill_color,
                    size: global.text_size,
                    stroke: global.text_stroke
                },
                offset: [0, ${iconSize / 2 + 2}px],
                collide: true
            }
            """.trimIndent())
            marker.setPoint(geometry.center)
            markers.add(marker)
        }

        // polygon / polylines marker(s)
        if (geometry is ElementPolygonsGeometry || geometry is ElementPolylinesGeometry) {

            if (geometry is ElementPolygonsGeometry) {
                for (polygon in geometry.toTangramGeometry()) {
                    val marker = ctrl.addMarker()
                    marker.setStylingFromString("""
                    {
                        style: 'geometry-polygons',
                        color: '$areaColor',
                        order: 2000,
                        collide: false
                    }
                    """.trimIndent())
                    marker.setPolygon(polygon)
                    markers.add(marker)
                }
            }

            /* Polygons should be styled to have a more opaque outline. Due to a technical
             *  limitation in tangram-es, these have to be actually two markers then. */
            val polylines: ElementPolylinesGeometry = when (geometry) {
                is ElementPolygonsGeometry -> ElementPolylinesGeometry(geometry.polygons, geometry.polygons.first().centerPointOfPolyline())
                is ElementPolylinesGeometry -> geometry
                else -> throw IllegalStateException()
            }
            for (polyline in polylines.toTangramGeometry()) {
                val marker = ctrl.addMarker()
                marker.setStylingFromString("""
                {
                    style: 'geometry-lines',
                    width: ${lineWidth}px,
                    color: '$lineColor',
                    order: 2000,
                    collide: false,
                    cap: round,
                    join: round
                }
                """.trimIndent())
                marker.setPolyline(polyline)
                markers.add(marker)
            }
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
        private const val areaColor = "#22D140D0"
        private const val lineColor = "#44D140D0"
        private const val pointColor = "#88D140D0"
        private const val pointColorOpaque = "#FFD140D0"
        private const val lineWidth = 6
        private const val pointSize = 16
    }
}

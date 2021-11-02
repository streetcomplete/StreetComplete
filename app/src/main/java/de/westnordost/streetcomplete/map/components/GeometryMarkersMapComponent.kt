package de.westnordost.streetcomplete.map.components

import android.content.res.Resources
import android.graphics.drawable.BitmapDrawable
import androidx.annotation.DrawableRes
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPointGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolygonsGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.ktx.getBitmapDrawable
import de.westnordost.streetcomplete.map.tangram.KtMapController
import de.westnordost.streetcomplete.map.tangram.Marker
import de.westnordost.streetcomplete.map.tangram.setElementGeometry

/** Manages putting some generic geometry markers with an optional drawable on the map */
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

        if (drawableResId != null || geometry is ElementPointGeometry) {
            val marker = ctrl.addMarker()
            val color: String
            if (drawableResId != null) {
                val drawable = getBitmapDrawable(drawableResId)
                marker.setDrawable(drawable)
                iconSize = (drawable.intrinsicWidth / resources.displayMetrics.density).toInt()
                color = "white"
            } else {
                iconSize = pointSize
                color = lineColor
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

        if (title != null) {
            val marker = ctrl.addMarker()
            marker.setStylingFromString("""
            {
                style: 'text',
                text_source: "function() { return '$title'; }",
                anchor: ${if (iconSize > 0) "bottom" else "center"},
                priority: 1,
                font: {
                    family: 'Roboto',
                    fill: '#124',
                    size: '17px',
                    stroke: { color: 'white', width: 2.5px }
                },
                offset: [0, ${iconSize / 2 + 2}px],
                collide: true
            }
            """.trimIndent())
            marker.setElementGeometry(geometry)
            markers.add(marker)
        }

        if (geometry is ElementPolygonsGeometry) {
            val marker = ctrl.addMarker()
            marker.setStylingFromString("""
            {
                style: 'geometry-polygons',
                color: '$areaColor',
                order: 2000,
                collide: false
            }
            """.trimIndent())
            marker.setElementGeometry(geometry)
            markers.add(marker)
        } else if (geometry is ElementPolylinesGeometry) {
            val marker = ctrl.addMarker()
            marker.setStylingFromString("""
            {
                style: 'geometry-lines',
                width: ${lineWidth}px,
                color: '$lineColor',
                order: 2000,
                collide: false
            }
            """.trimIndent())
            marker.setElementGeometry(geometry)
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
        private const val areaColor = "#22D140D0"
        private const val lineColor = "#44D140D0"
        private const val lineWidth = 8
        private const val pointSize = 16
    }
}

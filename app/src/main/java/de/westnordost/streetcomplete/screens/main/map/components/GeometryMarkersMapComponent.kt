package de.westnordost.streetcomplete.screens.main.map.components

import android.content.res.Resources
import android.graphics.drawable.BitmapDrawable
import androidx.annotation.DrawableRes
import com.mapbox.mapboxsdk.plugins.annotation.Circle
import com.mapbox.mapboxsdk.plugins.annotation.CircleOptions
import com.mapbox.mapboxsdk.plugins.annotation.Fill
import com.mapbox.mapboxsdk.plugins.annotation.FillOptions
import com.mapbox.mapboxsdk.plugins.annotation.Line
import com.mapbox.mapboxsdk.plugins.annotation.LineOptions
import com.mapbox.mapboxsdk.plugins.annotation.Symbol
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions
import com.mapbox.mapboxsdk.style.layers.Property
import de.westnordost.streetcomplete.data.maptiles.toLatLng
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPointGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolygonsGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.screens.MainActivity
import de.westnordost.streetcomplete.screens.main.map.MainMapFragment
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

    // annotations have an id, but we can delete it only using the actual annotation
    private val annotationsByPosition: MutableMap<LatLon, List<com.mapbox.mapboxsdk.plugins.annotation.Annotation<*>>> = HashMap()

    @Synchronized fun put(
        geometry: ElementGeometry,
        @DrawableRes drawableResId: Int? = null,
        title: String? = null
    ) {
        val center = geometry.center
        delete(geometry)

        val markers = mutableListOf<Marker>()
        val annotations = mutableListOf<com.mapbox.mapboxsdk.plugins.annotation.Annotation<*>>()
        var iconSize = 0

        // todo: symbols not showing (icon and text)
        //  maybe related to collision?
        //  maybe we need a second symbolManager for this?
        // todo2: lines/fill/circles show above symbols, but should actually show below

        // point / icon marker
        if (drawableResId != null || geometry is ElementPointGeometry) {
            val marker = ctrl.addMarker()
            val color: String
            if (drawableResId != null) {
                val drawable = getBitmapDrawable(drawableResId)
                marker.setDrawable(drawable)
                iconSize = (drawable.bitmap.width / resources.displayMetrics.density).toInt()
                color = pointColorOpaque
                MainActivity.activity?.runOnUiThread {
                    val symbol = MainMapFragment.geometrySymbolManager!!.create(SymbolOptions()
                        .withLatLng(center.toLatLng())
                        .withIconImage(resources.getResourceEntryName(drawableResId)) // todo: pinIcons not loaded in the style, but no priority
                    )
                    annotations.add(symbol)
                }
            } else {
                iconSize = pointSize
                color = pointColor
                MainActivity.activity?.runOnUiThread {
                    val circle = MainMapFragment.geometryCircleManger!!.create(CircleOptions()
                        .withLatLng(center.toLatLng())
                        .withCircleRadius(8f)
                        .withCircleColor("#D140D0")
                        .withCircleOpacity(0.7f)
                    )
                    annotations.add(circle)
                }
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
            MainActivity.activity?.runOnUiThread {
                val symbol = MainMapFragment.geometrySymbolManager!!.create(SymbolOptions()
                    .withLatLng(center.toLatLng())
                    .withTextField(escapedTitle)
                    .withTextOffset(arrayOf(0f, 1f))
                    .withTextMaxWidth(5f)
                    .withTextAnchor(Property.TEXT_ANCHOR_TOP)
                )
                annotations.add(symbol)
            }
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
                MainActivity.activity?.runOnUiThread {
                    val fill = MainMapFragment.geometryFillManager!!.create(FillOptions()
                            .withLatLngs(geometry.polygons.map { it.map { it.toLatLng() } })
                            .withFillColor("#D140D0")
                            .withFillOpacity(0.3f)
                    )
                    annotations.add(fill)
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
            MainActivity.activity?.runOnUiThread {
                val options = polylines.polylines.map { line ->
                    LineOptions()
                        .withLatLngs(line.map { it.toLatLng() })
                        .withLineColor("#D140D0")
                        .withLineWidth(6f)
                        .withLineOpacity(0.5f)
                }
                val lines = MainMapFragment.geometryLineManager!!.create(options)
                annotations.addAll(lines)
            }
        }

        markerIdsByPosition[center] = markers.map { it.markerId }
        annotationsByPosition[center] = annotations
    }

    @Synchronized fun delete(geometry: ElementGeometry) {
        val pos = geometry.center
        val markerIds = markerIdsByPosition[pos] ?: return
        markerIdsByPosition.remove(pos)
        markerIds.forEach { ctrl.removeMarker(it) }

        val annotations = annotationsByPosition[pos] ?: return
        annotationsByPosition.remove(pos)
        MainActivity.activity?.runOnUiThread {
            removeAnnotations(annotations)
        }
    }

    @Synchronized fun clear() {
        markerIdsByPosition.values.forEach { markerIds ->
            markerIds.forEach { ctrl.removeMarker(it) }
        }
        markerIdsByPosition.clear()
        MainActivity.activity?.runOnUiThread {
            removeAnnotations(annotationsByPosition.values.flatten())
        }
        annotationsByPosition.clear()
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

private fun removeAnnotations(annotations: Collection<com.mapbox.mapboxsdk.plugins.annotation.Annotation<*>>) {
    MainMapFragment.geometrySymbolManager!!.delete(annotations.filterIsInstance<Symbol>())
    MainMapFragment.geometryCircleManger!!.delete(annotations.filterIsInstance<Circle>())
    MainMapFragment.geometryLineManager!!.delete(annotations.filterIsInstance<Line>())
    MainMapFragment.geometryFillManager!!.delete(annotations.filterIsInstance<Fill>())
}

package de.westnordost.streetcomplete.screens.main.map.components

import android.content.res.Resources
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
import de.westnordost.streetcomplete.util.math.centerPointOfPolyline

/** Manages putting some generic geometry markers with an optional drawable on the map. I.e. to
 *  show the geometry of elements surrounding the selected quest */
class GeometryMarkersMapComponent(private val resources: Resources, private val ctrl: KtMapController) {

    // annotations have an id, but we can delete it only using the actual annotation
    private val annotationsByPosition: MutableMap<LatLon, List<com.mapbox.mapboxsdk.plugins.annotation.Annotation<*>>> = HashMap()

    @Synchronized fun put(
        geometry: ElementGeometry,
        @DrawableRes drawableResId: Int? = null,
        title: String? = null
    ) {
        val center = geometry.center
        delete(geometry)

        val annotations = mutableListOf<com.mapbox.mapboxsdk.plugins.annotation.Annotation<*>>()

        // todo: symbols not showing (icon and text)
        //  maybe related to collision?
        //  maybe we need a second symbolManager for this?
        // todo2: lines/fill/circles show above symbols, but should actually show below

        // point / icon marker
        if (drawableResId != null || geometry is ElementPointGeometry) {
            if (drawableResId != null) {
                MainActivity.activity?.runOnUiThread {
                    val symbol = MainMapFragment.geometrySymbolManager!!.create(SymbolOptions()
                        .withLatLng(center.toLatLng())
                        .withIconImage(resources.getResourceEntryName(drawableResId))
                        .withIconColor(color) // does not work...
                    )
                    annotations.add(symbol)
                }
            } else {
                MainActivity.activity?.runOnUiThread {
                    val circle = MainMapFragment.geometryCircleManger!!.create(CircleOptions()
                        .withLatLng(center.toLatLng())
                        .withCircleRadius(8f)
                        .withCircleColor(color)
                        .withCircleOpacity(pointOpacity)
                    )
                    annotations.add(circle)
                }
            }
        }

        // text marker
        if (title != null) {
            val escapedTitle = title
                .replace('\n', ' ')
                .replace("'", "''")
                .replace("\"", "\\\"")
            MainActivity.activity?.runOnUiThread { // todo: does not work
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
                MainActivity.activity?.runOnUiThread {
                    val fill = MainMapFragment.geometryFillManager!!.create(FillOptions()
                            .withLatLngs(geometry.polygons.map { it.map { it.toLatLng() } })
                            .withFillColor(color)
                            .withFillOpacity(areaOpacity)
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
            MainActivity.activity?.runOnUiThread {
                val options = polylines.polylines.map { line ->
                    LineOptions()
                        .withLatLngs(line.map { it.toLatLng() })
                        .withLineColor(color)
                        .withLineWidth(lineWidth)
                        .withLineOpacity(lineOpacity)
                }
                val lines = MainMapFragment.geometryLineManager!!.create(options)
                annotations.addAll(lines)
            }
        }

        annotationsByPosition[center] = annotations
    }

    @Synchronized fun delete(geometry: ElementGeometry) {
        val pos = geometry.center
        val annotations = annotationsByPosition[pos] ?: return
        annotationsByPosition.remove(pos)
        MainActivity.activity?.runOnUiThread {
            removeAnnotations(annotations)
        }
    }

    @Synchronized fun clear() {
        MainActivity.activity?.runOnUiThread {
            removeAnnotations(annotationsByPosition.values.flatten())
        }
        annotationsByPosition.clear()
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

private fun removeAnnotations(annotations: Collection<com.mapbox.mapboxsdk.plugins.annotation.Annotation<*>>) {
    MainMapFragment.geometrySymbolManager!!.delete(annotations.filterIsInstance<Symbol>())
    MainMapFragment.geometryCircleManger!!.delete(annotations.filterIsInstance<Circle>())
    MainMapFragment.geometryLineManager!!.delete(annotations.filterIsInstance<Line>())
    MainMapFragment.geometryFillManager!!.delete(annotations.filterIsInstance<Fill>())
}

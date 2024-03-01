package de.westnordost.streetcomplete.screens.main.map.tangram

import android.graphics.PointF
import android.graphics.RectF
import com.mapzen.tangram.LngLat
import com.mapzen.tangram.geometry.Geometry
import com.mapzen.tangram.geometry.Point
import com.mapzen.tangram.geometry.Polygon
import com.mapzen.tangram.geometry.Polyline
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPointGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolygonsGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.util.math.distanceTo
import de.westnordost.streetcomplete.util.math.isInPolygon
import de.westnordost.streetcomplete.util.math.isRingDefinedClockwise
import de.westnordost.streetcomplete.util.math.measuredArea

fun ElementGeometry.toTangramGeometry(properties: Map<String, String> = emptyMap()): List<Geometry> = when (this) {
    is ElementPolylinesGeometry -> toTangramGeometry(properties)
    is ElementPolygonsGeometry -> toTangramGeometry(properties)
    is ElementPointGeometry -> toTangramGeometry(properties)
}

fun ElementPointGeometry.toTangramGeometry(properties: Map<String, String> = emptyMap()): List<Point> =
    listOf(Point(center.toLngLat(), properties + ("type" to "point")))

fun ElementPolylinesGeometry.toTangramGeometry(properties: Map<String, String> = emptyMap()): List<Polyline> =
    polylines.map { polyline ->
        Polyline(polyline.map { it.toLngLat() }, properties + ("type" to "line"))
    }

fun ElementPolygonsGeometry.toTangramGeometry(properties: Map<String, String> = emptyMap()): List<Polygon> {
    val outerRings = mutableListOf<List<LatLon>>()
    val innerRings = mutableListOf<List<LatLon>>()
    if (polygons.size == 1) {
        outerRings.add(polygons.first())
    } else {
        polygons.forEach {
            if (it.isRingDefinedClockwise()) innerRings.add(it) else outerRings.add(it)
        }
    }

    // outerRings must be sorted size ascending to correctly handle outer rings within holes
    // of larger polygons.
    outerRings.sortBy { it.measuredArea() }

    return outerRings.map { outerRing ->
        val rings = mutableListOf<List<LngLat>>()
        rings.add(outerRing.map { it.toLngLat() })
        for (innerRing in innerRings.toList()) {
            if (innerRing[0].isInPolygon(outerRing)) {
                innerRings.remove(innerRing)
                rings.add(innerRing.map { it.toLngLat() })
            }
        }
        Polygon(rings, properties + ("type" to "poly"))
    }
}

fun LngLat.toLatLon(): LatLon = LatLon(latitude, longitude)

fun LatLon.toLngLat(): LngLat = LngLat(longitude, latitude)

fun KtMapController.screenAreaContains(g: ElementGeometry, offset: RectF): Boolean {
    val p = PointF()
    val mapView = glViewHolder!!.view
    return when (g) {
        is ElementPolylinesGeometry -> g.polylines
        is ElementPolygonsGeometry -> g.polygons
        else -> listOf(listOf(g.center))
    }.flatten().all {
        latLonToScreenPosition(it, p, false)
            && p.x >= offset.left
            && p.x <= mapView.width - offset.right
            && p.y >= offset.top
            && p.y <= mapView.height - offset.bottom
    }
}

fun KtMapController.screenBottomToCenterDistance(): Double? {
    val view = glViewHolder?.view ?: return null
    val w = view.width
    val h = view.height
    if (w == 0 || h == 0) return null

    val center = screenPositionToLatLon(PointF(w / 2f, h / 2f)) ?: return null
    val bottom = screenPositionToLatLon(PointF(w / 2f, h * 1f)) ?: return null
    return center.distanceTo(bottom)
}

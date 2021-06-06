package de.westnordost.streetcomplete.map.tangram

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

fun ElementGeometry.toTangramGeometry(): List<Geometry> = when(this) {
    is ElementPolylinesGeometry -> {
        polylines.map { polyline ->
            Polyline(polyline.map { it.toLngLat() }, mapOf("type" to "line"))
        }
    }
    is ElementPolygonsGeometry -> {
        listOf(
            Polygon(
                polygons.map { polygon ->
                    polygon.map { it.toLngLat() }
                },
                mapOf("type" to "poly")
            )
        )
    }
    is ElementPointGeometry -> {
        listOf(Point(center.toLngLat(), mapOf("type" to "point")))
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

package de.westnordost.streetcomplete.map.tangram

import com.mapzen.tangram.LngLat
import com.mapzen.tangram.geometry.Geometry
import com.mapzen.tangram.geometry.Point
import com.mapzen.tangram.geometry.Polygon
import com.mapzen.tangram.geometry.Polyline
import de.westnordost.osmapi.map.data.LatLon
import de.westnordost.osmapi.map.data.OsmLatLon
import de.westnordost.streetcomplete.data.osm.ElementGeometry
import de.westnordost.streetcomplete.data.osm.ElementPointGeometry
import de.westnordost.streetcomplete.data.osm.ElementPolygonsGeometry
import de.westnordost.streetcomplete.data.osm.ElementPolylinesGeometry

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

fun LngLat.toLatLon(): LatLon = OsmLatLon(latitude, longitude)

fun LatLon.toLngLat(): LngLat = LngLat(longitude, latitude)

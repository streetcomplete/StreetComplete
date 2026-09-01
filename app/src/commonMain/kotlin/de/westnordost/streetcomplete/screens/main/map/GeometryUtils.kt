package de.westnordost.streetcomplete.screens.main.map

import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPointGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolygonsGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.util.math.isInPolygon
import de.westnordost.streetcomplete.util.math.isRingDefinedClockwise
import de.westnordost.streetcomplete.util.math.measuredArea
import org.maplibre.spatialk.geojson.Geometry
import org.maplibre.spatialk.geojson.LineString
import org.maplibre.spatialk.geojson.MultiLineString
import org.maplibre.spatialk.geojson.MultiPolygon
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Polygon
import org.maplibre.spatialk.geojson.Position
import org.maplibre.spatialk.geojson.BoundingBox as MapLibreBoundingBox

fun LatLon.toPosition(): Position =
    Position(longitude = longitude, latitude = latitude)

fun MapLibreBoundingBox.toStreetCompleteBoundingBox() =
    de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox(
        minLatitude = southwest.latitude,
        minLongitude = southwest.longitude,
        maxLatitude = northeast.latitude,
        maxLongitude = northeast.longitude,
    )

fun List<LatLon>.toLineGeometry(): LineString? =
    if (size < 2) null else LineString(map { it.toPosition() })

fun List<List<LatLon>>.toMultiLineGeometry(): MultiLineString =
    MultiLineString(mapNotNull { line -> line.toLineGeometry()?.coordinates })

fun ElementGeometry.toGeometry(): Geometry = when (this) {
    is ElementPointGeometry -> Point(center.toPosition())
    is ElementPolylinesGeometry -> toGeometry()
    is ElementPolygonsGeometry -> toGeometry()
}

private fun ElementPolylinesGeometry.toGeometry(): Geometry =
    if (polylines.size == 1) {
        LineString(polylines.single().map { it.toPosition() })
    } else {
        MultiLineString(polylines.map { line -> line.map { it.toPosition() } })
    }

private fun ElementPolygonsGeometry.toGeometry(): Geometry {
    val outerRings = mutableListOf<List<LatLon>>()
    val innerRings = mutableListOf<List<LatLon>>()
    if (polygons.size == 1) {
        outerRings += polygons.first()
    } else {
        polygons.forEach { ring ->
            if (ring.isRingDefinedClockwise()) innerRings += ring else outerRings += ring
        }
    }

    if (outerRings.size == 1) {
        return Polygon((outerRings + innerRings).map { ring -> ring.map { it.toPosition() } })
    }

    // Allocate a hole to the smallest outer ring that contains it.
    outerRings.sortBy { it.measuredArea() }
    val groupedRings = outerRings.map { outerRing ->
        buildList {
            add(outerRing.map { it.toPosition() })
            innerRings.toList().forEach { innerRing ->
                if (innerRing.first().isInPolygon(outerRing)) {
                    innerRings.remove(innerRing)
                    add(innerRing.map { it.toPosition() })
                }
            }
        }
    }
    return MultiPolygon(groupedRings)
}

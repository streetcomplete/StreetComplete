package de.westnordost.streetcomplete.screens.main.map2

import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPointGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolygonsGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.util.math.isInPolygon
import de.westnordost.streetcomplete.util.math.isRingDefinedClockwise
import de.westnordost.streetcomplete.util.math.measuredArea
import io.github.dellisd.spatialk.geojson.Geometry
import io.github.dellisd.spatialk.geojson.LineString
import io.github.dellisd.spatialk.geojson.MultiLineString
import io.github.dellisd.spatialk.geojson.MultiPolygon
import io.github.dellisd.spatialk.geojson.Point
import io.github.dellisd.spatialk.geojson.Polygon
import io.github.dellisd.spatialk.geojson.Position

typealias GeoJsonBoundingBox = io.github.dellisd.spatialk.geojson.BoundingBox

fun BoundingBox.toGeoJsonBoundingBox(): GeoJsonBoundingBox =
    GeoJsonBoundingBox(
        west = min.longitude,
        south = min.latitude,
        east = max.longitude,
        north = max.latitude
    )

fun GeoJsonBoundingBox.toBoundingBox(): BoundingBox =
    BoundingBox(
        minLatitude = southwest.latitude,
        minLongitude = southwest.longitude,
        maxLatitude = northeast.latitude,
        maxLongitude = northeast.longitude
    )

fun ElementGeometry.toGeoJson(): Geometry = when (this) {
    is ElementPointGeometry -> toGeoJson()
    is ElementPolylinesGeometry -> toGeoJson()
    is ElementPolygonsGeometry -> toGeoJson()
}

fun ElementPointGeometry.toGeoJson(): Point =
    Point(center.toPosition())

fun ElementPolylinesGeometry.toGeoJson(): Geometry =
    if (polylines.size == 1) {
        LineString(polylines.single().map { it.toPosition() })
    } else {
        MultiLineString(polylines.map { polyline -> polyline.map { it.toPosition() } })
    }

fun ElementPolygonsGeometry.toGeoJson(): Geometry {
    val outerRings = mutableListOf<List<LatLon>>()
    val innerRings = mutableListOf<List<LatLon>>()
    if (polygons.size == 1) {
        outerRings.add(polygons.first())
    } else {
        polygons.forEach {
            if (it.isRingDefinedClockwise()) innerRings.add(it) else outerRings.add(it)
        }
    }

    if (outerRings.size == 1) {
        return Polygon(
            (outerRings + innerRings).map { ring -> ring.map { it.toPosition() } }
        )
    }

    // outerRings must be sorted size ascending to correctly handle outer rings within holes
    // of larger polygons.
    outerRings.sortBy { it.measuredArea() }

    // we need to allocate the holes to the different outer polygons
    val groupedRings = outerRings.map { outerRing ->
        val rings = mutableListOf<List<Position>>()
        rings.add(outerRing.map { it.toPosition() })
        for (innerRing in innerRings.toList()) {
            if (innerRing[0].isInPolygon(outerRing)) {
                innerRings.remove(innerRing)
                rings.add(innerRing.map { it.toPosition() })
            }
        }
        rings
    }
    return MultiPolygon(groupedRings)
}

fun LatLon.toPosition(): Position =
    Position(longitude = longitude, latitude = latitude)

fun Position.toLatLon(): LatLon =
    LatLon(latitude = latitude, longitude = longitude)

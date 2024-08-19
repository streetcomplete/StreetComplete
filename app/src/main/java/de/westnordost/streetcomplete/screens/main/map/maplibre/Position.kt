package de.westnordost.streetcomplete.screens.main.map.maplibre

import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPointGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolygonsGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.util.math.isInPolygon
import de.westnordost.streetcomplete.util.math.isRingDefinedClockwise
import de.westnordost.streetcomplete.util.math.measuredArea
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.Geometry
import org.maplibre.geojson.LineString
import org.maplibre.geojson.MultiLineString
import org.maplibre.geojson.MultiPolygon
import org.maplibre.geojson.Point
import org.maplibre.geojson.Polygon

fun MapLibreMap.getMetersPerPixel(): Double? =
    cameraPosition.target?.latitude?.let { projection.getMetersPerPixelAtLatitude(it) }

fun MapLibreMap.screenAreaToBoundingBox(): BoundingBox =
    projection.getVisibleRegion(true).latLngBounds.toBoundingBox()

fun LatLngBounds.toBoundingBox() =
    BoundingBox(latitudeSouth, longitudeWest, latitudeNorth, longitudeEast)

fun BoundingBox.toLatLngBounds() =
    LatLngBounds.from(max.latitude, max.longitude, min.latitude, min.longitude)

fun LatLng.toLatLon() = LatLon(latitude, longitude)
fun LatLon.toLatLng() = LatLng(latitude, longitude)

private fun ElementGeometry.toType(): String = when (this) {
    is ElementPointGeometry -> "point"
    is ElementPolygonsGeometry -> "polygon"
    is ElementPolylinesGeometry -> "line"
}

fun ElementGeometry.toMapLibreGeometry(): Geometry = when (this) {
    is ElementPointGeometry -> toMapLibreGeometry()
    is ElementPolygonsGeometry -> toMapLibreGeometry()
    is ElementPolylinesGeometry -> toMapLibreGeometry()
}

fun ElementPointGeometry.toMapLibreGeometry(): Point =
    Point.fromLngLat(center.longitude, center.latitude)

fun ElementPolylinesGeometry.toMapLibreGeometry(): Geometry =
    if (polylines.size == 1) {
        LineString.fromLngLats(polylines.single().map { it.toPoint() })
    } else {
        MultiLineString.fromLngLats(polylines.map { polyline -> polyline.map { it.toPoint() } })
    }

fun ElementPolygonsGeometry.toMapLibreGeometry(): Geometry {
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
        return Polygon.fromLngLats(
            (outerRings + innerRings).map { ring -> ring.map { it.toPoint() } }
        )
    }

    // outerRings must be sorted size ascending to correctly handle outer rings within holes
    // of larger polygons.
    outerRings.sortBy { it.measuredArea() }

    // we need to allocate the holes to the different outer polygons
    val groupedRings = outerRings.map { outerRing ->
        val rings = mutableListOf<List<Point>>()
        rings.add(outerRing.map { it.toPoint() })
        for (innerRing in innerRings.toList()) {
            if (innerRing[0].isInPolygon(outerRing)) {
                innerRings.remove(innerRing)
                rings.add(innerRing.map { it.toPoint() })
            }
        }
        rings
    }
    return MultiPolygon.fromLngLats(groupedRings)
}

fun LatLon.toPoint(): Point = Point.fromLngLat(longitude, latitude)
fun Point.toLatLon(): LatLon = LatLon(latitude(), longitude())

fun GeoJsonSource.clear() = setGeoJson(null as FeatureCollection?)

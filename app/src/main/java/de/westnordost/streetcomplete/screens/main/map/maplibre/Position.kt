package de.westnordost.streetcomplete.screens.main.map.maplibre

import android.graphics.PointF
import android.graphics.RectF
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Geometry
import com.mapbox.geojson.LineString
import com.mapbox.geojson.MultiLineString
import com.mapbox.geojson.MultiPolygon
import com.mapbox.geojson.Point
import com.mapbox.geojson.Polygon
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import de.westnordost.streetcomplete.data.maptiles.toLatLng
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPointGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolygonsGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.util.math.distanceTo
import de.westnordost.streetcomplete.util.math.enclosingBoundingBox
import de.westnordost.streetcomplete.util.math.initialBearingTo
import de.westnordost.streetcomplete.util.math.isInPolygon
import de.westnordost.streetcomplete.util.math.isRingDefinedClockwise
import de.westnordost.streetcomplete.util.math.measuredArea
import de.westnordost.streetcomplete.util.math.translate
import kotlin.math.pow

fun MapboxMap.screenPositionToLatLon(screenPosition: PointF): LatLon =
    projection.fromScreenLocation(screenPosition).toLatLon()

fun MapboxMap.latLonToScreenPosition(latLon: LatLon): PointF =
    projection.toScreenLocation(latLon.toLatLng())

fun MapboxMap.screenCenterToLatLon(padding: RectF): LatLon? {
    if (width == 0f || height == 0f) return null

    return screenPositionToLatLon(
        PointF(
            padding.left + (width - padding.left - padding.right) / 2f,
            padding.top + (height - padding.top - padding.bottom) / 2f
        )
    )
}

// todo: use mapboxMap.projection.getVisibleRegion(ignorePadding)?
//  just need to convert to bounding box
//  but if we want padding, we need to set it first, and unset it later
fun MapboxMap.screenAreaToBoundingBox(padding: RectF): BoundingBox? {
    if (width == 0f || height == 0f) return null

    val size = PointF(width - padding.left - padding.right, height - padding.top - padding.bottom)

    // the special cases here are: map tilt and map rotation:
    // * map tilt makes the screen area -> world map area into a trapezoid
    // * map rotation makes the screen area -> world map area into a rotated rectangle
    // dealing with tilt: this method is just not defined if the tilt is above a certain limit
    if (cameraPosition.tilt > Math.PI / 4f) return null // 45°

    val positions = listOf(
        screenPositionToLatLon(PointF(padding.left, padding.top)),
        screenPositionToLatLon(PointF(padding.left + size.x, padding.top)),
        screenPositionToLatLon(PointF(padding.left, padding.top + size.y)),
        screenPositionToLatLon(PointF(padding.left + size.x, padding.top + size.y))
    )

    return positions.enclosingBoundingBox()
}

fun MapboxMap.getLatLonThatCentersLatLon(position: LatLon, padding: RectF, zoom: Float = cameraPosition.zoom.toFloat()): LatLon? {
    if (width == 0f || height == 0f) return null

    val screenCenter = screenPositionToLatLon(PointF(width / 2f, height / 2f))
    val offsetScreenCenter = screenPositionToLatLon(
        PointF(
            padding.left + (width - padding.left - padding.right) / 2,
            padding.top + (height - padding.top - padding.bottom) / 2
        )
    )

    val zoomDelta = zoom.toDouble() - cameraPosition.zoom
    val distance = offsetScreenCenter.distanceTo(screenCenter)
    val angle = offsetScreenCenter.initialBearingTo(screenCenter)
    val distanceAfterZoom = distance * (2.0).pow(-zoomDelta)
    return position.translate(distanceAfterZoom, angle)
}

fun MapboxMap.screenBottomToCenterDistance(): Double? {
    if (width == 0f || height == 0f) return null

    val center = screenPositionToLatLon(PointF(width / 2f, height / 2f))
    val bottom = screenPositionToLatLon(PointF(width / 2f, height * 1f))
    return center.distanceTo(bottom)
}


fun LatLng.toLatLon() = LatLon(latitude, longitude)
fun LatLon.toLatLng() = LatLng(latitude, longitude)

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

fun GeoJsonSource.clear() = setGeoJson(FeatureCollection.fromFeatures(emptyList()))

package de.westnordost.streetcomplete.util

import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.LatLon
import de.westnordost.osmapi.map.data.OsmLatLon
import de.westnordost.streetcomplete.ktx.forEachPair
import kotlin.math.*

/** Calculate stuff assuming a spherical Earth. The Earth is not spherical, but it is a good
 * approximation and totally sufficient for our use here.  */

/** In meters. See https://en.wikipedia.org/wiki/Earth_radius#Mean_radius */
const val EARTH_RADIUS = 6371000.0
/** In meters. See https://en.wikipedia.org/wiki/Earth%27s_circumference */
const val EARTH_CIRCUMFERENCE = 40000000.0

/* --------------------------------- LatLon extension functions --------------------------------- */

/**
 * Return a bounding box that contains a circle with the given radius around this point. In
 * other words, it is a square centered at the given position and with a side length of radius*2.
 */
fun LatLon.enclosingBoundingBox(radius: Double, globeRadius: Double = EARTH_RADIUS): BoundingBox {
	val distance = sqrt(2.0) * radius
	val min = translate(distance, 225.0, globeRadius)
	val max = translate(distance, 45.0, globeRadius)
	return BoundingBox(min, max)
}

/**
 *  Returns the initial bearing from this point another.
 *
 *  If you take a globe and draw a line straight up to the north pole from this point and a second
 *  line that connects this point and the given one, this is the angle between those two lines
 */
fun LatLon.initialBearingTo(pos: LatLon): Double {
	var bearing = initialBearing(
		latitude.toRadians(),
		longitude.toRadians(),
		pos.latitude.toRadians(),
		pos.longitude.toRadians()
	).toDegrees()

	if (bearing < 0) bearing += 360.0
	if (bearing >= 360) bearing -= 360.0
	return bearing
}

/**
 * Returns the final bearing from one point to the other.
 *
 * If you take a globe and draw a line straight up to the north pole from the given point and a
 * second one that connects this point and the given point (and goes on straight after this), this
 * is the angle between those two lines
 */
fun LatLon.finalBearingTo(pos: LatLon): Double {
	var bearing = finalBearing(
		latitude.toRadians(),
		longitude.toRadians(),
		pos.latitude.toRadians(),
		pos.longitude.toRadians()
	).toDegrees()

	if (bearing < 0) bearing += 360.0
	if (bearing >= 360) bearing -= 360.0
	return bearing
}

/** Returns the distance from this point to the other point */
fun LatLon.distanceTo(pos: LatLon, globeRadius: Double = EARTH_RADIUS): Double =
	measuredLength(
		latitude.toRadians(),
		longitude.toRadians(),
		pos.latitude.toRadians(),
		pos.longitude.toRadians(),
        globeRadius
	)

/** Returns a new point in the given distance and angle from the this point */
fun LatLon.translate(distance: Double, angle: Double, globeRadius: Double = EARTH_RADIUS): LatLon {
	val pair = translate(
		latitude.toRadians(),
		longitude.toRadians(),
		angle.toRadians(),
		distance,
        globeRadius
	)
	return createTranslated(pair.first.toDegrees(), pair.second.toDegrees())
}

/** Returns the shortest distance between this point and the arc between the given points */
fun LatLon.crossTrackDistanceTo(start: LatLon, end: LatLon, globeRadius: Double = EARTH_RADIUS): Double =
	crossTrackDistance(
		start.latitude.toRadians(),
		start.longitude.toRadians(),
		end.latitude.toRadians(),
		end.longitude.toRadians(),
		latitude.toRadians(),
		longitude.toRadians(),
        globeRadius
	)

/** Returns the shortest distance between this point and the arcs between the given points */
fun LatLon.crossTrackDistanceTo(polyLine: List<LatLon>, globeRadius: Double = EARTH_RADIUS): Double {
	require(polyLine.isNotEmpty()) { "Polyline must not be empty" }
	if (polyLine.size == 1) return distanceTo(polyLine[0])

	var shortestDistance = Double.MAX_VALUE
	polyLine.forEachPair { first, second ->
		val distance = crossTrackDistanceTo(first, second, globeRadius)
		if (distance < shortestDistance) shortestDistance = distance
	}
	return shortestDistance
}

/**
 * Given an arc between the two given points, returns the distance of the start point to the point
 * on the arc that is closest to this point
 */
fun LatLon.alongTrackDistanceTo(start: LatLon, end: LatLon, globeRadius: Double = EARTH_RADIUS): Double =
	alongTrackDistance(
		start.latitude.toRadians(),
		start.longitude.toRadians(),
		end.latitude.toRadians(),
		end.longitude.toRadians(),
		latitude.toRadians(),
		longitude.toRadians(),
        globeRadius
	)


/* -------------------------------- Polyline extension functions -------------------------------- */

/** Returns a bounding box that contains all points */
fun Iterable<LatLon>.enclosingBoundingBox(): BoundingBox {
	val it = iterator()
	require(it.hasNext()) { "positions is empty" }
	val origin = it.next()
	var minLatOffset = 0.0
	var minLonOffset = 0.0
	var maxLatOffset = 0.0
	var maxLonOffset = 0.0
	while (it.hasNext()) {
		val pos = it.next()
		// calculate with offsets here to properly handle 180th meridian
		val lat = pos.latitude - origin.latitude
		val lon = normalizeLongitude(pos.longitude - origin.longitude)
		if (lat < minLatOffset) minLatOffset = lat
		if (lon < minLonOffset) minLonOffset = lon
		if (lat > maxLatOffset) maxLatOffset = lat
		if (lon > maxLonOffset) maxLonOffset = lon
	}
	return BoundingBox(
		origin.latitude + minLatOffset,
		normalizeLongitude(origin.longitude + minLonOffset),
		origin.latitude + maxLatOffset,
		normalizeLongitude(origin.longitude + maxLonOffset)
	)
}

/** Returns the distance covered by this polyline */
fun List<LatLon>.measuredLength(globeRadius: Double = EARTH_RADIUS): Double {
    if (isEmpty()) return 0.0
    var length = 0.0
    forEachPair { first, second ->
        length += first.distanceTo(second, globeRadius)
    }
    return length
}

/**
 * Returns whether any point on this polyline is within the given distance of a point on the other
 * line
 */
fun List<LatLon>.isWithinDistanceOf(distance: Double, line: List<LatLon>, globeRadius: Double = EARTH_RADIUS): Boolean {
    for (linePoint1 in this) {
        for (linePoint2 in line) {
            if (linePoint1.distanceTo(linePoint2, globeRadius) <= distance) {
                return true
            }
        }
    }
    return false
}

/** Returns the line around the center point of this polyline
 *  @throws IllegalArgumentException if list is empty  */
fun List<LatLon>.centerLineOfPolyline(globeRadius: Double = EARTH_RADIUS): Pair<LatLon, LatLon> {
    require(size >= 2) { "positions list must contain at least 2 elements" }
    var halfDistance = measuredLength() / 2

    forEachPair { first, second ->
        halfDistance -= first.distanceTo(second, globeRadius)
        if (halfDistance <= 0) {
            return Pair(first, second)
        }
    }
    throw RuntimeException()
}


/**
 * Returns the center point of this polyline
 */
fun List<LatLon>.centerPointOfPolyline(globeRadius: Double = EARTH_RADIUS): LatLon {
    require(isNotEmpty()) { "list is empty" }
    val halfDistance = measuredLength(globeRadius) / 2
    return pointOnPolylineFromStart(halfDistance) ?: first()
}

/**
 * Returns the point the distance into the polyline. Null if the polyline is not long enough.
 */
fun List<LatLon>.pointOnPolylineFromStart(distance: Double): LatLon? {
    return pointOnPolyline(distance, false)
}

/**
 * Returns the point the distance into the polyline, starting from the end. Null if the polyline is
 * not long enough.
 */
fun List<LatLon>.pointOnPolylineFromEnd(distance: Double): LatLon? {
    return pointOnPolyline(distance, true)
}

private fun List<LatLon>.pointOnPolyline(distance: Double, fromEnd: Boolean): LatLon? {
    val list = if (fromEnd) this.asReversed() else this
    var d = 0.0
    list.forEachPair { first, second ->
        val segmentDistance = first.distanceTo(second)
        if (segmentDistance > 0) {
            d += segmentDistance
            if (d >= distance) {
                val ratio = (d - distance) / segmentDistance
                val lat = second.latitude - ratio * (second.latitude - first.latitude)
                val lon = normalizeLongitude(second.longitude - ratio * normalizeLongitude(second.longitude - first.longitude))
                return OsmLatLon(lat, lon)
            }
        }
    }
    return null
}

/* --------------------------------- Polygon extension functions -------------------------------- */

/**
 * Returns the center point of the given polygon
 *
 * @throws IllegalArgumentException if positions list is empty
 */
fun List<LatLon>.centerPointOfPolygon(): LatLon {
    require(isNotEmpty()) { "positions list is empty" }

    var lon = 0.0
    var lat = 0.0
    var area = 0.0
    val origin = first()
    forEachPair { first, second ->
        // calculating with offsets to avoid rounding imprecision and 180th meridian problem
        val dx1 = normalizeLongitude(first.longitude - origin.longitude)
        val dy1 = first.latitude - origin.latitude
        val dx2 = normalizeLongitude(second.longitude - origin.longitude)
        val dy2 = second.latitude - origin.latitude
        val f = dx1 * dy2 - dx2 * dy1
        lon += (dx1 + dx2) * f
        lat += (dy1 + dy2) * f
        area += f
    }
    area *= 3.0

    return if (area == 0.0) origin else OsmLatLon(
        lat / area + origin.latitude,
        normalizeLongitude(lon / area + origin.longitude)
    )
}

/**
 * Returns whether the given position is within the given polygon. Whether the polygon is defined
 * clockwise or counterclockwise does not matter. The polygon boundary and its vertices are
 * considered inside the polygon
 */
fun LatLon.isInPolygon(polygon: List<LatLon>): Boolean {
    var oddNumberOfIntersections = false
    var lastWasIntersectionAtVertex = false
    val lon = longitude
    val lat = latitude
    polygon.forEachPair { first, second ->
        val lat0 = first.latitude
        val lat1 = second.latitude
        // scanline check, disregard line segments parallel to the cast ray
        if (lat0 != lat1 && inside(lat, lat0, lat1)) {
            val lon0 = first.longitude
            val lon1 = second.longitude
            val vt = (lat - lat1) / (lat0 - lat1)
            val intersectionLongitude = normalizeLongitude(lon1 + vt * normalizeLongitude(lon0 - lon1))
            val lonDiff = normalizeLongitude(intersectionLongitude - lon)
            // position is on polygon boundary
            if (lonDiff == 0.0) return true
            // ray crosses polygon boundary. ignore if this intersection was already counted
            // when looking at the last intersection
            if (lonDiff > 0 && !lastWasIntersectionAtVertex) {
                oddNumberOfIntersections = !oddNumberOfIntersections
                lastWasIntersectionAtVertex = intersectionLongitude == lon1
            } else {
                lastWasIntersectionAtVertex = false
            }
        }
    }
    return oddNumberOfIntersections
}

private fun inside(v: Double, bound0: Double, bound1: Double): Boolean =
    if (bound0 < bound1) v in bound0..bound1 else v in bound1..bound0

/**
 * Returns whether the given position is within the given multipolygon. Polygons defined
 * counterclockwise count as outer shells, polygons defined clockwise count as holes.
 *
 * It is assumed that shells do not overlap with other shells and holes do not overlap with other
 * holes. (Though, of course a shell can be within a hole within a shell, that's okay)
 */
fun LatLon.isInMultipolygon(multipolygon: List<List<LatLon>>): Boolean {
    var containment = 0
    for (polygon in multipolygon) {
        if (isInPolygon(polygon)) {
            if (polygon.isRingDefinedClockwise()) containment-- else containment++
        }
    }
    return containment > 0
}

/** Returns whether the given ring is defined clockwise
 *
 * @throws IllegalArgumentException if positions list is empty
 */
fun List<LatLon>.isRingDefinedClockwise(): Boolean {
    require(isNotEmpty()) { "positions list is empty" }

    var sum = 0.0
    val origin = first()
    forEachPair { first, second ->
        // calculating with offsets to handle 180th meridian
        val lon0 = normalizeLongitude(first.longitude - origin.longitude)
        val lat0 = first.latitude - origin.latitude
        val lon1 = normalizeLongitude(second.longitude - origin.longitude)
        val lat1 = second.latitude - origin.latitude
        sum += lon0 * lat1 - lon1 * lat0
    }
    return sum > 0
}


/* ------------------------------ Bounding Box extension functions  ----------------------------- */


/** Returns the area enclosed by this bbox */
fun BoundingBox.area(globeRadius: Double = EARTH_RADIUS): Double {
	val minLatMaxLon = OsmLatLon(min.latitude, max.longitude)
	val maxLatMinLon = OsmLatLon(max.latitude, min.longitude)
	return min.distanceTo(minLatMaxLon, globeRadius) * min.distanceTo(maxLatMinLon, globeRadius)
}



fun createTranslated(latitude: Double, longitude: Double): LatLon {
	var lat = latitude
	var lon = longitude
	lon = normalizeLongitude(lon)
	var crossedPole = false
	// north pole
	if (lat > 90) {
		lat = 180 - lat
		crossedPole = true
	} else if (lat < -90) {
		lat = -180 - lat
		crossedPole = true
	}
	if (crossedPole) {
		lon += 180.0
		if (lon > 180) lon -= 360.0
	}
	return OsmLatLon(lat, lon)
}

private fun Double.toRadians() = this / 180.0 * PI
private fun Double.toDegrees() = this / PI * 180.0

fun normalizeLongitude(lon: Double): Double {
	var lon = lon
	while (lon > 180) lon -= 360.0
	while (lon < -180) lon += 360.0
	return lon
}


/* The following formulas have been adapted from this excellent source:
   http://www.movable-type.co.uk/scripts/latlong.html
   Thanks to and (c) Chris Veness 2002-2019, MIT Licence

   All the calculations below are done with coordinates in radians.
*/

/** Return a new point translated in the given angle and distance on a sphere with the given radius */
private fun translate(φ1: Double, λ1: Double, α1: Double, distance: Double, radius: Double): Pair<Double, Double> {
	val σ12 = distance / radius
	val y = sin(φ1) * cos(σ12) + cos(φ1) * sin(σ12) * cos(α1)
	val a = cos(φ1) * cos(σ12) - sin(φ1) * sin(σ12) * cos(α1)
	val b = sin(σ12) * sin(α1)
	val x = sqrt(a.pow(2) + b.pow(2))
	val φ2 = atan2(y, x)
	val λ2 = λ1 + atan2(b, a)
	return Pair(φ2, λ2)
}

/** Returns the distance of two points on a sphere with the given radius */
private fun measuredLength(φ1: Double, λ1: Double, φ2: Double, λ2: Double, r: Double): Double {
	// see https://mathforum.org/library/drmath/view/51879.html for derivation
	val Δλ = λ2 - λ1
	val Δφ = φ2 - φ1
	val a = sin(Δφ / 2).pow(2) + cos(φ1) * cos(φ2) * sin(Δλ / 2).pow(2)
	val c = 2 * atan2(sqrt(a), sqrt(1 - a))
	return c * r
}

/** Returns the initial bearing from one point to another */
private fun initialBearing(φ1: Double, λ1: Double, φ2: Double, λ2: Double): Double {
	// see https://mathforum.org/library/drmath/view/55417.html for derivation
	val Δλ = λ2 - λ1
	return atan2(sin(Δλ) * cos(φ2), cos(φ1) * sin(φ2) - sin(φ1) * cos(φ2) * cos(Δλ))
}

/** Returns the final bearing from one point to another */
private fun finalBearing(φ1: Double, λ1: Double, φ2: Double, λ2: Double): Double {
	val Δλ = λ2 - λ1
	return atan2(sin(Δλ) * cos(φ1), -cos(φ2) * sin(φ1) + sin(φ2) * cos(φ1) * cos(Δλ))
}

/** Returns the shortest distance between point three and the arc between point one and two */
private fun crossTrackDistance(φ1: Double, λ1: Double, φ2: Double, λ2: Double, φ3: Double, λ3: Double, r: Double): Double {
	val θ12 = initialBearing(φ1, λ1, φ2, λ2)
	val θ13 = initialBearing(φ1, λ1, φ3, λ3)
	val δ13 = measuredLength(φ1, λ1, φ3, λ3, r) / r
	val δxt = asin(sin(δ13) * sin(θ13 - θ12))
	return abs(δxt * r)
}

/**
 * Given an arc between point one and two, returns the distance of point one from the point on the
 * arc that is closest to point three.
 */
private fun alongTrackDistance(φ1: Double, λ1: Double, φ2: Double, λ2: Double, φ3: Double, λ3: Double, r: Double): Double {
	val θ12 = initialBearing(φ1, λ1, φ2, λ2)
	val θ13 = initialBearing(φ1, λ1, φ3, λ3)
	val δ13 = measuredLength(φ1, λ1, φ3, λ3, r) / r
	val δxt = asin(sin(δ13) * sin(θ13 - θ12))
	val δat = acos(cos(δ13) / abs(cos(δxt)))
	return δat * sign(cos(θ12 - θ13)) * r
}


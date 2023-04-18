// we want to use the greek letters to stay close to the mathematical examples linked
@file:Suppress("NonAsciiCharacters")

package de.westnordost.streetcomplete.util.math

import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.splitAt180thMeridian
import de.westnordost.streetcomplete.util.ktx.asSequenceOfPairs
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sign
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

/** Calculate stuff assuming a spherical Earth. The Earth is not spherical, but it is a good
 * approximation and totally sufficient for our use here.  */

/** In meters. See https://en.wikipedia.org/wiki/Earth_radius#Mean_radius */
const val EARTH_RADIUS = 6371000.0
/** In meters. See https://en.wikipedia.org/wiki/Earth%27s_circumference
 *  Mean between polar and equator circumference */
const val EARTH_CIRCUMFERENCE = (40007863.0 + 40075017.0) / 2.0

//region LatLon extension functions

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

/** Returns whether this point is right of the line spanned between start and the given bearing. */
fun LatLon.isRightOf(lineStart: LatLon, bearing: Double): Boolean =
    normalizeDegrees(lineStart.initialBearingTo(this) - bearing, -180.0) > 0

/** Returns whether this point is right of both the line spanned by p0 and p1 and the line
 *  spanned by p1 and p2 */
fun LatLon.isRightOf(p0: LatLon, p1: LatLon, p2: LatLon): Boolean {
    val angle01 = p0.initialBearingTo(p1)
    val angle12 = p1.initialBearingTo(p2)
    val turnsRight = normalizeDegrees(angle12 - angle01, -180.0) > 0
    return if (turnsRight) {
        isRightOf(p0, angle01) && isRightOf(p1, angle12)
    } else {
        isRightOf(p0, angle01) || isRightOf(p1, angle12)
    }
}

/** Returns the distance from this point to the other point */
fun LatLon.distanceTo(pos: LatLon, globeRadius: Double = EARTH_RADIUS): Double =
    angularDistance(
        latitude.toRadians(),
        longitude.toRadians(),
        pos.latitude.toRadians(),
        pos.longitude.toRadians()
    ) * globeRadius

/** Returns a new point in the given distance and angle from the this point */
fun LatLon.translate(distance: Double, angle: Double, globeRadius: Double = EARTH_RADIUS): LatLon {
    val pair = translate(
        latitude.toRadians(),
        longitude.toRadians(),
        angle.toRadians(),
        distance / globeRadius
    )
    return createTranslated(pair.first.toDegrees(), pair.second.toDegrees())
}

/** Returns the shortest distance between this point and the great arc spanned by the two points.
 *  The sign tells on which side of the great arc this point is */
fun LatLon.crossTrackDistanceTo(start: LatLon, end: LatLon, globeRadius: Double = EARTH_RADIUS): Double =
    crossTrackAngularDistance(
        start.latitude.toRadians(),
        start.longitude.toRadians(),
        end.latitude.toRadians(),
        end.longitude.toRadians(),
        latitude.toRadians(),
        longitude.toRadians()
    ) * globeRadius

/**
 * Given the great arc spanned by the two given points, returns the distance of the start point to
 * the point on the great arc that is closest to this point.
 * The sign tells the direction of that point on the great arc seen from the start point.
 */
fun LatLon.alongTrackDistanceTo(start: LatLon, end: LatLon, globeRadius: Double = EARTH_RADIUS): Double =
    alongTrackAngularDistance(
        start.latitude.toRadians(),
        start.longitude.toRadians(),
        end.latitude.toRadians(),
        end.longitude.toRadians(),
        latitude.toRadians(),
        longitude.toRadians()
    ) * globeRadius

/** Returns the shortest distance between this point and the arc between the given points */
fun LatLon.distanceToArc(start: LatLon, end: LatLon, globeRadius: Double = EARTH_RADIUS): Double =
    abs(
        angularDistanceToArc(
            start.latitude.toRadians(),
            start.longitude.toRadians(),
            end.latitude.toRadians(),
            end.longitude.toRadians(),
            latitude.toRadians(),
            longitude.toRadians()
        )
    ) * globeRadius

/** Returns the shortest distance between this point and the arcs between the given points */
fun LatLon.distanceToArcs(polyLine: List<LatLon>, globeRadius: Double = EARTH_RADIUS): Double {
    require(polyLine.isNotEmpty()) { "Polyline must not be empty" }
    if (polyLine.size == 1) return distanceTo(polyLine[0])

    return polyLine
        .asSequenceOfPairs()
        .minOf { distanceToArc(it.first, it.second, globeRadius) }
}

/** Returns the point on the arc spanned between the given points that is closest to this
 *  point */
fun LatLon.nearestPointOnArc(start: LatLon, end: LatLon): LatLon {
    val alongTrackDistance = alongTrackDistanceTo(start, end)
    if (alongTrackDistance <= 0) return start
    val arc = listOf(start, end)
    return arc.pointOnPolylineFromStart(alongTrackDistance) ?: end
}

/** Returns the point on the given polyline that is closest to this point */
fun LatLon.nearestPointOnArcs(polyline: List<LatLon>): LatLon {
    val arc = polyline.asSequenceOfPairs().minBy { distanceToArc(it.first, it.second) }
    return nearestPointOnArc(arc.first, arc.second)
}

//endregion

//region Polyline extension functions

/** Returns the shortest distance between this polyline and given polyline */
fun List<LatLon>.distanceTo(polyline: List<LatLon>, globeRadius: Double = EARTH_RADIUS): Double {
    require(isNotEmpty()) { "Polyline must not be empty" }
    return minOf { it.distanceToArcs(polyline, globeRadius) }
}

/** Returns whether this polyline intersects with the given polyline. If a polyline touches the
 *  other at an endpoint (e.g. two consecutive polylines that share one endpoint), this doesn't
 *  count. */
fun List<LatLon>.intersectsWith(polyline: List<LatLon>): Boolean {
    require(size > 1 && polyline.size > 1) { "Polylines must each contain at least two elements" }
    val ns = map { it.toNormalOnSphere() }
    val npolyline = polyline.map { it.toNormalOnSphere() }
    ns.asSequenceOfPairs().forEach { (first, second) ->
        npolyline.asSequenceOfPairs().forEach { (otherFirst, otherSecond) ->
            val intersection = arcIntersection(first, second, otherFirst, otherSecond)
            if (intersection != null) {
                // touching endpoints don't count
                if (
                    first != npolyline.first() && first != npolyline.last()
                    && second != npolyline.first() && second != npolyline.last()
                ) return true
            }
        }
    }
    return false
}

/** Returns whether the arc spanned between p1 and p2 intersects with the arc spanned by p2 and p4 */
fun intersectionOf(p1: LatLon, p2: LatLon, p3: LatLon, p4: LatLon): LatLon? {
    return arcIntersection(
        p1.toNormalOnSphere(),
        p2.toNormalOnSphere(),
        p3.toNormalOnSphere(),
        p4.toNormalOnSphere()
    )?.toLatLon()
}

/** Returns a bounding box that contains all points */
fun Iterable<LatLon>.enclosingBoundingBox(): BoundingBox {
    val it = iterator()
    require(it.hasNext()) { "positions is empty" }
    val origin = it.next()
    var minLatOffset = 0.0
    var minLonOffset = 0.0
    var maxLatOffset = 0.0
    var maxLonOffset = 0.0
    var minLat = origin.latitude
    var minLon = origin.longitude
    var maxLat = origin.latitude
    var maxLon = origin.longitude
    while (it.hasNext()) {
        val pos = it.next()
        // calculate with offsets here to properly handle 180th meridian
        val latOffset = pos.latitude - origin.latitude
        val lonOffset = normalizeLongitude(pos.longitude - origin.longitude)
        if (latOffset < minLatOffset) {
            minLatOffset = latOffset
            minLat = pos.latitude
        }
        if (lonOffset < minLonOffset) {
            minLonOffset = lonOffset
            minLon = pos.longitude
        }
        if (latOffset > maxLatOffset) {
            maxLatOffset = latOffset
            maxLat = pos.latitude
        }
        if (lonOffset > maxLonOffset) {
            maxLonOffset = lonOffset
            maxLon = pos.longitude
        }
    }
    return BoundingBox(minLat, minLon, maxLat, maxLon)
}

/** Returns the distance covered by this polyline */
fun List<LatLon>.measuredLength(globeRadius: Double = EARTH_RADIUS): Double {
    return asSequenceOfPairs().sumOf { (first, second) ->
        first.distanceTo(second, globeRadius)
    }
}

/** Returns the line around the center point of this polyline
 *  @throws IllegalArgumentException if list is empty
 */
fun List<LatLon>.centerLineOfPolyline(globeRadius: Double = EARTH_RADIUS): Pair<LatLon, LatLon> {
    require(size >= 2) { "positions list must contain at least 2 elements" }
    var halfDistance = measuredLength() / 2

    asSequenceOfPairs().forEach { (first, second) ->
        halfDistance -= first.distanceTo(second, globeRadius)
        if (halfDistance <= 0) {
            return Pair(first, second)
        }
    }
    throw RuntimeException()
}

/**
 * Returns the center point of this polyline
 * @throws IllegalArgumentException if list is empty
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
    return pointsOnPolyline(false, distance).firstOrNull()
}

/**
 * Returns the points the distances into the polyline. Returns less points if the polyline is not
 * long enough.
 */
fun List<LatLon>.pointsOnPolylineFromStart(distances: List<Double>): List<LatLon> {
    return pointsOnPolyline(false, *distances.toDoubleArray())
}

/**
 * Returns the point the distance into the polyline, starting from the end. Null if the polyline is
 * not long enough.
 */
fun List<LatLon>.pointOnPolylineFromEnd(distance: Double): LatLon? {
    return pointsOnPolyline(true, distance).firstOrNull()
}

/**
 * Returns the points the distances into the polyline, starting from the end. Returns less points if
 * the polyline is not long enough.
 */
fun List<LatLon>.pointsOnPolylineFromEnd(distances: List<Double>): List<LatLon> {
    return pointsOnPolyline(true, *distances.toDoubleArray())
}

private fun List<LatLon>.pointsOnPolyline(fromEnd: Boolean, vararg distances: Double): List<LatLon> {
    if (distances.isEmpty()) return emptyList()
    val list = if (fromEnd) this.asReversed() else this
    distances.sort()
    var i = 0
    var d = 0.0
    val result = ArrayList<LatLon>(distances.size)
    list.asSequenceOfPairs().forEach { (first, second) ->
        val segmentDistance = first.distanceTo(second)
        if (segmentDistance > 0) {
            d += segmentDistance
            while (d >= distances[i]) {
                val ratio = (d - distances[i]) / segmentDistance
                val lat = second.latitude - ratio * (second.latitude - first.latitude)
                val lon = normalizeLongitude(second.longitude - ratio * normalizeLongitude(second.longitude - first.longitude))
                result.add(LatLon(lat, lon))
                ++i
                if (i == distances.size) return result
            }
        }
    }
    return result
}

//endregion

//region Polygon extension functions

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
    asSequenceOfPairs().forEach { (first, second) ->
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

    return if (area == 0.0) origin else LatLon(
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
    polygon.asSequenceOfPairs().forEach { (first, second) ->
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
            val isIntersectionAtVertex = lat == lat1
            if (lonDiff > 0 && !lastWasIntersectionAtVertex) {
                oddNumberOfIntersections = !oddNumberOfIntersections
            }
            lastWasIntersectionAtVertex = isIntersectionAtVertex
        }
    }
    return oddNumberOfIntersections
}

private fun inside(v: Double, bound0: Double, bound1: Double): Boolean =
    if (bound0 < bound1) v in bound0..bound1 else v in bound1..bound0

/**
 * Returns the area of a this multipolygon, assuming the outer shell is defined counterclockwise and
 * any holes are defined clockwise
 */
fun List<List<LatLon>>.measuredMultiPolygonArea(globeRadius: Double = EARTH_RADIUS): Double {
    return sumOf { it.measuredAreaSigned(globeRadius) }
}

/**
 * Returns the area of a this polygon
 */
fun List<LatLon>.measuredArea(globeRadius: Double = EARTH_RADIUS): Double {
    return abs(measuredAreaSigned(globeRadius))
}

/**
 * Returns the signed area of a this polygon. If it is defined counterclockwise, it'll return
 * something positive, clockwise something negative
 */
fun List<LatLon>.measuredAreaSigned(globeRadius: Double = EARTH_RADIUS): Double {
    // not closed: area 0
    if (size < 4) return 0.0
    if (first().latitude != last().latitude || first().longitude != last().longitude) return 0.0
    var area = 0.0
    /* The algorithm is basically the same as for the planar case, only the calculation of the area
     * for each polygon edge is the polar triangle area */
    asSequenceOfPairs().forEach { (first, second) ->
        area += polarTriangleArea(
            first.latitude.toRadians(),
            first.longitude.toRadians(),
            second.latitude.toRadians(),
            second.longitude.toRadians(),
        )
    }
    return area * (globeRadius * globeRadius)
}

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
    asSequenceOfPairs().forEach { (first, second) ->
        // calculating with offsets to handle 180th meridian
        val lon0 = normalizeLongitude(first.longitude - origin.longitude)
        val lat0 = first.latitude - origin.latitude
        val lon1 = normalizeLongitude(second.longitude - origin.longitude)
        val lat1 = second.latitude - origin.latitude
        sum += lon0 * lat1 - lon1 * lat0
    }
    return sum > 0
}

//endregion

//region Bounding Box extension functions

/** Returns the area enclosed by this bbox */
fun BoundingBox.area(globeRadius: Double = EARTH_RADIUS): Double {
    val minLatMaxLon = LatLon(min.latitude, max.longitude)
    val maxLatMinLon = LatLon(max.latitude, min.longitude)
    return min.distanceTo(minLatMaxLon, globeRadius) * min.distanceTo(maxLatMinLon, globeRadius)
}

/** Returns a new bounding box that is [radius] larger than this bounding box */
fun BoundingBox.enlargedBy(radius: Double, globeRadius: Double = EARTH_RADIUS): BoundingBox {
    return BoundingBox(
        min.translate(radius, 225.0, globeRadius),
        max.translate(radius, 45.0, globeRadius)
    )
}

/** returns whether this bounding box contains the given position */
operator fun BoundingBox.contains(pos: LatLon): Boolean {
    return if (crosses180thMeridian) {
        splitAt180thMeridian().any { it.containsCanonical(pos) }
    } else {
        containsCanonical(pos)
    }
}

/** returns whether this bounding box contains the given position, assuming the bounding box does
 *  not cross the 180th meridian */
private fun BoundingBox.containsCanonical(pos: LatLon): Boolean =
    pos.longitude in min.longitude..max.longitude &&
    pos.latitude in min.latitude..max.latitude

/** returns whether this bounding box intersects with the other. Works if any of the bounding boxes
 *  cross the 180th meridian */
fun BoundingBox.intersect(other: BoundingBox): Boolean =
    checkAlignment(other) { bbox1, bbox2 -> bbox1.intersectCanonical(bbox2) }

/** returns whether this bounding box is completely inside the other, assuming both bounding boxes
 *  do not cross the 180th meridian */
fun BoundingBox.isCompletelyInside(other: BoundingBox): Boolean =
    checkAlignment(other) { bbox1, bbox2 -> bbox1.isCompletelyInsideCanonical(bbox2) }

/** returns whether this bounding box intersects with the other, assuming both bounding boxes do
 *  not cross the 180th meridian */
private fun BoundingBox.intersectCanonical(other: BoundingBox): Boolean =
    max.longitude >= other.min.longitude &&
    min.longitude <= other.max.longitude &&
    max.latitude >= other.min.latitude &&
    min.latitude <= other.max.latitude

/** returns whether this bounding box is completely inside the other, assuming both bounding boxes
 *  do not cross the 180th meridian */
private fun BoundingBox.isCompletelyInsideCanonical(other: BoundingBox): Boolean =
    min.longitude >= other.min.longitude &&
    min.latitude >= other.min.latitude &&
    max.longitude <= other.max.longitude &&
    max.latitude <= other.max.latitude

private inline fun BoundingBox.checkAlignment(
    other: BoundingBox,
    canonicalCheck: (bbox1: BoundingBox, bbox2: BoundingBox) -> Boolean
): Boolean {
    return if (crosses180thMeridian) {
        val these = splitAt180thMeridian()
        if (other.crosses180thMeridian) {
            val others = other.splitAt180thMeridian()
            these.any { a -> others.any { b -> canonicalCheck(a, b) } }
        } else {
            these.any { canonicalCheck(it, other) }
        }
    } else {
        if (other.crosses180thMeridian) {
            val others = other.splitAt180thMeridian()
            others.any { canonicalCheck(this, it) }
        } else {
            canonicalCheck(this, other)
        }
    }
}

//endregion

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
    return LatLon(lat, lon)
}

private fun Double.toRadians() = this / 180.0 * PI
private fun Double.toDegrees() = this / PI * 180.0

fun normalizeLongitude(lon: Double): Double {
    var normalizedLon = lon % 360 // normalizedLon is -360..360
    if (normalizedLon < -180) normalizedLon += 360
    else if (normalizedLon >= 180) normalizedLon -= 360
    return normalizedLon
}

/* The following formulas have been adapted from this excellent source:
   http://www.movable-type.co.uk/scripts/latlong.html
   Thanks to and (c) Chris Veness 2002-2019, MIT Licence

   All the calculations below are done with coordinates in radians.
*/

/** Return a new point translated from the point [φ1], [λ1] in the initial bearing [α1] and angular distance [σ12] */
private fun translate(φ1: Double, λ1: Double, α1: Double, σ12: Double): Pair<Double, Double> {
    val y = sin(φ1) * cos(σ12) + cos(φ1) * sin(σ12) * cos(α1)
    val a = cos(φ1) * cos(σ12) - sin(φ1) * sin(σ12) * cos(α1)
    val b = sin(σ12) * sin(α1)
    val x = sqrt(a.pow(2) + b.pow(2))
    val φ2 = atan2(y, x)
    val λ2 = λ1 + atan2(b, a)
    return Pair(φ2, λ2)
}

/** Returns the distance of two points on a sphere */
private fun angularDistance(φ1: Double, λ1: Double, φ2: Double, λ2: Double): Double {
    // see https://mathforum.org/library/drmath/view/51879.html for derivation
    val Δλ = λ2 - λ1
    val Δφ = φ2 - φ1
    val a = sin(Δφ / 2).pow(2) + cos(φ1) * cos(φ2) * sin(Δλ / 2).pow(2)
    return 2 * asin(sqrt(a))
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

/** Returns the shortest distance between point three and the great arc spanned by one and two.
 *  The sign tells on which side point three is on */
private fun crossTrackAngularDistance(φ1: Double, λ1: Double, φ2: Double, λ2: Double, φ3: Double, λ3: Double): Double {
    val θ12 = initialBearing(φ1, λ1, φ2, λ2)
    val θ13 = initialBearing(φ1, λ1, φ3, λ3)
    val δ13 = angularDistance(φ1, λ1, φ3, λ3)
    return asin(sin(δ13) * sin(θ13 - θ12))
}

/**
 * Given the great arc spanned by point one and two, returns the distance of point one from the point on the
 * arc that is closest to point three.
 */
private fun alongTrackAngularDistance(φ1: Double, λ1: Double, φ2: Double, λ2: Double, φ3: Double, λ3: Double): Double {
    val θ12 = initialBearing(φ1, λ1, φ2, λ2)
    val θ13 = initialBearing(φ1, λ1, φ3, λ3)
    val δ13 = angularDistance(φ1, λ1, φ3, λ3)
    val δxt = asin(sin(δ13) * sin(θ13 - θ12)) // <- crossTrackAngularDistance
    return acos(cos(δ13) / abs(cos(δxt))) * sign(cos(θ12 - θ13))
}

/** Returns the shortest distance between point three and the arc between point one and two.
 *  The sign tells on which side point three is on */
private fun angularDistanceToArc(φ1: Double, λ1: Double, φ2: Double, λ2: Double, φ3: Double, λ3: Double): Double {
    val θ12 = initialBearing(φ1, λ1, φ2, λ2)
    val θ13 = initialBearing(φ1, λ1, φ3, λ3)

    val δ13 = angularDistance(φ1, λ1, φ3, λ3)
    val δ12 = angularDistance(φ1, λ1, φ2, λ2)

    val δxt = asin(sin(δ13) * sin(θ13 - θ12)) // <- crossTrackAngularDistance
    val δat = acos(cos(δ13) / abs(cos(δxt))) * sign(cos(θ12 - θ13)) // <- alongTrackAngularDistance

    // shortest distance to great arc is before point one -> shortest distance is distance to point one
    if (δat < 0) return δ13
    // shortest distance to great arc is after point two -> shortest distance is distance to point two
    if (δat > δ12) return angularDistance(φ2, λ2, φ3, λ3)
    return δxt
}

/** Returns the signed area of a triangle spanning between the north pole and the two given points.
 * */
private fun polarTriangleArea(φ1: Double, λ1: Double, φ2: Double, λ2: Double): Double {
    val tanφ1 = tan((PI / 2 - φ1) / 2)
    val tanφ2 = tan((PI / 2 - φ2) / 2)
    val Δλ = λ1 - λ2
    val tan = tanφ1 * tanφ2
    return 2 * atan2(tan * sin(Δλ), 1 + tan * cos(Δλ))
}

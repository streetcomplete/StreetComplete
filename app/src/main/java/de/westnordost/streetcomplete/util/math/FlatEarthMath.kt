@file:Suppress("NonAsciiCharacters")

package de.westnordost.streetcomplete.util.math

import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.util.ktx.asSequenceOfPairs
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sqrt

/** Calculate stuff assuming an (almost) flat Earth. The main exception from assuming a completely
 *  flat earth is use of cos(lat) to take into account decreasing longitude distance at
 *  high latitudes.
 *  Optimized for performance with precision within 1 m of spherical functions for up to
 *  0.03° difference between points (several km at common latitudes). */

// ~5 times faster than spherical version
/** Returns the approximate distance from this point to the other point.
 *  Result is within 0.1 m of the spherical version for less than 0.3° difference and EARTH_RADIUS */
fun LatLon.flatDistanceTo(pos: LatLon, globeRadius: Double = EARTH_RADIUS): Double =
    flatAngularDistance(
        latitude.toRadians(),
        longitude.toRadians(),
        pos.latitude.toRadians(),
        pos.longitude.toRadians()
    ) * globeRadius

// ~10 times faster than spherical version
/** Returns the shortest distance between this point and the arc between the given points. */
fun LatLon.flatDistanceToArc(start: LatLon, end: LatLon, globeRadius: Double = EARTH_RADIUS): Double {
    val dLongitudeStart = normalizeLongitude(longitude - start.longitude)
    val dLongitudeEnd = normalizeLongitude(longitude - end.longitude)
    return abs(
        flatAngularDistanceToArc(
            start.latitude.toRadians(),
            dLongitudeStart.toRadians(),
            end.latitude.toRadians(),
            dLongitudeEnd.toRadians(),
            latitude.toRadians(),
            0.0
        )
    ) * globeRadius
}

/** Returns the shortest distance between this point and the arcs between the given points */
fun LatLon.flatDistanceToArcs(polyLine: List<LatLon>, globeRadius: Double = EARTH_RADIUS): Double {
    require(polyLine.isNotEmpty()) { "Polyline must not be empty" }
    if (polyLine.size == 1) return flatDistanceTo(polyLine[0])

    return polyLine
        .asSequenceOfPairs()
        .minOf { flatDistanceToArc(it.first, it.second, globeRadius) }
}

/** Returns the approximate distance of two points on a sphere.
 *  Takes into account wrapping at 180th longitude */
private fun flatAngularDistance(φ1: Double, λ1: Double, φ2: Double, λ2: Double): Double {
    // https://en.wikipedia.org/wiki/Geographical_distance#Spherical_Earth_projected_to_a_plane
    val δφ = φ1 - φ2
    var δλ = abs(λ1 - λ2)
    if (δλ > PI) {
        δλ = 2 * PI - δλ
    }
    val cosδλ = approximateCos((φ1 + φ2) / 2) * δλ
    return sqrt(δφ * δφ + cosδλ * cosδλ)
}

/** Returns the shortest distance between point three and the arc/line between point one and two.
 *  Does not take into account wrapping at 180th longitude (handled by flatDistanceToArc)
 *  Contrary to spherical version, the sign is always positive */
// from http://paulbourke.net/geometry/pointlineplane/ and the linked java implementation
private fun flatAngularDistanceToArc(φ1: Double, λ1: Double, φ2: Double, λ2: Double, φ3: Double, λ3: Double): Double {
    val δφ12 = φ2 - φ1
    val δλ12 = λ2 - λ1

    if (δφ12 == 0.0 && δλ12 == 0.0) {
        return flatAngularDistance(φ1, λ1, φ3, λ3)
    }

    val δλ13 = λ3 - λ1
    val c = approximateCos(φ3)
    // need the cosine as sort of "weight factor", because λ distances are much shorter at high φ
    val u = ((φ3 - φ1) * δφ12 + δλ13 * δλ12 * c * c) / (δφ12 * δφ12 + δλ12 * δλ12 * c * c)

    val (closestPointφ, closestPointλ) = when {
        u < 0 -> φ1 to λ1
        u > 1 -> φ2 to λ2
        else -> φ1 + u * δφ12 to λ1 + u * δλ12
    }

    return flatAngularDistance(closestPointφ, closestPointλ, φ3, λ3)
}

/** approximate cosine using Taylor expansion, ~10 times faster than Math.cos
 * in interval [-PI/2, PI/2] it's within 2.5E-5 of Math.cos */
private fun approximateCos(radians: Double): Double {
    // not using pow because it's really slow (integers are converted to double)
    val rSquared = radians * radians
    return 1.0 - rSquared / 2 + rSquared * rSquared / 24 - rSquared * rSquared * rSquared / 720 + rSquared * rSquared * rSquared * rSquared / 40320
}

private fun Double.toRadians() = this / 180.0 * PI

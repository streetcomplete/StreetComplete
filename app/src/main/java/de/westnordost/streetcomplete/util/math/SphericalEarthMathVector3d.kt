// we want to use the greek letters to stay close to the mathematical examples linked below
@file:Suppress("NonAsciiCharacters")

package de.westnordost.streetcomplete.util.math

import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

/* The following formulas have been adapted from this excellent source:
   http://www.movable-type.co.uk/scripts/latlong-vectors.html#intersection
   Thanks to and (c) Chris Veness 2002-2019, MIT Licence

   All the calculations below are done with coordinates in radians.
*/

/** Return a new point translated from this point with initial bearing [θ] and angular distance [δ]  */
fun Vector3d.translate(θ: Double, δ: Double): Vector3d {
    val northPole = Vector3d(0.0, 0.0, 1.0)

    val de = (northPole x this).normalize()   // east direction vector @ n1 (Gade's k_e_E)
    val dn = this x de                        // north direction vector @ n1 (Gade's (k_n_E)

    val d = dn * cos(θ) + de * sin(θ)         // direction vector @ n1 (≡ C×n1; C = great circle)

    val x = this * cos(δ)                     // component of n2 parallel to n1
    val y = d * sin(δ)                        // component of n2 perpendicular to n1

    val n2 = x + y                            // Gade's n_EB_E

    return n2.normalize()
}

/** Returns the distance of two points on a sphere */
fun Vector3d.angularDistanceTo(o: Vector3d) = angleTo(o)

/** Returns the initial bearing from one point to another */
fun Vector3d.initialBearingTo(o: Vector3d): Double {
    val northPole = Vector3d(0.0, 0.0, 1.0)

    val c1 = this x o // great circle through p1 & p2
    val c2 = this x northPole // great circle through p1 & north pole

    return c1.angleTo(c2, this) // bearing is (signed) angle between c1 & c2
}

/** Returns the final bearing from one point to another */
fun Vector3d.finalBearingTo(o: Vector3d): Double =
    normalizeRadians(o.initialBearingTo(this) + PI, 0.0)

fun arcIntersection(a: Vector3d, b: Vector3d, p: Vector3d, q: Vector3d): Vector3d? {
    if (a == b || p == q) return null

    /* cab & cpq are the normals of planes whose intersection with the surface of the sphere
       define the great circles through start & end points. These two planes intersect each other
       in a line and this line pierce the sphere at two points. One on this side of the sphere,
       one directly opposite */
    val cab = a x b
    val cpq = p x q
    // there are two (antipodal) candidate intersection points; we have to choose which to return
    val i1 = (cab x cpq).normalize()

    // if cab x cpq == 0 this means that ab and pq are on the same great circle
    val intersections = if (i1.length == 0.0) {
        // so, just find any...
        sequenceOf(a, b, p, q)
    } else {
        // otherwise candidate + antipodal candidate
        sequenceOf(i1, -i1)
    }

    for (intersection in intersections) {
        val ab = a.angleTo(b)
        val ax = a.angleTo(intersection, cab)

        if (ax < min(0.0, ab) || ax > max(0.0, ab)) continue

        val pq = p.angleTo(q)
        val px = p.angleTo(intersection, cpq)

        if (px < min(0.0, pq) || px > max(0.0, pq)) continue

        return intersection
    }

    return null
}

fun Vector3d.toLatLon(): LatLon {
    val n = normalize()
    val φ = atan2(n.z, sqrt(n.x * n.x + n.y * n.y))
    val λ = atan2(n.y, n.x)
    return LatLon(φ.toDegrees(), λ.toDegrees())
}

fun LatLon.toNormalOnSphere(): Vector3d {
    val φ = latitude.toRadians()
    val λ = longitude.toRadians()

    // right-handed vector: x -> 0°E,0°N; y -> 90°E,0°N, z -> 90°N
    return Vector3d(cos(φ) * cos(λ), cos(φ) * sin(λ), sin(φ)).normalize()
}

private fun Double.toRadians() = this / 180.0 * PI
private fun Double.toDegrees() = this / PI * 180.0

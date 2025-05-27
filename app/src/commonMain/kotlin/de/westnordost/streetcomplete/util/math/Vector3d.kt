// we want to use the greek letters to stay close to the mathematical examples linked below
@file:Suppress("NonAsciiCharacters")

package de.westnordost.streetcomplete.util.math

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sign
import kotlin.math.sin
import kotlin.math.sqrt

/* The following code has been adapted from this excellent source:

   http://www.movable-type.co.uk/scripts/latlong-vectors.html
   Thanks to and (c) Chris Veness 2002-2019, MIT Licence
*/

class Vector3d(val x: Double, val y: Double, val z: Double) {

    val length: Double get() = sqrt(x * x + y * y + z * z)

    operator fun unaryPlus() = this
    operator fun unaryMinus() = Vector3d(-x, -y, -z)

    operator fun times(v: Double) = Vector3d(x * v, y * v, z * v)
    operator fun div(v: Double) = Vector3d(x / v, x / v, x / v)

    operator fun plus(o: Vector3d) = Vector3d(x + o.x, y + o.y, z + o.z)
    operator fun minus(o: Vector3d) = Vector3d(x - o.x, y - o.y, z - o.y)

    /** dot (scalar) product with [o] */
    operator fun times(o: Vector3d) = x * o.x + y * o.y + z * o.z

    /** cross (vector) product with [o] */
    infix fun x(o: Vector3d) = Vector3d(y * o.z - z * o.y, z * o.x - x * o.z, x * o.y - y * o.x)

    /** normalize to length == 1 */
    fun normalize(): Vector3d {
        val len = length
        if (len == 1.0 || len == 0.0) return this
        return Vector3d(x / len, y / len, z / len)
    }

    /**
     * Returns the angle between this vector and vector [o] in radians. If the plane normal [n] is
     * specified, the returned angle is signed +ve if this vector is clockwise looking along n, -ve
     * in opposite direction.
     */
    fun angleTo(o: Vector3d, n: Vector3d? = null): Double {
        // q.v. stackoverflow.com/questions/14066933#answer-16544330, but n·p₁×p₂ is numerically
        // ill-conditioned, so just calculate sign to apply to |p₁×p₂|

        // if n·p₁×p₂ is -ve, negate |p₁×p₂|
        val sign = if (n == null) 1.0 else sign((this x o) * n)

        val sinθ = (this x o).length * sign
        val cosθ = (this * o)

        return atan2(sinθ, cosθ)
    }

    /**
     * Rotates this point around the [axis] by the specified [angle] in radians
     */
    fun rotateAround(axis: Vector3d, angle: Double): Vector3d {
        val θ = angle

        // en.wikipedia.org/wiki/Rotation_matrix#Rotation_matrix_from_axis_and_angle
        // en.wikipedia.org/wiki/Quaternions_and_spatial_rotation#Quaternion-derived_rotation_matrix
        val p = normalize()
        val a = axis.normalize()

        val s = sin(θ)
        val c = cos(θ)
        val t = 1 - c
        val x = a.x
        val y = a.y
        val z = a.z

        val r = arrayOf( // rotation matrix for rotation about supplied axis
            arrayOf(t * x * x + c,     t * x * y - s * z, t * x * z + s * y),
            arrayOf(t * y * x + s * z, t * y * y + c,     t * y * z - s * x),
            arrayOf(t * z * x - s * y, t * z * y + s * x, t * z * z + c    ),
        )

        // multiply r × p
        val rp = arrayOf(
            r[0][0] * p.x + r[0][1] * p.y + r[0][2] * p.z,
            r[1][0] * p.x + r[1][1] * p.y + r[1][2] * p.z,
            r[2][0] * p.x + r[2][1] * p.y + r[2][2] * p.z,
        )
        val p2 = Vector3d(rp[0], rp[1], rp[2])

        return p2
        // qv en.wikipedia.org/wiki/Rodrigues'_rotation_formula...
    }

    override fun equals(other: Any?) =
        other === this || (other is Vector3d && x == other.x && y == other.y && z == other.z)

    override fun hashCode() = ((x.hashCode() * 31) + y.hashCode()) * 31 + z.hashCode()

    override fun toString() = "$x,$y,$z"
}

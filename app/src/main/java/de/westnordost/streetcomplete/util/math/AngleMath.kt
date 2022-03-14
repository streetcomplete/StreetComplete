package de.westnordost.streetcomplete.util.math

import kotlin.math.PI

/** returns a number between [startAt] - [startAt]+360 */
fun normalizeDegrees(value: Double, startAt: Double = 0.0): Double {
    var result = value % 360 // is now -360..360
    result = (result + 360) % 360 // is now 0..360
    if (result > startAt + 360) result -= 360
    return result
}

/** returns a number between [startAt] - [startAt]+2PI */
fun normalizeRadians(value: Double, startAt: Double = 0.0): Double {
    val pi2 = PI * 2
    var result = value % pi2 // is now -2PI..2PI
    result = (result + pi2) % pi2 // is now 0..2PI
    if (result > startAt + pi2) result -= pi2
    return result
}

/** returns a number between [startAt] - [startAt]+360 */
fun normalizeDegrees(value: Float, startAt: Float = 0f): Float {
    var result = value % 360 // is now -360..360
    result = (result + 360) % 360 // is now 0..360
    if (result > startAt + 360) result -= 360
    return result
}

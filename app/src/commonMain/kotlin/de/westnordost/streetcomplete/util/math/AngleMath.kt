package de.westnordost.streetcomplete.util.math

import kotlin.math.PI

/** returns a number in the range [startAt, startAt + 360) */
fun normalizeDegrees(value: Double, startAt: Double = 0.0): Double {
    var result = (value - startAt) % 360
    if (result < 0) result += 360
    return result + startAt
}

/** returns a number in the range [startAt, startAt + 2PI) */
fun normalizeRadians(value: Double, startAt: Double = 0.0): Double {
    val pi2 = PI * 2
    var result = (value - startAt) % pi2
    if (result < 0) result += pi2
    return result + startAt
}

/** returns a number in the range [startAt, startAt + 360) */
fun normalizeDegrees(value: Float, startAt: Float = 0f): Float {
    var result = (value - startAt) % 360
    if (result < 0) result += 360
    return result + startAt
}

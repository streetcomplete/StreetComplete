package de.westnordost.streetcomplete.screens.main.controls

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/** Given an imaginary line drawn from the center of [screen] to [target], returns the point and
 *  angle at which the line intersects with an ellipsis filling the [screen]. */
fun findEllipsisIntersection(
    screen: Rect?,
    target: Offset?
): Intersection? {
    val t = target ?: return null
    val s = screen ?: return null
    val o = s.center
    val delta = t - o

    val theta = atan2(delta.y, delta.x) + PI / 2
    val radius = ellipsisRadius(s.height / 2.0, s.width / 2.0, theta)
    val ellipsisDelta = Offset((sin(theta) * radius).toFloat(), (-cos(theta) * radius).toFloat())
    if (delta.getDistanceSquared() < ellipsisDelta.getDistanceSquared()) return null

    return Intersection(ellipsisDelta + o, theta)
}

private fun ellipsisRadius(a: Double, b: Double, angle: Double): Double {
    val x = sin(angle)
    val y = cos(angle)
    return a * b / sqrt(a * a * x * x + b * b * y * y)
}

data class Intersection(val position: Offset, val angle: Double)

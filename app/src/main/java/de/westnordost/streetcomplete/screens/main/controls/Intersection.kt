package de.westnordost.streetcomplete.screens.main.controls

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import kotlin.math.PI
import kotlin.math.atan2

data class Intersection(val position: Offset, val angle: Double)

/** Given an imaginary line drawn from [origin] to [target], returns the point and angle at which
 *  the line intersects with the bounds closest to [origin] of the [rects]. It returns null if there
 *  is no intersection */
fun findClosestIntersection(
    origin: Offset?,
    target: Offset?,
    rects: Iterable<Rect>,
): Intersection? {
    val o = origin ?: return null
    val t = target ?: return null

    var minA = Float.MAX_VALUE

    for (rect in rects) {
        val a = intersectionWithRect(rect, o, t)
        if (a < minA) minA = a
    }
    if (minA > 1f) return null

    return Intersection(
        position = Offset(o.x + (t.x - o.x) * minA, o.y + (t.y - o.y) * minA),
        angle = atan2(t.y - o.y, t.x - o.x) + PI / 2
    )
}

/** First intersection of line drawn from [o] to [t] with rect [r] or Float.MAX_VALUE if none */
private fun intersectionWithRect(r: Rect, o: Offset, t: Offset): Float {
    var minA = Float.MAX_VALUE
    var a: Float

    // left side
    a = intersectionWithVerticalSegment(o.x, o.y, t.x, t.y, r.left, r.top, r.height)
    if (a < minA) minA = a
    // right side
    a = intersectionWithVerticalSegment(o.x, o.y, t.x, t.y, r.right, r.top, r.height)
    if (a < minA) minA = a
    // top side
    a = intersectionWithHorizontalSegment(o.x, o.y, t.x, t.y, r.left, r.top, r.width)
    if (a < minA) minA = a
    // bottom side
    a = intersectionWithHorizontalSegment(o.x, o.y, t.x, t.y, r.left, r.bottom, r.width)
    if (a < minA) minA = a

    return minA
}

/** Intersection of line segment going from P to Q with vertical line starting at V and given
 *  length. Returns the f for P+f*(Q-P) or MAX_VALUE if no intersection found. */
private fun intersectionWithVerticalSegment(
    px: Float,
    py: Float,
    qx: Float,
    qy: Float,
    vx: Float,
    vy: Float,
    length: Float
): Float {
    val dx = qx - px
    if (dx == 0f) return Float.MAX_VALUE
    val a = (vx - px) / dx

    // not in range of line segment A
    if (a < 0f || a > 1f) return Float.MAX_VALUE

    val dy = qy - py
    val posY = py + dy * a

    // not in range of horizontal line segment
    if (posY < vy || posY > vy + length) return Float.MAX_VALUE

    return a
}

/** Intersection of line segment going from P to Q with horizontal line starting at H and given
 *  length. Returns the f for P+f*(Q-P) or MAX_VALUE if no intersection found. */
private fun intersectionWithHorizontalSegment(
    px: Float,
    py: Float,
    qx: Float,
    qy: Float,
    hx: Float,
    hy: Float,
    length: Float
): Float {
    val dy = qy - py
    if (dy == 0f) return Float.MAX_VALUE
    val a = (hy - py) / dy

    // not in range of line segment P-Q
    if (a < 0f || a > 1f) return Float.MAX_VALUE

    val dx = qx - px
    val posX = px + dx * a

    // not in range of horizontal line segment
    if (posX < hx || posX > hx + length) return Float.MAX_VALUE
    return a
}

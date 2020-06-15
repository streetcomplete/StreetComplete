package de.westnordost.streetcomplete.map

import android.graphics.PointF
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children

fun findClosestIntersection(v: ViewGroup, target: PointF): PointF? {
    val w = v.width.toFloat()
    val h = v.height.toFloat()
    val ox = w / 2
    val oy = h / 2
    val tx = target.x
    val ty = target.y

    var minA = Float.MAX_VALUE
    var a: Float

    // left side
    a = intersectionWithVerticalSegment(ox, oy, tx, ty, 0f, 0f, h)
    if (a < minA) minA = a
    // right side
    a = intersectionWithVerticalSegment(ox, oy, tx, ty, w, 0f, h)
    if (a < minA) minA = a
    // top side
    a = intersectionWithHorizontalSegment(ox, oy, tx, ty, 0f, 0f, w)
    if (a < minA) minA = a
    // bottom side
    a = intersectionWithHorizontalSegment(ox, oy, tx, ty, 0f, h, w)
    if (a < minA) minA = a

    for (child in v.children) {
        if (child.visibility != View.VISIBLE) continue
        val t = child.top.toFloat()
        val b = child.bottom.toFloat()
        val r = child.right.toFloat()
        val l = child.left.toFloat()
        // left side
        if (l > ox && l < w) {
            a = intersectionWithVerticalSegment(ox, oy, tx, ty, l, t, b - t)
            if (a < minA) minA = a
        }
        // right side
        if (r > 0 && r < ox) {
            a = intersectionWithVerticalSegment(ox, oy, tx, ty, r, t, b - t)
            if (a < minA) minA = a
        }
        // top side
        if (t > oy && t < h) {
            a = intersectionWithHorizontalSegment(ox, oy, tx, ty, l, t, r - l)
            if (a < minA) minA = a
        }
        // bottom side
        if (b > 0 && b < oy) {
            a = intersectionWithHorizontalSegment(ox, oy, tx, ty, l, b, r - l)
            if (a < minA) minA = a
        }
    }

    return if (minA <= 1f) PointF(ox + (tx - ox) * minA, oy + (ty - oy) * minA) else null
}

/** Intersection of line segment going from P to Q with vertical line starting at V and given
 *  length. Returns the f for P+f*(Q-P) or MAX_VALUE if no intersection found. */
private fun intersectionWithVerticalSegment(
    px: Float, py: Float,
    qx: Float, qy: Float,
    vx: Float, vy: Float,
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
    px: Float, py: Float,
    qx: Float, qy: Float,
    hx: Float, hy: Float,
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

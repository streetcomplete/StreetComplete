package de.westnordost.streetcomplete.data.osm.geometry.polygons

import kotlin.math.sqrt

class Cell(
    val centerX: Double,
    val centerY: Double,
    val half: Double, // half of the cell size
    val distance: Double, // distance between cell center and polygon. Positive if inside
) : Comparable<Cell> {

    /* max distance to expect, optimistic bound */
    val max: Double = distance + half * SQRT2

    /* Looking for the most promising cell */
    override fun compareTo(other: Cell): Int = other.max.compareTo(this.max)

    companion object {
        private val SQRT2 = sqrt(2.0)
    }
}

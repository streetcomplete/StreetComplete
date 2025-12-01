package de.westnordost.streetcomplete.data.osm.geometry

import de.westnordost.streetcomplete.data.osm.geometry.polygons.Point
import de.westnordost.streetcomplete.data.osm.geometry.polygons.Polygon
import de.westnordost.streetcomplete.data.osm.geometry.polygons.PolygonAlgorithms
import kotlin.test.Test
import kotlin.test.assertTrue

class PolylabelHoleTest {

    @Test
    fun testSquareDonut() {
        val outer = listOf(
            Point(0.0, 0.0),
            Point(20.0, 0.0),
            Point(20.0, 20.0),
            Point(0.0, 20.0),
            Point(0.0, 0.0)
        )

        val hole = listOf(
            Point(8.0, 8.0),
            Point(12.0, 8.0),
            Point(12.0, 12.0),
            Point(8.0, 12.0),
            Point(8.0, 8.0)
        )

        val polygon = Polygon(outer, holes = listOf(hole))
        val result = PolygonAlgorithms.polylabel(polygon, precision = 0.5)

        // The result must be inside the outer polygon
        assertTrue(isPointInRing(result, outer), "Result should be inside the outer polygon")

        // The result must be outside the hole
        assertTrue(!isPointInRing(result, hole), "Result should be outside the hole")

        // Optional: approximate distance to nearest boundary
        val dist = PolygonAlgorithms.run {
            privatePointToPolygonDist(result, polygon)
        }
        assertTrue(dist > 3.5, "Result should be reasonably far from edges")
    }

    // Helper (copy of existing point-in-ring logic)
    private fun isPointInRing(p: Point, ring: List<Point>): Boolean {
        var inside = false
        var j = ring.lastIndex
        for (i in ring.indices) {
            val xi = ring[i].x
            val yi = ring[i].y
            val xj = ring[j].x
            val yj = ring[j].y
            if ((yi > p.y) != (yj > p.y) &&
                (p.x < (xj - xi) * (p.y - yi) / (yj - yi + 0.0) + xi)) {
                inside = !inside
            }
            j = i
        }
        return inside
    }

    // Expose pointToPolygonDist for distance check (since private in PolygonAlgorithms)
    private fun PolygonAlgorithms.privatePointToPolygonDist(p: Point, polygon: Polygon): Double {
        val method = PolygonAlgorithms::class.java.getDeclaredMethod(
            "pointToPolygonDist", Point::class.java, Polygon::class.java
        )
        method.isAccessible = true
        return method.invoke(this, p, polygon) as Double
    }
}

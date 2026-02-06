package de.westnordost.streetcomplete.data.osm.geometry

import de.westnordost.streetcomplete.data.osm.geometry.polygons.Point
import de.westnordost.streetcomplete.data.osm.geometry.polygons.Polygon
import de.westnordost.streetcomplete.data.osm.geometry.polygons.PolygonAlgorithms
import kotlin.test.Test
import kotlin.test.assertTrue

class PolylabelTest {

    @Test
    fun testIrregularPolygon() {
        val shape = listOf(
            Point(100.0, 100.0),
            Point(500.0, 120.0),
            Point(480.0, 400.0),
            Point(200.0, 450.0),
            Point(120.0, 300.0),
            Point(100.0, 100.0)
        )

        val polygon = Polygon(shape)

        val result = PolygonAlgorithms.polylabel(polygon, precision = 5.0)

        // We don’t hardcode a value — we test geometric properties.
        // 1. It must be inside the bounding box:
        assertTrue(result.x in 100.0..500.0)
        assertTrue(result.y in 100.0..450.0)

        // 2. It must be inside polygon (distance > 0)
        val dist = PolygonAlgorithms.pointToPolygonDist(result, polygon)
        assertTrue(dist > 0.0)
    }
}

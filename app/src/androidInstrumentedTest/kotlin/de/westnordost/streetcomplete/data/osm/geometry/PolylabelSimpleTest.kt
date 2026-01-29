package de.westnordost.streetcomplete.data.osm.geometry

import de.westnordost.streetcomplete.data.osm.geometry.polygons.Point
import de.westnordost.streetcomplete.data.osm.geometry.polygons.Polygon
import de.westnordost.streetcomplete.data.osm.geometry.polygons.PolygonAlgorithms
import kotlin.test.Test
import kotlin.test.assertEquals

class PolylabelSimpleTest {

    @Test
    fun testSimpleSquare() {
        val poly = Polygon(
            shape = listOf(
                Point(0.0, 0.0),
                Point(10.0, 0.0),
                Point(10.0, 10.0),
                Point(0.0, 10.0),
                Point(0.0, 0.0)
            )
        )

        val result = PolygonAlgorithms.polylabel(poly, precision = 1.0)

        assertEquals(5.0, result.x, 1.0)
        assertEquals(5.0, result.y, 1.0)
    }
}

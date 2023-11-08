package de.westnordost.streetcomplete.data.import

import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.util.math.area
import de.westnordost.streetcomplete.util.math.contains
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.math.pow
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue

class GpxImportMapToCenteredSquaresTest {
    @Test
    fun `fails with bad parameters`() = runTest {
        assertFails {
            emptyFlow<LatLon>().mapToCenteredSquares(-1.0).count()
        }
    }

    @Test
    fun `maps correctly`() = runTest {
        val point = LatLon(12.0, 37.1)
        val halfSideLength = 100.0
        val squares = flowOf(point).mapToCenteredSquares(halfSideLength).toList()
        assertEquals(
            1,
            squares.size,
            "expected a single bounding box, ${squares.size} returned"
        )
        assertTrue(squares[0].contains(point), "center not contained in bounding box")
        assertEquals(
            (halfSideLength * 2).pow(2),
            squares[0].area(),
            10.0,
            "area not matching"
        )
    }
}

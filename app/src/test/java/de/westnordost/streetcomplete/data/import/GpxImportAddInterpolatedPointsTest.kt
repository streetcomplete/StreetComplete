package de.westnordost.streetcomplete.data.import

import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.util.math.distanceTo
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue

class GpxImportAddInterpolatedPointsTest {
    @Test
    fun `fails with bad parameters`() = runTest {
        assertFails {
            emptyFlow<LatLon>().addInterpolatedPoints(-15.0).count()
        }
    }

    @Test
    fun `gracefully handles small flows`() = runTest {
        assertEquals(
            0,
            emptyFlow<LatLon>().addInterpolatedPoints(1.0).count(),
            "empty flow invents points"
        )
        assertEquals(
            1,
            flowOf(LatLon(89.9, 27.1)).addInterpolatedPoints(77.0).count(),
            "size 1 flow invents points"
        )
    }

    @Test
    fun `does not add unnecessary points`() = runTest {
        val originalPoints = listOf(LatLon(0.0, 0.0), LatLon(0.1, -0.5), LatLon(1.0, -1.0))
        val samplingDistance =
            originalPoints.zipWithNext().maxOf { it.first.distanceTo(it.second) }
        val interpolatedPoints =
            originalPoints.asFlow().addInterpolatedPoints(samplingDistance).toList()
        assertEquals(originalPoints.size, interpolatedPoints.size)
    }

    @Test
    fun `ensures promised sampling distance on single segment`() = runTest {
        val p1 = LatLon(-11.1, 36.7)
        val p2 = LatLon(-89.0, 61.0)
        val numIntermediatePoints = 100
        val samplingDistance = p1.distanceTo(p2) / (numIntermediatePoints + 1)
        val originalPoints = listOf(p1, p2)
        val interpolatedPoints =
            originalPoints.asFlow().addInterpolatedPoints(samplingDistance).toList()
        assertEquals(
            numIntermediatePoints + 2,
            interpolatedPoints.size,
            "wrong number of points created"
        )
        assertCorrectSampling(originalPoints, interpolatedPoints, samplingDistance)
    }

    @Test
    fun `ensures promised sampling distance on multiple segments`() = runTest {
        val originalPoints =
            listOf(LatLon(0.0, 0.0), LatLon(1.0, 1.0), LatLon(2.1, 1.3), LatLon(0.0, 0.0))
        val samplingDistance =
            originalPoints.zipWithNext().minOf { it.first.distanceTo(it.second) } / 100
        val interpolatedPoints =
            originalPoints.asFlow().addInterpolatedPoints(samplingDistance).toList()
        assertCorrectSampling(originalPoints, interpolatedPoints, samplingDistance)
    }

    private fun assertCorrectSampling(
        originalPoints: List<LatLon>,
        interpolatedPoints: List<LatLon>,
        samplingDistance: Double,
    ) {
        // some tolerance is needed due to rounding errors
        val maxToleratedDistance = samplingDistance * 1.001
        val minToleratedDistance = samplingDistance * 0.999

        val distances = interpolatedPoints.zipWithNext().map { it.first.distanceTo(it.second) }
        distances.forEachIndexed { index, distance ->
            assertTrue(
                distance <= maxToleratedDistance,
                "distance between consecutive points too big; $distance > $maxToleratedDistance"
            )

            // the only distance that may be smaller than samplingDistance is between the last
            // interpolated point of one segment and the original point starting the next segment
            if (distance < minToleratedDistance) {
                assertContains(
                    originalPoints, interpolatedPoints[index + 1],
                    "distance between two interpolated points too small ($distance < $minToleratedDistance"
                )
            }

        }

        originalPoints.forEach {
            assertContains(
                interpolatedPoints, it,
                "original point $it is missing in interpolated points"
            )
        }
    }
}

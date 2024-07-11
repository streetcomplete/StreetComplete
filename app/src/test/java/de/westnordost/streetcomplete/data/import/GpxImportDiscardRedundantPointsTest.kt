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
import kotlin.test.assertEquals
import kotlin.test.assertFails

class GpxImportDiscardRedundantPointsTest {
    @Test
    fun `fails with bad parameters`() = runTest {
        assertFails {
            emptyFlow<LatLon>().discardRedundantPoints(-15.0).count()
        }
    }

    @Test
    fun `gracefully handles small flows`() = runTest {
        assertEquals(
            0,
            emptyFlow<LatLon>().discardRedundantPoints(1.0).count(),
            "empty flow not retained"
        )
        assertEquals(
            1,
            flowOf(LatLon(89.9, 27.1)).discardRedundantPoints(77.0).count(),
            "size 1 flow not retained"
        )
        assertEquals(
            2,
            flowOf(LatLon(-41.7, -39.8), LatLon(33.1, 78.8)).discardRedundantPoints(20.0)
                .count(),
            "size 2 flow not retained"
        )
    }

    @Test
    fun `keeps non-redundant points`() = runTest {
        val originalPoints = listOf(LatLon(10.10, -2.0), LatLon(19.0, -54.4), LatLon(51.04, -71.30))
        val samplingDistance = originalPoints.zipWithNext().minOf { it.first.distanceTo(it.second) }
        val retainedPoints =
            originalPoints.asFlow().discardRedundantPoints(samplingDistance).toList()
        assertEquals(
            originalPoints.size,
            retainedPoints.size,
            "dropping non-redundant points"
        )
    }

    @Test
    fun `discards single redundant point`() = runTest {
        val originalPoints = listOf(LatLon(10.10, -2.0), LatLon(19.0, -54.4), LatLon(51.04, -71.30))
        val epsilon = 0.00001
        val samplingDistance =
            originalPoints.zipWithNext().maxOf { it.first.distanceTo(it.second) } + epsilon
        assertEquals(
            originalPoints.size - 1,
            originalPoints.asFlow().discardRedundantPoints(samplingDistance).count(),
            "failed to drop redundant point"
        )
    }

    @Test
    fun `discards multiple adjacent redundant points`() = runTest {
        val originalPoints = listOf(
            LatLon(1.0, 0.0),
            LatLon(2.0, 0.0),
            LatLon(3.0, 0.0),
            LatLon(4.0, 0.0)
        )
        val samplingDistance = originalPoints.first().distanceTo(originalPoints.last())
        assertEquals(
            2,
            originalPoints.asFlow().discardRedundantPoints(samplingDistance).count(),
            "failed to drop redundant point"
        )
    }

    @Test
    fun `discards multiple non-adjacent redundant points`() = runTest {
        val originalPoints = listOf(
            LatLon(1.0, 0.0),
            LatLon(2.0, 0.0),
            LatLon(3.0, 0.0),
            LatLon(7.0, 0.0),
            LatLon(8.0, 0.0),
            LatLon(9.0, 0.0),
        )
        val samplingDistance = originalPoints[0].distanceTo(originalPoints[2])
        assertEquals(
            4,
            originalPoints.asFlow().discardRedundantPoints(samplingDistance).count(),
            "failed to drop redundant point"
        )
    }
}

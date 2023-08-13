package de.westnordost.streetcomplete.util.math

import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.pow
import kotlin.random.Random
import kotlin.system.measureTimeMillis

class FlatEarthMathTest {

    @Test fun flatAngularDistancePrecision() {
        val deltaDegrees = 0.03
        val precisionInMeters = 0.1
        repeat(1000000) {
            val p1 = LatLon(
                Random.nextDouble(-90 + deltaDegrees, 90 - deltaDegrees),
                Random.nextDouble(-180 + deltaDegrees, 180 - deltaDegrees)
            )
            val p2 = LatLon(
                p1.latitude + Random.nextDouble(-deltaDegrees, deltaDegrees),
                p1.longitude + Random.nextDouble(-deltaDegrees, deltaDegrees)
            )
            val dExact = p1.distanceTo(p2)
            val dApproximate = p1.flatDistanceTo(p2)
            assertTrue(abs(dExact - dApproximate) < precisionInMeters)
        }
    }

    @Test fun flatAngularDistanceToArcPrecision() {
        val deltaDegrees = 0.03
        val precisionInMeters = 0.5
        repeat(1000000) {
            val p1 = LatLon(
                Random.nextDouble(-90 + deltaDegrees, 90 - deltaDegrees),
                Random.nextDouble(-180 + deltaDegrees, 180 - deltaDegrees)
            )
            val p2 = LatLon(
                p1.latitude + Random.nextDouble(-deltaDegrees, deltaDegrees),
                p1.longitude + Random.nextDouble(-deltaDegrees, deltaDegrees)
            )
            val p3 = LatLon(
                p1.latitude + Random.nextDouble(-deltaDegrees, deltaDegrees),
                p1.longitude + Random.nextDouble(-deltaDegrees, deltaDegrees)
            )
            val dExact = p3.distanceToArc(p1, p2)
            val dApproximate = p3.flatDistanceToArc(p1, p2)
            assertTrue(abs(dExact - dApproximate) < precisionInMeters)
        }
    }
}

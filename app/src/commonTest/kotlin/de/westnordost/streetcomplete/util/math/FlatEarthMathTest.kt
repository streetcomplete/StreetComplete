package de.westnordost.streetcomplete.util.math

import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

class FlatEarthMathTest {

    @Test fun flatAngularDistancePrecision() {
        val deltaDegrees = 0.03
        val precisionInMeters = 0.1
        repeat(1000000) {
            val p1 = LatLon(
                Random.nextDouble(-90 + deltaDegrees, 90 - deltaDegrees),
                Random.nextDouble(-180.0, 180.0)
            )
            val p2 = LatLon(
                p1.latitude + Random.nextDouble(-deltaDegrees, deltaDegrees),
                normalizeLongitude(p1.longitude + Random.nextDouble(-deltaDegrees, deltaDegrees))
            )
            val dExact = p1.distanceTo(p2)
            val dApproximate = p1.flatDistanceTo(p2)
            assertEquals(dExact, dApproximate, precisionInMeters)
        }
    }

    @Test fun flatAngularDistanceToArcPrecision() {
        val deltaDegrees = 0.03
        val precisionInMeters = 0.6
        repeat(1000000) {
            val p1 = LatLon(
                Random.nextDouble(-90 + deltaDegrees, 90 - deltaDegrees),
                Random.nextDouble(-180.0, 180.0)
            )
            val p2 = LatLon(
                p1.latitude + Random.nextDouble(-deltaDegrees, deltaDegrees),
                normalizeLongitude(p1.longitude + Random.nextDouble(-deltaDegrees, deltaDegrees))
            )
            val p3 = LatLon(
                p1.latitude + Random.nextDouble(-deltaDegrees, deltaDegrees),
                normalizeLongitude(p1.longitude + Random.nextDouble(-deltaDegrees, deltaDegrees))
            )
            val dExact = p3.distanceToArc(p1, p2)
            val dApproximate = p3.flatDistanceToArc(p1, p2)
            assertEquals(dExact, dApproximate, precisionInMeters)
        }
    }
}

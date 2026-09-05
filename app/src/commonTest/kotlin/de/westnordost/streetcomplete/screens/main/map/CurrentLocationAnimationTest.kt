package de.westnordost.streetcomplete.screens.main.map

import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.screens.main.map.layers.locationRadiusAtEquator
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CurrentLocationAnimationTest {

    @Test fun positionTakesShortestPathAcrossAntimeridian() {
        val midpoint = interpolateLatLon(LatLon(10.0, 179.0), LatLon(20.0, -179.0), 0.5)

        assertEquals(15.0, midpoint.latitude)
        assertTrue(abs(abs(midpoint.longitude) - 180.0) < 0.000001)
    }

    @Test fun clockwiseRotationTakesShortestTurnAcrossZero() {
        assertEquals(370f, shortestRotationTarget(start = 350f, target = 10f))
    }

    @Test fun counterClockwiseRotationTakesShortestTurnAcrossZero() {
        assertEquals(-10f, shortestRotationTarget(start = 10f, target = 350f))
    }

    @Test fun meterScaleAccountsForMercatorLatitude() {
        assertTrue(metersSizeFactor(60.0) < metersSizeFactor(0.0))
    }

    @Test fun equatorAdjustedRadiusPreservesLatitudeScale() {
        val latitude = 60.0
        val radiusMeters = 12f
        val adjusted = locationRadiusAtEquator(radiusMeters, latitude)

        assertEquals(
            radiusMeters / metersSizeFactor(latitude),
            adjusted / metersSizeFactor(0.0),
            absoluteTolerance = 0.000001f,
        )
    }
}

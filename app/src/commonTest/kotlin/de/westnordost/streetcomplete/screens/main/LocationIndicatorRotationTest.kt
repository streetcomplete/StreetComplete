package de.westnordost.streetcomplete.screens.main

import org.maplibre.spatialk.units.Bearing
import kotlin.test.Test
import kotlin.test.assertEquals

class LocationIndicatorRotationTest {

    @Test fun headingRotatesClockwiseFromNorth() {
        val directions = listOf(
            Bearing.North to 0f,
            Bearing.Northeast to 45f,
            Bearing.East to 90f,
            Bearing.South to 180f,
            Bearing.West to 270f,
        )
        for ((heading, expected) in directions) {
            assertEquals(expected, locationIndicatorRotation(heading, cameraBearing = 0.0))
        }
    }

    @Test fun cameraRotationIsSubtractedFromHeading() {
        assertEquals(45f, locationIndicatorRotation(Bearing.East, cameraBearing = 45.0))
        assertEquals(-90f, locationIndicatorRotation(Bearing.North, cameraBearing = 90.0))
    }

    @Test fun headingAlignedWithCameraPointsUp() {
        assertEquals(0f, locationIndicatorRotation(Bearing.East, cameraBearing = 90.0))
        assertEquals(0f, locationIndicatorRotation(Bearing.West, cameraBearing = 270.0))
    }
}

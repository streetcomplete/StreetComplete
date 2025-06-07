package de.westnordost.streetcomplete.util.math

import kotlin.math.PI
import kotlin.test.Test
import kotlin.test.assertEquals

class AngleMathTest {
    @Test fun normalizeDegreesDouble() {
        assertEquals(0.0, normalizeDegrees(0.0), 1e-12)
        assertEquals(0.0, normalizeDegrees(360.0), 1e-12)
        assertEquals(0.0, normalizeDegrees(-360.0), 1e-12)
        assertEquals(10.0, normalizeDegrees(370.0), 1e-12)
        assertEquals(350.0, normalizeDegrees(-370.0), 1e-12)
        assertEquals(180.0, normalizeDegrees(540.0, 180.0), 1e-12)
        assertEquals(-180.0, normalizeDegrees(180.0, -180.0), 1e-12)
        assertEquals(-180.0, normalizeDegrees(540.0, -180.0), 1e-12)
        assertEquals(0.0, normalizeDegrees(0.0, -180.0), 1e-12)
        assertEquals(225.0, normalizeDegrees(-135.0, -90.0), 1e-12)
    }

    @Test fun normalizeRadiansDouble() {
        assertEquals(0.0, normalizeRadians(0.0), 1e-12)
        assertEquals(0.0, normalizeRadians(2 * PI), 1e-12)
        assertEquals(0.0, normalizeRadians(-2 * PI), 1e-12)
        assertEquals(PI / 2, normalizeRadians(PI / 2), 1e-12)
        assertEquals(PI, normalizeRadians(3 * PI), 1e-12)
        assertEquals(PI, normalizeRadians(-3 * PI), 1e-12)
        assertEquals(-PI, normalizeRadians(3 * PI, -PI), 1e-12)
        assertEquals(0.0, normalizeRadians(0.0, -PI), 1e-12)
        assertEquals(PI / 2, normalizeRadians(PI / 2, -PI / 2), 1e-12)
        assertEquals(PI / 2, normalizeRadians(-3 * PI / 2, -PI), 1e-12)
    }

    @Test fun normalizeDegreesFloat() {
        assertEquals(0f, normalizeDegrees(0f))
        assertEquals(0f, normalizeDegrees(360f))
        assertEquals(0f, normalizeDegrees(-360f))
        assertEquals(10f, normalizeDegrees(370f))
        assertEquals(350f, normalizeDegrees(-370f))
        assertEquals(180f, normalizeDegrees(540f, 180f))
        assertEquals(-180f, normalizeDegrees(180f, -180f))
        assertEquals(-180f, normalizeDegrees(540f, -180f))
        assertEquals(0f, normalizeDegrees(0f, -180f))
        assertEquals(225f, normalizeDegrees(-135f, -90f))
    }
}

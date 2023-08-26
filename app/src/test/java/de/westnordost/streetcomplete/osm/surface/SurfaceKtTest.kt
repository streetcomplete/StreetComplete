package de.westnordost.streetcomplete.osm.surface

import kotlin.test.*
import kotlin.test.Test

class SurfaceKtTest {

    @Test fun `surface=unpaved is underspecified and must be described`() {
        assertTrue(Surface.UNPAVED.shouldBeDescribed)
        assertTrue(Surface.UNPAVED.shouldBeDescribed)
    }

    @Test fun `surface=asphalt is well specified and does not need description`() {
        assertFalse(Surface.ASPHALT.shouldBeDescribed)
    }

    @Test fun `surface=ground is underspecified and does not need description`() {
        assertFalse(Surface.GROUND.shouldBeDescribed)
        assertFalse(Surface.GROUND.shouldBeDescribed)
    }
}

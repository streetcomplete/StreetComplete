package de.westnordost.streetcomplete.overlays.surface

import de.westnordost.streetcomplete.osm.surface.Surface
import kotlin.test.Test
import kotlin.test.assertEquals

class SurfaceOverlayAnswerParserTest {

    @Test fun `parse single surface`() {
        assertEquals(
            SingleSurface(Surface.ASPHALT),
            parseSurfaceOverlayAnswer(mapOf(
                "surface" to "asphalt",
                "highway" to "residential"
            ))
        )
    }

    @Test fun `parse segregated surface`() {
        assertEquals(
            SegregatedSurface(Surface.PAVING_STONES, Surface.ASPHALT),
            parseSurfaceOverlayAnswer(mapOf(
                "highway" to "path",
                "footway:surface" to "paving_stones",
                "cycleway:surface" to "asphalt"
            ))
        )
    }

    @Test fun `parse no surface`() {
        assertEquals(
            SingleSurface(null),
            parseSurfaceOverlayAnswer(emptyMap())
        )
    }
}

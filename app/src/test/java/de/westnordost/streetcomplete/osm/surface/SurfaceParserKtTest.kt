package de.westnordost.streetcomplete.osm.surface

import kotlin.test.*
import kotlin.test.Test

class SurfaceParserKtTest {

    @Test fun `parse surface`() {
        assertEquals(parseSurface("asphalt"), Surface.ASPHALT)
        assertEquals(parseSurface("earth"), Surface.EARTH)
        assertEquals(parseSurface("soil"), Surface.SOIL)
    }

    @Test fun `parse unknown surface`() {
        assertEquals(parseSurface("wobbly_goo"), Surface.UNKNOWN)
    }

    @Test fun `parse invalid surface`() {
        assertNull(parseSurface("cobblestone"))
        assertNull(parseSurface("cement"))
        assertNull(parseSurface("paved;unpaved"))
        assertNull(parseSurface("<different>"))
    }

    @Test fun `parse no surface`() {
        assertNull(parseSurface(null))
    }
}

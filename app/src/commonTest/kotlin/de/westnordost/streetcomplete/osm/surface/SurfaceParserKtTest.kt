package de.westnordost.streetcomplete.osm.surface

import kotlin.test.*
import kotlin.test.Test

class SurfaceParserKtTest {

    @Test fun `parse surface`() {
        assertEquals(parseSurface("asphalt"), Surface.ASPHALT)
    }

    @Test fun `parse surface alias`() {
        assertEquals(parseSurface("earth"), Surface.DIRT)
        assertEquals(parseSurface("tartan"), Surface.RUBBER)
    }

    @Test fun `parse unknown surface`() {
        assertEquals(parseSurface("wobbly_goo"), Surface.UNSUPPORTED)
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

package de.westnordost.streetcomplete.osm.surface

import kotlin.test.*
import kotlin.test.Test

class SurfaceParserKtTest {

    @Test fun `parse surface`() {
        assertEquals(
            parseSurfaceAndNote(mapOf("surface" to "asphalt")),
            SurfaceAndNote(Surface.ASPHALT, null)
        )
        assertEquals(
            parseSurfaceAndNote(mapOf("surface" to "earth")),
            SurfaceAndNote(Surface.EARTH, null)
        )
        assertEquals(
            parseSurfaceAndNote(mapOf("surface" to "soil")),
            SurfaceAndNote(Surface.SOIL, null)
        )
    }

    @Test fun `parse unknown surface`() {
        assertEquals(
            parseSurfaceAndNote(mapOf("surface" to "wobbly_goo")),
            SurfaceAndNote(Surface.UNKNOWN, null)
        )
    }

    @Test fun `parse invalid surface`() {
        assertNull(parseSurfaceAndNote(mapOf("surface" to "cobblestone")))
        assertNull(parseSurfaceAndNote(mapOf("surface" to "cement")))
        assertNull(parseSurfaceAndNote(mapOf("surface" to "paved;unpaved")))
        assertNull(parseSurfaceAndNote(mapOf("surface" to "<different>")))
    }

    @Test fun `parse surface and note`() {
        assertEquals(
            parseSurfaceAndNote(mapOf("surface" to "asphalt", "surface:note" to "blurgl")),
            SurfaceAndNote(Surface.ASPHALT, "blurgl")
        )
    }

    @Test fun `parse no surface`() {
        assertNull(parseSurfaceAndNote(mapOf()))
    }

    @Test fun `parse surface with prefix`() {
        assertEquals(
            parseSurfaceAndNote(mapOf("footway:surface" to "asphalt", "footway:surface:note" to "hey"), "footway"),
            SurfaceAndNote(Surface.ASPHALT, "hey")
        )
    }
}

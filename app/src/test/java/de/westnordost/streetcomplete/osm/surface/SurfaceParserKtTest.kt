package de.westnordost.streetcomplete.osm.surface

import kotlin.test.*
import kotlin.test.Test

class SurfaceParserKtTest {

    @Test fun `parse surface`() {
        assertEquals(
            createSurfaceAndNote(mapOf("surface" to "asphalt")),
            SurfaceAndNote(Surface.ASPHALT, null)
        )
        assertEquals(
            createSurfaceAndNote(mapOf("surface" to "earth")),
            SurfaceAndNote(Surface.EARTH, null)
        )
        assertEquals(
            createSurfaceAndNote(mapOf("surface" to "soil")),
            SurfaceAndNote(Surface.SOIL, null)
        )
    }

    @Test fun `parse unknown surface`() {
        assertEquals(
            createSurfaceAndNote(mapOf("surface" to "wobbly_goo")),
            SurfaceAndNote(Surface.UNKNOWN, null)
        )
    }

    @Test fun `parse invalid surface`() {
        assertNull(createSurfaceAndNote(mapOf("surface" to "cobblestone")))
        assertNull(createSurfaceAndNote(mapOf("surface" to "cement")))
        assertNull(createSurfaceAndNote(mapOf("surface" to "paved;unpaved")))
        assertNull(createSurfaceAndNote(mapOf("surface" to "<different>")))
    }

    @Test fun `parse surface and note`() {
        assertEquals(
            createSurfaceAndNote(mapOf("surface" to "asphalt", "surface:note" to "blurgl")),
            SurfaceAndNote(Surface.ASPHALT, "blurgl")
        )
    }

    @Test fun `parse no surface`() {
        assertNull(createSurfaceAndNote(mapOf()))
    }

    @Test fun `parse surface with prefix`() {
        assertEquals(
            createSurfaceAndNote(mapOf("footway:surface" to "asphalt", "footway:surface:note" to "hey"), "footway"),
            SurfaceAndNote(Surface.ASPHALT, "hey")
        )
    }
}

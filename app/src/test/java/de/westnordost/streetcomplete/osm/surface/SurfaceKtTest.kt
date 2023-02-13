package de.westnordost.streetcomplete.osm.surface

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SurfaceKtTest {
    @Test
    fun `poor tracktype conflicts with paved surface`() {
        assertTrue(isSurfaceAndTracktypeMismatching("asphalt", "grade5"))
    }

    @Test
    fun `high quality tracktype conflicts with poor surface`() {
        assertTrue(isSurfaceAndTracktypeMismatching("gravel", "grade1"))
    }

    @Test
    fun `high quality tracktype fits good surface`() {
        assertFalse(isSurfaceAndTracktypeMismatching("paving_stones", "grade1"))
    }

    @Test
    fun `unknown tracktype does not crash or conflict`() {
        assertFalse(isSurfaceAndTracktypeMismatching("paving_stones", "lorem ipsum"))
    }

    @Test
    fun `unknown surface does not crash or conflict`() {
        assertFalse(isSurfaceAndTracktypeMismatching("zażółć", "grade1"))
    }

    @Test
    fun `specific surface generates specific surface status for roads`() {
        assertEquals(createMainSurfaceStatus(mapOf("surface" to "asphalt")).value, Surface.ASPHALT)
        assertEquals(createMainSurfaceStatus(mapOf("surface" to "asphalt")).note, null)
    }

    @Test
    fun `specific surface generates specific surface status for paths`() {
        assertEquals(createSurfaceStatus(mapOf("surface" to "asphalt")).main, Surface.ASPHALT)
        assertEquals(createSurfaceStatus(mapOf("surface" to "asphalt")).note, null)
        assertEquals(createSurfaceStatus(mapOf("surface" to "asphalt")).cycleway, null)
        assertEquals(createSurfaceStatus(mapOf("surface" to "asphalt")).cyclewayNote, null)
        assertEquals(createSurfaceStatus(mapOf("surface" to "asphalt")).footway, null)
        assertEquals(createSurfaceStatus(mapOf("surface" to "asphalt")).footwayNote, null)
    }

    @Test
    fun `note tag results in a diferent status for roads`() {
        assertEquals(createMainSurfaceStatus(mapOf("surface" to "asphalt", "surface:note" to "useful info")).value, Surface.ASPHALT)
        assertEquals(createMainSurfaceStatus(mapOf("surface" to "asphalt", "surface:note" to "useful info")).note, "useful info")
    }

    @Test
    fun `note tag results in a diferent status for paths`() {
        assertEquals(createSurfaceStatus(mapOf("surface" to "asphalt", "surface:note" to "useful info")).main, Surface.ASPHALT)
        assertEquals(createSurfaceStatus(mapOf("surface" to "asphalt", "surface:note" to "useful info")).note, "useful info")
        assertEquals(createSurfaceStatus(mapOf("surface" to "asphalt", "surface:note" to "useful info")).cycleway, null)
        assertEquals(createSurfaceStatus(mapOf("surface" to "asphalt", "surface:note" to "useful info")).cyclewayNote, null)
        assertEquals(createSurfaceStatus(mapOf("surface" to "asphalt", "surface:note" to "useful info")).footway, null)
        assertEquals(createSurfaceStatus(mapOf("surface" to "asphalt", "surface:note" to "useful info")).footwayNote, null)
    }

    @Test
    fun `paved and unpaved is treated as missing surface for roads and paths`() {
        assertEquals(createMainSurfaceStatus(mapOf("surface" to "unpaved")).value, null)
        assertEquals(createMainSurfaceStatus(mapOf("surface" to "unpaved")).note, null)
        assertEquals(createSurfaceStatus(mapOf("surface" to "unpaved")).main, null)
        assertEquals(createSurfaceStatus(mapOf("surface" to "unpaved")).note, null)
        assertEquals(createSurfaceStatus(mapOf("surface" to "unpaved")).cycleway, null)
        assertEquals(createSurfaceStatus(mapOf("surface" to "unpaved")).cyclewayNote, null)
        assertEquals(createSurfaceStatus(mapOf("surface" to "unpaved")).footway, null)
        assertEquals(createSurfaceStatus(mapOf("surface" to "unpaved")).footwayNote, null)
    }

    @Test
    fun `paved and unpaved is not removed when with note for both roads and paths`() {
        assertTrue(createSurfaceStatus(mapOf("surface" to "unpaved", "surface:note" to "foobar")).main in listOf(Surface.UNPAVED_ROAD, Surface.UNPAVED_AREA))
        assertEquals(createSurfaceStatus(mapOf("surface" to "unpaved", "surface:note" to "foobar")).note, "foobar")
        assertEquals(createSurfaceStatus(mapOf("surface" to "unpaved", "surface:note" to "foobar")).cycleway, null)
        assertEquals(createSurfaceStatus(mapOf("surface" to "unpaved", "surface:note" to "foobar")).cyclewayNote, null)
        assertEquals(createSurfaceStatus(mapOf("surface" to "unpaved", "surface:note" to "foobar")).footway, null)
        assertEquals(createSurfaceStatus(mapOf("surface" to "unpaved", "surface:note" to "foobar")).footwayNote, null)
    }

    @Test
    fun `cobblestone is treated as missing surface for roads and paths`() {
        assertEquals(createMainSurfaceStatus(mapOf("surface" to "cobblestone")).value, null)
        assertEquals(createMainSurfaceStatus(mapOf("surface" to "cobblestone")).note, null)
        assertEquals(createSurfaceStatus(mapOf("surface" to "cobblestone")).main, null)
        assertEquals(createSurfaceStatus(mapOf("surface" to "cobblestone")).note, null)
        assertEquals(createSurfaceStatus(mapOf("surface" to "cobblestone")).cycleway, null)
        assertEquals(createSurfaceStatus(mapOf("surface" to "cobblestone")).cyclewayNote, null)
        assertEquals(createSurfaceStatus(mapOf("surface" to "cobblestone")).footway, null)
        assertEquals(createSurfaceStatus(mapOf("surface" to "cobblestone")).footwayNote, null)
    }

    @Test
    fun `surface note is taken into account when generating surface status fo path with cycleway and footway split`() {
        // https://www.openstreetmap.org/way/925626513 version 4
        val tags = mapOf(
            "bicycle" to "designated",
            "cycleway:surface" to "paving_stones",
            "foot" to "designated",
            "footway:surface" to "asphalt",
            "highway" to "path",
            "lit" to "yes",
            "oneway:bicycle" to "yes",
            "path" to "sidewalk",
            "segregated" to "yes",
            "surface" to "paving_stones",
            "surface:note" to "Rad Pflastersteine Fußgänger Asphalt",
        )
        val status = createSurfaceStatus(tags)
        assertTrue(status is ParsedCyclewayFootwaySurfacesWithNote)
        if (status is ParsedCyclewayFootwaySurfacesWithNote) {
            assertEquals("Rad Pflastersteine Fußgänger Asphalt", status.note)
        }
    }

    @Test
    fun `find shared surface for two paved`() {
        assertEquals("paved", commonSurfaceDescription("asphalt", "paving_stones"))
        assertTrue(commonSurfaceObject("asphalt", "paving_stones")!!.osmValue == "paved")
    }

    @Test
    fun `find shared surface for two unpaved`() {
        assertEquals("unpaved", commonSurfaceDescription("gravel", "sand"))
        assertTrue(commonSurfaceObject("gravel", "sand")!!.osmValue == "unpaved")
    }

    @Test
    fun `find shared surface for two identical`() {
        assertEquals("sand", commonSurfaceDescription("sand", "sand"))
        assertEquals(Surface.SAND, commonSurfaceObject("sand", "sand"))
    }

    @Test
    fun `find shared surface for two without shared surface`() {
        assertEquals(null, commonSurfaceDescription("asphalt", "sand"))
        assertEquals(null, commonSurfaceObject("asphalt", "sand"))
    }

    @Test
    fun `converting tags to enum works`() {
        assertEquals(Surface.ASPHALT, surfaceTextValueToSurfaceEnum("asphalt"))
        assertEquals(Surface.SAND, surfaceTextValueToSurfaceEnum("sand"))
    }

    @Test
    fun `converting tags to enum supports synonyms`() {
        assertEquals(Surface.EARTH, surfaceTextValueToSurfaceEnum("earth"))
        assertEquals(Surface.SOIL, surfaceTextValueToSurfaceEnum("soil"))
    }

    @Test
    fun `converting tags to enum return null for ones that should be retagged`() {
        assertEquals(null, surfaceTextValueToSurfaceEnum("cement"))
        assertEquals(null, surfaceTextValueToSurfaceEnum("cobblestone"))
    }

    @Test
    fun `check date is among keys removed on surface change`() {
        assertTrue("check_date:cycleway:surface" in keysToBeRemovedOnSurfaceChange("cycleway:"))
    }
}

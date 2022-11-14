package de.westnordost.streetcomplete.osm.surface

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryDelete
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryModify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.assertj.core.api.Assertions.assertThat

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
        assertTrue(createMainSurfaceStatus(mapOf("surface" to "asphalt")) is SingleSurface)
    }

    @Test
    fun `specific surface generates specific surface status for paths`() {
        assertTrue(createSurfaceStatus(mapOf("surface" to "asphalt")) is SingleSurface)
    }

    @Test
    fun `note tag results in a diferent status for roads`() {
        assertTrue(createMainSurfaceStatus(mapOf("surface" to "asphalt", "surface:note" to "useful info")) is SingleSurfaceWithNote)
    }

    @Test
    fun `note tag results in a diferent status for paths`() {
        assertTrue(createSurfaceStatus(mapOf("surface" to "asphalt")) is SingleSurface)
    }

    @Test
    fun `find shared surface for two paved`() {
        assertEquals(commonSurfaceDescription("asphalt", "paving_stones"), "paved")
        assertTrue(commonSurfaceObject("asphalt", "paving_stones")!!.osmValue == "paved")
    }

    @Test
    fun `find shared surface for two unpaved`() {
        assertEquals(commonSurfaceDescription("gravel", "sand"), "unpaved")
        assertTrue(commonSurfaceObject("gravel", "sand")!!.osmValue == "unpaved")
    }

    @Test
    fun `find shared surface for two identical`() {
        assertEquals(commonSurfaceDescription("sand", "sand"), "sand")
        assertEquals(commonSurfaceObject("sand", "sand"), Surface.SAND)
    }

    @Test
    fun `find shared surface for two without shared surface`() {
        assertEquals(commonSurfaceDescription("asphalt", "sand"), null)
        assertEquals(commonSurfaceObject("asphalt", "sand"), null)
    }
}

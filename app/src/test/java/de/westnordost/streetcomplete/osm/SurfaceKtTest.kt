package de.westnordost.streetcomplete.osm

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
}

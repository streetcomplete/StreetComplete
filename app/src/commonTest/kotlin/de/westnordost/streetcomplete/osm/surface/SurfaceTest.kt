package de.westnordost.streetcomplete.osm.surface

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SurfaceTest {

    @Test
    fun `getSelectableValuesForWays includes laterite for valid country codes`() {
        val surfaces = Surface.getSelectableValuesForWays("IN")
        assertTrue(surfaces.contains(Surface.LATERITE), "LATERITE should be present for India (IN)")
    }

    @Test
    fun `getSelectableValuesForWays excludes laterite for non-laterite country codes`() {
        val surfaces = Surface.getSelectableValuesForWays("DE")
        assertFalse(surfaces.contains(Surface.LATERITE), "LATERITE should NOT be present for Germany (DE)")
    }

    @Test
    fun `getSelectableValuesForWays extracts country code from subdivision code`() {
        val surfaces = Surface.getSelectableValuesForWays("IN-MH")
        assertTrue(surfaces.contains(Surface.LATERITE), "LATERITE should be present for Maharashtra, India (IN-MH)")
    }

    @Test
    fun `getSelectableValuesForWays excludes laterite when country code is null`() {
        val surfaces = Surface.getSelectableValuesForWays(null)
        assertFalse(surfaces.contains(Surface.LATERITE), "LATERITE should NOT be present for null country code")
    }
}

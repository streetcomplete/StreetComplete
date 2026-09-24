package de.westnordost.streetcomplete.data.location

import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds
import kotlinx.datetime.Month

class CurrentSeasonTest {

    @Test
    fun `isSouthernHemisphere returns false for positive latitude`() {
        val location = Location(LatLon(48.0, 11.0), 10f, 0.seconds)
        assertEquals(false, CurrentSeason.isSouthernHemisphere(location))
    }

    @Test
    fun `isSouthernHemisphere returns true for negative latitude`() {
        val location = Location(LatLon(-23.0, 133.0), 10f, 0.seconds)
        assertEquals(true, CurrentSeason.isSouthernHemisphere(location))
    }

    @Test
    fun `season calculation is correct`() {
        assertEquals("winter", CurrentSeason.updateSeason(false, Month.DECEMBER))
        assertEquals("spring", CurrentSeason.updateSeason(false, Month.MARCH))
        assertEquals("winter", CurrentSeason.updateSeason(true, Month.JULY))
        assertEquals("summer", CurrentSeason.updateSeason(true, Month.DECEMBER))
    }

    @Test
    fun `seasons in NORTH hemisphere`() {
        val location = Location(LatLon(48.0, 11.0), 10f, 0.seconds)
        CurrentSeason.addRecentLocation(location)
        val season = CurrentSeason.getCurrentSeason
        assertTrue(season in listOf("spring", "summer", "autumn", "winter"))
    }

    @Test
    fun `seasons depend on hemisphere`() {
        val locationInNorthernHemisphere = Location(LatLon(48.0, 11.0), 10f, 0.seconds)
        val locationInSouthernHemisphere = Location(LatLon(-48.0, -11.0), 10f, 0.seconds)
        CurrentSeason.addRecentLocation(locationInNorthernHemisphere)
        val seasonNorthernHemisphere = CurrentSeason.getCurrentSeason
        CurrentSeason.addRecentLocation(locationInSouthernHemisphere)
        val seasonSouthernHemisphere = CurrentSeason.getCurrentSeason
        assertNotEquals(seasonNorthernHemisphere, seasonSouthernHemisphere)
    }
}

package de.westnordost.streetcomplete.data.location

import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

class CurrentHemisphereTest {

    @Test
    fun `addRecentLocation sets NORTH for positive latitude`() {
        val location = Location(LatLon(48.0, 11.0), 10f, 0.seconds)
        CurrentHemisphere.addRecentLocation(location)
        assertEquals(CurrentHemisphere.Hemisphere.NORTH, CurrentHemisphere.hemisphere)
    }

    @Test
    fun `addRecentLocation sets SOUTH for negative latitude`() {
        val location = Location(LatLon(-23.0, 133.0), 10f, 0.seconds)
        CurrentHemisphere.addRecentLocation(location)
        assertEquals(CurrentHemisphere.Hemisphere.SOUTH, CurrentHemisphere.hemisphere)
    }

    @Test
    fun `seasons in NORTH hemisphere`() {
        val location = Location(LatLon(48.0, 11.0), 10f, 0.seconds)
        CurrentHemisphere.addRecentLocation(location)

        // The currentSeason property uses the current date, so we can't easily test all months
        // without mocking the clock.
        val season = CurrentHemisphere.currentSeason
        assertTrue(season in listOf("spring", "summer", "autumn", "winter"))
    }

    @Test
    fun `seasons depend on hemisphere`() {
        val locationInNorthernHemisphere = Location(LatLon(48.0, 11.0), 10f, 0.seconds)
        val locationInSouthernHemisphere = Location(LatLon(-48.0, -11.0), 10f, 0.seconds)
        CurrentHemisphere.addRecentLocation(locationInNorthernHemisphere)
        val seasonNorthernHemisphere = CurrentHemisphere.currentSeason
        CurrentHemisphere.addRecentLocation(locationInSouthernHemisphere)
        val seasonSouthernHemisphere = CurrentHemisphere.currentSeason
        assertNotEquals(seasonNorthernHemisphere, seasonSouthernHemisphere)
    }
}

package de.westnordost.streetcomplete.data.location

import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import dev.mokkery.context.MokkeryContext.Empty.get
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import kotlin.time.Instant
import kotlinx.datetime.Month

class CurrentSeasonTest {

    var currentSeason = CurrentSeason()

    @Test
    fun `isSouthernHemisphere returns false for positive latitude`() {
        val location = LatLon(48.0, 11.0)
        assertEquals(false, currentSeason.isSouthernHemisphere(location))
    }

    @Test
    fun `isSouthernHemisphere returns true for negative latitude`() {
        val location = LatLon(-23.0, 133.0)
        assertEquals(true, currentSeason.isSouthernHemisphere(location))
    }

    @Test
    fun `seasons depend on hemisphere`() {
       val seasonNorthernHemisphere = currentSeason.getSeason(true)
        val seasonSouthernHemisphere = currentSeason.getSeason(false)
        assertNotEquals(seasonNorthernHemisphere, seasonSouthernHemisphere)
    }
}

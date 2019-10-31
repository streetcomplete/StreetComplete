package de.westnordost.streetcomplete.ktx

import de.westnordost.osmapi.map.data.OsmLatLon
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LatLonTest {
    @Test fun `different LatLon is different`() {
        assertFalse(OsmLatLon(0.0000000, 0.0000000).equalsInOsm(OsmLatLon(0.0000001, 0.0000000)))
        assertFalse(OsmLatLon(0.0000000, 0.0000000).equalsInOsm(OsmLatLon(0.0000000, 0.0000001)))
    }

    @Test fun `same LatLon is same`() {
        assertTrue(OsmLatLon(0.00000000, 0.00000000).equalsInOsm(OsmLatLon(0.00000001, 0.00000000)))
        assertTrue(OsmLatLon(0.00000000, 0.00000000).equalsInOsm(OsmLatLon(0.00000000, 0.00000001)))
    }
}

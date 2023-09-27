package de.westnordost.streetcomplete.util.ktx

import de.westnordost.streetcomplete.testutils.p
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LatLonTest {
    @Test fun `different LatLon is different`() {
        assertFalse(p(0.0000000, 0.0000000).equalsInOsm(p(0.0000001, 0.0000000)))
        assertFalse(p(0.0000000, 0.0000000).equalsInOsm(p(0.0000000, 0.0000001)))
    }

    @Test fun `same LatLon is same`() {
        assertTrue(p(0.00000000, 0.00000000).equalsInOsm(p(0.00000001, 0.00000000)))
        assertTrue(p(0.00000000, 0.00000000).equalsInOsm(p(0.00000000, 0.00000001)))
    }
}

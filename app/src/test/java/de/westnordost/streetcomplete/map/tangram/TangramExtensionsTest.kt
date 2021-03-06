package de.westnordost.streetcomplete.map.tangram

import org.junit.Test

import de.westnordost.streetcomplete.testutils.p

import org.junit.Assert.*

class TangramExtensionsTest {

    @Test fun `convert single`() {
        val pos = p(5.0, 10.0)
        val pos2 = pos.toLngLat().toLatLon()

        assertEquals(pos, pos2)
    }
}

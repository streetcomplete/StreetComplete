package de.westnordost.streetcomplete.screens.main.map.tangram

import de.westnordost.streetcomplete.testutils.p
import org.junit.Assert.assertEquals
import org.junit.Test

class TangramExtensionsTest {

    @Test fun `convert single`() {
        val pos = p(5.0, 10.0)
        val pos2 = pos.toLngLat().toLatLon()

        assertEquals(pos, pos2)
    }
}

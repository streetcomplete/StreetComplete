package de.westnordost.streetcomplete.screens.main.map.tangram

import de.westnordost.streetcomplete.data.maptiles.toLatLng
import de.westnordost.streetcomplete.testutils.p
import kotlin.test.Test
import kotlin.test.assertEquals

class TangramExtensionsTest {

    @Test fun `convert single`() {
        val pos = p(5.0, 10.0)
        val pos2 = pos.toLatLng().toLatLon()

        assertEquals(pos, pos2)
    }
}

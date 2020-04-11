package de.westnordost.streetcomplete.map.tangram

import org.junit.Test

import de.westnordost.osmapi.map.data.OsmLatLon

import org.junit.Assert.*

class TangramExtensionsTest {

    @Test fun `convert single`() {
        val pos = OsmLatLon(5.0, 10.0)
        val pos2 = pos.toLngLat().toLatLon()

        assertEquals(pos, pos2)
    }
}

package de.westnordost.streetcomplete.tangram

import org.junit.Test

import de.westnordost.osmapi.map.data.OsmLatLon
import de.westnordost.streetcomplete.tangram.TangramConst.toLatLon
import de.westnordost.streetcomplete.tangram.TangramConst.toLngLat

import org.junit.Assert.*

class TangramConstTest {

    @Test fun `convert single`() {
        val pos = OsmLatLon(5.0, 10.0)
        val pos2 = toLatLon(toLngLat(pos))

        assertEquals(pos, pos2)
    }

    @Test fun `convert lists`() {
        val positionLists = listOf(
            listOf(),
            listOf(OsmLatLon(1.0, 2.0), OsmLatLon(3.0, 4.0)),
            listOf(OsmLatLon(5.0, 6.0))
        )
        val positionLists2 = toLatLon(toLngLat(positionLists))

        assertEquals(positionLists, positionLists2)
    }
}

package de.westnordost.streetcomplete.osm.sidewalk

import de.westnordost.streetcomplete.osm.Sides
import kotlin.test.Test
import kotlin.test.assertEquals

class SidewalkKtTest {
    @Test fun validOrNullValues() {
        assertEquals(
            Sides<Sidewalk>(null, null),
            Sides<Sidewalk>(Sidewalk.INVALID, Sidewalk.INVALID).validOrNullValues()
        )
        assertEquals(
            Sides<Sidewalk>(Sidewalk.NO, null),
            Sides<Sidewalk>(Sidewalk.NO, Sidewalk.INVALID).validOrNullValues()
        )
        assertEquals(
            Sides<Sidewalk>(null, Sidewalk.NO),
            Sides<Sidewalk>(Sidewalk.INVALID, Sidewalk.NO).validOrNullValues()
        )
    }
}

package de.westnordost.streetcomplete.osm.sidewalk

import de.westnordost.streetcomplete.osm.Sides
import kotlin.test.Test
import kotlin.test.assertEquals

class SidewalkKtTest {
    @Test fun validOrNullValues() {
        assertEquals(
            Sides<Sidewalk>(null, null),
            Sides(Sidewalk.INVALID, Sidewalk.INVALID).validOrNullValues()
        )
        assertEquals(
            Sides(Sidewalk.NO, null),
            Sides(Sidewalk.NO, Sidewalk.INVALID).validOrNullValues()
        )
        assertEquals(
            Sides(null, Sidewalk.NO),
            Sides(Sidewalk.INVALID, Sidewalk.NO).validOrNullValues()
        )
    }
}

package de.westnordost.streetcomplete.osm.sidewalk

import kotlin.test.Test
import kotlin.test.assertEquals

class SidewalkKtTest {
    @Test fun validOrNullValues() {
        assertEquals(
            LeftAndRightSidewalk(null, null),
            LeftAndRightSidewalk(Sidewalk.INVALID, Sidewalk.INVALID).validOrNullValues()
        )
        assertEquals(
            LeftAndRightSidewalk(Sidewalk.NO, null),
            LeftAndRightSidewalk(Sidewalk.NO, Sidewalk.INVALID).validOrNullValues()
        )
        assertEquals(
            LeftAndRightSidewalk(null, Sidewalk.NO),
            LeftAndRightSidewalk(Sidewalk.INVALID, Sidewalk.NO).validOrNullValues()
        )
    }
}

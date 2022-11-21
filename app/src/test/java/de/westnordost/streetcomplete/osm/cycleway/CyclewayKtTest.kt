package de.westnordost.streetcomplete.osm.cycleway

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CyclewayKtTest {

    @Test fun `was no oneway for cyclists but now it is`() {
        assertTrue(LeftAndRightCycleway(Cycleway.NONE, Cycleway.NONE)
            .wasNoOnewayForCyclistsButNowItIs(mapOf(
                "oneway" to "yes",
                "oneway:bicycle" to "no",
                "cycleway:both" to "no"
            ), false)
        )
    }

    @Test fun `was no oneway for cyclists and still is`() {
        assertFalse(LeftAndRightCycleway(Cycleway.NONE_NO_ONEWAY, Cycleway.NONE)
            .wasNoOnewayForCyclistsButNowItIs(mapOf(
                "oneway" to "yes",
                "oneway:bicycle" to "no",
                "cycleway:both" to "no"
            ), false)
        )
    }

    @Test fun `was oneway for cyclists and still is`() {
        assertFalse(LeftAndRightCycleway(Cycleway.NONE, Cycleway.NONE)
            .wasNoOnewayForCyclistsButNowItIs(mapOf(
                "oneway" to "yes",
                "cycleway:both" to "no"
            ), false)
        )
    }

    @Test fun `was oneway for cyclists and is no anymore`() {
        assertFalse(LeftAndRightCycleway(Cycleway.NONE_NO_ONEWAY, Cycleway.NONE)
            .wasNoOnewayForCyclistsButNowItIs(mapOf(
                "oneway" to "yes",
                "cycleway:both" to "no"
            ), false)
        )
    }
}

package de.westnordost.streetcomplete.osm

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OnewayKtTest {

    private val oppositeCyclewayValues = listOf("opposite", "opposite_track", "opposite_lane")

    @Test fun `not oneway for cyclists (old tagging)`() {
        for (isLeftHandTraffic in listOf(true, false)) {
            for (cycleway in oppositeCyclewayValues) {
                assertTrue(isNotOnewayForCyclists(mapOf("cycleway" to cycleway), isLeftHandTraffic))
            }
        }
        assertFalse(isNotOnewayForCyclists(mapOf("cycleway" to "track"), false))
    }

    @Test fun `not oneway for cyclists (old per-side tagging)`() {
        for (cycleway in oppositeCyclewayValues) {
            assertTrue(isNotOnewayForCyclists(mapOf("cycleway:left" to cycleway), false))
            assertTrue(isNotOnewayForCyclists(mapOf("cycleway:right" to cycleway), true))
            assertTrue(isNotOnewayForCyclists(mapOf("cycleway:left" to cycleway, "oneway" to "-1"), true))
            assertTrue(isNotOnewayForCyclists(mapOf("cycleway:right" to cycleway, "oneway" to "-1"), false))

            assertFalse(isNotOnewayForCyclists(mapOf("cycleway:left" to cycleway), true))
            assertFalse(isNotOnewayForCyclists(mapOf("cycleway:right" to cycleway), false))
            assertFalse(isNotOnewayForCyclists(mapOf("cycleway:left" to cycleway, "oneway" to "-1"), false))
            assertFalse(isNotOnewayForCyclists(mapOf("cycleway:right" to cycleway, "oneway" to "-1"), true))
        }
    }

    @Test fun `not oneway for cyclists (modern tagging)`() {
        for (isLeftHandTraffic in listOf(true, false)) {
            assertFalse(isNotOnewayForCyclists(mapOf("oneway:bicycle" to "yes"), isLeftHandTraffic))
            assertTrue(isNotOnewayForCyclists(mapOf("oneway:bicycle" to "no"), isLeftHandTraffic))
        }
    }

    @Test fun `is in contraflow of oneway`() {
        val oneway = mapOf("oneway" to "yes")
        val reverseOneway = mapOf("oneway" to "-1")

        // not in oneway
        assertFalse(isInContraflowOfOneway(false, mapOf(), false))
        assertFalse(isInContraflowOfOneway(false, mapOf(), true))
        assertFalse(isInContraflowOfOneway(true, mapOf(), false))
        assertFalse(isInContraflowOfOneway(true, mapOf(), true))

        // sides of oneway
        assertTrue(isInContraflowOfOneway(false, oneway, false))
        assertFalse(isInContraflowOfOneway(true, oneway, false))

        // sides of reverse oneway
        assertFalse(isInContraflowOfOneway(false, reverseOneway, false))
        assertTrue(isInContraflowOfOneway(true, reverseOneway, false))

        // sides of reverse oneway in left hand traffic countries
        assertTrue(isInContraflowOfOneway(false, reverseOneway, true))
        assertFalse(isInContraflowOfOneway(true, reverseOneway, true))

        // sides of oneway in left hand traffic countries
        assertFalse(isInContraflowOfOneway(false, oneway, true))
        assertTrue(isInContraflowOfOneway(true, oneway, true))
    }
}

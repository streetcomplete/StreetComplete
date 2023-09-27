package de.westnordost.streetcomplete.osm

import de.westnordost.streetcomplete.osm.cycleway.Direction
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

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
        assertFalse(isInContraflowOfOneway(mapOf(), Direction.FORWARD))
        assertFalse(isInContraflowOfOneway(mapOf(), Direction.BACKWARD))

        // forward oneway
        assertTrue(isInContraflowOfOneway(oneway, Direction.BACKWARD))
        assertFalse(isInContraflowOfOneway(oneway, Direction.FORWARD))

        // reverse oneway
        assertTrue(isInContraflowOfOneway(reverseOneway, Direction.FORWARD))
        assertFalse(isInContraflowOfOneway(reverseOneway, Direction.BACKWARD))
    }
}

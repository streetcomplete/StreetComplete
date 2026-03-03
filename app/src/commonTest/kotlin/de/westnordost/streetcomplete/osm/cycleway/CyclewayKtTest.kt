package de.westnordost.streetcomplete.osm.cycleway

import de.westnordost.streetcomplete.osm.Sides
import de.westnordost.streetcomplete.osm.cycleway.Cycleway.*
import de.westnordost.streetcomplete.osm.oneway.Direction.*
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CyclewayKtTest {

    @Test fun `was no oneway for cyclists but now it is`() {
        assertTrue(
            Sides(
                CyclewayAndDirection(NONE, BACKWARD),
                CyclewayAndDirection(NONE, FORWARD)
            )
            .wasNoOnewayForCyclistsButNowItIs(mapOf(
                "oneway" to "yes",
                "oneway:bicycle" to "no",
                "cycleway:both" to "no"
            ), false)
        )
    }

    @Test fun `was no oneway for cyclists and still is`() {
        assertFalse(
            Sides(
                CyclewayAndDirection(NONE_NO_ONEWAY, BACKWARD),
                CyclewayAndDirection(NONE, FORWARD)
            )
            .wasNoOnewayForCyclistsButNowItIs(mapOf(
                "oneway" to "yes",
                "oneway:bicycle" to "no",
                "cycleway:both" to "no"
            ), false)
        )
    }

    @Test fun `was oneway for cyclists and still is`() {
        assertFalse(
            Sides(
                CyclewayAndDirection(NONE, BACKWARD),
                CyclewayAndDirection(NONE, FORWARD)
            )
            .wasNoOnewayForCyclistsButNowItIs(mapOf(
                "oneway" to "yes",
                "cycleway:both" to "no"
            ), false)
        )
    }

    @Test fun `was oneway for cyclists and is not anymore`() {
        assertFalse(
            Sides(
                CyclewayAndDirection(NONE_NO_ONEWAY, BACKWARD),
                CyclewayAndDirection(NONE, FORWARD)
            )
            .wasNoOnewayForCyclistsButNowItIs(mapOf(
                "oneway" to "yes",
                "cycleway:both" to "no"
            ), false)
        )
    }

    @Test fun `not a oneway for cyclists is still not a oneway for cyclists when contra-flow side is not defined`() {
        val noOnewayForCyclists = mapOf("oneway" to "yes", "oneway:bicycle" to "no")
        val noOnewayForCyclistsReverse = mapOf("oneway" to "-1", "oneway:bicycle" to "no")
        val forwardTrack = CyclewayAndDirection(TRACK, FORWARD)
        val backwardTrack = CyclewayAndDirection(TRACK, BACKWARD)

        assertNull(
            Sides<CyclewayAndDirection>(null, null).isNotOnewayForCyclistsNow(noOnewayForCyclists)
        )
        assertNull(
            Sides(null, forwardTrack)
                .isNotOnewayForCyclistsNow(noOnewayForCyclists)
        )
        assertNull(
            Sides(backwardTrack, null)
                .isNotOnewayForCyclistsNow(noOnewayForCyclistsReverse)
        )

        assertNull(
            Sides<CyclewayAndDirection>(null, null).isNotOnewayForCyclistsNow(noOnewayForCyclists)
        )
        assertNull(
            Sides(forwardTrack, null)
                .isNotOnewayForCyclistsNow(noOnewayForCyclists)
        )
        assertNull(
            Sides(null, backwardTrack)
                .isNotOnewayForCyclistsNow(noOnewayForCyclistsReverse)
        )
    }

    @Test fun `not a oneway is no oneway for cyclists`() {
        assertTrue(
            Sides(
                CyclewayAndDirection(NONE, BACKWARD),
                CyclewayAndDirection(NONE, FORWARD)
            ).isNotOnewayForCyclistsNow(mapOf())!!
        )
    }

    @Test fun `forward oneway is oneway for cyclists`() {
        assertFalse(
            Sides(
                CyclewayAndDirection(NONE, BACKWARD),
                CyclewayAndDirection(NONE, FORWARD)
            ).isNotOnewayForCyclistsNow(mapOf("oneway" to "yes"))!!
        )
    }

    @Test fun `reverse oneway is oneway for cyclists`() {
        assertFalse(
            Sides(
                CyclewayAndDirection(NONE, BACKWARD),
                CyclewayAndDirection(NONE, FORWARD)
            ).isNotOnewayForCyclistsNow(mapOf("oneway" to "-1"))!!
        )
    }

    @Test fun `oneway is no oneway for cyclists when there is a dual track`() {
        assertTrue(
            Sides(
                CyclewayAndDirection(NONE, BACKWARD),
                CyclewayAndDirection(TRACK, BOTH)
            ).isNotOnewayForCyclistsNow(mapOf("oneway" to "yes"))!!
        )
        assertTrue(
            Sides(
                CyclewayAndDirection(TRACK, BOTH),
                CyclewayAndDirection(NONE, FORWARD)
            ).isNotOnewayForCyclistsNow(mapOf("oneway" to "-1"))!!
        )
    }

    @Test fun `oneway is no oneway for cyclists when any cycleway goes in contra-flow direction`() {
        val forwardTrack = CyclewayAndDirection(TRACK, FORWARD)
        val backwardTrack = CyclewayAndDirection(TRACK, BACKWARD)
        val none = CyclewayAndDirection(NONE, BOTH)

        assertTrue(
            Sides(backwardTrack, none).isNotOnewayForCyclistsNow(mapOf("oneway" to "yes"))!!
        )
        assertTrue(
            Sides(none, backwardTrack).isNotOnewayForCyclistsNow(mapOf("oneway" to "yes"))!!
        )
        assertTrue(
            Sides(forwardTrack, none).isNotOnewayForCyclistsNow(mapOf("oneway" to "-1"))!!
        )
        assertTrue(
            Sides(none, forwardTrack).isNotOnewayForCyclistsNow(mapOf("oneway" to "-1"))!!
        )
    }

    @Test fun `oneway is a oneway for cyclists when separately mapped cycleway goes in contra-flow direction`() {
        // ... because direction of separate cycleway and no cycleway is ignored
        val separate = CyclewayAndDirection(SEPARATE, BOTH)
        val none = CyclewayAndDirection(NONE, BOTH)

        for (oneway in listOf(mapOf("oneway" to "yes"), mapOf("oneway" to "-1"))) {
            assertFalse(
                Sides(separate, none).isNotOnewayForCyclistsNow(oneway)!!
            )
            assertFalse(
                Sides(none, separate).isNotOnewayForCyclistsNow(oneway)!!
            )
        }
    }

    @Test fun `oneway is oneway for cyclists when no cycleway goes in contra-flow direction`() {
        val forwardTrack = CyclewayAndDirection(TRACK, FORWARD)
        val backwardTrack = CyclewayAndDirection(TRACK, BACKWARD)
        // direction does not matter for NONE, best way to test that is to set BOTH
        val none = CyclewayAndDirection(NONE, BOTH)

        assertFalse(
            Sides(forwardTrack, none).isNotOnewayForCyclistsNow(mapOf("oneway" to "yes"))!!
        )
        assertFalse(
            Sides(none, forwardTrack).isNotOnewayForCyclistsNow(mapOf("oneway" to "yes"))!!
        )
        assertFalse(
            Sides(backwardTrack, none).isNotOnewayForCyclistsNow(mapOf("oneway" to "-1"))!!
        )
        assertFalse(
            Sides(none, backwardTrack).isNotOnewayForCyclistsNow(mapOf("oneway" to "-1"))!!
        )
    }
}

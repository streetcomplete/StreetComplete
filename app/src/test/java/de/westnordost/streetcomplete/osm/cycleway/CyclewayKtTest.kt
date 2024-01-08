package de.westnordost.streetcomplete.osm.cycleway

import de.westnordost.streetcomplete.osm.cycleway.Cycleway.*
import de.westnordost.streetcomplete.osm.cycleway.Direction.*
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CyclewayKtTest {

    @Test fun `was no oneway for cyclists but now it is`() {
        assertTrue(
            LeftAndRightCycleway(
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
            LeftAndRightCycleway(
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
            LeftAndRightCycleway(
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
            LeftAndRightCycleway(
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
            LeftAndRightCycleway(null, null)
                .isNotOnewayForCyclistsNow(noOnewayForCyclists)
        )
        assertNull(
            LeftAndRightCycleway(null, forwardTrack)
                .isNotOnewayForCyclistsNow(noOnewayForCyclists)
        )
        assertNull(
            LeftAndRightCycleway(backwardTrack, null)
                .isNotOnewayForCyclistsNow(noOnewayForCyclistsReverse)
        )

        assertNull(
            LeftAndRightCycleway(null, null)
                .isNotOnewayForCyclistsNow(noOnewayForCyclists)
        )
        assertNull(
            LeftAndRightCycleway(forwardTrack, null)
                .isNotOnewayForCyclistsNow(noOnewayForCyclists)
        )
        assertNull(
            LeftAndRightCycleway(null, backwardTrack)
                .isNotOnewayForCyclistsNow(noOnewayForCyclistsReverse)
        )
    }

    @Test fun `not a oneway is no oneway for cyclists`() {
        assertTrue(
            LeftAndRightCycleway(
                CyclewayAndDirection(NONE, BACKWARD),
                CyclewayAndDirection(NONE, FORWARD)
            ).isNotOnewayForCyclistsNow(mapOf())!!
        )
    }

    @Test fun `forward oneway is oneway for cyclists`() {
        assertFalse(
            LeftAndRightCycleway(
                CyclewayAndDirection(NONE, BACKWARD),
                CyclewayAndDirection(NONE, FORWARD)
            ).isNotOnewayForCyclistsNow(mapOf("oneway" to "yes"))!!
        )
    }

    @Test fun `reverse oneway is oneway for cyclists`() {
        assertFalse(
            LeftAndRightCycleway(
                CyclewayAndDirection(NONE, BACKWARD),
                CyclewayAndDirection(NONE, FORWARD)
            ).isNotOnewayForCyclistsNow(mapOf("oneway" to "-1"))!!
        )
    }

    @Test fun `oneway is no oneway for cyclists when there is a dual track`() {
        assertTrue(
            LeftAndRightCycleway(
                CyclewayAndDirection(NONE, BACKWARD),
                CyclewayAndDirection(TRACK, BOTH)
            ).isNotOnewayForCyclistsNow(mapOf("oneway" to "yes"))!!
        )
        assertTrue(
            LeftAndRightCycleway(
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
            LeftAndRightCycleway(backwardTrack, none)
                .isNotOnewayForCyclistsNow(mapOf("oneway" to "yes"))!!
        )
        assertTrue(
            LeftAndRightCycleway(none, backwardTrack)
                .isNotOnewayForCyclistsNow(mapOf("oneway" to "yes"))!!
        )
        assertTrue(
            LeftAndRightCycleway(forwardTrack, none)
                .isNotOnewayForCyclistsNow(mapOf("oneway" to "-1"))!!
        )
        assertTrue(
            LeftAndRightCycleway(none, forwardTrack)
                .isNotOnewayForCyclistsNow(mapOf("oneway" to "-1"))!!
        )
    }

    @Test fun `oneway is a oneway for cyclists when separately mapped cycleway goes in contra-flow direction`() {
        // ... because direction of separate cycleway and no cycleway is ignored
        val separate = CyclewayAndDirection(SEPARATE, BOTH)
        val none = CyclewayAndDirection(NONE, BOTH)

        for (oneway in listOf(mapOf("oneway" to "yes"), mapOf("oneway" to "-1"))) {
            assertFalse(
                LeftAndRightCycleway(separate, none)
                    .isNotOnewayForCyclistsNow(oneway)!!
            )
            assertFalse(
                LeftAndRightCycleway(none, separate)
                    .isNotOnewayForCyclistsNow(oneway)!!
            )
        }
    }

    @Test fun `oneway is oneway for cyclists when no cycleway goes in contra-flow direction`() {
        val forwardTrack = CyclewayAndDirection(TRACK, FORWARD)
        val backwardTrack = CyclewayAndDirection(TRACK, BACKWARD)
        // direction does not matter for NONE, best way to test that is to set BOTH
        val none = CyclewayAndDirection(NONE, BOTH)

        assertFalse(
            LeftAndRightCycleway(forwardTrack, none)
                .isNotOnewayForCyclistsNow(mapOf("oneway" to "yes"))!!
        )
        assertFalse(
            LeftAndRightCycleway(none, forwardTrack)
                .isNotOnewayForCyclistsNow(mapOf("oneway" to "yes"))!!
        )
        assertFalse(
            LeftAndRightCycleway(backwardTrack, none)
                .isNotOnewayForCyclistsNow(mapOf("oneway" to "-1"))!!
        )
        assertFalse(
            LeftAndRightCycleway(none, backwardTrack)
                .isNotOnewayForCyclistsNow(mapOf("oneway" to "-1"))!!
        )
    }
}

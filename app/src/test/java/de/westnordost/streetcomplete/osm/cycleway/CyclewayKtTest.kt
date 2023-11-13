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
                .isNotOnewayForCyclistsNow(noOnewayForCyclists, false)
        )
        assertNull(
            LeftAndRightCycleway(null, forwardTrack)
                .isNotOnewayForCyclistsNow(noOnewayForCyclists, false)
        )
        assertNull(
            LeftAndRightCycleway(backwardTrack, null)
                .isNotOnewayForCyclistsNow(noOnewayForCyclistsReverse, false)
        )

        assertNull(
            LeftAndRightCycleway(null, null)
                .isNotOnewayForCyclistsNow(noOnewayForCyclists, true)
        )
        assertNull(
            LeftAndRightCycleway(forwardTrack, null)
                .isNotOnewayForCyclistsNow(noOnewayForCyclists, true)
        )
        assertNull(
            LeftAndRightCycleway(null, backwardTrack)
                .isNotOnewayForCyclistsNow(noOnewayForCyclistsReverse, true)
        )
    }

    @Test fun `not a oneway is no oneway for cyclists`() {
        assertTrue(
            LeftAndRightCycleway(
                CyclewayAndDirection(NONE, BACKWARD),
                CyclewayAndDirection(NONE, FORWARD)
            ).isNotOnewayForCyclistsNow(mapOf(), false)!!
        )
    }

    @Test fun `forward oneway is oneway for cyclists`() {
        assertFalse(
            LeftAndRightCycleway(
                CyclewayAndDirection(NONE, BACKWARD),
                CyclewayAndDirection(NONE, FORWARD)
            ).isNotOnewayForCyclistsNow(mapOf("oneway" to "yes"), false)!!
        )
    }

    @Test fun `reverse oneway is oneway for cyclists`() {
        assertFalse(
            LeftAndRightCycleway(
                CyclewayAndDirection(NONE, BACKWARD),
                CyclewayAndDirection(NONE, FORWARD)
            ).isNotOnewayForCyclistsNow(mapOf("oneway" to "-1"), false)!!
        )
    }

    @Test fun `oneway is no oneway for cyclists when there is a dual track`() {
        assertTrue(
            LeftAndRightCycleway(
                CyclewayAndDirection(NONE, BACKWARD),
                CyclewayAndDirection(TRACK, BOTH)
            ).isNotOnewayForCyclistsNow(mapOf("oneway" to "yes"), false)!!
        )
        assertTrue(
            LeftAndRightCycleway(
                CyclewayAndDirection(TRACK, BOTH),
                CyclewayAndDirection(NONE, FORWARD)
            ).isNotOnewayForCyclistsNow(mapOf("oneway" to "-1"), false)!!
        )
    }

    @Test fun `oneway is no oneway for cyclists when any cycleway goes in contra-flow direction`() {
        val forwardTrack = CyclewayAndDirection(TRACK, FORWARD)
        val backwardTrack = CyclewayAndDirection(TRACK, BACKWARD)

        assertTrue(
            LeftAndRightCycleway(backwardTrack, null)
                .isNotOnewayForCyclistsNow(mapOf("oneway" to "yes"), false)!!
        )
        assertTrue(
            LeftAndRightCycleway(null, backwardTrack)
                .isNotOnewayForCyclistsNow(mapOf("oneway" to "yes"), false)!!
        )
        assertTrue(
            LeftAndRightCycleway(forwardTrack, null)
                .isNotOnewayForCyclistsNow(mapOf("oneway" to "-1"), false)!!
        )
        assertTrue(
            LeftAndRightCycleway(null, forwardTrack)
                .isNotOnewayForCyclistsNow(mapOf("oneway" to "-1"), false)!!
        )
    }

    @Test fun `oneway is a oneway for cyclists when separately mapped cycleway goes in contra-flow direction`() {
        // ... because direction of separate cycleway and no cycleway is ignored
        val separate = CyclewayAndDirection(SEPARATE, BOTH)
        val none = CyclewayAndDirection(NONE, BOTH)

        for (isLeftHandTraffic in listOf(false, true)) {
            for (oneway in listOf(mapOf("oneway" to "yes"), mapOf("oneway" to "-1"))) {
                assertFalse(
                    LeftAndRightCycleway(separate, none)
                        .isNotOnewayForCyclistsNow(oneway, isLeftHandTraffic)!!
                )
                assertFalse(
                    LeftAndRightCycleway(none, separate)
                        .isNotOnewayForCyclistsNow(oneway, isLeftHandTraffic)!!
                )
            }
        }
    }

    @Test fun `oneway is oneway for cyclists when no cycleway goes in contra-flow direction`() {
        val forwardTrack = CyclewayAndDirection(TRACK, FORWARD)
        val backwardTrack = CyclewayAndDirection(TRACK, BACKWARD)
        // direction does not matter for NONE, best way to test that is to set BOTH
        val none = CyclewayAndDirection(NONE, BOTH)

        for (isLeftHandTraffic in listOf(false, true)) {
            assertFalse(
                LeftAndRightCycleway(forwardTrack, none)
                    .isNotOnewayForCyclistsNow(mapOf("oneway" to "yes"), isLeftHandTraffic)!!
            )
            assertFalse(
                LeftAndRightCycleway(none, forwardTrack)
                    .isNotOnewayForCyclistsNow(mapOf("oneway" to "yes"), isLeftHandTraffic)!!
            )
            assertFalse(
                LeftAndRightCycleway(backwardTrack, none)
                    .isNotOnewayForCyclistsNow(mapOf("oneway" to "-1"), isLeftHandTraffic)!!
            )
            assertFalse(
                LeftAndRightCycleway(none, backwardTrack)
                    .isNotOnewayForCyclistsNow(mapOf("oneway" to "-1"), isLeftHandTraffic)!!
            )
        }
    }
}

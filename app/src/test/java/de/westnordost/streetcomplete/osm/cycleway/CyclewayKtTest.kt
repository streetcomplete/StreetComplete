package de.westnordost.streetcomplete.osm.cycleway

import de.westnordost.streetcomplete.osm.cycleway.Cycleway.*
import de.westnordost.streetcomplete.osm.cycleway.Direction.*
import kotlin.test.Test
import kotlin.test.assertFalse
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

    @Test fun `not a oneway is no oneway for cyclists`() {
        assertTrue(
            LeftAndRightCycleway(
                CyclewayAndDirection(NONE, BACKWARD),
                CyclewayAndDirection(NONE, FORWARD)
            ).isNotOnewayForCyclistsNow(mapOf(), false)
        )
    }

    @Test fun `forward oneway is oneway for cyclists`() {
        assertFalse(
            LeftAndRightCycleway(
                CyclewayAndDirection(NONE, BACKWARD),
                CyclewayAndDirection(NONE, FORWARD)
            ).isNotOnewayForCyclistsNow(mapOf("oneway" to "yes"), false)
        )
    }

    @Test fun `reverse oneway is oneway for cyclists`() {
        assertFalse(
            LeftAndRightCycleway(
                CyclewayAndDirection(NONE, BACKWARD),
                CyclewayAndDirection(NONE, FORWARD)
            ).isNotOnewayForCyclistsNow(mapOf("oneway" to "-1"), false)
        )
    }

    @Test fun `oneway is no oneway for cyclists when there is a dual track`() {
        assertTrue(
            LeftAndRightCycleway(
                CyclewayAndDirection(NONE, BACKWARD),
                CyclewayAndDirection(TRACK, BOTH)
            ).isNotOnewayForCyclistsNow(mapOf("oneway" to "yes"), false)
        )
        assertTrue(
            LeftAndRightCycleway(
                CyclewayAndDirection(TRACK, BOTH),
                CyclewayAndDirection(NONE, FORWARD)
            ).isNotOnewayForCyclistsNow(mapOf("oneway" to "-1"), false)
        )
    }

    @Test fun `oneway is no oneway for cyclists when any cycleway goes in contra-flow direction`() {
        assertTrue(
            LeftAndRightCycleway(CyclewayAndDirection(TRACK, BACKWARD), null)
                .isNotOnewayForCyclistsNow(mapOf("oneway" to "yes"), false)
        )
        assertTrue(
            LeftAndRightCycleway(null, CyclewayAndDirection(TRACK, BACKWARD))
                .isNotOnewayForCyclistsNow(mapOf("oneway" to "yes"), false)
        )
        assertTrue(
            LeftAndRightCycleway(CyclewayAndDirection(TRACK, FORWARD), null)
                .isNotOnewayForCyclistsNow(mapOf("oneway" to "-1"), false)
        )
        assertTrue(
            LeftAndRightCycleway(null, CyclewayAndDirection(TRACK, FORWARD))
                .isNotOnewayForCyclistsNow(mapOf("oneway" to "-1"), false)
        )
    }

    @Test fun `oneway is a oneway for cyclists when separately mapped cycleway goes in contra-flow direction`() {
        assertFalse(
            LeftAndRightCycleway(CyclewayAndDirection(SEPARATE, BACKWARD), null)
                .isNotOnewayForCyclistsNow(mapOf("oneway" to "yes"), false)
        )
        assertFalse(
            LeftAndRightCycleway(null, CyclewayAndDirection(SEPARATE, BACKWARD))
                .isNotOnewayForCyclistsNow(mapOf("oneway" to "yes"), false)
        )
        assertFalse(
            LeftAndRightCycleway(CyclewayAndDirection(SEPARATE, FORWARD), null)
                .isNotOnewayForCyclistsNow(mapOf("oneway" to "-1"), false)
        )
        assertFalse(
            LeftAndRightCycleway(null, CyclewayAndDirection(SEPARATE, FORWARD))
                .isNotOnewayForCyclistsNow(mapOf("oneway" to "-1"), false)
        )
    }

    @Test fun `oneway is oneway for cyclists when no cycleway goes in contra-flow direction`() {
        assertFalse(
            LeftAndRightCycleway(CyclewayAndDirection(TRACK, FORWARD), null)
                .isNotOnewayForCyclistsNow(mapOf("oneway" to "yes"), false)
        )
        assertFalse(
            LeftAndRightCycleway(null, CyclewayAndDirection(TRACK, FORWARD))
                .isNotOnewayForCyclistsNow(mapOf("oneway" to "yes"), false)
        )
        assertFalse(
            LeftAndRightCycleway(CyclewayAndDirection(TRACK, BACKWARD), null)
                .isNotOnewayForCyclistsNow(mapOf("oneway" to "-1"), false)
        )
        assertFalse(
            LeftAndRightCycleway(null, CyclewayAndDirection(TRACK, BACKWARD))
                .isNotOnewayForCyclistsNow(mapOf("oneway" to "-1"), false)
        )
    }
}

package de.westnordost.streetcomplete.quests.parking_lanes

import org.junit.Assert.*
import org.junit.Test

import de.westnordost.streetcomplete.quests.parking_lanes.ParkingLanePosition.*


class ParkingLanesParserKtTest {

    @Test fun `parallel parking`() {
        assertEquals(
            LeftAndRightParkingLane(ParallelParkingLane(null), null),
            createParkingLaneSides(mapOf("parking:lane:left" to "parallel")))

        assertEquals(
            LeftAndRightParkingLane(ParallelParkingLane(ON_STREET), null),
            createParkingLaneSides(mapOf(
                "parking:lane:left" to "parallel",
                "parking:lane:left:parallel" to "on_street"
            )))
    }

    @Test fun `diagonal parking`() {
        assertEquals(
            LeftAndRightParkingLane(DiagonalParkingLane(null), null),
            createParkingLaneSides(mapOf("parking:lane:left" to "diagonal")))

        assertEquals(
            LeftAndRightParkingLane(DiagonalParkingLane(ON_STREET), null),
            createParkingLaneSides(mapOf(
                "parking:lane:left" to "diagonal",
                "parking:lane:left:diagonal" to "on_street"
            )))
    }

    @Test fun `perpendicular parking`() {
        assertEquals(
            LeftAndRightParkingLane(PerpendicularParkingLane(null), null),
            createParkingLaneSides(mapOf("parking:lane:left" to "perpendicular")))

        assertEquals(
            LeftAndRightParkingLane(PerpendicularParkingLane(ON_STREET), null),
            createParkingLaneSides(mapOf(
                "parking:lane:left" to "perpendicular",
                "parking:lane:left:perpendicular" to "on_street"
            )))
    }

    @Test fun `marked parking`() {
        assertEquals(
            LeftAndRightParkingLane(MarkedParkingLane, null),
            createParkingLaneSides(mapOf("parking:lane:left" to "marked")))
    }

    @Test fun `no parking`() {
        assertEquals(
            LeftAndRightParkingLane(NoParking, null),
            createParkingLaneSides(mapOf("parking:lane:left" to "no_parking")))
    }

    @Test fun `no stopping`() {
        assertEquals(
            LeftAndRightParkingLane(NoStopping, null),
            createParkingLaneSides(mapOf("parking:lane:left" to "no_stopping")))
    }

    @Test fun `no parking lane`() {
        assertEquals(
            LeftAndRightParkingLane(NoParkingLane, null),
            createParkingLaneSides(mapOf("parking:lane:left" to "no")))
    }

    @Test fun `fire lane`() {
        assertEquals(
            LeftAndRightParkingLane(FireLane, null),
            createParkingLaneSides(mapOf("parking:lane:left" to "fire_lane")))
    }

    @Test fun `unknown lane`() {
        assertEquals(
            LeftAndRightParkingLane(UnknownParkingLane, null),
            createParkingLaneSides(mapOf("parking:lane:left" to "something something")))
    }

    @Test fun `all unknown`() {
        assertEquals(
            null,
            createParkingLaneSides(mapOf()))
    }

    @Test fun `only left lane`() {
        assertEquals(
            LeftAndRightParkingLane(MarkedParkingLane, null),
            createParkingLaneSides(mapOf("parking:lane:left" to "marked")))
    }

    @Test fun `only right lane`() {
        assertEquals(
            LeftAndRightParkingLane(null, MarkedParkingLane),
            createParkingLaneSides(mapOf("parking:lane:right" to "marked")))
    }

    @Test fun `left and right different lane`() {
        assertEquals(
            LeftAndRightParkingLane(NoParking, MarkedParkingLane),
            createParkingLaneSides(mapOf(
                "parking:lane:right" to "marked",
                "parking:lane:left" to "no_parking"
            )))
    }

    @Test fun `left and right same lane`() {
        assertEquals(
            LeftAndRightParkingLane(NoParking, NoParking),
            createParkingLaneSides(mapOf(
                "parking:lane:right" to "no_parking",
                "parking:lane:left" to "no_parking"
            )))

        assertEquals(
            LeftAndRightParkingLane(NoParking, NoParking),
            createParkingLaneSides(mapOf(
                "parking:lane:both" to "no_parking"
            )))

        assertEquals(
            LeftAndRightParkingLane(NoParking, NoParking),
            createParkingLaneSides(mapOf(
                "parking:lane" to "no_parking"
            )))
    }

    @Test fun `parking lane position on street`() {
        assertEquals(
            LeftAndRightParkingLane(ParallelParkingLane(ON_STREET), null),
            createParkingLaneSides(mapOf(
                "parking:lane:left" to "parallel",
                "parking:lane:left:parallel" to "on_street"
            ))
        )
    }

    @Test fun `parking lane position half on kerb`() {
        assertEquals(
            LeftAndRightParkingLane(ParallelParkingLane(HALF_ON_KERB), null),
            createParkingLaneSides(mapOf(
                "parking:lane:left" to "parallel",
                "parking:lane:left:parallel" to "half_on_kerb"
            ))
        )
    }

    @Test fun `parking lane position on kerb`() {
        assertEquals(
            LeftAndRightParkingLane(ParallelParkingLane(ON_KERB), null),
            createParkingLaneSides(mapOf(
                "parking:lane:left" to "parallel",
                "parking:lane:left:parallel" to "on_kerb"
            ))
        )
    }

    @Test fun `parking lane position on shoulder`() {
        assertEquals(
            LeftAndRightParkingLane(ParallelParkingLane(SHOULDER), null),
            createParkingLaneSides(mapOf(
                "parking:lane:left" to "parallel",
                "parking:lane:left:parallel" to "shoulder"
            ))
        )
    }

    @Test fun `parking lane position on painted area only`() {
        assertEquals(
            LeftAndRightParkingLane(ParallelParkingLane(PAINTED_AREA_ONLY), null),
            createParkingLaneSides(mapOf(
                "parking:lane:left" to "parallel",
                "parking:lane:left:parallel" to "painted_area_only"
            ))
        )
    }

    @Test fun `parking lane position in lay by`() {
        assertEquals(
            LeftAndRightParkingLane(ParallelParkingLane(LAY_BY), null),
            createParkingLaneSides(mapOf(
                "parking:lane:left" to "parallel",
                "parking:lane:left:parallel" to "lay_by"
            ))
        )
    }

    @Test fun `parking lane position unknown`() {
        assertEquals(
            LeftAndRightParkingLane(ParallelParkingLane(UNKNOWN), null),
            createParkingLaneSides(mapOf(
                "parking:lane:left" to "parallel",
                "parking:lane:left:parallel" to "what is this"
            ))
        )
    }

    @Test fun `both is applied to both sides`() {
        assertEquals(
            LeftAndRightParkingLane(ParallelParkingLane(ON_STREET), ParallelParkingLane(null)),
            createParkingLaneSides(mapOf(
                "parking:lane:both" to "parallel",
                "parking:lane:left:parallel" to "on_street"
            ))
        )
    }
}
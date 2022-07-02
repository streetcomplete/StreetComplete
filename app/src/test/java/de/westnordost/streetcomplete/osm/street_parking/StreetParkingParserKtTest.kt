package de.westnordost.streetcomplete.osm.street_parking

import de.westnordost.streetcomplete.osm.street_parking.ParkingOrientation.DIAGONAL
import de.westnordost.streetcomplete.osm.street_parking.ParkingOrientation.PARALLEL
import de.westnordost.streetcomplete.osm.street_parking.ParkingOrientation.PERPENDICULAR
import de.westnordost.streetcomplete.osm.street_parking.ParkingPosition.HALF_ON_KERB
import de.westnordost.streetcomplete.osm.street_parking.ParkingPosition.ON_KERB
import de.westnordost.streetcomplete.osm.street_parking.ParkingPosition.ON_STREET
import de.westnordost.streetcomplete.osm.street_parking.ParkingPosition.STREET_SIDE
import org.junit.Assert.assertEquals
import org.junit.Test

class StreetParkingParserKtTest {

    @Test fun `parallel parking on street left`() {
        assertEquals(
            LeftAndRightStreetParking(StreetParkingPositionAndOrientation(PARALLEL, ON_STREET), null),
            createStreetParkingSides(mapOf(
                "parking:lane:left" to "parallel",
                "parking:lane:left:parallel" to "on_street"
            ))
        )
    }

    @Test fun `diagonal parking on kerb right`() {
        assertEquals(
            LeftAndRightStreetParking(null, StreetParkingPositionAndOrientation(DIAGONAL, ON_KERB)),
            createStreetParkingSides(mapOf(
                "parking:lane:right" to "diagonal",
                "parking:lane:right:diagonal" to "on_kerb"
            ))
        )
    }

    @Test fun `perpendicular street side parking both`() {
        assertEquals(
            LeftAndRightStreetParking(
                StreetParkingPositionAndOrientation(PERPENDICULAR, STREET_SIDE),
                StreetParkingPositionAndOrientation(PERPENDICULAR, STREET_SIDE)
            ),
            createStreetParkingSides(mapOf(
                "parking:lane:both" to "perpendicular",
                "parking:lane:both:perpendicular" to "street_side"
            ))
        )

        assertEquals(
            LeftAndRightStreetParking(
                StreetParkingPositionAndOrientation(PERPENDICULAR, STREET_SIDE),
                StreetParkingPositionAndOrientation(PERPENDICULAR, HALF_ON_KERB)
            ),
            createStreetParkingSides(mapOf(
                "parking:lane:both" to "perpendicular",
                "parking:lane:left:perpendicular" to "street_side",
                "parking:lane:right:perpendicular" to "half_on_kerb",
            ))
        )
    }

    @Test fun `no parking`() {
        assertEquals(
            LeftAndRightStreetParking(NoStreetParking, null),
            createStreetParkingSides(mapOf("parking:lane:left" to "no")))

        assertEquals(
            LeftAndRightStreetParking(NoStreetParking, null),
            createStreetParkingSides(mapOf(
                "parking:lane:left" to "no",
                "parking:condition:left" to "no"
            )))
    }

    @Test fun `stopping prohibited`() {
        assertEquals(
            LeftAndRightStreetParking(StreetStoppingProhibited, null),
            createStreetParkingSides(mapOf("parking:lane:left" to "no_stopping")))

        assertEquals(
            LeftAndRightStreetParking(StreetStoppingProhibited, null),
            createStreetParkingSides(mapOf(
                "parking:lane:left" to "no",
                "parking:condition:left" to "no_stopping"
            )))
    }

    @Test fun `parking prohibited`() {
        assertEquals(
            LeftAndRightStreetParking(StreetParkingProhibited, null),
            createStreetParkingSides(mapOf("parking:lane:left" to "no_parking")))

        assertEquals(
            LeftAndRightStreetParking(StreetParkingProhibited, null),
            createStreetParkingSides(mapOf(
                "parking:lane:left" to "no",
                "parking:condition:left" to "no_parking"
            )))
    }

    @Test fun `standing prohibited`() {
        assertEquals(
            LeftAndRightStreetParking(StreetStandingProhibited, null),
            createStreetParkingSides(mapOf("parking:lane:left" to "no_standing")))

        assertEquals(
            LeftAndRightStreetParking(StreetStandingProhibited, null),
            createStreetParkingSides(mapOf(
                "parking:lane:left" to "no",
                "parking:condition:left" to "no_standing"
            )))
    }

    @Test fun `prohibited tagged without explicit lane tag`() {
        assertEquals(
            LeftAndRightStreetParking(StreetStandingProhibited, NoStreetParking),
            createStreetParkingSides(mapOf(
                "parking:condition:left" to "no_standing",
                "parking:condition:right" to "no"
            )))
    }

    @Test fun `parking separate`() {
        assertEquals(
            LeftAndRightStreetParking(StreetParkingSeparate, null),
            createStreetParkingSides(mapOf("parking:lane:left" to "separate")))
    }

    @Test fun `parking yes`() {
        assertEquals(
            LeftAndRightStreetParking(IncompleteStreetParking, null),
            createStreetParkingSides(mapOf("parking:lane:left" to "yes")))
    }

    @Test fun `unknown orientation leads to unknown`() {
        assertEquals(
            LeftAndRightStreetParking(UnknownStreetParking, null),
            createStreetParkingSides(mapOf("parking:lane:left" to "something")))
    }

    @Test fun `unknown position leads to unknown`() {
        assertEquals(
            LeftAndRightStreetParking(UnknownStreetParking, null),
            createStreetParkingSides(mapOf(
                "parking:lane:left" to "parallel",
                "parking:lane:left:parallel" to "something"
            )))
    }

    @Test fun `marked is interpreted as incomplete`() {
        assertEquals(
            LeftAndRightStreetParking(IncompleteStreetParking, null),
            createStreetParkingSides(mapOf(
                "parking:lane:left" to "marked",
                "parking:lane:left:marked" to "on_kerb"
            )))
    }

    @Test fun `orientation without position is interpreted as incomplete`() {
        assertEquals(
            LeftAndRightStreetParking(IncompleteStreetParking, null),
            createStreetParkingSides(mapOf("parking:lane:left" to "diagonal")))
    }

    @Test fun `all unknown`() {
        assertEquals(
            null,
            createStreetParkingSides(mapOf()))
    }

    @Test fun `left and right different parking`() {
        assertEquals(
            LeftAndRightStreetParking(StreetParkingProhibited, NoStreetParking),
            createStreetParkingSides(mapOf(
                "parking:lane:left" to "no_parking",
                "parking:lane:right" to "no"
            )))
    }

    @Test fun `left and right same lane`() {
        assertEquals(
            LeftAndRightStreetParking(StreetParkingProhibited, StreetParkingProhibited),
            createStreetParkingSides(mapOf(
                "parking:lane:right" to "no_parking",
                "parking:lane:left" to "no_parking"
            )))

        assertEquals(
            LeftAndRightStreetParking(StreetParkingProhibited, StreetParkingProhibited),
            createStreetParkingSides(mapOf(
                "parking:lane:both" to "no_parking"
            )))

        assertEquals(
            LeftAndRightStreetParking(StreetParkingProhibited, StreetParkingProhibited),
            createStreetParkingSides(mapOf(
                "parking:lane" to "no_parking"
            )))
    }
}

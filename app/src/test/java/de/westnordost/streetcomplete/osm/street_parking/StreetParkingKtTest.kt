package de.westnordost.streetcomplete.osm.street_parking

import kotlin.test.Test
import kotlin.test.assertEquals

class StreetParkingTest {
    @Test fun validOrNullValues() {
        for (invalidParking in listOf(IncompleteStreetParking, UnknownStreetParking)) {
            assertEquals(
                LeftAndRightStreetParking(null, null),
                LeftAndRightStreetParking(invalidParking, invalidParking).validOrNullValues()
            )
            assertEquals(
                LeftAndRightStreetParking(NoStreetParking, null),
                LeftAndRightStreetParking(NoStreetParking, invalidParking).validOrNullValues()
            )
            assertEquals(
                LeftAndRightStreetParking(null, NoStreetParking),
                LeftAndRightStreetParking(invalidParking, NoStreetParking).validOrNullValues()
            )
        }
    }
}

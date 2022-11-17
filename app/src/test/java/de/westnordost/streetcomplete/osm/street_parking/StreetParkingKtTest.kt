package de.westnordost.streetcomplete.osm.street_parking

import org.junit.Assert.assertEquals
import org.junit.Test

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

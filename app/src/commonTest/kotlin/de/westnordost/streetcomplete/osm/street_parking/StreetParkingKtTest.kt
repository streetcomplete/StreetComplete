package de.westnordost.streetcomplete.osm.street_parking

import kotlin.test.Test
import kotlin.test.assertEquals

class StreetParkingKtTest {
    @Test fun validOrNullValues() {
        for (invalidParking in listOf(StreetParking.Incomplete, StreetParking.Unknown)) {
            assertEquals(
                LeftAndRightStreetParking(null, null),
                LeftAndRightStreetParking(invalidParking, invalidParking).validOrNullValues()
            )
            assertEquals(
                LeftAndRightStreetParking(StreetParking.None, null),
                LeftAndRightStreetParking(StreetParking.None, invalidParking).validOrNullValues()
            )
            assertEquals(
                LeftAndRightStreetParking(null, StreetParking.None),
                LeftAndRightStreetParking(invalidParking, StreetParking.None).validOrNullValues()
            )
        }
    }
}

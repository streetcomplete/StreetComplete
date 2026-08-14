package de.westnordost.streetcomplete.osm.street_parking

import de.westnordost.streetcomplete.osm.Sides
import kotlin.test.Test
import kotlin.test.assertEquals

class StreetParkingKtTest {
    @Test fun validOrNullValues() {
        for (invalidParking in listOf(StreetParking.Incomplete, StreetParking.Unknown)) {
            assertEquals(
                Sides<StreetParking>(null, null),
                Sides<StreetParking>(invalidParking, invalidParking).validOrNullValues()
            )
            assertEquals(
                Sides<StreetParking>(StreetParking.None, null),
                Sides<StreetParking>(StreetParking.None, invalidParking).validOrNullValues()
            )
            assertEquals(
                Sides<StreetParking>(null, StreetParking.None),
                Sides<StreetParking>(invalidParking, StreetParking.None).validOrNullValues()
            )
        }
    }
}

package de.westnordost.streetcomplete.osm.street_parking

import de.westnordost.streetcomplete.osm.Sides
import kotlin.test.Test

class StreetParkingKtTest {
    @Test fun validOrNullValues() {
        for (invalidParking in listOf(StreetParking.Incomplete, StreetParking.Unknown)) {
            assertEquals(
                Sides(null, null),
                Sides(invalidParking, invalidParking).validOrNullValues()
            )
            assertEquals(
                Sides(StreetParking.None, null),
                Sides(StreetParking.None, invalidParking).validOrNullValues()
            )
            assertEquals(
                Sides(null, StreetParking.None),
                Sides(invalidParking, StreetParking.None).validOrNullValues()
            )
        }
    }
}

private fun assertEquals(expected: Sides<StreetParking>?, actual: Sides<StreetParking>?) =
    kotlin.test.assertEquals(expected, actual)

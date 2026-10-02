package de.westnordost.streetcomplete.quests.charging_station_bicycles

import de.westnordost.streetcomplete.testutils.node
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AddChargingStationBicyclesTest {

    private val questType = AddChargingStationBicycles()

    @Test fun `is not applicable to empty tags`() {
        assertFalse(questType.isApplicableTo(node()))
    }

    @Test fun `is applicable to charging station with schuko = 2`() {
        assertTrue(questType.isApplicableTo(node(tags = mapOf(
            "amenity" to "charging_station",
            "socket:schuko" to "2",
        ))))
    }

    @Test fun `is not applicable charging station without bicycle compatible sockets`() {
        assertFalse(questType.isApplicableTo(node(tags = mapOf(
            "amenity" to "charging_station",
            "socket:schuko" to "0",
            "socket:type2" to "10",
        ))))
    }
}

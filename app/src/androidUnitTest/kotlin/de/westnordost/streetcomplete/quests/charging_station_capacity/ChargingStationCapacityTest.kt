package de.westnordost.streetcomplete.quests.charging_station_capacity

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.quests.answerApplied
import de.westnordost.streetcomplete.quests.car_wash_type.CarWashType.SERVICE
import de.westnordost.streetcomplete.testutils.node
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ChargingStationCapacityTest {
    private val questType = AddChargingStationCapacity()

    @Test fun `applicable to motorcar charging station`() {
        assertTrue(questType.isApplicableTo(node(tags = mapOf(
            "amenity" to "charging_station",
            "motorcar" to "yes",
            "boat" to "no",
            "hgv" to "yes"
        ))))
    }

    @Test fun `applicable to charging station without vehicle`() {
        assertTrue(questType.isApplicableTo(node(tags = mapOf(
            "amenity" to "charging_station"
        ))))
    }

    @Test fun `not applicable to charging station with capacity already set`() {
        assertFalse(questType.isApplicableTo(node(tags = mapOf(
            "amenity" to "charging_station",
            "capacity:motorcar" to "5"
        ))))
    }

    @Test fun `applies capacity tag to charging station without any other forms of transport`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("capacity", "1")
            ),
            questType.answerApplied(1)
        )
    }
}

package de.westnordost.streetcomplete.quests.charging_station_socket

import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.meta.IncompleteCountryInfo
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryDelete
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryModify
import de.westnordost.streetcomplete.data.osm.geometry.ElementPointGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolygonsGeometry
import de.westnordost.streetcomplete.osm.nowAsCheckDateString
import de.westnordost.streetcomplete.quests.answerApplied
import de.westnordost.streetcomplete.quests.answerAppliedTo
import de.westnordost.streetcomplete.quests.charging_station_socket.SocketType.CHADEMO
import de.westnordost.streetcomplete.quests.charging_station_socket.SocketType.TYPE2
import de.westnordost.streetcomplete.quests.charging_station_socket.SocketType.TYPE2_CABLE
import de.westnordost.streetcomplete.quests.charging_station_socket.SocketType.TYPE2_COMBO
import de.westnordost.streetcomplete.testutils.TestMapDataWithGeometry
import de.westnordost.streetcomplete.testutils.createMapData
import de.westnordost.streetcomplete.testutils.node
import de.westnordost.streetcomplete.testutils.p
import de.westnordost.streetcomplete.testutils.way
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AddChargingStationSocketTest {

    private val countryInfoWithSockets = CountryInfo(
        "DE",
        listOf(IncompleteCountryInfo(chargingStationSocketTypes = listOf(
            "type2", "type2_cable", "type2_combo", "chademo"
        )))
    )
    private val countryInfoWithoutSpecificSockets = CountryInfo(
        "CN",
        listOf(IncompleteCountryInfo(chargingStationSocketTypes = emptyList()))
    )

    private val questType = AddChargingStationSocket()

    @Test fun `applicable to charging station without socket tags`() {
        assertTrue(questType.isApplicableTo(chargingStation())!!)
        val mapData = TestMapDataWithGeometry(listOf(chargingStation()))
        assertEquals(1, questType.getApplicableElements(mapData).toList().size)
    }

    @Test fun `not applicable to motorcar=no`() {
        assertFalse(questType.isApplicableTo(chargingStation("motorcar" to "no"))!!)
    }

    @Test fun `not applicable to motor_vehicle=no`() {
        assertFalse(questType.isApplicableTo(chargingStation("motor_vehicle" to "no"))!!)
    }

    @Test fun `not applicable to access=private`() {
        assertFalse(questType.isApplicableTo(chargingStation("access" to "private"))!!)
    }

    @Test fun `not applicable to access=no`() {
        assertFalse(questType.isApplicableTo(chargingStation("access" to "no"))!!)
    }

    @Test fun `applicable to socket type2=yes`() {
        assertTrue(questType.isApplicableTo(chargingStation("socket:type2" to "yes"))!!)
    }

    @Test fun `not applicable to numeric socket type2=2`() {
        assertFalse(questType.isApplicableTo(chargingStation("socket:type2" to "2"))!!)
    }

    @Test fun `applicable to mixed numeric and yes socket values`() {
        assertTrue(questType.isApplicableTo(chargingStation(
            "socket:type2" to "2",
            "socket:chademo" to "yes"
        ))!!)
    }

    @Test fun `applicable when deprecated socket keys are present`() {
        assertTrue(questType.isApplicableTo(chargingStation("socket:tesla_standard" to "2"))!!)
        assertTrue(questType.isApplicableTo(chargingStation("socket:css" to "1"))!!)
        assertTrue(questType.isApplicableTo(chargingStation("socket:unknown" to "yes"))!!)
        assertTrue(questType.isApplicableTo(chargingStation("socket:type" to "type2"))!!)
    }

    @Test fun `unrelated socket tags alone do not hide the quest`() {
        assertTrue(questType.isApplicableTo(chargingStation("socket:usb" to "yes"))!!)
    }

    @Test fun `applicable when country metadata has no specific socket types because domestic is always available`() {
        assertTrue(questType.isApplicableTo(chargingStation())!!)
        assertEquals(
            listOf(SocketType.DOMESTIC),
            socketTypesForCountry(countryInfoWithoutSpecificSockets)
        )
    }

    @Test fun `domestic is offered independently of country metadata`() {
        val types = socketTypesForCountry(countryInfoWithSockets)
        assertTrue(SocketType.DOMESTIC in types)
        assertTrue(SocketType.TYPE2 in types)
        assertTrue(SocketType.TYPE2_CABLE in types)
        assertTrue(SocketType.TYPE2_COMBO in types)
        assertTrue(SocketType.CHADEMO in types)
        assertEquals(SocketType.DOMESTIC, types.last())
    }

    @Test fun `isApplicableTo for ways needs surrounding map data`() {
        assertNull(questType.isApplicableTo(chargingStationWay()))
    }

    @Test fun `not applicable to charging station area containing charge_point`() {
        val station = chargingStationWay()
        val chargePoint = node(10, INSIDE, mapOf("man_made" to "charge_point"))
        val mapData = createMapData(mapOf(
            station to STATION_GEOMETRY,
            chargePoint to ElementPointGeometry(INSIDE)
        ))
        assertEquals(0, questType.getApplicableElements(mapData).toList().size)
    }

    @Test fun `applicable to charging station area without charge_point inside`() {
        val station = chargingStationWay()
        val chargePoint = node(10, OUTSIDE, mapOf("man_made" to "charge_point"))
        val mapData = createMapData(mapOf(
            station to STATION_GEOMETRY,
            chargePoint to ElementPointGeometry(OUTSIDE)
        ))
        assertEquals(1, questType.getApplicableElements(mapData).toList().size)
    }

    @Test fun `apply multiple socket counts`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("socket:type2", "2"),
                StringMapEntryAdd("socket:type2_combo", "1"),
                StringMapEntryAdd("socket:type2_cable", "no")
            ),
            questType.answerApplied(mapOf(TYPE2 to 2, TYPE2_COMBO to 1))
        )
    }

    @Test fun `zero-count values are not written`() {
        val changes = questType.answerApplied(mapOf(TYPE2 to 2, CHADEMO to 0))
        assertTrue(changes.none { it.key == "socket:chademo" })
        assertFalse(changes.contains(StringMapEntryAdd("socket:chademo", "0")))
    }

    @Test fun `sets type2_cable=no when type2 is present without cable`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("socket:type2", "2"),
                StringMapEntryAdd("socket:type2_cable", "no")
            ),
            questType.answerApplied(mapOf(TYPE2 to 2))
        )
    }

    @Test fun `does not set type2_cable=no when cable is selected`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("socket:type2", "1"),
                StringMapEntryAdd("socket:type2_cable", "1")
            ),
            questType.answerApplied(mapOf(TYPE2 to 1, TYPE2_CABLE to 1))
        )
    }

    @Test fun `unrelated socket subkeys survive`() {
        val changes = questType.answerAppliedTo(
            mapOf(TYPE2 to 2),
            mapOf(
                "socket:type2:output" to "22 kW",
                "socket:type2:voltage" to "400",
                "socket:type2:current" to "32"
            )
        )
        assertFalse(changes.any { it is StringMapEntryDelete && it.key.startsWith("socket:type2:") })
        assertTrue(changes.contains(StringMapEntryAdd("socket:type2", "2")))
        assertTrue(changes.contains(StringMapEntryAdd("socket:type2_cable", "no")))
    }

    @Test fun `unrelated normal tags and other socket types survive`() {
        val changes = questType.answerAppliedTo(
            mapOf(TYPE2 to 2),
            mapOf(
                "name" to "Stadtwerke",
                "operator" to "EnBW",
                "socket:nacs" to "1"
            )
        )
        assertFalse(changes.any { it.key == "name" || it.key == "operator" || it.key == "socket:nacs" })
    }

    @Test fun `removes deprecated socket keys`() {
        val changes = questType.answerAppliedTo(
            mapOf(TYPE2 to 1),
            mapOf(
                "socket:tesla_standard" to "2",
                "socket:css" to "1",
                "socket:unknown" to "yes",
                "socket:type" to "type2"
            )
        )
        assertTrue(changes.contains(StringMapEntryDelete("socket:tesla_standard", "2")))
        assertTrue(changes.contains(StringMapEntryDelete("socket:css", "1")))
        assertTrue(changes.contains(StringMapEntryDelete("socket:unknown", "yes")))
        assertTrue(changes.contains(StringMapEntryDelete("socket:type", "type2")))
    }

    @Test fun `updates existing numeric values and adds check date when unchanged`() {
        assertEquals(
            setOf(
                StringMapEntryModify("socket:type2", "2", "2"),
                StringMapEntryModify("socket:type2_cable", "no", "no"),
                StringMapEntryAdd("check_date:socket", nowAsCheckDateString())
            ),
            questType.answerAppliedTo(
                mapOf(TYPE2 to 2, TYPE2_CABLE to 0),
                mapOf(
                    "socket:type2" to "2",
                    "socket:type2_cable" to "no"
                )
            )
        )
    }

    @Test fun `replaces yes value with numeric count`() {
        assertEquals(
            setOf(
                StringMapEntryModify("socket:type2", "yes", "2"),
                StringMapEntryAdd("socket:type2_cable", "no")
            ),
            questType.answerAppliedTo(
                mapOf(TYPE2 to 2),
                mapOf("socket:type2" to "yes")
            )
        )
    }

    @Test fun `removes managed key when count is set to zero`() {
        assertEquals(
            setOf(
                StringMapEntryDelete("socket:chademo", "1"),
                StringMapEntryAdd("socket:type2", "2"),
                StringMapEntryAdd("socket:type2_cable", "no")
            ),
            questType.answerAppliedTo(
                mapOf(TYPE2 to 2, CHADEMO to 0),
                mapOf("socket:chademo" to "1")
            )
        )
    }
}

private fun chargingStation(vararg tags: Pair<String, String>) = node(
    tags = mapOf("amenity" to "charging_station") + tags
)

private fun chargingStationWay() = way(
    1,
    listOf(1, 2, 3, 4, 1),
    mapOf("amenity" to "charging_station")
)

private val P1 = p(0.25, 0.25)
private val P2 = p(0.25, 0.75)
private val P3 = p(0.75, 0.75)
private val P4 = p(0.75, 0.25)
private val INSIDE = p(0.5, 0.5)
private val OUTSIDE = p(1.5, 1.5)
private val STATION_GEOMETRY = ElementPolygonsGeometry(listOf(listOf(P1, P4, P3, P2, P1)), INSIDE)

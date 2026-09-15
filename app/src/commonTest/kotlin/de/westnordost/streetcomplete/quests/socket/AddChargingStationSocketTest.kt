package de.westnordost.streetcomplete.quests.socket

import de.westnordost.streetcomplete.data.elementfilter.dateDaysAgo
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.meta.IncompleteCountryInfo
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryDelete
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryModify
import de.westnordost.streetcomplete.data.osm.geometry.ElementPointGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolygonsGeometry
import de.westnordost.streetcomplete.osm.nowAsCheckDateString
import de.westnordost.streetcomplete.osm.toCheckDateString
import de.westnordost.streetcomplete.quests.answerApplied
import de.westnordost.streetcomplete.quests.answerAppliedTo
import de.westnordost.streetcomplete.quests.socket.ChargingStationSocket.CHADEMO
import de.westnordost.streetcomplete.quests.socket.ChargingStationSocket.DOMESTIC
import de.westnordost.streetcomplete.quests.socket.ChargingStationSocket.TYPE2
import de.westnordost.streetcomplete.quests.socket.ChargingStationSocket.TYPE2_CABLE
import de.westnordost.streetcomplete.quests.socket.ChargingStationSocket.TYPE2_COMBO
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

    private val questType = AddChargingStationSocket()

    private val expiredCheckDate = dateDaysAgo(365 * 2 + 2f).toCheckDateString()
    private val recentCheckDate = nowAsCheckDateString()

    @Test fun `applicable to charging station without socket tags`() {
        val s = chargingStation()
        assertTrue(questType.isApplicableTo(s)!!)
        val mapData = TestMapDataWithGeometry(listOf(s))
        assertEquals(1, questType.getApplicableElements(mapData).toList().size)
    }

    @Test fun `applicable to charging station with only unknown socket tags`() {
        val s = chargingStation("socket:cee_blue" to "3")
        assertTrue(questType.isApplicableTo(s)!!)
        val mapData = TestMapDataWithGeometry(listOf(s))
        assertEquals(1, questType.getApplicableElements(mapData).toList().size)
    }

    @Test fun `not applicable to charging station with also unknown socket tags`() {
        val s = chargingStation("socket:cee_blue" to "3", "socket:type1" to "1")
        assertFalse(questType.isApplicableTo(s)!!)
        val mapData = TestMapDataWithGeometry(listOf(s))
        assertEquals(0, questType.getApplicableElements(mapData).toList().size)
    }

    @Test fun `applicable to charging station with known socket set to yes`() {
        val s = chargingStation("socket:type1" to "1", "socket:type1_combo" to "yes")
        assertTrue(questType.isApplicableTo(s)!!)
        val mapData = TestMapDataWithGeometry(listOf(s))
        assertEquals(1, questType.getApplicableElements(mapData).toList().size)
    }

    @Test fun `applicable to charging station with known invalid sockets`() {
        val s = chargingStation("socket:type1" to "1", "socket:ccs" to "1")
        assertTrue(questType.isApplicableTo(s)!!)
        val mapData = TestMapDataWithGeometry(listOf(s))
        assertEquals(1, questType.getApplicableElements(mapData).toList().size)
    }

    @Test fun `not applicable to recent check date`() {
        val s = chargingStation("socket:type1" to "1", "check_date:socket" to recentCheckDate)
        assertFalse(questType.isApplicableTo(s)!!)
        val mapData = TestMapDataWithGeometry(listOf(s))
        assertEquals(0, questType.getApplicableElements(mapData).toList().size)
    }

    @Test fun `applicable to tagging with old check date`() {
        val s = chargingStation("socket:type1" to "1", "check_date:socket" to expiredCheckDate)
        assertTrue(questType.isApplicableTo(s)!!)
        val mapData = TestMapDataWithGeometry(listOf(s))
        assertEquals(1, questType.getApplicableElements(mapData).toList().size)
    }

    @Test fun `isApplicableTo for ways needs surrounding map data`() {
        assertNull(questType.isApplicableTo(chargingStationWay()))
    }

    @Test fun `not applicable to charging station area containing charge_point node`() {
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

    @Test fun `answer with different counts`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("socket:type2", "no"),
                StringMapEntryAdd("socket:type2_cable", "1"),
                StringMapEntryModify("socket:type2_combo", "yes", "2"),
                StringMapEntryDelete("socket:chademo", "yes"),
                StringMapEntryDelete("socket:ccs", "yes"),
                StringMapEntryAdd("check_date:socket", nowAsCheckDateString())
            ),
            questType.answerAppliedTo(
                answer = mapOf(
                    TYPE2 to 0,
                    TYPE2_CABLE to 1,
                    TYPE2_COMBO to 2,
                ),
                tags = mapOf(
                    "socket:type2_combo" to "yes",
                    // invalid keys are always deleted
                    "socket:ccs" to "yes",
                    // unknown keys are not touched
                    "socket:cee_blue" to "1",
                    "socket:type2:output" to "22kW",
                    // DO delete sockets not selected, but...
                    "socket:chademo" to "yes",
                    "socket:type1" to "0", // don't delete explicit "no"
                    "socket:type3a" to "no", // don't delete explicit "0"

                )
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

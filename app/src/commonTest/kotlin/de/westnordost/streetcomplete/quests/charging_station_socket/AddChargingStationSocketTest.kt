package de.westnordost.streetcomplete.quests.charging_station_socket

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
import de.westnordost.streetcomplete.quests.charging_station_socket.SocketType.CHADEMO
import de.westnordost.streetcomplete.quests.charging_station_socket.SocketType.DOMESTIC
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

    private val countryInfoDe = CountryInfo(
        "DE",
        listOf(IncompleteCountryInfo(
            chargingStationSocketTypes = listOf("type2", "type2_cable", "type2_combo", "chademo"),
            domesticSocketType = listOf("typec", "schuko")
        ))
    )
    private val countryInfoEmpty = CountryInfo(
        "CN",
        listOf(IncompleteCountryInfo(chargingStationSocketTypes = emptyList()))
    )
    private val countryInfoUnsupportedOnly = CountryInfo(
        "US",
        listOf(IncompleteCountryInfo(chargingStationSocketTypes = listOf(
            "type1", "type1_combo", "nacs"
        )))
    )
    private val countryInfoGb = CountryInfo(
        "GB",
        listOf(IncompleteCountryInfo(
            chargingStationSocketTypes = listOf("type2", "type2_cable", "type2_combo", "chademo"),
            domesticSocketType = listOf("bs1363")
        ))
    )
    private val countryInfoCh = CountryInfo(
        "CH",
        listOf(IncompleteCountryInfo(
            chargingStationSocketTypes = listOf("type2", "type2_cable", "type2_combo", "chademo"),
            domesticSocketType = listOf("typec", "sev1011_t13")
        ))
    )
    private val countryInfoIlNoDomestic = CountryInfo(
        "IL",
        listOf(IncompleteCountryInfo(
            chargingStationSocketTypes = listOf("type2", "type2_cable", "type2_combo", "chademo")
        ))
    )
    private val countryInfoDomesticOnly = CountryInfo(
        "CN",
        listOf(IncompleteCountryInfo(
            chargingStationSocketTypes = listOf("gb_ac", "gb_dc"),
            domesticSocketType = listOf("gb1002")
        ))
    )

    private val questType = AddChargingStationSocket { countryInfoDe }

    private val expiredCheckDate = dateDaysAgo(365 * 2 + 1f).toCheckDateString()
    private val recentCheckDate = nowAsCheckDateString()

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

    @Test fun `not applicable to lockable=yes`() {
        assertFalse(questType.isApplicableTo(chargingStation("lockable" to "yes"))!!)
    }

    @Test fun `applicable to socket type2=yes`() {
        assertTrue(questType.isApplicableTo(chargingStation("socket:type2" to "yes"))!!)
    }

    @Test fun `applicable to numeric socket type2=2 without check_date`() {
        assertTrue(questType.isApplicableTo(chargingStation("socket:type2" to "2"))!!)
    }

    @Test fun `not applicable to numeric socket type2=2 with recent check_date`() {
        assertFalse(questType.isApplicableTo(chargingStation(
            "socket:type2" to "2",
            "check_date:socket" to recentCheckDate
        ))!!)
    }

    @Test fun `applicable to numeric socket type2=2 with expired check_date`() {
        assertTrue(questType.isApplicableTo(chargingStation(
            "socket:type2" to "2",
            "check_date:socket" to expiredCheckDate
        ))!!)
    }

    @Test fun `applicable to mixed numeric and yes regardless of recent check_date`() {
        assertTrue(questType.isApplicableTo(chargingStation(
            "socket:type2" to "2",
            "socket:chademo" to "yes",
            "check_date:socket" to recentCheckDate
        ))!!)
    }

    @Test fun `applicable when deprecated socket keys are present regardless of recent check_date`() {
        assertTrue(questType.isApplicableTo(chargingStation(
            "socket:tesla_standard" to "2",
            "check_date:socket" to recentCheckDate
        ))!!)
        assertTrue(questType.isApplicableTo(chargingStation(
            "socket:css" to "1",
            "check_date:socket" to recentCheckDate
        ))!!)
        assertTrue(questType.isApplicableTo(chargingStation(
            "socket:unknown" to "yes",
            "check_date:socket" to recentCheckDate
        ))!!)
        assertTrue(questType.isApplicableTo(chargingStation(
            "socket:type" to "type2",
            "check_date:socket" to recentCheckDate
        ))!!)
    }

    @Test fun `unrelated socket tags alone do not hide the quest`() {
        assertTrue(questType.isApplicableTo(chargingStation("socket:usb" to "yes"))!!)
    }

    @Test fun `not applicable when country metadata has no socket types`() {
        val quest = AddChargingStationSocket { countryInfoEmpty }
        assertFalse(quest.isApplicableTo(chargingStation())!!)
        assertEquals(emptyList(), socketTypesForCountry(countryInfoEmpty))
    }

    @Test fun `not applicable when country metadata has only unsupported socket types`() {
        val quest = AddChargingStationSocket { countryInfoUnsupportedOnly }
        assertFalse(quest.isApplicableTo(chargingStation())!!)
        assertEquals(emptyList(), socketTypesForCountry(countryInfoUnsupportedOnly))
    }

    @Test fun `applicable for DE-like metadata with implemented types and E F domestic plugs`() {
        assertTrue(questType.isApplicableTo(chargingStation())!!)
        assertTrue(hasSupportedSocketTypes(countryInfoDe))
        val types = socketTypesForCountry(countryInfoDe)
        assertTrue(TYPE2 in types)
        assertTrue(TYPE2_CABLE in types)
        assertTrue(TYPE2_COMBO in types)
        assertTrue(CHADEMO in types)
        assertTrue(DOMESTIC in types)
        assertEquals(DOMESTIC, types.last())
        assertEquals(
            listOf(DomesticPlugType.TYPEC, DomesticPlugType.SCHUKO),
            domesticPlugTypesForCountry(countryInfoDe)
        )
    }

    @Test fun `domestic is offered for a different household plug type such as GB`() {
        val types = socketTypesForCountry(countryInfoGb)
        assertTrue(TYPE2 in types)
        assertTrue(DOMESTIC in types)
        assertEquals(listOf(DomesticPlugType.BS1363), domesticPlugTypesForCountry(countryInfoGb))
    }

    @Test fun `domestic is offered for Swiss SEV metadata`() {
        assertEquals(
            listOf(DomesticPlugType.TYPEC, DomesticPlugType.SEV1011_T13),
            domesticPlugTypesForCountry(countryInfoCh)
        )
        assertTrue(DOMESTIC in socketTypesForCountry(countryInfoCh))
    }

    @Test fun `domestic is not offered without domesticSocketType metadata`() {
        val types = socketTypesForCountry(countryInfoIlNoDomestic)
        assertTrue(TYPE2 in types)
        assertFalse(DOMESTIC in types)
        assertEquals(emptyList(), domesticPlugTypesForCountry(countryInfoIlNoDomestic))
        val quest = AddChargingStationSocket { countryInfoIlNoDomestic }
        assertTrue(quest.isApplicableTo(chargingStation())!!)
    }

    @Test fun `unknown domesticSocketType values are ignored`() {
        val country = CountryInfo(
            "XX",
            listOf(IncompleteCountryInfo(
                chargingStationSocketTypes = listOf("type2"),
                domesticSocketType = listOf("not_a_real_plug", "schuko")
            ))
        )
        assertEquals(listOf(DomesticPlugType.SCHUKO), domesticPlugTypesForCountry(country))
        assertTrue(DOMESTIC in socketTypesForCountry(country))
    }

    @Test fun `domestic metadata alone does not make the quest applicable`() {
        val quest = AddChargingStationSocket { countryInfoDomesticOnly }
        assertFalse(quest.isApplicableTo(chargingStation())!!)
        assertEquals(emptyList(), socketTypesForCountry(countryInfoDomesticOnly))
        assertEquals(listOf(DomesticPlugType.GB1002), domesticPlugTypesForCountry(countryInfoDomesticOnly))
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

    @Test fun `resurvey preloads numeric socket counts`() {
        val tags = mapOf(
            "socket:type2" to "2",
            "socket:type2_combo" to "1",
            "socket:chademo" to "yes",
            "socket:nacs" to "3",
            "socket:type2:output" to "22 kW"
        )
        val types = listOf(TYPE2, TYPE2_CABLE, TYPE2_COMBO, CHADEMO, DOMESTIC)
        assertEquals(
            mapOf(
                TYPE2 to 2,
                TYPE2_CABLE to 0,
                TYPE2_COMBO to 1,
                CHADEMO to 0, // "yes" stays unresolved
                DOMESTIC to 0
            ),
            initialSocketCounts(tags, types)
        )
    }

    @Test fun `apply multiple socket counts always sets check_date`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("socket:type2", "2"),
                StringMapEntryAdd("socket:type2_combo", "1"),
                StringMapEntryAdd("socket:type2_cable", "no"),
                StringMapEntryAdd("check_date:socket", nowAsCheckDateString())
            ),
            questType.answerApplied(mapOf(TYPE2 to 2, TYPE2_COMBO to 1))
        )
    }

    @Test fun `zero-count values are not written`() {
        val changes = questType.answerApplied(mapOf(TYPE2 to 2, CHADEMO to 0))
        assertTrue(changes.none { it.key == "socket:chademo" })
        assertFalse(changes.contains(StringMapEntryAdd("socket:chademo", "0")))
        assertTrue(changes.contains(StringMapEntryAdd("check_date:socket", nowAsCheckDateString())))
    }

    @Test fun `sets type2_cable=no when type2 is present without cable`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("socket:type2", "2"),
                StringMapEntryAdd("socket:type2_cable", "no"),
                StringMapEntryAdd("check_date:socket", nowAsCheckDateString())
            ),
            questType.answerApplied(mapOf(TYPE2 to 2))
        )
    }

    @Test fun `does not set type2_cable=no when cable is selected`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("socket:type2", "1"),
                StringMapEntryAdd("socket:type2_cable", "1"),
                StringMapEntryAdd("check_date:socket", nowAsCheckDateString())
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
        assertTrue(changes.contains(StringMapEntryAdd("check_date:socket", nowAsCheckDateString())))
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
        assertTrue(changes.contains(StringMapEntryAdd("check_date:socket", nowAsCheckDateString())))
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
        assertTrue(changes.contains(StringMapEntryAdd("check_date:socket", nowAsCheckDateString())))
    }

    @Test fun `answering unchanged values refreshes check_date`() {
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

    @Test fun `answering unchanged values updates existing check_date`() {
        assertEquals(
            setOf(
                StringMapEntryModify("socket:type2", "2", "2"),
                StringMapEntryAdd("socket:type2_cable", "no"),
                StringMapEntryModify("check_date:socket", expiredCheckDate, nowAsCheckDateString())
            ),
            questType.answerAppliedTo(
                mapOf(TYPE2 to 2),
                mapOf(
                    "socket:type2" to "2",
                    "check_date:socket" to expiredCheckDate
                )
            )
        )
    }

    @Test fun `answering changed values also sets check_date`() {
        assertEquals(
            setOf(
                StringMapEntryModify("socket:type2", "2", "3"),
                StringMapEntryAdd("socket:type2_cable", "no"),
                StringMapEntryAdd("check_date:socket", nowAsCheckDateString())
            ),
            questType.answerAppliedTo(
                mapOf(TYPE2 to 3),
                mapOf("socket:type2" to "2")
            )
        )
    }

    @Test fun `answering changed values updates existing check_date`() {
        assertEquals(
            setOf(
                StringMapEntryModify("socket:type2", "2", "3"),
                StringMapEntryAdd("socket:type2_cable", "no"),
                StringMapEntryModify("check_date:socket", expiredCheckDate, nowAsCheckDateString())
            ),
            questType.answerAppliedTo(
                mapOf(TYPE2 to 3),
                mapOf(
                    "socket:type2" to "2",
                    "check_date:socket" to expiredCheckDate
                )
            )
        )
    }

    @Test fun `replaces yes value with numeric count and sets check_date`() {
        assertEquals(
            setOf(
                StringMapEntryModify("socket:type2", "yes", "2"),
                StringMapEntryAdd("socket:type2_cable", "no"),
                StringMapEntryAdd("check_date:socket", nowAsCheckDateString())
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
                StringMapEntryAdd("socket:type2_cable", "no"),
                StringMapEntryAdd("check_date:socket", nowAsCheckDateString())
            ),
            questType.answerAppliedTo(
                mapOf(TYPE2 to 2, CHADEMO to 0),
                mapOf("socket:chademo" to "1")
            )
        )
    }

    @Test fun `household plug count writes socket domestic not the concrete plug key`() {
        val changes = questType.answerApplied(mapOf(DOMESTIC to 2))
        assertTrue(changes.contains(StringMapEntryAdd("socket:domestic", "2")))
        assertTrue(changes.none { it.key == "socket:schuko" || it.key == "socket:typec" || it.key == "socket:bs1363" })
        assertTrue(changes.contains(StringMapEntryAdd("check_date:socket", nowAsCheckDateString())))
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

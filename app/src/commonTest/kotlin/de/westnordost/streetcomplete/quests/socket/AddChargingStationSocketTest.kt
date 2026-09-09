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
import de.westnordost.streetcomplete.quests.socket.SocketType.CHADEMO
import de.westnordost.streetcomplete.quests.socket.SocketType.DOMESTIC
import de.westnordost.streetcomplete.quests.socket.SocketType.TYPE2
import de.westnordost.streetcomplete.quests.socket.SocketType.TYPE2_CABLE
import de.westnordost.streetcomplete.quests.socket.SocketType.TYPE2_COMBO
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

    // Realistic DE-like: all implemented connectors (+ domestic presentation).
    private val countryInfoDe = CountryInfo(
        "DE",
        listOf(IncompleteCountryInfo(
            chargingStationSocketTypes = listOf("type2", "type2_cable", "type2_combo", "chademo"),
            domesticSocketType = listOf("typec", "schuko")
        ))
    )
    // Realistic US metadata: nacs/type1 dominate — must not enable a partial survey.
    private val countryInfoUs = CountryInfo(
        "US",
        listOf(IncompleteCountryInfo(
            chargingStationSocketTypes = listOf("nacs", "type1", "type1_combo", "chademo")
        ))
    )
    // Realistic JP metadata: chademo + type1 — partial survey would be misleading.
    private val countryInfoJp = CountryInfo(
        "JP",
        listOf(IncompleteCountryInfo(
            chargingStationSocketTypes = listOf("chademo", "type1", "type1_combo")
        ))
    )
    private val countryInfoEmpty = CountryInfo(
        "XX",
        listOf(IncompleteCountryInfo(chargingStationSocketTypes = emptyList()))
    )
    private val countryInfoUnsupportedOnly = CountryInfo(
        "CN",
        listOf(IncompleteCountryInfo(chargingStationSocketTypes = listOf(
            "gb_ac", "gb_dc", "chaoji"
        )))
    )
    private val countryInfoGb = CountryInfo(
        "GB",
        listOf(IncompleteCountryInfo(
            chargingStationSocketTypes = listOf("type2", "type2_cable", "type2_combo", "chademo"),
            domesticSocketType = listOf("bs1363")
        ))
    )
    // CH-like fixture used only for domestic plug presentation (real CH.yml also has type1*).
    private val countryInfoChPlugs = CountryInfo(
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
    private val countryInfoWithoutType2Cable = CountryInfo(
        "XX",
        listOf(IncompleteCountryInfo(
            chargingStationSocketTypes = listOf("type2", "type2_combo", "chademo")
        ))
    )

    private val questType = AddChargingStationSocket { countryInfoDe }
    private val deTypes = socketTypesForCountry(countryInfoDe)
    private val ilTypes = socketTypesForCountry(countryInfoIlNoDomestic)

    private val expiredCheckDate = dateDaysAgo(365 * 2 + 1f).toCheckDateString()
    private val recentCheckDate = nowAsCheckDateString()

    // region filter / access

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

    // endregion
    // region YES SAFETY

    @Test fun `yes remains unresolved and is not preloaded as zero`() {
        assertEquals(SocketPresence.Yes, parseSocketPresence("yes"))
        assertNull(parseSocketPresence("yes").toFormCount())
        assertEquals(
            mapOf(
                TYPE2 to 2,
                TYPE2_CABLE to null,
                TYPE2_COMBO to null,
                CHADEMO to null,
                DOMESTIC to null
            ),
            initialSocketFormCounts(
                mapOf("socket:type2" to "2", "socket:chademo" to "yes"),
                deTypes
            )
        )
    }

    @Test fun `form is incomplete while displayed yes remains unresolved`() {
        val tags = mapOf("socket:type2" to "2", "socket:chademo" to "yes")
        assertFalse(isCompleteSocketSurvey(tags, deTypes))
        assertFalse(isCompleteSocketSurvey(tags, ilTypes))
        // Mixed numeric + yes cannot be confirmed unchanged: yes stays null in the form.
        val form = initialSocketFormCounts(tags, listOf(TYPE2, CHADEMO))
        assertEquals(2, form[TYPE2])
        assertNull(form[CHADEMO])
        assertFalse(form.values.all { it != null })
    }

    @Test fun `resolving yes to numeric writes count`() {
        assertEquals(
            setOf(
                StringMapEntryModify("socket:chademo", "yes", "2"),
                StringMapEntryAdd("check_date:socket", nowAsCheckDateString())
            ),
            questType.answerAppliedTo(mapOf(CHADEMO to 2), mapOf("socket:chademo" to "yes"))
        )
    }

    @Test fun `resolving yes to zero writes no`() {
        assertEquals(
            setOf(
                StringMapEntryModify("socket:chademo", "yes", "no"),
                StringMapEntryAdd("check_date:socket", nowAsCheckDateString())
            ),
            questType.answerAppliedTo(mapOf(CHADEMO to 0), mapOf("socket:chademo" to "yes"))
        )
    }

    @Test fun `answering other types does not turn untouched yes into no`() {
        val changes = questType.answerAppliedTo(
            mapOf(TYPE2 to 2),
            mapOf("socket:type2" to "1", "socket:chademo" to "yes")
        )
        assertTrue(changes.contains(StringMapEntryModify("socket:type2", "1", "2")))
        assertTrue(changes.none { it.key == "socket:chademo" })
    }

    // endregion
    // region SURVEY COMPLETENESS

    @Test fun `one numeric displayed type with missing others and recent check_date is applicable`() {
        assertTrue(questType.isApplicableTo(chargingStation(
            "socket:type2" to "2",
            "check_date:socket" to recentCheckDate
        ))!!)
    }

    @Test fun `all displayed types numeric or no with recent check_date is not applicable`() {
        assertFalse(questType.isApplicableTo(chargingStation(*completeDeSurvey(),
            "check_date:socket" to recentCheckDate
        ))!!)
    }

    @Test fun `all displayed types numeric or no with expired check_date is resurvey`() {
        val tags = mapOf(*completeDeSurvey(), "check_date:socket" to expiredCheckDate)
        assertTrue(isCompleteSocketSurvey(tags, deTypes))
        assertTrue(questType.isApplicableTo(node(tags = tags + ("amenity" to "charging_station")))!!)
    }

    @Test fun `all displayed types no without check_date is applicable`() {
        assertTrue(questType.isApplicableTo(chargingStation(*completeDeSurveyAllNo()))!!)
    }

    @Test fun `all-negative answer writes no for every answered type and refreshes check_date`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("socket:type2", "no"),
                StringMapEntryAdd("socket:type2_cable", "no"),
                StringMapEntryAdd("socket:type2_combo", "no"),
                StringMapEntryAdd("socket:chademo", "no"),
                StringMapEntryAdd("socket:domestic", "no"),
                StringMapEntryAdd("check_date:socket", nowAsCheckDateString())
            ),
            questType.answerApplied(mapOf(
                TYPE2 to 0,
                TYPE2_CABLE to 0,
                TYPE2_COMBO to 0,
                CHADEMO to 0,
                DOMESTIC to 0
            ))
        )
    }

    @Test fun `all-negative resurvey confirmation refreshes check_date`() {
        assertEquals(
            setOf(
                StringMapEntryModify("socket:type2", "no", "no"),
                StringMapEntryModify("socket:type2_cable", "no", "no"),
                StringMapEntryModify("socket:type2_combo", "no", "no"),
                StringMapEntryModify("socket:chademo", "no", "no"),
                StringMapEntryModify("socket:domestic", "no", "no"),
                StringMapEntryModify("check_date:socket", expiredCheckDate, nowAsCheckDateString())
            ),
            questType.answerAppliedTo(
                mapOf(
                    TYPE2 to 0,
                    TYPE2_CABLE to 0,
                    TYPE2_COMBO to 0,
                    CHADEMO to 0,
                    DOMESTIC to 0
                ),
                mapOf(*completeDeSurveyAllNo(), "check_date:socket" to expiredCheckDate)
            )
        )
    }

    @Test fun `no preloads as zero and missing stays unresolved`() {
        assertEquals(0, parseSocketPresence("no").toFormCount())
        assertNull(parseSocketPresence(null).toFormCount())
        assertEquals(
            mapOf(TYPE2 to 0, TYPE2_CABLE to null),
            initialSocketFormCounts(
                mapOf("socket:type2" to "no"),
                listOf(TYPE2, TYPE2_CABLE)
            )
        )
    }

    @Test fun `incomplete first survey is not a resurvey`() {
        assertFalse(isCompleteSocketSurvey(emptyMap(), deTypes))
        assertFalse(isCompleteSocketSurvey(mapOf("socket:type2" to "yes"), deTypes))
        assertFalse(isCompleteSocketSurvey(mapOf("socket:type2" to "2"), deTypes))
    }

    @Test fun `complete numeric no survey is a resurvey candidate`() {
        assertTrue(isCompleteSocketSurvey(mapOf(*completeDeSurvey()), deTypes))
    }

    // endregion
    // region TYPE2 AMBIGUITY

    @Test fun `positive type2 without type2_cable is applicable even with recent check_date`() {
        assertTrue(hasAmbiguousType2CableTagging(mapOf("socket:type2" to "3")))
        assertTrue(questType.isApplicableTo(chargingStation(
            "socket:type2" to "3",
            "socket:type2_combo" to "no",
            "socket:chademo" to "no",
            "socket:domestic" to "no",
            "check_date:socket" to recentCheckDate
        ))!!)
    }

    @Test fun `explicit type2_cable no resolves ambiguity`() {
        assertFalse(hasAmbiguousType2CableTagging(mapOf(
            "socket:type2" to "3",
            "socket:type2_cable" to "no"
        )))
        assertFalse(questType.isApplicableTo(chargingStation(
            *completeDeSurvey("socket:type2" to "3", "socket:type2_cable" to "no"),
            "check_date:socket" to recentCheckDate
        ))!!)
    }

    @Test fun `numeric type2_cable resolves ambiguity`() {
        assertFalse(hasAmbiguousType2CableTagging(mapOf(
            "socket:type2" to "3",
            "socket:type2_cable" to "1"
        )))
        assertFalse(questType.isApplicableTo(chargingStation(
            *completeDeSurvey("socket:type2" to "3", "socket:type2_cable" to "1"),
            "check_date:socket" to recentCheckDate
        ))!!)
    }

    @Test fun `type2 ambiguity rule does not apply when TYPE2_CABLE is unsupported`() {
        val quest = AddChargingStationSocket { countryInfoWithoutType2Cable }
        assertFalse(TYPE2_CABLE in specificSocketTypesForCountry(countryInfoWithoutType2Cable))
        assertTrue(hasAmbiguousType2CableTagging(mapOf("socket:type2" to "3")))
        assertFalse(quest.isApplicableTo(chargingStation(
            "socket:type2" to "3",
            "socket:type2_combo" to "no",
            "socket:chademo" to "no",
            "check_date:socket" to recentCheckDate
        ))!!)
    }

    @Test fun `resolving ambiguous type2 writes type2_cable no`() {
        assertEquals(
            setOf(
                StringMapEntryModify("socket:type2", "3", "3"),
                StringMapEntryAdd("socket:type2_cable", "no"),
                StringMapEntryAdd("check_date:socket", nowAsCheckDateString())
            ),
            questType.answerAppliedTo(
                mapOf(TYPE2 to 3, TYPE2_CABLE to 0),
                mapOf("socket:type2" to "3")
            )
        )
    }

    // endregion
    // region DATA SAFETY

    @Test fun `unrelated socket types and subkeys survive`() {
        val changes = questType.answerAppliedTo(
            mapOf(TYPE2 to 2),
            mapOf(
                "name" to "Stadtwerke",
                "socket:nacs" to "1",
                "socket:type2:output" to "22 kW",
                "socket:type2:voltage" to "400",
                "socket:type2:current" to "32"
            )
        )
        assertFalse(changes.any { it.key == "name" || it.key == "socket:nacs" })
        assertFalse(changes.any { it is StringMapEntryDelete && it.key.startsWith("socket:type2:") })
        assertTrue(changes.contains(StringMapEntryAdd("socket:type2", "2")))
        assertTrue(changes.contains(StringMapEntryAdd("check_date:socket", nowAsCheckDateString())))
    }

    @Test fun `socket types not in the answer map are not written as no`() {
        val changes = questType.answerAppliedTo(
            mapOf(TYPE2 to 2),
            mapOf("socket:chademo" to "1")
        )
        assertTrue(changes.contains(StringMapEntryAdd("socket:type2", "2")))
        assertTrue(changes.none { it.key == "socket:chademo" })
        assertTrue(changes.none { it.key == "socket:type2_cable" || it.key == "socket:domestic" })
    }

    @Test fun `deprecated ambiguous keys are preserved not deleted`() {
        val tags = mapOf(
            "socket:ccs" to "1",
            "socket:css" to "1",
            "socket:tesla_standard" to "2",
            "socket:tesla_supercharger" to "4",
            "socket:unknown" to "yes",
            "socket:type" to "type2"
        )
        val changes = questType.answerAppliedTo(mapOf(TYPE2 to 1), tags)
        assertTrue(changes.none { it is StringMapEntryDelete })
        assertTrue(changes.contains(StringMapEntryAdd("socket:type2", "1")))
        assertTrue(changes.contains(StringMapEntryAdd("check_date:socket", nowAsCheckDateString())))
    }

    // endregion
    // region INTEGER INPUT / FORM COMPLETION

    @Test fun `socket count input accepts common and large existing values without truncation`() {
        assertEquals(ParseSocketCountResult.Value(9), parseSocketCountInput("9"))
        assertEquals(ParseSocketCountResult.Value(10), parseSocketCountInput("10"))
        assertEquals(ParseSocketCountResult.Value(99), parseSocketCountInput("99"))
        assertEquals(ParseSocketCountResult.Value(100), parseSocketCountInput("100"))
        assertEquals(ParseSocketCountResult.Value(123), parseSocketCountInput("123"))
        assertEquals("123", formatSocketCountInput(123))
        assertEquals("100", formatSocketCountInput(100))
        assertEquals("", formatSocketCountInput(null))
    }

    @Test fun `clearing socket count input returns unresolved`() {
        assertEquals(ParseSocketCountResult.Cleared, parseSocketCountInput(""))
    }

    @Test fun `invalid socket count input is rejected`() {
        assertEquals(ParseSocketCountResult.Invalid, parseSocketCountInput("12a"))
        assertEquals(ParseSocketCountResult.Invalid, parseSocketCountInput("-1"))
        assertEquals(ParseSocketCountResult.Invalid, parseSocketCountInput("1.5"))
        assertEquals(ParseSocketCountResult.Invalid, parseSocketCountInput(" "))
    }

    @Test fun `existing counts above 99 preload and format exactly`() {
        assertEquals(
            mapOf(TYPE2 to 123, TYPE2_CABLE to 100),
            initialSocketFormCounts(
                mapOf("socket:type2" to "123", "socket:type2_cable" to "100"),
                listOf(TYPE2, TYPE2_CABLE)
            )
        )
        assertEquals(123, increaseSocketCount(123)) // already at stepper max for that value
        assertEquals(122, decreaseSocketCount(123))
        assertEquals(99, increaseSocketCount(98))
        assertEquals(99, increaseSocketCount(99))
        assertEquals(0, decreaseSocketCount(null))
    }

    @Test fun `empty displayed socket types are never form-complete`() {
        assertFalse(isSocketFormComplete(emptyMap(), emptyList()))
        assertFalse(isSocketFormComplete(mapOf(TYPE2 to 1), emptyList()))
        assertFalse(isSocketFormComplete(emptyMap(), listOf(TYPE2)))
        assertFalse(isSocketFormComplete(mapOf(TYPE2 to null), listOf(TYPE2)))
        assertTrue(isSocketFormComplete(mapOf(TYPE2 to 0, TYPE2_CABLE to 1), listOf(TYPE2, TYPE2_CABLE)))
    }

    // endregion
    // region COUNTRY COVERAGE

    @Test fun `DE-like metadata enables full implemented survey with domestic`() {
        assertTrue(hasSupportedSocketTypes(countryInfoDe))
        assertTrue(questType.isApplicableTo(chargingStation())!!)
        val types = socketTypesForCountry(countryInfoDe)
        assertEquals(listOf(TYPE2, TYPE2_CABLE, TYPE2_COMBO, CHADEMO, DOMESTIC), types)
    }

    @Test fun `US-like metadata does not enable a misleading partial survey`() {
        val quest = AddChargingStationSocket { countryInfoUs }
        assertFalse(hasSupportedSocketTypes(countryInfoUs))
        assertEquals(emptyList(), socketTypesForCountry(countryInfoUs))
        assertFalse(quest.isApplicableTo(chargingStation())!!)
    }

    @Test fun `JP-like metadata does not enable a misleading partial survey`() {
        val quest = AddChargingStationSocket { countryInfoJp }
        assertFalse(hasSupportedSocketTypes(countryInfoJp))
        assertEquals(emptyList(), socketTypesForCountry(countryInfoJp))
        assertFalse(quest.isApplicableTo(chargingStation())!!)
    }

    @Test fun `unsupported-only metadata disables the quest`() {
        val quest = AddChargingStationSocket { countryInfoUnsupportedOnly }
        assertFalse(hasSupportedSocketTypes(countryInfoUnsupportedOnly))
        assertFalse(quest.isApplicableTo(chargingStation())!!)
    }

    @Test fun `empty socket metadata disables the quest`() {
        val quest = AddChargingStationSocket { countryInfoEmpty }
        assertFalse(quest.isApplicableTo(chargingStation())!!)
    }

    @Test fun `domestic metadata alone does not make the quest applicable`() {
        val quest = AddChargingStationSocket { countryInfoDomesticOnly }
        assertFalse(quest.isApplicableTo(chargingStation())!!)
        assertEquals(listOf(DomesticPlugType.GB1002), domesticPlugTypesForCountry(countryInfoDomesticOnly))
    }

    @Test fun `domestic is offered for GB and CH-like plug metadata`() {
        assertEquals(listOf(DomesticPlugType.BS1363), domesticPlugTypesForCountry(countryInfoGb))
        assertTrue(DOMESTIC in socketTypesForCountry(countryInfoGb))
        assertEquals(
            listOf(DomesticPlugType.TYPEC, DomesticPlugType.SEV1011_T13),
            domesticPlugTypesForCountry(countryInfoChPlugs)
        )
    }

    @Test fun `domestic is not offered without domesticSocketType metadata`() {
        assertFalse(DOMESTIC in socketTypesForCountry(countryInfoIlNoDomestic))
        assertTrue(AddChargingStationSocket { countryInfoIlNoDomestic }.isApplicableTo(chargingStation())!!)
    }

    @Test fun `unknown domesticSocketType values are ignored`() {
        val country = CountryInfo(
            "XX",
            listOf(IncompleteCountryInfo(
                chargingStationSocketTypes = listOf("type2", "type2_cable", "type2_combo", "chademo"),
                domesticSocketType = listOf("not_a_real_plug", "schuko")
            ))
        )
        assertEquals(listOf(DomesticPlugType.SCHUKO), domesticPlugTypesForCountry(country))
        assertTrue(DOMESTIC in socketTypesForCountry(country))
    }

    // endregion
    // region geometry / charge_point

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

    // endregion
    // region answer tagging

    @Test fun `positive values write numeric counts and zeros write no`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("socket:type2", "2"),
                StringMapEntryAdd("socket:type2_cable", "no"),
                StringMapEntryAdd("check_date:socket", nowAsCheckDateString())
            ),
            questType.answerApplied(mapOf(TYPE2 to 2, TYPE2_CABLE to 0))
        )
    }

    @Test fun `household plug count writes socket domestic not the concrete plug key`() {
        val changes = questType.answerApplied(mapOf(DOMESTIC to 2))
        assertTrue(changes.contains(StringMapEntryAdd("socket:domestic", "2")))
        assertTrue(changes.none { it.key == "socket:schuko" || it.key == "socket:typec" || it.key == "socket:bs1363" })
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
                mapOf("socket:type2" to "2", "socket:type2_cable" to "no")
            )
        )
    }

    @Test fun `cable selected writes numeric count`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("socket:type2", "1"),
                StringMapEntryAdd("socket:type2_cable", "1"),
                StringMapEntryAdd("check_date:socket", nowAsCheckDateString())
            ),
            questType.answerApplied(mapOf(TYPE2 to 1, TYPE2_CABLE to 1))
        )
    }

    // endregion
}

/** Complete DE survey tags (includes domestic). Override individual keys via [overrides]. */
private fun completeDeSurvey(vararg overrides: Pair<String, String>): Array<Pair<String, String>> =
    (mapOf(
        "socket:type2" to "2",
        "socket:type2_cable" to "no",
        "socket:type2_combo" to "1",
        "socket:chademo" to "no",
        "socket:domestic" to "no",
    ) + overrides).toList().toTypedArray()

private fun completeDeSurveyAllNo(): Array<Pair<String, String>> = arrayOf(
    "socket:type2" to "no",
    "socket:type2_cable" to "no",
    "socket:type2_combo" to "no",
    "socket:chademo" to "no",
    "socket:domestic" to "no",
)

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

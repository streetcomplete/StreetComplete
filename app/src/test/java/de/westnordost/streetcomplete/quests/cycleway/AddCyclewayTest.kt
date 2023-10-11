package de.westnordost.streetcomplete.quests.cycleway

import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.quests.TestMapDataWithGeometry
import de.westnordost.streetcomplete.testutils.mock
import de.westnordost.streetcomplete.testutils.on
import de.westnordost.streetcomplete.testutils.pGeom
import de.westnordost.streetcomplete.testutils.way
import de.westnordost.streetcomplete.util.ktx.nowAsEpochMilliseconds
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AddCyclewayTest {

    private lateinit var countryInfo: CountryInfo
    private lateinit var questType: AddCycleway

    @BeforeTest fun setUp() {
        countryInfo = mock()
        questType = AddCycleway { _ -> countryInfo }
    }

    @Test fun `applicable to road with missing cycleway`() {
        val way = way(1L, listOf(1, 2, 3), mapOf(
            "highway" to "primary"
        ))
        val mapData = TestMapDataWithGeometry(listOf(way))

        assertEquals(1, questType.getApplicableElements(mapData).toList().size)
        assertTrue(questType.isApplicableTo(way)!!)
    }

    @Test fun `not applicable to road with cycleway=separate`() {
        for (cyclewayKey in listOf("cycleway", "cycleway:left", "cycleway:right", "cycleway:both")) {
            val way = way(1L, listOf(1, 2, 3), mapOf(
                "highway" to "primary",
                cyclewayKey to "separate"
            ))
            val mapData = TestMapDataWithGeometry(listOf(way))

            assertEquals(0, questType.getApplicableElements(mapData).toList().size)
            assertFalse(questType.isApplicableTo(way)!!)
        }
    }

    @Test fun `not applicable to non-road`() {
        val way = way(tags = mapOf("waterway" to "river"))
        val mapData = TestMapDataWithGeometry(listOf(way))

        assertEquals(0, questType.getApplicableElements(mapData).toList().size)
        assertFalse(questType.isApplicableTo(way)!!)
    }

    @Test fun `not applicable to road with cycleway that is not old enough`() {
        val way = way(1L, listOf(1, 2, 3), mapOf(
            "highway" to "primary",
            "cycleway" to "track"
        ), timestamp = nowAsEpochMilliseconds())
        val mapData = TestMapDataWithGeometry(listOf(way))

        assertEquals(0, questType.getApplicableElements(mapData).toList().size)
        assertFalse(questType.isApplicableTo(way)!!)
    }

    @Test fun `applicable to road with cycleway that is old enough`() {
        val way = way(1L, listOf(1, 2, 3), mapOf(
            "highway" to "primary",
            "cycleway" to "track",
            "check_date:cycleway" to "2001-01-01"
        ), timestamp = nowAsEpochMilliseconds())
        val mapData = TestMapDataWithGeometry(listOf(way))

        assertEquals(1, questType.getApplicableElements(mapData).toList().size)
        assertTrue(questType.isApplicableTo(way)!!)
    }

    @Test fun `not applicable to road with cycleway that is old enough but has unknown cycleway tagging`() {
        val way = way(1L, listOf(1, 2, 3), mapOf(
            "highway" to "primary",
            "cycleway" to "whatsthis",
            "check_date:cycleway" to "2001-01-01"
        ), timestamp = nowAsEpochMilliseconds())
        val mapData = TestMapDataWithGeometry(listOf(way))

        assertEquals(0, questType.getApplicableElements(mapData).toList().size)
        assertFalse(questType.isApplicableTo(way)!!)
    }

    @Test fun `applicable to road with cycleway that is tagged with an invalid value`() {
        val way = way(1L, listOf(1, 2, 3), mapOf(
            "highway" to "primary",
            "cycleway" to "yes",
        ))
        val mapData = TestMapDataWithGeometry(listOf(way))

        assertEquals(1, questType.getApplicableElements(mapData).toList().size)
        assertTrue(questType.isApplicableTo(way)!!)
    }

    @Test fun `not applicable to road with cycleway that is tagged with an unknown + invalid value`() {
        val way = way(1L, listOf(1, 2, 3), mapOf(
            "highway" to "primary",
            "cycleway:left" to "yes", // invalid
            "cycleway:right" to "doorzone2", // unknown
        ))
        val mapData = TestMapDataWithGeometry(listOf(way))

        assertEquals(0, questType.getApplicableElements(mapData).toList().size)
        assertFalse(questType.isApplicableTo(way)!!)
    }

    @Test fun `applicable to road with ambiguous cycleway value`() {
        val way = way(1L, listOf(1, 2, 3), mapOf(
            "highway" to "primary",
            "cycleway" to "shared_lane",
        ))
        val mapData = TestMapDataWithGeometry(listOf(way))

        assertEquals(1, questType.getApplicableElements(mapData).toList().size)
        assertTrue(questType.isApplicableTo(way)!!)
    }

    @Test fun `not applicable to road with ambiguous + unknown cycleway value`() {
        val way = way(1L, listOf(1, 2, 3), mapOf(
            "highway" to "primary",
            "cycleway:left" to "shared_lane",
            "cycleway:right" to "strange",
        ))
        val mapData = TestMapDataWithGeometry(listOf(way))

        assertEquals(0, questType.getApplicableElements(mapData).toList().size)
        assertFalse(questType.isApplicableTo(way)!!)
    }

    @Test fun `applicable to road with ambiguous cycle lane not in Belgium`() {
        val way = way(1L, listOf(1, 2, 3), mapOf(
            "highway" to "primary",
            "cycleway" to "lane",
        ))
        val mapData = TestMapDataWithGeometry(listOf(way))
        mapData.wayGeometriesById[1L] = pGeom(0.0, 0.0)
        on(countryInfo.countryCode).thenReturn("DE")
        on(countryInfo.hasAdvisoryCycleLane).thenReturn(true)

        assertEquals(1, questType.getApplicableElements(mapData).toList().size)
        // because we don't know if we are in Belgium
        assertNull(questType.isApplicableTo(way))
    }

    @Test fun `unspecified cycle lane is not ambiguous in Belgium`() {
        val way = way(1L, listOf(1, 2, 3), mapOf(
            "highway" to "primary",
            "cycleway" to "lane",
        ))
        val mapData = TestMapDataWithGeometry(listOf(way))
        mapData.wayGeometriesById[1L] = pGeom(0.0, 0.0)
        on(countryInfo.countryCode).thenReturn("BE")
        on(countryInfo.hasAdvisoryCycleLane).thenReturn(true)

        assertEquals(0, questType.getApplicableElements(mapData).toList().size)
        // because we don't know if we are in Belgium
        assertNull(questType.isApplicableTo(way))
    }

    @Test
    fun `applicable to maxspeed 30 zone with zone_traffic urban`() {
        val residentialWayIn30Zone = way(1L, listOf(1, 2, 3), mapOf(
            "highway" to "residential",
            "maxspeed" to "30",
            "zone:traffic" to "DE:urban",
            "zone:maxspeed" to "DE:30",
        ))

        val mapData = TestMapDataWithGeometry(listOf(residentialWayIn30Zone))

        assertEquals(1, questType.getApplicableElements(mapData).toList().size)
        assertTrue(questType.isApplicableTo(residentialWayIn30Zone)!!)
    }

    @Test
    fun `applicable to maxspeed 30 in built-up area`() {
        val residentialWayInBuiltUpAreaWithMaxspeed30 = way(tags = mapOf(
            "highway" to "residential",
            "maxspeed" to "30",
            "zone:traffic" to "DE:urban"
        ))

        val mapData = TestMapDataWithGeometry(listOf(residentialWayInBuiltUpAreaWithMaxspeed30))

        assertEquals(1, questType.getApplicableElements(mapData).toList().size)
        assertTrue(questType.isApplicableTo(residentialWayInBuiltUpAreaWithMaxspeed30)!!)
    }

    @Test
    fun `not applicable to residential way in maxspeed 30 zone`() {
        val residentialWayIn30Zone = way(1L, listOf(1, 2, 3), mapOf(
            "highway" to "residential",
            "maxspeed" to "30",
            "zone:maxspeed" to "DE:30",
        ))

        val mapData = TestMapDataWithGeometry(listOf(residentialWayIn30Zone))

        assertEquals(0, questType.getApplicableElements(mapData).toList().size)
        assertFalse(questType.isApplicableTo(residentialWayIn30Zone)!!)
    }
}

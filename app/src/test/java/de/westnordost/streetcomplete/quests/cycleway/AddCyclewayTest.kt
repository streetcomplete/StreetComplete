package de.westnordost.streetcomplete.quests.cycleway

import de.westnordost.countryboundaries.CountryBoundaries
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.meta.CountryInfos
import de.westnordost.streetcomplete.data.meta.getByLocation
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.quests.TestMapDataWithGeometry
import de.westnordost.streetcomplete.testutils.mock
import de.westnordost.streetcomplete.testutils.on
import de.westnordost.streetcomplete.testutils.p
import de.westnordost.streetcomplete.testutils.pGeom
import de.westnordost.streetcomplete.testutils.way
import de.westnordost.streetcomplete.util.ktx.nowAsEpochMilliseconds
import de.westnordost.streetcomplete.util.math.translate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyDouble
import java.util.concurrent.FutureTask

class AddCyclewayTest {

    private lateinit var countryInfo: CountryInfo
    private lateinit var countryInfos: CountryInfos
    private lateinit var questType: AddCycleway

    @Before fun setUp() {
        val countryBoundaries: CountryBoundaries = mock()
        val futureTask = FutureTask { countryBoundaries }
        futureTask.run()

        countryInfo = mock()
        countryInfos = mock()
        on(countryInfos.getByLocation(countryBoundaries, anyDouble(), anyDouble())).thenReturn(countryInfo)

        questType = AddCycleway(countryInfos, futureTask)
    }

    @Test fun `applicable to road with missing cycleway`() {
        val way = way(1L, listOf(1, 2, 3), mapOf(
            "highway" to "primary"
        ))
        val mapData = TestMapDataWithGeometry(listOf(way))

        assertEquals(1, questType.getApplicableElements(mapData).toList().size)
        // because geometry/surroundings are not known
        assertNull(questType.isApplicableTo(way))
    }

    @Test fun `not applicable to road with nearby cycleway`() {
        val mapData = TestMapDataWithGeometry(listOf(
            way(1L, listOf(1, 2), mapOf(
                "highway" to "primary",
                "width" to "18"
            )),
            way(2L, listOf(3, 4), mapOf(
                "highway" to "cycleway"
            ))
        ))
        val p1 = p(0.0, 0.0)
        val p2 = p1.translate(50.0, 45.0)
        val p3 = p1.translate(12.999, 135.0)
        val p4 = p3.translate(50.0, 45.0)

        mapData.wayGeometriesById[1L] = ElementPolylinesGeometry(listOf(listOf(p1, p2)), p1)
        mapData.wayGeometriesById[2L] = ElementPolylinesGeometry(listOf(listOf(p3, p4)), p3)

        assertEquals(0, questType.getApplicableElements(mapData).toList().size)
    }

    @Test fun `applicable to road with nearby cycleway that is not aligned to the road`() {
        val mapData = TestMapDataWithGeometry(listOf(
            way(1L, listOf(1, 2), mapOf(
                "highway" to "primary",
                "width" to "18"
            )),
            way(2L, listOf(3, 4), mapOf(
                "highway" to "cycleway"
            ))
        ))
        val p1 = p(0.0, 0.0)
        val p2 = p1.translate(50.0, 45.0)
        val p3 = p1.translate(14.0, 135.0)
        val p4 = p3.translate(50.0, 75.0)

        mapData.wayGeometriesById[1L] = ElementPolylinesGeometry(listOf(listOf(p1, p2)), p1)
        mapData.wayGeometriesById[2L] = ElementPolylinesGeometry(listOf(listOf(p3, p4)), p3)

        assertEquals(1, questType.getApplicableElements(mapData).toList().size)
    }

    @Test fun `applicable to road with cycleway that is far away enough`() {
        val mapData = TestMapDataWithGeometry(listOf(
            way(1L, listOf(1, 2), mapOf(
                "highway" to "primary",
                "width" to "18"
            )),
            way(2L, listOf(3, 4), mapOf(
                "highway" to "cycleway"
            ))
        ))
        val p1 = p(0.0, 0.0)
        val p2 = p1.translate(50.0, 45.0)
        val p3 = p1.translate(16.0, 135.0)
        val p4 = p3.translate(50.0, 45.0)

        mapData.wayGeometriesById[1L] = ElementPolylinesGeometry(listOf(listOf(p1, p2)), p1)
        mapData.wayGeometriesById[2L] = ElementPolylinesGeometry(listOf(listOf(p3, p4)), p3)

        assertEquals(1, questType.getApplicableElements(mapData).toList().size)
    }

    @Test fun `applicable to small road with cycleway that is far away enough`() {
        val mapData = TestMapDataWithGeometry(listOf(
            way(1L, listOf(1, 2), mapOf(
                "highway" to "primary",
                "lanes" to "2"
            )),
            way(2L, listOf(3, 4), mapOf(
                "highway" to "cycleway"
            ))
        ))
        val p1 = p(0.0, 0.0)
        val p2 = p1.translate(50.0, 45.0)
        val p3 = p1.translate(10.0, 135.0)
        val p4 = p3.translate(50.0, 45.0)

        mapData.wayGeometriesById[1L] = ElementPolylinesGeometry(listOf(listOf(p1, p2)), p1)
        mapData.wayGeometriesById[2L] = ElementPolylinesGeometry(listOf(listOf(p3, p4)), p3)

        assertEquals(1, questType.getApplicableElements(mapData).toList().size)
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
}

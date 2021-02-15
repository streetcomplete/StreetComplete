package de.westnordost.streetcomplete.quests.roof_shape

import de.westnordost.osmapi.map.data.OsmLatLon
import de.westnordost.osmapi.map.data.OsmWay
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.meta.CountryInfos
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementPointGeometry
import de.westnordost.streetcomplete.ktx.containsExactlyInAnyOrder
import de.westnordost.streetcomplete.mock
import de.westnordost.streetcomplete.on
import de.westnordost.streetcomplete.quests.TestMapDataWithGeometry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyDouble

class AddRoofShapeTest {
    private lateinit var countryInfos: CountryInfos
    private lateinit var questType: AddRoofShape

    @Before fun setUp() {
        countryInfos = mock()
        questType = AddRoofShape(countryInfos)
    }

    @Test fun `not applicable to roofs with shapes already set`() {
        assertEquals(false, questType.isApplicableTo(
            OsmWay(1L, 1, listOf(), mapOf("roof:levels" to "1", "roof:shape" to "something"))
        ))
    }

    @Test fun `applicable to roofs`() {
        assertEquals(true, questType.isApplicableTo(
            OsmWay(1L, 1, listOf(), mapOf("roof:levels" to "1"))
        ))
    }

    @Test fun `unknown if applicable to roofs with 0 roof levels`() {
        assertEquals(null, questType.isApplicableTo(
            OsmWay(1L, 1, listOf(), mapOf("roof:levels" to "0"))
        ))
    }

    @Test fun `unknown if applicable to roofs with no roof levels tag`() {
        assertEquals(null, questType.isApplicableTo(
            OsmWay(1L, 1, listOf(), mapOf("building:levels" to "5"))
        ))
    }

    @Test fun `create quest for roofs`() {
        val element = OsmWay(1L, 1, listOf(), mapOf("roof:levels" to "1"))

        val quests = questType.getApplicableElements(TestMapDataWithGeometry(listOf(element)))

        assertEquals(element, quests.single())
    }

    @Test fun `create quest for 0 or null-level roofs only in countries with no flat roofs`() {
        val noFlatRoofs = CountryInfo()
        val field = noFlatRoofs.javaClass.getDeclaredField("roofsAreUsuallyFlat")
        field.isAccessible = true
        field.set(noFlatRoofs, false)

        on(countryInfos.get(anyDouble(), anyDouble())).thenReturn(noFlatRoofs)
        val element = OsmWay(1L, 1, listOf(), mapOf("roof:levels" to "0"))
        val element2 = OsmWay(2L, 1, listOf(), mapOf("building:levels" to "3"))

        val mapData = TestMapDataWithGeometry(listOf(element, element2))
        mapData.wayGeometriesById[1L] = ElementPointGeometry(OsmLatLon(0.0,0.0))
        mapData.wayGeometriesById[2L] = ElementPointGeometry(OsmLatLon(0.0,0.0))

        val quests = questType.getApplicableElements(mapData)

        assertTrue(quests.containsExactlyInAnyOrder(listOf(element, element2)))
    }

    @Test fun `create quest for 0 or null-level roofs not in countries with flat roofs`() {
        val flatRoofs = CountryInfo()
        val field = flatRoofs.javaClass.getDeclaredField("roofsAreUsuallyFlat")
        field.isAccessible = true
        field.set(flatRoofs, true)

        on(countryInfos.get(anyDouble(), anyDouble())).thenReturn(flatRoofs)
        val element = OsmWay(1L, 1, listOf(), mapOf("roof:levels" to "0"))
        val element2 = OsmWay(2L, 1, listOf(), mapOf("building:levels" to "3"))

        val mapData = TestMapDataWithGeometry(listOf(element, element2))
        mapData.wayGeometriesById[1L] = ElementPointGeometry(OsmLatLon(0.0,0.0))
        mapData.wayGeometriesById[2L] = ElementPointGeometry(OsmLatLon(0.0,0.0))

        val quests = questType.getApplicableElements(mapData)

        assertTrue(quests.isEmpty())
    }
}

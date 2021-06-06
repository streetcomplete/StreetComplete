package de.westnordost.streetcomplete.quests.roof_shape

import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.meta.CountryInfos
import de.westnordost.streetcomplete.ktx.containsExactlyInAnyOrder
import de.westnordost.streetcomplete.quests.TestMapDataWithGeometry
import de.westnordost.streetcomplete.testutils.mock
import de.westnordost.streetcomplete.testutils.on
import de.westnordost.streetcomplete.testutils.pGeom
import de.westnordost.streetcomplete.testutils.way
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
            way(tags = mapOf("roof:levels" to "1", "roof:shape" to "something"))
        ))
    }

    @Test fun `not applicable to building parts`() {
        assertEquals(false, questType.isApplicableTo(
            way(tags = mapOf("building:levels" to "1", "building:part" to "something"))
        ))
    }

    @Test fun `not applicable to demolished building`() {
        assertEquals(false, questType.isApplicableTo(
            way(tags = mapOf("building:levels" to "1", "demolished:building" to "something"))
        ))
    }

    @Test fun `not applicable to negated building`() {
        assertEquals(false, questType.isApplicableTo(
            way(tags = mapOf("building:levels" to "1", "building" to "no"))
        ))
    }

    @Test fun `not applicable to building under contruction`() {
        assertEquals(false, questType.isApplicableTo(
            way(tags = mapOf("building:levels" to "1", "building" to "construction"))
        ))
    }

    @Test fun `applicable to roofs`() {
        assertEquals(true, questType.isApplicableTo(
            way(tags = mapOf("roof:levels" to "1", "building" to "apartments"))
        ))
    }

    @Test fun `not applicable to buildings with many levels and few or no roof levels`() {
        assertEquals(false, questType.isApplicableTo(
            way(tags = mapOf("building:levels" to "6.5", "roof:levels" to "1", "building" to "apartments"))
        ))
        assertEquals(false, questType.isApplicableTo(
            way(tags = mapOf("building:levels" to "8", "roof:levels" to "2", "building" to "apartments"))
        ))
        assertEquals(false, questType.isApplicableTo(
            way(tags = mapOf("building:levels" to "5", "building" to "apartments"))
        ))
        assertEquals(false, questType.isApplicableTo(
            way(tags = mapOf("building:levels" to "4", "roof:levels" to "0", "building" to "apartments"))
        ))
    }

    @Test fun `applicable to buildings with many levels and enough roof levels to be visible from below`() {
        assertEquals(true, questType.isApplicableTo(
            way(tags = mapOf("building:levels" to "6", "roof:levels" to "1.5", "building" to "apartments"))
        ))
        assertEquals(true, questType.isApplicableTo(
            way(tags = mapOf("building:levels" to "8", "roof:levels" to "3", "building" to "apartments"))
        ))
        assertEquals(true, questType.isApplicableTo(
            way(tags = mapOf("building:levels" to "4.5", "roof:levels" to "0.5", "building" to "apartments"))
        ))
    }

    @Test fun `unknown if applicable to buildings with no or few levels and 0 or no roof levels`() {
        assertEquals(null, questType.isApplicableTo(
            way(tags = mapOf("roof:levels" to "0", "building" to "apartments"))
        ))
        assertEquals(null, questType.isApplicableTo(
            way(tags = mapOf("roof:levels" to "0", "building" to "apartments", "building:levels" to "3"))
        ))
        assertEquals(null, questType.isApplicableTo(
            way(tags = mapOf("building" to "apartments", "building:levels" to "2"))
        ))
    }

    @Test fun `create quest for roofs`() {
        val element = way(tags = mapOf("roof:levels" to "1", "building" to "apartments"))

        val quests = questType.getApplicableElements(TestMapDataWithGeometry(listOf(element)))

        assertEquals(element, quests.single())
    }

    @Test fun `do not create quest for buildings with too many levels`() {
        val element = way(tags = mapOf("building:levels" to "6", "roof:levels" to "1", "building" to "apartments"))

        val quests = questType.getApplicableElements(TestMapDataWithGeometry(listOf(element)))

        assertEquals(true, quests.isEmpty())
    }

    @Test fun `create quest for 0 or null-level roofs only in countries with no flat roofs`() {
        val noFlatRoofs = CountryInfo()
        val field = noFlatRoofs.javaClass.getDeclaredField("roofsAreUsuallyFlat")
        field.isAccessible = true
        field.set(noFlatRoofs, false)

        on(countryInfos.get(anyDouble(), anyDouble())).thenReturn(noFlatRoofs)

        val element = way(1, tags = mapOf("roof:levels" to "0", "building" to "apartments"))
        val element2 = way(2, tags = mapOf("building:levels" to "3", "building" to "apartments"))

        val mapData = TestMapDataWithGeometry(listOf(element, element2))
        mapData.wayGeometriesById[1L] = pGeom()
        mapData.wayGeometriesById[2L] = pGeom()

        val quests = questType.getApplicableElements(mapData)

        assertTrue(quests.containsExactlyInAnyOrder(listOf(element, element2)))
    }

    @Test fun `create quest for 0 or null-level roofs not in countries with flat roofs`() {
        val flatRoofs = CountryInfo()
        val field = flatRoofs.javaClass.getDeclaredField("roofsAreUsuallyFlat")
        field.isAccessible = true
        field.set(flatRoofs, true)

        on(countryInfos.get(anyDouble(), anyDouble())).thenReturn(flatRoofs)
        val element = way(1, tags = mapOf("roof:levels" to "0", "building" to "apartments"))
        val element2 = way(1, tags = mapOf("building:levels" to "3", "building" to "apartments"))

        val mapData = TestMapDataWithGeometry(listOf(element, element2))
        mapData.wayGeometriesById[1L] = pGeom()
        mapData.wayGeometriesById[2L] = pGeom()

        val quests = questType.getApplicableElements(mapData)

        assertTrue(quests.isEmpty())
    }
}

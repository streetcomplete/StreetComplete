package de.westnordost.streetcomplete.quests.address

import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.quests.TestMapDataWithGeometry
import de.westnordost.streetcomplete.testutils.member
import de.westnordost.streetcomplete.testutils.node
import de.westnordost.streetcomplete.testutils.rel
import de.westnordost.streetcomplete.testutils.way
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AddAddressStreetTest {

    private val questType = AddAddressStreet()

    @Test fun `applicable to place without street name`() {
        val addr = node(tags = mapOf("addr:housenumber" to "123"))
        val mapData = TestMapDataWithGeometry(listOf(addr))
        assertEquals(1, questType.getApplicableElements(mapData).toList().size)
        assertNull(questType.isApplicableTo(addr))
    }

    @Test fun `not applicable to place with street name`() {
        val addr = node(tags = mapOf(
            "addr:housenumber" to "123",
            "addr:street" to "onetwothree",
        ))
        val mapData = TestMapDataWithGeometry(listOf(addr))
        assertEquals(0, questType.getApplicableElements(mapData).toList().size)
        assertEquals(false, questType.isApplicableTo(addr))
    }

    @Test fun `not applicable to place with substreet name`() {
        val addr = node(tags = mapOf(
            "addr:housenumber" to "123",
            "addr:substreet" to "onetwothree",
        ))
        val mapData = TestMapDataWithGeometry(listOf(addr))
        assertEquals(0, questType.getApplicableElements(mapData).toList().size)
        assertEquals(false, questType.isApplicableTo(addr))
    }

    @Test fun `not applicable to place with parentstreet name`() {
        val addr = node(tags = mapOf(
            "addr:housenumber" to "123",
            "addr:parentstreet" to "onetwothree",
        ))
        val mapData = TestMapDataWithGeometry(listOf(addr))
        assertEquals(0, questType.getApplicableElements(mapData).toList().size)
        assertEquals(false, questType.isApplicableTo(addr))
    }

    @Test fun `not applicable to place without street name but in a associatedStreet relation`() {
        val addr = node(1, tags = mapOf("addr:housenumber" to "123"))
        val associatedStreetRelation = rel(
            members = listOf(member(ElementType.NODE, 1)),
            tags = mapOf("type" to "associatedStreet")
        )

        val mapData = TestMapDataWithGeometry(listOf(addr, associatedStreetRelation))
        assertEquals(0, questType.getApplicableElements(mapData).toList().size)
        assertNull(questType.isApplicableTo(addr))
    }

    @Test fun `applicable to place in interpolation without street name`() {
        val addr = node(tags = mapOf("addr:housenumber" to "123"))
        val addrInterpolation = way(nodes = listOf(1, 2, 3), tags = mapOf(
            "addr:interpolation" to "whatever",
        ))
        val mapData = TestMapDataWithGeometry(listOf(addr, addrInterpolation))
        assertEquals(1, questType.getApplicableElements(mapData).toList().size)
        assertNull(questType.isApplicableTo(addr))
    }

    @Test fun `not applicable to place in interpolation with street name`() {
        val addr = node(tags = mapOf("addr:housenumber" to "123"))
        val addrInterpolation = way(nodes = listOf(1, 2, 3), tags = mapOf(
            "addr:interpolation" to "whatever",
            "addr:street" to "Street Name"
        ))
        val mapData = TestMapDataWithGeometry(listOf(addr, addrInterpolation))
        assertEquals(0, questType.getApplicableElements(mapData).toList().size)
        assertNull(questType.isApplicableTo(addr))
    }
}

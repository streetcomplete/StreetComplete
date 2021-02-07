package de.westnordost.streetcomplete.quests.address

import de.westnordost.osmapi.map.data.*
import de.westnordost.streetcomplete.quests.TestMapDataWithGeometry
import org.junit.Assert.*
import org.junit.Test

class AddAddressStreetTest {

    private val questType = AddAddressStreet()

    @Test fun `applicable to place without street name`() {
        val addr = OsmNode(1L, 1, 0.0,0.0, mapOf(
            "addr:housenumber" to "123"
        ))
        val mapData = TestMapDataWithGeometry(listOf(addr))
        assertEquals(1, questType.getApplicableElements(mapData).toList().size)
        assertNull(questType.isApplicableTo(addr))
    }

    @Test fun `not applicable to place with street name`() {
        val addr = OsmNode(1L, 1, 0.0,0.0, mapOf(
            "addr:housenumber" to "123",
            "addr:street" to "onetwothree",
        ))
        val mapData = TestMapDataWithGeometry(listOf(addr))
        assertEquals(0, questType.getApplicableElements(mapData).toList().size)
        assertEquals(false, questType.isApplicableTo(addr))
    }

    @Test fun `not applicable to place without street name but in a associatedStreet relation`() {
        val addr = OsmNode(1L, 1, 0.0,0.0, mapOf(
            "addr:housenumber" to "123"
        ))
        val associatedStreetRelation = OsmRelation(1L, 1, listOf(
            OsmRelationMember(1L, "doesntmatter", Element.Type.NODE)
        ), mapOf(
            "type" to "associatedStreet"
        ))

        val mapData = TestMapDataWithGeometry(listOf(addr, associatedStreetRelation))
        assertEquals(0, questType.getApplicableElements(mapData).toList().size)
        assertNull(questType.isApplicableTo(addr))
    }

    @Test fun `applicable to place in interpolation without street name`() {
        val addr = OsmNode(1L, 1, 0.0,0.0, mapOf(
            "addr:housenumber" to "123"
        ))
        val addrInterpolation = OsmWay(1L, 1, listOf(1,2,3), mapOf(
            "addr:interpolation" to "whatever",
        ))
        val mapData = TestMapDataWithGeometry(listOf(addr, addrInterpolation))
        assertEquals(1, questType.getApplicableElements(mapData).toList().size)
        assertNull(questType.isApplicableTo(addr))
    }

    @Test fun `not applicable to place in interpolation with street name`() {
        val addr = OsmNode(1L, 1, 0.0,0.0, mapOf(
            "addr:housenumber" to "123"
        ))
        val addrInterpolation = OsmWay(1L, 1, listOf(1,2,3), mapOf(
            "addr:interpolation" to "whatever",
            "addr:street" to "Street Name"
        ))
        val mapData = TestMapDataWithGeometry(listOf(addr, addrInterpolation))
        assertEquals(0, questType.getApplicableElements(mapData).toList().size)
        assertNull(questType.isApplicableTo(addr))
    }
}

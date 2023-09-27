package de.westnordost.streetcomplete.quests.address

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryModify
import de.westnordost.streetcomplete.data.osm.geometry.ElementPointGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolygonsGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.data.osm.mapdata.Way
import de.westnordost.streetcomplete.osm.address.ConscriptionNumber
import de.westnordost.streetcomplete.osm.address.HouseAndBlockNumber
import de.westnordost.streetcomplete.osm.address.HouseNumber
import de.westnordost.streetcomplete.quests.createMapData
import de.westnordost.streetcomplete.quests.verifyAnswer
import de.westnordost.streetcomplete.testutils.member
import de.westnordost.streetcomplete.testutils.node
import de.westnordost.streetcomplete.testutils.p
import de.westnordost.streetcomplete.testutils.rel
import de.westnordost.streetcomplete.testutils.way
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AddHousenumberTest {

    private val questType = AddHousenumber()

    @Test fun `does not create quest for generic building`() {
        val building = way(1L, NODES1, mapOf("building" to "yes"))
        val mapData = createMapData(mapOf(building to POSITIONS1))
        assertEquals(0, questType.getApplicableElements(mapData).toList().size)
        assertEquals(false, questType.isApplicableTo(building))
    }

    @Test fun `does not create quest for building with address`() {
        val building = way(1L, NODES1, mapOf(
            "building" to "detached",
            "addr:housenumber" to "123"
        ))
        val mapData = createMapData(mapOf(building to POSITIONS1))
        assertEquals(0, questType.getApplicableElements(mapData).toList().size)
        assertEquals(false, questType.isApplicableTo(building))
    }

    @Test fun `does create quest for building without address`() {
        val building = way(1L, NODES1, mapOf(
            "building" to "detached"
        ))
        val mapData = createMapData(mapOf(building to POSITIONS1))
        assertEquals(1, questType.getApplicableElements(mapData).toList().size)
        assertNull(questType.isApplicableTo(building))
    }

    @Test fun `does not create quest for building with address node on outline`() {
        val building = way(1, NODES1, mapOf(
            "building" to "detached"
        ))
        val addr = node(2, P2, mapOf(
            "addr:housenumber" to "123"
        ))
        val mapData = createMapData(mapOf(
            building to POSITIONS1,
            addr to ElementPointGeometry(P2)
        ))
        assertEquals(0, questType.getApplicableElements(mapData).toList().size)
        assertNull(questType.isApplicableTo(building))
    }

    @Test fun `does not create quest for building that is part of a relation with an address`() {
        val building = way(1, NODES1, mapOf(
            "building" to "detached"
        ))
        val relationWithAddr = rel(
            members = listOf(member(ElementType.WAY, 1)),
            tags = mapOf("addr:housenumber" to "123")
        )

        val mapData = createMapData(mapOf(
            building to POSITIONS1,
            relationWithAddr to ElementPointGeometry(P2)
        ))
        assertEquals(0, questType.getApplicableElements(mapData).toList().size)
        assertNull(questType.isApplicableTo(building))
    }

    @Test fun `does not create quest for building that is inside an area with an address`() {
        val building = way(1L, NODES1, mapOf(
            "building" to "detached"
        ))
        val areaWithAddr = way(1L, NODES2, mapOf(
            "addr:housenumber" to "123",
            "amenity" to "school",
        ))
        val mapData = createMapData(mapOf(
            building to POSITIONS1,
            areaWithAddr to POSITIONS2,
        ))
        assertEquals(0, questType.getApplicableElements(mapData).toList().size)
        assertNull(questType.isApplicableTo(building))
    }

    @Test fun `does not create quest for building that is inside an area with an address on its outline`() {
        val mapData = createMapData(mapOf(
            Way(1L, NODES1, mapOf(
                "building" to "detached"
            ), 1) to POSITIONS1,
            Way(1L, NODES2, mapOf(
                "amenity" to "school"
            ), 1) to POSITIONS2,
            Node(6L, P6, mapOf(
                "addr:housenumber" to "123"
            ), 1) to ElementPointGeometry(P6)
        ))
        assertEquals(0, questType.getApplicableElements(mapData).toList().size)
    }

    @Test fun `does not create quest for building that contains an address node`() {
        val building = way(1L, NODES1, mapOf(
            "building" to "detached"
        ))
        val addr = node(1L, PC, mapOf(
            "addr:housenumber" to "123"
        ))
        val mapData = createMapData(mapOf(
            building to POSITIONS1,
            addr to ElementPointGeometry(PC),
        ))
        assertEquals(0, questType.getApplicableElements(mapData).toList().size)
        assertNull(questType.isApplicableTo(building))
    }

    @Test fun `does not create quest for building that intersects bounding box`() {
        val building = way(1L, NODES1, mapOf(
            "building" to "detached"
        ))
        val mapData = createMapData(mapOf(
            building to ElementPolygonsGeometry(listOf(listOf(P1, P2, PO, P4, P1)), PC)
        ))
        assertEquals(0, questType.getApplicableElements(mapData).toList().size)
        assertNull(questType.isApplicableTo(building))
    }

    @Test fun `apply house number answer`() {
        questType.verifyAnswer(
            AddressNumberOrName(HouseNumber("99b"), null),
            StringMapEntryAdd("addr:housenumber", "99b")
        )
    }

    @Test fun `apply house name answer`() {
        questType.verifyAnswer(
            AddressNumberOrName(null, "La Escalera"),
            StringMapEntryAdd("addr:housename", "La Escalera")
        )
    }

    @Test fun `apply conscription number answer`() {
        questType.verifyAnswer(
            AddressNumberOrName(ConscriptionNumber("I.123"), null),
            StringMapEntryAdd("addr:conscriptionnumber", "I.123"),
            StringMapEntryAdd("addr:housenumber", "I.123")
        )
    }

    @Test fun `apply conscription and street number answer`() {
        questType.verifyAnswer(
            AddressNumberOrName(ConscriptionNumber("I.123", "12b"), null),
            StringMapEntryAdd("addr:conscriptionnumber", "I.123"),
            StringMapEntryAdd("addr:streetnumber", "12b"),
            StringMapEntryAdd("addr:housenumber", "12b")
        )
    }

    @Test fun `apply block and house number answer`() {
        questType.verifyAnswer(
            AddressNumberOrName(HouseAndBlockNumber("12A", "123"), null),
            StringMapEntryAdd("addr:block_number", "123"),
            StringMapEntryAdd("addr:housenumber", "12A")
        )
    }

    @Test fun `apply no house number answer`() {
        questType.verifyAnswer(
            AddressNumberOrName(null, null),
            StringMapEntryAdd("nohousenumber", "yes")
        )
    }

    @Test fun `apply wrong building type answer`() {
        questType.verifyAnswer(
            mapOf("building" to "residential"),
            WrongBuildingType,
            StringMapEntryModify("building", "residential", "yes")
        )
    }
}

private val P1 = p(0.25, 0.25)
private val P2 = p(0.25, 0.75)
private val P3 = p(0.75, 0.75)
private val P4 = p(0.75, 0.25)

private val P5 = p(0.1, 0.1)
private val P6 = p(0.1, 0.9)
private val P7 = p(0.9, 0.9)
private val P8 = p(0.9, 0.1)

private val PO = p(1.5, 1.5)
private val PC = p(0.5, 0.5)

private val NODES1 = listOf<Long>(1, 2, 3, 4, 1)
private val NODES2 = listOf<Long>(5, 6, 7, 8, 5)

private val POSITIONS1 = ElementPolygonsGeometry(listOf(listOf(P1, P2, P3, P4, P1)), PC)
private val POSITIONS2 = ElementPolygonsGeometry(listOf(listOf(P5, P6, P7, P8, P5)), PC)

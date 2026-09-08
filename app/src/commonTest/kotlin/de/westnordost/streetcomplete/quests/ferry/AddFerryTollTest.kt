package de.westnordost.streetcomplete.quests.ferry

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.quests.answerApplied
import de.westnordost.streetcomplete.testutils.TestMapDataWithGeometry
import de.westnordost.streetcomplete.testutils.member
import de.westnordost.streetcomplete.testutils.node
import de.westnordost.streetcomplete.testutils.rel
import de.westnordost.streetcomplete.testutils.way
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AddFerryTollTest {
    private val questType = AddFerryToll()

    @Test fun `applicable to ferry way`() {
        val ferry = way(tags = mapOf("route" to "ferry"))
        val mapData = TestMapDataWithGeometry(listOf(ferry))
        assertEquals(1, questType.getApplicableElements(mapData).toList().size)
        assertNull(questType.isApplicableTo(ferry))
    }

    @Test fun `applicable to ferry relation`() {
        val ferry = rel(tags = mapOf("route" to "ferry"))
        val mapData = TestMapDataWithGeometry(listOf(ferry))
        assertEquals(1, questType.getApplicableElements(mapData).toList().size)
        assertEquals(true, questType.isApplicableTo(ferry))
    }

    @Test fun `not applicable to non-ferry`() {
        val highway = way(tags = mapOf("highway" to "residential"))
        val mapData = TestMapDataWithGeometry(listOf(highway))
        assertEquals(0, questType.getApplicableElements(mapData).toList().size)
        assertEquals(false, questType.isApplicableTo(highway))
    }

    @Test fun `not applicable to nodes`() {
        val ferryNode = node(tags = mapOf("route" to "ferry"))
        val mapData = TestMapDataWithGeometry(listOf(ferryNode))
        assertEquals(0, questType.getApplicableElements(mapData).toList().size)
        assertEquals(false, questType.isApplicableTo(ferryNode))
    }

    @Test fun `not applicable to ferry way that is a member of a ferry route relation`() {
        val ferryWay = way(id = 1, tags = mapOf("route" to "ferry"))
        val ferryRelation = rel(
            id = 1,
            members = listOf(member(ElementType.WAY, 1L)),
            tags = mapOf("route" to "ferry")
        )
        val mapData = TestMapDataWithGeometry(listOf(ferryWay, ferryRelation))
        val applicable = questType.getApplicableElements(mapData).toList()
        assertEquals(1, applicable.size)
        assertEquals(ferryRelation, applicable.single())
        assertNull(questType.isApplicableTo(ferryWay))
    }

    @Test fun `not applicable when toll is yes`() {
        val ferry = way(tags = mapOf("route" to "ferry", "toll" to "yes"))
        val mapData = TestMapDataWithGeometry(listOf(ferry))
        assertEquals(0, questType.getApplicableElements(mapData).toList().size)
        assertEquals(false, questType.isApplicableTo(ferry))
    }

    @Test fun `not applicable when toll is no`() {
        val ferry = way(tags = mapOf("route" to "ferry", "toll" to "no"))
        val mapData = TestMapDataWithGeometry(listOf(ferry))
        assertEquals(0, questType.getApplicableElements(mapData).toList().size)
        assertEquals(false, questType.isApplicableTo(ferry))
    }

    @Test fun `not applicable when fee is set`() {
        val ferryYes = way(tags = mapOf("route" to "ferry", "fee" to "yes"))
        val ferryNo = way(tags = mapOf("route" to "ferry", "fee" to "no"))
        val mapData = TestMapDataWithGeometry(listOf(ferryYes, ferryNo))
        assertEquals(0, questType.getApplicableElements(mapData).toList().size)
        assertEquals(false, questType.isApplicableTo(ferryYes))
        assertEquals(false, questType.isApplicableTo(ferryNo))
    }

    @Test fun `not applicable when toll subkeys are set`() {
        val ferry = way(tags = mapOf("route" to "ferry", "toll:motorcar" to "yes"))
        val mapData = TestMapDataWithGeometry(listOf(ferry))
        assertEquals(0, questType.getApplicableElements(mapData).toList().size)
        assertEquals(false, questType.isApplicableTo(ferry))
    }

    @Test fun `not applicable when fee subkeys are set`() {
        val ferry = way(tags = mapOf("route" to "ferry", "fee:foot" to "no"))
        val mapData = TestMapDataWithGeometry(listOf(ferry))
        assertEquals(0, questType.getApplicableElements(mapData).toList().size)
        assertEquals(false, questType.isApplicableTo(ferry))
    }

    @Test fun `not applicable for other transport-specific toll or fee subkeys`() {
        val bicycleToll = way(id = 1, tags = mapOf("route" to "ferry", "toll:bicycle" to "no"))
        val hgvFee = way(id = 2, tags = mapOf("route" to "ferry", "fee:hgv" to "yes"))
        val conditionalFee = way(id = 3, tags = mapOf("route" to "ferry", "fee:conditional" to "no @ (age < 6)"))
        val mapData = TestMapDataWithGeometry(listOf(bicycleToll, hgvFee, conditionalFee))
        assertEquals(0, questType.getApplicableElements(mapData).toList().size)
        assertEquals(false, questType.isApplicableTo(bicycleToll))
        assertEquals(false, questType.isApplicableTo(hgvFee))
        assertEquals(false, questType.isApplicableTo(conditionalFee))
    }

    @Test fun `apply yes answer`() {
        assertEquals(
            setOf(StringMapEntryAdd("toll", "yes")),
            questType.answerApplied(true)
        )
    }

    @Test fun `apply no answer`() {
        assertEquals(
            setOf(StringMapEntryAdd("toll", "no")),
            questType.answerApplied(false)
        )
    }
}

package de.westnordost.streetcomplete.data.osm.osmquests

import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.Element
import de.westnordost.osmapi.map.data.LatLon
import de.westnordost.osmapi.map.data.OsmLatLon
import de.westnordost.streetcomplete.data.ApplicationDbTestCase
import de.westnordost.streetcomplete.data.osm.geometry.*
import de.westnordost.streetcomplete.data.quest.QuestType
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.ktx.containsExactlyInAnyOrder
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class OsmQuestDaoTest : ApplicationDbTestCase() {
    private lateinit var geometryDao: ElementGeometryDao
    private lateinit var dao: OsmQuestDao

    @Before fun createDao() {
        val elementGeometryEntryMapping = ElementGeometryEntryMapping(ElementGeometryMapping(serializer))
        geometryDao = ElementGeometryDao(dbHelper, elementGeometryEntryMapping)
        val list = listOf<QuestType<*>>(TEST_QUEST_TYPE, TEST_QUEST_TYPE2)
        dao = OsmQuestDao(dbHelper, NewOsmQuestMapping(QuestTypeRegistry(list), elementGeometryEntryMapping.geometryMapping))
    }

    @Test fun addGet() {
        assertNull(dao.get(1L))
        val q = addToDaos(TEST_QUEST_TYPE, Element.Type.NODE, 12L)
        assertNotNull(q.id)
        assertEquals(q, dao.get(q.id!!))
        assertEquals(q, dao.get(OsmQuestKey(Element.Type.NODE, 12L, TEST_QUEST_TYPE.javaClass.simpleName)))
    }

    @Test fun delete() {
        val q = addToDaos(TEST_QUEST_TYPE)
        dao.delete(123L)
        assertNotNull(q.id)
        dao.delete(q.id!!)
        assertNull(dao.get(q.id!!))
    }

    @Test fun deleteAll() {
        val q1 = addToDaos(TEST_QUEST_TYPE)
        val q2 = addToDaos(TEST_QUEST_TYPE2)
        val q3 = addToDaos(TEST_QUEST_TYPE, elementId = 1)
        dao.deleteAll(listOf(q1.id!!, q2.id!!))

        assertNull(dao.get(q1.id!!))
        assertNull(dao.get(q2.id!!))
        assertEquals(q3, dao.get(q3.id!!))
    }

    @Test fun getAllForElement() {
        val q1 = addToDaos(TEST_QUEST_TYPE, Element.Type.NODE, 0L)
        val q2 = addToDaos(TEST_QUEST_TYPE2, Element.Type.NODE, 0L)
        addToDaos(TEST_QUEST_TYPE, Element.Type.WAY, 0L)
        addToDaos(TEST_QUEST_TYPE, Element.Type.NODE, 1L)
        assertTrue(dao.getAllForElement(Element.Type.NODE, 0L).containsExactlyInAnyOrder(listOf(q1,q2)))
    }

    @Test fun getAllInBBox() {
        // in
        val q1 = addToDaos(TEST_QUEST_TYPE, Element.Type.NODE, 0L, p(0.0,0.0))
        val q2 = addToDaos(TEST_QUEST_TYPE, Element.Type.NODE, 1L, p(1.0,1.0))
        val q3 = addToDaos(TEST_QUEST_TYPE, Element.Type.NODE, 2L, p(0.5,0.5))
        // in but wrong quest type
        addToDaos(TEST_QUEST_TYPE2, Element.Type.NODE, 3L, p(0.5,0.5))
        // out
        addToDaos(TEST_QUEST_TYPE, Element.Type.NODE, 4L, p(-0.5,0.5))
        addToDaos(TEST_QUEST_TYPE, Element.Type.NODE, 5L, p(0.5,-0.5))
        addToDaos(TEST_QUEST_TYPE, Element.Type.NODE, 6L, p(0.5,1.5))
        addToDaos(TEST_QUEST_TYPE, Element.Type.NODE, 7L, p(1.5,0.5))
        assertTrue(dao.getAllInBBox(
            BoundingBox(0.0,0.0,1.0,1.0),
            listOf("TestQuestType")
        ).containsExactlyInAnyOrder(listOf(q1,q2,q3)))
    }

    @Test fun getAllInBBoxCount() {
        // in
        val q1 = addToDaos(TEST_QUEST_TYPE, Element.Type.NODE, 0L, p(0.0,0.0))
        val q2 = addToDaos(TEST_QUEST_TYPE, Element.Type.NODE, 1L, p(1.0,1.0))
        val q3 = addToDaos(TEST_QUEST_TYPE, Element.Type.NODE, 2L, p(0.5,0.5))
        // out
        addToDaos(TEST_QUEST_TYPE, Element.Type.NODE, 3L, p(-0.5,0.5))
        addToDaos(TEST_QUEST_TYPE, Element.Type.NODE, 4L, p(0.5,-0.5))
        addToDaos(TEST_QUEST_TYPE, Element.Type.NODE, 5L, p(0.5,1.5))
        addToDaos(TEST_QUEST_TYPE, Element.Type.NODE, 6L, p(1.5,0.5))
        assertEquals(3, dao.getAllInBBoxCount(BoundingBox(0.0,0.0,1.0,1.0)))
    }

    private fun addToDaos(
        questType: OsmElementQuestType<*>,
        elementType: Element.Type = Element.Type.NODE,
        elementId: Long = 0L,
        pos: LatLon = p(0.0,0.0)
    ): OsmQuest {
        val g = ElementPointGeometry(pos)
        geometryDao.put(ElementGeometryEntry(elementType, elementId, g))
        val q = OsmQuest(null, questType, elementType, elementId, g)
        dao.add(q)
        return q
    }

    private fun p(x: Double, y: Double): LatLon = OsmLatLon(y,x)
}

private val TEST_QUEST_TYPE = TestQuestType()
private val TEST_QUEST_TYPE2 = TestQuestType2()

package de.westnordost.streetcomplete.data.osm.changes

import de.westnordost.osmapi.map.data.Element
import de.westnordost.osmapi.map.data.OsmLatLon
import de.westnordost.streetcomplete.data.ApplicationDbTestCase
import de.westnordost.streetcomplete.data.osm.changes.delete.DeletePoiNodeAction
import de.westnordost.streetcomplete.data.osm.changes.split_way.SplitAtLinePosition
import de.westnordost.streetcomplete.data.osm.changes.split_way.SplitAtPoint
import de.westnordost.streetcomplete.data.osm.changes.split_way.SplitWayAction
import de.westnordost.streetcomplete.data.osm.changes.update_tags.*
import de.westnordost.streetcomplete.data.osm.osmquest.TestQuestType
import de.westnordost.streetcomplete.data.osm.osmquest.TestQuestType2
import de.westnordost.streetcomplete.data.quest.QuestType
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.ktx.containsExactlyInAnyOrder
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ElementEditsDaoTest : ApplicationDbTestCase() {
    private lateinit var dao: ElementEditsDao

    @Before fun createDao() {
        val list = listOf<QuestType<*>>(TEST_QUEST_TYPE, TEST_QUEST_TYPE2)
        dao = ElementEditsDao(dbHelper, ElementEditsMapping(QuestTypeRegistry(list), serializer))
    }

    @Test fun addGet_UpdateElementTagsEdit() {
        val element = createUpdateElementTagsEdit()
        dao.add(element)
        assertNotNull(element.id)
        val dbElement = dao.get(element.id!!)
        assertEquals(element, dbElement)
    }

    @Test fun addGet_RevertUpdateElementTagsEdit() {
        val element = createRevertUpdateElementTagsEdit()
        dao.add(element)
        assertNotNull(element.id)
        val dbElement = dao.get(element.id!!)
        assertEquals(element, dbElement)
    }

    @Test fun addGet_DeletePoiNodeEdit() {
        val element = createDeletePoiNodeEdit()
        dao.add(element)
        assertNotNull(element.id)
        val dbElement = dao.get(element.id!!)
        assertEquals(element, dbElement)
    }

    @Test fun addGet_SplitWayEdit() {
        val element = createSplitWayEdit()
        dao.add(element)
        assertNotNull(element.id)
        val dbElement = dao.get(element.id!!)
        assertEquals(element, dbElement)
    }

    @Test fun addGetDelete() {
        val element = createUpdateElementTagsEdit()
        // nothing there
        assertFalse(dao.delete(1L))
        assertNull(dao.get(1L))
        // now it is added
        dao.add(element)
        assertNotNull(element.id)
        assertNotNull(dao.get(element.id!!))
        // delete again -> nothing there again
        assertTrue(dao.delete(element.id!!))
        assertFalse(dao.delete(element.id!!))
        assertNull(dao.get(element.id!!))
    }

    @Test fun getAll() {
        val e1 = createUpdateElementTagsEdit(timestamp = 10)
        val e2 = createDeletePoiNodeEdit(timestamp = 100)
        val e3 = createSplitWayEdit(timestamp = 1000)

        dao.add(e2)
        dao.add(e1)
        dao.add(e3)

        // sorted by timestamp ascending
        assertEquals(listOf(e1, e2, e3), dao.getAll())
    }

    @Test fun getAllUnsynced() {
        val e1 = createUpdateElementTagsEdit(timestamp = 10)
        val e2 = createDeletePoiNodeEdit(timestamp = 100)
        val e3 = createSplitWayEdit(timestamp = 1000)
        val e4 = createSplitWayEdit(timestamp = 500, isSynced = true)

        dao.add(e2)
        dao.add(e1)
        dao.add(e3)
        dao.add(e4)

        // synced are not included, sorted by timestamp ascending
        assertEquals(listOf(e1, e2, e3), dao.getAllUnsynced())
    }

    @Test fun markSynced() {
        val e = createUpdateElementTagsEdit(isSynced = false)
        dao.add(e)
        dao.markSynced(e.id!!)
        assertTrue(dao.get(e.id!!)!!.isSynced)
    }

    @Test fun peekUnsynced() {
        assertNull(dao.getOldestUnsynced())

        val e1 = createUpdateElementTagsEdit(isSynced = true)
        dao.add(e1)
        assertNull(dao.getOldestUnsynced())

        val e2 = createUpdateElementTagsEdit(timestamp = 1000, isSynced = false)
        dao.add(e2)
        assertEquals(e2, dao.getOldestUnsynced())

        val e3 = createUpdateElementTagsEdit(timestamp = 1500, isSynced = false)
        dao.add(e3)
        assertEquals(e2, dao.getOldestUnsynced())

        val e4 = createUpdateElementTagsEdit(timestamp = 500, isSynced = false)
        dao.add(e4)
        assertEquals(e4, dao.getOldestUnsynced())
    }

    @Test fun getUnsyncedCount() {
        assertEquals(0, dao.getUnsyncedCount())

        dao.add(createUpdateElementTagsEdit(isSynced = true))
        assertEquals(0, dao.getUnsyncedCount())

        dao.add(createUpdateElementTagsEdit(isSynced = false))
        assertEquals(1, dao.getUnsyncedCount())

        dao.add(createUpdateElementTagsEdit(isSynced = false))
        assertEquals(2, dao.getUnsyncedCount())
    }

    @Test fun deleteSyncedOlderThan() {
        val oldEnough = createUpdateElementTagsEdit(timestamp = 500, isSynced = true)
        val tooYoung = createUpdateElementTagsEdit(timestamp = 1000, isSynced = true)
        val notSynced = createUpdateElementTagsEdit(timestamp = 500, isSynced = false)

        dao.add(oldEnough)
        dao.add(tooYoung)
        dao.add(notSynced)

        assertEquals(1, dao.deleteSyncedOlderThan(1000))
        assertTrue(dao.getAll().containsExactlyInAnyOrder(listOf(tooYoung, notSynced)))
    }

    @Test fun updateElementId() {
        assertEquals(0, dao.updateElementId(Element.Type.NODE, -5, 6))

        val e1 = createUpdateElementTagsEdit(elementType = Element.Type.NODE, elementId = -5)
        val e2 = createUpdateElementTagsEdit(elementType = Element.Type.NODE, elementId = -5)
        val e3 = createUpdateElementTagsEdit(elementType = Element.Type.WAY, elementId = -5)
        val e4 = createUpdateElementTagsEdit(elementType = Element.Type.NODE, elementId = -3)
        dao.add(e1)
        dao.add(e2)
        dao.add(e3)
        dao.add(e4)

        assertEquals(2, dao.updateElementId(Element.Type.NODE, -5, 6))

        assertEquals(6, dao.get(e1.id!!)!!.elementId)
        assertEquals(6, dao.get(e2.id!!)!!.elementId)
        assertEquals(-5, dao.get(e3.id!!)!!.elementId)
        assertEquals(-3, dao.get(e4.id!!)!!.elementId)
    }

    @Test fun deleteAllForElement() {
        val e1 = createUpdateElementTagsEdit(elementType = Element.Type.NODE, elementId = 1)
        val e2 = createUpdateElementTagsEdit(elementType = Element.Type.NODE, elementId = 1)
        val e3 = createUpdateElementTagsEdit(elementType = Element.Type.WAY, elementId = 1)
        val e4 = createUpdateElementTagsEdit(elementType = Element.Type.NODE, elementId = 2)
        dao.add(e1)
        dao.add(e2)
        dao.add(e3)
        dao.add(e4)

        assertEquals(2, dao.deleteAllForElement(Element.Type.NODE, 1))

        assertNull(dao.get(e1.id!!))
        assertNull(dao.get(e2.id!!))
        assertNotNull(dao.get(e3.id!!))
        assertNotNull(dao.get(e4.id!!))
    }

    private fun createUpdateElementTagsEdit(
        elementType: Element.Type = Element.Type.NODE,
        elementId: Long = 1L,
        timestamp: Long = 123L,
        isSynced: Boolean = false
    ) = ElementEdit(
        null,
        TEST_QUEST_TYPE,
        elementType,
        elementId,
        "survey",
        OsmLatLon(0.0,0.0),
        timestamp,
        isSynced,
        UpdateElementTagsAction(
            SpatialPartsOfNode(OsmLatLon(0.0,0.0)),
            StringMapChanges(listOf(
                StringMapEntryAdd("a", "b"),
                StringMapEntryModify("c", "d", "e"),
                StringMapEntryDelete("f", "g"),
            )),
            TEST_QUEST_TYPE
        )
    )

    private fun createRevertUpdateElementTagsEdit(timestamp: Long = 123L, isSynced: Boolean = false) = ElementEdit(
        null,
        TEST_QUEST_TYPE,
        Element.Type.NODE,
        1L,
        "survey",
        OsmLatLon(0.0,0.0),
        timestamp,
        isSynced,
        RevertUpdateElementTagsAction(
            SpatialPartsOfNode(OsmLatLon(0.0,0.0)),
            StringMapChanges(listOf(
                StringMapEntryAdd("a", "b"),
                StringMapEntryModify("c", "d", "e"),
                StringMapEntryDelete("f", "g"),
            ))
        )
    )

    private fun createDeletePoiNodeEdit(timestamp: Long = 123L, isSynced: Boolean = false) = ElementEdit(
        null,
        TEST_QUEST_TYPE,
        Element.Type.NODE,
        1L,
        "survey",
        OsmLatLon(0.0,0.0),
        timestamp,
        isSynced,
        DeletePoiNodeAction(1)
    )

    private fun createSplitWayEdit(timestamp: Long = 123L, isSynced: Boolean = false) = ElementEdit(
        null,
        TEST_QUEST_TYPE,
        Element.Type.WAY,
        1L,
        "survey",
        OsmLatLon(0.0,0.0),
        timestamp,
        isSynced,
        SplitWayAction(
            arrayListOf(
                SplitAtPoint(OsmLatLon(0.0,0.0)),
                SplitAtLinePosition(
                    OsmLatLon(0.0,0.0),
                    OsmLatLon(1.0,1.0),
                    0.5
                )
            ),
            0,
            1
        )
    )
}

private val TEST_QUEST_TYPE = TestQuestType()
private val TEST_QUEST_TYPE2 = TestQuestType2()

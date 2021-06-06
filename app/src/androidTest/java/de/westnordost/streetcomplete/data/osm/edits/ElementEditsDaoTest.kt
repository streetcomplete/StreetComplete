package de.westnordost.streetcomplete.data.osm.edits

import de.westnordost.streetcomplete.data.ApplicationDbTestCase
import de.westnordost.streetcomplete.data.osm.edits.delete.DeletePoiNodeAction
import de.westnordost.streetcomplete.data.osm.edits.delete.RevertDeletePoiNodeAction
import de.westnordost.streetcomplete.data.osm.edits.split_way.SplitAtLinePosition
import de.westnordost.streetcomplete.data.osm.edits.split_way.SplitAtPoint
import de.westnordost.streetcomplete.data.osm.edits.split_way.SplitWayAction
import de.westnordost.streetcomplete.data.osm.edits.update_tags.*
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPointGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.*
import de.westnordost.streetcomplete.data.osm.osmquests.TestQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.TestQuestType2
import de.westnordost.streetcomplete.data.quest.QuestType
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ElementEditsDaoTest : ApplicationDbTestCase() {
    private lateinit var dao: ElementEditsDao

    @Before fun createDao() {
        val list = listOf<QuestType<*>>(TEST_QUEST_TYPE, TEST_QUEST_TYPE2)
        dao = ElementEditsDao(database, QuestTypeRegistry(list))
    }

    @Test fun addGet_UpdateElementTagsEdit() {
        val edit = updateTags()
        dao.add(edit)
        assertNotNull(edit.id)
        val dbEdit = dao.get(edit.id)
        assertEquals(edit, dbEdit)
    }

    @Test fun addGet_RevertUpdateElementTagsEdit() {
        val edit = revertUpdateTags()
        dao.add(edit)
        assertNotNull(edit.id)
        val dbEdit = dao.get(edit.id)
        assertEquals(edit, dbEdit)
    }

    @Test fun addGet_DeletePoiNodeEdit() {
        val edit = deletePoi()
        dao.add(edit)
        assertNotNull(edit.id)
        val dbEdit = dao.get(edit.id)
        assertEquals(edit, dbEdit)
    }

    @Test fun addGet_RevertDeletePoiNodeEdit() {
        val edit = revertDeletePoi()
        dao.add(edit)
        assertNotNull(edit.id)
        val dbEdit = dao.get(edit.id)
        assertEquals(edit, dbEdit)
    }

    @Test fun addGet_SplitWayEdit() {
        val edit = splitWay()
        dao.add(edit)
        assertNotNull(edit.id)
        val dbEdit = dao.get(edit.id)
        assertEquals(edit, dbEdit)
    }

    @Test fun getByElement() {
        val e1 = updateTags(element = Node(123, p))
        val e2 = updateTags(element = Node(123, p))
        val e3 = updateTags(element = Way(123, listOf()))
        val e4 = updateTags(element = Node(124, p))
        dao.addAll(e1, e2, e3, e4)

        val edits = dao.getByElement(ElementType.NODE, 123L)

        assertEquals(2, edits.size)
        assertTrue(edits.all { it.elementType == ElementType.NODE && it.elementId == 123L })
    }

    @Test fun addGetDelete() {
        val edit = updateTags()
        // nothing there
        assertFalse(dao.delete(1L))
        assertNull(dao.get(1L))
        // now it is added
        dao.add(edit)
        assertNotNull(edit.id)
        assertNotNull(dao.get(edit.id))
        // delete again -> nothing there again
        assertTrue(dao.delete(edit.id))
        assertFalse(dao.delete(edit.id))
        assertNull(dao.get(edit.id))
    }

    @Test fun deleteAll() {
        val e1 = updateTags()
        val e2 = updateTags()
        val e3 = updateTags()

        dao.addAll(e1, e2, e3)

        assertNotNull(dao.get(1))
        assertNotNull(dao.get(2))
        assertNotNull(dao.get(3))

        dao.deleteAll(listOf(1,2,3))

        assertNull(dao.get(1))
        assertNull(dao.get(2))
        assertNull(dao.get(3))
    }

    @Test fun getAll() {
        val e1 = updateTags(timestamp = 10)
        val e2 = deletePoi(timestamp = 100)
        val e3 = splitWay(timestamp = 1000)

        dao.addAll(e1, e2, e3)

        // sorted by timestamp ascending
        assertEquals(listOf(e1, e2, e3), dao.getAll())
    }

    @Test fun getAllUnsynced() {
        val e1 = updateTags(timestamp = 10)
        val e2 = deletePoi(timestamp = 100)
        val e3 = splitWay(timestamp = 1000)
        val e4 = splitWay(timestamp = 500, isSynced = true)

        dao.addAll(e1, e2, e3, e4)

        // synced are not included, sorted by timestamp ascending
        assertEquals(listOf(e1, e2, e3), dao.getAllUnsynced())
    }

    @Test fun markSynced() {
        val e = updateTags(isSynced = false)
        dao.add(e)
        val id = e.id
        assertFalse(dao.get(id)!!.isSynced)
        dao.markSynced(id)
        assertTrue(dao.get(id)!!.isSynced)
    }

    @Test fun peekUnsynced() {
        assertNull(dao.getOldestUnsynced())

        val e1 = updateTags(isSynced = true)
        dao.add(e1)
        assertNull(dao.getOldestUnsynced())

        val e2 = updateTags(timestamp = 1000, isSynced = false)
        dao.add(e2)
        assertEquals(e2, dao.getOldestUnsynced())

        val e3 = updateTags(timestamp = 1500, isSynced = false)
        dao.add(e3)
        assertEquals(e2, dao.getOldestUnsynced())

        val e4 = updateTags(timestamp = 500, isSynced = false)
        dao.add(e4)
        assertEquals(e4, dao.getOldestUnsynced())
    }

    @Test fun getUnsyncedCount() {
        assertEquals(0, dao.getUnsyncedCount())

        dao.add(updateTags(isSynced = true))
        assertEquals(0, dao.getUnsyncedCount())

        dao.add(updateTags(isSynced = false))
        assertEquals(1, dao.getUnsyncedCount())

        dao.add(updateTags(isSynced = false))
        assertEquals(2, dao.getUnsyncedCount())
    }

    @Test fun getSyncedOlderThan() {
        val oldEnough = updateTags(timestamp = 500, isSynced = true)
        val tooYoung = updateTags(timestamp = 1000, isSynced = true)
        val notSynced = updateTags(timestamp = 500, isSynced = false)

        dao.addAll(oldEnough, tooYoung, notSynced)

        assertEquals(listOf(oldEnough), dao.getSyncedOlderThan(1000))
    }

    @Test fun updateElementId() {
        assertEquals(0, dao.updateElementId(ElementType.NODE, -5, 6))

        val e1 = updateTags(element = Node(-5, p))
        val e2 = updateTags(element = Node(-5, p))
        val e3 = updateTags(element = Way(-5, listOf()))
        val e4 = updateTags(element = Node(-3, p))

        dao.addAll(e1, e2, e3, e4)

        assertEquals(2, dao.updateElementId(ElementType.NODE, -5, 6))

        assertEquals(6, dao.get(e1.id)!!.elementId)
        assertEquals(6, dao.get(e2.id)!!.elementId)
        assertEquals(-5, dao.get(e3.id)!!.elementId)
        assertEquals(-3, dao.get(e4.id)!!.elementId)
    }
}

private fun ElementEditsDao.addAll(vararg edits: ElementEdit) = edits.forEach { add(it) }

private fun updateTags(
    element: Element = node,
    geometry: ElementGeometry = geom,
    timestamp: Long = 123L,
    isSynced: Boolean = false
) = ElementEdit(
    0,
    TEST_QUEST_TYPE,
    element.type,
    element.id,
    element,
    geometry,
    "survey",
    timestamp,
    isSynced,
    UpdateElementTagsAction(
        StringMapChanges(listOf(
            StringMapEntryAdd("a", "b"),
            StringMapEntryModify("c", "d", "e"),
            StringMapEntryDelete("f", "g"),
        ))
    )
)

private fun revertUpdateTags(timestamp: Long = 123L, isSynced: Boolean = false) = ElementEdit(
    0,
    TEST_QUEST_TYPE,
    node.type,
    node.id,
    node,
    geom,
    "survey",
    timestamp,
    isSynced,
    RevertUpdateElementTagsAction(
        StringMapChanges(listOf(
            StringMapEntryAdd("a", "b"),
            StringMapEntryModify("c", "d", "e"),
            StringMapEntryDelete("f", "g"),
        ))
    )
)

private fun deletePoi(timestamp: Long = 123L, isSynced: Boolean = false) = ElementEdit(
    0,
    TEST_QUEST_TYPE,
    node.type,
    node.id,
    node,
    geom,
    "survey",
    timestamp,
    isSynced,
    DeletePoiNodeAction
)

private fun revertDeletePoi(timestamp: Long = 123L, isSynced: Boolean = false) = ElementEdit(
    0,
    TEST_QUEST_TYPE,
    node.type,
    node.id,
    node,
    geom,
    "survey",
    timestamp,
    isSynced,
    RevertDeletePoiNodeAction
)

private fun splitWay(timestamp: Long = 123L, isSynced: Boolean = false) = ElementEdit(
    0,
    TEST_QUEST_TYPE,
    ElementType.WAY,
    1,
    Way(1, listOf(0,1)),
    ElementPolylinesGeometry(listOf(listOf(LatLon(0.0, 0.0), LatLon(1.0,1.0))), LatLon(0.5, 0.5)),
    "survey",
    timestamp,
    isSynced,
    SplitWayAction(
        arrayListOf(
            SplitAtPoint(LatLon(0.0,0.0)),
            SplitAtLinePosition(
                LatLon(0.0,0.0),
                LatLon(1.0,1.0),
                0.5
            )
        )
    )
)

private val p = LatLon(0.0, 0.0)
private val node = Node(1, p)
private val geom = ElementPointGeometry(p)

private val TEST_QUEST_TYPE = TestQuestType()
private val TEST_QUEST_TYPE2 = TestQuestType2()

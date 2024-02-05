package de.westnordost.streetcomplete.data.osm.edits

import de.westnordost.streetcomplete.data.ApplicationDbTestCase
import de.westnordost.streetcomplete.data.osm.edits.create.CreateNodeAction
import de.westnordost.streetcomplete.data.osm.edits.create.RevertCreateNodeAction
import de.westnordost.streetcomplete.data.osm.edits.delete.DeletePoiNodeAction
import de.westnordost.streetcomplete.data.osm.edits.delete.RevertDeletePoiNodeAction
import de.westnordost.streetcomplete.data.osm.edits.split_way.SplitAtLinePosition
import de.westnordost.streetcomplete.data.osm.edits.split_way.SplitAtPoint
import de.westnordost.streetcomplete.data.osm.edits.split_way.SplitWayAction
import de.westnordost.streetcomplete.data.osm.edits.update_tags.RevertUpdateElementTagsAction
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChanges
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryDelete
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryModify
import de.westnordost.streetcomplete.data.osm.edits.update_tags.UpdateElementTagsAction
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPointGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.data.osm.mapdata.Way
import de.westnordost.streetcomplete.data.osm.osmquests.TestQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.TestQuestType2
import de.westnordost.streetcomplete.data.overlays.OverlayRegistry
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement
import de.westnordost.streetcomplete.overlays.Overlay
import de.westnordost.streetcomplete.overlays.Style
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ElementEditsDaoTest : ApplicationDbTestCase() {
    private lateinit var dao: ElementEditsDao

    @BeforeTest fun createDao() {
        val list = listOf(1 to TEST_QUEST_TYPE, 2 to TEST_QUEST_TYPE2)
        val list2 = listOf(1 to TestOverlay)
        dao = ElementEditsDao(database, QuestTypeRegistry(list), OverlayRegistry(list2))
    }

    @Test fun addGet_UpdateElementTagsEdit() {
        val edit = updateTags()
        dao.put(edit)
        assertNotNull(edit.id)
        val dbEdit = dao.get(edit.id)
        assertEquals(edit, dbEdit)
    }

    @Test fun addGet_RevertUpdateElementTagsEdit() {
        val edit = revertUpdateTags()
        dao.put(edit)
        assertNotNull(edit.id)
        val dbEdit = dao.get(edit.id)
        assertEquals(edit, dbEdit)
    }

    @Test fun addGet_DeletePoiNodeEdit() {
        val edit = deletePoi()
        dao.put(edit)
        assertNotNull(edit.id)
        val dbEdit = dao.get(edit.id)
        assertEquals(edit, dbEdit)
    }

    @Test fun addGet_RevertDeletePoiNodeEdit() {
        val edit = revertDeletePoi()
        dao.put(edit)
        assertNotNull(edit.id)
        val dbEdit = dao.get(edit.id)
        assertEquals(edit, dbEdit)
    }

    @Test fun addGet_SplitWayEdit() {
        val edit = splitWay()
        dao.put(edit)
        assertNotNull(edit.id)
        val dbEdit = dao.get(edit.id)
        assertEquals(edit, dbEdit)
    }

    @Test fun addGet_AddNodeEdit() {
        val edit = createNode()
        dao.put(edit)
        assertNotNull(edit.id)
        val dbEdit = dao.get(edit.id)
        assertEquals(edit, dbEdit)
    }

    @Test fun addGet_RevertAddNodeEdit() {
        val edit = revertCreateNode()
        dao.put(edit)
        assertNotNull(edit.id)
        val dbEdit = dao.get(edit.id)
        assertEquals(edit, dbEdit)
    }

    @Test fun addGetDelete() {
        val edit = updateTags()
        // nothing there
        assertFalse(dao.delete(1L))
        assertNull(dao.get(1L))
        // now it is added
        dao.put(edit)
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

        dao.deleteAll(listOf(1, 2, 3))

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
        dao.put(e)
        val id = e.id
        assertFalse(dao.get(id)!!.isSynced)
        dao.markSynced(id)
        assertTrue(dao.get(id)!!.isSynced)
    }

    @Test fun peekUnsynced() {
        assertNull(dao.getOldestUnsynced())

        val e1 = updateTags(isSynced = true)
        dao.put(e1)
        assertNull(dao.getOldestUnsynced())

        val e2 = updateTags(timestamp = 1000, isSynced = false)
        dao.put(e2)
        assertEquals(e2, dao.getOldestUnsynced())

        val e3 = updateTags(timestamp = 1500, isSynced = false)
        dao.put(e3)
        assertEquals(e2, dao.getOldestUnsynced())

        val e4 = updateTags(timestamp = 500, isSynced = false)
        dao.put(e4)
        assertEquals(e4, dao.getOldestUnsynced())
    }

    @Test fun getUnsyncedCount() {
        assertEquals(0, dao.getUnsyncedCount())

        dao.put(updateTags(isSynced = true))
        assertEquals(0, dao.getUnsyncedCount())

        dao.put(updateTags(isSynced = false))
        assertEquals(1, dao.getUnsyncedCount())

        dao.put(updateTags(isSynced = false))
        assertEquals(2, dao.getUnsyncedCount())
    }

    @Test fun getSyncedOlderThan() {
        val oldEnough = updateTags(timestamp = 500, isSynced = true)
        val tooYoung = updateTags(timestamp = 1000, isSynced = true)
        val notSynced = updateTags(timestamp = 500, isSynced = false)

        dao.addAll(oldEnough, tooYoung, notSynced)

        assertEquals(listOf(oldEnough), dao.getSyncedOlderThan(1000))
    }

    @Test fun put_with_same_id_overwrites() {
        val edit = updateTags()
        dao.put(edit)
        val updatedEdit = edit.copy(createdTimestamp = 999L)
        dao.put(updatedEdit)

        assertEquals(edit.id, updatedEdit.id)

        assertEquals(999L, dao.get(edit.id)!!.createdTimestamp)
    }
}

private fun ElementEditsDao.addAll(vararg edits: ElementEdit) = edits.forEach { put(it) }

private fun updateTags(
    element: Element = node,
    geometry: ElementGeometry = geom,
    timestamp: Long = 123L,
    isSynced: Boolean = false
) = ElementEdit(
    0,
    TEST_QUEST_TYPE,
    geometry,
    "survey",
    timestamp,
    isSynced,
    UpdateElementTagsAction(
        element,
        StringMapChanges(listOf(
            StringMapEntryAdd("a", "b"),
            StringMapEntryModify("c", "d", "e"),
            StringMapEntryDelete("f", "g"),
        ))
    ),
    false
)

private fun revertUpdateTags(timestamp: Long = 123L, isSynced: Boolean = false) = ElementEdit(
    0,
    TEST_QUEST_TYPE,
    geom,
    "survey",
    timestamp,
    isSynced,
    RevertUpdateElementTagsAction(
        node,
        StringMapChanges(listOf(
            StringMapEntryAdd("a", "b"),
            StringMapEntryModify("c", "d", "e"),
            StringMapEntryDelete("f", "g"),
        ))
    ),
    false
)

private fun deletePoi(timestamp: Long = 123L, isSynced: Boolean = false) = ElementEdit(
    0,
    TEST_QUEST_TYPE,
    geom,
    "survey",
    timestamp,
    isSynced,
    DeletePoiNodeAction(node),
    false
)

private fun revertDeletePoi(timestamp: Long = 123L, isSynced: Boolean = false) = ElementEdit(
    0,
    TEST_QUEST_TYPE,
    geom,
    "survey",
    timestamp,
    isSynced,
    RevertDeletePoiNodeAction(node),
    false
)

private fun splitWay(timestamp: Long = 123L, isSynced: Boolean = false) = ElementEdit(
    0,
    TEST_QUEST_TYPE,
    ElementPolylinesGeometry(listOf(listOf(LatLon(0.0, 0.0), LatLon(1.0, 1.0))), LatLon(0.5, 0.5)),
    "survey",
    timestamp,
    isSynced,
    SplitWayAction(
        Way(1, listOf(0, 1)),
        arrayListOf(
            SplitAtPoint(LatLon(0.0, 0.0)),
            SplitAtLinePosition(
                LatLon(0.0, 0.0),
                LatLon(1.0, 1.0),
                0.5
            )
        )
    ),
    false
)

private fun createNode(timestamp: Long = 123L, isSynced: Boolean = false) = ElementEdit(
    0,
    TEST_QUEST_TYPE,
    geom,
    "survey",
    timestamp,
    isSynced,
    CreateNodeAction(p, mapOf("shop" to "supermarket")),
    false
)

private fun revertCreateNode(timestamp: Long = 123L, isSynced: Boolean = false) = ElementEdit(
    0,
    TEST_QUEST_TYPE,
    geom,
    "survey",
    timestamp,
    isSynced,
    RevertCreateNodeAction(node),
    false
)

private val p = LatLon(56.7, 89.10)
private val node = Node(1, p)
private val geom = ElementPointGeometry(p)

private val TEST_QUEST_TYPE = TestQuestType()
private val TEST_QUEST_TYPE2 = TestQuestType2()

private object TestOverlay : Overlay {
    override fun getStyledElements(mapData: MapDataWithGeometry) = sequenceOf<Pair<Element, Style>>()
    override fun createForm(element: Element?) = null
    override val changesetComment = "bla"
    override val icon = 0
    override val title = 0
    override val wikiLink = null
    override val achievements = listOf<EditTypeAchievement>()
}

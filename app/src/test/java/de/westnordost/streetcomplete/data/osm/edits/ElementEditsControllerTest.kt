package de.westnordost.streetcomplete.data.osm.edits

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChanges
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.edits.update_tags.UpdateElementTagsAction
import de.westnordost.streetcomplete.data.osm.edits.upload.LastEditTimeStore
import de.westnordost.streetcomplete.data.osm.mapdata.ElementIdUpdate
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType.NODE
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType.WAY
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataUpdates
import de.westnordost.streetcomplete.data.quest.TestQuestTypeA
import de.westnordost.streetcomplete.testutils.any
import de.westnordost.streetcomplete.testutils.edit
import de.westnordost.streetcomplete.testutils.eq
import de.westnordost.streetcomplete.testutils.mock
import de.westnordost.streetcomplete.testutils.node
import de.westnordost.streetcomplete.testutils.on
import de.westnordost.streetcomplete.testutils.pGeom
import de.westnordost.streetcomplete.util.ktx.nowAsEpochMilliseconds
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.Mockito.verify

class ElementEditsControllerTest {

    private lateinit var ctrl: ElementEditsController
    private lateinit var db: ElementEditsDao
    private lateinit var elementsDb: EditElementsDao
    private lateinit var listener: ElementEditsSource.Listener
    private lateinit var lastEditTimeStore: LastEditTimeStore
    private lateinit var idProvider: ElementIdProviderDao

    @Before fun setUp() {
        db = mock()
        on(db.delete(anyLong())).thenReturn(true)
        on(db.markSynced(anyLong())).thenReturn(true)
        elementsDb = mock()
        idProvider = mock()
        lastEditTimeStore = mock()

        listener = mock()
        ctrl = ElementEditsController(db, elementsDb, idProvider, lastEditTimeStore)
        ctrl.addListener(listener)
    }

    @Test fun add() {
        val action = mock<ElementEditAction>()
        val elementKeys = listOf(ElementKey(NODE, 1), ElementKey(WAY, 2))
        on(action.newElementsCount).thenReturn(NewElementsCount(1, 2, 3))
        on(action.elementKeys).thenReturn(elementKeys)

        ctrl.add(QUEST_TYPE, pGeom(), "test", action)

        verifyAdd(ElementEdit(0, QUEST_TYPE, pGeom(), "test", nowAsEpochMilliseconds(), false, action))
    }

    @Test fun markSyncFailed() {
        val edit = edit(action = mock())

        on(idProvider.get(anyLong())).thenReturn(ElementIdProvider(listOf()))

        ctrl.markSyncFailed(edit)

        verifyDelete(edit)
    }

    @Test fun markSynced() {
        val edit0 = edit(action = mock())

        // upload shall create two elements: node -1 and node -8...
        val updates = MapDataUpdates(idUpdates = listOf(
            ElementIdUpdate(NODE, -1, 2),
            ElementIdUpdate(NODE, -8, 20),
        ))
        val updatesMap = updates.idUpdates.associate { ElementKey(it.elementType, it.oldElementId) to it.newElementId }

        // edit 9 uses node -1 -> it must be updated
        val edit1Action = mock<ElementEditAction>()
        on(edit1Action.elementKeys).thenReturn(listOf(ElementKey(NODE, -1)))

        val edit1ActionNew = mock<ElementEditAction>()
        on(edit1ActionNew.elementKeys).thenReturn(listOf(ElementKey(NODE, 2)))

        on(edit1Action.idsUpdatesApplied(updatesMap)).thenReturn(edit1ActionNew)

        val edit1 = edit(id = 9, action = edit1Action)
        on(db.get(9)).thenReturn(edit1)
        on(elementsDb.getAllByElement(NODE, -1)).thenReturn(listOf(edit1.id))

        // no edit uses node -8, nothing to do here
        on(elementsDb.getAllByElement(NODE, -8)).thenReturn(listOf())

        ctrl.markSynced(edit0, updates)

        // as explained above, edit 9 is get-put and its element keys updated
        val edit1New = edit1.copy(action = edit1ActionNew)
        verify(db).put(eq(edit1New))
        verify(elementsDb).delete(edit1New.id)
        verify(elementsDb).put(edit1New.id, edit1New.action.elementKeys)

        verify(db).markSynced(edit0.id)
        verify(idProvider).updateIds(updates.idUpdates)
        verify(listener).onSyncedEdit(edit0)
    }

    @Test fun `undo unsynced`() {
        val edit = edit(action = mock(), isSynced = false)

        on(idProvider.get(anyLong())).thenReturn(ElementIdProvider(listOf()))

        ctrl.undo(edit)

        verifyDelete(edit)
    }

    @Test fun `delete edits based on the the one being undone`() {
        val edit1 = edit(action = mock(), id = 1L)
        val edit2 = edit(action = mock(), id = 2L)
        val edit3 = edit(action = mock(), id = 3L)
        val edit4 = edit(action = mock(), id = 4L)
        val edit5 = edit(action = mock(), id = 5L)

        on(idProvider.get(1L)).thenReturn(ElementIdProvider(listOf(
            ElementKey(NODE, -1),
            ElementKey(NODE, -2),
        )))
        on(idProvider.get(2L)).thenReturn(ElementIdProvider(listOf(
            ElementKey(NODE, -3),
        )))
        on(idProvider.get(3L)).thenReturn(ElementIdProvider(listOf()))
        on(idProvider.get(4L)).thenReturn(ElementIdProvider(listOf()))
        on(idProvider.get(5L)).thenReturn(ElementIdProvider(listOf()))

        on(elementsDb.getAllByElement(NODE, -1)).thenReturn(listOf(2, 3))
        on(elementsDb.getAllByElement(NODE, -2)).thenReturn(listOf(4))
        on(elementsDb.getAllByElement(NODE, -3)).thenReturn(listOf(5))

        on(db.get(1L)).thenReturn(edit1)
        on(db.get(2L)).thenReturn(edit2)
        on(db.get(3L)).thenReturn(edit3)
        on(db.get(4L)).thenReturn(edit4)
        on(db.get(5L)).thenReturn(edit5)

        ctrl.undo(edit1)

        verifyDelete(edit5, edit2, edit3, edit4, edit1)
    }

    @Test fun `undo synced`() {
        val node = node()
        val action = UpdateElementTagsAction(node, StringMapChanges(listOf(StringMapEntryAdd("a", "b"))))
        val edit = edit(action = action, isSynced = true)
        val elementIdProvider = ElementIdProvider(listOf())
        val revertedEdit = edit.copy(id = 0, action = action.createReverted(elementIdProvider))

        on(idProvider.get(anyLong())).thenReturn(elementIdProvider)

        ctrl.undo(edit)

        verifyDelete(edit)
        verifyAdd(revertedEdit)
    }

    private fun verifyAdd(edit: ElementEdit) {
        verify(db).put(any())
        verify(elementsDb).put(edit.id, edit.action.elementKeys)
        val c = edit.action.newElementsCount
        verify(idProvider).assign(edit.id, c.nodes, c.ways, c.relations)
        verify(listener).onAddedEdit(any())
        verify(lastEditTimeStore).touch()
    }

    private fun verifyDelete(vararg edits: ElementEdit) {
        val editIds = edits.map { it.id }
        verify(db).deleteAll(eq(editIds))
        verify(idProvider).deleteAll(eq(editIds))
        verify(elementsDb).deleteAll(eq(editIds))
        verify(listener).onDeletedEdits(edits.toList())
    }
}

private val QUEST_TYPE = TestQuestTypeA()

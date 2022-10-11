package de.westnordost.streetcomplete.data.osm.edits

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChanges
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.edits.update_tags.UpdateElementTagsAction
import de.westnordost.streetcomplete.data.osm.edits.upload.LastEditTimeStore
import de.westnordost.streetcomplete.data.osm.mapdata.ElementIdUpdate
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType.NODE
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataUpdates
import de.westnordost.streetcomplete.data.quest.TestQuestTypeA
import de.westnordost.streetcomplete.testutils.any
import de.westnordost.streetcomplete.testutils.edit
import de.westnordost.streetcomplete.testutils.eq
import de.westnordost.streetcomplete.testutils.mock
import de.westnordost.streetcomplete.testutils.node
import de.westnordost.streetcomplete.testutils.on
import de.westnordost.streetcomplete.testutils.pGeom
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.Mockito.verify

class ElementEditsControllerTest {

    private lateinit var ctrl: ElementEditsController
    private lateinit var db: ElementEditsDao
    private lateinit var listener: ElementEditsSource.Listener
    private lateinit var lastEditTimeStore: LastEditTimeStore
    private lateinit var idProvider: ElementIdProviderDao

    @Before fun setUp() {
        db = mock()
        on(db.delete(anyLong())).thenReturn(true)
        on(db.markSynced(anyLong())).thenReturn(true)
        idProvider = mock()
        lastEditTimeStore = mock()

        listener = mock()
        ctrl = ElementEditsController(db, idProvider, lastEditTimeStore)
        ctrl.addListener(listener)
    }

    @Test fun add() {
        val action = mock<ElementEditAction>()
        on(action.newElementsCount).thenReturn(NewElementsCount(1, 2, 3))

        ctrl.add(QUEST_TYPE, pGeom(), "test", action)

        verify(db).add(any())
        verify(idProvider).assign(0L, 1, 2, 3)
        verify(listener).onAddedEdit(any())
        verify(lastEditTimeStore).touch()
    }

    @Test fun syncFailed() {
        val edit = edit(action = mock())

        on(idProvider.get(anyLong())).thenReturn(ElementIdProvider(listOf()))

        ctrl.markSyncFailed(edit)

        verify(db).deleteAll(eq(listOf(edit.id)))
        verify(idProvider).deleteAll(eq(listOf(edit.id)))
        verify(listener).onDeletedEdits(listOf(edit))
    }

    @Test fun synced() {
        val edit = edit(action = mock())

        val idUpdates = listOf(
            ElementIdUpdate(NODE, -1, 2),
            ElementIdUpdate(NODE, -8, 20),
        )
        val updates = MapDataUpdates(idUpdates = idUpdates)

        ctrl.markSynced(edit, updates)

        verify(db).updateElementId(NODE, -1, 2)
        verify(db).updateElementId(NODE, -8, 20)
        verify(db).markSynced(edit.id)
        verify(idProvider).delete(edit.id)
        verify(listener).onSyncedEdit(edit)
    }

    @Test fun `undo unsynced`() {
        val node = node()
        val edit = edit(
            action = UpdateElementTagsAction(node, StringMapChanges(listOf(StringMapEntryAdd("a", "b")))),
            isSynced = false
        )

        on(idProvider.get(anyLong())).thenReturn(ElementIdProvider(listOf()))
        on(db.get(anyLong())).thenReturn(edit)

        ctrl.undo(edit)

        verify(db).deleteAll(eq(listOf(edit.id)))
        verify(idProvider).deleteAll(eq(listOf(edit.id)))
        verify(listener).onDeletedEdits(listOf(edit))
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

        on(db.getByElement(NODE, -1)).thenReturn(listOf(edit2, edit3))
        on(db.getByElement(NODE, -2)).thenReturn(listOf(edit4))
        on(db.getByElement(NODE, -3)).thenReturn(listOf(edit5))

        on(db.get(1L)).thenReturn(edit1)
        on(db.get(2L)).thenReturn(edit2)
        on(db.get(3L)).thenReturn(edit3)
        on(db.get(4L)).thenReturn(edit4)
        on(db.get(5L)).thenReturn(edit5)

        ctrl.undo(edit1)

        val deletedEditIds = listOf(5L, 2L, 3L, 4L, 1L)

        verify(db).deleteAll(eq(deletedEditIds))
        verify(idProvider).deleteAll(eq(deletedEditIds))
        verify(listener).onDeletedEdits(listOf(edit5, edit2, edit3, edit4, edit1))
    }

    @Test fun `undo synced`() {
        val node = node()
        val edit = edit(
            action = UpdateElementTagsAction(node, StringMapChanges(listOf(StringMapEntryAdd("a", "b")))),
            isSynced = true
        )

        on(db.get(anyLong())).thenReturn(edit)
        on(idProvider.get(anyLong())).thenReturn(ElementIdProvider(listOf()))

        ctrl.undo(edit)

        verify(db).deleteAll(eq(listOf(edit.id)))
        verify(idProvider).deleteAll(eq(listOf(edit.id)))
        verify(listener).onDeletedEdits(listOf(edit))

        verify(db).add(any())
        verify(idProvider).assign(0L, 0, 0, 0)
        verify(listener).onAddedEdit(any())
        verify(lastEditTimeStore).touch()
    }
}

private val QUEST_TYPE = TestQuestTypeA()

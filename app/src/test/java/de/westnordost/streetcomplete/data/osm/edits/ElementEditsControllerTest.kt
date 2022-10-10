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

        ctrl.markSynced(edit)

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

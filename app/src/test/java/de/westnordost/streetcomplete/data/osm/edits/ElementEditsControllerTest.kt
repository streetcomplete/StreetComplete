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

        val updates = MapDataUpdates(idUpdates = listOf(
            ElementIdUpdate(NODE, -1, 2),
            ElementIdUpdate(NODE, -8, 20),
        ))

        ctrl.markSynced(edit0, updates)

        verify(db).markSynced(edit0.id)
        verify(idProvider).delete(edit0.id)
        verify(listener).onSyncedEdit(edit0)
    }

    @Test fun `undo unsynced`() {
        val edit = edit(action = mock(), isSynced = false)

        on(idProvider.get(anyLong())).thenReturn(ElementIdProvider(listOf()))

        ctrl.undo(edit)

        verifyDelete(edit)
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
        val c = edit.action.newElementsCount
        verify(idProvider).assign(edit.id, c.nodes, c.ways, c.relations)
        verify(listener).onAddedEdit(any())
        verify(lastEditTimeStore).touch()
    }

    private fun verifyDelete(edit: ElementEdit) {
        verify(db).delete(edit.id)
        verify(idProvider).delete(edit.id)
        verify(listener).onDeletedEdits(listOf(edit))
    }
}

private val QUEST_TYPE = TestQuestTypeA()

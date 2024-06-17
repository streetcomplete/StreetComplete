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
import de.westnordost.streetcomplete.testutils.edit
import de.westnordost.streetcomplete.testutils.node
import de.westnordost.streetcomplete.testutils.pGeom
import de.westnordost.streetcomplete.testutils.verifyInvokedExactlyOnce
import de.westnordost.streetcomplete.util.ktx.nowAsEpochMilliseconds
import io.mockative.Mock
import io.mockative.any
import io.mockative.classOf
import io.mockative.every
import io.mockative.mock
import kotlin.test.BeforeTest
import kotlin.test.Test

class ElementEditsControllerTest {

    private lateinit var ctrl: ElementEditsController
    @Mock private lateinit var db: ElementEditsDao
    @Mock private lateinit var elementsDb: EditElementsDao
    @Mock private lateinit var listener: ElementEditsSource.Listener
    @Mock private lateinit var lastEditTimeStore: LastEditTimeStore
    @Mock private lateinit var idProvider: ElementIdProviderDao

    // dummy
    @Mock private lateinit var action: ElementEditAction

    @BeforeTest fun setUp() {
        db = mock(classOf<ElementEditsDao>())
        every { db.delete(any()) }.returns(true)
        every { db.markSynced(any()) }.returns(true)
        elementsDb = mock(classOf<EditElementsDao>())
        idProvider = mock(classOf<ElementIdProviderDao>())
        lastEditTimeStore = mock(classOf<LastEditTimeStore>())

        listener = mock(classOf<ElementEditsSource.Listener>())
        ctrl = ElementEditsController(db, elementsDb, idProvider, lastEditTimeStore)
        ctrl.addListener(listener)
    }

    @Test fun add() {
        val action = mock(classOf<ElementEditAction>())
        val elementKeys = listOf(ElementKey(NODE, 1), ElementKey(WAY, 2))
        every { action.newElementsCount }.returns(NewElementsCount(1, 2, 3))
        every { action.elementKeys }.returns(elementKeys)

        ctrl.add(QUEST_TYPE, pGeom(), "test", action, true)

        verifyAdd(ElementEdit(0, QUEST_TYPE, pGeom(), "test", nowAsEpochMilliseconds(), false, action, true))
    }

    @Test fun markSyncFailed() {
        val edit = edit(action = mock(classOf<ElementEditAction>()))

        every { idProvider.get(any()) }.returns(ElementIdProvider(listOf()))

        ctrl.markSyncFailed(edit)

        verifyDelete(edit)
    }

    @Test fun markSynced() {
        val edit0 = edit(action = mock(classOf<ElementEditAction>()))

        // upload shall create two elements: node -1 and node -8...
        val updates = MapDataUpdates(idUpdates = listOf(
            ElementIdUpdate(NODE, -1, 2),
            ElementIdUpdate(NODE, -8, 20),
        ))
        val updatesMap = updates.idUpdates.associate { ElementKey(it.elementType, it.oldElementId) to it.newElementId }

        // edit 9 uses node -1 -> it must be updated
        val edit1Action = mock(classOf<ElementEditAction>())
        every { edit1Action.elementKeys }.returns(listOf(ElementKey(NODE, -1)))

        val edit1ActionNew = mock(classOf<ElementEditAction>())
        every { edit1ActionNew.elementKeys }.returns(listOf(ElementKey(NODE, 2)))

        every { edit1Action.idsUpdatesApplied(updatesMap) }.returns(edit1ActionNew)

        val edit1 = edit(id = 9, action = edit1Action)
        every { db.get(9) }.returns(edit1)
        every { elementsDb.getAllByElement(NODE, -1) }.returns(listOf(edit1.id))

        // no edit uses node -8, nothing to do here
        every { elementsDb.getAllByElement(NODE, -8) }.returns(listOf())

        ctrl.markSynced(edit0, updates)
        val edit0Synced = edit0.copy(isSynced = true)

        // as explained above, edit 9 is get-put and its element keys updated
        val edit1New = edit1.copy(action = edit1ActionNew)
        verifyInvokedExactlyOnce { db.put(edit1New) }
        verifyInvokedExactlyOnce { elementsDb.delete(edit1New.id) }
        verifyInvokedExactlyOnce { elementsDb.put(edit1New.id, edit1New.action.elementKeys) }

        verifyInvokedExactlyOnce { db.markSynced(edit0Synced.id) }
        verifyInvokedExactlyOnce { idProvider.updateIds(updates.idUpdates) }
        verifyInvokedExactlyOnce { listener.onSyncedEdit(edit0Synced) }
    }

    @Test fun `undo unsynced`() {
        val edit = edit(action = mock(classOf<ElementEditAction>()), isSynced = false)

        every { idProvider.get(any()) }.returns(ElementIdProvider(listOf()))

        ctrl.undo(edit)

        verifyDelete(edit)
    }

    @Test fun `delete edits based on the the one being undone`() {
        val edit1 = edit(action = mock(classOf<ElementEditAction>()), id = 1L)
        val edit2 = edit(action = mock(classOf<ElementEditAction>()), id = 2L)
        val edit3 = edit(action = mock(classOf<ElementEditAction>()), id = 3L)
        val edit4 = edit(action = mock(classOf<ElementEditAction>()), id = 4L)
        val edit5 = edit(action = mock(classOf<ElementEditAction>()), id = 5L)

        every { idProvider.get(1L) }.returns(ElementIdProvider(listOf(
            ElementKey(NODE, -1),
            ElementKey(NODE, -2),
        )))
        every { idProvider.get(2L) }.returns(ElementIdProvider(listOf(
            ElementKey(NODE, -3),
        )))
        every { idProvider.get(3L) }.returns(ElementIdProvider(listOf()))
        every { idProvider.get(4L) }.returns(ElementIdProvider(listOf()))
        every { idProvider.get(5L) }.returns(ElementIdProvider(listOf()))

        every { elementsDb.getAllByElement(NODE, -1) }.returns(listOf(2, 3))
        every { elementsDb.getAllByElement(NODE, -2) }.returns(listOf(4))
        every { elementsDb.getAllByElement(NODE, -3) }.returns(listOf(5))

        every { db.get(1L) }.returns(edit1)
        every { db.get(2L) }.returns(edit2)
        every { db.get(3L) }.returns(edit3)
        every { db.get(4L) }.returns(edit4)
        every { db.get(5L) }.returns(edit5)

        ctrl.undo(edit1)

        verifyDelete(edit5, edit2, edit3, edit4, edit1)
    }

    @Test fun `undo synced`() {
        val node = node()
        val action = UpdateElementTagsAction(node, StringMapChanges(listOf(StringMapEntryAdd("a", "b"))))
        val edit = edit(action = action, isSynced = true)
        val elementIdProvider = ElementIdProvider(listOf())
        val revertedEdit = edit.copy(id = 0, action = action.createReverted(elementIdProvider))

        every { idProvider.get(any()) }.returns(elementIdProvider)

        ctrl.undo(edit)

        verifyDelete(edit)
        verifyAdd(revertedEdit)
    }

    private fun verifyAdd(edit: ElementEdit) {
        verifyInvokedExactlyOnce { db.put(any()) }
        verifyInvokedExactlyOnce { elementsDb.put(edit.id, edit.action.elementKeys) }
        val c = edit.action.newElementsCount
        verifyInvokedExactlyOnce { idProvider.assign(edit.id, c.nodes, c.ways, c.relations) }
        verifyInvokedExactlyOnce { listener.onAddedEdit(any()) }
        verifyInvokedExactlyOnce { lastEditTimeStore.touch() }
    }

    private fun verifyDelete(vararg edits: ElementEdit) {
        val editIds = edits.map { it.id }
        verifyInvokedExactlyOnce { db.deleteAll(editIds) }
        verifyInvokedExactlyOnce { idProvider.deleteAll(editIds) }
        verifyInvokedExactlyOnce { elementsDb.deleteAll(editIds) }
        verifyInvokedExactlyOnce { listener.onDeletedEdits(edits.toList()) }
    }
}

private val QUEST_TYPE = TestQuestTypeA()

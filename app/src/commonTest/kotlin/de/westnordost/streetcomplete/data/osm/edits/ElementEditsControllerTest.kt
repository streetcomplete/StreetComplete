package de.westnordost.streetcomplete.data.osm.edits

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChanges
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.edits.update_tags.UpdateElementTagsAction
import de.westnordost.streetcomplete.data.osm.mapdata.ElementIdUpdate
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType.NODE
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType.WAY
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataUpdates
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.data.quest.TestQuestTypeA
import dev.mokkery.matcher.any
import de.westnordost.streetcomplete.testutils.edit
import dev.mokkery.mock
import de.westnordost.streetcomplete.testutils.node
import dev.mokkery.answering.returns
import dev.mokkery.every
import de.westnordost.streetcomplete.testutils.pGeom
import de.westnordost.streetcomplete.util.ktx.nowAsEpochMilliseconds
import dev.mokkery.verify
import kotlin.test.BeforeTest
import kotlin.test.Test

class ElementEditsControllerTest {

    private lateinit var ctrl: ElementEditsController
    private lateinit var db: ElementEditsDao
    private lateinit var elementsDb: EditElementsDao
    private lateinit var listener: ElementEditsSource.Listener
    private lateinit var prefs: Preferences
    private lateinit var idProvider: ElementIdProviderDao

    @BeforeTest fun setUp() {
        db = mock() {
            every { delete(any()) } returns true
            every { markSynced(any()) } returns true
        }
        elementsDb = mock()
        idProvider = mock()
        prefs = mock()

        listener = mock()
        ctrl = ElementEditsController(db, elementsDb, idProvider, prefs)
        ctrl.addListener(listener)
    }

    @Test fun add() {
        val action = mock<ElementEditAction>()
        val elementKeys = listOf(ElementKey(NODE, 1), ElementKey(WAY, 2))
        every { action.newElementsCount } returns NewElementsCount(1, 2, 3)
        every { action.elementKeys } returns elementKeys

        ctrl.add(QUEST_TYPE, pGeom(), "test", action, true)

        verifyAdd(ElementEdit(0, QUEST_TYPE, pGeom(), "test", nowAsEpochMilliseconds(), false, action, true))
    }

    @Test fun markSyncFailed() {
        val edit = edit(action = mock())

        every { idProvider.get(any()) } returns ElementIdProvider(listOf())

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
        every { edit1Action.elementKeys } returns listOf(ElementKey(NODE, -1))

        val edit1ActionNew = mock<ElementEditAction>()
        every { edit1ActionNew.elementKeys } returns listOf(ElementKey(NODE, 2))

        every { edit1Action.idsUpdatesApplied(updatesMap) } returns edit1ActionNew

        val edit1 = edit(id = 9, action = edit1Action)
        every { db.get(9) } returns edit1
        every { elementsDb.getAllByElement(NODE, -1) } returns listOf(edit1.id)

        // no edit uses node -8, nothing to do here
        every { elementsDb.getAllByElement(NODE, -8) } returns listOf()

        ctrl.markSynced(edit0, updates)
        val edit0Synced = edit0.copy(isSynced = true)

        // as explained above, edit 9 is get-put and its element keys updated
        val edit1New = edit1.copy(action = edit1ActionNew)
        verify { db.put(edit1New) }
        verify { elementsDb.delete(edit1New.id) }
        verify { elementsDb.put(edit1New.id, edit1New.action.elementKeys) }

        verify { db.markSynced(edit0Synced.id) }
        verify { idProvider.updateIds(updates.idUpdates) }
        verify { listener.onSyncedEdit(edit0Synced) }
    }

    @Test fun `undo unsynced`() {
        val edit = edit(action = mock(), isSynced = false)

        every { idProvider.get(any()) } returns ElementIdProvider(listOf())

        ctrl.undo(edit)

        verifyDelete(edit)
    }

    @Test fun `delete edits based on the the one being undone`() {
        val edit1 = edit(action = mock(), id = 1L)
        val edit2 = edit(action = mock(), id = 2L)
        val edit3 = edit(action = mock(), id = 3L)
        val edit4 = edit(action = mock(), id = 4L)
        val edit5 = edit(action = mock(), id = 5L)

        every { idProvider.get(1L) } returns ElementIdProvider(listOf(
            ElementKey(NODE, -1),
            ElementKey(NODE, -2),
        ))
        every { idProvider.get(2L) } returns ElementIdProvider(listOf(
            ElementKey(NODE, -3),
        ))
        every { idProvider.get(3L) } returns ElementIdProvider(listOf())
        every { idProvider.get(4L) } returns ElementIdProvider(listOf())
        every { idProvider.get(5L) } returns ElementIdProvider(listOf())

        every { elementsDb.getAllByElement(NODE, -1) } returns listOf(2, 3)
        every { elementsDb.getAllByElement(NODE, -2) } returns listOf(4)
        every { elementsDb.getAllByElement(NODE, -3) } returns listOf(5)

        every { db.get(1L) } returns edit1
        every { db.get(2L) } returns edit2
        every { db.get(3L) } returns edit3
        every { db.get(4L) } returns edit4
        every { db.get(5L) } returns edit5

        ctrl.undo(edit1)

        verifyDelete(edit5, edit2, edit3, edit4, edit1)
    }

    @Test fun `undo synced`() {
        val node = node()
        val action = UpdateElementTagsAction(node, StringMapChanges(listOf(StringMapEntryAdd("a", "b"))))
        val edit = edit(action = action, isSynced = true)
        val elementIdProvider = ElementIdProvider(listOf())
        val revertedEdit = edit.copy(id = 0, action = action.createReverted(elementIdProvider))

        every { idProvider.get(any()) } returns elementIdProvider

        ctrl.undo(edit)

        verifyDelete(edit)
        verifyAdd(revertedEdit)
    }

    private fun verifyAdd(edit: ElementEdit) {
        verify { db.put(any()) }
        verify { elementsDb.put(edit.id, edit.action.elementKeys) }
        val c = edit.action.newElementsCount
        verify { idProvider.assign(edit.id, c.nodes, c.ways, c.relations) }
        verify { listener.onAddedEdit(any()) }
        verify { prefs.lastEditTime = any() }    }

    private fun verifyDelete(vararg edits: ElementEdit) {
        val editIds = edits.map { it.id }
        verify { db.deleteAll(editIds) }
        verify { idProvider.deleteAll(editIds) }
        verify { elementsDb.deleteAll(editIds) }
        verify { listener.onDeletedEdits(edits.toList()) }
    }
}

private val QUEST_TYPE = TestQuestTypeA()

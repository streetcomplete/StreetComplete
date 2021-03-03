package de.westnordost.streetcomplete.data.osm.edits

import de.westnordost.osmapi.map.ElementIdUpdate
import de.westnordost.osmapi.map.ElementUpdates
import de.westnordost.osmapi.map.data.Element
import de.westnordost.osmapi.map.data.OsmLatLon
import de.westnordost.streetcomplete.any
import de.westnordost.streetcomplete.data.osm.edits.update_tags.SpatialPartsOfNode
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChanges
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.edits.update_tags.UpdateElementTagsAction
import de.westnordost.streetcomplete.data.osm.edits.upload.LastEditTimeStore
import de.westnordost.streetcomplete.data.quest.TestQuestTypeA
import de.westnordost.streetcomplete.mock
import de.westnordost.streetcomplete.on
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
        val p = OsmLatLon(2.0,4.0)
        val action =  mock<ElementEditAction>()
        on(action.newElementsCount).thenReturn(NewElementsCount(1,2,3))

        ctrl.add(QUEST_TYPE, Element.Type.NODE, 1L, "test", p, action)

        verify(db).add(any())
        verify(idProvider).assign(0L, 1, 2, 3)
        verify(listener).onAddedEdit(any())
        verify(lastEditTimeStore).touch()
    }

    @Test fun syncFailed() {
        val edit = edit(action = mock())

        ctrl.syncFailed(edit)

        verify(db).delete(edit.id)
        verify(idProvider).delete(edit.id)
        verify(listener).onDeletedEdit(edit)
    }

    @Test fun synced() {
        val edit = edit(action = mock())

        val idUpdates = listOf(
            ElementIdUpdate(Element.Type.NODE, -1,2),
            ElementIdUpdate(Element.Type.NODE, -8,20),
        )
        val updates = ElementUpdates(idUpdates = idUpdates)

        ctrl.synced(edit, updates)

        verify(db).updateElementId(Element.Type.NODE, -1,2)
        verify(db).updateElementId(Element.Type.NODE, -8,20)
        verify(db).markSynced(edit.id)
        verify(idProvider).delete(edit.id)
        verify(listener).onSyncedEdit(edit)
    }

    @Test fun `undo unsynced`() {
        val action = UpdateElementTagsAction(
            SpatialPartsOfNode(OsmLatLon(0.0,0.0)),
            StringMapChanges(listOf(StringMapEntryAdd("a", "b"))),
            QUEST_TYPE
        )
        val edit = edit(action = action, isSynced = false)

        on(db.get(anyLong())).thenReturn(edit)

        ctrl.undo(1)

        verify(db).delete(edit.id)
        verify(idProvider).delete(edit.id)
        verify(listener).onDeletedEdit(edit)
    }

    @Test fun `undo synced`() {
        val action = UpdateElementTagsAction(
            SpatialPartsOfNode(OsmLatLon(0.0,0.0)),
            StringMapChanges(listOf(StringMapEntryAdd("a", "b"))),
            QUEST_TYPE
        )
        val edit = edit(action = action, isSynced = true)

        on(db.get(anyLong())).thenReturn(edit)

        ctrl.undo(1)

        verify(db).delete(edit.id)
        verify(idProvider).delete(edit.id)
        verify(listener).onDeletedEdit(edit)

        verify(db).add(any())
        verify(idProvider).assign(0L, 0, 0, 0)
        verify(listener).onAddedEdit(any())
        verify(lastEditTimeStore).touch()
    }
}

private val QUEST_TYPE = TestQuestTypeA()

private fun edit(
    elementType: Element.Type = Element.Type.NODE,
    elementId: Long = -1L,
    pos: OsmLatLon = OsmLatLon(0.0,0.0),
    timestamp: Long = 123L,
    action: ElementEditAction,
    isSynced: Boolean = false
) = ElementEdit(
    1L,
    QUEST_TYPE,
    elementType,
    elementId,
    "survey",
    pos,
    timestamp,
    isSynced,
    action
)

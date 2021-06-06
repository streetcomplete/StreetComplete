package de.westnordost.streetcomplete.data.edithistory

import de.westnordost.streetcomplete.data.osm.edits.ElementEditsController
import de.westnordost.streetcomplete.data.osm.edits.ElementEditsSource
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestController
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEditsController
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEditsSource
import de.westnordost.streetcomplete.data.osmnotes.notequests.OsmNoteQuestController
import de.westnordost.streetcomplete.data.quest.OsmQuestKey
import de.westnordost.streetcomplete.data.quest.TestQuestTypeA
import de.westnordost.streetcomplete.testutils.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.Mockito.verify

class EditHistoryControllerTest {

    private lateinit var elementEditsController: ElementEditsController
    private lateinit var noteEditsController: NoteEditsController
    private lateinit var osmQuestController: OsmQuestController
    private lateinit var osmNoteQuestController: OsmNoteQuestController
    private lateinit var listener: EditHistorySource.Listener
    private lateinit var ctrl: EditHistoryController

    private lateinit var elementEditsListener: ElementEditsSource.Listener
    private lateinit var noteEditsListener: NoteEditsSource.Listener
    private lateinit var hideNoteQuestsListener: OsmNoteQuestController.HideOsmNoteQuestListener
    private lateinit var hideQuestsListener: OsmQuestController.HideOsmQuestListener

    @Before fun setUp() {
        elementEditsController = mock()
        noteEditsController = mock()
        osmQuestController = mock()
        osmNoteQuestController = mock()
        listener = mock()

        elementEditsListener = mock()
        noteEditsListener = mock()
        hideNoteQuestsListener = mock()
        hideQuestsListener = mock()

        on(elementEditsController.addListener(any())).then { invocation ->
            elementEditsListener = invocation.getArgument(0)
            Unit
        }
        on(noteEditsController.addListener(any())).then { invocation ->
            noteEditsListener = invocation.getArgument(0)
            Unit
        }
        on(osmNoteQuestController.addHideQuestsListener(any())).then { invocation ->
            hideNoteQuestsListener = invocation.getArgument(0)
            Unit
        }
        on(osmQuestController.addHideQuestsListener(any())).then { invocation ->
            hideQuestsListener = invocation.getArgument(0)
            Unit
        }

        ctrl = EditHistoryController(elementEditsController, noteEditsController, osmNoteQuestController, osmQuestController)
        ctrl.addListener(listener)
    }

    @Test fun getAll() {
        val edit1 = edit(timestamp = 10L)
        val edit2 = noteEdit(timestamp = 20L)
        val edit3 = edit(timestamp = 50L)
        val edit4 = noteEdit(timestamp = 80L)
        val edit5 = questHidden(timestamp = 100L)
        val edit6 = noteQuestHidden(timestamp = 120L)

        on(elementEditsController.getAll()).thenReturn(listOf(edit1, edit3))
        on(noteEditsController.getAll()).thenReturn(listOf(edit2, edit4))
        on(osmQuestController.getAllHiddenNewerThan(anyLong())).thenReturn(listOf(edit5))
        on(osmNoteQuestController.getAllHiddenNewerThan(anyLong())).thenReturn(listOf(edit6))

        assertEquals(
            listOf(edit6, edit5, edit4, edit3, edit2, edit1),
            ctrl.getAll()
        )
    }

    @Test fun `undo element edit`() {
        val e = edit()
        ctrl.undo(e)
        verify(elementEditsController).undo(e)
    }

    @Test fun `undo note edit`() {
        val e = noteEdit()
        ctrl.undo(e)
        verify(noteEditsController).undo(e)
    }

    @Test fun `undo hid quest`() {
        val e = questHidden(ElementType.NODE, 1L, TestQuestTypeA())
        ctrl.undo(e)
        verify(osmQuestController).unhide(OsmQuestKey(ElementType.NODE, 1L, "TestQuestTypeA"))
    }

    @Test fun `undo hid note quest`() {
        val e = noteQuestHidden()
        ctrl.undo(e)
        verify(osmNoteQuestController).unhide(e.note.id)
    }

    @Test fun `relays added element edit`() {
        val e = edit()
        elementEditsListener.onAddedEdit(e)
        verify(listener).onAdded(e)
    }

    @Test fun `relays removed element edit`() {
        val e = edit()
        elementEditsListener.onDeletedEdits(listOf(e))
        verify(listener).onDeleted(eq(listOf(e)))
    }

    @Test fun `relays synced element edit`() {
        val e = edit()
        elementEditsListener.onSyncedEdit(e)
        verify(listener).onSynced(e)
    }

    @Test fun `relays added note edit`() {
        val e = noteEdit()
        noteEditsListener.onAddedEdit(e)
        verify(listener).onAdded(e)
    }

    @Test fun `relays removed note edit`() {
        val e = noteEdit()
        noteEditsListener.onDeletedEdits(listOf(e))
        verify(listener).onDeleted(eq(listOf(e)))
    }

    @Test fun `relays synced note edit`() {
        val e = noteEdit()
        noteEditsListener.onSyncedEdit(e)
        verify(listener).onSynced(e)
    }

    @Test fun `relays hid quest`() {
        val e = questHidden()
        hideQuestsListener.onHid(e)
        verify(listener).onAdded(e)
    }

    @Test fun `relays unhid quest`() {
        val e = questHidden()
        hideQuestsListener.onUnhid(e)
        verify(listener).onDeleted(eq(listOf(e)))
    }

    @Test fun `relays unhid all quests`() {
        hideQuestsListener.onUnhidAll()
        verify(listener).onInvalidated()
    }

    @Test fun `relays hid note quest`() {
        val e = noteQuestHidden()
        hideNoteQuestsListener.onHid(e)
        verify(listener).onAdded(e)
    }

    @Test fun `relays unhid note quest`() {
        val e = noteQuestHidden()
        hideNoteQuestsListener.onUnhid(e)
        verify(listener).onDeleted(eq(listOf(e)))
    }

    @Test fun `relays unhid all note quests`() {
        hideNoteQuestsListener.onUnhidAll()
        verify(listener).onInvalidated()
    }
}

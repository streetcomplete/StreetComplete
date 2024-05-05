package de.westnordost.streetcomplete.data.edithistory

import de.westnordost.streetcomplete.data.osm.edits.ElementEditsController
import de.westnordost.streetcomplete.data.osm.edits.ElementEditsSource
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestsHiddenController
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestsHiddenSource
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEditsController
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEditsSource
import de.westnordost.streetcomplete.data.osmnotes.notequests.OsmNoteQuestsHiddenController
import de.westnordost.streetcomplete.data.osmnotes.notequests.OsmNoteQuestsHiddenSource
import de.westnordost.streetcomplete.data.quest.TestQuestTypeA
import de.westnordost.streetcomplete.testutils.any
import de.westnordost.streetcomplete.testutils.edit
import de.westnordost.streetcomplete.testutils.eq
import de.westnordost.streetcomplete.testutils.mock
import de.westnordost.streetcomplete.testutils.noteEdit
import de.westnordost.streetcomplete.testutils.noteQuestHidden
import de.westnordost.streetcomplete.testutils.on
import de.westnordost.streetcomplete.testutils.questHidden
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.Mockito.verify
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class EditHistoryControllerTest {

    private lateinit var elementEditsController: ElementEditsController
    private lateinit var noteEditsController: NoteEditsController
    private lateinit var osmQuestsHiddenController: OsmQuestsHiddenController
    private lateinit var osmNoteQuestsHiddenController: OsmNoteQuestsHiddenController
    private lateinit var listener: EditHistorySource.Listener
    private lateinit var ctrl: EditHistoryController

    private lateinit var elementEditsListener: ElementEditsSource.Listener
    private lateinit var noteEditsListener: NoteEditsSource.Listener
    private lateinit var hideNoteQuestsListener: OsmNoteQuestsHiddenSource.Listener
    private lateinit var hideQuestsListener: OsmQuestsHiddenSource.Listener

    @BeforeTest fun setUp() {
        elementEditsController = mock()
        noteEditsController = mock()
        osmQuestsHiddenController = mock()
        osmNoteQuestsHiddenController = mock()
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
        on(osmNoteQuestsHiddenController.addListener(any())).then { invocation ->
            hideNoteQuestsListener = invocation.getArgument(0)
            Unit
        }
        on(osmQuestsHiddenController.addListener(any())).then { invocation ->
            hideQuestsListener = invocation.getArgument(0)
            Unit
        }

        ctrl = EditHistoryController(elementEditsController, noteEditsController, osmNoteQuestsHiddenController, osmQuestsHiddenController)
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
        on(osmQuestsHiddenController.getAllHiddenNewerThan(anyLong())).thenReturn(listOf(edit5))
        on(osmNoteQuestsHiddenController.getAllHiddenNewerThan(anyLong())).thenReturn(listOf(edit6))

        assertEquals(
            listOf(edit6, edit5, edit4, edit3, edit2, edit1),
            ctrl.getAll()
        )
    }

    @Test fun `undo element edit`() {
        val e = edit()
        on(elementEditsController.get(e.id)).thenReturn(e)
        ctrl.undo(e.key)
        verify(elementEditsController).undo(e)
    }

    @Test fun `undo note edit`() {
        val e = noteEdit()
        on(noteEditsController.get(e.id)).thenReturn(e)
        ctrl.undo(e.key)
        verify(noteEditsController).undo(e)
    }

    @Test fun `undo hid quest`() {
        val e = questHidden(ElementType.NODE, 1L, TestQuestTypeA())
        on(osmQuestsHiddenController.getHidden(e.questKey)).thenReturn(e)
        ctrl.undo(e.key)
        verify(osmQuestsHiddenController).unhide(e.questKey)
    }

    @Test fun `undo hid note quest`() {
        val e = noteQuestHidden()
        on(osmNoteQuestsHiddenController.getHidden(e.note.id)).thenReturn(e)
        ctrl.undo(e.key)
        verify(osmNoteQuestsHiddenController).unhide(e.note.id)
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

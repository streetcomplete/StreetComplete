package de.westnordost.streetcomplete.data.osmnotes.edits

import de.westnordost.streetcomplete.testutils.*
import de.westnordost.streetcomplete.testutils.any
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.Mockito.verify
import org.mockito.Mockito.never

class NoteEditsControllerTest {

    private lateinit var ctrl: NoteEditsController
    private lateinit var db: NoteEditsDao
    private lateinit var listener: NoteEditsSource.Listener

    @Before fun setUp() {
        db = mock()
        on(db.delete(anyLong())).thenReturn(true)
        on(db.markSynced(anyLong())).thenReturn(true)

        listener = mock()
        ctrl = NoteEditsController(db)
        ctrl.addListener(listener)
    }

    @Test fun add() {
        ctrl.add(1L, NoteEditAction.COMMENT, p(1.0, 1.0))

        verify(db).add(any())
        verify(listener).onAddedEdit(any())
    }

    @Test fun syncFailed() {
        val edit = noteEdit(noteId = 1)
        ctrl.markSyncFailed(edit)

        verify(db).delete(1)
        verify(listener).onDeletedEdits(eq(listOf(edit)))
    }

    @Test fun synced() {
        val edit = noteEdit(id = 3, noteId = 1)
        val note = note(1)

        ctrl.markSynced(edit, note)

        verify(db).markSynced(3)
        verify(db, never()).updateNoteId(anyLong(), anyLong())
        verify(listener).onSyncedEdit(edit)
    }

    @Test fun `synced with new id`() {
        val edit = noteEdit(id = 3, noteId = -100)
        val note = note(123)

        ctrl.markSynced(edit, note)

        verify(db).markSynced(3)
        verify(db).updateNoteId(-100L, 123L)
        verify(listener).onSyncedEdit(edit)
    }
}

package de.westnordost.streetcomplete.data.osmnotes.edits

import de.westnordost.osmapi.map.data.OsmLatLon
import de.westnordost.osmapi.notes.Note
import de.westnordost.streetcomplete.any
import de.westnordost.streetcomplete.mock
import de.westnordost.streetcomplete.on
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.Mockito.*

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
        ctrl.add(1L, NoteEditAction.COMMENT, OsmLatLon(1.0, 1.0))

        verify(db).add(any())
        verify(listener).onAddedEdit(any())
    }

    @Test fun syncFailed() {
        val edit = NoteEdit(0L, 1L, OsmLatLon(1.0,1.0), NoteEditAction.COMMENT, null, emptyList(), 0L, false, false)
        ctrl.syncFailed(edit)

        verify(db).delete(anyLong())
        verify(listener).onDeletedEdit(edit)
    }

    @Test fun synced() {
        val edit = NoteEdit(0L, 1L, OsmLatLon(1.0,1.0), NoteEditAction.COMMENT, null, emptyList(), 0L, false, false)
        val note = Note()
        note.id = 1L

        ctrl.synced(edit, note)

        verify(db).markSynced(anyLong())
        verify(db, never()).updateNoteId(anyLong(), anyLong())
        verify(listener).onSyncedEdit(edit)
    }

    @Test fun `synced with new id`() {
        val edit = NoteEdit(0L, -100L, OsmLatLon(1.0,1.0), NoteEditAction.COMMENT, null, emptyList(), 0L, false, false)
        val note = Note()
        note.id = 123L

        ctrl.synced(edit, note)

        verify(db).markSynced(anyLong())
        verify(db).updateNoteId(-100L, 123L)
        verify(listener).onSyncedEdit(edit)
    }
}

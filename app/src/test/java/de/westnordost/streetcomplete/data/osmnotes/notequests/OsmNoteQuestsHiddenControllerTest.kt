package de.westnordost.streetcomplete.data.osmnotes.notequests

import de.westnordost.streetcomplete.data.osmnotes.edits.NotesWithEditsSource
import de.westnordost.streetcomplete.testutils.eq
import de.westnordost.streetcomplete.testutils.mock
import de.westnordost.streetcomplete.testutils.note
import de.westnordost.streetcomplete.testutils.on
import org.mockito.Mockito.verify
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class OsmNoteQuestsHiddenControllerTest {

    private lateinit var db: NoteQuestsHiddenDao
    private lateinit var noteSource: NotesWithEditsSource

    private lateinit var ctrl: OsmNoteQuestsHiddenController

    private lateinit var hideListener: OsmNoteQuestsHiddenSource.Listener

    @BeforeTest fun setUp() {
        db = mock()
        hideListener = mock()
        noteSource = mock()
        ctrl = OsmNoteQuestsHiddenController(db, noteSource)
        ctrl.addListener(hideListener)
    }


    @Test fun hide() {
        val note = note(1)
        val ts = 123L

        on(db.getTimestamp(1)).thenReturn(ts)
        on(noteSource.get(1)).thenReturn(note)

        ctrl.hide(1)

        verify(db).add(1)
        verify(hideListener).onHid(eq(OsmNoteQuestHidden(note, ts)))
    }

    @Test fun unhide() {
        val note = note(1)
        val ts = 123L

        on(db.getTimestamp(1)).thenReturn(ts)
        on(noteSource.get(1)).thenReturn(note)
        on(db.delete(1)).thenReturn(true)

        ctrl.unhide(1)

        verify(hideListener).onUnhid(eq(OsmNoteQuestHidden(note, ts)))
    }

    @Test fun unhideAll() {
        val hiddenNoteIds = listOf<Long>(1, 2, 3)
        val hiddenNotes = listOf(note(1), note(2), note(3))

        on(db.getAllIds()).thenReturn(hiddenNoteIds)
        on(noteSource.getAll(hiddenNoteIds)).thenReturn(hiddenNotes)

        ctrl.unhideAll()

        verify(db).deleteAll()
        verify(hideListener).onUnhidAll()
    }

    @Test fun get() {
        val note = note(1)
        on(noteSource.get(1)).thenReturn(note)
        on(db.getTimestamp(1)).thenReturn(123)
        assertEquals(OsmNoteQuestHidden(note, 123), ctrl.get(1))
    }

    @Test fun isHidden() {
        on(db.contains(1)).thenReturn(true)
        on(db.contains(2)).thenReturn(false)

        assertTrue(ctrl.isHidden(1))
        assertFalse(ctrl.isHidden(2))
    }

    @Test fun getAllNewerThan() {
        val note1 = note(1)
        val note2 = note(2)

        on(db.getNewerThan(123L)).thenReturn(listOf(
            NoteIdWithTimestamp(1, 300),
            NoteIdWithTimestamp(2, 500),
            NoteIdWithTimestamp(3, 600), // missing note
        ))
        on(noteSource.getAll(eq(listOf(1L, 2L, 3L)))).thenReturn(listOf(note1, note2))

        assertEquals(
            listOf(
                OsmNoteQuestHidden(note1, 300),
                OsmNoteQuestHidden(note2, 500),
            ),
            ctrl.getAllNewerThan(123L)
        )
    }

    @Test fun countAll() {
        on(db.countAll()).thenReturn(123L)
        assertEquals(123L, ctrl.countAll())
    }
}

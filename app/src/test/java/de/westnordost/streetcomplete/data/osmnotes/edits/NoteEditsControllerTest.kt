package de.westnordost.streetcomplete.data.osmnotes.edits

import de.westnordost.streetcomplete.data.osm.mapdata.ElementIdUpdate
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.testutils.note
import de.westnordost.streetcomplete.testutils.noteEdit
import de.westnordost.streetcomplete.testutils.p
import de.westnordost.streetcomplete.testutils.verifyInvokedExactly
import de.westnordost.streetcomplete.testutils.verifyInvokedExactlyOnce
import io.mockative.Mock
import io.mockative.any
import io.mockative.classOf
import io.mockative.every
import io.mockative.mock
import kotlin.test.BeforeTest
import kotlin.test.Test

class NoteEditsControllerTest {

    private lateinit var ctrl: NoteEditsController
    @Mock private lateinit var db: NoteEditsDao
    @Mock private lateinit var listener: NoteEditsSource.Listener

    @BeforeTest fun setUp() {
        db = mock(classOf<NoteEditsDao>())
        every { db.delete(any()) }.returns(true)
        every { db.markSynced(any()) }.returns(true)

        listener = mock(classOf<NoteEditsSource.Listener>())
        ctrl = NoteEditsController(db)
        ctrl.addListener(listener)
    }

    @Test fun add() {
        every { db.add(any()) }.returns(true)
        ctrl.add(1L, NoteEditAction.COMMENT, p(1.0, 1.0))

        verifyInvokedExactlyOnce { db.add(any()) }
        verifyInvokedExactlyOnce { listener.onAddedEdit(any()) }
    }

    @Test fun syncFailed() {
        val edit = noteEdit(noteId = 1)
        ctrl.markSyncFailed(edit)

        verifyInvokedExactlyOnce { db.delete(1) }
        verifyInvokedExactlyOnce { listener.onDeletedEdits(listOf(edit)) }
    }

    @Test fun synced() {
        val edit = noteEdit(id = 3, noteId = 1)
        val note = note(1)

        ctrl.markSynced(edit, note)
        val editSynced = edit.copy(isSynced = true)

        verifyInvokedExactlyOnce { db.markSynced(3) }
        verifyInvokedExactlyOnce { listener.onSyncedEdit(editSynced) }
    }

    @Test fun `synced with new id`() {
        val edit = noteEdit(id = 3, noteId = -100)
        val note = note(123)
        every { db.updateNoteId(-100, 123) }.returns(1)

        ctrl.markSynced(edit, note)
        val editSynced = edit.copy(isSynced = true)

        verifyInvokedExactlyOnce { db.markSynced(3) }
        verifyInvokedExactlyOnce { db.updateNoteId(-100L, 123L) }
        verifyInvokedExactlyOnce { listener.onSyncedEdit(editSynced) }
    }

    @Test fun `update element ids`() {
        ctrl.updateElementIds(listOf(
            ElementIdUpdate(ElementType.NODE, -9, 1234),
            ElementIdUpdate(ElementType.WAY, 4, 999),
            ElementIdUpdate(ElementType.RELATION, 8, 234),
        ))

        verifyInvokedExactlyOnce { db.replaceTextInUnsynced("osm.org/node/-9 ", "osm.org/node/1234 ") }
        verifyInvokedExactlyOnce { db.replaceTextInUnsynced("osm.org/way/4 ", "osm.org/way/999 ") }
        verifyInvokedExactlyOnce { db.replaceTextInUnsynced("osm.org/relation/8 ", "osm.org/relation/234 ") }
    }
}

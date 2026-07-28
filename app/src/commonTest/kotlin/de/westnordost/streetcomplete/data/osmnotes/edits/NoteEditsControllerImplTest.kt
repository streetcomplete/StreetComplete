package de.westnordost.streetcomplete.data.osmnotes.edits

import de.westnordost.streetcomplete.data.osm.mapdata.ElementIdUpdate
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import dev.mokkery.matcher.any
import dev.mokkery.mock
import de.westnordost.streetcomplete.testutils.note
import de.westnordost.streetcomplete.testutils.noteEdit
import dev.mokkery.answering.returns
import dev.mokkery.every
import de.westnordost.streetcomplete.testutils.p
import dev.mokkery.verify
import dev.mokkery.verify.VerifyMode.Companion.not
import kotlin.test.BeforeTest
import kotlin.test.Test

class NoteEditsControllerImplTest {

    private lateinit var ctrl: NoteEditsController
    private lateinit var db: NoteEditsDao
    private lateinit var listener: NoteEditsSource.Listener

    @BeforeTest fun setUp() {
        db = mock() {
            every { delete(any()) } returns true
            every { markSynced(any()) } returns true
        }

        listener = mock()
        ctrl = NoteEditsControllerImpl(db)
        ctrl.addListener(listener)
    }

    @Test fun add() {
        ctrl.add(1L, NoteEditAction.COMMENT, p(1.0, 1.0))

        verify { db.add(any()) }
        verify { listener.onAddedEdit(any()) }
    }

    @Test fun syncFailed() {
        val edit = noteEdit(noteId = 1)
        ctrl.markSyncFailed(edit)

        verify { db.delete(1) }
        verify { listener.onDeletedEdits(listOf(edit)) }
    }

    @Test fun synced() {
        val edit = noteEdit(id = 3, noteId = 1)
        val note = note(1)

        ctrl.markSynced(edit, note)
        val editSynced = edit.copy(isSynced = true)

        verify { db.markSynced(3) }
        verify(not) { db.updateNoteId(any(), any()) }
        verify { listener.onSyncedEdit(editSynced) }
    }

    @Test fun `synced with new id`() {
        val edit = noteEdit(id = 3, noteId = -100)
        val note = note(123)

        ctrl.markSynced(edit, note)
        val editSynced = edit.copy(isSynced = true)

        verify { db.markSynced(3) }
        verify { db.updateNoteId(-100L, 123L) }
        verify { listener.onSyncedEdit(editSynced) }
    }

    @Test fun `update element ids`() {
        ctrl.updateElementIds(listOf(
            ElementIdUpdate(ElementType.NODE, -9, 1234),
            ElementIdUpdate(ElementType.WAY, 4, 999),
            ElementIdUpdate(ElementType.RELATION, 8, 234),
        ))

        verify { db.replaceTextInUnsynced("osm.org/node/-9 ", "osm.org/node/1234 ") }
        verify { db.replaceTextInUnsynced("osm.org/way/4 ", "osm.org/way/999 ") }
        verify { db.replaceTextInUnsynced("osm.org/relation/8 ", "osm.org/relation/234 ") }
    }
}

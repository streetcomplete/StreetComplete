package de.westnordost.streetcomplete.data.edithistory

import de.westnordost.streetcomplete.data.osm.edits.ElementEditsController
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEditsController
import de.westnordost.streetcomplete.testutils.edit
import de.westnordost.streetcomplete.testutils.mock
import de.westnordost.streetcomplete.testutils.noteEdit
import de.westnordost.streetcomplete.testutils.on
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class EditHistoryControllerTest {

    private lateinit var elementEditsController: ElementEditsController
    private lateinit var noteEditsController: NoteEditsController
    private lateinit var listener: EditHistorySource.Listener
    private lateinit var ctrl: EditHistoryController

    @Before fun setUp() {
        elementEditsController = mock()
        noteEditsController = mock()
        listener = mock()

        ctrl = EditHistoryController(elementEditsController, noteEditsController)
        ctrl.addListener(listener)
    }

    @Test fun getAll() {
        val edit1 = edit(timestamp = 10L)
        val edit2 = noteEdit(timestamp = 20L)
        val edit3 = edit(timestamp = 50L)
        val edit4 = noteEdit(timestamp = 80L)

        on(elementEditsController.getAll()).thenReturn(listOf(edit1, edit3))
        on(noteEditsController.getAll()).thenReturn(listOf(edit2, edit4))

        assertEquals(
            listOf(edit4, edit3, edit2, edit1),
            ctrl.getAll()
        )
    }
}

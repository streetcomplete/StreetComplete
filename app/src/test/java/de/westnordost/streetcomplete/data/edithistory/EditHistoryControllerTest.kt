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
import de.westnordost.streetcomplete.testutils.edit
import de.westnordost.streetcomplete.testutils.noteEdit
import de.westnordost.streetcomplete.testutils.noteQuestHidden
import de.westnordost.streetcomplete.testutils.questHidden
import de.westnordost.streetcomplete.testutils.verifyInvokedExactlyOnce
import io.mockative.Mock
import io.mockative.any
import io.mockative.classOf
import io.mockative.eq
import io.mockative.every
import io.mockative.mock

import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class EditHistoryControllerTest {

    @Mock private lateinit var elementEditsController: ElementEditsController
    @Mock private lateinit var noteEditsController: NoteEditsController
    @Mock private lateinit var osmQuestsHiddenController: OsmQuestsHiddenController
    @Mock private lateinit var osmNoteQuestsHiddenController: OsmNoteQuestsHiddenController
    @Mock private lateinit var listener: EditHistorySource.Listener
    @Mock private lateinit var ctrl: EditHistoryController

    @Mock private lateinit var elementEditsListener: ElementEditsSource.Listener
    @Mock private lateinit var noteEditsListener: NoteEditsSource.Listener
    @Mock private lateinit var hideNoteQuestsListener: OsmNoteQuestsHiddenSource.Listener
    @Mock private lateinit var hideQuestsListener: OsmQuestsHiddenSource.Listener

    @BeforeTest fun setUp() {
        elementEditsController = mock(classOf<ElementEditsController>())
        noteEditsController = mock(classOf<NoteEditsController>())
        osmQuestsHiddenController = mock(classOf<OsmQuestsHiddenController>())
        osmNoteQuestsHiddenController = mock(classOf<OsmNoteQuestsHiddenController>())
        listener = mock(classOf<EditHistorySource.Listener>())

        elementEditsListener = mock(classOf<ElementEditsSource.Listener>())
        noteEditsListener = mock(classOf<NoteEditsSource.Listener>())
        hideNoteQuestsListener = mock(classOf<OsmNoteQuestsHiddenSource.Listener>())
        hideQuestsListener = mock(classOf<OsmQuestsHiddenSource.Listener>())

        every { elementEditsController.addListener(any()) }.invokes { args ->
            elementEditsListener = args[0] as ElementEditsSource.Listener
        }
        every {noteEditsController.addListener(any()) }.invokes { args ->
            noteEditsListener = args[0] as NoteEditsSource.Listener
        }
        every {osmNoteQuestsHiddenController.addListener(any())}.invokes { args ->
            hideNoteQuestsListener = args[0] as OsmNoteQuestsHiddenSource.Listener
        }
        every { osmQuestsHiddenController.addListener(any()) }.invokes { args ->
            hideQuestsListener = args[0] as OsmQuestsHiddenSource.Listener
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

        every { elementEditsController.getAll() }.returns(listOf(edit1, edit3))
        every {noteEditsController.getAll()}.returns(listOf(edit2, edit4))
        every {osmQuestsHiddenController.getAllHiddenNewerThan(any())}.returns(listOf(edit5))
        every {osmNoteQuestsHiddenController.getAllHiddenNewerThan(any()) }.returns(listOf(edit6))

        assertEquals(
            listOf(edit6, edit5, edit4, edit3, edit2, edit1),
            ctrl.getAll()
        )
    }

    @Test fun `undo element edit`() {
        val e = edit()
        every { elementEditsController.get(e.id) }.returns(e)
        ctrl.undo(e.key)
        verifyInvokedExactlyOnce { elementEditsController.undo(e) }
    }

    @Test fun `undo note edit`() {
        val e = noteEdit()
        every { noteEditsController.get(e.id) }.returns(e)
        ctrl.undo(e.key)
        verifyInvokedExactlyOnce { noteEditsController.undo(e) }
    }

    @Test fun `undo hid quest`() {
        val e = questHidden(ElementType.NODE, 1L, TestQuestTypeA())
        every { osmQuestsHiddenController.getHidden(e.questKey) }.returns(e)
        ctrl.undo(e.key)
        verifyInvokedExactlyOnce { osmQuestsHiddenController.unhide(e.questKey) }
    }

    @Test fun `undo hid note quest`() {
        val e = noteQuestHidden()
        every { osmNoteQuestsHiddenController.getHidden(e.note.id) }.returns(e)
        ctrl.undo(e.key)
        verifyInvokedExactlyOnce { osmNoteQuestsHiddenController.unhide(e.note.id) }
    }

    @Test fun `relays added element edit`() {
        val e = edit()
        elementEditsListener.onAddedEdit(e)
        verifyInvokedExactlyOnce { listener.onAdded(e) }
    }

    @Test fun `relays removed element edit`() {
        val e = edit()
        elementEditsListener.onDeletedEdits(listOf(e))
        verifyInvokedExactlyOnce { listener.onDeleted(eq(listOf(e))) }
    }

    @Test fun `relays synced element edit`() {
        val e = edit()
        elementEditsListener.onSyncedEdit(e)
        verifyInvokedExactlyOnce { listener.onSynced(e) }
    }

    @Test fun `relays added note edit`() {
        val e = noteEdit()
        noteEditsListener.onAddedEdit(e)
        verifyInvokedExactlyOnce { listener.onAdded(e) }
    }

    @Test fun `relays removed note edit`() {
        val e = noteEdit()
        noteEditsListener.onDeletedEdits(listOf(e))
        verifyInvokedExactlyOnce { listener.onDeleted(eq(listOf(e))) }
    }

    @Test fun `relays synced note edit`() {
        val e = noteEdit()
        noteEditsListener.onSyncedEdit(e)
        verifyInvokedExactlyOnce { listener.onSynced(e) }
    }

    @Test fun `relays hid quest`() {
        val e = questHidden()
        hideQuestsListener.onHid(e)
        verifyInvokedExactlyOnce { listener.onAdded(e) }
    }

    @Test fun `relays unhid quest`() {
        val e = questHidden()
        hideQuestsListener.onUnhid(e)
        verifyInvokedExactlyOnce { listener.onDeleted(eq(listOf(e))) }
    }

    @Test fun `relays unhid all quests`() {
        hideQuestsListener.onUnhidAll()
        verifyInvokedExactlyOnce { listener.onInvalidated() }
    }

    @Test fun `relays hid note quest`() {
        val e = noteQuestHidden()
        hideNoteQuestsListener.onHid(e)
        verifyInvokedExactlyOnce { listener.onAdded(e) }
    }

    @Test fun `relays unhid note quest`() {
        val e = noteQuestHidden()
        hideNoteQuestsListener.onUnhid(e)
        verifyInvokedExactlyOnce { listener.onDeleted(eq(listOf(e))) }
    }

    @Test fun `relays unhid all note quests`() {
        hideNoteQuestsListener.onUnhidAll()
        verifyInvokedExactlyOnce { listener.onInvalidated() }
    }
}

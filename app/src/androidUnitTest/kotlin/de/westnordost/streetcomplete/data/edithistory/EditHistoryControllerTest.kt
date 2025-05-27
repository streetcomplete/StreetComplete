package de.westnordost.streetcomplete.data.edithistory

import de.westnordost.streetcomplete.data.osm.edits.ElementEditsController
import de.westnordost.streetcomplete.data.osm.edits.ElementEditsSource
import de.westnordost.streetcomplete.data.osm.edits.MapDataWithEditsSource
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEditsController
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEditsSource
import de.westnordost.streetcomplete.data.osmnotes.edits.NotesWithEditsSource
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.data.visiblequests.QuestsHiddenController
import de.westnordost.streetcomplete.data.visiblequests.QuestsHiddenSource
import de.westnordost.streetcomplete.testutils.QUEST_TYPE
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
    private lateinit var hiddenQuestsController: QuestsHiddenController
    private lateinit var notesSource: NotesWithEditsSource
    private lateinit var mapDataSource: MapDataWithEditsSource
    private lateinit var questTypeRegistry: QuestTypeRegistry
    private lateinit var listener: EditHistorySource.Listener
    private lateinit var ctrl: EditHistoryController

    private lateinit var elementEditsListener: ElementEditsSource.Listener
    private lateinit var noteEditsListener: NoteEditsSource.Listener
    private lateinit var hiddenQuestsListener: QuestsHiddenSource.Listener

    @BeforeTest fun setUp() {
        elementEditsController = mock()
        noteEditsController = mock()
        hiddenQuestsController = mock()
        notesSource = mock()
        mapDataSource = mock()
        questTypeRegistry = QuestTypeRegistry(listOf(
            0 to QUEST_TYPE,
        ))
        listener = mock()

        elementEditsListener = mock()
        noteEditsListener = mock()
        hiddenQuestsListener = mock()

        on(elementEditsController.addListener(any())).then { invocation ->
            elementEditsListener = invocation.getArgument(0)
            Unit
        }
        on(noteEditsController.addListener(any())).then { invocation ->
            noteEditsListener = invocation.getArgument(0)
            Unit
        }
        on(hiddenQuestsController.addListener(any())).then { invocation ->
            hiddenQuestsListener = invocation.getArgument(0)
            Unit
        }

        ctrl = EditHistoryController(
            elementEditsController, noteEditsController, hiddenQuestsController, notesSource,
            mapDataSource, questTypeRegistry
        )
        ctrl.addListener(listener)
    }

    @Test fun getAll() {
        val edit1 = edit(timestamp = 10L)
        val edit2 = noteEdit(timestamp = 20L)
        val edit3 = edit(timestamp = 50L)
        val edit4 = noteEdit(timestamp = 80L)

        val edit5 = questHidden(timestamp = 100L)
        val edit6 = noteQuestHidden(timestamp = 120L)

        on(mapDataSource.getGeometry(edit5.elementType, edit5.elementId)).thenReturn(edit5.geometry)
        on(notesSource.get(edit6.note.id)).thenReturn(edit6.note)

        on(elementEditsController.getAll()).thenReturn(listOf(edit1, edit3))
        on(noteEditsController.getAll()).thenReturn(listOf(edit2, edit4))
        on(hiddenQuestsController.getAllNewerThan(anyLong())).thenReturn(listOf(
            edit5.questKey to edit5.createdTimestamp,
            edit6.questKey to edit6.createdTimestamp,
        ))

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
        val e = questHidden()
        on(mapDataSource.getGeometry(e.elementType, e.elementId)).thenReturn(e.geometry)
        on(hiddenQuestsController.get(e.questKey)).thenReturn(e.createdTimestamp)
        ctrl.undo(e.key)
        verify(hiddenQuestsController).unhide(e.questKey)
    }

    @Test fun `undo hid note quest`() {
        val e = noteQuestHidden()
        on(notesSource.get(e.note.id)).thenReturn(e.note)
        on(hiddenQuestsController.get(e.questKey)).thenReturn(e.createdTimestamp)
        ctrl.undo(e.key)
        verify(hiddenQuestsController).unhide(e.questKey)
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
        on(mapDataSource.getGeometry(e.elementType, e.elementId)).thenReturn(e.geometry)
        hiddenQuestsListener.onHid(e.questKey, e.createdTimestamp)
        verify(listener).onAdded(e)
    }

    @Test fun `relays unhid quest`() {
        val e = questHidden()
        on(mapDataSource.getGeometry(e.elementType, e.elementId)).thenReturn(e.geometry)
        hiddenQuestsListener.onUnhid(e.questKey, e.createdTimestamp)
        verify(listener).onDeleted(eq(listOf(e)))
    }

    @Test fun `relays unhid all quests`() {
        hiddenQuestsListener.onUnhidAll()
        verify(listener).onInvalidated()
    }

    @Test fun `relays hid note quest`() {
        val e = noteQuestHidden()
        on(notesSource.get(e.note.id)).thenReturn(e.note)
        hiddenQuestsListener.onHid(e.questKey, e.createdTimestamp)
        verify(listener).onAdded(e)
    }

    @Test fun `relays unhid note quest`() {
        val e = noteQuestHidden()
        on(notesSource.get(e.note.id)).thenReturn(e.note)
        hiddenQuestsListener.onUnhid(e.questKey, e.createdTimestamp)
        verify(listener).onDeleted(eq(listOf(e)))
    }

    @Test fun `relays unhid all note quests`() {
        hiddenQuestsListener.onUnhidAll()
        verify(listener).onInvalidated()
    }
}

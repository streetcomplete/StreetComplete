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
import dev.mokkery.matcher.any
import de.westnordost.streetcomplete.testutils.edit
import dev.mokkery.mock
import de.westnordost.streetcomplete.testutils.noteEdit
import de.westnordost.streetcomplete.testutils.noteQuestHidden
import dev.mokkery.answering.returns
import dev.mokkery.every
import de.westnordost.streetcomplete.testutils.questHidden
import dev.mokkery.answering.calls
import dev.mokkery.verify
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
        elementEditsController = mock() {
            every { addListener(any()) } calls { (listener: ElementEditsSource.Listener) ->
                elementEditsListener = listener
            }
        }
        noteEditsController = mock() {
            every { addListener(any()) } calls { (listener: NoteEditsSource.Listener) ->
                noteEditsListener = listener
            }
        }
        hiddenQuestsController = mock() {
            every { addListener(any()) } calls { (listener: QuestsHiddenSource.Listener) ->
                hiddenQuestsListener = listener
            }
        }
        notesSource = mock()
        mapDataSource = mock()
        questTypeRegistry = QuestTypeRegistry(listOf(
            0 to QUEST_TYPE,
        ))
        listener = mock()

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

        every { mapDataSource.getGeometry(edit5.elementType, edit5.elementId) } returns edit5.geometry
        every { notesSource.get(edit6.note.id) } returns edit6.note

        every { elementEditsController.getAll() } returns listOf(edit1, edit3)
        every { noteEditsController.getAll() } returns listOf(edit2, edit4)
        every { hiddenQuestsController.getAllNewerThan(any()) } returns listOf(
            edit5.questKey to edit5.createdTimestamp,
            edit6.questKey to edit6.createdTimestamp,
        )

        assertEquals(
            listOf(edit6, edit5, edit4, edit3, edit2, edit1),
            ctrl.getAll()
        )
    }

    @Test fun `undo element edit`() {
        val e = edit()
        every { elementEditsController.get(e.id) } returns e
        ctrl.undo(e.key)
        verify { elementEditsController.undo(e) }
    }

    @Test fun `undo note edit`() {
        val e = noteEdit()
        every { noteEditsController.get(e.id) } returns e
        ctrl.undo(e.key)
        verify { noteEditsController.undo(e) }
    }

    @Test fun `undo hid quest`() {
        val e = questHidden()
        every { mapDataSource.getGeometry(e.elementType, e.elementId) } returns e.geometry
        every { hiddenQuestsController.get(e.questKey) } returns e.createdTimestamp
        ctrl.undo(e.key)
        verify { hiddenQuestsController.unhide(e.questKey) }
    }

    @Test fun `undo hid note quest`() {
        val e = noteQuestHidden()
        every { notesSource.get(e.note.id) } returns e.note
        every { hiddenQuestsController.get(e.questKey) } returns e.createdTimestamp
        ctrl.undo(e.key)
        verify { hiddenQuestsController.unhide(e.questKey) }
    }

    @Test fun `relays added element edit`() {
        val e = edit()
        elementEditsListener.onAddedEdit(e)
        verify { listener.onAdded(e) }
    }

    @Test fun `relays removed element edit`() {
        val e = edit()
        elementEditsListener.onDeletedEdits(listOf(e))
        verify { listener.onDeleted(listOf(e)) }
    }

    @Test fun `relays synced element edit`() {
        val e = edit()
        elementEditsListener.onSyncedEdit(e)
        verify { listener.onSynced(e) }
    }

    @Test fun `relays added note edit`() {
        val e = noteEdit()
        noteEditsListener.onAddedEdit(e)
        verify { listener.onAdded(e) }
    }

    @Test fun `relays removed note edit`() {
        val e = noteEdit()
        noteEditsListener.onDeletedEdits(listOf(e))
        verify { listener.onDeleted(listOf(e)) }
    }

    @Test fun `relays synced note edit`() {
        val e = noteEdit()
        noteEditsListener.onSyncedEdit(e)
        verify { listener.onSynced(e) }
    }

    @Test fun `relays hid quest`() {
        val e = questHidden()
        every { mapDataSource.getGeometry(e.elementType, e.elementId) } returns e.geometry
        hiddenQuestsListener.onHid(e.questKey, e.createdTimestamp)
        verify { listener.onAdded(e) }
    }

    @Test fun `relays unhid quest`() {
        val e = questHidden()
        every { mapDataSource.getGeometry(e.elementType, e.elementId) } returns e.geometry
        hiddenQuestsListener.onUnhid(e.questKey, e.createdTimestamp)
        verify { listener.onDeleted(listOf(e)) }
    }

    @Test fun `relays unhid all quests`() {
        hiddenQuestsListener.onUnhidAll()
        verify { listener.onInvalidated() }
    }

    @Test fun `relays hid note quest`() {
        val e = noteQuestHidden()
        every { notesSource.get(e.note.id) } returns e.note
        hiddenQuestsListener.onHid(e.questKey, e.createdTimestamp)
        verify { listener.onAdded(e) }
    }

    @Test fun `relays unhid note quest`() {
        val e = noteQuestHidden()
        every { notesSource.get(e.note.id) } returns e.note
        hiddenQuestsListener.onUnhid(e.questKey, e.createdTimestamp)
        verify { listener.onDeleted(listOf(e)) }
    }

    @Test fun `relays unhid all note quests`() {
        hiddenQuestsListener.onUnhidAll()
        verify { listener.onInvalidated() }
    }
}

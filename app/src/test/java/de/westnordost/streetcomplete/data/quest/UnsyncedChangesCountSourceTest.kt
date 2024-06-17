package de.westnordost.streetcomplete.data.quest

import de.westnordost.streetcomplete.data.UnsyncedChangesCountSource
import de.westnordost.streetcomplete.data.osm.edits.ElementEdit
import de.westnordost.streetcomplete.data.osm.edits.ElementEditAction
import de.westnordost.streetcomplete.data.osm.edits.ElementEditType
import de.westnordost.streetcomplete.data.osm.edits.ElementEditsSource
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestSource
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEditsSource
import de.westnordost.streetcomplete.data.osmnotes.notequests.OsmNoteQuestSource
import de.westnordost.streetcomplete.testutils.noteEdit
import de.westnordost.streetcomplete.testutils.pGeom
import de.westnordost.streetcomplete.testutils.verifyInvokedExactlyOnce
import io.mockative.Mock
import io.mockative.any
import io.mockative.classOf
import io.mockative.every
import io.mockative.mock
import kotlinx.coroutines.runBlocking
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class UnsyncedChangesCountSourceTest {
    @Mock private lateinit var osmQuestSource: OsmQuestSource
    @Mock private lateinit var osmNoteQuestSource: OsmNoteQuestSource
    @Mock private lateinit var noteEditsSource: NoteEditsSource
    @Mock private lateinit var elementEditsSource: ElementEditsSource

    @Mock private lateinit var elementEditType: ElementEditType
    @Mock private lateinit var elementEditAction: ElementEditAction

    private lateinit var noteQuestListener: OsmNoteQuestSource.Listener
    private lateinit var questListener: OsmQuestSource.Listener
    private lateinit var noteEditsListener: NoteEditsSource.Listener
    private lateinit var elementEditsListener: ElementEditsSource.Listener

    @Mock private lateinit var listener: UnsyncedChangesCountSource.Listener

    private lateinit var source: UnsyncedChangesCountSource

    private val baseCount = 3 + 4

    @BeforeTest fun setUp() {
        osmQuestSource = mock(classOf<OsmQuestSource>())
        every { osmQuestSource.addListener(any()) }.invokes { arguments ->
            questListener = arguments[0] as OsmQuestSource.Listener
            Unit
        }

        osmNoteQuestSource = mock(classOf<OsmNoteQuestSource>())
        every { osmNoteQuestSource.addListener(any()) }.invokes { arguments ->
            noteQuestListener = arguments[0] as OsmNoteQuestSource.Listener
            Unit
        }

        noteEditsSource = mock(classOf<NoteEditsSource>())
        every { noteEditsSource.addListener(any()) }.invokes { arguments ->
            noteEditsListener = arguments[0] as NoteEditsSource.Listener
            Unit
        }

        elementEditsSource = mock(classOf<ElementEditsSource>())
        every { elementEditsSource.addListener(any()) }.invokes { arguments ->
            elementEditsListener = arguments[0] as ElementEditsSource.Listener
            Unit
        }

        every { noteEditsSource.getUnsyncedCount() }.returns(3)
        every { elementEditsSource.getUnsyncedCount() }.returns(4)
        every { elementEditsSource.getPositiveUnsyncedCount() }.returns(2)

        source = UnsyncedChangesCountSource(noteEditsSource, elementEditsSource)

        listener = mock(classOf<UnsyncedChangesCountSource.Listener>())
        source.addListener(listener)
    }

    @Test fun count() = runBlocking {
        assertEquals(baseCount, source.getCount())
    }

    @Test fun `add unsynced element edit triggers listener`() {
        elementEditType = mock(classOf<ElementEditType>())
        elementEditAction = mock(classOf<ElementEditAction>())
        val edit = ElementEdit(1, elementEditType, pGeom(), "a", 1L, false, elementEditAction, true)
        // every { edit.isSynced }.returns(false)
        elementEditsListener.onAddedEdit(edit)
        verifyInvokedExactlyOnce { listener.onIncreased() }
    }

    @Test fun `remove unsynced element edit triggers listener`() {
        elementEditType = mock(classOf<ElementEditType>())
        elementEditAction = mock(classOf<ElementEditAction>())
        val edit = ElementEdit(1, elementEditType, pGeom(), "a", 1L, false, elementEditAction, true)
        elementEditsListener.onDeletedEdits(listOf(edit))
        verifyInvokedExactlyOnce { listener.onDecreased() }
    }

    @Test fun `add synced element edit does not trigger listener`() {
        elementEditType = mock(classOf<ElementEditType>())
        elementEditAction = mock(classOf<ElementEditAction>())
        val change = ElementEdit(1, elementEditType, pGeom(), "a", 1L, true, elementEditAction, true)
        elementEditsListener.onAddedEdit(change)
    }

    @Test fun `remove synced element edit does not trigger listener`() {
        elementEditType = mock(classOf<ElementEditType>())
        elementEditAction = mock(classOf<ElementEditAction>())
        val edit = ElementEdit(1, elementEditType, pGeom(), "a", 1L, true, elementEditAction, true)
        elementEditsListener.onDeletedEdits(listOf(edit))
    }

    @Test fun `add note edit triggers listener`() {
        noteEditsListener.onAddedEdit(noteEdit())
        verifyInvokedExactlyOnce { listener.onIncreased() }
    }

    @Test fun `remove note edit triggers listener`() {
        noteEditsListener.onDeletedEdits(listOf(noteEdit()))
        verifyInvokedExactlyOnce { listener.onDecreased() }
    }

    @Test fun `marked note edit synced triggers listener`() {
        noteEditsListener.onSyncedEdit(noteEdit())
        verifyInvokedExactlyOnce { listener.onDecreased() }
    }
}

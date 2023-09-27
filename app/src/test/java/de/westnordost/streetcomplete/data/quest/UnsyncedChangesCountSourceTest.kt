package de.westnordost.streetcomplete.data.quest

import de.westnordost.streetcomplete.data.UnsyncedChangesCountSource
import de.westnordost.streetcomplete.data.osm.edits.ElementEdit
import de.westnordost.streetcomplete.data.osm.edits.ElementEditsSource
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestSource
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEditsSource
import de.westnordost.streetcomplete.data.osmnotes.notequests.OsmNoteQuestSource
import de.westnordost.streetcomplete.testutils.any
import de.westnordost.streetcomplete.testutils.mock
import de.westnordost.streetcomplete.testutils.noteEdit
import de.westnordost.streetcomplete.testutils.on
import kotlinx.coroutines.runBlocking
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.invocation.InvocationOnMock
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class UnsyncedChangesCountSourceTest {
    private lateinit var osmQuestSource: OsmQuestSource
    private lateinit var osmNoteQuestSource: OsmNoteQuestSource
    private lateinit var noteEditsSource: NoteEditsSource
    private lateinit var elementEditsSource: ElementEditsSource

    private lateinit var noteQuestListener: OsmNoteQuestSource.Listener
    private lateinit var questListener: OsmQuestSource.Listener
    private lateinit var noteEditsListener: NoteEditsSource.Listener
    private lateinit var elementEditsListener: ElementEditsSource.Listener

    private lateinit var listener: UnsyncedChangesCountSource.Listener

    private lateinit var source: UnsyncedChangesCountSource

    private val baseCount = 3 + 4

    @BeforeTest fun setUp() {
        osmQuestSource = mock()
        on(osmQuestSource.addListener(any())).then { invocation: InvocationOnMock ->
            questListener = invocation.arguments[0] as OsmQuestSource.Listener
            Unit
        }

        osmNoteQuestSource = mock()
        on(osmNoteQuestSource.addListener(any())).then { invocation: InvocationOnMock ->
            noteQuestListener = invocation.arguments[0] as OsmNoteQuestSource.Listener
            Unit
        }

        noteEditsSource = mock()
        on(noteEditsSource.addListener(any())).then { invocation: InvocationOnMock ->
            noteEditsListener = invocation.arguments[0] as NoteEditsSource.Listener
            Unit
        }

        elementEditsSource = mock()
        on(elementEditsSource.addListener(any())).then { invocation: InvocationOnMock ->
            elementEditsListener = invocation.arguments[0] as ElementEditsSource.Listener
            Unit
        }

        on(noteEditsSource.getUnsyncedCount()).thenReturn(3)
        on(elementEditsSource.getUnsyncedCount()).thenReturn(4)
        on(elementEditsSource.getPositiveUnsyncedCount()).thenReturn(2)

        source = UnsyncedChangesCountSource(noteEditsSource, elementEditsSource)

        listener = mock()
        source.addListener(listener)
    }

    @Test fun count() = runBlocking {
        assertEquals(baseCount, source.getCount())
    }

    @Test fun `add unsynced element edit triggers listener`() {
        val edit = mock<ElementEdit>()
        on(edit.isSynced).thenReturn(false)
        elementEditsListener.onAddedEdit(edit)
        verify(listener).onIncreased()
    }

    @Test fun `remove unsynced element edit triggers listener`() {
        val edit = mock<ElementEdit>()
        on(edit.isSynced).thenReturn(false)
        elementEditsListener.onDeletedEdits(listOf(edit))
        verify(listener).onDecreased()
    }

    @Test fun `add synced element edit does not trigger listener`() {
        val change = mock<ElementEdit>()
        on(change.isSynced).thenReturn(true)
        elementEditsListener.onAddedEdit(change)
        verifyNoInteractions(listener)
    }

    @Test fun `remove synced element edit does not trigger listener`() {
        val edit = mock<ElementEdit>()
        on(edit.isSynced).thenReturn(true)
        elementEditsListener.onDeletedEdits(listOf(edit))
        verifyNoInteractions(listener)
    }

    @Test fun `add note edit triggers listener`() {
        noteEditsListener.onAddedEdit(noteEdit())
        verify(listener).onIncreased()
    }

    @Test fun `remove note edit triggers listener`() {
        noteEditsListener.onDeletedEdits(listOf(noteEdit()))
        verify(listener).onDecreased()
    }

    @Test fun `marked note edit synced triggers listener`() {
        noteEditsListener.onSyncedEdit(noteEdit())
        verify(listener).onDecreased()
    }
}

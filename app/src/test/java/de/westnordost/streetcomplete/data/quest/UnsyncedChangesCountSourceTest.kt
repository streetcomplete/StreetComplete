package de.westnordost.streetcomplete.data.quest

import de.westnordost.streetcomplete.testutils.any
import de.westnordost.streetcomplete.data.osm.edits.ElementEdit
import de.westnordost.streetcomplete.data.osm.edits.ElementEditsSource
import de.westnordost.streetcomplete.data.UnsyncedChangesCountSource
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestSource
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEditsSource
import de.westnordost.streetcomplete.data.osmnotes.notequests.OsmNoteQuestSource
import de.westnordost.streetcomplete.testutils.mock
import de.westnordost.streetcomplete.testutils.on
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*
import org.mockito.invocation.InvocationOnMock

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

    private val baseCount = 3+4

    @Before fun setUp() {
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

    @Test fun count() {
        assertEquals(baseCount, source.count)
    }

    @Test fun `add unsynced element change triggers listener`() {
        val change = mock<ElementEdit>()
        on(change.isSynced).thenReturn(false)
        elementEditsListener.onAddedEdit(change)
        verifyIncreased()
    }

    @Test fun `remove unsynced element change triggers listener`() {
        val change = mock<ElementEdit>()
        on(change.isSynced).thenReturn(false)
        elementEditsListener.onDeletedEdit(change)
        verifyDecreased()
    }

    @Test fun `add synced element change does not trigger listener`() {
        val change = mock<ElementEdit>()
        on(change.isSynced).thenReturn(true)
        elementEditsListener.onAddedEdit(change)
        verifyNoInteractions(listener)
    }

    @Test fun `remove synced element change does not trigger listener`() {
        val change = mock<ElementEdit>()
        on(change.isSynced).thenReturn(true)
        elementEditsListener.onDeletedEdit(change)
        verifyNoInteractions(listener)
    }

    @Test fun `add note edit triggers listener`() {
        noteEditsListener.onAddedEdit(mock())
        verifyIncreased()
    }

    @Test fun `remove note edit triggers listener`() {
        noteEditsListener.onDeletedEdit(mock())
        verifyDecreased()
    }

    @Test fun `markd note edit synced triggers listener`() {
        noteEditsListener.onSyncedEdit(mock())
        verifyDecreased()
    }

    private fun verifyDecreased() {
        verify(listener).onDecreased()
        assertEquals(baseCount - 1, source.count)
    }

    private fun verifyIncreased() {
        verify(listener).onIncreased()
        assertEquals(baseCount + 1, source.count)
    }
}

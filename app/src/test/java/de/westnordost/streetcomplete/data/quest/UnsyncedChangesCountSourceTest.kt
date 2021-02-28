package de.westnordost.streetcomplete.data.quest

import de.westnordost.streetcomplete.any
import de.westnordost.streetcomplete.data.osm.edits.ElementEdit
import de.westnordost.streetcomplete.data.osm.edits.ElementEditsSource
import de.westnordost.streetcomplete.data.UnsyncedChangesCountListener
import de.westnordost.streetcomplete.data.UnsyncedChangesCountSource
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestSource
import de.westnordost.streetcomplete.data.osmnotes.commentnotes.CommentNoteDao
import de.westnordost.streetcomplete.data.osmnotes.createnotes.CreateNoteDao
import de.westnordost.streetcomplete.data.osmnotes.notequests.OsmNoteQuestSource
import de.westnordost.streetcomplete.mock
import de.westnordost.streetcomplete.on
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyZeroInteractions
import org.mockito.invocation.InvocationOnMock

class UnsyncedChangesCountSourceTest {
    private lateinit var osmQuestSource: OsmQuestSource
    private lateinit var osmNoteQuestSource: OsmNoteQuestSource
    private lateinit var createNoteDao: CreateNoteDao
    private lateinit var commentNoteDao: CommentNoteDao
    private lateinit var elementEditsSource: ElementEditsSource

    private lateinit var noteQuestListener: OsmNoteQuestSource.Listener
    private lateinit var questListener: OsmQuestSource.Listener
    private lateinit var createNoteListener: CreateNoteDao.Listener
    private lateinit var commentNoteListener: CommentNoteDao.Listener
    private lateinit var elementEditsListener: ElementEditsSource.Listener

    private lateinit var listener: UnsyncedChangesCountListener

    private lateinit var source: UnsyncedChangesCountSource

    private val baseCount = 2+3+4

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

        createNoteDao = mock()
        on(createNoteDao.addListener(any())).then { invocation: InvocationOnMock ->
            createNoteListener = invocation.arguments[0] as CreateNoteDao.Listener
            Unit
        }

        commentNoteDao = mock()
        on(commentNoteDao.addListener(any())).then { invocation: InvocationOnMock ->
            commentNoteListener = invocation.arguments[0] as CommentNoteDao.Listener
            Unit
        }

        elementEditsSource = mock()
        on(elementEditsSource.addListener(any())).then { invocation: InvocationOnMock ->
            elementEditsListener = invocation.arguments[0] as ElementEditsSource.Listener
            Unit
        }

        on(commentNoteDao.getCount()).thenReturn(2)
        on(createNoteDao.getCount()).thenReturn(3)
        on(elementEditsSource.getUnsyncedCount()).thenReturn(4)
        on(elementEditsSource.getPositiveUnsyncedCount()).thenReturn(2)

        source = UnsyncedChangesCountSource(commentNoteDao, createNoteDao, elementEditsSource)

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
        verifyZeroInteractions(listener)
    }

    @Test fun `remove synced element change does not trigger listener`() {
        val change = mock<ElementEdit>()
        on(change.isSynced).thenReturn(true)
        elementEditsListener.onDeletedEdit(change)
        verifyZeroInteractions(listener)
    }

    @Test fun `add create note triggers listener`() {
        createNoteListener.onAddedCreateNote()
        verifyIncreased()
    }

    @Test fun `remove create note triggers listener`() {
        createNoteListener.onDeletedCreateNote()
        verifyDecreased()
    }

    @Test fun `add comment note triggers listener`() {
        commentNoteListener.onAddedCommentNote(mock())
        verifyIncreased()
    }

    @Test fun `remove comment note triggers listener`() {
        commentNoteListener.onDeletedCommentNote(0L)
        verifyDecreased()
    }

    private fun verifyDecreased() {
        verify(listener).onUnsyncedChangesCountDecreased()
        assertEquals(baseCount - 1, source.count)
    }

    private fun verifyIncreased() {
        verify(listener).onUnsyncedChangesCountIncreased()
        assertEquals(baseCount + 1, source.count)
    }
}

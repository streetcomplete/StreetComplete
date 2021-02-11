package de.westnordost.streetcomplete.data.quest

import de.westnordost.streetcomplete.any
import de.westnordost.streetcomplete.data.osm.changes.UnsyncedChangesCountListener
import de.westnordost.streetcomplete.data.osm.changes.UnsyncedChangesCountSource
import de.westnordost.streetcomplete.data.osm.delete_element.DeleteOsmElementDao
import de.westnordost.streetcomplete.data.osm.osmquest.OsmQuestSource
import de.westnordost.streetcomplete.data.osm.osmquest.changes.OsmElementTagChangesDao
import de.westnordost.streetcomplete.data.osm.splitway.SplitOsmWayDao
import de.westnordost.streetcomplete.data.osmnotes.commentnotes.CommentNoteDao
import de.westnordost.streetcomplete.data.osmnotes.createnotes.CreateNoteDao
import de.westnordost.streetcomplete.data.osmnotes.notequests.OsmNoteQuestSource
import de.westnordost.streetcomplete.mock
import de.westnordost.streetcomplete.on
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.verify
import org.mockito.invocation.InvocationOnMock

class UnsyncedChangesCountSourceTest {
    private lateinit var osmQuestSource: OsmQuestSource
    private lateinit var osmNoteQuestSource: OsmNoteQuestSource
    private lateinit var createNoteDao: CreateNoteDao
    private lateinit var commentNoteDao: CommentNoteDao
    private lateinit var splitWayDao: SplitOsmWayDao
    private lateinit var deleteOsmElementDao: DeleteOsmElementDao
    private lateinit var osmElementTagChangesDao: OsmElementTagChangesDao

    private lateinit var noteQuestListener: OsmNoteQuestSource.Listener
    private lateinit var questListener: OsmQuestSource.Listener
    private lateinit var createNoteListener: CreateNoteDao.Listener
    private lateinit var commentNoteListener: CommentNoteDao.Listener
    private lateinit var osmElementTagChangesListener: OsmElementTagChangesDao.Listener
    private lateinit var splitWayListener: SplitOsmWayDao.Listener
    private lateinit var deleteElementListener: DeleteOsmElementDao.Listener

    private lateinit var listener: UnsyncedChangesCountListener

    private lateinit var source: UnsyncedChangesCountSource

    private val baseCount = 2+3+4+5

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

        splitWayDao = mock()
        on(splitWayDao.addListener(any())).then { invocation: InvocationOnMock ->
            splitWayListener = invocation.arguments[0] as SplitOsmWayDao.Listener
            Unit
        }

        deleteOsmElementDao = mock()
        on(deleteOsmElementDao.addListener(any())).then { invocation: InvocationOnMock ->
            deleteElementListener = invocation.arguments[0] as DeleteOsmElementDao.Listener
            Unit
        }

        osmElementTagChangesDao = mock()
        on(osmElementTagChangesDao.addListener(any())).then { invocation: InvocationOnMock ->
            osmElementTagChangesListener = invocation.arguments[0] as OsmElementTagChangesDao.Listener
            Unit
        }

        on(commentNoteDao.getCount()).thenReturn(2)
        on(createNoteDao.getCount()).thenReturn(3)
        on(splitWayDao.getCount()).thenReturn(4)
        on(osmElementTagChangesDao.getCount()).thenReturn(5)

        source = UnsyncedChangesCountSource(
            commentNoteDao,
            createNoteDao,
            splitWayDao,
            deleteOsmElementDao,
            osmElementTagChangesDao)

        listener = mock()
        source.addListener(listener)
    }

    @Test fun count() {
        assertEquals(baseCount, source.count)
    }

    @Test fun `add element tag change triggers listener`() {
        osmElementTagChangesListener.onAddedElementTagChanges()
        verifyIncreased()
    }

    @Test fun `remove element tag change triggers listener`() {
        osmElementTagChangesListener.onDeletedElementTagChanges()
        verifyDecreased()
    }

    @Test fun `add split way triggers listener`() {
        splitWayListener.onAddedSplitWay()
        verifyIncreased()
    }

    @Test fun `remove split way triggers listener`() {
        splitWayListener.onDeletedSplitWay()
        verifyDecreased()
    }

    @Test fun `add delete element triggers listener`() {
        deleteElementListener.onAddedDeleteOsmElement()
        verifyIncreased()
    }

    @Test fun `remove delete element triggers listener`() {
        deleteElementListener.onDeletedDeleteOsmElement()
        verifyDecreased()
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

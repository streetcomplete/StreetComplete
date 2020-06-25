package de.westnordost.streetcomplete.data.quest

import de.westnordost.osmapi.map.data.Element
import de.westnordost.osmapi.map.data.OsmLatLon
import de.westnordost.streetcomplete.any
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementPointGeometry
import de.westnordost.streetcomplete.data.osm.osmquest.OsmQuest
import de.westnordost.streetcomplete.data.osm.osmquest.OsmQuestController
import de.westnordost.streetcomplete.data.osm.osmquest.undo.UndoOsmQuestDao
import de.westnordost.streetcomplete.data.osm.splitway.OsmQuestSplitWayDao
import de.westnordost.streetcomplete.data.osmnotes.createnotes.CreateNoteDao
import de.westnordost.streetcomplete.data.osmnotes.notequests.OsmNoteQuest
import de.westnordost.streetcomplete.data.osmnotes.notequests.OsmNoteQuestController
import de.westnordost.streetcomplete.data.osmnotes.notequests.OsmNoteQuestType
import de.westnordost.streetcomplete.mock
import de.westnordost.streetcomplete.on
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyZeroInteractions
import org.mockito.invocation.InvocationOnMock
import java.util.*

class UnsyncedChangesCountSourceTest {
    private lateinit var osmQuestController: OsmQuestController
    private lateinit var osmNoteQuestController: OsmNoteQuestController
    private lateinit var createNoteDao: CreateNoteDao
    private lateinit var splitWayDao: OsmQuestSplitWayDao
    private lateinit var undoOsmQuestDao: UndoOsmQuestDao

    private lateinit var noteQuestStatusListener: OsmNoteQuestController.QuestStatusListener
    private lateinit var questStatusListener: OsmQuestController.QuestStatusListener
    private lateinit var createNoteListener: CreateNoteDao.Listener
    private lateinit var undoOsmQuestListener: UndoOsmQuestDao.Listener
    private lateinit var splitWayListener: OsmQuestSplitWayDao.Listener

    private lateinit var listener: UnsyncedChangesCountListener

    private lateinit var source: UnsyncedChangesCountSource

    private val baseCount = 1+2+3+4+5

    @Before fun setUp() {
        osmQuestController = mock()
        on(osmQuestController.addQuestStatusListener(any())).then { invocation: InvocationOnMock ->
            questStatusListener = invocation.arguments[0] as OsmQuestController.QuestStatusListener
            Unit
        }

        osmNoteQuestController = mock()
        on(osmNoteQuestController.addQuestStatusListener(any())).then { invocation: InvocationOnMock ->
            noteQuestStatusListener = invocation.arguments[0] as OsmNoteQuestController.QuestStatusListener
            Unit
        }

        createNoteDao = mock()
        on(createNoteDao.addListener(any())).then { invocation: InvocationOnMock ->
            createNoteListener = invocation.arguments[0] as CreateNoteDao.Listener
            Unit
        }

        splitWayDao = mock()
        on(splitWayDao.addListener(any())).then { invocation: InvocationOnMock ->
            splitWayListener = invocation.arguments[0] as OsmQuestSplitWayDao.Listener
            Unit
        }

        undoOsmQuestDao = mock()
        on(undoOsmQuestDao.addListener(any())).then { invocation: InvocationOnMock ->
            undoOsmQuestListener = invocation.arguments[0] as UndoOsmQuestDao.Listener
            Unit
        }

        on(osmQuestController.getAllAnsweredCount()).thenReturn(1)
        on(osmNoteQuestController.getAllAnsweredCount()).thenReturn(2)
        on(createNoteDao.getCount()).thenReturn(3)
        on(splitWayDao.getCount()).thenReturn(4)
        on(undoOsmQuestDao.getCount()).thenReturn(5)

        source = UnsyncedChangesCountSource(osmQuestController, osmNoteQuestController, createNoteDao, splitWayDao, undoOsmQuestDao)

        listener = mock()
        source.addListener(listener)
    }

    @Test fun count() {
        assertEquals(baseCount, source.count)
    }

    @Test fun `add undo quest triggers listener`() {
        undoOsmQuestListener.onAddedUndoOsmQuest()
        verifyIncreased()
    }

    @Test fun `remove undo quest triggers listener`() {
        undoOsmQuestListener.onDeletedUndoOsmQuest()
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

    @Test fun `add create note triggers listener`() {
        createNoteListener.onAddedCreateNote()
        verifyIncreased()
    }

    @Test fun `remove create note triggers listener`() {
        createNoteListener.onDeletedCreateNote()
        verifyDecreased()
    }

    @Test fun `remove answered osm quest triggers listener`() {
        questStatusListener.onRemoved(1L, QuestStatus.ANSWERED)
        verifyDecreased()
    }

    @Test fun `remove non-answered osm quest does not trigger listener`() {
        questStatusListener.onRemoved(2L, QuestStatus.NEW)
        questStatusListener.onRemoved(3L, QuestStatus.INVISIBLE)
        questStatusListener.onRemoved(4L, QuestStatus.REVERT)
        questStatusListener.onRemoved(5L, QuestStatus.CLOSED)
        questStatusListener.onRemoved(6L, QuestStatus.HIDDEN)
        verifyNothingHappened()
    }

    @Test fun `change osm quest to answered triggers listener`() {
        questStatusListener.onChanged(osmQuest(1L, QuestStatus.ANSWERED), QuestStatus.NEW)
        verifyIncreased()
    }

    @Test fun `change osm quest from answered triggers listener`() {
        questStatusListener.onChanged(osmQuest(1L, QuestStatus.CLOSED), QuestStatus.ANSWERED)
        verifyDecreased()
    }

    @Test fun `change osm quest from non-answered does not trigger listener`() {
        questStatusListener.onChanged(osmQuest(1L, QuestStatus.REVERT), QuestStatus.CLOSED)
        verifyNothingHappened()
    }

    @Test fun `update of osm quests triggers listener`() {
        on(osmQuestController.getAllAnsweredCount()).thenReturn(101)
        questStatusListener.onUpdated(listOf(), listOf(), listOf())
        verify(listener).onUnsyncedChangesCountIncreased()
        assertEquals(baseCount + 100, source.count)
    }

    @Test fun `remove answered osm note quest triggers listener`() {
        noteQuestStatusListener.onRemoved(1L, QuestStatus.ANSWERED)
        verifyDecreased()
    }

    @Test fun `remove non-answered osm note quest does not trigger listener`() {
        noteQuestStatusListener.onRemoved(2L, QuestStatus.NEW)
        noteQuestStatusListener.onRemoved(3L, QuestStatus.INVISIBLE)
        noteQuestStatusListener.onRemoved(4L, QuestStatus.REVERT)
        noteQuestStatusListener.onRemoved(5L, QuestStatus.CLOSED)
        noteQuestStatusListener.onRemoved(6L, QuestStatus.HIDDEN)
        verifyNothingHappened()
    }

    @Test fun `change osm note quest to answered triggers listener`() {
        noteQuestStatusListener.onChanged(osmNoteQuest(1L, QuestStatus.ANSWERED), QuestStatus.NEW)
        verifyIncreased()
    }

    @Test fun `change osm note quest from answered triggers listener`() {
        noteQuestStatusListener.onChanged(osmNoteQuest(1L, QuestStatus.CLOSED), QuestStatus.ANSWERED)
        verifyDecreased()
    }

    @Test fun `change osm note quest from non-answered does not trigger listener`() {
        noteQuestStatusListener.onChanged(osmNoteQuest(1L, QuestStatus.REVERT), QuestStatus.CLOSED)
        verifyNothingHappened()
    }

    @Test fun `add answered osm note quest triggers listener`() {
        noteQuestStatusListener.onAdded(osmNoteQuest(1L, QuestStatus.ANSWERED))
        verifyIncreased()
    }

    @Test fun `add non-answered osm note quest does not trigger listener`() {
        noteQuestStatusListener.onAdded(osmNoteQuest(1L, QuestStatus.NEW))
        verifyNothingHappened()
    }

    @Test fun `update of osm note quests triggers listener`() {
        on(osmNoteQuestController.getAllAnsweredCount()).thenReturn(102)
        noteQuestStatusListener.onUpdated(listOf(), listOf(), listOf())
        verify(listener).onUnsyncedChangesCountIncreased()
        assertEquals(baseCount + 100, source.count)
    }

    private fun verifyDecreased() {
        verify(listener).onUnsyncedChangesCountDecreased()
        assertEquals(baseCount - 1, source.count)
    }

    private fun verifyIncreased() {
        verify(listener).onUnsyncedChangesCountIncreased()
        assertEquals(baseCount + 1, source.count)
    }

    private fun verifyNothingHappened() {
        verifyZeroInteractions(listener)
        assertEquals(baseCount, source.count)
    }

    private fun osmQuest(id: Long, status: QuestStatus): OsmQuest {
        return OsmQuest(id, mock(), Element.Type.NODE, 1L, status, null, null, Date(), ElementPointGeometry(OsmLatLon(0.0,0.0)))
    }

    private fun osmNoteQuest(id: Long, status: QuestStatus): OsmNoteQuest {
        return OsmNoteQuest(id, mock(), status, "", Date(), OsmNoteQuestType(), null)
    }
}
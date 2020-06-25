package de.westnordost.streetcomplete.data.quest

import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.Element
import de.westnordost.osmapi.map.data.OsmLatLon
import de.westnordost.streetcomplete.any
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementPointGeometry
import de.westnordost.streetcomplete.data.osm.osmquest.OsmQuest
import de.westnordost.streetcomplete.data.osm.osmquest.OsmQuestController
import de.westnordost.streetcomplete.data.osmnotes.notequests.OsmNoteQuest
import de.westnordost.streetcomplete.data.osmnotes.notequests.OsmNoteQuestController
import de.westnordost.streetcomplete.data.osmnotes.notequests.OsmNoteQuestType
import de.westnordost.streetcomplete.mock
import de.westnordost.streetcomplete.on
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyZeroInteractions
import org.mockito.invocation.InvocationOnMock
import java.util.*

class VisibleQuestsSourceTest {

    private lateinit var osmQuestController: OsmQuestController
    private lateinit var osmNoteQuestController: OsmNoteQuestController
    private lateinit var source: VisibleQuestsSource

    private lateinit var noteQuestStatusListener: OsmNoteQuestController.QuestStatusListener
    private lateinit var questStatusListener: OsmQuestController.QuestStatusListener

    private lateinit var listener: VisibleQuestListener

    private val bbox = BoundingBox(0.0,0.0,1.0,1.0)
    private val questTypes = listOf("a","b","c")

    @Before fun setUp() {
        osmNoteQuestController = mock()
        osmQuestController = mock()

        on(osmNoteQuestController.addQuestStatusListener(any())).then { invocation: InvocationOnMock ->
            noteQuestStatusListener = (invocation.arguments[0] as OsmNoteQuestController.QuestStatusListener)
            Unit
        }
        on(osmQuestController.addQuestStatusListener(any())).then { invocation: InvocationOnMock ->
            questStatusListener = (invocation.arguments[0] as OsmQuestController.QuestStatusListener)
            Unit
        }

        source = VisibleQuestsSource(osmQuestController, osmNoteQuestController)

        listener = mock()
        source.addListener(listener)
    }

    @Test fun getAllVisibleCount() {
        on(osmQuestController.getAllVisibleInBBoxCount(bbox, questTypes)).thenReturn(3)
        on(osmNoteQuestController.getAllVisibleInBBoxCount(bbox)).thenReturn(4)

        assertEquals(7, source.getAllVisibleCount(bbox, questTypes))
    }

    @Test fun getAllVisible() {
        on(osmQuestController.getAllVisibleInBBox(bbox, questTypes)).thenReturn(listOf(mock(), mock(), mock()))
        on(osmNoteQuestController.getAllVisibleInBBox(bbox)).thenReturn(listOf(mock(), mock()))

        val quests = source.getAllVisible(bbox, questTypes)
        assertEquals(5, quests.size)
        val osmQuests = quests.filter { it.group == QuestGroup.OSM && it.quest is OsmQuest }
        assertEquals(3, osmQuests.size)
        val osmNoteQuests = quests.filter { it.group == QuestGroup.OSM_NOTE && it.quest is OsmNoteQuest }
        assertEquals(2, osmNoteQuests.size)
    }

    @Test fun `removal of new osm quest triggers listener`() {
        questStatusListener.onRemoved(123L, QuestStatus.NEW)
        verify(listener).onUpdatedVisibleQuests(listOf(), listOf(123L), QuestGroup.OSM)
    }

    @Test fun `removal of other osm quests does not trigger listener`() {
        questStatusListener.onRemoved(1L, QuestStatus.ANSWERED)
        questStatusListener.onRemoved(2L, QuestStatus.INVISIBLE)
        questStatusListener.onRemoved(3L, QuestStatus.HIDDEN)
        questStatusListener.onRemoved(4L, QuestStatus.REVERT)
        questStatusListener.onRemoved(5L, QuestStatus.CLOSED)
        verifyZeroInteractions(listener)
    }

    @Test fun `change of osm quest status to new triggers listener`() {
        val q = osmQuest(1L, QuestStatus.NEW)
        questStatusListener.onChanged(q, QuestStatus.HIDDEN)
        verify(listener).onUpdatedVisibleQuests(listOf(q), listOf(), QuestGroup.OSM)
    }

    @Test fun `change of osm quest status from new triggers listener`() {
        questStatusListener.onChanged(osmQuest(1L, QuestStatus.HIDDEN), QuestStatus.NEW)
        verify(listener).onUpdatedVisibleQuests(listOf(), listOf(1L), QuestGroup.OSM)
    }

    @Test fun `change of osm quest status not involving new does not trigger listener`() {
        questStatusListener.onChanged(osmQuest(1L, QuestStatus.ANSWERED), QuestStatus.CLOSED)
        questStatusListener.onChanged(osmQuest(2L, QuestStatus.HIDDEN), QuestStatus.INVISIBLE)
        questStatusListener.onChanged(osmQuest(3L, QuestStatus.CLOSED), QuestStatus.REVERT)
        verifyZeroInteractions(listener)
    }

    @Test fun `update of osm quests triggers listener`() {
        val q1 = osmQuest(1L, QuestStatus.NEW)
        val q2 = osmQuest(2L, QuestStatus.INVISIBLE) // not visible

        val q3 = osmQuest(3L, QuestStatus.NEW)
        val q4 = osmQuest(4L, QuestStatus.CLOSED)

        val added = listOf(q1, q2)
        val updated = listOf(q3, q4)
        val deleted = listOf(5L, 6L)
        questStatusListener.onUpdated(added, updated, deleted)
        verify(listener).onUpdatedVisibleQuests(listOf(q1, q3), listOf(4L, 5L, 6L), QuestGroup.OSM)
    }

    @Test fun `addition of new osm note quest triggers listener`() {
        val q = osmNoteQuest(123L, QuestStatus.NEW)
        noteQuestStatusListener.onAdded(q)
        verify(listener).onUpdatedVisibleQuests(listOf(q), listOf(), QuestGroup.OSM_NOTE)
    }

    @Test fun `addition of not-new osm note quest does not trigger listener`() {
        val q = osmNoteQuest(123L, QuestStatus.INVISIBLE)
        noteQuestStatusListener.onAdded(q)
        verifyZeroInteractions(listener)
    }

    @Test fun `removal of new osm note quest triggers listener`() {
        noteQuestStatusListener.onRemoved(123L, QuestStatus.NEW)
        verify(listener).onUpdatedVisibleQuests(listOf(), listOf(123L), QuestGroup.OSM_NOTE)
    }

    @Test fun `removal of other osm note quests does not trigger listener`() {
        noteQuestStatusListener.onRemoved(1L, QuestStatus.ANSWERED)
        noteQuestStatusListener.onRemoved(2L, QuestStatus.INVISIBLE)
        noteQuestStatusListener.onRemoved(3L, QuestStatus.HIDDEN)
        noteQuestStatusListener.onRemoved(4L, QuestStatus.REVERT)
        noteQuestStatusListener.onRemoved(5L, QuestStatus.CLOSED)
        verifyZeroInteractions(listener)
    }

    @Test fun `change of osm note quest status to new triggers listener`() {
        val q = osmNoteQuest(1L, QuestStatus.NEW)
        noteQuestStatusListener.onChanged(q, QuestStatus.HIDDEN)
        verify(listener).onUpdatedVisibleQuests(listOf(q), listOf(), QuestGroup.OSM_NOTE)
    }

    @Test fun `change of osm note quest status not involving new does not trigger listener`() {
        noteQuestStatusListener.onChanged(osmNoteQuest(1L, QuestStatus.ANSWERED), QuestStatus.CLOSED)
        noteQuestStatusListener.onChanged(osmNoteQuest(2L, QuestStatus.HIDDEN), QuestStatus.INVISIBLE)
        noteQuestStatusListener.onChanged(osmNoteQuest(3L, QuestStatus.CLOSED), QuestStatus.REVERT)
        verifyZeroInteractions(listener)
    }

    @Test fun `update of osm note quests triggers listener`() {
        val q1 = osmNoteQuest(1L, QuestStatus.NEW)
        val q2 = osmNoteQuest(2L, QuestStatus.INVISIBLE) // not visible

        val q3 = osmNoteQuest(3L, QuestStatus.NEW)
        val q4 = osmNoteQuest(4L, QuestStatus.CLOSED)

        val added = listOf(q1, q2)
        val updated = listOf(q3, q4)
        val deleted = listOf(5L, 6L)
        noteQuestStatusListener.onUpdated(added, updated, deleted)
        verify(listener).onUpdatedVisibleQuests(listOf(q1, q3), listOf(4L, 5L, 6L), QuestGroup.OSM_NOTE)
    }

    private fun osmQuest(id: Long, status: QuestStatus): OsmQuest {
        return OsmQuest(id, mock(), Element.Type.NODE, 1L, status, null, null, Date(), ElementPointGeometry(OsmLatLon(0.0,0.0)))
    }

    private fun osmNoteQuest(id: Long, status: QuestStatus): OsmNoteQuest {
        return OsmNoteQuest(id, mock(), status, "", Date(), OsmNoteQuestType(), null)
    }
}


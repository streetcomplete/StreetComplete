package de.westnordost.streetcomplete.data.quest

import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuest
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestSource
import de.westnordost.streetcomplete.data.osmnotes.notequests.OsmNoteQuest
import de.westnordost.streetcomplete.data.osmnotes.notequests.OsmNoteQuestSource
import de.westnordost.streetcomplete.data.visiblequests.TeamModeQuestFilter
import de.westnordost.streetcomplete.data.visiblequests.VisibleQuestTypeSource
import de.westnordost.streetcomplete.testutils.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions

class VisibleQuestsSourceTest {

    private lateinit var osmQuestSource: OsmQuestSource
    private lateinit var questTypeRegistry: QuestTypeRegistry
    private lateinit var osmNoteQuestSource: OsmNoteQuestSource
    private lateinit var visibleQuestTypeSource: VisibleQuestTypeSource
    private lateinit var teamModeQuestFilter: TeamModeQuestFilter
    private lateinit var source: VisibleQuestsSource

    private lateinit var noteQuestListener: OsmNoteQuestSource.Listener
    private lateinit var questListener: OsmQuestSource.Listener
    private lateinit var visibleQuestTypeListener: VisibleQuestTypeSource.Listener
    private lateinit var teamModeListener: TeamModeQuestFilter.TeamModeChangeListener

    private lateinit var listener: VisibleQuestsSource.Listener

    private val bbox = bbox(0.0,0.0,1.0,1.0)
    private val questTypes = listOf(TestQuestTypeA(),TestQuestTypeB(), TestQuestTypeC())
    private val questTypeNames = questTypes.map { it::class.simpleName!! }

    @Before fun setUp() {
        osmNoteQuestSource = mock()
        osmQuestSource = mock()
        visibleQuestTypeSource = mock()
        teamModeQuestFilter = mock()
        questTypeRegistry = mock()

        on(questTypeRegistry.all).thenReturn(questTypes)

        on(visibleQuestTypeSource.isVisible(any())).thenReturn(true)
        on(teamModeQuestFilter.isVisible(any())).thenReturn(true)

        on(osmNoteQuestSource.addListener(any())).then { invocation ->
            noteQuestListener = (invocation.arguments[0] as OsmNoteQuestSource.Listener)
            Unit
        }
        on(osmQuestSource.addListener(any())).then { invocation ->
            questListener = (invocation.arguments[0] as OsmQuestSource.Listener)
            Unit
        }
        on(visibleQuestTypeSource.addListener(any())).then { invocation ->
            visibleQuestTypeListener = (invocation.arguments[0] as VisibleQuestTypeSource.Listener)
            Unit
        }
        on(teamModeQuestFilter.addListener(any())).then { invocation ->
            teamModeListener = (invocation.arguments[0] as TeamModeQuestFilter.TeamModeChangeListener)
            Unit
        }

        source = VisibleQuestsSource(questTypeRegistry, osmQuestSource, osmNoteQuestSource, visibleQuestTypeSource, teamModeQuestFilter)

        listener = mock()
        source.addListener(listener)
    }

    @Test fun getAllVisible() {
        on(osmQuestSource.getAllVisibleInBBox(bbox, questTypeNames)).thenReturn(listOf(mock(), mock(), mock()))
        on(osmNoteQuestSource.getAllVisibleInBBox(bbox)).thenReturn(listOf(mock(), mock()))

        val quests = source.getAllVisible(bbox)
        assertEquals(5, quests.size)
        assertEquals(3, quests.filterIsInstance<OsmQuest>().size)
        assertEquals(2, quests.filterIsInstance<OsmNoteQuest>().size)
    }

    @Test fun `getAllVisible returns only quests of types that are visible`() {
        val t1 = TestQuestTypeA()
        val t2 = TestQuestTypeB()
        val q1 = osmQuest(elementId = 1, questType = t1)
        val q2 = osmQuest(elementId = 2, questType = t2)
        on(osmQuestSource.getAllVisibleInBBox(bbox, questTypeNames)).thenReturn(listOf(q1, q2))
        on(visibleQuestTypeSource.isVisible(t1)).thenReturn(false)
        on(visibleQuestTypeSource.isVisible(t2)).thenReturn(true)


        val quests = source.getAllVisible(bbox)
        assertEquals(q2, quests.single())
    }

    @Test fun `getAllVisible does not return those that are invisible in team mode`() {
        on(osmQuestSource.getAllVisibleInBBox(bbox, questTypeNames)).thenReturn(listOf(mock()))
        on(osmNoteQuestSource.getAllVisibleInBBox(bbox)).thenReturn(listOf(mock()))
        on(teamModeQuestFilter.isVisible(any())).thenReturn(false)

        val quests = source.getAllVisible(bbox)
        assertTrue(quests.isEmpty())
    }

    @Test fun `osm quests added or removed triggers listener`() {
        val quests = listOf(osmQuest(elementId = 1), osmQuest(elementId = 2))
        val deleted = listOf(osmQuestKey(elementId = 3), osmQuestKey(elementId = 4))
        questListener.onUpdated(quests, deleted)
        verify(listener).onUpdatedVisibleQuests(eq(quests), eq(deleted))
    }

    @Test fun `osm quests added of invisible type does not trigger listener`() {
        val quests = listOf(osmQuest(elementId = 1), osmQuest(elementId = 2))
        on(visibleQuestTypeSource.isVisible(any())).thenReturn(false)
        questListener.onUpdated(quests, emptyList())
        verifyNoInteractions(listener)
    }

    @Test fun `osm note quests added or removed triggers listener`() {
        val quests = listOf(osmNoteQuest(1L), osmNoteQuest(2L))
        val deleted = listOf(OsmNoteQuestKey(3), OsmNoteQuestKey(4))
        noteQuestListener.onUpdated(quests, listOf(3L, 4L))
        verify(listener).onUpdatedVisibleQuests(eq(quests), eq(deleted))
    }

    @Test fun `osm note quests added of invisible type does not trigger listener`() {
        val quests = listOf(osmNoteQuest(1L), osmNoteQuest(2L))
        on(visibleQuestTypeSource.isVisible(any())).thenReturn(false)
        noteQuestListener.onUpdated(quests, emptyList())
        verifyNoInteractions(listener)
    }

    @Test fun `trigger invalidate listener if quest type visibilities changed`() {
        visibleQuestTypeListener.onQuestTypeVisibilitiesChanged()
        verify(listener).onVisibleQuestsInvalidated()
    }

    @Test fun `trigger invalidate listener if visible note quests were invalidated`() {
        noteQuestListener.onInvalidated()
        verify(listener).onVisibleQuestsInvalidated()
    }
}

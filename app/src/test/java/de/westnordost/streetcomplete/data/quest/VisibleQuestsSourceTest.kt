package de.westnordost.streetcomplete.data.quest

import de.westnordost.streetcomplete.data.download.tiles.asBoundingBoxOfEnclosingTiles
import de.westnordost.streetcomplete.data.osm.geometry.ElementPointGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuest
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestSource
import de.westnordost.streetcomplete.data.osmnotes.notequests.OsmNoteQuest
import de.westnordost.streetcomplete.data.osmnotes.notequests.OsmNoteQuestSource
import de.westnordost.streetcomplete.data.overlays.SelectedOverlaySource
import de.westnordost.streetcomplete.data.visiblequests.TeamModeQuestFilter
import de.westnordost.streetcomplete.data.visiblequests.VisibleQuestTypeSource
import de.westnordost.streetcomplete.overlays.Overlay
import de.westnordost.streetcomplete.testutils.any
import de.westnordost.streetcomplete.testutils.bbox
import de.westnordost.streetcomplete.testutils.eq
import de.westnordost.streetcomplete.testutils.mock
import de.westnordost.streetcomplete.testutils.on
import de.westnordost.streetcomplete.testutils.osmNoteQuest
import de.westnordost.streetcomplete.testutils.osmQuest
import de.westnordost.streetcomplete.testutils.osmQuestKey
import de.westnordost.streetcomplete.testutils.pGeom
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class VisibleQuestsSourceTest {

    private lateinit var osmQuestSource: OsmQuestSource
    private lateinit var questTypeRegistry: QuestTypeRegistry
    private lateinit var osmNoteQuestSource: OsmNoteQuestSource
    private lateinit var visibleQuestTypeSource: VisibleQuestTypeSource
    private lateinit var teamModeQuestFilter: TeamModeQuestFilter
    private lateinit var selectedOverlaySource: SelectedOverlaySource
    private lateinit var source: VisibleQuestsSource

    private lateinit var noteQuestListener: OsmNoteQuestSource.Listener
    private lateinit var questListener: OsmQuestSource.Listener
    private lateinit var visibleQuestTypeListener: VisibleQuestTypeSource.Listener
    private lateinit var teamModeListener: TeamModeQuestFilter.TeamModeChangeListener
    private lateinit var selectedOverlayListener: SelectedOverlaySource.Listener

    private lateinit var listener: VisibleQuestsSource.Listener

    private val bbox = bbox(0.0, 0.0, 1.0, 1.0)
    private val questTypes = listOf(TestQuestTypeA(), TestQuestTypeB(), TestQuestTypeC())
    private val questTypeNames = questTypes.map { it.name }

    @BeforeTest fun setUp() {
        osmNoteQuestSource = mock()
        osmQuestSource = mock()
        visibleQuestTypeSource = mock()
        teamModeQuestFilter = mock()
        selectedOverlaySource = mock()
        questTypeRegistry = QuestTypeRegistry(questTypes.mapIndexed { index, questType -> index to questType })

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
        on(selectedOverlaySource.addListener(any())).then { invocation ->
            selectedOverlayListener = (invocation.arguments[0] as SelectedOverlaySource.Listener)
            Unit
        }

        source = VisibleQuestsSource(questTypeRegistry, osmQuestSource, osmNoteQuestSource, visibleQuestTypeSource, teamModeQuestFilter, selectedOverlaySource)

        listener = mock()
        source.addListener(listener)
    }

    @Test fun getAllVisible() {
        val bboxCacheWillRequest = bbox.asBoundingBoxOfEnclosingTiles(16)
        val osmQuests = questTypes.map { OsmQuest(it, ElementType.NODE, 1L, pGeom()) }
        val noteQuests = listOf(OsmNoteQuest(0L, LatLon(0.0, 0.0)), OsmNoteQuest(1L, LatLon(1.0, 1.0)))
        on(osmQuestSource.getAllVisibleInBBox(bboxCacheWillRequest, questTypeNames)).thenReturn(osmQuests)
        on(osmNoteQuestSource.getAllVisibleInBBox(bboxCacheWillRequest)).thenReturn(noteQuests)

        val quests = source.getAllVisible(bbox)
        assertEquals(5, quests.size)
        assertEquals(3, quests.filterIsInstance<OsmQuest>().size)
        assertEquals(2, quests.filterIsInstance<OsmNoteQuest>().size)
    }

    @Test fun `getAllVisible does not return those that are invisible in team mode`() {
        on(osmQuestSource.getAllVisibleInBBox(bbox, questTypeNames)).thenReturn(listOf(mock()))
        on(osmNoteQuestSource.getAllVisibleInBBox(bbox)).thenReturn(listOf(mock()))
        on(teamModeQuestFilter.isVisible(any())).thenReturn(false)
        on(teamModeQuestFilter.isEnabled).thenReturn(true)

        val quests = source.getAllVisible(bbox)
        assertTrue(quests.isEmpty())
    }

    @Test fun `getAllVisible does not return those that are invisible because of an overlay`() {
        on(osmQuestSource.getAllVisibleInBBox(bbox.asBoundingBoxOfEnclosingTiles(16), listOf("TestQuestTypeA")))
            .thenReturn(listOf(OsmQuest(TestQuestTypeA(), ElementType.NODE, 1, ElementPointGeometry(bbox.min))))
        on(osmNoteQuestSource.getAllVisibleInBBox(bbox.asBoundingBoxOfEnclosingTiles(16))).thenReturn(listOf())

        val overlay: Overlay = mock()
        on(overlay.hidesQuestTypes).thenReturn(setOf("TestQuestTypeB", "TestQuestTypeC"))
        on(selectedOverlaySource.selectedOverlay).thenReturn(overlay)

        val quests = source.getAllVisible(bbox)
        assertEquals(1, quests.size)
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

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
import de.westnordost.streetcomplete.testutils.bbox
import de.westnordost.streetcomplete.testutils.osmNoteQuest
import de.westnordost.streetcomplete.testutils.osmQuest
import de.westnordost.streetcomplete.testutils.osmQuestKey
import de.westnordost.streetcomplete.testutils.pGeom
import de.westnordost.streetcomplete.testutils.verifyInvokedExactlyOnce
import io.mockative.Mock
import io.mockative.any
import io.mockative.classOf
import io.mockative.every
import io.mockative.mock
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class VisibleQuestsSourceTest {

    @Mock private lateinit var osmQuestSource: OsmQuestSource
    private lateinit var questTypeRegistry: QuestTypeRegistry
    @Mock private lateinit var osmNoteQuestSource: OsmNoteQuestSource
    @Mock private lateinit var visibleQuestTypeSource: VisibleQuestTypeSource
    @Mock private lateinit var teamModeQuestFilter: TeamModeQuestFilter
    @Mock private lateinit var selectedOverlaySource: SelectedOverlaySource
    private lateinit var source: VisibleQuestsSource

    private lateinit var noteQuestListener: OsmNoteQuestSource.Listener
    private lateinit var questListener: OsmQuestSource.Listener
    private lateinit var visibleQuestTypeListener: VisibleQuestTypeSource.Listener
    private lateinit var teamModeListener: TeamModeQuestFilter.TeamModeChangeListener
    private lateinit var selectedOverlayListener: SelectedOverlaySource.Listener

    @Mock private lateinit var listener: VisibleQuestsSource.Listener

    private val bbox = bbox(0.0, 0.0, 1.0, 1.0)
    private val questTypes = listOf(TestQuestTypeA(), TestQuestTypeB(), TestQuestTypeC())
    private val questTypeNames = questTypes.map { it.name }

    @BeforeTest fun setUp() {
        osmNoteQuestSource = mock(classOf<OsmNoteQuestSource>())
        osmQuestSource = mock(classOf<OsmQuestSource>())
        visibleQuestTypeSource = mock(classOf<VisibleQuestTypeSource>())
        teamModeQuestFilter = mock(classOf<TeamModeQuestFilter>())
        selectedOverlaySource = mock(classOf<SelectedOverlaySource>())
        questTypeRegistry = QuestTypeRegistry(questTypes.mapIndexed { index, questType -> index to questType })

        every { visibleQuestTypeSource.isVisible(any()) }.returns(true)
        every { teamModeQuestFilter.isVisible(any()) }.returns(true)

        every { osmNoteQuestSource.addListener(any()) }.invokes { arguments ->
            noteQuestListener = arguments[0] as OsmNoteQuestSource.Listener
            Unit
        }
        every { osmQuestSource.addListener(any()) }.invokes { arguments ->
            questListener = arguments[0] as OsmQuestSource.Listener
            Unit
        }
        every { visibleQuestTypeSource.addListener(any()) }.invokes { arguments ->
            visibleQuestTypeListener = arguments[0] as VisibleQuestTypeSource.Listener
            Unit
        }
        every { teamModeQuestFilter.addListener(any()) }.invokes { arguments ->
            teamModeListener = arguments[0] as TeamModeQuestFilter.TeamModeChangeListener
            Unit
        }
        every { selectedOverlaySource.addListener(any()) }.invokes { arguments ->
            selectedOverlayListener = arguments[0] as SelectedOverlaySource.Listener
            Unit
        }

        source = VisibleQuestsSource(questTypeRegistry, osmQuestSource, osmNoteQuestSource, visibleQuestTypeSource, teamModeQuestFilter, selectedOverlaySource)

        listener = mock(classOf<VisibleQuestsSource.Listener>())
        source.addListener(listener)
    }

    @Test fun getAllVisible() {
        val bboxCacheWillRequest = bbox.asBoundingBoxOfEnclosingTiles(16)
        val osmQuests = questTypes.map { OsmQuest(it, ElementType.NODE, 1L, pGeom()) }
        val noteQuests = listOf(OsmNoteQuest(0L, LatLon(0.0, 0.0)), OsmNoteQuest(1L, LatLon(1.0, 1.0)))
        every { osmQuestSource.getAllVisibleInBBox(bboxCacheWillRequest, questTypeNames) }.returns(osmQuests)
        every { osmNoteQuestSource.getAllVisibleInBBox(bboxCacheWillRequest) }.returns(noteQuests)
        every { selectedOverlaySource.selectedOverlay }.returns(null)

        val quests = source.getAllVisible(bbox)
        assertEquals(5, quests.size)
        assertEquals(3, quests.filterIsInstance<OsmQuest>().size)
        assertEquals(2, quests.filterIsInstance<OsmNoteQuest>().size)
    }

    @Test fun `getAllVisible does not return those that are invisible in team mode`() {
        val bboxCacheWillRequest = bbox.asBoundingBoxOfEnclosingTiles(16)
        val osmQuests = questTypes.map { OsmQuest(it, ElementType.NODE, 1L, pGeom()) }
        val noteQuests = listOf(OsmNoteQuest(0L, LatLon(0.0, 0.0)), OsmNoteQuest(1L, LatLon(1.0, 1.0)))
        every { osmQuestSource.getAllVisibleInBBox(bboxCacheWillRequest, questTypeNames) }.returns(osmQuests)
        every { osmNoteQuestSource.getAllVisibleInBBox(bboxCacheWillRequest) }.returns(noteQuests)
        every { teamModeQuestFilter.isVisible(any()) }.returns(false)
        every { teamModeQuestFilter.isEnabled }.returns(true)
        every { selectedOverlaySource.selectedOverlay }.returns(null)

        val quests = source.getAllVisible(bbox)
        assertTrue(quests.isEmpty())
    }

    @Test fun `getAllVisible does not return those that are invisible because of an overlay`() {
        every { osmQuestSource.getAllVisibleInBBox(bbox.asBoundingBoxOfEnclosingTiles(16), listOf("TestQuestTypeA")) }
            .returns(listOf(OsmQuest(TestQuestTypeA(), ElementType.NODE, 1, ElementPointGeometry(bbox.min))))
        every { osmNoteQuestSource.getAllVisibleInBBox(bbox.asBoundingBoxOfEnclosingTiles(16)) }.returns(listOf())

        val overlay: Overlay = mock(classOf<Overlay>())
        every { overlay.hidesQuestTypes }.returns(setOf("TestQuestTypeB", "TestQuestTypeC"))
        every { selectedOverlaySource.selectedOverlay }.returns(overlay)

        val quests = source.getAllVisible(bbox)
        assertEquals(1, quests.size)
    }

    @Test fun `osm quests added or removed triggers listener`() {
        val quests = listOf(osmQuest(elementId = 1), osmQuest(elementId = 2))
        val deleted = listOf(osmQuestKey(elementId = 3), osmQuestKey(elementId = 4))
        questListener.onUpdated(quests, deleted)
        verifyInvokedExactlyOnce { listener.onUpdatedVisibleQuests(quests, deleted) }
    }

    @Test fun `osm quests added of invisible type does not trigger listener`() {
        val quests = listOf(osmQuest(elementId = 1), osmQuest(elementId = 2))
        every { visibleQuestTypeSource.isVisible(any()) }.returns(false)
        questListener.onUpdated(quests, emptyList())
        // verifyNoInteractions(listener)
    }

    @Test fun `osm note quests added or removed triggers listener`() {
        val quests = listOf(osmNoteQuest(1L), osmNoteQuest(2L))
        val deleted = listOf(OsmNoteQuestKey(3), OsmNoteQuestKey(4))
        noteQuestListener.onUpdated(quests, listOf(3L, 4L))
        verifyInvokedExactlyOnce { listener.onUpdatedVisibleQuests(quests, deleted) }
    }

    @Test fun `osm note quests added of invisible type does not trigger listener`() {
        val quests = listOf(osmNoteQuest(1L), osmNoteQuest(2L))
        every { visibleQuestTypeSource.isVisible(any()) }.returns(false)
        noteQuestListener.onUpdated(quests, emptyList())
        // verifyNoInteractions(listener)
    }

    @Test fun `trigger invalidate listener if quest type visibilities changed`() {
        visibleQuestTypeListener.onQuestTypeVisibilitiesChanged()
        verifyInvokedExactlyOnce { listener.onVisibleQuestsInvalidated() }
    }

    @Test fun `trigger invalidate listener if visible note quests were invalidated`() {
        noteQuestListener.onInvalidated()
        verifyInvokedExactlyOnce { listener.onVisibleQuestsInvalidated() }
    }
}

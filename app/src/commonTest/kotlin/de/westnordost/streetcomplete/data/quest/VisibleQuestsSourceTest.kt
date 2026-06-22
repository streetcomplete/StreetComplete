package de.westnordost.streetcomplete.data.quest

import de.westnordost.streetcomplete.data.download.tiles.asBoundingBoxOfEnclosingTiles
import de.westnordost.streetcomplete.data.osm.geometry.ElementPointGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuest
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestSource
import de.westnordost.streetcomplete.data.osmnotes.notequests.OsmNoteQuest
import de.westnordost.streetcomplete.data.osmnotes.notequests.OsmNoteQuestSource
import de.westnordost.streetcomplete.data.overlays.Overlay
import de.westnordost.streetcomplete.data.overlays.SelectedOverlaySource
import de.westnordost.streetcomplete.data.visiblequests.QuestsHiddenSource
import de.westnordost.streetcomplete.data.visiblequests.TeamModeQuestFilterSource
import de.westnordost.streetcomplete.data.visiblequests.VisibleEditTypeSource
import dev.mokkery.matcher.any
import de.westnordost.streetcomplete.testutils.bbox
import dev.mokkery.mock
import dev.mokkery.answering.returns
import dev.mokkery.every
import de.westnordost.streetcomplete.testutils.osmNoteQuest
import de.westnordost.streetcomplete.testutils.osmQuest
import de.westnordost.streetcomplete.testutils.osmQuestKey
import de.westnordost.streetcomplete.testutils.p
import de.westnordost.streetcomplete.testutils.pGeom
import dev.mokkery.answering.calls
import dev.mokkery.verify
import dev.mokkery.verifyNoMoreCalls
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class VisibleQuestsSourceTest {

    private lateinit var osmQuestSource: OsmQuestSource
    private lateinit var questsHiddenSource: QuestsHiddenSource
    private lateinit var questTypeRegistry: QuestTypeRegistry
    private lateinit var osmNoteQuestSource: OsmNoteQuestSource
    private lateinit var visibleEditTypeSource: VisibleEditTypeSource
    private lateinit var teamModeQuestFilterSource: TeamModeQuestFilterSource
    private lateinit var selectedOverlaySource: SelectedOverlaySource
    private lateinit var source: VisibleQuestsSource

    private lateinit var noteQuestListener: OsmNoteQuestSource.Listener
    private lateinit var questListener: OsmQuestSource.Listener
    private lateinit var questsHiddenListener: QuestsHiddenSource.Listener
    private lateinit var visibleEditTypeListener: VisibleEditTypeSource.Listener
    private lateinit var teamModeListener: TeamModeQuestFilterSource.Listener
    private lateinit var selectedOverlayListener: SelectedOverlaySource.Listener

    private lateinit var listener: VisibleQuestsSource.Listener

    private val bbox = bbox(0.0, 0.0, 1.0, 1.0)
    private val questTypes = listOf(TestQuestTypeA(), TestQuestTypeB(), TestQuestTypeC())
    private val questTypeNames = questTypes.map { it.name }

    @BeforeTest fun setUp() {
        osmNoteQuestSource = mock() {
            every { addListener(any()) } calls { (listener: OsmNoteQuestSource.Listener) ->
                noteQuestListener = listener
            }
        }

        osmQuestSource = mock() {
            every { addListener(any()) } calls { (listener: OsmQuestSource.Listener) ->
                questListener = listener
            }
        }

        questsHiddenSource = mock() {
            every { addListener(any()) } calls { (listener: QuestsHiddenSource.Listener) ->
                questsHiddenListener = listener
            }
        }

        visibleEditTypeSource = mock() {
            every { addListener(any()) } calls { (listener: VisibleEditTypeSource.Listener) ->
                visibleEditTypeListener = listener
            }
            every { isVisible(any()) } returns true
        }

        teamModeQuestFilterSource = mock() {
            every { addListener(any()) } calls { (listener: TeamModeQuestFilterSource.Listener) ->
                teamModeListener = listener
            }
            every { isVisible(any()) } returns true
        }

        selectedOverlaySource = mock() {
            every { addListener(any()) } calls { (listener: SelectedOverlaySource.Listener) ->
                selectedOverlayListener = listener
            }
        }

        questTypeRegistry = QuestTypeRegistry(questTypes.mapIndexed { index, questType -> index to questType })

        source = VisibleQuestsSource(
            questTypeRegistry, osmQuestSource, osmNoteQuestSource, questsHiddenSource,
            visibleEditTypeSource, teamModeQuestFilterSource, selectedOverlaySource
        )

        listener = mock()
        source.addListener(listener)
    }

    @Test fun getAll() {
        val bboxCacheWillRequest = bbox.asBoundingBoxOfEnclosingTiles(16)
        val osmQuests = questTypes.map { OsmQuest(it, ElementType.NODE, 1L, pGeom()) }
        val noteQuests = listOf(osmNoteQuest(0L, p(0.0, 0.0)), osmNoteQuest(1L, p(1.0, 1.0)))
        every { osmQuestSource.getAllInBBox(bboxCacheWillRequest, questTypeNames) } returns osmQuests
        every { osmNoteQuestSource.getAllInBBox(bboxCacheWillRequest) } returns noteQuests
        every { questsHiddenSource.get(any()) } returns null

        val quests = source.getAll(bbox)
        assertEquals(5, quests.size)
        assertEquals(3, quests.filterIsInstance<OsmQuest>().size)
        assertEquals(2, quests.filterIsInstance<OsmNoteQuest>().size)
    }

    @Test fun `getAll does not return those that are hidden by user`() {
        val bboxCacheWillRequest = bbox.asBoundingBoxOfEnclosingTiles(16)
        val osmQuests = questTypes.map { OsmQuest(it, ElementType.NODE, 1L, pGeom()) }
        val noteQuests = listOf(osmNoteQuest(0L, p(0.0, 0.0)), osmNoteQuest(1L, p(1.0, 1.0)))
        every { osmQuestSource.getAllInBBox(bboxCacheWillRequest, questTypeNames) } returns osmQuests
        every { osmNoteQuestSource.getAllInBBox(bboxCacheWillRequest) } returns noteQuests

        every { questsHiddenSource.get(any()) } returns 1

        val quests = source.getAll(bbox)
        assertTrue(quests.isEmpty())
    }

    @Test fun `getAll does not return those that are invisible in team mode`() {
        val bboxCacheWillRequest = bbox.asBoundingBoxOfEnclosingTiles(16)
        val osmQuest = OsmQuest(questTypes.first(), ElementType.NODE, 1L, pGeom())
        val noteQuest = osmNoteQuest(0L, p(0.0, 0.0))
        every { osmQuestSource.getAllInBBox(bboxCacheWillRequest, questTypeNames) } returns listOf(osmQuest)
        every { osmNoteQuestSource.getAllInBBox(bboxCacheWillRequest) } returns listOf(noteQuest)
        every { questsHiddenSource.get(any()) } returns null
        every { teamModeQuestFilterSource.isVisible(any()) } returns false
        every { teamModeQuestFilterSource.isEnabled } returns true

        val quests = source.getAll(bbox)
        assertTrue(quests.isEmpty())
    }

    @Test fun `getAll does not return those that are invisible because of an overlay`() {
        val bboxCacheWillRequest = bbox.asBoundingBoxOfEnclosingTiles(16)
        every { osmQuestSource.getAllInBBox(bboxCacheWillRequest, listOf("TestQuestTypeA")) } returns
             listOf(OsmQuest(TestQuestTypeA(), ElementType.NODE, 1, ElementPointGeometry(bbox.min)))
        every { osmNoteQuestSource.getAllInBBox(bboxCacheWillRequest) } returns listOf()
        every { questsHiddenSource.get(any()) } returns null

        val overlay: Overlay = mock()
        every { overlay.hidesQuestTypes } returns setOf("TestQuestTypeB", "TestQuestTypeC")
        every { selectedOverlaySource.selectedOverlay } returns overlay

        val quests = source.getAll(bbox)
        assertEquals(1, quests.size)
    }

    @Test fun `osm quests added or removed triggers listener`() {
        val quests = listOf(osmQuest(elementId = 1), osmQuest(elementId = 2))
        val deleted = listOf(osmQuestKey(elementId = 3), osmQuestKey(elementId = 4))
        every { questsHiddenSource.get(any()) } returns null

        questListener.onUpdated(quests, deleted)
        verify { listener.onUpdated(quests, deleted) }
    }

    @Test fun `osm quests added of invisible type does not trigger listener`() {
        val quests = listOf(osmQuest(elementId = 1), osmQuest(elementId = 2))
        every { visibleEditTypeSource.isVisible(any()) } returns false
        every { questsHiddenSource.get(any()) } returns null

        questListener.onUpdated(quests, emptyList())
        verifyNoMoreCalls(listener)
    }

    @Test fun `osm note quests added or removed triggers listener`() {
        val quests = listOf(osmNoteQuest(1L), osmNoteQuest(2L))
        val deleted = listOf(OsmNoteQuestKey(3), OsmNoteQuestKey(4))
        every { questsHiddenSource.get(any()) } returns null

        noteQuestListener.onUpdated(quests, listOf(3L, 4L))
        verify { listener.onUpdated(quests, deleted) }
    }

    @Test fun `osm note quests added of invisible type does not trigger listener`() {
        val quests = listOf(osmNoteQuest(1L), osmNoteQuest(2L))
        every { visibleEditTypeSource.isVisible(any()) } returns false
        every { questsHiddenSource.get(any()) } returns null

        noteQuestListener.onUpdated(quests, emptyList())
        verifyNoMoreCalls(listener)
    }

    @Test fun `trigger invalidate listener if quest type visibilities changed`() {
        visibleEditTypeListener.onVisibilitiesChanged()
        verify { listener.onInvalidated() }
    }

    @Test fun `trigger invalidate listener if visible note quests were invalidated`() {
        noteQuestListener.onInvalidated()
        verify { listener.onInvalidated() }
    }

    @Test fun `trigger invalidate when all quests have been unhid`() {
        questsHiddenListener.onUnhidAll()
        verify { listener.onInvalidated() }
    }

    @Test fun `trigger update when quest is hidden`() {
        val key = osmQuestKey()
        questsHiddenListener.onHid(key, 123)
        verify { listener.onUpdated(added = listOf(), removed = listOf(key)) }
    }

    @Test fun `trigger update when quest is unhidden`() {
        val quest = osmQuest()
        every { osmQuestSource.get(quest.key) } returns quest
        every { questsHiddenSource.get(any()) } returns null

        questsHiddenListener.onUnhid(quest.key, 123)

        verify { listener.onUpdated(added = listOf(quest), removed = listOf()) }
    }
}

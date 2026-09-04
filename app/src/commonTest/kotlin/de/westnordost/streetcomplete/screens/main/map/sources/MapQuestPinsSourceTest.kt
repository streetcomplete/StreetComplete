package de.westnordost.streetcomplete.screens.main.map.sources

import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuest
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestSource
import de.westnordost.streetcomplete.data.osmnotes.notequests.OsmNoteQuestSource
import de.westnordost.streetcomplete.data.overlays.SelectedOverlaySource
import de.westnordost.streetcomplete.data.quest.OsmNoteQuestKey
import de.westnordost.streetcomplete.data.quest.OsmQuestKey
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.data.quest.TestQuestTypeA
import de.westnordost.streetcomplete.data.quest.VisibleQuestsSource
import de.westnordost.streetcomplete.data.visiblequests.QuestTypeOrderSource
import de.westnordost.streetcomplete.data.visiblequests.QuestsHiddenSource
import de.westnordost.streetcomplete.data.visiblequests.TeamModeQuestFilterSource
import de.westnordost.streetcomplete.data.visiblequests.VisibleEditTypeSource
import de.westnordost.streetcomplete.screens.main.map.layers.Pin
import de.westnordost.streetcomplete.screens.main.map.layers.PinSnapshot
import de.westnordost.streetcomplete.testutils.bbox
import de.westnordost.streetcomplete.testutils.pGeom
import dev.mokkery.answering.returns
import dev.mokkery.answering.calls
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MapQuestPinsSourceTest {

    @Test fun osmQuestKeyRoundTripsThroughMapProperties() {
        val key = OsmQuestKey(ElementType.WAY, 123L, "AddRoadName")

        assertEquals(key, key.questKeyProperties().toMap().toQuestKeyOrNull())
    }

    @Test fun osmNoteQuestKeyRoundTripsThroughMapProperties() {
        val key = OsmNoteQuestKey(456L)

        assertEquals(key, key.questKeyProperties().toMap().toQuestKeyOrNull())
    }

    @Test fun malformedMapPropertiesAreIgnored() {
        assertNull(mapOf("quest_group" to "osm", "element_id" to "not-a-number").toQuestKeyOrNull())
        assertNull(mapOf("quest_group" to "unknown").toQuestKeyOrNull())
        assertNull(emptyMap<String, String>().toQuestKeyOrNull())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test fun viewportLoadsSharedDrawablePinsWithQuestOrder() = runTest {
        val questType = TestQuestTypeA()
        val quest = OsmQuest(questType, ElementType.NODE, 12L, pGeom(1.0, 2.0))
        val orders: QuestTypeOrderSource = mock()
        val osmQuests: OsmQuestSource = mock {
            every { getAllInBBox(any(), any()) } returns listOf(quest)
        }
        val visibleEditTypes: VisibleEditTypeSource = mock {
            every { isVisible(any()) } returns true
        }
        val teamMode: TeamModeQuestFilterSource = mock {
            every { isVisible(any()) } returns true
        }
        val hiddenQuests: QuestsHiddenSource = mock {
            every { get(any()) } returns null
        }
        val selectedOverlaySource: SelectedOverlaySource = mock {
            every { selectedOverlay } returns null
        }
        val osmNoteQuests: OsmNoteQuestSource = mock {
            every { getAllInBBox(any()) } returns emptyList()
        }
        val questTypeRegistry = QuestTypeRegistry(listOf(7 to questType))
        val quests = VisibleQuestsSource(
            questTypeRegistry = questTypeRegistry,
            osmQuestSource = osmQuests,
            osmNoteQuestSource = osmNoteQuests,
            questsHiddenSource = hiddenQuests,
            visibleEditTypeSource = visibleEditTypes,
            teamModeQuestFilterSource = teamMode,
            selectedOverlaySource = selectedOverlaySource,
        )
        val source = MapQuestPinsSource(
            questTypeOrderSource = orders,
            questTypeRegistry = questTypeRegistry,
            visibleQuestsSource = quests,
            workerDispatcher = StandardTestDispatcher(testScheduler),
        )

        source.onViewportChanged(zoom = 14.0, displayedArea = bbox(0.9999, 1.9999, 1.0001, 2.0001))
        advanceUntilIdle()
        assertEquals(emptyList(), source.pins.value.pins)

        source.setActive(true)
        source.onViewportChanged(zoom = 13.9, displayedArea = bbox(0.9, 1.9, 1.1, 2.1))
        advanceUntilIdle()
        assertEquals(emptyList(), source.pins.value.pins)

        source.onViewportChanged(zoom = 14.0, displayedArea = bbox(0.9999, 1.9999, 1.0001, 2.0001))
        advanceUntilIdle()

        val pin = source.pins.value.pins.single()
        assertEquals(quest.position, pin.position)
        assertEquals(questType.icon, pin.icon)
        assertEquals(0, pin.order)
        assertEquals(quest.key, source.getQuestKey(pin.properties.toMap()))
        source.close()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test fun supersededViewportCannotPublishAfterNewerViewport() = runTest {
        val questType = TestQuestTypeA()
        val first = OsmQuest(questType, ElementType.NODE, 1L, pGeom(1.0, 2.0))
        val second = OsmQuest(questType, ElementType.NODE, 2L, pGeom(20.0, 30.0))
        val orders: QuestTypeOrderSource = mock()
        lateinit var source: MapQuestPinsSource
        var loads = 0
        val osmQuests: OsmQuestSource = mock {
            every { getAllInBBox(any(), any()) } calls {
                if (loads++ == 0) {
                    source.onViewportChanged(14.0, bbox(19.9999, 29.9999, 20.0001, 30.0001))
                    listOf(first)
                } else {
                    listOf(second)
                }
            }
        }
        val visibleEditTypes: VisibleEditTypeSource = mock {
            every { isVisible(any()) } returns true
        }
        val teamMode: TeamModeQuestFilterSource = mock {
            every { isVisible(any()) } returns true
        }
        val hiddenQuests: QuestsHiddenSource = mock {
            every { get(any()) } returns null
        }
        val selectedOverlaySource: SelectedOverlaySource = mock {
            every { selectedOverlay } returns null
        }
        val osmNoteQuests: OsmNoteQuestSource = mock {
            every { getAllInBBox(any()) } returns emptyList()
        }
        val registry = QuestTypeRegistry(listOf(7 to questType))
        val quests = VisibleQuestsSource(
            registry,
            osmQuests,
            osmNoteQuests,
            hiddenQuests,
            visibleEditTypes,
            teamMode,
            selectedOverlaySource,
        )
        source = MapQuestPinsSource(
            orders,
            registry,
            quests,
            UnconfinedTestDispatcher(testScheduler),
        )
        source.setActive(true)

        source.onViewportChanged(14.0, bbox(0.9999, 1.9999, 1.0001, 2.0001))
        advanceUntilIdle()

        assertEquals(
            second.key,
            source.getQuestKey(source.pins.value.pins.single().properties.toMap()),
        )
        source.close()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test fun supersededViewportCannotPublishInFlightDelta() = runTest {
        lateinit var source: MapQuestPinsSource
        var supersedeWhileCreatingDelta = false
        val questType = SupersedingQuestType {
            if (supersedeWhileCreatingDelta) {
                supersedeWhileCreatingDelta = false
                source.onViewportChanged(
                    14.0,
                    bbox(19.9999, 29.9999, 20.0001, 30.0001),
                )
            }
        }
        val first = OsmQuest(questType, ElementType.NODE, 1L, pGeom(1.0, 2.0))
        val delta = OsmQuest(questType, ElementType.NODE, 2L, pGeom(1.0, 2.0))
        val latest = OsmQuest(questType, ElementType.NODE, 3L, pGeom(20.0, 30.0))
        lateinit var osmQuestListener: OsmQuestSource.Listener
        var loads = 0
        val osmQuests: OsmQuestSource = mock {
            every { addListener(any()) } calls { (listener: OsmQuestSource.Listener) ->
                osmQuestListener = listener
            }
            every { getAllInBBox(any(), any()) } calls {
                if (loads++ == 0) listOf(first) else listOf(latest)
            }
        }
        val visibleEditTypes: VisibleEditTypeSource = mock {
            every { isVisible(any()) } returns true
        }
        val teamMode: TeamModeQuestFilterSource = mock {
            every { isVisible(any()) } returns true
        }
        val hiddenQuests: QuestsHiddenSource = mock {
            every { get(any()) } returns null
        }
        val selectedOverlaySource: SelectedOverlaySource = mock {
            every { selectedOverlay } returns null
        }
        val osmNoteQuests: OsmNoteQuestSource = mock {
            every { getAllInBBox(any()) } returns emptyList()
        }
        val orders: QuestTypeOrderSource = mock()
        val registry = QuestTypeRegistry(listOf(7 to questType))
        val quests = VisibleQuestsSource(
            registry,
            osmQuests,
            osmNoteQuests,
            hiddenQuests,
            visibleEditTypes,
            teamMode,
            selectedOverlaySource,
        )
        source = MapQuestPinsSource(
            orders,
            registry,
            quests,
            UnconfinedTestDispatcher(testScheduler),
        )
        source.setActive(true)
        val publications = mutableListOf<PinSnapshot>()
        val collectionJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            source.pins.toList(publications)
        }

        source.onViewportChanged(14.0, bbox(0.9999, 1.9999, 1.0001, 2.0001))
        advanceUntilIdle()
        supersedeWhileCreatingDelta = true
        osmQuestListener.onUpdated(listOf(delta), emptyList())
        advanceUntilIdle()

        assertEquals(
            latest.key,
            source.getQuestKey(source.pins.value.pins.single().properties.toMap()),
        )
        assertTrue(
            publications.none { snapshot ->
                snapshot.pins.any { source.getQuestKey(it.properties.toMap()) == delta.key }
            },
        )
        collectionJob.cancel()
        source.close()
    }
}

private class SupersedingQuestType(private val onIcon: () -> Unit) : TestQuestTypeA() {
    override val icon
        get() = super.icon.also { onIcon() }
}

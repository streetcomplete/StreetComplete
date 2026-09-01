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
import de.westnordost.streetcomplete.testutils.bbox
import de.westnordost.streetcomplete.testutils.pGeom
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

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

        source.onViewportChanged(zoom = 13.9, displayedArea = bbox(0.9, 1.9, 1.1, 2.1))
        advanceUntilIdle()
        assertEquals(emptyList(), source.pins.value)

        source.onViewportChanged(zoom = 14.0, displayedArea = bbox(0.9999, 1.9999, 1.0001, 2.0001))
        advanceUntilIdle()

        val pin = source.pins.value.single()
        assertEquals(quest.position, pin.position)
        assertEquals(questType.icon, pin.icon)
        assertEquals(0, pin.order)
        assertEquals(quest.key, source.getQuestKey(pin.properties.toMap()))
        source.close()
    }
}

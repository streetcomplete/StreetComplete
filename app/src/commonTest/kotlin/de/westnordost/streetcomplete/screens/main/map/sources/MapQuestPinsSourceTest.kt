package de.westnordost.streetcomplete.screens.main.map.sources

import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.data.quest.TestQuestTypeB
import de.westnordost.streetcomplete.data.quest.VisibleQuestsSource
import de.westnordost.streetcomplete.data.visiblequests.QuestTypeOrderSource
import de.westnordost.streetcomplete.screens.main.map.layers.Pin
import de.westnordost.streetcomplete.testutils.FakeVisibleQuestsSource
import de.westnordost.streetcomplete.testutils.QUEST_TYPE
import de.westnordost.streetcomplete.testutils.bbox
import de.westnordost.streetcomplete.testutils.collectEmissions
import de.westnordost.streetcomplete.testutils.osmQuest
import de.westnordost.streetcomplete.testutils.p
import de.westnordost.streetcomplete.testutils.pGeom
import dev.mokkery.mock
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.JsonObject
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MapQuestPinsSourceTest {

    private lateinit var visibleQuestsSource: FakeVisibleQuestsSource
    private lateinit var questTypeOrderSource: QuestTypeOrderSource
    private val questsListener: VisibleQuestsSource.Listener get() = visibleQuestsSource.listener!!
    private lateinit var source: MapQuestPinsSource

    private val view = bbox(0.0, 0.0, 0.001, 0.001)
    private val questInView = osmQuest(elementId = 1, geometry = pGeom(0.0005, 0.0005))
    private val questOutsideView = osmQuest(elementId = 2, geometry = pGeom(5.0, 5.0))

    @BeforeTest fun setUp() {
        visibleQuestsSource = FakeVisibleQuestsSource()
        visibleQuestsSource.quests = listOf(questInView)
        questTypeOrderSource = mock()
        val registry = QuestTypeRegistry(listOf(0 to QUEST_TYPE, 1 to TestQuestTypeB()))
        source = MapQuestPinsSource(questTypeOrderSource, registry, visibleQuestsSource)
    }

    @Test fun `no pins while the displayed area is unknown`() = runBlocking {
        assertTrue(source.pins.first().isEmpty())
    }

    @Test fun `no pins while zoomed out`() = runBlocking {
        source.onMapMoved(13.0, view)
        assertTrue(source.pins.first().isEmpty())
    }

    @Test fun `pins of the quests in the displayed area`() = runBlocking {
        source.onMapMoved(16.0, view)
        val pins = source.pins.first()
        assertEquals(listOf(questInView.position), pins.map { it.position })
        assertEquals(questInView.key, source.getQuestKey(pins.single().toGeoJsonFeatureProperties()))
    }

    @Test fun `pins are updated when quests change`() = runBlocking {
        source.onMapMoved(16.0, view)
        val pins = collectEmissions(source.pins)
        assertEquals(1, pins.next().size)

        questsListener.onUpdated(added = listOf(questOutsideView), removed = emptyList())
        assertEquals(1, pins.next().size)

        val addedInView = osmQuest(elementId = 3, geometry = pGeom(0.0006, 0.0006))
        questsListener.onUpdated(added = listOf(addedInView), removed = emptyList())
        assertEquals(setOf(questInView.position, addedInView.position), pins.next().map { it.position }.toSet())

        questsListener.onUpdated(added = emptyList(), removed = listOf(questInView.key))
        assertEquals(listOf(addedInView.position), pins.next().map { it.position })

        pins.stop()
    }

    @Test fun `all pins are reloaded when invalidated`() = runBlocking {
        source.onMapMoved(16.0, view)
        val pins = collectEmissions(source.pins)
        assertEquals(1, pins.next().size)

        visibleQuestsSource.quests = emptyList()
        questsListener.onInvalidated()
        assertTrue(pins.next().isEmpty())

        pins.stop()
    }

    @Test fun `quests with a marker in view are kept when their center leaves the view`() = runBlocking {
        // ~2.2 km long way with markers every 400 m, the first ~15 m from its start
        val longQuest = osmQuest(
            elementId = 4,
            geometry = ElementPolylinesGeometry(listOf(listOf(p(0.0005, 0.0), p(0.0005, 0.02))), p(0.0005, 0.01))
        )
        visibleQuestsSource.quests = listOf(longQuest)

        // view containing the center
        source.onMapMoved(16.0, bbox(0.0, 0.0095, 0.001, 0.0105))
        val pins = collectEmissions(source.pins)
        assertTrue(pins.next().size > 1)

        // view containing only the first marker
        source.onMapMoved(16.0, view)
        assertTrue(pins.next().isNotEmpty())

        pins.stop()
    }
}

private fun Pin.toGeoJsonFeatureProperties() = JsonObject(properties.toMap())

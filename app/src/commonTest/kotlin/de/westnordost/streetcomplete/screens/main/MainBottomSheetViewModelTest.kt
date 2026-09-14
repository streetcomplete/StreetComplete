package de.westnordost.streetcomplete.screens.main

import de.westnordost.streetcomplete.data.location.SurveyChecker
import de.westnordost.streetcomplete.data.osm.edits.MapDataWithEditsSource
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.data.osm.mapdata.MutableMapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestSource
import de.westnordost.streetcomplete.data.overlays.Overlay
import de.westnordost.streetcomplete.data.overlays.OverlayRegistry
import de.westnordost.streetcomplete.testutils.FakeVisibleQuestsSource
import de.westnordost.streetcomplete.testutils.bbox
import de.westnordost.streetcomplete.testutils.node
import de.westnordost.streetcomplete.testutils.osmQuest
import dev.mokkery.answering.calls
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class MainBottomSheetViewModelTest {

    private lateinit var mapDataSource: MapDataWithEditsSource
    private lateinit var mapDataListener: MapDataWithEditsSource.Listener
    private lateinit var visibleQuestsSource: FakeVisibleQuestsSource
    private lateinit var viewModel: MainBottomSheetViewModel

    private val element = node(1)
    private val quest = osmQuest(elementType = ElementType.NODE, elementId = 1)
    private val selection = MainBottomSheetSelection.Quest(quest.key)

    @BeforeTest fun setUp() {
        visibleQuestsSource = FakeVisibleQuestsSource()
        visibleQuestsSource.quests = listOf(quest)
        mapDataSource = mock {
            every { addListener(any()) } calls { (listener: MapDataWithEditsSource.Listener) ->
                mapDataListener = listener
            }
            every { get(ElementType.NODE, 1) } returns element
        }
        val osmQuestSource = mock<OsmQuestSource> {
            every { get(quest.key) } returns quest
        }
        viewModel = MainBottomSheetViewModelImpl(
            mapDataSource = mapDataSource,
            notesSource = mock(),
            osmQuestSource = osmQuestSource,
            osmNoteQuestSource = mock(),
            elementEditsController = mock(),
            noteEditsController = mock(),
            hiddenQuestsController = mock(),
            surveyChecker = SurveyChecker(),
            visibleQuestsSource = visibleQuestsSource,
            overlayRegistry = OverlayRegistry(listOf(0 to mock<Overlay>())),
            featureDictionary = lazy { error("not used") },
        )
    }

    @Test fun `shows the selected quest`() = runBlocking {
        val sheets = collectSheets()
        assertEquals(ShownBottomSheet.OsmQuest(quest, element), sheets.next())
        sheets.stop()
    }

    @Test fun `shows nothing when the selected quest does not exist`() = runBlocking {
        visibleQuestsSource.quests = emptyList()
        val sheets = collectSheets()
        assertNull(sheets.next())
        sheets.stop()
    }

    @Test fun `keeps showing the quest as selected while it is updated`() = runBlocking {
        val sheets = collectSheets()
        sheets.next()

        val updatedElement = node(1, tags = mapOf("changed" to "yes"))
        every { mapDataSource.get(ElementType.NODE, 1) } returns updatedElement
        mapDataListener.onUpdated(MutableMapDataWithGeometry().also { it.put(updatedElement, null) }, emptyList())
        visibleQuestsSource.listener!!.onUpdated(added = listOf(quest), removed = emptyList())
        visibleQuestsSource.listener!!.onInvalidated()

        assertNull(withTimeoutOrNull(300) { sheets.next() })
        sheets.stop()
    }

    @Test fun `closes when the quest is removed`() = runBlocking {
        val sheets = collectSheets()
        sheets.next()

        visibleQuestsSource.quests = emptyList()
        visibleQuestsSource.listener!!.onUpdated(added = emptyList(), removed = listOf(quest.key))

        assertNull(sheets.next())
        sheets.stop()
    }

    @Test fun `closes when the element is gone after a download`() = runBlocking {
        val sheets = collectSheets()
        sheets.next()

        every { mapDataSource.get(ElementType.NODE, 1) } returns null
        mapDataListener.onReplacedForBBox(bbox(), MutableMapDataWithGeometry())

        assertNull(sheets.next())
        sheets.stop()
    }

    private fun CoroutineScope.collectSheets(): Emissions {
        val channel = Channel<ShownBottomSheet?>(Channel.UNLIMITED)
        val job = launch(Dispatchers.Default) { viewModel.bottomSheet(selection).collect { channel.send(it) } }
        return Emissions(channel, job)
    }

    private class Emissions(private val channel: Channel<ShownBottomSheet?>, private val job: Job) {
        suspend fun next(): ShownBottomSheet? = channel.receive()
        fun stop() = job.cancel()
    }
}

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
import de.westnordost.streetcomplete.testutils.collectEmissions
import de.westnordost.streetcomplete.testutils.node
import de.westnordost.streetcomplete.testutils.osmQuest
import dev.mokkery.answering.calls
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock
import kotlinx.coroutines.runBlocking
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
        val sheets = collectEmissions(viewModel.bottomSheet(selection))
        assertEquals(ShownBottomSheet.OsmQuest(quest, element), sheets.next())
        sheets.stop()
    }

    @Test fun `shows nothing when the selected quest does not exist`() = runBlocking {
        visibleQuestsSource.quests = emptyList()
        val sheets = collectEmissions(viewModel.bottomSheet(selection))
        assertNull(sheets.next())
        sheets.stop()
    }

    @Test fun `keeps showing the quest as selected while it is updated`() = runBlocking {
        val sheets = collectEmissions(viewModel.bottomSheet(selection))
        sheets.next()

        val updatedElement = node(1, tags = mapOf("changed" to "yes"))
        every { mapDataSource.get(ElementType.NODE, 1) } returns updatedElement
        mapDataListener.onUpdated(MutableMapDataWithGeometry().also { it.put(updatedElement, null) }, emptyList())
        visibleQuestsSource.listener!!.onUpdated(added = listOf(quest), removed = emptyList())
        visibleQuestsSource.listener!!.onInvalidated()
        // a removal afterwards must be the next emission, i.e. the updates emitted nothing
        visibleQuestsSource.quests = emptyList()
        visibleQuestsSource.listener!!.onUpdated(added = emptyList(), removed = listOf(quest.key))

        assertNull(sheets.next())
        sheets.stop()
    }

    @Test fun `closes when the quest is removed`() = runBlocking {
        val sheets = collectEmissions(viewModel.bottomSheet(selection))
        sheets.next()

        visibleQuestsSource.quests = emptyList()
        visibleQuestsSource.listener!!.onUpdated(added = emptyList(), removed = listOf(quest.key))

        assertNull(sheets.next())
        sheets.stop()
    }

    @Test fun `closes when the element is gone after a download`() = runBlocking {
        val sheets = collectEmissions(viewModel.bottomSheet(selection))
        sheets.next()

        every { mapDataSource.get(ElementType.NODE, 1) } returns null
        mapDataListener.onReplacedForBBox(bbox(), MutableMapDataWithGeometry())

        assertNull(sheets.next())
        sheets.stop()
    }
}

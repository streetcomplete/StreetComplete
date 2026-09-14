package de.westnordost.streetcomplete.screens.main

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.SaveableStateHolder
import androidx.compose.runtime.snapshots.Snapshot
import de.westnordost.streetcomplete.data.osm.edits.ElementEditAction
import de.westnordost.streetcomplete.data.osm.edits.ElementEditType
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osmnotes.Note
import de.westnordost.streetcomplete.data.osmtracks.Trackpoint
import de.westnordost.streetcomplete.data.quest.OsmNoteQuestKey
import de.westnordost.streetcomplete.data.quest.QuestKey
import de.westnordost.streetcomplete.testutils.node
import de.westnordost.streetcomplete.testutils.note
import de.westnordost.streetcomplete.testutils.osmNoteQuest
import de.westnordost.streetcomplete.testutils.osmQuest
import de.westnordost.streetcomplete.ui.common.quest.Marker
import dev.mokkery.mock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class MainSheetStateTest {

    /** what the view model resolves each selection to; null closes, absent never resolves */
    private val resolved = mutableMapOf<MainBottomSheetSelection, List<ShownBottomSheet?>>()

    private val viewModel = object : FakeMainBottomSheetViewModel() {
        override fun bottomSheet(selection: MainBottomSheetSelection): Flow<ShownBottomSheet?> = flow {
            resolved[selection]?.forEach { emit(it) }
            awaitCancellation()
        }
    }
    private val state = MainSheetState(viewModel, mock<SaveableStateHolder>(), mutableStateOf(null), mutableStateOf(""))

    private val quest = osmQuest(elementId = 1)
    private val questSheet = ShownBottomSheet.OsmQuest(quest, node(1))
    private val questSelection = MainBottomSheetSelection.Quest(quest.key)

    @Test fun `shows what the selection resolves to`() = observing {
        resolved[questSelection] = listOf(questSheet)
        state.show(questSelection)
        await { state.shownBottomSheet == questSheet }
    }

    @Test fun `shows nothing for a new selection until it is resolved`() = observing {
        resolved[questSelection] = listOf(questSheet)
        state.show(questSelection)
        await { state.shownBottomSheet == questSheet }

        val other = MainBottomSheetSelection.Overlay("overlay")
        state.show(other)
        await { state.shownBottomSheet == null }
        assertEquals(other, state.selection)
    }

    @Test fun `closes when the selected object disappears`() = observing {
        resolved[questSelection] = listOf(questSheet, null)
        state.show(questSelection)
        await { state.selection == null }
        assertNull(state.shownBottomSheet)
    }

    @Test fun `selects the note that blocks an overlay element`() = observing {
        val noteQuest = osmNoteQuest(id = 5)
        val noteSheet = ShownBottomSheet.OsmNoteQuest(noteQuest, note(id = 5))
        val elementSelection = MainBottomSheetSelection.Overlay("overlay", ElementKey(ElementType.NODE, 1))
        val noteSelection = MainBottomSheetSelection.Quest(OsmNoteQuestKey(5))
        resolved[elementSelection] = listOf(noteSheet)
        resolved[noteSelection] = listOf(noteSheet)

        state.show(elementSelection)
        await { state.shownBottomSheet == noteSheet }
        assertEquals(noteSelection, state.selection)
    }

    private fun observing(block: suspend () -> Unit) = runBlocking {
        val job = launch(Dispatchers.Default) { state.observe() }
        block()
        job.cancel()
    }

    /** Snapshot state written outside a composition is only observed once applied */
    private suspend fun await(condition: () -> Boolean) = withTimeout(5000) {
        while (!condition()) {
            Snapshot.sendApplyNotifications()
            delay(10)
        }
    }
}

private abstract class FakeMainBottomSheetViewModel : MainBottomSheetViewModel() {
    override suspend fun getHighlightedMarkers(sheet: ShownBottomSheet): List<Marker> = emptyList()
    override fun hideQuest(questKey: QuestKey) {}
    override fun isSurvey(geometry: ElementGeometry): Boolean = true
    override fun submitEdit(elementEditType: ElementEditType, geometry: ElementGeometry, elementEditAction: ElementEditAction) {}
    override fun commentNote(note: Note, text: String?, imagePaths: List<String>) {}
    override fun createNote(position: LatLon, text: String, imagePaths: List<String>, trackpoints: List<Trackpoint>?) {}
}

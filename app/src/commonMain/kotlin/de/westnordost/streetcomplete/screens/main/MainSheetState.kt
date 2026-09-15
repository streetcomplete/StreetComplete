package de.westnordost.streetcomplete.screens.main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.SaveableStateHolder
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import de.westnordost.streetcomplete.ui.common.quest.MapClick
import de.westnordost.streetcomplete.ui.common.quest.Marker
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlin.uuid.Uuid

@Composable
fun rememberMainSheetState(viewModel: MainBottomSheetViewModel): MainSheetState {
    val formStateHolder = rememberSaveableStateHolder()
    val selection = rememberSaveable(stateSaver = MainBottomSheetSelection.Saver) {
        mutableStateOf<MainBottomSheetSelection?>(null)
    }
    val id = rememberSaveable { mutableStateOf("") }
    val state = remember(viewModel) { MainSheetState(viewModel, formStateHolder, selection, id) }
    LaunchedEffect(state) { state.observe() }
    return state
}

/** The bottom sheet selection, what it resolved to, and the open form's map interaction. */
@Stable
class MainSheetState internal constructor(
    private val viewModel: MainBottomSheetViewModel,
    /** Holds the state of the form(s), keyed by [id] */
    val formStateHolder: SaveableStateHolder,
    selection: MutableState<MainBottomSheetSelection?>,
    id: MutableState<String>,
) {
    /** The selected object; saved to survive process death */
    var selection by selection
        private set

    /** Identifies the form instance; a new form must not inherit a previous form's state */
    var id by id
        private set

    /** What the selected object resolved to. Null when nothing is selected or while loading. */
    var shownBottomSheet by mutableStateOf<ShownBottomSheet?>(null)
        private set

    /** Markers the open form asks the map to display */
    var formMarkers by mutableStateOf<List<Marker>?>(null)

    /** Where the user clicked on the map while the form was open */
    var lastMapClick by mutableStateOf<MapClick?>(null)

    val isOpen: Boolean get() = selection != null

    fun show(selection: MainBottomSheetSelection) {
        formStateHolder.removeState(id)
        id = Uuid.random().toString()
        this.selection = selection
        formMarkers = null
        lastMapClick = null
    }

    fun close() {
        selection = null
        formMarkers = null
        lastMapClick = null
        formStateHolder.removeState(id)
    }

    /** Keeps [shownBottomSheet] in sync with the [selection]. Runs until cancelled. */
    @OptIn(ExperimentalCoroutinesApi::class)
    internal suspend fun observe() {
        snapshotFlow { selection }.collectLatest { selection ->
            // never show the previous selection's sheet for a new selection
            shownBottomSheet = null
            if (selection == null) return@collectLatest
            viewModel.bottomSheet(selection).collect { sheet ->
                if (selection is MainBottomSheetSelection.Overlay && sheet is ShownBottomSheet.OsmNoteQuest) {
                    // A note at the element blocks editing it. Selecting the note instead also
                    // closes this sheet when the note is hidden or deleted.
                    this.selection = MainBottomSheetSelection.Quest(sheet.quest.key)
                    return@collect
                }
                shownBottomSheet = sheet
                // null: the selected object does not exist anymore
                if (sheet == null) close()
            }
        }
    }
}

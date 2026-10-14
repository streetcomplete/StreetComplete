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
import de.westnordost.streetcomplete.data.edithistory.Edit
import de.westnordost.streetcomplete.data.edithistory.EditKey
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.screens.main.edithistory.EditHistoryViewModel
import de.westnordost.streetcomplete.screens.main.edithistory.EditItem
import de.westnordost.streetcomplete.ui.common.quest.MapClick
import de.westnordost.streetcomplete.ui.common.quest.MapOverlayContent
import de.westnordost.streetcomplete.ui.common.quest.Marker
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

@Composable
fun rememberMainSheetState(
    viewModel: MainBottomSheetViewModel,
    editHistoryViewModel: EditHistoryViewModel,
): MainSheetState {
    val formStateHolder = rememberSaveableStateHolder()
    val selection = rememberSaveable(stateSaver = MainSheetSelection.Saver) {
        mutableStateOf<MainSheetSelection?>(null)
    }
    val id = rememberSaveable { mutableStateOf("") }
    val state = remember(viewModel, editHistoryViewModel) {
        MainSheetState(viewModel, editHistoryViewModel, formStateHolder, selection, id)
    }
    LaunchedEffect(state) { state.observe() }
    return state
}

/** The sheet selection, what it resolved to, and the open form's map interaction. The edit
 *  history sidebar is never shown together with a bottom sheet, so it is one of the selections. */
@Stable
class MainSheetState internal constructor(
    private val viewModel: MainBottomSheetViewModel,
    private val editHistoryViewModel: EditHistoryViewModel,
    /** Holds the state of the form(s), keyed by [id] */
    val formStateHolder: SaveableStateHolder,
    selection: MutableState<MainSheetSelection?>,
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

    /** The edit selected in the edit history sidebar. Null when it is not open or while loading. */
    var shownEdit by mutableStateOf<ShownEdit?>(null)
        private set

    /** All edits that can be undone. Null while loading. */
    var editItems by mutableStateOf<List<EditItem>?>(null)
        private set

    /** Markers the open form asks the map to display; null uses the quest's default highlights. */
    var formMarkers by mutableStateOf<List<Marker>?>(null)

    /** Content the open form places on the map, e.g. a pin that snaps to a way */
    var formMapOverlay by mutableStateOf<MapOverlayContent?>(null)

    /** Where the user clicked on the map while the form was open */
    var lastMapClick by mutableStateOf<MapClick?>(null)

    val isOpen: Boolean get() = selection != null

    /** Whether a bottom sheet is open, as opposed to the edit history sidebar */
    val isFormOpen: Boolean get() = selection.let { it != null && it !is MainSheetSelection.EditHistory }

    val hasEdits: Boolean get() = !editItems.isNullOrEmpty()

    fun show(selection: MainSheetSelection) {
        formStateHolder.removeState(id)
        id = Uuid.random().toString()
        this.selection = selection
        formMarkers = null
        formMapOverlay = null
        lastMapClick = null
    }

    /** Shows the edit history sidebar with the most recent edit selected */
    fun showEditHistory() {
        val newest = editItems?.lastOrNull() ?: return
        show(MainSheetSelection.EditHistory(newest.edit.key))
    }

    fun close() {
        selection = null
        formMarkers = null
        formMapOverlay = null
        lastMapClick = null
        formStateHolder.removeState(id)
    }

    /** Keeps [shownBottomSheet] in sync with the [selection]. Runs until cancelled. */
    internal suspend fun observe(): Unit = coroutineScope {
        launch {
            editHistoryViewModel.editItems.collect { editItems = it }
        }
        launch {
            snapshotFlow { selection }.collectLatest { selection ->
                // never show the previous selection's sheet for a new selection
                shownBottomSheet = null
                shownEdit = null
                when (selection) {
                    null -> {}
                    is MainSheetSelection.EditHistory -> observeEdit(selection.editKey)
                    else -> showBottomSheet(selection)
                }
            }
        }
    }

    private suspend fun showBottomSheet(selection: MainSheetSelection) {
        val sheet = viewModel.getBottomSheet(selection)
        if (sheet != null) shownBottomSheet = sheet else close()
    }

    private suspend fun observeEdit(key: EditKey) {
        editHistoryViewModel.editItems.collect { items ->
            if (items == null) return@collect
            val edit = items.find { it.edit.key == key }?.edit
            if (edit != null) {
                shownEdit = ShownEdit(edit, editHistoryViewModel.getEditGeometry(edit))
            } else {
                // the edit was undone or is synced: select the newest remaining edit, if any
                val newest = items.lastOrNull()?.edit?.key
                if (newest != null) selection = MainSheetSelection.EditHistory(newest) else close()
            }
        }
    }
}

/** An edit selected in the edit history sidebar, with its geometry to show on the map */
data class ShownEdit(val edit: Edit, val geometry: ElementGeometry)

package de.westnordost.streetcomplete.screens.main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import de.westnordost.streetcomplete.data.edithistory.Edit
import de.westnordost.streetcomplete.data.edithistory.EditKey
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.screens.main.edithistory.EditHistoryViewModel
import de.westnordost.streetcomplete.screens.main.edithistory.EditItem
import de.westnordost.streetcomplete.ui.util.rememberSerializable
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
fun rememberEditHistoryState(viewModel: EditHistoryViewModel): EditHistoryState {
    val isShowing = rememberSaveable { mutableStateOf(false) }
    val selectedEditKey = rememberSerializable { mutableStateOf<EditKey?>(null) }
    val state = remember(viewModel) { EditHistoryState(viewModel, isShowing, selectedEditKey) }
    LaunchedEffect(state) { state.observe() }
    return state
}

/** The edit history sidebar: whether it is shown and which edit is selected in it. */
@Stable
class EditHistoryState internal constructor(
    private val viewModel: EditHistoryViewModel,
    isShowing: MutableState<Boolean>,
    selectedEditKey: MutableState<EditKey?>,
) {
    var isShowing by isShowing
        private set

    var selectedEditKey by selectedEditKey
        private set

    /** All edits that can be undone. Null while loading. */
    var editItems by mutableStateOf<List<EditItem>?>(null)
        private set

    val hasEdits: Boolean get() = !editItems.isNullOrEmpty()

    val selectedEdit: Edit?
        get() = if (isShowing) editItems?.find { it.edit.key == selectedEditKey }?.edit else null

    /** Geometry of the [selectedEdit], to highlight it on the map */
    var highlightedGeometry by mutableStateOf<ElementGeometry?>(null)
        private set

    /** Shows the sidebar with the most recent edit selected */
    fun show() {
        selectedEditKey = editItems?.lastOrNull()?.edit?.key
        isShowing = true
    }

    fun select(key: EditKey) {
        selectedEditKey = key
    }

    fun hide() {
        isShowing = false
        selectedEditKey = null
    }

    fun undo(key: EditKey) {
        viewModel.undo(key)
    }

    /** Keeps [editItems], the selection and [highlightedGeometry] in sync. Runs until cancelled. */
    @OptIn(ExperimentalCoroutinesApi::class)
    internal suspend fun observe(): Unit = coroutineScope {
        launch {
            viewModel.editItems.collect { items ->
                editItems = items
                if (items == null) return@collect
                if (items.none { it.edit.key == selectedEditKey }) selectedEditKey = null
                // nothing left to show once the last edit was undone
                if (items.isEmpty()) isShowing = false
            }
        }
        launch {
            snapshotFlow { selectedEdit }.collectLatest { edit ->
                highlightedGeometry = edit?.let { viewModel.getEditGeometry(it) }
            }
        }
    }
}

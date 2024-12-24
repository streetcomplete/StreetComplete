package de.westnordost.streetcomplete.screens.main.edithistory

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Divider
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.edithistory.Edit
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.ui.ktx.isItemAtIndexFullyVisible
import de.westnordost.streetcomplete.ui.theme.titleSmall
import de.westnordost.streetcomplete.util.ktx.toast
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.text.DateFormat

/** Shows the edit history in a sidebar. The edit history is grouped by time and date, ordered by
 *  the most recent edit at the bottom. The list always scrolls to the currently selected edit. */
@Composable
fun EditHistorySidebar(
    editItems: List<EditItem>,
    selectedEdit: Edit?,
    onSelectEdit: (Edit) -> Unit,
    onUndoEdit: (Edit) -> Unit,
    onDismissRequest: () -> Unit,
    featureDictionaryLazy: Lazy<FeatureDictionary>,
    getEditElement: suspend (Edit) -> Element?,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()

    val context = LocalContext.current
    val dir = LocalLayoutDirection.current

    val insets = WindowInsets.safeDrawing.asPaddingValues()

    var showUndoDialog by remember { mutableStateOf(false) }
    var editElement by remember { mutableStateOf<Element?>(null) }

    // scrolling to selected item
    val selectedIndex = remember(selectedEdit) {
        if (selectedEdit != null) editItems
            .indexOfLast { it.edit == selectedEdit }
            .takeUnless { it < 0 }
        else null
    }
    val state = rememberLazyListState(initialFirstVisibleItemIndex = selectedIndex ?: editItems.lastIndex)
    LaunchedEffect(selectedEdit) {
        // except for first scroll, only scroll if not fully visible
        if (selectedIndex != null && !state.isItemAtIndexFullyVisible(selectedIndex)) {
            state.animateScrollToItem(selectedIndex)
        }
    }

    // close on back
    BackHandler {
        onDismissRequest()
    }

    fun onClickUndoEdit(edit: Edit) {
        if (edit.isUndoable) {
            scope.launch {
                editElement = getEditElement(edit)
                showUndoDialog = true
            }
        } else {
            context.toast(R.string.toast_undo_unavailable, Toast.LENGTH_LONG)
        }
    }

    // take care of insets:
    // vertical offset as lazy column content padding, left padding as padding of the surface
    Surface(
        modifier = modifier
            .padding(end = insets.calculateEndPadding(dir))
            .fillMaxHeight()
            .consumeWindowInsets(insets)
            .shadow(16.dp),
        // not using surface's elevation here because we don't want it to change its background
        // color to gray in dark mode
    ) {
        LazyColumn(
            modifier = Modifier
                .padding(start = insets.calculateStartPadding(dir))
                .width(80.dp),
            state = state,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom,
            contentPadding = PaddingValues(
                top = insets.calculateTopPadding(),
                bottom = insets.calculateBottomPadding() + 24.dp // to align with undo button
            )
        ) {
            items(
                items = editItems,
                // NOTE: unfortunately, keys must be parcelable on Android. I wonder how they solved
                //       that in Compose multiplatform
                key = { Json.encodeToString(it.edit.key) }
            ) { editItem ->
                Column {
                    DateTimeHeader(
                        timestamp = editItem.edit.createdTimestamp,
                        showDate = editItem.showDate,
                        showTime = editItem.showTime
                    )
                    EditHistoryItem(
                        selected = selectedEdit == editItem.edit,
                        onSelect = { onSelectEdit(editItem.edit) },
                        onUndo = { onClickUndoEdit(editItem.edit) },
                        edit = editItem.edit,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }

    if (showUndoDialog && selectedEdit != null) {
        UndoDialog(
            edit = selectedEdit,
            element = editElement,
            featureDictionaryLazy = featureDictionaryLazy,
            onDismissRequest = {
                showUndoDialog = false
                editElement = null
            },
            onConfirmed = { onUndoEdit(selectedEdit) }
        )
    }
}

@Composable
private fun DateTimeHeader(
    timestamp: Long,
    showDate: Boolean,
    showTime: Boolean,
    modifier: Modifier = Modifier
) {
    CompositionLocalProvider(
        LocalTextStyle provides MaterialTheme.typography.titleSmall,
        LocalContentAlpha provides ContentAlpha.medium
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier,
        ) {
            // divider to demarcate time boundary
            if (showDate || showTime) {
                Divider()
            }
            if (showDate) {
                // locale-dependent, e.g. 13/8/24
                Text(DateFormat.getDateInstance(DateFormat.SHORT).format(timestamp))
            }
            if (showTime) {
                // locale-dependent, e.g. 12:30 PM
                Text(DateFormat.getTimeInstance(DateFormat.SHORT).format(timestamp))
            }
        }
    }
}

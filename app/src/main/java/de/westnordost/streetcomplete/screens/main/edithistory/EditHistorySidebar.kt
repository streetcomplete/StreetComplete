package de.westnordost.streetcomplete.screens.main.edithistory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Divider
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.data.edithistory.Edit
import de.westnordost.streetcomplete.ui.theme.titleSmall
import de.westnordost.streetcomplete.util.ktx.toLocalDateTime
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import java.text.DateFormat

/** Shows the edit history in a sidebar. The edit history is grouped by time and date, ordered by
 *  the most recent edit at the bottom. The list always scrolls to the currently selected edit. */
@Composable
fun EditHistorySidebar(
    editItems: List<EditItem>,
    selectedEdit: Edit?,
    onSelectEdit: (Edit) -> Unit,
    onUndoEdit: (Edit) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedIndex = remember(selectedEdit) {
        if (selectedEdit != null) editItems
            .indexOfLast { it.edit == selectedEdit }
            .takeUnless { it < 0 }
        else null
    }

    val state = rememberLazyListState(initialFirstVisibleItemIndex = selectedIndex ?: editItems.lastIndex)

    LaunchedEffect(selectedEdit) {
        if (selectedIndex != null) state.animateScrollToItem(selectedIndex)
    }

    Surface(
        modifier = modifier.width(80.dp).fillMaxHeight(),
        elevation = 16.dp,
    ) {
        val insets = WindowInsets.safeDrawing.asPaddingValues()
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .consumeWindowInsets(insets),
            state = state,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom,
            contentPadding = insets
        ) {
            items(editItems) { editItem ->

                Column {
                    DateTimeHeader(
                        timestamp = editItem.edit.createdTimestamp,
                        showDate = editItem.showDate,
                        showTime = editItem.showTime
                    )
                    EditHistoryItem(
                        selected = selectedEdit == editItem.edit,
                        onSelect = { onSelectEdit(editItem.edit) },
                        onUndo = { onUndoEdit(editItem.edit) },
                        edit = editItem.edit,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun DateTimeHeader(
    timestamp: Long,
    showDate: Boolean,
    showTime: Boolean,
    modifier: Modifier = Modifier
) {
    val contentWithMediumAlpha = LocalContentColor.current.copy(ContentAlpha.medium)
    ProvideTextStyle(MaterialTheme.typography.titleSmall.copy(color = contentWithMediumAlpha)) {
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

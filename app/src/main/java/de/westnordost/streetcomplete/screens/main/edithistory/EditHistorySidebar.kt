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
    edits: List<Edit>,
    selectedEdit: Edit?,
    onSelectEdit: (Edit) -> Unit,
    onUndoEdit: (Edit) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state = rememberLazyListState()

    LaunchedEffect(selectedEdit) {
        val index = edits.indexOf(selectedEdit)
        if (index != -1) state.animateScrollToItem(index)
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
            var currentDateTime: LocalDateTime? = null
            items(edits) { edit ->
                /* We do not use sticky headers for the dates/times because this doesn't play well
                   with window insets and we'd rather need two levels of sticky headers
                   (1. date, 2. time) which is not directly supported (compose 1.6) */
                val instant = Instant.fromEpochMilliseconds(edit.createdTimestamp)
                val dateTime = instant.toLocalDateTime()
                val showDate = dateTime.date != currentDateTime?.date
                val showTime = dateTime.hour != currentDateTime?.hour || dateTime.minute != currentDateTime?.minute
                currentDateTime = dateTime

                Column {
                    DateTimeHeader(
                        timestamp = edit.createdTimestamp,
                        showDate = showDate,
                        showTime = showTime
                    )
                    EditHistoryItem(
                        selected = selectedEdit == edit,
                        onSelect = { onSelectEdit(edit) },
                        onUndo = { onUndoEdit(edit) },
                        edit = edit,
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
            if (showDate || showTime) {
                Divider()
            }
            if (showDate) {
                Text(DateFormat.getDateInstance(DateFormat.SHORT).format(timestamp))
            }
            if (showTime) {
                Text(DateFormat.getTimeInstance(DateFormat.SHORT).format(timestamp))
            }
        }
    }
}

// TODO for initial selection, don't animate
// TODO selection animation?

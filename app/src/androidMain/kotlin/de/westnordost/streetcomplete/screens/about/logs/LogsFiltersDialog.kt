package de.westnordost.streetcomplete.screens.about.logs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.AlertDialog
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.data.logs.LogsFilters
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.cancel
import de.westnordost.streetcomplete.resources.label_log_message_contains
import de.westnordost.streetcomplete.resources.label_log_newer_than
import de.westnordost.streetcomplete.resources.label_log_older_than
import de.westnordost.streetcomplete.resources.ok
import de.westnordost.streetcomplete.resources.title_logs_filters
import de.westnordost.streetcomplete.ui.common.ClearIcon
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun LogsFiltersDialog(
    initialFilters: LogsFilters,
    onDismissRequest: () -> Unit,
    onApplyFilters: (filters: LogsFilters) -> Unit,
    modifier: Modifier = Modifier
) {
    var logLevels by remember { mutableStateOf(initialFilters.levels) }
    var messageContains by remember {
        mutableStateOf(TextFieldValue(initialFilters.messageContains.orEmpty()))
    }
    var timestampNewerThan by remember { mutableStateOf(initialFilters.timestampNewerThan) }
    var timestampOlderThan by remember { mutableStateOf(initialFilters.timestampOlderThan) }

    fun getFilters(): LogsFilters =
        LogsFilters(logLevels, messageContains.text, timestampNewerThan, timestampOlderThan)

    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = { onApplyFilters(getFilters()) }) {
                Text(stringResource(Res.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(Res.string.cancel))
            }
        },
        modifier = modifier,
        title = { Text(stringResource(Res.string.title_logs_filters)) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                LogLevelFilterChips(
                    selectedLogLevels = logLevels,
                    onSelectedLogLevels = { logLevels = it }
                )
                OutlinedTextField(
                    value = messageContains,
                    onValueChange = { messageContains = it },
                    label = { Text(stringResource(Res.string.label_log_message_contains)) },
                    trailingIcon = if (messageContains.text.isNotEmpty()) {
                        {
                            IconButton(onClick = { messageContains = TextFieldValue() }) {
                                ClearIcon()
                            }
                        }
                    } else {
                        null
                    },
                    singleLine = true,
                )
                DateTimeSelectField(
                    value = timestampNewerThan,
                    onValueChange = { timestampNewerThan = it },
                    label = { Text(stringResource(Res.string.label_log_newer_than)) },
                )
                DateTimeSelectField(
                    value = timestampOlderThan,
                    onValueChange = { timestampOlderThan = it },
                    label = { Text(stringResource(Res.string.label_log_older_than)) },
                )
            }
        }
    )
}

@Preview
@Composable
private fun LogsFiltersDialogPreview() {
    LogsFiltersDialog(
        initialFilters = LogsFilters(),
        onDismissRequest = { },
        onApplyFilters = { }
    )
}

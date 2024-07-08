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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.logs.LogsFilters
import de.westnordost.streetcomplete.ui.common.ClearIcon

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
                Text(stringResource(android.R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(android.R.string.cancel))
            }
        },
        modifier = modifier,
        title = { Text(stringResource(R.string.title_logs_filters)) },
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
                    label = { Text(stringResource(R.string.label_log_message_contains)) },
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
                    label = { Text(stringResource(R.string.label_log_newer_than)) },
                )
                DateTimeSelectField(
                    value = timestampOlderThan,
                    onValueChange = { timestampOlderThan = it },
                    label = { Text(stringResource(R.string.label_log_older_than)) },
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

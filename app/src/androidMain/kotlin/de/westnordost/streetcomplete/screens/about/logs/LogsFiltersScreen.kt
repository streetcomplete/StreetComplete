package de.westnordost.streetcomplete.screens.about.logs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.AppBarDefaults
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import de.westnordost.streetcomplete.data.logs.LogsFilters
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.label_log_message_contains
import de.westnordost.streetcomplete.resources.label_log_newer_than
import de.westnordost.streetcomplete.resources.label_log_older_than
import de.westnordost.streetcomplete.resources.title_logs_filters
import de.westnordost.streetcomplete.ui.common.BackIcon
import de.westnordost.streetcomplete.ui.common.ClearIcon
import org.jetbrains.compose.resources.stringResource

/** Allows to change filters for logs screen */
@Composable
fun LogsFiltersScreen(
    viewModel: LogsViewModel,
    onClickBack: () -> Unit,
) {
    val filters by viewModel.filters.collectAsState()

    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(stringResource(Res.string.title_logs_filters)) },
            windowInsets = AppBarDefaults.topAppBarWindowInsets,
            navigationIcon = { IconButton(onClick = onClickBack) { BackIcon() } },
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .windowInsetsPadding(WindowInsets.safeDrawing.only(
                    WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom
                ))
                .padding(16.dp)
        ) {
            LogLevelFilterChips(
                selectedLogLevels = filters.levels,
                onSelectedLogLevels = { viewModel.setFilters(filters.copy(levels = it)) }
            )
            OutlinedTextField(
                value = filters.messageContains.orEmpty(),
                onValueChange = { viewModel.setFilters(filters.copy(messageContains = it.takeIf { it.isNotEmpty() })) },
                label = { Text(stringResource(Res.string.label_log_message_contains)) },
                trailingIcon = if (filters.messageContains.orEmpty().isNotEmpty()) {
                    {
                        IconButton(onClick = { viewModel.setFilters(filters.copy(messageContains = null)) }) {
                            ClearIcon()
                        }
                    }
                } else {
                    null
                },
                singleLine = true,
            )
            DateTimeSelectField(
                value = filters.timestampNewerThan,
                onValueChange = { viewModel.setFilters(filters.copy(timestampNewerThan = it)) },
                label = { Text(stringResource(Res.string.label_log_newer_than)) },
            )
            DateTimeSelectField(
                value = filters.timestampOlderThan,
                onValueChange = { viewModel.setFilters(filters.copy(timestampOlderThan = it)) },
                label = { Text(stringResource(Res.string.label_log_older_than)) },
            )
        }
    }
}

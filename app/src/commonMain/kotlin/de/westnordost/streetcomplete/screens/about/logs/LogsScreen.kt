package de.westnordost.streetcomplete.screens.about.logs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.AppBarDefaults
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.data.logs.LogsFilters
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.BackIcon
import de.westnordost.streetcomplete.ui.common.CenteredLargeTitleHint
import de.westnordost.streetcomplete.ui.util.rememberShareFileLauncher
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** Shows the app logs */
@Composable
fun LogsScreen(
    viewModel: LogsViewModel,
    filters: LogsFilters,
    onClickFilters: () -> Unit,
    onClickBack: () -> Unit,
) {
    val logs by remember(filters) { viewModel.getLogs(filters) }.collectAsState(emptyList())
    val filtersCount = remember(filters) { filters.count() }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val shareFileLauncher = rememberShareFileLauncher()

    // Follow new logs if the previously last log is visible, i.e. the user didn't scroll up.
    // Checking whether the list is scrolled to the end now doesn't work, as the list may have
    // already been laid out with the new logs at this point
    var previousLogsCount by remember { mutableIntStateOf(0) }
    LaunchedEffect(logs.size) {
        val lastVisibleIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
        if (lastVisibleIndex == null || lastVisibleIndex >= previousLogsCount - 1) {
            listState.scrollToItem(logs.size)
        }
        previousLogsCount = logs.size
    }

    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(stringResource(Res.string.about_title_logs, logs.size)) },
            windowInsets = AppBarDefaults.topAppBarWindowInsets,
            navigationIcon = { IconButton(onClick = onClickBack) { BackIcon() } },
            actions = {
                IconButton(onClick = onClickFilters) {
                    Box {
                        Icon(
                            painter = painterResource(Res.drawable.ic_filter_list_24),
                            contentDescription = stringResource(Res.string.action_filter)
                        )
                        if (filtersCount > 0) {
                            FiltersCounter(filtersCount, Modifier.align(Alignment.TopEnd))
                        }
                    }
                }
                IconButton(onClick = {
                    coroutineScope.launch {
                        shareFileLauncher(viewModel.createLogsFile(logs))
                    }
                }) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_share_24),
                        contentDescription = stringResource(Res.string.action_share)
                    )
                }
            }
        )
        if (logs.isEmpty()) {
            CenteredLargeTitleHint(stringResource(Res.string.no_search_results))
        } else {
            val insets = WindowInsets.safeDrawing.only(
                WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom
            ).asPaddingValues()
            LazyColumn(
                state = listState,
                contentPadding = insets,
                modifier = Modifier.consumeWindowInsets(insets)
            ) {
                itemsIndexed(logs) { index, item ->
                    if (index > 0) Divider()
                    LogsRow(item, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                }
            }
        }
    }
}

@Composable
private fun FiltersCounter(count: Int, modifier: Modifier = Modifier) {
    Text(
        text = count.toString(),
        modifier = modifier
            .size(16.dp)
            .background(
                color = MaterialTheme.colors.secondary,
                shape = CircleShape
            ),
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.caption
    )
}

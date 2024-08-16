package de.westnordost.streetcomplete.screens.about.logs

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.BuildConfig
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.logs.format
import de.westnordost.streetcomplete.ui.common.BackIcon
import de.westnordost.streetcomplete.ui.common.CenteredLargeTitleHint
import de.westnordost.streetcomplete.ui.ktx.isScrolledToEnd
import de.westnordost.streetcomplete.util.ktx.now
import kotlinx.datetime.LocalDateTime

/** Shows the app logs */
@Composable
fun LogsScreen(
    viewModel: LogsViewModel,
    onClickBack: () -> Unit,
) {
    val logs by viewModel.logs.collectAsState()
    val filters by viewModel.filters.collectAsState()
    val filtersCount = remember(filters) { filters.count() }

    var showFiltersDialog by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    LaunchedEffect(logs.size) {
        if (listState.isScrolledToEnd) listState.scrollToItem(logs.size)
    }

    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(stringResource(R.string.about_title_logs, logs.size)) },
            navigationIcon = { IconButton(onClick = onClickBack) { BackIcon() } },
            actions = {
                IconButton(onClick = { showFiltersDialog = true }) {
                    Box {
                        Icon(
                            painter = painterResource(R.drawable.ic_filter_list_24dp),
                            contentDescription = stringResource(R.string.action_filter)
                        )
                        if (filtersCount > 0) {
                            FiltersCounter(filtersCount, Modifier.align(Alignment.TopEnd))
                        }
                    }
                }
                val context = LocalContext.current
                IconButton(onClick = { context.shareLog(viewModel.logs.value.format()) }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_share_24dp),
                        contentDescription = stringResource(R.string.action_share)
                    )
                }
            }
        )
        if (logs.isEmpty()) {
            CenteredLargeTitleHint(stringResource(R.string.no_search_results))
        } else {
            LazyColumn(state = listState) {
                itemsIndexed(logs) { index, item ->
                    if (index > 0) Divider()
                    LogsItem(item, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                }
            }
        }
    }

    if (showFiltersDialog) {
        LogsFiltersDialog(
            initialFilters = filters,
            onDismissRequest = { showFiltersDialog = false },
            onApplyFilters = {
                showFiltersDialog = false
                viewModel.setFilters(it)
            }
        )
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

private fun Context.shareLog(logText: String) {
    val logTimestamp = LocalDateTime.now().toString()
    val logTitle = "${BuildConfig.APPLICATION_ID}_${BuildConfig.VERSION_NAME}_$logTimestamp.log"

    val shareIntent = Intent(Intent.ACTION_SEND).also {
        it.putExtra(Intent.EXTRA_TEXT, logText)
        it.putExtra(Intent.EXTRA_TITLE, logTitle)
        it.type = "text/plain"
    }

    startActivity(Intent.createChooser(shareIntent, null))
}

package de.westnordost.streetcomplete.screens.user.edits

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.AppBarDefaults
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.ui.common.CenteredLargeTitleHint
import kotlinx.coroutines.launch

/** Shows the user's edit statistics, alternatively either grouped by edit type or by country */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EditStatisticsScreen(
    viewModel: EditStatisticsViewModel
) {
    val hasEdits by viewModel.hasEdits.collectAsState()
    val editTypeStatistics by viewModel.editTypeStatistics.collectAsState()
    val countryStatistics by viewModel.countryStatistics.collectAsState()
    val flagAlignments by viewModel.flagAlignments.collectAsState()

    if (hasEdits) {
        Column {
            val scope = rememberCoroutineScope()
            val pagerState = rememberPagerState(pageCount = { EditStatisticsTab.entries.size })
            val page = pagerState.targetPage

            TabRow(
                selectedTabIndex = page,
                modifier = Modifier.shadow(AppBarDefaults.TopAppBarElevation)
            ) {
                for (tab in EditStatisticsTab.entries) {
                    val index = tab.ordinal
                    Tab(
                        selected = page == index,
                        onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                        text = { Text(stringResource(tab.textId)) }
                    )
                }
            }

            HorizontalPager(
                state = pagerState,
                userScrollEnabled = false,
                modifier = Modifier.weight(1f)
            ) { p ->
                when (EditStatisticsTab.entries[p]) {
                    EditStatisticsTab.ByType -> {
                        LaunchedEffect(Unit) { viewModel.queryEditTypeStatistics() }
                        val statistics = editTypeStatistics
                        if (statistics != null) {
                            EditTypeStatisticsColumn(statistics = statistics)
                        }
                    }
                    EditStatisticsTab.ByCountry -> {
                        LaunchedEffect(Unit) { viewModel.queryCountryStatistics() }
                        val statistics = countryStatistics
                        val alignments = flagAlignments
                        if (statistics != null && alignments != null) {
                            CountryStatisticsColumn(
                                statistics = statistics,
                                flagAlignments = alignments
                            )
                        }
                    }
                }
            }
        }
    } else {
        val isSynchronizingStatistics by viewModel.isSynchronizingStatistics.collectAsState()
        CenteredLargeTitleHint(
            stringResource(
                if (isSynchronizingStatistics) R.string.stats_are_syncing
                else R.string.quests_empty
            )
        )
    }
}

private enum class EditStatisticsTab(val textId: Int) {
    ByType(textId = R.string.user_statistics_filter_by_quest_type),
    ByCountry(textId = R.string.user_statistics_filter_by_country)
}

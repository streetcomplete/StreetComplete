package de.westnordost.streetcomplete.screens.user.statistics

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.stringResource
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.ui.common.CenteredLargeTitleHint
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EditStatisticsScreen(
    viewModel: EditStatisticsViewModel
) {
    val isSynchronizingStatistics by viewModel.isSynchronizingStatistics.collectAsState()
    val hasEdits by viewModel.hasEdits.collectAsState()
    val countryStatistics by viewModel.countryStatistics.collectAsState()
    val editTypeStatistics by viewModel.editTypeStatistics.collectAsState()

    if (hasEdits) {
        Column {
            val coroutineScope = rememberCoroutineScope()
            val pagerState = rememberPagerState(pageCount = { 2 })
            val page = pagerState.currentPage

            LaunchedEffect(page) {
                when (page) {
                    0 -> viewModel.queryEditTypeStatistics()
                    1 -> viewModel.queryCountryStatistics()
                }
            }

            TabRow(selectedTabIndex = page) {
                Tab(
                    selected = page == 0,
                    onClick = { coroutineScope.launch { pagerState.animateScrollToPage(0) } },
                    text = { Text(stringResource(R.string.user_statistics_filter_by_quest_type)) }
                )
                Tab(
                    selected = page == 1,
                    onClick = { coroutineScope.launch { pagerState.animateScrollToPage(1) } },
                    text = { Text(stringResource(R.string.user_statistics_filter_by_country)) }
                )
            }
            HorizontalPager(
                state = pagerState,
                userScrollEnabled = true
            ) { p ->
                when (p) {
                    0 -> {
                        editTypeStatistics?.let { EditTypeStatisticsColumn(editTypeObjStatistics = it) }
                    }
                    1 -> {
                        countryStatistics?.let { CountryStatisticsColumn(countryStatistics = it) }
                    }
                }
            }
        }
    } else {
        CenteredLargeTitleHint(
            stringResource(
                if (isSynchronizingStatistics) R.string.stats_are_syncing
                else R.string.quests_empty
            )
        )
    }
}


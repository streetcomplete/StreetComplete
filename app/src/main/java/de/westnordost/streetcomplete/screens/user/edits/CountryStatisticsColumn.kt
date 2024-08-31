package de.westnordost.streetcomplete.screens.user.edits

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.ui.theme.GrassGreen
import de.westnordost.streetcomplete.ui.theme.LeafGreen

/** Simple bar chart of solved quests by country */
@Composable
fun CountryStatisticsColumn(
    statistics: List<CompleteCountryStatistics>,
    flagAlignments: Map<String, FlagAlignment>,
    modifier: Modifier = Modifier,
) {
    var showInfo by remember { mutableStateOf<CompleteCountryStatistics?>(null) }

    // list is sorted by largest count descending
    val maxCount = statistics.firstOrNull()?.count ?: 0
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(top = 16.dp)
    ) {
        items(
            items = statistics,
            key = { it.countryCode }
        ) { item ->
            BarChartRow(
                title = {
                    CircularFlag(
                        countryCode = item.countryCode,
                        flagAlignment = flagAlignments[item.countryCode] ?: FlagAlignment.Center,
                        modifier = Modifier.size(48.dp)
                    )
                },
                count = item.count,
                countNew = item.countCurrentWeek,
                maxCount = maxCount,
                modifier = Modifier
                    .clickable { showInfo = item }
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                color = GrassGreen,
                colorNew = LeafGreen
            )
        }
    }

    showInfo?.let {
        CountryDialog(
            countryCode = it.countryCode,
            rank = it.rank,
            rankCurrentWeek = it.rankCurrentWeek,
            onDismissRequest = { showInfo = null }
        )
    }
}

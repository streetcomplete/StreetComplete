package de.westnordost.streetcomplete.screens.user.edits

import androidx.compose.foundation.clickable
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
import de.westnordost.streetcomplete.data.user.statistics.CountryStatistics
import de.westnordost.streetcomplete.ui.theme.GrassGreen

/** Simple bar chart of solved quests by country */
@Composable
fun CountryStatisticsColumn(
    countryStatistics: List<CountryStatistics>,
    flagAlignments: Map<String, FlagAlignment>,
    modifier: Modifier = Modifier,
) {
    var showInfo by remember { mutableStateOf<CountryStatistics?>(null) }

    // list is sorted by largest count descending
    val maxCount = countryStatistics.firstOrNull()?.count ?: 0
    LazyColumn(modifier) {
        items(
            items = countryStatistics,
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
                maxCount = maxCount,
                modifier = Modifier
                    .clickable { showInfo = item }
                    .padding(8.dp),
                color = GrassGreen,
            )
        }
    }

    showInfo?.let {
        CountryDialog(
            countryCode = it.countryCode,
            rank = it.rank,
            onDismissRequest = { showInfo = null }
        )
    }
}

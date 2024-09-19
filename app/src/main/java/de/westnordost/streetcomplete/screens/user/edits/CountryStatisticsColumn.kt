package de.westnordost.streetcomplete.screens.user.edits

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
    statistics: List<CountryStatistics>,
    flagAlignments: Map<String, FlagAlignment>,
    isCurrentWeek: Boolean,
    modifier: Modifier = Modifier,
) {
    var showInfo by remember { mutableStateOf<CountryStatistics?>(null) }

    val countUpAnim = remember(statistics) { Animatable(0f) }
    LaunchedEffect(statistics) {
        countUpAnim.animateTo(1f, tween(2000))
    }

    // list is sorted by largest count descending
    val maxCount = statistics.firstOrNull()?.count ?: 0
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(top = 16.dp)
    ) {
        items(statistics) { item ->
            BarChartRow(
                title = {
                    CircularFlag(
                        countryCode = item.countryCode,
                        flagAlignment = flagAlignments[item.countryCode] ?: FlagAlignment.Center,
                        modifier = Modifier.size(48.dp)
                    )
                },
                count = item.count * countUpAnim.value,
                maxCount = maxCount,
                modifier = Modifier
                    .clickable { showInfo = item }
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                color = GrassGreen,
            )
        }
    }

    showInfo?.let { country ->
        CountryDialog(
            countryCode = country.countryCode,
            rank = country.rank,
            isCurrentWeek = isCurrentWeek,
            onDismissRequest = { showInfo = null }
        )
    }
}

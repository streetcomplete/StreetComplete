package de.westnordost.streetcomplete.ui.common.opening_hours

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.osm.opening_hours.HierarchicOpeningHours
import de.westnordost.streetcomplete.osm.opening_hours.toWeekdaysSelectors
import kotlinx.serialization.Serializable

/** Displays the given [openingHours] for editing and has an Add-button to add times */
@Composable
fun OpeningHoursTable(
    openingHours: HierarchicOpeningHours,
    onChange: (HierarchicOpeningHours) -> Unit,
    timeMode: TimeMode,
    countryInfo: CountryInfo,
    addButtonContent: @Composable (RowScope.() -> Unit),
    modifier: Modifier = Modifier,
    countryLocale: Locale? = null,
    userLocale: Locale? = null,
    enabled: Boolean = true,
) {
    val workweek = remember(countryInfo) {
        countryInfo.workweek.toWeekdaysSelectors()
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MonthsColumn(
            monthsList = openingHours.monthsList,
            onChange = { onChange(HierarchicOpeningHours(it)) },
            countryLocale = countryLocale,
            userLocale = userLocale,
            enabled = enabled,
        )

        if (enabled) {
            AddOpeningHoursButton(
                openingHours = openingHours,
                onChange = onChange,
                timeMode = timeMode,
                workweek = workweek,
                countryLocale = countryLocale,
                userLocale = userLocale,
                content = addButtonContent,
            )
        }
    }
}

@Serializable
enum class TimeMode {
    /** May only add time points, e.g. "08:00" */
    Points,
    /** May only add time spans, e.g. "08:00-12:00" */
    Spans,
}

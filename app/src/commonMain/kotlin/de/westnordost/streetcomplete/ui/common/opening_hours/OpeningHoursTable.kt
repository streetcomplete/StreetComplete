package de.westnordost.streetcomplete.ui.common.opening_hours

import androidx.compose.material.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.osm.opening_hours.HierarchicOpeningHours
import de.westnordost.streetcomplete.osm.opening_hours.toWeekdaysSelectors
import de.westnordost.streetcomplete.ui.ktx.pxToDp
import de.westnordost.streetcomplete.util.locale.DateTimeFormatStyle
import de.westnordost.streetcomplete.util.locale.LocalTimeFormatter
import kotlinx.datetime.LocalTime

/** Displays the given [openingHours] for editing */
@Composable
fun OpeningHoursTable(
    openingHours: HierarchicOpeningHours,
    onChange: (HierarchicOpeningHours) -> Unit,
    timeMode: TimeMode,
    countryInfo: CountryInfo,
    modifier: Modifier = Modifier,
    locale: Locale = Locale.current,
    userLocale: Locale = Locale.current,
    enabled: Boolean = true,
    addMonthsEnabledWhenEmpty: Boolean = true,
) {
    val initialWeekdaysSelectors = remember(countryInfo) {
        countryInfo.workweek.toWeekdaysSelectors()
    }

    MonthsColumn(
        monthsList = openingHours.monthsList,
        onChange = { onChange(HierarchicOpeningHours(it)) },
        timeMode = timeMode,
        modifier = modifier,
        initialWeekdaysSelectors = initialWeekdaysSelectors,
        locale = locale,
        userLocale = userLocale,
        enabled = enabled,
        addEnabledWhenEmpty = addMonthsEnabledWhenEmpty,
    )
}

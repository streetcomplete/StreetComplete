package de.westnordost.streetcomplete.ui.common.opening_hours

import androidx.compose.material.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.rememberTextMeasurer
import de.westnordost.streetcomplete.osm.opening_hours.HierarchicOpeningHours
import de.westnordost.streetcomplete.ui.ktx.pxToDp
import de.westnordost.streetcomplete.util.locale.DateTimeFormatStyle
import de.westnordost.streetcomplete.util.locale.LocalTimeFormatter
import kotlinx.datetime.LocalTime

/** Displays the given [openingHours] for editing */
@Composable
fun MonthsColumn(
    openingHours: HierarchicOpeningHours,
    onChange: (HierarchicOpeningHours) -> Unit,
    timeMode: TimeMode,
    modifier: Modifier = Modifier,
    locale: Locale = Locale.current,
    userLocale: Locale = Locale.current,
    enabled: Boolean = true,
) {
    val textStyle = LocalTextStyle.current
    val textMeasurer = rememberTextMeasurer(1)
    val timeWidthPx = remember(locale) {
        val timeFormatter = LocalTimeFormatter(locale = locale, style = DateTimeFormatStyle.Short)
        val rangeText = localizedRange(
            start = timeFormatter.format(LocalTime(13,59)),
            end = timeFormatter.format(LocalTime(23,59)),
            locale = locale,
        )
        textMeasurer.measure(text = rangeText, style = textStyle).size.width
    }
    val timeWidth = timeWidthPx.pxToDp()

    MonthsColumn(
        monthsList = openingHours.monthsList,
        onChange = { onChange(HierarchicOpeningHours(it)) },
        timeMode = timeMode,
        timeTextWidth = timeWidth,
        modifier = modifier,
        locale = locale,
        userLocale = userLocale,
        enabled = enabled,
    )
}

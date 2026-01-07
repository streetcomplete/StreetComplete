package de.westnordost.streetcomplete.ui.common.opening_hours

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.LayoutDirection.Ltr
import androidx.compose.ui.unit.dp
import de.westnordost.osm_opening_hours.model.Holiday
import de.westnordost.osm_opening_hours.model.HolidaySelector
import de.westnordost.osm_opening_hours.model.Weekday
import de.westnordost.osm_opening_hours.model.WeekdayRange
import de.westnordost.osm_opening_hours.model.WeekdaysSelector
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.quest_openingHours_unspecified_range
import de.westnordost.streetcomplete.util.locale.DateTimeTextSymbolStyle
import org.jetbrains.compose.resources.stringResource

/** A text that shows a list of localized weekdays and holidays and lets the user change them.
 *  E.g. Mon-Fri, Sun, PH */
@Composable
fun WeekdayAndHolidaySelectorsText(
    weekdays: List<WeekdaysSelector>,
    holidays: List<HolidaySelector>,
    onChange: (weekdays: List<WeekdaysSelector>, holidays: List<HolidaySelector>) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    locale: Locale = Locale.current,
    userLocale: Locale = Locale.current,
) {
    val layoutDirection = LocalLayoutDirection.current
    var showDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .clickable(enabled) { showDialog = true }
            .defaultMinSize(minHeight = 48.dp)
            .padding(8.dp),
        contentAlignment = Alignment.CenterEnd
    ) {
        Text(
            text = getWeekdaysAndHolidaysString(weekdays, holidays, locale, layoutDirection),
            textAlign = TextAlign.End,
        )
    }

    if (showDialog) {
        WeekdayAndHolidaySelectDialog(
            onDismissRequest = { showDialog = false },
            initialWeekdays = weekdays,
            initialHolidays = holidays,
            onSelected = onChange,
            locale = locale,
            userLocale = userLocale,
        )
    }
}

@Composable
private fun getWeekdaysAndHolidaysString(
    weekdays: List<WeekdaysSelector>,
    holidays: List<HolidaySelector>,
    locale: Locale,
    layoutDirection: LayoutDirection,
): String {
    if (weekdays.isEmpty() && holidays.isEmpty()) {
        return "(" + stringResource(Res.string.quest_openingHours_unspecified_range) + ")"
    }

    val style = DateTimeTextSymbolStyle.Short
    val sb = StringBuilder()

    val weekdaysStrings = weekdays.map { it.toLocalizedString(
        style = style,
        layoutDirection = layoutDirection,
        locale = locale,
    ) }
    val holidaysStrings = holidays.map { holiday ->
        when (holiday) {
            is Holiday -> stringResource(holiday.getDisplayNameResource(style))
            else -> throw UnsupportedOperationException()
        }
    }

    sb.append(weekdaysStrings.joinToLocalizedString(
        locale = locale,
        layoutDirection = layoutDirection,
    ))
    if (holidays.isNotEmpty()) {
        if (sb.isNotEmpty()) {
            sb.append(enumerationSeparator(locale))
        }
        sb.append(holidaysStrings.joinToLocalizedString(
            locale = locale,
            layoutDirection = layoutDirection,
        ))
    }
    return sb.toString()
}

private fun WeekdaysSelector.toLocalizedString(
    style: DateTimeTextSymbolStyle = DateTimeTextSymbolStyle.Short,
    layoutDirection: LayoutDirection = Ltr,
    locale: Locale? = null
): String =
    when (this) {
        is Weekday -> {
            getDisplayName(style, locale)
        }
        is WeekdayRange -> {
            localizedRange(
                start = start.getDisplayName(style, locale),
                end = end.getDisplayName(style, locale),
                locale = locale,
                layoutDirection = layoutDirection
            )
        }
        else -> throw UnsupportedOperationException()
    }

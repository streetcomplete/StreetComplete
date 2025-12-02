package de.westnordost.streetcomplete.ui.common.opening_hours

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.LayoutDirection.Ltr
import de.westnordost.osm_opening_hours.model.Holiday
import de.westnordost.osm_opening_hours.model.HolidaySelector
import de.westnordost.osm_opening_hours.model.Weekday
import de.westnordost.osm_opening_hours.model.WeekdayRange
import de.westnordost.osm_opening_hours.model.WeekdaysSelector
import de.westnordost.streetcomplete.util.locale.DateTimeTextSymbolStyle
import org.jetbrains.compose.resources.stringResource

/** A text that shows a list of localized weekdays and holidays. E.g. Mon-Fri, Sun, PH */
@Composable
fun WeekAndHolidaysSelectorsText(
    weekdays: List<WeekdaysSelector>,
    holidays: List<HolidaySelector>,
    isRestrictedByHolidays: Boolean,
    modifier: Modifier = Modifier,
) {
    if (isRestrictedByHolidays) throw UnsupportedOperationException()

    val locale = Locale.current
    val layoutDirection = LocalLayoutDirection.current
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
        holidaysStrings.joinToLocalizedString(
            locale = locale,
            layoutDirection = layoutDirection,
        )
    }
    Text(sb.toString(), modifier)
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

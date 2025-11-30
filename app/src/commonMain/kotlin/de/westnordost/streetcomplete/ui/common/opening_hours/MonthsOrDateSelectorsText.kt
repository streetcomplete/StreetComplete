package de.westnordost.streetcomplete.ui.common.opening_hours

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.LayoutDirection.Ltr
import de.westnordost.osm_opening_hours.model.CalendarDate
import de.westnordost.osm_opening_hours.model.DateRange
import de.westnordost.osm_opening_hours.model.DatesInMonth
import de.westnordost.osm_opening_hours.model.Month
import de.westnordost.osm_opening_hours.model.MonthRange
import de.westnordost.osm_opening_hours.model.MonthsOrDateSelector
import de.westnordost.osm_opening_hours.model.SingleMonth
import de.westnordost.osm_opening_hours.model.SpecificWeekdayDate
import de.westnordost.osm_opening_hours.model.StartingAtDate
import de.westnordost.osm_opening_hours.model.VariableDate
import de.westnordost.streetcomplete.util.ktx.getDisplayName
import de.westnordost.streetcomplete.util.locale.DateTimeTextSymbolStyle

/** A text that shows a list of localized months. E.g. Apr-Aug, Dec */
@Composable
fun MonthsOrDateSelectorsText(
    months: List<MonthsOrDateSelector>,
    modifier: Modifier,
) {
    val locale = Locale.current
    val layoutDirection = LocalLayoutDirection.current
    val style = DateTimeTextSymbolStyle.Short

    val monthsStrings = months
        .map { it.toLocalizedString(
                style = style,
                layoutDirection = layoutDirection,
                locale = locale,
        ) }
        .joinToLocalizedString(
            locale = locale,
            layoutDirection = layoutDirection,
        )

    Text(monthsStrings, modifier)
}

private fun MonthsOrDateSelector.toLocalizedString(
    style: DateTimeTextSymbolStyle = DateTimeTextSymbolStyle.Short,
    layoutDirection: LayoutDirection = Ltr,
    locale: Locale? = null,
): String {
    return when (this) {
        is MonthRange -> {
            if (year != null) throw UnsupportedOperationException()
            localizedRange(
                start = start.toKotlinDateTimeMonth().getDisplayName(style, locale),
                end = end.toKotlinDateTimeMonth().getDisplayName(style, locale),
                locale = locale,
                layoutDirection = layoutDirection,
            )
        }
        is SingleMonth -> {
            if (year != null) throw UnsupportedOperationException()
            month.toKotlinDateTimeMonth().getDisplayName(style, locale)
        }
        // any date not supported
        is StartingAtDate,
        is CalendarDate,
        is DateRange,
        is DatesInMonth,
        is VariableDate,
        is SpecificWeekdayDate,
            -> throw UnsupportedOperationException()
    }
}

private fun Month.toKotlinDateTimeMonth() =
    kotlinx.datetime.Month(ordinal + 1)

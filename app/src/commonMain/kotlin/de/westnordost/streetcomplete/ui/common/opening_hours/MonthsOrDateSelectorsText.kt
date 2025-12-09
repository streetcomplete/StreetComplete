package de.westnordost.streetcomplete.ui.common.opening_hours

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.LayoutDirection.Ltr
import de.westnordost.osm_opening_hours.model.MonthRange
import de.westnordost.osm_opening_hours.model.MonthsOrDateSelector
import de.westnordost.osm_opening_hours.model.SingleMonth
import de.westnordost.streetcomplete.util.locale.DateTimeTextSymbolStyle

/** A text that shows a list of localized months. E.g. Apr-Aug, Dec */
@Composable
fun MonthsOrDateSelectorsText(
    months: List<MonthsOrDateSelector>,
    modifier: Modifier = Modifier,
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
                start = start.getDisplayName(style, locale),
                end = end.getDisplayName(style, locale),
                locale = locale,
                layoutDirection = layoutDirection,
            )
        }
        is SingleMonth -> {
            if (year != null) throw UnsupportedOperationException()
            month.getDisplayName(style, locale)
        }
        // any date not supported
        else -> throw UnsupportedOperationException()
    }
}

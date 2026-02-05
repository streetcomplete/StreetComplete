package de.westnordost.streetcomplete.ui.common.opening_hours

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.LayoutDirection.Ltr
import androidx.compose.ui.unit.dp
import de.westnordost.osm_opening_hours.model.MonthRange
import de.westnordost.osm_opening_hours.model.MonthsOrDateSelector
import de.westnordost.osm_opening_hours.model.SingleMonth
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.quest_openingHours_unspecified_range
import de.westnordost.streetcomplete.util.locale.DateTimeTextSymbolStyle
import org.jetbrains.compose.resources.stringResource

/** A text that shows a list of localized months. E.g. Apr-Aug, Dec */
@Composable
fun MonthsText(
    months: List<MonthsOrDateSelector>,
    onChange: (List<MonthsOrDateSelector>) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    locale: Locale = Locale.current,
    userLocale: Locale = Locale.current,
) {
    val layoutDirection = LocalLayoutDirection.current
    var showDialog by remember { mutableStateOf(false) }

    val isError = months.isEmpty()
    Text(
        text = getMonthsString(months, locale, layoutDirection),
        modifier = modifier
            .clickable(enabled) { showDialog = true }
            .padding(8.dp),
        color = if (isError) MaterialTheme.colors.error else Color.Unspecified
    )

    if (showDialog) {
        MonthsOrDateSelectorSelectDialog(
            onDismissRequest = { showDialog = false },
            initialMonths = months,
            onSelected = onChange,
            locale = locale,
            userLocale = userLocale,
        )
    }
}

@Composable
private fun getMonthsString(
    months: List<MonthsOrDateSelector>,
    locale: Locale,
    layoutDirection: LayoutDirection,
): String {
    if (months.isEmpty()) {
        return "(" + stringResource(Res.string.quest_openingHours_unspecified_range) + ")"
    }
    val style = DateTimeTextSymbolStyle.Short

    return months
        .map { it.toLocalizedString(
            style = style,
            layoutDirection = layoutDirection,
            locale = locale,
        ) }
        .joinToLocalizedString(
            locale = locale,
            layoutDirection = layoutDirection,
        )
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

package de.westnordost.streetcomplete.ui.common.opening_hours

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.rememberTextMeasurer
import de.westnordost.streetcomplete.osm.opening_hours.HierarchicOpeningHours
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.ic_add_24
import de.westnordost.streetcomplete.resources.quest_openingHours_add_months
import de.westnordost.streetcomplete.ui.ktx.pxToDp
import de.westnordost.streetcomplete.util.locale.DateTimeFormatStyle
import de.westnordost.streetcomplete.util.locale.LocalTimeFormatter
import kotlinx.datetime.LocalTime
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** Displays the given [openingHours] for editing */
@Composable
fun OpeningHoursTable(
    openingHours: HierarchicOpeningHours,
    modifier: Modifier = Modifier,
    locale: Locale = Locale.current,
    userLocale: Locale = Locale.current,
    enabled: Boolean = true,
) {
    val locale = Locale.current
    val textStyle = LocalTextStyle.current
    val textMeasurer = rememberTextMeasurer()
    val timesWidthPx = remember(locale) {
        val timeFormatter = LocalTimeFormatter(locale = locale, style = DateTimeFormatStyle.Short)
        // TODO only if we display ranges...
        val rangeText = localizedRange(
             start = timeFormatter.format(LocalTime(13,59)),
             end = timeFormatter.format(LocalTime(23,59)),
             locale = locale,
        )
        textMeasurer.measure(text = rangeText, style = textStyle).size.width
    }
    val timesWidth = timesWidthPx.pxToDp()

    Column(
        modifier = modifier
    ) {
        for ((monthsIndex, months) in openingHours.monthsList.withIndex()) {
            if (months.monthsSelectors.isNotEmpty()) {
                MonthsText(
                    months = months.monthsSelectors,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled) {
                            // TODO
                        }
                )
            }
            if (monthsIndex > 0 || months.monthsSelectors.isNotEmpty()) {
                Divider()
            }

        }
        if (enabled) {
            Divider()
            OutlinedButton(
                onClick = { /** TODO add month... */ }
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_add_24),
                    contentDescription = stringResource(Res.string.quest_openingHours_add_months)
                )
            }
        }
    }
}


package de.westnordost.streetcomplete.ui.common.opening_hours

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Divider
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateSet
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.dp
import de.westnordost.osm_opening_hours.model.Holiday
import de.westnordost.osm_opening_hours.model.HolidaySelector
import de.westnordost.osm_opening_hours.model.Weekday
import de.westnordost.osm_opening_hours.model.WeekdaysSelector
import de.westnordost.streetcomplete.osm.opening_hours.getHolidays
import de.westnordost.streetcomplete.osm.opening_hours.getWeekdays
import de.westnordost.streetcomplete.osm.opening_hours.toWeekdaysSelectors
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.cancel
import de.westnordost.streetcomplete.resources.ok
import de.westnordost.streetcomplete.resources.quest_openingHours_chooseWeekdaysTitle
import de.westnordost.streetcomplete.resources.quest_opening_hours_two_languages
import de.westnordost.streetcomplete.ui.common.CheckboxGroup
import de.westnordost.streetcomplete.ui.common.dialogs.ScrollableAlertDialog
import de.westnordost.streetcomplete.ui.ktx.fadingVerticalScrollEdges
import org.jetbrains.compose.resources.stringResource

/** Dialog in which to select a number of weekdays */
@Composable
fun WeekdayAndHolidaySelectDialog(
    onDismissRequest: () -> Unit,
    onSelected: (weekdays: List<WeekdaysSelector>, holidays: List<HolidaySelector>) -> Unit,
    modifier: Modifier = Modifier,
    initialWeekdays: List<WeekdaysSelector> = emptyList(),
    initialHolidays: List<HolidaySelector> = emptyList(),
    locale: Locale = Locale.current,
    userLocale: Locale = Locale.current,
) {
    val scrollState = rememberScrollState()
    val weekdayItems = Weekday.entries
    val weekdaySelection = remember(initialWeekdays) {
        SnapshotStateSet<Weekday>().also { it.addAll(initialWeekdays.getWeekdays()) }
    }
    val holidayItems = listOf(Holiday.PublicHoliday)
    val holidaySelection = remember(initialHolidays) {
        SnapshotStateSet<Holiday>().also { it.addAll(initialHolidays.getHolidays()) }
    }

    ScrollableAlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        title = { Text(stringResource(Res.string.quest_openingHours_chooseWeekdaysTitle)) },
        content = {
            CompositionLocalProvider(
                LocalContentAlpha provides ContentAlpha.high,
                LocalTextStyle provides MaterialTheme.typography.body1
            ) {
                Column(
                    Modifier
                        .fadingVerticalScrollEdges(scrollState, 32.dp)
                        .padding(horizontal = 24.dp)
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    CheckboxGroup(
                        options = weekdayItems,
                        onSelectionChange = { weekday, checked ->
                            if (checked) weekdaySelection.add(weekday)
                            else weekdaySelection.remove(weekday)
                        },
                        selectedOptions = weekdaySelection,
                        itemContent = {
                            val text = it.getDisplayName(locale = locale)
                            val textInUserLocale = it.getDisplayName(locale = userLocale)
                            if (text != textInUserLocale) {
                                Text(stringResource(Res.string.quest_opening_hours_two_languages, text, textInUserLocale))
                            } else {
                                Text(text)
                            }
                        }
                    )
                    Divider()
                    CheckboxGroup(
                        options = holidayItems,
                        onSelectionChange = { holiday, checked ->
                            if (checked) holidaySelection.add(holiday)
                            else holidaySelection.remove(holiday)
                        },
                        selectedOptions = holidaySelection,
                        itemContent = { Text(stringResource(it.getDisplayNameResource())) }
                    )
                }
            }
        },
        buttons = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(Res.string.cancel))
            }
            TextButton(onClick = {
                onSelected(
                    weekdaySelection.toWeekdaysSelectors(),
                    holidaySelection.toList()
                )
                onDismissRequest()
            }) {
                Text(stringResource(Res.string.ok))
            }
        }
    )
}

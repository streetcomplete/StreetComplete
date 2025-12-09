package de.westnordost.streetcomplete.ui.common.opening_hours

import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateSet
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.intl.Locale
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
import de.westnordost.streetcomplete.ui.common.CheckboxList
import de.westnordost.streetcomplete.ui.common.dialogs.ScrollableAlertDialog
import org.jetbrains.compose.resources.stringResource
import kotlin.jvm.JvmInline

/** Dialog in which to select a number of weekdays */
@Composable
fun WeekdaySelectDialog(
    onDismissRequest: () -> Unit,
    initialWeekdays: List<WeekdaysSelector>,
    initialHolidays: List<HolidaySelector>,
    onSelected: (weekdays: List<WeekdaysSelector>, holidays: List<HolidaySelector>) -> Unit,
    modifier: Modifier = Modifier,
) {
    val locale = Locale.current
    val scrollState = rememberScrollState()
    val items =
        Weekday.entries.map { WeekdayOrHoliday.Wd(it) } +
        WeekdayOrHoliday.Hd(Holiday.PublicHoliday)
    val selection = remember(initialWeekdays, initialHolidays) {
        SnapshotStateSet<WeekdayOrHoliday>().also { set ->
            set.addAll(initialWeekdays.getWeekdays().map { WeekdayOrHoliday.Wd(it) })
            set.addAll(initialHolidays.getHolidays().map { WeekdayOrHoliday.Hd(it) })
        }
    }

    ScrollableAlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        title = { Text(stringResource(Res.string.quest_openingHours_chooseWeekdaysTitle)) },
        content = {
            CheckboxList(
                options = items,
                onToggle = { w, checked ->
                    if (checked) selection.add(w)
                    else selection.remove(w)
                },
                selectedOptions = selection,
                itemContent = { w ->
                    val text = when (w) {
                        is WeekdayOrHoliday.Hd ->
                            stringResource(w.value.getDisplayNameResource())
                        is WeekdayOrHoliday.Wd ->
                            w.value.getDisplayName(locale = locale)
                    }
                    Text(text)
                }
            )
        },
        buttons = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(Res.string.cancel))
            }
            TextButton(onClick = {
                val selectedWeekdays = selection
                    .filterIsInstance<WeekdayOrHoliday.Wd>()
                    .mapTo(HashSet()) { it.value }
                val selectedHolidays = selection
                    .filterIsInstance<WeekdayOrHoliday.Hd>()
                    .map { it.value }

                onDismissRequest()
                onSelected(
                    selectedWeekdays.toWeekdaysSelectors(),
                    selectedHolidays
                )
            }) {
                Text(stringResource(Res.string.ok))
            }
        }
    )
}

private sealed interface WeekdayOrHoliday {
    @JvmInline value class Wd(val value: Weekday): WeekdayOrHoliday
    @JvmInline value class Hd(val value: Holiday): WeekdayOrHoliday
}

package de.westnordost.streetcomplete.ui.common.opening_hours

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Checkbox
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateSet
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import de.westnordost.osm_opening_hours.model.Holiday
import de.westnordost.osm_opening_hours.model.HolidaySelector
import de.westnordost.osm_opening_hours.model.HolidayWithOffset
import de.westnordost.osm_opening_hours.model.SpecificWeekdays
import de.westnordost.osm_opening_hours.model.Weekday
import de.westnordost.osm_opening_hours.model.WeekdayRange
import de.westnordost.osm_opening_hours.model.WeekdaysSelector
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.cancel
import de.westnordost.streetcomplete.resources.ok
import de.westnordost.streetcomplete.resources.quest_openingHours_chooseWeekdaysTitle
import de.westnordost.streetcomplete.ui.common.dialogs.AlertDialogLayout
import de.westnordost.streetcomplete.ui.ktx.fadingVerticalScrollEdges
import org.jetbrains.compose.resources.stringResource

/** Dialog in which to select a number of weekdays */
@Composable
fun WeekdaySelectDialog(
    onDismissRequest: () -> Unit,
    selectedWeekdays: List<WeekdaysSelector>,
    selectedHolidays: List<HolidaySelector>,
    onSelected: (weekdays: List<WeekdaysSelector>, holidays: List<HolidaySelector>) -> Unit,
    modifier: Modifier = Modifier,
) {
    val locale = Locale.current
    val scrollState = rememberScrollState()
    val weekdaysSelection = remember(selectedWeekdays) {
        SnapshotStateSet<Weekday>().also { it.addAll(selectedWeekdays.getSelectedWeekdays()) }
    }
    var isPublicHolidaySelected by remember(selectedHolidays) {
        mutableStateOf(selectedHolidays.isPublicHolidaySelected())
    }

    fun toggleWeekday(checked: Boolean, weekday: Weekday) {
        if (checked) weekdaysSelection.add(weekday)
        else weekdaysSelection.remove(weekday)
    }

    Dialog(
        onDismissRequest = onDismissRequest,
    ) {
        AlertDialogLayout(
            modifier = modifier,
            title = { Text(stringResource(Res.string.quest_openingHours_chooseWeekdaysTitle)) },
            content = {
                Column(Modifier
                    .fadingVerticalScrollEdges(scrollState, 64.dp)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 24.dp),
                ) {
                    for (weekday in Weekday.entries) {
                        val checked = weekday in weekdaysSelection
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .toggleable(checked) { toggleWeekday(it, weekday) }
                        ) {
                            Checkbox(
                                checked = checked,
                                onCheckedChange = { toggleWeekday(it, weekday) },
                            )
                            Text(weekday.getDisplayName(locale = locale))
                        }
                    }
                    Divider()
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .toggleable(isPublicHolidaySelected) { isPublicHolidaySelected = it }
                    ) {
                        Checkbox(
                            checked = isPublicHolidaySelected,
                            onCheckedChange = { isPublicHolidaySelected = it }
                        )
                        Text(stringResource(Holiday.PublicHoliday.getDisplayNameResource()))
                    }
                }
            },
            buttons = {
                TextButton(onClick = onDismissRequest) {
                    Text(stringResource(Res.string.cancel))
                }
                TextButton(onClick = {
                    onDismissRequest()
                    onSelected(
                        weekdaysSelection.toWeekdaysSelectors(),
                        if (isPublicHolidaySelected) listOf(Holiday.PublicHoliday) else listOf()
                    )
                }) {
                    Text(stringResource(Res.string.ok))
                }
            }
        )
    }
}

fun List<HolidaySelector>.isPublicHolidaySelected(): Boolean {
    for (selector in this) {
        when (selector) {
            Holiday.PublicHoliday -> return true
            Holiday.SchoolHoliday -> throw UnsupportedOperationException()
            is HolidayWithOffset -> throw UnsupportedOperationException()
        }
    }
    return false
}

private fun Set<Weekday>.toWeekdaysSelectors(): List<WeekdaysSelector> =
    toOrdinalRanges(Weekday.entries).flatMap {
        val start = it.start
        val end = it.endInclusive
        if (start == end) {
            listOf(Weekday.entries[start])
        } else if (start + 1 == end) {
            listOf(Weekday.entries[start], Weekday.entries[end])
        } else {
            listOf(WeekdayRange(Weekday.entries[start], Weekday.entries[end]))
        }
    }

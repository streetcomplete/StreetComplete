package de.westnordost.streetcomplete.ui.common.opening_hours

import androidx.compose.foundation.layout.Box
import androidx.compose.material.DropdownMenu
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.intl.Locale
import de.westnordost.osm_opening_hours.model.WeekdaysSelector
import de.westnordost.streetcomplete.osm.opening_hours.HierarchicOpeningHours
import de.westnordost.streetcomplete.osm.opening_hours.Times
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.quest_openingHours_add_hours
import de.westnordost.streetcomplete.resources.quest_openingHours_add_months
import de.westnordost.streetcomplete.resources.quest_openingHours_add_off_days
import de.westnordost.streetcomplete.resources.quest_openingHours_add_time
import de.westnordost.streetcomplete.resources.quest_openingHours_add_times
import de.westnordost.streetcomplete.resources.quest_openingHours_add_weekdays
import de.westnordost.streetcomplete.ui.common.Button2
import de.westnordost.streetcomplete.ui.common.DropdownMenuItem
import org.jetbrains.compose.resources.stringResource

@Composable
fun AddOpeningHoursButton(
    openingHours: HierarchicOpeningHours,
    onChange: (HierarchicOpeningHours) -> Unit,
    timeMode: TimeMode,
    workweek: List<WeekdaysSelector>,
    modifier: Modifier = Modifier,
    locale: Locale = Locale.current,
    userLocale: Locale = Locale.current,
    addMonthsEnabledWhenEmpty: Boolean = true,
) {
    var showAddDropdown by remember { mutableStateOf(false) }
    var addOpeningHoursRequest by remember { mutableStateOf<AddOpeningHoursRequest?>(null) }

    Box(modifier) {
        Button2(
            onClick = { showAddDropdown = true }
        ) {
            Text(stringResource(Res.string.quest_openingHours_add_times))
        }
        SelectOpeningHoursDropdown(
            expanded = showAddDropdown,
            openingHours = openingHours,
            timeMode = timeMode,
            addMonthsEnabledWhenEmpty = addMonthsEnabledWhenEmpty,
            onDismissRequest = { showAddDropdown = false },
            onSelect = { addOpeningHoursRequest = it }
        )
    }

    addOpeningHoursRequest?.let { requestedData ->
        AddOpeningHoursDialogCascade(
            onDismissRequest = { addOpeningHoursRequest = null },
            requestedData = requestedData,
            openingHours = openingHours,
            onChange = onChange,
            workweek = workweek,
            timeMode = timeMode,
            locale = locale,
            userLocale = userLocale,
        )
    }
}


@Composable
private fun SelectOpeningHoursDropdown(
    expanded: Boolean,
    openingHours: HierarchicOpeningHours,
    timeMode: TimeMode,
    addMonthsEnabledWhenEmpty: Boolean,
    onDismissRequest: () -> Unit,
    onSelect: (AddOpeningHoursRequest) -> Unit,
    modifier: Modifier = Modifier,
) {
    val showAddTimes = openingHours.monthsList.lastOrNull()?.weekdaysList?.lastOrNull()?.times is Times
    val showAddMonths = addMonthsEnabledWhenEmpty || openingHours.monthsList.any { it.selectors.isNotEmpty() }

    // don't even show dropdown menu when one can only add weekdays
    if (expanded && !showAddTimes && !showAddMonths) {
        LaunchedEffect(expanded && !showAddTimes && !showAddMonths) {
            onSelect(AddOpeningHoursRequest.SelectWeekdays)
        }
    }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier,
    ) {
        if (showAddTimes) {
            DropdownMenuItem(onClick = { onSelect(AddOpeningHoursRequest.SelectTimes) }) {
                Text(stringResource(when (timeMode) {
                    TimeMode.Points -> Res.string.quest_openingHours_add_time
                    TimeMode.Spans -> Res.string.quest_openingHours_add_hours
                }))
            }
        }
        DropdownMenuItem(onClick = { onSelect(AddOpeningHoursRequest.SelectWeekdays) }) {
            Text(stringResource(Res.string.quest_openingHours_add_weekdays))
        }
        DropdownMenuItem(onClick = { onSelect(AddOpeningHoursRequest.SelectOffWeekdays) }) {
            Text(stringResource(Res.string.quest_openingHours_add_off_days))
        }
        if (showAddMonths) {
            DropdownMenuItem(onClick = { onSelect(AddOpeningHoursRequest.SelectMonths) }) {
                Text(stringResource(Res.string.quest_openingHours_add_months))
            }
        }
    }
}

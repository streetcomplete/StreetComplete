package de.westnordost.streetcomplete.ui.common.opening_hours

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.DropdownMenu
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
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
import de.westnordost.streetcomplete.resources.quest_openingHours_add_weekdays
import de.westnordost.streetcomplete.ui.common.Button2
import de.westnordost.streetcomplete.ui.common.DropdownMenuItem
import org.jetbrains.compose.resources.stringResource

/** Button for adding opening hours to the end / bottom of the given opening hours. */
@Composable
fun AddOpeningHoursButton(
    openingHours: HierarchicOpeningHours,
    onChange: (HierarchicOpeningHours) -> Unit,
    timeMode: TimeMode,
    workweek: List<WeekdaysSelector>,
    modifier: Modifier = Modifier,
    locale: Locale = Locale.current,
    userLocale: Locale = Locale.current,
    content: @Composable (RowScope.() -> Unit),
) {
    var showAddDropdown by remember { mutableStateOf(false) }
    var addOpeningHoursRequest by remember { mutableStateOf<AddOpeningHoursRequest?>(null) }

    Box(modifier) {
        Button2(
            onClick = { showAddDropdown = true },
            content = content
        )
        SelectOpeningHoursDropdown(
            expanded = showAddDropdown,
            openingHours = openingHours,
            timeMode = timeMode,
            onDismissRequest = { showAddDropdown = false },
            onSelect = {
                showAddDropdown = false
                addOpeningHoursRequest = it
            }
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
    onDismissRequest: () -> Unit,
    onSelect: (AddOpeningHoursRequest) -> Unit,
    modifier: Modifier = Modifier,
) {
    val showAddTimes = openingHours.monthsList.lastOrNull()?.weekdaysList?.lastOrNull()?.times is Times
    val showAddMonths = openingHours.monthsList.any { it.selectors.isNotEmpty() }

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

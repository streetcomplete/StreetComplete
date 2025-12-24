package de.westnordost.streetcomplete.ui.common.opening_hours

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.dp
import de.westnordost.osm_opening_hours.model.Month
import de.westnordost.osm_opening_hours.model.WeekdaysSelector
import de.westnordost.streetcomplete.osm.opening_hours.Months
import de.westnordost.streetcomplete.osm.opening_hours.getMonths
import de.westnordost.streetcomplete.osm.opening_hours.toMonthsSelectors
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.ic_add_24
import de.westnordost.streetcomplete.resources.ic_delete_24
import de.westnordost.streetcomplete.resources.quest_openingHours_add_months
import de.westnordost.streetcomplete.resources.quest_openingHours_delete
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** Displays the given list of months */
@Composable
fun MonthsColumn(
    monthsList: List<Months>,
    onChange: (List<Months>) -> Unit,
    timeMode: TimeMode,
    modifier: Modifier = Modifier,
    initialWeekdaysSelectors: List<WeekdaysSelector> = emptyList(),
    locale: Locale = Locale.current,
    userLocale: Locale = Locale.current,
    enabled: Boolean = true,
    addEnabledWhenEmpty: Boolean = true,
) {
    var showDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
    ) {
        for ((index, months) in monthsList.withIndex()) {
            if (months.selectors.isNotEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    MonthsText(
                        months = months.selectors,
                        onChange = { newMonthsSelectorList ->
                            onChange(
                                monthsList.toMutableList().also {
                                    it[index] = it[index].copy(selectors = newMonthsSelectorList)
                                }
                            )
                        },
                        modifier = Modifier.weight(1f),
                        enabled = enabled,
                        locale = locale,
                        userLocale = userLocale,
                    )
                    if (enabled) {
                        IconButton(
                            onClick = {
                                onChange(monthsList.toMutableList().also { it.removeAt(index) })
                            }
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_delete_24),
                                contentDescription = stringResource(Res.string.quest_openingHours_delete)
                            )
                        }
                    } else {
                        Spacer(Modifier.size(48.dp))
                    }
                }
            }
            if (index > 0 || months.selectors.isNotEmpty()) {
                Divider()
            }
            WeekdaysColumn(
                weekdaysList = months.weekdaysList,
                onChange = { newWeekdaysList ->
                    onChange(
                        monthsList.toMutableList().also {
                            it[index] = it[index].copy(weekdaysList = newWeekdaysList)
                        }
                    )
                },
                timeMode = timeMode,
                modifier = Modifier.fillMaxWidth(),
                initialWeekdaysSelectors = initialWeekdaysSelectors,
                locale = locale,
                userLocale = userLocale,
                enabled = enabled,
            )
        }
        if (enabled && (addEnabledWhenEmpty || monthsList.any { it.selectors.isNotEmpty() })) {
            Divider()
            OutlinedButton(
                onClick = { showDialog = true }
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_add_24),
                    contentDescription = stringResource(Res.string.quest_openingHours_add_months)
                )
            }
        }
    }
    if (showDialog) {
        val unmentionedMonthsSelectors = remember(monthsList) {
            val mentioned = monthsList.flatMap { it.selectors }.getMonths()
            val unmentioned = (Month.entries.toSet() - mentioned)
            unmentioned.toMonthsSelectors()
        }

        MonthsOrDateSelectorSelectDialog(
            onDismissRequest = { showDialog = false },
            initialMonths = unmentionedMonthsSelectors,
            onSelected = { newMonthsSelectorList ->
                onChange(
                    monthsList.toMutableList().also {
                        it.add(Months(newMonthsSelectorList, listOf()))
                    }
                )
            },
            locale = locale,
            userLocale = userLocale,
        )
    }
}

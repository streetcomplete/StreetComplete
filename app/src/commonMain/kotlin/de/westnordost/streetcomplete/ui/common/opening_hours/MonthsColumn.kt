package de.westnordost.streetcomplete.ui.common.opening_hours

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.intl.Locale
import de.westnordost.streetcomplete.osm.opening_hours.Months

/** Displays the given list of months */
@Composable
fun MonthsColumn(
    monthsList: List<Months>,
    onChange: (List<Months>) -> Unit,
    modifier: Modifier = Modifier,
    locale: Locale = Locale.current,
    userLocale: Locale = Locale.current,
    enabled: Boolean = true,
) {
    val displayMonths = monthsList.size > 1 || monthsList.any { it.selectors.isNotEmpty() }
    Column(modifier = modifier) {
        for ((index, months) in monthsList.withIndex()) {
            if (displayMonths) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    MonthsText(
                        months = months.selectors,
                        onChange = { newMonthsSelectorList ->
                            val newMonthsList = monthsList.toMutableList()
                            newMonthsList[index] = newMonthsList[index].copy(selectors = newMonthsSelectorList)
                            onChange(newMonthsList)
                        },
                        modifier = Modifier.weight(1f),
                        enabled = enabled,
                        locale = locale,
                        userLocale = userLocale,
                    )
                    DeleteRowButton(
                        onClick = {
                            if (monthsList.size <= 1) {
                                // last entry? -> reset months
                                onChange(listOf(Months(emptyList(), emptyList())))
                            } else {
                                val newMonthsList = monthsList.toMutableList()
                                newMonthsList.removeAt(index)
                                onChange(newMonthsList)
                            }
                        },
                        visible = enabled
                    )
                }
            }
            if (index > 0 || displayMonths) {
                Divider()
            }
            WeekdaysColumn(
                weekdaysList = months.weekdaysList,
                onChange = { newWeekdaysList ->
                    val newMonthsList = monthsList.toMutableList()
                    newMonthsList[index] = newMonthsList[index].copy(weekdaysList = newWeekdaysList)
                    onChange(newMonthsList)
                },
                modifier = Modifier.fillMaxWidth(),
                locale = locale,
                userLocale = userLocale,
                enabled = enabled,
            )
        }
    }
}

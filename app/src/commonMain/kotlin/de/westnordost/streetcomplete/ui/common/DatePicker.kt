package de.westnordost.streetcomplete.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.util.ktx.getDisplayName
import de.westnordost.streetcomplete.util.locale.DateComponent
import de.westnordost.streetcomplete.util.locale.DateFormatElements
import de.westnordost.streetcomplete.util.locale.DateTimeTextSymbolStyle
import kotlin.time.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun rememberDatePickerState(
    initialDate: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
    years: IntRange = (initialDate.year - 10)..(initialDate.year + 10),
) = remember {
    DatePickerState(initialDate, years)
}

class DatePickerState(
    initialDate: LocalDate,
    years: IntRange,
) {
    internal val yearPickerState: WheelPickerState
    internal val monthPickerState: WheelPickerState
    internal val dayPickerState: WheelPickerState

    val selectableYears: List<Int> = years.toList()
    val selectableMonths: List<Month> = Month.entries

    val selectableDays: List<Int> by derivedStateOf {
        (1..lengthOfMonth(year, month)).toList()
    }
    val year: Int by derivedStateOf {
        selectableYears[yearPickerState.selectedItemIndex.coerceIn(selectableYears.indices)]
    }

    val month: Month by derivedStateOf {
        selectableMonths[monthPickerState.selectedItemIndex.coerceIn(selectableMonths.indices)]
    }
    val date: LocalDate by derivedStateOf {
        val day = (dayPickerState.selectedItemIndex + 1).coerceIn(1, selectableDays.size)
        LocalDate(year, month, day)
    }

    init {
        val yearIndex = selectableYears.indexOf(initialDate.year).coerceAtLeast(0)
        val monthIndex = initialDate.monthNumber - 1
        val dayIndex = initialDate.dayOfMonth - 1

        yearPickerState = WheelPickerState(yearIndex)
        monthPickerState = WheelPickerState(monthIndex)
        dayPickerState = WheelPickerState(dayIndex)
    }
}

@Composable
fun DatePicker(
    state: DatePickerState,
    dateFormatElements: DateFormatElements,
    modifier: Modifier = Modifier,
    locale: Locale? = null,
    visibleAdjacentItems: Int = 1,
) {
    // When month/year changes and the day list shrinks, clamp the day wheel
    val maxDay = state.selectableDays.size
    val currentDayIndex = state.dayPickerState.selectedItemIndex
    LaunchedEffect(maxDay) {
        if (currentDayIndex >= maxDay) {
            state.dayPickerState.animateScrollToItem(maxDay - 1)
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier,
    ) {
        for ((index, component) in dateFormatElements.order.withIndex()) {
            if (index > 0) {
                Text(dateFormatElements.separator)
            }
            when (component) {
                DateComponent.Year -> WheelPicker(
                    items = state.selectableYears,
                    state = state.yearPickerState,
                    content = { Text(it.toString()) },
                    visibleAdjacentItems = visibleAdjacentItems,
                )
                DateComponent.Month -> WheelPicker(
                    items = state.selectableMonths,
                    state = state.monthPickerState,
                    content = { Text(it.getDisplayName(DateTimeTextSymbolStyle.Short, locale)) },
                    visibleAdjacentItems = visibleAdjacentItems,
                )
                DateComponent.Day -> WheelPicker(
                    items = state.selectableDays,
                    state = state.dayPickerState,
                    content = { Text(it.toString().padStart(2, ' ')) },
                    visibleAdjacentItems = visibleAdjacentItems,
                )
            }
        }
    }
}

private fun lengthOfMonth(year: Int, month: Month): Int = when (month) {
    Month.JANUARY, Month.MARCH, Month.MAY, Month.JULY,
    Month.AUGUST, Month.OCTOBER, Month.DECEMBER -> 31
    Month.APRIL, Month.JUNE, Month.SEPTEMBER, Month.NOVEMBER -> 30
    Month.FEBRUARY -> if (isLeapYear(year)) 29 else 28
}

private fun isLeapYear(year: Int): Boolean =
    year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)

@Composable
@Preview
private fun DatePickerPreview() {
    val elements = DateFormatElements.of(Locale.current)
    val state = rememberDatePickerState()
    Column {
        DatePicker(state, elements, locale = Locale.current)
        Text(state.date.toString())
    }
}

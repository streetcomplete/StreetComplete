package de.westnordost.streetcomplete.ui.common.opening_hours

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.material.DropdownMenu
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import de.westnordost.osm_opening_hours.model.TimesSelector
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.ic_add_24
import de.westnordost.streetcomplete.resources.ic_delete_24
import de.westnordost.streetcomplete.resources.quest_openingHours_add_hours
import de.westnordost.streetcomplete.resources.quest_openingHours_delete
import de.westnordost.streetcomplete.ui.common.DropdownMenuItem
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** A column of time spans and times which can each be changed, deleted and new ones added.
 *
 *  E.g.
 *  ```
 *  08:00-12:00      [x]
 *  14:00-16:00      [x]
 *  20:00 until late [x]
 *  [+]
 *  ```
 *
 *  [timeMode] decides whether only time points or only time spans may be added. null if both are
 *  allowed. */
@Composable
fun TimesSelectorsColumn(
    times: List<TimesSelector>,
    onChangeTimes: (times: List<TimesSelector>) -> Unit,
    timeMode: TimeMode?,
    timeTextWidth: Dp,
    modifier: Modifier = Modifier,
    locale: Locale = Locale.current,
    enabled: Boolean = true,
) {
    var showDialog by remember { mutableStateOf(false) }
    var selectedTimeMode by remember { mutableStateOf<TimeMode?>(null) }

    Column(modifier) {
        for ((index, time) in times.withIndex()) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TimesSelectorText(
                    time = time,
                    onChangeTime = { newTime ->
                        onChangeTimes(times.toMutableList().also { it[index] = newTime })
                    },
                    modifier = Modifier.width(timeTextWidth),
                    locale = locale,
                    enabled = enabled,
                )
                if (enabled) {
                    IconButton(
                        onClick = {
                            onChangeTimes(times.toMutableList().also { it.removeAt(index) })
                        }
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_delete_24),
                            contentDescription = stringResource(Res.string.quest_openingHours_delete)
                        )
                    }
                }
            }
        }
        if (enabled) {
            OutlinedButton(
                onClick = { showDialog = true }
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_add_24),
                    contentDescription = timeMode?.titleResource?.let { stringResource(it) })
            }
        }
    }

    val currentTimeMode = timeMode ?: selectedTimeMode

    TimeModeDropdownMenu(
        expanded = showDialog && currentTimeMode == null,
        onDismissRequest = { showDialog = false },
        onSelect = { selectedTimeMode = it }
    )
    if (showDialog && currentTimeMode != null) {
        TimesSelectorDialog(
            onDismissRequest = {
                showDialog = false
                selectedTimeMode = null
            },
            mode = currentTimeMode,
            onSelect = { newTime ->
                onChangeTimes(times.toMutableList().also { it.add(newTime) })
            },
            locale = locale,
        )
    }
}

@Composable
private fun TimeModeDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    onSelect: (TimeMode) -> Unit,
    modifier: Modifier = Modifier
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier
    ) {
        for (timeMode in TimeMode.entries) {
            DropdownMenuItem(onClick = { onSelect(timeMode) }) {
                Text(stringResource(timeMode.titleResource))
            }
        }
    }
}

private val TimeMode.titleResource: StringResource get() = when (this) {
    TimeMode.Points -> Res.string.quest_openingHours_add_time
    TimeMode.Spans -> Res.string.quest_openingHours_add_hours
}

enum class TimeMode {
    /** May only add time points, e.g. "08:00" */
    Points,
    /** May only add time spans, e.g. "08:00-12:00" */
    Spans,
}

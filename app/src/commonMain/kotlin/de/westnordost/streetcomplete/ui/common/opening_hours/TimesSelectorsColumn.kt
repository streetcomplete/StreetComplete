package de.westnordost.streetcomplete.ui.common.opening_hours

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import de.westnordost.osm_opening_hours.model.TimesSelector
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.ic_add_24
import de.westnordost.streetcomplete.resources.ic_delete_24
import de.westnordost.streetcomplete.resources.quest_openingHours_add_hours
import de.westnordost.streetcomplete.resources.quest_openingHours_add_time
import de.westnordost.streetcomplete.resources.quest_openingHours_delete
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
 *  [timeMode] decides whether only time points or only time spans may be added. */
@Composable
fun TimesSelectorsColumn(
    times: List<TimesSelector>,
    onChange: (times: List<TimesSelector>) -> Unit,
    timeMode: TimeMode,
    timeTextWidth: Dp,
    modifier: Modifier = Modifier,
    locale: Locale = Locale.current,
    enabled: Boolean = true,
) {
    var showDialog by remember { mutableStateOf(false) }

    Column(modifier) {
        for ((index, time) in times.withIndex()) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TimesSelectorText(
                    time = time,
                    onChange = { newTime ->
                        onChange(times.toMutableList().also { it[index] = newTime })
                    },
                    modifier = Modifier.width(timeTextWidth),
                    locale = locale,
                    enabled = enabled,
                )
                if (enabled) {
                    IconButton(
                        onClick = {
                            onChange(times.toMutableList().also { it.removeAt(index) })
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
                    contentDescription = stringResource(timeMode.titleResource)
                )
            }
        }
    }

    if (showDialog) {
        TimesSelectorDialog(
            onDismissRequest = { showDialog = false },
            mode = timeMode,
            onSelect = { newTime ->
                onChange(times.toMutableList().also { it.add(newTime) })
            },
            locale = locale,
        )
    }
}

enum class TimeMode {
    /** May only add time points, e.g. "08:00" */
    Points,
    /** May only add time spans, e.g. "08:00-12:00" */
    Spans,
}

private val TimeMode.titleResource: StringResource get() = when (this) {
    TimeMode.Points -> Res.string.quest_openingHours_add_time
    TimeMode.Spans -> Res.string.quest_openingHours_add_hours
}


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
import de.westnordost.osm_opening_hours.model.ClockTime
import de.westnordost.osm_opening_hours.model.Time
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.ic_add_24
import de.westnordost.streetcomplete.resources.ic_delete_24
import de.westnordost.streetcomplete.resources.quest_collectionTimes_add_times
import de.westnordost.streetcomplete.resources.quest_openingHours_delete
import de.westnordost.streetcomplete.ui.ktx.conditional
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** A column of times which can each be changed, deleted and new ones added. */
@Composable
fun TimesColumn(
    times: List<Time>,
    onChangeTimes: (times: List<Time>) -> Unit,
    modifier: Modifier = Modifier,
    timeTextWidth: Dp? = null,
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
                TimeText(
                    time = time,
                    onChangeTime = { newTime ->
                        onChangeTimes(times.toMutableList().also { it[index] = newTime })
                    },
                    modifier = Modifier.conditional(timeTextWidth) { width(it) },
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
                    contentDescription = stringResource(Res.string.quest_collectionTimes_add_times)
                )
            }
        }
    }

    if (showDialog) {
        TimeSelectDialog(
            onDismissRequest = { showDialog = false },
            onSelect = { newTime ->
                onChangeTimes(times.toMutableList().also { it.add(newTime) })
            },
            initialTime = ClockTime(0,0),
            locale = locale,
        )
    }
}

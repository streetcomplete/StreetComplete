package de.westnordost.streetcomplete.ui.common.opening_hours

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.intl.Locale
import de.westnordost.osm_opening_hours.model.TimesSelector

/** A column of time spans and times which can each be changed and deleted.
 *
 *  E.g.
 *  ```
 *  08:00-12:00      [x]
 *  14:00-16:00      [x]
 *  20:00 until late [x]
 *  ```
 */
@Composable
fun TimesSelectorsColumn(
    times: List<TimesSelector>,
    onChange: (times: List<TimesSelector>) -> Unit,
    modifier: Modifier = Modifier,
    locale: Locale = Locale.current,
    enabled: Boolean = true,
) {
    Column(modifier) {
        for ((index, time) in times.withIndex()) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                TimesSelectorText(
                    time = time,
                    onChange = { newTime ->
                        val newTimes = times.toMutableList()
                        newTimes[index] = newTime
                        onChange(newTimes)
                    },
                    modifier = Modifier.weight(1f),
                    locale = locale,
                    enabled = enabled,
                )
                DeleteRowButton(
                    onClick = {
                        val newTimes = times.toMutableList()
                        newTimes.removeAt(index)
                        onChange(newTimes)
                    },
                    visible = enabled,
                )
            }
        }
    }
}

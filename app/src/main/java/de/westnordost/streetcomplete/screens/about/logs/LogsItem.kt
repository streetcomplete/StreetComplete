package de.westnordost.streetcomplete.screens.about.logs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.data.logs.LogLevel
import de.westnordost.streetcomplete.data.logs.LogMessage
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun LogsItem(
    log: LogMessage,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = log.toString(),
            modifier = Modifier.weight(1f),
            fontFamily = FontFamily.Monospace,
            color = log.level.color,
            style = MaterialTheme.typography.body2
        )
        val dateText = Instant
            .fromEpochMilliseconds(log.timestamp)
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .time
            .toString()
        Text(
            text = dateText,
            style = MaterialTheme.typography.caption
        )
    }
}

@Preview
@Composable
private fun LogsItemPreview() {
    LogsItem(LogMessage(
        level = LogLevel.DEBUG,
        tag = "Test",
        message = "Aspernatur rerum aperiam id error laborum possimus rerum",
        error = null,
        timestamp = 1716388402L
    ))
}

package de.westnordost.streetcomplete.quests.socket

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp

/** Displays EU labels for the given [socket] */
@Composable
fun ChargingStationSocketEuLabels(
    socket: ChargingStationSocket,
    modifier: Modifier = Modifier,
) {
    val euLabels = socket.euLabels
    if (euLabels.isNotEmpty()) {
        val textMeasurer = rememberTextMeasurer()
        Column(modifier = modifier) {
            for (label in euLabels) {
                Image(
                    painter = ChargingStationSocketEuLabelPainter(label, socket.hasCable, textMeasurer),
                    contentDescription = label.toString(),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

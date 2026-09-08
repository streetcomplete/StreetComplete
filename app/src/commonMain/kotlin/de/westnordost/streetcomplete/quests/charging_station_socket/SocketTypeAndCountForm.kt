package de.westnordost.streetcomplete.quests.charging_station_socket

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.StepperButton
import de.westnordost.streetcomplete.ui.theme.AppTheme
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

private const val MAX_SOCKET_COUNT = 50

@Composable
fun SocketTypeAndCountForm(
    socketTypes: List<SocketType>,
    counts: Map<SocketType, Int>,
    onCountsChanged: (Map<SocketType, Int>) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        socketTypes.forEach { type ->
            val count = counts[type] ?: 0
            SocketRow(
                type = type,
                count = count,
                onIncrease = {
                    if (count < MAX_SOCKET_COUNT) {
                        onCountsChanged(counts + (type to count + 1))
                    }
                },
                onDecrease = {
                    if (count > 0) {
                        onCountsChanged(counts + (type to count - 1))
                    }
                }
            )
        }
    }
}

@Composable
private fun SocketRow(
    type: SocketType,
    count: Int,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Image(
                painter = painterResource(type.icon),
                contentDescription = stringResource(type.title),
                modifier = Modifier.size(48.dp)
            )
            if (type.euLabels.isNotEmpty()) {
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    type.euLabels.forEach { label ->
                        EuConnectorLabel(
                            drawable = label,
                            needsLightBackdrop = type.hasBlackEuLabels
                        )
                    }
                }
            }
            Text(
                text = stringResource(type.title),
                style = MaterialTheme.typography.body2,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .border(
                        width = 1.dp,
                        color = if (count > 0) {
                            MaterialTheme.colors.primary
                        } else {
                            MaterialTheme.colors.onSurface.copy(alpha = 0.4f)
                        },
                        shape = RoundedCornerShape(6.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.h6
                )
            }
            StepperButton(
                onIncrease = onIncrease,
                onDecrease = onDecrease,
                increaseEnabled = count < MAX_SOCKET_COUNT,
                decreaseEnabled = count > 0
            )
        }
    }
}

@Composable
private fun EuConnectorLabel(
    drawable: DrawableResource,
    needsLightBackdrop: Boolean,
    modifier: Modifier = Modifier
) {
    val darkTheme = !MaterialTheme.colors.isLight
    val labelModifier = if (needsLightBackdrop && darkTheme) {
        modifier
            .background(Color(0xFFE8E8E8), RoundedCornerShape(4.dp))
            .padding(2.dp)
            .size(28.dp)
    } else {
        modifier.size(28.dp)
    }
    Image(
        painter = painterResource(drawable),
        contentDescription = null,
        modifier = labelModifier
    )
}

@PreviewLightDark
@Composable
private fun SocketTypeAndCountFormPreview() {
    AppTheme {
        var counts by remember {
            mutableStateOf(
                mapOf(
                    SocketType.TYPE2 to 2,
                    SocketType.TYPE2_CABLE to 0,
                    SocketType.TYPE2_COMBO to 1,
                    SocketType.CHADEMO to 0,
                    SocketType.DOMESTIC to 0
                )
            )
        }
        SocketTypeAndCountForm(
            socketTypes = SocketType.entries,
            counts = counts,
            onCountsChanged = { counts = it },
            modifier = Modifier.padding(8.dp)
        )
    }
}

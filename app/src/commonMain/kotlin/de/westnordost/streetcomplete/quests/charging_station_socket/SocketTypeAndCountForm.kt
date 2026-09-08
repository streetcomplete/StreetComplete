package de.westnordost.streetcomplete.quests.charging_station_socket

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.StepperButton
import de.westnordost.streetcomplete.ui.common.TextFieldStyle
import de.westnordost.streetcomplete.ui.common.input.DecimalInput
import de.westnordost.streetcomplete.ui.theme.AppTheme
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

private const val MAX_SOCKET_COUNT = 50

@Composable
fun SocketTypeAndCountForm(
    socketTypes: List<SocketType>,
    counts: Map<SocketType, Int?>,
    onCountsChanged: (Map<SocketType, Int?>) -> Unit,
    modifier: Modifier = Modifier,
    domesticIcons: List<DrawableResource> = emptyList(),
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        socketTypes.forEach { type ->
            val count = counts[type]
            val icons = if (type == SocketType.DOMESTIC && domesticIcons.isNotEmpty()) {
                domesticIcons
            } else {
                listOf(type.icon)
            }
            SocketRow(
                type = type,
                icons = icons,
                count = count,
                onCountChange = { onCountsChanged(counts + (type to it)) },
            )
        }
    }
}

@Composable
private fun SocketRow(
    type: SocketType,
    icons: List<DrawableResource>,
    count: Int?,
    onCountChange: (Int?) -> Unit,
) {
    val value = count ?: 0
    val iconSize = if (icons.size > 1) 36.dp else 48.dp
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
            icons.forEach { icon ->
                Image(
                    painter = painterResource(icon),
                    contentDescription = stringResource(type.title),
                    modifier = Modifier.size(iconSize)
                )
            }
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
            DecimalInput(
                value = count?.toDouble(),
                onValueChange = { raw ->
                    onCountChange(
                        raw?.toInt()?.coerceIn(0, MAX_SOCKET_COUNT)
                    )
                },
                // Enough for two digits (0..50) with Outlined TextField padding; same ballpark as BuildingLevelsForm
                modifier = Modifier.width(72.dp),
                maxIntegerDigits = 2,
                maxFractionDigits = 0,
                isUnsigned = true,
                style = TextFieldStyle.Outlined,
                textStyle = MaterialTheme.typography.h6.copy(textAlign = TextAlign.Center),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
            StepperButton(
                onIncrease = {
                    onCountChange((value + 1).coerceAtMost(MAX_SOCKET_COUNT))
                },
                onDecrease = {
                    onCountChange((value - 1).coerceAtLeast(0))
                },
                increaseEnabled = value < MAX_SOCKET_COUNT,
                decreaseEnabled = value > 0
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
            mutableStateOf<Map<SocketType, Int?>>(
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
            domesticIcons = listOf(
                SocketType.DOMESTIC.icon,
                Res.drawable.socket_domestic_typec,
            ),
            modifier = Modifier.padding(8.dp)
        )
    }
}

package de.westnordost.streetcomplete.quests.charging_station_socket

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.ui.common.StepperButton
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.material.Icon

@Composable
fun SocketTypeAndCountForm(
    counts: Map<SocketType, Int>,
    onCountsChanged: (Map<SocketType, Int>) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SocketType.selectableValues.forEach { type ->

            val count = counts[type] ?: 0

            SocketRow(
                type = type,
                count = count,
                onIncrease = {
                    val newCount = count + 1
                    if (newCount <= 50) {
                        val newMap = counts.toMutableMap()
                        newMap[type] = newCount
                        onCountsChanged(newMap)
                    }
                },
                onDecrease = {
                    val newMap = counts.toMutableMap()
                    if (count <= 1) {
                        newMap.remove(type)
                    } else {
                        newMap[type] = count - 1
                    }
                    onCountsChanged(newMap)
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
    Surface {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // LEFT: SOCKET ICON + EU HEX + LABEL
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {

                // Socket icon (bigger)
                Icon(
                    painter = painterResource(type.icon),
                    contentDescription = null,
                    modifier = Modifier.size(60.dp)
                )

                Spacer(Modifier.width(4.dp))

                // EU Hex (smaller)
                Icon(
                    painter = painterResource(type.euLabel),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp)
                )

                Spacer(Modifier.width(12.dp))

                Text(
                    text = stringResource(type.title),
                    style = MaterialTheme.typography.body1
                )
            }

            // RIGHT: COUNT + STEPPER
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                // Number with Border
                Box(
                    modifier = Modifier
                        .border(
                            width = 1.dp,
                            color = if (count > 0) MaterialTheme.colors.primary else Color.DarkGray,
                            shape = RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 20.dp),
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
                    increaseEnabled = count < 50,
                    decreaseEnabled = count > 0
                )
            }
        }
    }
}

package de.westnordost.streetcomplete.quests.charging_station_socket

import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.ui.common.StepperButton
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.material.Icon
import androidx.compose.foundation.layout.Arrangement

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
            SocketRow(
                type = type,
                count = counts[type] ?: 0,
                onIncrease = {
                    val newCount = (counts[type] ?: 0) + 1
                    if (newCount <= 50) {
                        val newMap = counts.toMutableMap()
                        newMap[type] = newCount
                        onCountsChanged(newMap)
                    }
                },
                onDecrease = {
                    val current = counts[type] ?: 0
                    val newMap = counts.toMutableMap()
                    if (current <= 1) {
                        newMap.remove(type)
                    } else {
                        newMap[type] = current - 1
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
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            // LEFT: ICON + EU HEX + LABEL
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    painter = painterResource(type.icon),
                    contentDescription = null
                )
                Icon(
                    painter = painterResource(type.euLabel),
                    contentDescription = null
                )
                Text(
                    text = stringResource(type.title),
                    style = MaterialTheme.typography.body1
                )
            }

            // RIGHT: COUNTER + STEPPER
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.h6
                )

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

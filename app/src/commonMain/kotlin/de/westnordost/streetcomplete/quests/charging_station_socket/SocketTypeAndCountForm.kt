package de.westnordost.streetcomplete.quests.charging_station_socket

import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun SocketTypeAndCountForm(
    counts: Map<SocketType, Int>,
    onIncrement: (SocketType) -> Unit,
    onDecrement: (SocketType) -> Unit,
    modifier: Modifier = Modifier
) {

    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SocketType.selectableValues.forEach { type ->

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {

                Row(verticalAlignment = Alignment.CenterVertically) {

                    Icon(
                        painter = painterResource(type.icon),
                        contentDescription = null,
                        modifier = Modifier.size(32.dp)
                    )

                    Spacer(Modifier.width(8.dp))

                    Icon(
                        painter = painterResource(type.euLabel),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )

                    Spacer(Modifier.width(12.dp))

                    Text(stringResource(type.title))
                }

                Row(verticalAlignment = Alignment.CenterVertically) {

                    IconButton(onClick = { onDecrement(type) }) {
                        Text("-")
                    }

                    Text(
                        text = (counts[type] ?: 0).toString(),
                        modifier = Modifier.width(32.dp),
                    )

                    IconButton(onClick = { onIncrement(type) }) {
                        Text("+")
                    }
                }
            }
        }
    }
}

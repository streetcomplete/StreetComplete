package de.westnordost.streetcomplete.quests.socket

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.resources.*
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun ChargingStationSocketsForm(
    sockets: Map<ChargingStationSocket, Int?>,
    onSocketsChanged: (Map<ChargingStationSocket, Int?>) -> Unit,
    modifier: Modifier = Modifier,
    domesticSocketIcon: DrawableResource? = null,
    showEuLabels: Boolean = false
) {
    val selectableSockets = remember(sockets.keys) { ChargingStationSocket.entries - sockets.keys }
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        for ((socket, count) in sockets) {
            val icon = if (socket == ChargingStationSocket.DOMESTIC && domesticSocketIcon != null) {
                domesticSocketIcon
            } else {
                socket.icon
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SocketIconWithLabel(
                    socket = socket,
                    icon = icon,
                    showEuLabels = showEuLabels,
                    modifier = Modifier.weight(1f)
                )
                SocketCountInput(
                    count = count,
                    onCountChange = { newCount ->
                        onSocketsChanged(sockets.toMutableMap().also { it[socket] = newCount })
                    },
                )
                DeleteChargingStationSocketButton(
                    onClick = {
                        onSocketsChanged(sockets.toMutableMap().also { it.remove(socket) })
                    }
                )
            }
        }
        ChargingStationSocketAddButton(
            selectableSockets = selectableSockets,
            onSelect = { socket ->
                onSocketsChanged(sockets.toMutableMap().also { it[socket] = null })
            },
            domesticSocketIcon = domesticSocketIcon,
            showEuLabels = showEuLabels,
        )
    }
}


@Composable
private fun SocketIconWithLabel(
    socket: ChargingStationSocket,
    icon: DrawableResource,
    showEuLabels: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
            )
            if (showEuLabels) {
                ChargingStationSocketEuLabels(socket)
            }
        }
        Text(
            text = stringResource(socket.title),
            style = MaterialTheme.typography.body2,
            textAlign = TextAlign.Center
        )
    }
}


@Composable
private fun DeleteChargingStationSocketButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier,
    ) {
        Icon(
            painter = painterResource(Res.drawable.ic_delete_24),
            contentDescription = stringResource(Res.string.quest_openingHours_delete)
        )
    }
}


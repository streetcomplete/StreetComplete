package de.westnordost.streetcomplete.quests.socket

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.DropdownMenu
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.Button2
import de.westnordost.streetcomplete.ui.common.DropdownMenuItem
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** Dropdown button to select a [ChargingStationSocket] from a list of sockets */
@Composable
fun ChargingStationSocketAddButton(
    selectableSockets: Set<ChargingStationSocket>,
    commonSockets: List<ChargingStationSocket>,
    onSelect: (ChargingStationSocket) -> Unit,
    showEuLabels: Boolean,
    domesticSocketIcon: DrawableResource?,
    modifier: Modifier = Modifier,
) {
    var showAddDropdown by remember { mutableStateOf(false) }
    var showAllSockets by remember { mutableStateOf(false) }

    val shownSockets = remember(showAllSockets, commonSockets, selectableSockets) {
        val sockets = if (showAllSockets) {
            ChargingStationSocket.entries.toMutableList().apply {
                // common sockets come first
                removeAll(commonSockets)
                addAll(0, commonSockets)
            }
        } else {
            commonSockets
        }
        sockets.filter { it in selectableSockets }
    }

    Box(modifier) {
        Button2(onClick = {
            showAddDropdown = true
            showAllSockets = false
        }) {
            Text(stringResource(Res.string.quest_charging_station_add_socket))
        }
        DropdownMenu(
            expanded = showAddDropdown,
            onDismissRequest = { showAddDropdown = false },
        ) {
            for (socket in shownSockets) {
                DropdownMenuItem(onClick = { showAddDropdown = false; onSelect(socket) }) {
                    ChargingStationSocketDropdownMenuItemContent(
                        socket = socket,
                        domesticSocketIcon = domesticSocketIcon,
                        showEuLabels = showEuLabels,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
            if (!showAllSockets) {
                DropdownMenuItem(onClick = { showAllSockets = true }) {
                    Text(stringResource(Res.string.quest_charging_station_other_socket))
                }
            }
        }
    }
}

@Composable
private fun ChargingStationSocketDropdownMenuItemContent(
    socket: ChargingStationSocket,
    domesticSocketIcon: DrawableResource?,
    showEuLabels: Boolean,
    modifier: Modifier = Modifier,
) {
    val icon = if (socket == ChargingStationSocket.DOMESTIC && domesticSocketIcon != null) {
        domesticSocketIcon
    } else {
        socket.icon
    }
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
            )
        }
        if (showEuLabels) {
            ChargingStationSocketEuLabels(socket)
        }
        Text(
            text = stringResource(socket.title),
            style = MaterialTheme.typography.body2,
        )
    }
}

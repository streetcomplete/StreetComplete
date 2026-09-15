package de.westnordost.streetcomplete.quests.socket

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import de.westnordost.streetcomplete.ui.common.DropdownButton
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** Dropdown button to select a [ChargingStationSocket] from a list of sockets */
@Composable
fun ChargingStationSocketAddButton(
    selectableSockets: List<ChargingStationSocket>,
    onSelect: (ChargingStationSocket) -> Unit,
    showEuLabels: Boolean,
    domesticSocketIcon: DrawableResource?,
    modifier: Modifier = Modifier,
) {
    var showAddDropdown by remember { mutableStateOf(false) }

    DropdownButton(
        items = selectableSockets,
        onSelectedItem = onSelect,
        modifier = modifier,
        enabled = selectableSockets.isNotEmpty(),
        itemContent = { socket ->
            val icon = if (socket == ChargingStationSocket.DOMESTIC && domesticSocketIcon != null) {
                domesticSocketIcon
            } else {
                socket.icon
            }
            Row(
                modifier = Modifier.padding(vertical = 2.dp),
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
    ) {
        Text(stringResource(Res.string.quest_charging_station_add_socket))
    }
}

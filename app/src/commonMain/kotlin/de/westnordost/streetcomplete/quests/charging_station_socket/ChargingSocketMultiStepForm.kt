package de.westnordost.streetcomplete.quests.charging_station_socket

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cheonjaeung.compose.grid.SimpleGridCells
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.quest_multiselect_hint
import de.westnordost.streetcomplete.ui.common.item_select.ItemsSelectGrid
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun ChargingSocketMultiStepForm(
    selectedTypes: List<SocketType>,
    socketCounts: List<SocketCount>,
    onTypeSelected: (SocketType) -> Unit,
    onTypeDeselected: (SocketType) -> Unit,
    onCountChanged: (SocketType, Int) -> Unit
) {

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

        Text(stringResource(Res.string.quest_multiselect_hint))

        ItemsSelectGrid(
            columns = SimpleGridCells.Fixed(3),
            items = SocketType.selectableValues,
            selectedItems = selectedTypes.toSet(),
            onSelect = { type, selected ->
                if (selected) onTypeSelected(type)
                else onTypeDeselected(type)
            }
        ) {
            Icon(painterResource(it.icon), null)
            Text(stringResource(it.title))
        }

        selectedTypes.forEach { type ->
            var value by remember { mutableStateOf(1) }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(stringResource(type.title))
                TextField(
                    value = value.toString(),
                    onValueChange = {
                        val int = it.toIntOrNull()
                        if (int != null) {
                            value = int
                            onCountChanged(type, int)
                        }
                    },
                    modifier = Modifier.width(80.dp)
                )
            }
        }
    }
}

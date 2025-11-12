package de.westnordost.streetcomplete.quests.max_weight

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cheonjaeung.compose.grid.SimpleGridCells
import de.westnordost.streetcomplete.data.meta.WeightMeasurementUnit
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.ic_delete_24
import de.westnordost.streetcomplete.resources.quest_maxweight_add_sign
import de.westnordost.streetcomplete.ui.common.Button2
import de.westnordost.streetcomplete.ui.common.dialogs.SimpleItemSelectDialog
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** Form to (first) select a number of maxweight [signs] (via MaxWeightSignForm) */
@Composable
fun MaxWeightForm(
    signs: List<MaxWeight>,
    onSignAdded: (MaxWeight) -> Unit,
    onSignRemoved: (index: Int) -> Unit,
    onSignChanged: (index: Int, MaxWeight) -> Unit,
    countryCode: String,
    selectableUnits: List<WeightMeasurementUnit>,
    modifier: Modifier = Modifier,
) {
    var showSelectionDialog by remember { mutableStateOf(false) }

    val selectableMaxWeightTypes = MaxWeightType.entries.filter { type ->
        type !in signs.map { it.type } &&
        type.getIcon(countryCode) != null
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        signs.forEachIndexed { index, sign ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                MaxWeightSignForm(
                    type = sign.type,
                    weight = sign.weight,
                    onWeightChange = { onSignChanged(index, signs[index].copy(weight = it)) },
                    countryCode = countryCode,
                    selectableUnits = selectableUnits,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { onSignRemoved(index) }) {
                    Icon(painterResource(Res.drawable.ic_delete_24), null)
                }
            }
        }
        if (selectableMaxWeightTypes.isNotEmpty()) {
            Button2(onClick = { showSelectionDialog = true }) {
                Text(stringResource(Res.string.quest_maxweight_add_sign))
            }
        }
    }

    if (showSelectionDialog) {
        SimpleItemSelectDialog(
            onDismissRequest = { showSelectionDialog = false },
            columns = SimpleGridCells.Fixed(2),
            items = selectableMaxWeightTypes,
            onSelected = { onSignAdded(MaxWeight(it, null)) },
            itemContent = {
                val icon = it.getIcon(countryCode)
                if (icon != null) Image(painterResource(icon), null)
            }
        )
    }
}

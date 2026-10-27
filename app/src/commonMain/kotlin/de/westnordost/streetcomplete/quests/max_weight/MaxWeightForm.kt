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
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.dp
import com.cheonjaeung.compose.grid.SimpleGridCells
import de.westnordost.streetcomplete.data.meta.WeightMeasurementUnit
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.Button2
import de.westnordost.streetcomplete.ui.common.dialogs.SimpleItemSelectDialog
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** Form to (first) select a number of maxweight [signs] (via MaxWeightSignForm) */
@Composable
fun MaxWeightForm(
    signs: List<MaxWeightRestriction>,
    onSignAdded: (MaxWeightRestriction) -> Unit,
    onSignRemoved: (index: Int) -> Unit,
    onSignChanged: (index: Int, MaxWeightRestriction) -> Unit,
    locale: Locale,
    selectableUnits: List<WeightMeasurementUnit>,
    modifier: Modifier = Modifier,
) {
    var showSelectionDialog by remember { mutableStateOf(false) }

    val countryCode = locale.region

    val selectableMaxWeightRestrictions = remember(countryCode, signs) {
        MaxWeightRestriction.getSelectableEntries(countryCode)
            .filter { sign -> signs.none { sign::class == it::class } }
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
                MaxWeightRestrictionSignForm(
                    value = sign,
                    onValueChange = { onSignChanged(index, it) },
                    locale = locale,
                    selectableUnits = selectableUnits,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { onSignRemoved(index) }) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_delete_24),
                        contentDescription = stringResource(Res.string.quest_maxweight_remove_sign)
                    )
                }
            }
        }
        if (selectableMaxWeightRestrictions.isNotEmpty()) {
            Button2(onClick = { showSelectionDialog = true }) {
                Text(stringResource(Res.string.quest_maxweight_add_sign))
            }
        }
    }

    if (showSelectionDialog) {
        SimpleItemSelectDialog(
            onDismissRequest = { showSelectionDialog = false },
            columns = SimpleGridCells.Fixed(2),
            items = selectableMaxWeightRestrictions,
            onSelected = { onSignAdded(it) },
            itemContent = { Image(painterResource(it.getIcon(countryCode)), null) }
        )
    }
}

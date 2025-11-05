package de.westnordost.streetcomplete.quests.max_weight

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.cheonjaeung.compose.grid.SimpleGridCells
import de.westnordost.streetcomplete.data.meta.WeightMeasurementUnit
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.resources.quest_maxweight_remove_sign
import de.westnordost.streetcomplete.resources.quest_maxweight_select_sign
import de.westnordost.streetcomplete.ui.common.Button2
import de.westnordost.streetcomplete.ui.common.dialogs.SimpleItemSelectDialog
import de.westnordost.streetcomplete.ui.util.FallDownTransitionSpec
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** Form to (first) select a sign [type] and then input [weight] (via MaxWeightSignForm) */
@Composable
fun MaxWeightForm(
    type: MaxWeightType?,
    onSelectType: (MaxWeightType?) -> Unit,
    weight: Weight?,
    onChangeWeight: (Weight?) -> Unit,
    countryCode: String,
    selectableUnits: List<WeightMeasurementUnit>,
    modifier: Modifier = Modifier,
    selectedTypes: List<MaxWeightType>
) {
    var showSelectionDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Box {
            AnimatedContent(
                targetState = type,
                transitionSpec = FallDownTransitionSpec,
                contentAlignment = Alignment.Center,
            ) { type ->
                if (type != null) {
                    MaxWeightSignForm(
                        type = type,
                        weight = weight,
                        onWeightChange = onChangeWeight,
                        countryCode = countryCode,
                        selectableUnits = selectableUnits,
                    )
                } else {
                    showSelectionDialog = true
                }
            }
        }
    }

    if (showSelectionDialog) {
        SimpleItemSelectDialog(
            onDismissRequest = { showSelectionDialog = false },
            columns = SimpleGridCells.Fixed(2),
            items = MaxWeightType.entries.filter { it !in selectedTypes && it.getIcon(countryCode) != null },
            onSelected = { onSelectType(it); showSelectionDialog = false },
            itemContent = {
                val icon = it.getIcon(countryCode)
                if (icon != null) Image(painterResource(icon), null)
            }
        )
    }
}

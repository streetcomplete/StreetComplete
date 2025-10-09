package de.westnordost.streetcomplete.quests.max_weight

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.cheonjaeung.compose.grid.SimpleGridCells
import de.westnordost.streetcomplete.data.meta.WeightMeasurementUnit
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.quest_maxweight_select_sign
import de.westnordost.streetcomplete.ui.common.Button2
import de.westnordost.streetcomplete.ui.common.dialogs.SimpleItemSelectDialog
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
) {
    var showSelectionDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = type,
            transitionSpec = {
                (fadeIn(tween(220)) + scaleIn(tween(220), initialScale = 2f))
                    .togetherWith(fadeOut(tween(90)))
            },
            contentAlignment = Alignment.Center,
        ) { type ->
            if (type == null) {
                Button2(
                    onClick = { showSelectionDialog = true },
                ) {
                    Text(stringResource(Res.string.quest_maxweight_select_sign))
                }
            } else {
                MaxWeightSignForm(
                    type = type,
                    weight = weight,
                    onWeightChange = onChangeWeight,
                    countryCode = countryCode,
                    selectableUnits = selectableUnits,
                )
            }
        }
    }

    if (showSelectionDialog) {
        SimpleItemSelectDialog(
            onDismissRequest = { showSelectionDialog = false },
            columns = SimpleGridCells.Fixed(2),
            items = MaxWeightType.entries.filter { it.getIcon(countryCode) != null },
            onSelected = { onSelectType(it) },
            itemContent = {
                val icon = it.getIcon(countryCode)
                if (icon != null) Image(painterResource(icon), null)
            }
        )
    }
}

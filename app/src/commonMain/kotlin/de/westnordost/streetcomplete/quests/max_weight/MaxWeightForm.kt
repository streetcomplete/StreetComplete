package de.westnordost.streetcomplete.quests.max_weight

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cheonjaeung.compose.grid.SimpleGridCells
import de.westnordost.streetcomplete.data.meta.WeightMeasurementUnit
import de.westnordost.streetcomplete.quests.max_weight.getIcon
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.ic_delete_24
import de.westnordost.streetcomplete.resources.quest_maxweight_add_sign
import de.westnordost.streetcomplete.resources.quest_maxweight_select_sign
import de.westnordost.streetcomplete.ui.common.Button2
import de.westnordost.streetcomplete.ui.common.dialogs.SimpleItemSelectDialog
import de.westnordost.streetcomplete.ui.util.FallDownTransitionSpec
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** Form to (first) select a sign [type] and then input [weight] (via MaxWeightSignForm) */
@Composable
fun MaxWeightForm(
    types: SnapshotStateList<MutableState<MaxWeightType>>,
    weights: SnapshotStateList<MutableState<Weight?>>,
    countryCode: String,
    selectableUnits: List<WeightMeasurementUnit>,
    modifier: Modifier = Modifier,
    checkIsFormComplete: () -> Unit,
) {
    var showSelectionDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Box {
            AnimatedContent(
                targetState = types.size,
                transitionSpec = FallDownTransitionSpec,
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    types.forEachIndexed { i, _ ->
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                                MaxWeightSignForm(
                                    type = types[i].value,
                                    weight = weights[i].value,
                                    onWeightChange = {
                                        weights[i].value = it
                                        checkIsFormComplete()
                                    },
                                    countryCode = countryCode,
                                    selectableUnits = selectableUnits,
                                )
                            }
                            IconButton(
                                onClick = {
                                    types.removeAt(i)
                                    weights.removeAt(i)
                                    checkIsFormComplete() },
                                modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)
                            ) {
                                Icon(painterResource(Res.drawable.ic_delete_24), null)
                            }
                        }
                        Spacer(Modifier.height(6.dp))
                    }

                    if (types.size < maxSupportedSigns(countryCode)) {
                        Button2(
                            onClick = {
                                showSelectionDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            if (types.isEmpty())
                                Text(stringResource(Res.string.quest_maxweight_select_sign))
                            else
                                Text(stringResource(Res.string.quest_maxweight_add_sign))
                        }
                    }

                    if (showSelectionDialog) {
                        SimpleItemSelectDialog(
                            onDismissRequest = { showSelectionDialog = false },
                            columns = SimpleGridCells.Fixed(2),
                            items = MaxWeightType.entries.filter { it !in types.map { it.value } as List<MaxWeightType> && it.getIcon(countryCode) != null },
                            onSelected = {
                                types.add(mutableStateOf(it))
                                weights.add(mutableStateOf(null))
                                checkIsFormComplete()
                                showSelectionDialog = false
                            },
                            itemContent = {
                                val icon = it.getIcon(countryCode)
                                if (icon != null) Image(painterResource(icon), null)
                            }
                        )
                    }
                }
            }
        }
    }
}

private fun maxSupportedSigns(countryCode: String): Int = when (countryCode) {
    "AU", "CA", "US", "DE" -> 5
    else -> 4
}

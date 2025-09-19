package de.westnordost.streetcomplete.quests.max_weight

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import de.westnordost.streetcomplete.data.meta.WeightMeasurementUnit
import de.westnordost.streetcomplete.quests.max_weight.MaxWeightType.MAX_AXLE_LOAD
import de.westnordost.streetcomplete.quests.max_weight.MaxWeightType.MAX_TANDEM_AXLE_LOAD
import de.westnordost.streetcomplete.quests.max_weight.MaxWeightType.MAX_WEIGHT
import de.westnordost.streetcomplete.quests.max_weight.MaxWeightType.MAX_WEIGHT_RATING
import de.westnordost.streetcomplete.quests.max_weight.MaxWeightType.MAX_WEIGHT_RATING_HGV
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.maxweight_sign_axleload
import de.westnordost.streetcomplete.resources.maxweight_sign_axleload_mutcd
import de.westnordost.streetcomplete.resources.maxweight_sign_axleload_yellow
import de.westnordost.streetcomplete.resources.maxweight_sign_bogieweight
import de.westnordost.streetcomplete.resources.maxweight_sign_bogieweight_mutcd
import de.westnordost.streetcomplete.resources.maxweight_sign_bogieweight_yellow
import de.westnordost.streetcomplete.resources.maxweight_sign_weight
import de.westnordost.streetcomplete.resources.maxweight_sign_weight_mutcd
import de.westnordost.streetcomplete.resources.maxweight_sign_weight_yellow
import de.westnordost.streetcomplete.resources.maxweight_sign_weightrating_de
import de.westnordost.streetcomplete.resources.maxweight_sign_weightrating_gb
import de.westnordost.streetcomplete.resources.maxweight_sign_weightrating_hgv
import de.westnordost.streetcomplete.resources.maxweight_sign_weightrating_hgv_de
import de.westnordost.streetcomplete.resources.maxweight_sign_weightrating_hgv_mutcd
import de.westnordost.streetcomplete.resources.maxweight_sign_weightrating_hgv_yellow
import de.westnordost.streetcomplete.resources.maxweight_sign_weightrating_mutcd
import de.westnordost.streetcomplete.resources.quest_maxweight_select_sign
import de.westnordost.streetcomplete.ui.common.Button2
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.stringResource

// TODO
@Composable
fun MaxWeightForm(
    type: MaxWeightType?,
    value: Double?,
    unit: WeightMeasurementUnit,
    onValueChange: (Double?) -> Unit,
    onUnitChange: (WeightMeasurementUnit) -> Unit,
    countryCode: String,
    selectableUnits: List<WeightMeasurementUnit>,
    modifier: Modifier = Modifier,
) {
    var showSelectionDialog by remember { mutableStateOf(false) }

    if (type == null) {
        Button2(
            onClick = { showSelectionDialog = true },
            modifier = modifier,
        ) {
            Text(stringResource(Res.string.quest_maxweight_select_sign))
        }
    } else {
        MaxWeightSignForm(
            type = type,
            value = value,
            unit = unit,
            onValueChange = onValueChange,
            onUnitChange = onUnitChange,
            countryCode = countryCode,
            selectableUnits = selectableUnits,
            modifier = modifier,
        )
    }

    if (showSelectionDialog) {
        TODO()
    }
}

private fun MaxWeightType.getIcon(countryCode: String): DrawableResource? = when (this) {
    MAX_WEIGHT -> when (countryCode) {
        "AU", "CA", "US" -> Res.drawable.maxweight_sign_weight_mutcd
        "FI", "IS", "SE" -> Res.drawable.maxweight_sign_weight_yellow
        else ->             Res.drawable.maxweight_sign_weight
    }
    MAX_WEIGHT_RATING -> when (countryCode) {
        "AU", "CA", "US" -> Res.drawable.maxweight_sign_weightrating_mutcd
        "DE" ->             Res.drawable.maxweight_sign_weightrating_de
        "GB" ->             Res.drawable.maxweight_sign_weightrating_gb
        else ->             null
    }
    MAX_WEIGHT_RATING_HGV -> when (countryCode) {
        "AU", "CA", "US" -> Res.drawable.maxweight_sign_weightrating_hgv_mutcd
        "FI", "IS", "SE"->  Res.drawable.maxweight_sign_weightrating_hgv_yellow
        "DE" ->             Res.drawable.maxweight_sign_weightrating_hgv_de
        "GB" ->             null
        else ->             Res.drawable.maxweight_sign_weightrating_hgv
    }
    MAX_AXLE_LOAD -> when (countryCode) {
        "AU", "CA", "US" -> Res.drawable.maxweight_sign_axleload_mutcd
        "FI", "IS", "SE" -> Res.drawable.maxweight_sign_axleload_yellow
        else ->             Res.drawable.maxweight_sign_axleload
    }
    MAX_TANDEM_AXLE_LOAD -> when (countryCode) {
        "AU", "CA", "US" -> Res.drawable.maxweight_sign_bogieweight_mutcd
        "FI", "IS", "SE" -> Res.drawable.maxweight_sign_bogieweight_yellow
        else ->             Res.drawable.maxweight_sign_bogieweight
    }
}

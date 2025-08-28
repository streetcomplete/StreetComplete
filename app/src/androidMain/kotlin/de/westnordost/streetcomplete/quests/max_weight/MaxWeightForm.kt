package de.westnordost.streetcomplete.quests.max_weight

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.WeightMeasurementUnit
import de.westnordost.streetcomplete.quests.max_weight.MaxWeightType.MAX_AXLE_LOAD
import de.westnordost.streetcomplete.quests.max_weight.MaxWeightType.MAX_TANDEM_AXLE_LOAD
import de.westnordost.streetcomplete.quests.max_weight.MaxWeightType.MAX_WEIGHT
import de.westnordost.streetcomplete.quests.max_weight.MaxWeightType.MAX_WEIGHT_RATING
import de.westnordost.streetcomplete.quests.max_weight.MaxWeightType.MAX_WEIGHT_RATING_HGV
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.sign_maxaxleload
import de.westnordost.streetcomplete.resources.sign_maxaxleload_mutcd
import de.westnordost.streetcomplete.resources.sign_maxaxleload_yellow
import de.westnordost.streetcomplete.resources.sign_maxbogieweight
import de.westnordost.streetcomplete.resources.sign_maxbogieweight_mutcd
import de.westnordost.streetcomplete.resources.sign_maxbogieweight_yellow
import de.westnordost.streetcomplete.resources.sign_maxweight
import de.westnordost.streetcomplete.resources.sign_maxweight_mutcd
import de.westnordost.streetcomplete.resources.sign_maxweight_yellow
import de.westnordost.streetcomplete.resources.sign_maxweightrating_de
import de.westnordost.streetcomplete.resources.sign_maxweightrating_gb
import de.westnordost.streetcomplete.resources.sign_maxweightrating_hgv
import de.westnordost.streetcomplete.resources.sign_maxweightrating_hgv_de
import de.westnordost.streetcomplete.resources.sign_maxweightrating_hgv_mutcd
import de.westnordost.streetcomplete.resources.sign_maxweightrating_hgv_yellow
import de.westnordost.streetcomplete.resources.sign_maxweightrating_mutcd
import de.westnordost.streetcomplete.ui.common.Button2
import org.jetbrains.compose.resources.DrawableResource

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
            Text(stringResource(R.string.quest_maxweight_select_sign))
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
        "AU", "CA", "US" -> Res.drawable.sign_maxweight_mutcd
        "FI", "IS", "SE" -> Res.drawable.sign_maxweight_yellow
        else ->             Res.drawable.sign_maxweight
    }
    MAX_WEIGHT_RATING -> when (countryCode) {
        "AU", "CA", "US" -> Res.drawable.sign_maxweightrating_mutcd
        "DE" ->             Res.drawable.sign_maxweightrating_de
        "GB" ->             Res.drawable.sign_maxweightrating_gb
        else ->             null
    }
    MAX_WEIGHT_RATING_HGV -> when (countryCode) {
        "AU", "CA", "US" -> Res.drawable.sign_maxweightrating_hgv_mutcd
        "FI", "IS", "SE"->  Res.drawable.sign_maxweightrating_hgv_yellow
        "DE" ->             Res.drawable.sign_maxweightrating_hgv_de
        "GB" ->             null
        else ->             Res.drawable.sign_maxweightrating_hgv
    }
    MAX_AXLE_LOAD -> when (countryCode) {
        "AU", "CA", "US" -> Res.drawable.sign_maxaxleload_mutcd
        "FI", "IS", "SE" -> Res.drawable.sign_maxaxleload_yellow
        else ->             Res.drawable.sign_maxaxleload
    }
    MAX_TANDEM_AXLE_LOAD -> when (countryCode) {
        "AU", "CA", "US" -> Res.drawable.sign_maxbogieweight_mutcd
        "FI", "IS", "SE" -> Res.drawable.sign_maxbogieweight_yellow
        else ->             Res.drawable.sign_maxbogieweight
    }
}

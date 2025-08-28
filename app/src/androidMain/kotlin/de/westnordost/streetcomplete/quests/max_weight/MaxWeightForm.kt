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
import de.westnordost.streetcomplete.ui.common.Button2

/** Two-step form: First, select sign [type], second, specify [weight] */
@Composable
fun MaxWeightForm(
    type: MaxWeightType?,
    weight: Weight?,
    countryCode: String,
    modifier: Modifier, // TODO
) {
    var showSelectionDialog by remember { mutableStateOf(false) }

    when (type) {
        null -> {
            Button2(onClick = { showSelectionDialog = true }) {
                Text(stringResource(R.string.quest_maxweight_select_sign))
            }
        }
        MaxWeightType.MAX_WEIGHT -> when (countryCode) {
            "AU", "CA", "US" -> TODO()
            "FI", "IS", "SE" -> TODO()
            else ->             TODO()
        }
        MaxWeightType.MAX_WEIGHT_RATING -> when (countryCode) {
            "AU", "CA", "US" -> TODO()
            "GB" ->             TODO()
        }
        MaxWeightType.MAX_WEIGHT_RATING_HGV -> when (countryCode) {
            "FI", "IS", "SE"->  TODO()
            "DE" ->             TODO()
            else ->             TODO()
        }
        MaxWeightType.MAX_AXLE_LOAD -> when (countryCode) {
            "AU", "CA", "US" -> TODO()
            "FI", "IS", "SE" -> TODO()
            else ->             TODO()
        }
        MaxWeightType.MAX_TANDEM_AXLE_LOAD -> when (countryCode) {
            "AU", "CA", "US" -> TODO()
            "FI", "IS", "SE" -> TODO()
            else ->             TODO()
        }
    }

    if (showSelectionDialog) {
        TODO()
    }
}

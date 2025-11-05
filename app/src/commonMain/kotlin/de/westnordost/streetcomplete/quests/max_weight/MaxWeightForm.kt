package de.westnordost.streetcomplete.quests.max_weight

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import de.westnordost.streetcomplete.data.meta.WeightMeasurementUnit
import de.westnordost.streetcomplete.ui.util.FallDownTransitionSpec

/** Form to (first) select a sign [type] and then input [weight] (via MaxWeightSignForm) */
@Composable
fun MaxWeightForm(
    type: MaxWeightType,
    weight: Weight?,
    onChangeWeight: (Weight?) -> Unit,
    countryCode: String,
    selectableUnits: List<WeightMeasurementUnit>,
    modifier: Modifier = Modifier
) {
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
}

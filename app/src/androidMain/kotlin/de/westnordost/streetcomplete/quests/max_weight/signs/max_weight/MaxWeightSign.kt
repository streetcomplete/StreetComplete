package de.westnordost.streetcomplete.quests.max_weight.signs.max_weight

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import de.westnordost.streetcomplete.quests.max_weight.MaxWeightSignCountyType

@Composable
fun MaxWeightSign(signType: MaxWeightSignCountyType, modifier: Modifier = Modifier) = when (signType) {
    MaxWeightSignCountyType.US_TYPE -> MaxWeightUSSign(modifier)
    MaxWeightSignCountyType.FINISH_TYPE -> MaxWeightFinnishSign(modifier)
    else -> MaxWeightGeneralSign(modifier)
}

@Composable
@Preview(showBackground = true,  name = "Max Weight (US)")
fun MaxWeightSignPreview() {
    MaxWeightSign(MaxWeightSignCountyType.US_TYPE, Modifier)
}

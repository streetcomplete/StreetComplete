package de.westnordost.streetcomplete.quests.max_weight.signs.max_tandem_load

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import de.westnordost.streetcomplete.quests.max_weight.MaxWeightSignCountyType

@Composable
fun MaxTandemAxleLoadSign(signType: MaxWeightSignCountyType, modifier: Modifier = Modifier) {
    // return the appropriate sign for country here
}

@Preview(showBackground = true,  name = "Max Tandem Axle Load (US)")
@Composable
fun MaxTandemAxleLoadSignPreview() {
    MaxTandemAxleLoadSign(MaxWeightSignCountyType.US_TYPE)
}

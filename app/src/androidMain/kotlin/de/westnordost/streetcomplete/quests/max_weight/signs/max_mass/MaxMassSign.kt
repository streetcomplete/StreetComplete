package de.westnordost.streetcomplete.quests.max_weight.signs.max_mass

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import de.westnordost.streetcomplete.quests.max_weight.MaxWeightSignCountyType

@Composable
fun MaxMassSign(signType: MaxWeightSignCountyType, modifier: Modifier = Modifier) {
    // return the appropriate sign for country here
}

@Preview(showBackground = true,  name = "Max Mass (US)")
@Composable
fun MaxMassSignPreview() {
    MaxMassSign(MaxWeightSignCountyType.US_TYPE)
}


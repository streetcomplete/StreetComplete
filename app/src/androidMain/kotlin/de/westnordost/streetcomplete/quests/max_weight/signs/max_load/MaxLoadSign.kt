package de.westnordost.streetcomplete.quests.max_weight.signs.max_load

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import de.westnordost.streetcomplete.quests.max_weight.MaxWeightSignCountyType

@Composable
fun MaxLoadSign(signType: MaxWeightSignCountyType, modifier: Modifier = Modifier) {
    // return the appropriate sign for country here
}

@Preview(showBackground = true,  name = "Max Load (US)")
@Composable
fun MaxLoadSignPreview() {
    MaxLoadSign(MaxWeightSignCountyType.US_TYPE)
}

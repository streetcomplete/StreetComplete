package de.westnordost.streetcomplete.quests.max_weight.signs

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import de.westnordost.streetcomplete.quests.max_weight.MaxWeightSign
import de.westnordost.streetcomplete.quests.max_weight.MaxWeightSignCountyType
import de.westnordost.streetcomplete.quests.max_weight.signs.max_load.MaxLoadSign
import de.westnordost.streetcomplete.quests.max_weight.signs.max_mass.MaxMassSign
import de.westnordost.streetcomplete.quests.max_weight.signs.max_tandem_load.MaxTandemAxleLoadSign
import de.westnordost.streetcomplete.quests.max_weight.signs.max_weight.MaxWeightSign

@Composable
fun GeneralMaxWeightSign(maxWeightSign: MaxWeightSign, signType: MaxWeightSignCountyType, modifier: Modifier = Modifier) {
    return when (maxWeightSign) {
        MaxWeightSign.MAX_WEIGHT -> MaxWeightSign(signType, modifier)
        MaxWeightSign.MAX_GROSS_VEHICLE_MASS -> MaxMassSign(signType, modifier)
        MaxWeightSign.MAX_AXLE_LOAD -> MaxLoadSign(signType, modifier)
        MaxWeightSign.MAX_TANDEM_AXLE_LOAD -> MaxTandemAxleLoadSign(signType, modifier)
    }
}

@Preview(showBackground = true,  name = "Max Weight (US)")
@Composable
fun GeneralMaxWeightSignPreview() {
    GeneralMaxWeightSign(MaxWeightSign.MAX_WEIGHT, signType = MaxWeightSignCountyType.US_TYPE)
}

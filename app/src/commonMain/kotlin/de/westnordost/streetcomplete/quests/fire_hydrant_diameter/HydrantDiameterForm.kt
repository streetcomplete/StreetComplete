package de.westnordost.streetcomplete.quests.fire_hydrant_diameter

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import de.westnordost.streetcomplete.quests.fire_hydrant_diameter.FireHydrantDiameter.Unit.Inch
import de.westnordost.streetcomplete.quests.fire_hydrant_diameter.FireHydrantDiameter.Unit.Millimeter
import de.westnordost.streetcomplete.ui.common.AutoFitTextFieldFontSize
import de.westnordost.streetcomplete.ui.common.TextField2
import de.westnordost.streetcomplete.ui.theme.extraLargeInput

/** Form to input the hydrant diameter as written on the sign */
@Composable
fun HydrantDiameterForm(
    value: FireHydrantDiameter?,
    onValueChange: (FireHydrantDiameter?) -> Unit,
    countryCode: String?,
    modifier: Modifier = Modifier,
) {
    HydrantDiameterSign(
        countryCode = countryCode,
        modifier = modifier,
    ) {
        val stringValue = value?.value.toString().orEmpty()
        ProvideTextStyle(MaterialTheme.typography.extraLargeInput) {
            AutoFitTextFieldFontSize(stringValue) {
                TextField2(
                    value = stringValue,
                    onValueChange = { value ->
                        if (value.isEmpty() || value.all { it.isDigit() } && value.length <= 4) {
                            val diameter = value.toIntOrNull()
                            if (diameter == null) {
                                onValueChange(null)
                            } else {
                                val unit = getHydrantDiameterUnit(diameter, countryCode)
                                onValueChange(FireHydrantDiameter(diameter, unit))
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
            }
        }
    }
}

// We can determine the unit automatically based on country, so it does not need to appear as an
// input field in the form: In all countries for which this quest is enabled so far, millimeter is
// used.
// The exception is United Kingdom, which sometimes uses millimeter, sometimes inches. Fortunately,
// this can be determined automatically, too
private fun getHydrantDiameterUnit(diameter: Int, countryCode: String?): FireHydrantDiameter.Unit {
    return if (countryCode == "GB" && diameter <= 25) Inch else Millimeter
}

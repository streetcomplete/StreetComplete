package de.westnordost.streetcomplete.quests.fire_hydrant_diameter

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import de.westnordost.streetcomplete.ui.common.TextField2
import de.westnordost.streetcomplete.ui.theme.extraLargeInput

/** Form to input the hydrant diameter as written on the sign */
@Composable
fun HydrantDiameterForm(
    value: Int?,
    onValueChange: (Int?) -> Unit,
    countryCode: String?,
    modifier: Modifier = Modifier,
) {
    HydrantDiameterSign(
        countryCode = countryCode,
        modifier = modifier,
    ) {
        TextField2(
            value = value?.toString().orEmpty(),
            onValueChange = { value ->
                if (value.isEmpty() || value.all { it.isDigit() } && value.length <= 4) {
                    onValueChange(value.toIntOrNull())
                }
            },
            singleLine = true,
            textStyle = MaterialTheme.typography.extraLargeInput.copy(textAlign = TextAlign.Center),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            autoFitFontSize = true
        )
    }
}

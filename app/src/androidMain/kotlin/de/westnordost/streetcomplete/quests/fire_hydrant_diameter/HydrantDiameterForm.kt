package de.westnordost.streetcomplete.quests.fire_hydrant_diameter

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import de.westnordost.streetcomplete.ui.common.TextField2
import de.westnordost.streetcomplete.ui.common.TextFieldStyle
import de.westnordost.streetcomplete.ui.theme.extraLargeInput

/** Form to input the hydrant diameter as written on the sign */
@Composable
fun HydrantDiameterForm(
    value: Int?,
    onValueChange: (Int?) -> Unit,
    countryCode: String?,
    modifier: Modifier = Modifier,
) {
    var valueState by remember { mutableStateOf(value) }
    HydrantDiameterSign(
        countryCode = countryCode,
        modifier = modifier,
    ) {
        TextField2(
            value = valueState?.toString().orEmpty(),
            onValueChange = { value ->
                if (value.isEmpty() || value.all { it.isDigit() }) {
                    valueState = value.toIntOrNull()
                }
                onValueChange(valueState)
            },
            style = TextFieldStyle.Filled,
            singleLine = true,
            textStyle = MaterialTheme.typography.extraLargeInput.copy(textAlign = TextAlign.Center),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            autoFitFontSize = true
        )
    }
}
// TODO max length
// TODO monospace??

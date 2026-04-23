package de.westnordost.streetcomplete.quests.address

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.osm.address.parseHouseNumbers
import de.westnordost.streetcomplete.ui.ktx.pxToDp
import de.westnordost.streetcomplete.ui.theme.extraLargeInput
import androidx.compose.ui.tooling.preview.Preview

/** Input field for inputting housenumbers.
 *
 *  Shows buttons to step up the housenumber (12c -> 13 / 12d) and to step down the
 *  housenumber (12c -> 11 / 12b).
 *
 *  When the user didn't input anything yet, will show the [suggestion] for which those buttons
 *  work, too.
 *  */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HouseNumberInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    suggestion: String? = null,
) {
    var houseNumberInputHeightPx by remember { mutableIntStateOf(0) }

    Row(modifier = modifier) {
        AnAddressNumberInput(
            value = value,
            onValueChange = { if (it != value) onValueChange(it) },
            suggestion = suggestion,
            modifier = Modifier
                .weight(1f)
                .onSizeChanged { houseNumberInputHeightPx = it.height }
        )
        val useSuggestion = value.isEmpty() && !suggestion.isNullOrEmpty()
        val valueOrSuggestion = if (useSuggestion) suggestion else value

        val houseNumbers = remember(valueOrSuggestion) { parseHouseNumbers(valueOrSuggestion) }

        val stepperModifier = Modifier
            .width(48.dp)
            .height(houseNumberInputHeightPx.pxToDp())

        HouseNumberStepperButton(
            value = houseNumbers,
            onValueChange = { onValueChange(it.toString()) },
            modifier = stepperModifier
        )
        HouseNumberMinorStepperButton(
            value = houseNumbers,
            onValueChange = { onValueChange(it.toString()) },
            modifier = stepperModifier
        )
    }
}

@Composable @Preview
private fun HouseNumberInputPreview() {
    var value by remember { mutableStateOf("") }
    ProvideTextStyle(MaterialTheme.typography.extraLargeInput) {
        HouseNumberInput(
            value = value,
            suggestion = "12c",
            onValueChange = { value = it },
            modifier = Modifier.width(224.dp)
        )
    }
}

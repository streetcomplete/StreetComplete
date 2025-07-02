package de.westnordost.streetcomplete.quests.max_height

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.LengthUnit
import de.westnordost.streetcomplete.osm.Length
import de.westnordost.streetcomplete.osm.unit
import de.westnordost.streetcomplete.ui.common.LengthFeetInchesInput
import de.westnordost.streetcomplete.ui.common.LengthMetersInput
import de.westnordost.streetcomplete.ui.common.SelectButton
import de.westnordost.streetcomplete.ui.common.TextFieldStyle
import de.westnordost.streetcomplete.ui.theme.extraLargeInput
import de.westnordost.streetcomplete.ui.theme.largeInput

/** Displays a form to input the max height, as specified on the sign. For clarity and fun, the
 *  input fields are shown on a sign background that resembles a maxheight sign in the given
 *  [countryCode] */
@Composable
fun MaxHeightForm(
    length: Length?,
    onChange: (Length?) -> Unit,
    selectableUnits: List<LengthUnit>,
    countryCode: String?,
    modifier: Modifier = Modifier,
) {
    var selectedUnit by remember { mutableStateOf(length?.unit ?: selectableUnits[0]) }
    // only change the unit when the new `length` has a different unit than we have currently
    val lengthUnitHasChanged = length != null && length.unit != selectedUnit
    if (lengthUnitHasChanged) {
        selectedUnit = length.unit
    }
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (selectableUnits.size > 1) {
            SelectButton(
                items = selectableUnits,
                selectedItem = selectedUnit,
                onSelectedItem = { unit ->
                    selectedUnit = unit
                    onChange(null)
                }
            ) {
                Text(it.toString())
            }
        }
        MaxHeightSign(countryCode = countryCode) {
            when (selectedUnit) {
                LengthUnit.METER -> {
                    ProvideTextStyle(MaterialTheme.typography.extraLargeInput.copy(fontWeight = FontWeight.Bold)) {
                        LengthMetersInput(
                            length = length as? Length.Meters,
                            onChange = onChange,
                            maxMeterDigits = Pair(3, 2),
                            style = TextFieldStyle.Outlined,
                            autoFitFontSize = true,
                        )
                    }
                }
                LengthUnit.FOOT_AND_INCH -> {
                    ProvideTextStyle(MaterialTheme.typography.largeInput.copy(fontWeight = FontWeight.Bold)) {
                        LengthFeetInchesInput(
                            length = length as? Length.FeetAndInches,
                            onChange = onChange,
                            maxFeetDigits = 3,
                            style = TextFieldStyle.Outlined,
                            autoFitFontSize = true,
                        )
                    }
                }
            }
        }
    }
}

@Preview @Composable
fun MaxHeightFormPreview() {
    var length: Length? by remember { mutableStateOf(Length.Meters(10.00)) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        MaxHeightForm(
            length = length,
            onChange = { length = it },
            selectableUnits = listOf(LengthUnit.FOOT_AND_INCH, LengthUnit.METER),
            countryCode = "US",
        )
        Text(length?.toOsmValue().orEmpty(), Modifier.padding(16.dp))
    }
}

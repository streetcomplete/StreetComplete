package de.westnordost.streetcomplete.quests

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.Divider
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.LengthUnit
import de.westnordost.streetcomplete.osm.Length
import de.westnordost.streetcomplete.osm.unit
import de.westnordost.streetcomplete.ui.common.LengthFeetInchesInput
import de.westnordost.streetcomplete.ui.common.LengthMetersInput
import de.westnordost.streetcomplete.ui.common.MeasurementIcon
import de.westnordost.streetcomplete.ui.common.SelectButton
import de.westnordost.streetcomplete.ui.common.TextFieldStyle
import de.westnordost.streetcomplete.ui.theme.largeInput

/** Displays a form to input and/or measure the length, in meter or feet+inch */
@Composable
fun LengthForm(
    length: Length?,
    onChange: (Length?) -> Unit,
    selectableUnits: List<LengthUnit>,
    showMeasureButton: Boolean,
    onClickMeasure: (LengthUnit) -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedUnit by remember { mutableStateOf(length?.unit ?: selectableUnits[0]) }
    // only change the unit when the new `length` has a different unit that we have currently
    val lengthUnitHasChanged = length != null && length.unit != selectedUnit
    if (lengthUnitHasChanged) {
        selectedUnit = length.unit
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
    ) {
        Row(
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
                    },
                ) {
                    Text(it.toString())
                }
            }

            ProvideTextStyle(MaterialTheme.typography.largeInput) {
                when (selectedUnit) {
                    LengthUnit.METER -> {
                        LengthMetersInput(
                            length = length as? Length.Meters,
                            onChange = onChange,
                            maxMeterDigits = Pair(3, 2),
                            modifier = Modifier.weight(1f),
                            style = TextFieldStyle.Outlined,
                            autoFitFontSize = true,
                        )
                    }
                    LengthUnit.FOOT_AND_INCH -> {
                        LengthFeetInchesInput(
                            length = length as? Length.FeetAndInches,
                            onChange = onChange,
                            maxFeetDigits = 3,
                            modifier = Modifier.weight(1f),
                            style = TextFieldStyle.Outlined,
                            autoFitFontSize = true,
                        )
                    }
                }
            }

        }

        if (showMeasureButton) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Divider(Modifier.weight(1f))
                Text(stringResource(R.string.or), Modifier.padding(horizontal = 16.dp))
                Divider(Modifier.weight(1f))
            }

            MeasureButton(onClick = { onClickMeasure(selectedUnit) })
        }
    }
}

@Composable
private fun MeasureButton(onClick: () -> Unit) {
    Button (
        onClick = onClick,
    ) {
        MeasurementIcon()
        Spacer(Modifier.width(8.dp))
        Text(stringResource(R.string.ar_measure))
    }
}

@Composable
@Preview
private fun LengthFormPreview() {
    var length: Length? by remember { mutableStateOf(Length.Meters(10.00)) }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        LengthForm(
            length = length,
            onChange = { length = it },
            selectableUnits = LengthUnit.entries.toList().reversed(),
            showMeasureButton = true,
            onClickMeasure = { length = Length.Meters(99.99) },
        )
        Text(length?.toOsmValue().orEmpty(), Modifier.padding(16.dp))
    }
}

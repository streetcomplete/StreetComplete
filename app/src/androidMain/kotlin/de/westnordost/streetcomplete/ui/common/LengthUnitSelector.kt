package de.westnordost.streetcomplete.ui.common

import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import de.westnordost.streetcomplete.data.meta.LengthUnit

@Composable
fun LengthUnitSelector(
    selectableUnits: List<LengthUnit>,
    selectedUnit: LengthUnit,
    onUnitChanged: (LengthUnit) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedUnitIndex = remember { mutableIntStateOf(selectableUnits.indexOf(selectedUnit)) }

    Button(
        onClick = {
            selectedUnitIndex.intValue =
                (selectedUnitIndex.intValue + 1) % selectableUnits.size
            onUnitChanged(selectableUnits[selectedUnitIndex.intValue])
        },
        enabled = selectableUnits.size > 1,
        modifier = modifier
    ) {
        Text(selectableUnits[selectedUnitIndex.intValue].toString())
    }
}

@Composable
@Preview
private fun LengthInputSelectorPreview() {

    LengthUnitSelector(
        selectableUnits = listOf(LengthUnit.METER, LengthUnit.FOOT_AND_INCH),
        selectedUnit = LengthUnit.METER,
        onUnitChanged = { }
    )
}

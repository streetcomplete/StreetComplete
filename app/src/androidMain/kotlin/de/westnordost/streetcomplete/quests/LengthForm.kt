package de.westnordost.streetcomplete.quests

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.FilledIconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.data.meta.LengthUnit
import de.westnordost.streetcomplete.osm.Length
import de.westnordost.streetcomplete.ui.common.FootInchAppearance
import de.westnordost.streetcomplete.ui.common.LengthInput
import de.westnordost.streetcomplete.ui.common.LengthUnitSelector
import de.westnordost.streetcomplete.ui.common.MeasurementIcon

@Composable
fun LengthForm(
    currentLength: Length?,
    syncLength: Boolean,
    onLengthChanged: (Length?) -> Unit,
    maxFeetDigits: Int,
    maxMeterDigits: Pair<Int, Int>,
    selectableUnits: List<LengthUnit>,
    showMeasureButton: Boolean,
    takeMeasurementClick: (LengthUnit) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedUnit = remember { mutableStateOf(selectableUnits[0]) }
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End
    )
    {
        CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.h4) {
            LengthInput(
                selectedUnit = selectedUnit.value,
                currentLength = currentLength,
                syncLength = syncLength,
                onLengthChanged = onLengthChanged,
                maxFeetDigits = maxFeetDigits,
                maxMeterDigits = maxMeterDigits,
                footInchAppearance = FootInchAppearance.PRIME
            )

            LengthUnitSelector(
                selectableUnits = selectableUnits,
                selectedUnit = selectedUnit.value,
                onUnitChanged = { selectedUnit.value = it },
                modifier = Modifier.padding(5.dp)
            )

            Spacer(Modifier.width(25.dp))

            if (showMeasureButton) {
                MeasureButton(onClick = { takeMeasurementClick(selectedUnit.value) })
            }
        }
    }
}

@Composable
private fun MeasureButton(onClick: () -> Unit) {
    FilledIconButton(onClick = { onClick() }) {
        MeasurementIcon()
    }
}

@Composable
@Preview(showBackground = true)
private fun LengthFormPreview() {
    val length = remember { mutableStateOf<Length?>(Length.Meters(10.00)) }

    LengthForm(
        currentLength = length.value,
        syncLength = true,
        onLengthChanged = { length.value = it },
        maxFeetDigits = 3,
        maxMeterDigits = Pair(2, 2),
        selectableUnits = listOf(LengthUnit.METER),
        showMeasureButton = true,
        takeMeasurementClick = {},
    )
}


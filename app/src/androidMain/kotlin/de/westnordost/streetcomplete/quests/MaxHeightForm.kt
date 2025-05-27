package de.westnordost.streetcomplete.quests

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.LengthUnit
import de.westnordost.streetcomplete.osm.Length
import de.westnordost.streetcomplete.ui.common.LengthInput
import de.westnordost.streetcomplete.ui.common.LengthUnitSelector

@Composable
fun MaxHeightForm(
    selectableUnits: List<LengthUnit>,
    onLengthChanged: (Length?) -> Unit,
    maxFeetDigits: Int,
    maxMeterDigits: Pair<Int, Int>,
    modifier: Modifier = Modifier

    ) {

    val selectedUnit = remember { mutableStateOf(selectableUnits[0]) }


    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    )
    {
        Box {
            Image(
                painter = painterResource(R.drawable.background_maxheight_sign),
                contentDescription = "Maximum Height Sign"
            )

            LengthInput(
                selectedUnit = selectedUnit.value,
                currentLength = null, // sync in this direction not required here
                syncLength = false, // sync in this direction not required here
                onLengthChanged = {onLengthChanged(it)},
                maxFeetDigits = maxFeetDigits,
                maxMeterDigits = maxMeterDigits,
                modifier = Modifier.align(Alignment.Center),
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent
                ),
                textStyle = MaterialTheme.typography.h5
            )
        }
        LengthUnitSelector(selectableUnits, selectedUnit.value, { selectedUnit.value = it })

    }
}

@Preview(showBackground = true)
@Composable
fun MaxHeightFormPreview() {
    MaxHeightForm(
        selectableUnits = listOf(LengthUnit.METER),
        onLengthChanged = {},
        maxFeetDigits = 2,
        maxMeterDigits = Pair(2, 2)
    )
}

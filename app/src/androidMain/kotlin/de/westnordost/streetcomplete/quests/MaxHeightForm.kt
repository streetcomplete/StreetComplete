package de.westnordost.streetcomplete.quests

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.meta.IncompleteCountryInfo
import de.westnordost.streetcomplete.data.meta.LengthUnit
import de.westnordost.streetcomplete.osm.Length
import de.westnordost.streetcomplete.quests.max_height.MaxHeightSign
import de.westnordost.streetcomplete.ui.common.LengthUnitSelector

@Composable
fun MaxHeightForm(
    selectableUnits: List<LengthUnit>,
    onLengthChanged: (Length?) -> Unit,
    maxFeetDigits: Int,
    maxMeterDigits: Pair<Int, Int>,
    countryInfo: CountryInfo,
    modifier: Modifier = Modifier,
) {

    val selectedUnit = remember { mutableStateOf(selectableUnits[0]) }


    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    )
    {
        CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.h4) {
            MaxHeightSign(
                countryInfo,
                selectedUnit.value,
                maxFeetDigits,
                maxMeterDigits,
                onLengthChanged
            )
        }
        LengthUnitSelector(
            selectableUnits,
            selectedUnit.value,
            { selectedUnit.value = it },
            modifier = Modifier.padding(10.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MaxHeightFormPreview() {

    val countryInfo = CountryInfo(
        infos = listOf(
            IncompleteCountryInfo(
                countryCode = "LI",
            )
        )
    )


    MaxHeightForm(
        selectableUnits = listOf(LengthUnit.METER, LengthUnit.FOOT_AND_INCH),
        onLengthChanged = {},
        maxFeetDigits = 2,
        maxMeterDigits = Pair(2, 2),
        countryInfo = countryInfo
    )
}

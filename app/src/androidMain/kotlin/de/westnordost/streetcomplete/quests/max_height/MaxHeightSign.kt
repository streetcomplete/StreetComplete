package de.westnordost.streetcomplete.quests.max_height

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.meta.IncompleteCountryInfo
import de.westnordost.streetcomplete.data.meta.LengthUnit
import de.westnordost.streetcomplete.osm.Length
import de.westnordost.streetcomplete.ui.common.FootInchAppearance
import de.westnordost.streetcomplete.ui.common.LengthInput

@Composable
fun MaxHeightSign(
    countryInfo: CountryInfo,
    selectedUnit: LengthUnit,
    maxFeetDigits: Int,
    maxMeterDigits: Pair<Int, Int>,
    onLengthChanged: (Length?) -> Unit,
) {
    when (countryInfo.countryCode) {
        "AU", "NZ", "US", "CA" -> MaxHeightSignMutcd {
            LengthInput(
                selectedUnit = selectedUnit,
                currentLength = null,
                syncLength = false,
                onLengthChanged = onLengthChanged,
                maxFeetDigits = maxFeetDigits,
                maxMeterDigits = maxMeterDigits,
                footInchAppearance = FootInchAppearance.UPPERCASE_ABBREVIATION
            )
        }

        "FI", "IS", "SE" -> MaxHeightSignNordic {
            LengthInput(
                selectedUnit = selectedUnit,
                currentLength = null,
                syncLength = false,
                onLengthChanged = onLengthChanged,
                maxFeetDigits = maxFeetDigits,
                maxMeterDigits = maxMeterDigits,
                footInchAppearance = FootInchAppearance.PRIME
            )
        }

        else -> MaxHeightSignDefault {
            LengthInput(
                selectedUnit = selectedUnit,
                currentLength = null,
                syncLength = false,
                onLengthChanged = onLengthChanged,
                maxFeetDigits = maxFeetDigits,
                maxMeterDigits = maxMeterDigits,
                footInchAppearance = FootInchAppearance.PRIME
            )
        }
    }
}

@Composable
@Preview(showBackground = true)
fun MaxHeightSignPreview() {
    val countryInfo = CountryInfo(
        infos = listOf(
            IncompleteCountryInfo(
                countryCode = "DE",
            )
        )
    )
    MaxHeightSign(countryInfo, LengthUnit.METER, 2, Pair(2, 2), {})
}

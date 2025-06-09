package de.westnordost.streetcomplete.quests.max_height

import androidx.compose.foundation.layout.Column
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.LengthUnit
import de.westnordost.streetcomplete.osm.Length
import de.westnordost.streetcomplete.ui.common.FootInchAppearance
import de.westnordost.streetcomplete.ui.common.LengthInput
import de.westnordost.streetcomplete.ui.common.RectangularSign
import de.westnordost.streetcomplete.ui.theme.TrafficYellow

@Composable
fun MaxHeightSign(
    countryCode: String?,
    selectedUnit: LengthUnit,
    maxFeetDigits: Int,
    maxMeterDigits: Pair<Int, Int>,
    modifier: Modifier = Modifier,
    onLengthChanged: (Length?) -> Unit,
) {
    when (countryCode) {
        "AU", "NZ", "US", "CA" -> {
            RectangularSign(
                modifier = modifier,
                color = TrafficYellow,
            ) {
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
        }
        else -> {
            MaxHeightSignRound(
                resourceId = when (countryCode) {
                    "FI", "IS", "SE" -> R.drawable.background_maxheight_sign_yellow
                    else ->             R.drawable.background_maxheight_sign
                },
                modifier = modifier
            ) {
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
}

@Composable
@Preview(showBackground = true)
fun MaxHeightSignPreview() {
    CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.h4) {
        Column {
            MaxHeightSign("DE", LengthUnit.METER, 2, Pair(2, 2)) {}
            MaxHeightSign("FI", LengthUnit.METER, 2, Pair(2, 2)) {}
            MaxHeightSign("GB", LengthUnit.FOOT_AND_INCH, 2, Pair(2, 2)) {}
            MaxHeightSign("US", LengthUnit.FOOT_AND_INCH, 2, Pair(2, 2)) {}
        }
    }
}

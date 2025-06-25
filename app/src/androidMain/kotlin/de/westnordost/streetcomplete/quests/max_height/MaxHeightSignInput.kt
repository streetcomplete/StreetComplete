package de.westnordost.streetcomplete.quests.max_height

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.LengthUnit
import de.westnordost.streetcomplete.osm.Length
import de.westnordost.streetcomplete.ui.common.FootInchAppearance
import de.westnordost.streetcomplete.ui.common.LengthInput
import de.westnordost.streetcomplete.ui.common.RectangularSign
import de.westnordost.streetcomplete.ui.theme.TrafficSignColor

/** Input field for a max height, styled to mimic a physical sign in the country with the given
 *  [countryCode]. */
@Composable
fun MaxHeightSignInput(
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
                color = TrafficSignColor.Yellow,
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
            val bg = painterResource(when (countryCode) {
                "FI", "IS", "SE" -> R.drawable.background_maxheight_sign_yellow
                else ->             R.drawable.background_maxheight_sign
            })
            Box(
                modifier = modifier
                    .size(240.dp)
                    .drawBehind { with(bg) { draw(size) } },
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    LengthInput(
                        selectedUnit = selectedUnit,
                        currentLength = null,
                        syncLength = false,
                        onLengthChanged = onLengthChanged,
                        maxFeetDigits = maxFeetDigits,
                        maxMeterDigits = maxMeterDigits,
                        footInchAppearance = FootInchAppearance.PRIME
                    )
                    if (selectedUnit == LengthUnit.METER) {
                        Text("m")
                    }
                }
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
fun MaxHeightSignPreview() {
    Column {
        MaxHeightSignInput("DE", LengthUnit.METER, 2, Pair(2, 2)) {}
        MaxHeightSignInput("FI", LengthUnit.METER, 2, Pair(2, 2)) {}
        MaxHeightSignInput("GB", LengthUnit.FOOT_AND_INCH, 2, Pair(2, 2)) {}
        MaxHeightSignInput("US", LengthUnit.FOOT_AND_INCH, 2, Pair(2, 2)) {}
    }
}

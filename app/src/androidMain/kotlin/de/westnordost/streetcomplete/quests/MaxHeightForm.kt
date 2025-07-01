package de.westnordost.streetcomplete.quests

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.data.meta.LengthUnit
import de.westnordost.streetcomplete.osm.Length
import de.westnordost.streetcomplete.osm.unit
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.background_maxheight_sign
import de.westnordost.streetcomplete.resources.background_maxheight_sign_mutcd
import de.westnordost.streetcomplete.resources.background_maxheight_sign_yellow
import de.westnordost.streetcomplete.ui.common.LengthFeetInchesInput
import de.westnordost.streetcomplete.ui.common.LengthMetersInput
import de.westnordost.streetcomplete.ui.common.Selector
import de.westnordost.streetcomplete.ui.common.TextFieldStyle
import de.westnordost.streetcomplete.ui.theme.extraLargeInput
import de.westnordost.streetcomplete.ui.theme.largeInput
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

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
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        val signBackground = painterResource(getMaxHeightSignDrawable(countryCode))
        Box(
            modifier = Modifier
                .size(256.dp)
                .drawBehind { with(signBackground) { draw(size) } }
                .padding(48.dp),
            contentAlignment = Alignment.Center,
        ) {
            when (selectedUnit) {
                LengthUnit.METER -> {
                    CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.extraLargeInput) {
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
                    CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.largeInput) {
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
        if (selectableUnits.size > 1) {
            Selector(
                items = selectableUnits,
                selectedItem = selectedUnit,
                onSelectedItem = { unit ->
                    selectedUnit = unit
                    onChange(null)
                },
                modifier = Modifier.width(112.dp),
                style = TextFieldStyle.Outlined
            )
        }
    }
}

private fun getMaxHeightSignDrawable(countryCode: String?): DrawableResource =
    when (countryCode) {
        "FI", "IS", "SE" -> {
            Res.drawable.background_maxheight_sign_yellow
        }
        // source: https://commons.wikimedia.org/wiki/File:Road_Warning_signs_around_the_World.svg
        "AR", "AU", "BR", "BZ", "CA", "CL", "CO", "CR", "DO", "EC", "GT", "GY", "HN",
        "ID", "IE", "JM", "JP", "LK", "LR", "MM", "MX", "MY", "NI", "NZ", "PA", "PE",
        "PG","SV", "TH", "TL", "US", "UY", "VE" -> {
            Res.drawable.background_maxheight_sign_mutcd
        }
        else -> {
            Res.drawable.background_maxheight_sign
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

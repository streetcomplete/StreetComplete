package de.westnordost.streetcomplete.quests.max_speed

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.osm.maxspeed.Speed
import de.westnordost.streetcomplete.quests.max_speed.MaxSpeedSign.Type.*
import de.westnordost.streetcomplete.ui.common.AutoFitTextFieldFontSize
import de.westnordost.streetcomplete.ui.common.ButtonStyle
import de.westnordost.streetcomplete.ui.common.DropdownButton
import de.westnordost.streetcomplete.ui.common.TextField2

/** Input for a max speed input type (normal, advisory, zone) */
@Composable
fun MaxSpeedInput(
    type: MaxSpeedSign.Type,
    speed: Speed,
    onChangeSpeed: (Speed) -> Unit,
    countryInfo: CountryInfo,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        val content = @Composable {
            SpeedValueInput(
                value = speed.value,
                onValueChange = { onChangeSpeed(speed.copy(value = it)) }
            )
        }
        when (type) {
            NORMAL -> MaxSpeedSign(countryCode = countryInfo.countryCode) { content() }
            ADVISORY -> AdvisorySpeedSign(countryInfo = countryInfo) { content() }
            ZONE -> MaxSpeedZoneSign(countryInfo = countryInfo) { content() }
        }
        if (countryInfo.speedUnits.size > 1) {
            DropdownButton(
                items = countryInfo.speedUnits,
                onSelectedItem = { onChangeSpeed(speed.copy(unit = it)) },
                selectedItem = speed.unit,
                style = ButtonStyle.Outlined,
                itemContent = { Text(it.displayString,) },
                modifier = Modifier.align(Alignment.CenterEnd)
            )
        }
    }
}

@Composable
private fun SpeedValueInput(
    value: Int?,
    onValueChange: (Int?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val stringValue = value?.toString().orEmpty()
    AutoFitTextFieldFontSize(value = stringValue, modifier = modifier) {
        TextField2(
            value = stringValue,
            onValueChange = { newText: String ->
                val newValue = newText.toIntOrNull()
                if (newText.isEmpty() || newValue != null && newValue <= 999 && newValue > 0) {
                    onValueChange(newValue)
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

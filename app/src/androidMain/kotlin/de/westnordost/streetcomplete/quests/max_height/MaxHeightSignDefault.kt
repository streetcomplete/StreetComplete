package de.westnordost.streetcomplete.quests.max_height

import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.tooling.preview.Preview
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.LengthUnit
import de.westnordost.streetcomplete.ui.common.FootInchAppearance
import de.westnordost.streetcomplete.ui.common.LengthInput

@Composable
fun MaxHeightSignDefault(content: @Composable () -> Unit) {
    MaxHeightSignRound(R.drawable.background_maxheight_sign) { content() }
}

@Composable
@Preview(showBackground = true)
fun MaxHeightSignDefaultPreview() {
    CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.h4) {
        MaxHeightSignDefault {
            LengthInput(
                selectedUnit = LengthUnit.FOOT_AND_INCH,
                currentLength = null,
                syncLength = false,
                onLengthChanged = {},
                maxFeetDigits = 2,
                maxMeterDigits = Pair(2, 2),
                footInchAppearance = FootInchAppearance.PRIME
            )
        }
    }
}

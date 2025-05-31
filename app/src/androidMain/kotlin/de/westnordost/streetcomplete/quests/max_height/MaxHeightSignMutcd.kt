package de.westnordost.streetcomplete.quests.max_height

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.data.meta.LengthUnit
import de.westnordost.streetcomplete.ui.common.FootInchAppearance
import de.westnordost.streetcomplete.ui.common.LengthInput
import de.westnordost.streetcomplete.ui.theme.TrafficYellow

@Composable
fun MaxHeightSignMutcd(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.h4) {
        Box(
            modifier = Modifier
                .width(IntrinsicSize.Max)
                .height(IntrinsicSize.Min)
        ) {
            Surface(
                color = TrafficYellow, modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth()
                    .border(
                        width = 2.dp,
                        color = Color.Black,
                        shape = RoundedCornerShape(10.dp)
                    )
            ) {
                content()
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
fun MaxHeightSignMutcdPreview() {
    CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.h4) {
        MaxHeightSignMutcd() {
            LengthInput(
                selectedUnit = LengthUnit.FOOT_AND_INCH,
                currentLength = null,
                syncLength = false,
                onLengthChanged = {},
                maxFeetDigits = 2,
                maxMeterDigits = Pair(2, 2),
                footInchAppearance = FootInchAppearance.UPPERCASE_ABBREVIATION
            )

        }
    }
}

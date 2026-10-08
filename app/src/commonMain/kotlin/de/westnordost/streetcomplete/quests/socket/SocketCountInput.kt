package de.westnordost.streetcomplete.quests.socket

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.ui.common.StepperButton
import de.westnordost.streetcomplete.ui.ktx.pxToDp
import kotlin.math.max

/** Input field for a socket count */
@Composable
fun SocketCountInput(
    count: Int?,
    onCountChange: (Int?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        var inputHeightPx by remember { mutableIntStateOf(0) }

        TextField(
            value = count?.toString() ?: "",
            onValueChange = { text ->
                if (text.isEmpty()) {
                    onCountChange(null)
                } else {
                    text.toIntOrNull()?.let { onCountChange(it) }
                }
            },
            modifier = Modifier
                .width(64.dp)
                .onSizeChanged { inputHeightPx = it.height },
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        )
        StepperButton(
            onIncrease = { onCountChange((count ?: 0) + 1) },
            onDecrease = { onCountChange(if (count == null) 0 else max(0, count - 1)) },
            increaseEnabled = true,
            decreaseEnabled = count == null || count > 0,
            modifier = Modifier
                .width(48.dp)
                .height(inputHeightPx.pxToDp())
        )
    }
}

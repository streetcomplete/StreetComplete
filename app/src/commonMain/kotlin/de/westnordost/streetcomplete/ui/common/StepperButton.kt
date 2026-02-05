package de.westnordost.streetcomplete.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.ic_down_24
import de.westnordost.streetcomplete.resources.ic_up_24
import de.westnordost.streetcomplete.ui.common.button_group.ButtonGroup
import de.westnordost.streetcomplete.ui.common.button_group.ButtonGroupButton
import org.jetbrains.compose.resources.painterResource
import androidx.compose.ui.tooling.preview.Preview

/** A [stepper](https://developer.apple.com/design/human-interface-guidelines/steppers) similar to
 *  the one from Apple's design */
@Composable
fun StepperButton(
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    modifier: Modifier = Modifier,
    increaseEnabled: Boolean = true,
    decreaseEnabled: Boolean = true,
    increaseContent: @Composable RowScope.() -> Unit = {
        Icon(painterResource(Res.drawable.ic_up_24), "+")
    },
    decreaseContent: @Composable RowScope.() -> Unit = {
        Icon(painterResource(Res.drawable.ic_down_24), "-")
    },
    style: ButtonStyle = ButtonStyle.Outlined
) {
    ButtonGroup(
        modifier = modifier,
        style = style
    ) {
        Column(modifier = Modifier
            .width(IntrinsicSize.Min)
            .height(IntrinsicSize.Min)
        ) {
            ButtonGroupButton(
                modifier = Modifier.weight(1f),
                onClick = onIncrease,
                style = style,
                enabled = increaseEnabled,
                contentPadding = PaddingValues(0.dp),
                content = increaseContent
            )
            Divider()
            ButtonGroupButton(
                modifier = Modifier.weight(1f),
                onClick = onDecrease,
                style = style,
                enabled = decreaseEnabled,
                contentPadding = PaddingValues(0.dp),
                content = decreaseContent
            )
        }
    }
}

@Composable @Preview
private fun StepperButtonPreview() {
    var count by remember { mutableIntStateOf(0) }
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        StepperButton(
            onIncrease = { count++ },
            onDecrease = { count-- },
            increaseEnabled = count < 9,
            decreaseEnabled = count > 0,
        )
        Text(count.toString(), fontSize = 44.sp)
    }
}

package de.westnordost.streetcomplete.ui.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.ic_keyboard_24
import org.jetbrains.compose.resources.painterResource

/** Button to switch keyboard between ABC and 123 */
@Composable
fun SwitchKeyboardButton(
    isAbc: Boolean,
    onChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    style: ButtonStyle = ButtonStyle.Default,
) {
    Button2(
        onClick = { onChange(!isAbc) },
        modifier = modifier.size(48.dp),
        style = style,
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(painterResource(Res.drawable.ic_keyboard_24), null)
            Text(if (isAbc) "ABC" else "123", letterSpacing = 0.sp)
        }
    }
}

@Composable @Preview
private fun SwitchKeyboardButtonPreview() {
    var isAbc by remember { mutableStateOf(true) }
    SwitchKeyboardButton(
        isAbc = isAbc,
        onChange = { isAbc = it }
    )
}

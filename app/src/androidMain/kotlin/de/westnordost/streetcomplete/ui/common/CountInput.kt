package de.westnordost.streetcomplete.ui.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.ui.theme.extraLargeInput

/** An outlined text field that accepts only integer input with an illustration that should display
 *  the "unit" (steps, bicycles, ...) of what is input next to it */
@Composable
fun CountInput(
    count: Int?,
    onCountChange: (Int?) -> Unit,
    iconPainter: Painter,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = LocalTextStyle.current,
) {
    Row (
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        OutlinedTextField(
            value = count?.toString().orEmpty(),
            onValueChange = { newText: String ->
                if (newText.isEmpty()) {
                    onCountChange(null)
                } else {
                    newText.toIntOrNull()?.let { onCountChange(it) }
                }
            },
            modifier = Modifier.weight(1f),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            textStyle = textStyle.copy(textAlign = TextAlign.Center),
        )
        Text(text = "×", style = textStyle)
        Image(
            painter = iconPainter,
            contentDescription = null
        )
    }
}

@Composable
@Preview
private fun StepCountFormPreview() {
    val count = remember { mutableStateOf<Int?>(1) }
    CountInput(
        count = count.value,
        onCountChange = { count.value = it },
        iconPainter = painterResource(R.drawable.ic_step),
        textStyle = MaterialTheme.typography.extraLargeInput
    )
}

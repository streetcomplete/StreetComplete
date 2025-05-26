package de.westnordost.streetcomplete.ui.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun CountForm(count: Int, onCountChange: (Int) -> Unit, iconPainter: Painter ) {
    Row (
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround
    ){
        OutlinedTextField(
            value = count.toString(),
            onValueChange = { newText: String ->
                if (newText.all { it.isDigit() }) {
                    onCountChange(newText.toInt())
                }
            },
            modifier = Modifier.weight(1f),
            textStyle = MaterialTheme.typography.h3,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Text(
            text = " × ",
            modifier = Modifier.padding(horizontal = 6.dp),
            style = MaterialTheme.typography.h3
        )
        Image(
            painter = iconPainter,
            contentDescription = null
        )
    }
}


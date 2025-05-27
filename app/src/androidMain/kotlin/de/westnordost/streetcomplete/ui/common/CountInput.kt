package de.westnordost.streetcomplete.ui.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.R

@Composable
fun CountInput(count: Int, onCountChange: (Int) -> Unit, iconPainter: Painter ) {
    Row (
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround
    ){
        OutlinedTextField(
            value = count.toString(),
            onValueChange = { newText: String ->
                newText.toIntOrNull()?.let { onCountChange(it) }
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

@Composable
@Preview(
    name = "Bike Parking Capacity Form",
    showBackground = true,
    widthDp = 360,
    heightDp = 100
)
private fun StepCountFormPreview() {
    val count = remember { mutableIntStateOf(1) }
    CountInput(count = count.intValue, onCountChange = { count.intValue = it }, iconPainter = painterResource(
        R.drawable.ic_step))
}


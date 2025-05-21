package de.westnordost.streetcomplete.quests.step_count

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.westnordost.streetcomplete.R

@Composable
fun StepCountForm(count: Int, onCountChange: (Int) -> Unit) {
    Row (
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ){
        BasicTextField(
            value = count.toString(),
            onValueChange = { newText: String ->
                if (newText.all { it.isDigit() }) {
                    onCountChange(newText.toInt())
                }
            },
            modifier = Modifier
                .width(IntrinsicSize.Min),
            textStyle = LocalTextStyle.current.copy(fontSize = dimensionResource(id = R.dimen.x_large_input).value.sp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Text(
            text = " × ",
            modifier = Modifier.padding(horizontal = 6.dp),
            fontSize = dimensionResource(id = R.dimen.x_large_input).value.sp
        )
        Image(
            painter = painterResource(R.drawable.ic_step),
            contentDescription = null,
            modifier = Modifier
                .size(48.dp)
        )
    }
}

@Composable
@Preview(
    name = "Step Count Form",
    showBackground = true,
    widthDp = 360,
    heightDp = 100
)
private fun StepCountFormPreview() {
    val count = remember { mutableIntStateOf(1) }
    StepCountForm(count = count.intValue, onCountChange = { count.intValue = it })
}

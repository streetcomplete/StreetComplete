package de.westnordost.streetcomplete.quests.step_count

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.CountForm

@Composable
fun StepCountForm(count: Int, onCountChange: (Int) -> Unit) {
    CountForm(count = count, onCountChange = onCountChange, iconResource = R.drawable.ic_step)
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

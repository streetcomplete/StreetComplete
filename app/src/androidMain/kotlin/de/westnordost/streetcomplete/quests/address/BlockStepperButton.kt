package de.westnordost.streetcomplete.quests.address

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.core.text.isDigitsOnly
import de.westnordost.streetcomplete.ui.common.StepperButton

@Composable
fun BlockStepperButton(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val next = remember(value) { stepBlock(value, +1) }
    val prev = remember(value) { stepBlock(value, -1) }

    StepperButton(
        onIncrease = { if (next != null) { onValueChange(next) } },
        onDecrease = { if (prev != null) { onValueChange(prev) } },
        modifier = modifier.alpha(if (next == null && prev == null) 0f else 1f),
        increaseEnabled = next != null,
        decreaseEnabled = prev != null,
    )
}

private fun stepBlock(value: String, step: Int): String? {
    // step block numbers
    if (value.isDigitsOnly()) {
        val result = value.toInt() + step
        if (step < 1) return null
        return result.toString()
    }
    // step block single-letters
    if (value.length == 1) {
        val c = value[0] + step
        val isUpperOrLowercaseLetter =
            c.category == CharCategory.LOWERCASE_LETTER ||
            c.category == CharCategory.UPPERCASE_LETTER
        if (!isUpperOrLowercaseLetter) return null
        return c.toString()
    }
    return null
}

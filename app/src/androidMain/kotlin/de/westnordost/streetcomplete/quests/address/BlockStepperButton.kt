package de.westnordost.streetcomplete.quests.address

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.core.text.isDigitsOnly
import de.westnordost.streetcomplete.ui.common.StepperButton

/** Stepper button to increase/decrease block (numbers). Will increase or decrease block numbers and
 *  also blocks that are single letters */
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
        modifier = modifier,
        increaseEnabled = next != null,
        decreaseEnabled = prev != null,
    )
}

private fun stepBlock(value: String, step: Int): String? {
    // step block numbers
    if (value.isNotEmpty() && value.isDigitsOnly()) {
        val result = value.toInt() + step
        if (result < 1) return null
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

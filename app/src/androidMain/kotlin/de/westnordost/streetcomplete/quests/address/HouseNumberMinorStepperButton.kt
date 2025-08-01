package de.westnordost.streetcomplete.quests.address

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import de.westnordost.streetcomplete.osm.address.StructuredHouseNumber
import de.westnordost.streetcomplete.osm.address.StructuredHouseNumbers
import de.westnordost.streetcomplete.ui.common.StepperButton

/** Stepper button to minor-increase/decrease a house number (e.g. 12c -> 12a or 12c) */
@Composable
fun HouseNumberMinorStepperButton(
    value: StructuredHouseNumbers?,
    onValueChange: (StructuredHouseNumber) -> Unit,
    modifier: Modifier = Modifier,
) {
    val nextMinor = remember(value) { value?.minorStep(+1) }
    val prevMinor = remember(value) { value?.minorStep(-1) }

    StepperButton(
        onIncrease = { if (nextMinor != null) { onValueChange(nextMinor) } },
        onDecrease = { if (prevMinor != null) { onValueChange(prevMinor) } },
        modifier = modifier.alpha(if (nextMinor == null && prevMinor == null) 0f else 1f),
        increaseEnabled = nextMinor != null,
        decreaseEnabled = prevMinor != null,
        increaseContent = { Text(nextMinor?.minor.orEmpty()) },
        decreaseContent = { Text(prevMinor?.minor.orEmpty()) }
    )
}

private val StructuredHouseNumber.minor: String? get() = when (this) {
    is StructuredHouseNumber.Simple -> null
    is StructuredHouseNumber.WithLetter -> letter
    is StructuredHouseNumber.WithNumber -> number2.toString()
}

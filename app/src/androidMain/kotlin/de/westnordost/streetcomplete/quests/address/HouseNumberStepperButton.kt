package de.westnordost.streetcomplete.quests.address

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import de.westnordost.streetcomplete.osm.address.StructuredHouseNumber
import de.westnordost.streetcomplete.osm.address.StructuredHouseNumbers
import de.westnordost.streetcomplete.ui.common.StepperButton

/** Stepper button to increase/decrease a house number (e.g. 12 -> 11 or 13) */
@Composable
fun HouseNumberStepperButton(
    value: StructuredHouseNumbers?,
    onValueChange: (StructuredHouseNumber) -> Unit,
    modifier: Modifier = Modifier,
) {
    val next = remember(value) { value?.step(+1) }
    val prev = remember(value) { value?.step(-1) }

    StepperButton(
        onIncrease = { if (next != null) { onValueChange(next) } },
        onDecrease = { if (prev != null) { onValueChange(prev) } },
        modifier = modifier,
        increaseEnabled = next != null,
        decreaseEnabled = prev != null,
    )
}

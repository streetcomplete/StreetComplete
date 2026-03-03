package de.westnordost.streetcomplete.quests.level

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.ui.common.StepperButton
import de.westnordost.streetcomplete.ui.common.input.DecimalInput
import de.westnordost.streetcomplete.ui.theme.extraLargeInput
import kotlin.math.ceil
import kotlin.math.floor

/** Form to input a level, with convenient stepper button */
@Composable
fun LevelForm(
    level: Double?,
    onLevelChange: (Double?) -> Unit,
    selectableLevels: List<Double>,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        DecimalInput(
            value = level,
            onValueChange = onLevelChange,
            modifier = Modifier.width(128.dp),
            maxIntegerDigits = 3,
            maxFractionDigits = 1,
            isUnsigned = false,
            textStyle = MaterialTheme.typography.extraLargeInput,
        )
        StepperButton(
            onIncrease = {
                onLevelChange(
                    if (level != null) {
                        /* usually +1, but if the selectable levels contain any intermediate floors
                           (e.g. 0.5), step to these instead */
                        val nextInt = floor(level + 1.0)
                        selectableLevels.find { it > level && it < nextInt } ?: nextInt
                    } else {
                        selectableLevels.find { it >= 0 } ?: selectableLevels.firstOrNull() ?: 0.0
                    }
                )
            },
            onDecrease = {
                onLevelChange(
                    if (level != null) {
                        val prevInt = ceil(level - 1.0)
                        selectableLevels.findLast { it < level && it > prevInt } ?: prevInt
                    } else {
                        selectableLevels.findLast { it <= 0 } ?: selectableLevels.firstOrNull() ?: 0.0
                    }
                )
            },
        )
    }
}

package de.westnordost.streetcomplete.ui.common.quest

import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import de.westnordost.streetcomplete.ui.common.CountInput
import de.westnordost.streetcomplete.ui.theme.extraLargeInput

/** A simple quest form that simply allows to input a positive integer. An [icon] is shown next
 *  to the input field to visualize what should be input */
@Composable
fun CountInputQuestForm(
    icon: Painter,
    onClickOk: (Int) -> Unit,
    modifier: Modifier = Modifier,
    otherAnswers: List<Answer> = emptyList()
) {
    var count by rememberSaveable { mutableStateOf<Int?>(null) }

    QuestForm(
        answers = Confirm(
            isComplete = count?.let { it > 0 } == true,
            onClick = { onClickOk(count!!) }
        ),
        modifier = modifier,
        otherAnswers = otherAnswers,
    ) {
        ProvideTextStyle(MaterialTheme.typography.extraLargeInput) {
            CountInput(
                count = count,
                onCountChange = { count = it },
                iconPainter = icon,
            )
        }
    }
}

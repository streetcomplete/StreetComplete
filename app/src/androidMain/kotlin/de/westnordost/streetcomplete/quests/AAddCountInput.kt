package de.westnordost.streetcomplete.quests

import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import de.westnordost.streetcomplete.ui.common.CountInput
import de.westnordost.streetcomplete.ui.common.quest.Confirm
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import de.westnordost.streetcomplete.ui.theme.extraLargeInput
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

abstract class AAddCountInput : AbstractOsmQuestForm<Int>() {

    abstract val icon: DrawableResource

    @Composable
    override fun Content() {
        var count by rememberSaveable { mutableStateOf<Int?>(null) }

        QuestForm(
            answers = Confirm(
                isComplete = count?.let { it > 0 } == true,
                onClick = { applyAnswer(count!!) }
            )
        ) {
            ProvideTextStyle(MaterialTheme.typography.extraLargeInput) {
                CountInput(
                    count = count,
                    onCountChange = { count = it },
                    iconPainter = painterResource(icon),
                )
            }
        }
    }
}

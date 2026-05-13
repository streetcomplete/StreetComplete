package de.westnordost.streetcomplete.quests.traffic_signals_vibrate

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.quest.Answer
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun AddTrafficSignalsVibrationForm(
    onAnswer: (Boolean) -> Unit,
) {
    QuestForm(
        answers = listOf(
            Answer(stringResource(Res.string.quest_generic_hasFeature_no)) { onAnswer(false) },
            Answer(stringResource(Res.string.quest_generic_hasFeature_yes)) { onAnswer(true) }
        ),
        hintImages = listOf(
            when (countryInfo.countryCode) {
                "AU" -> Res.drawable.vibrating_button_illustration_au
                "GB" -> Res.drawable.vibrating_button_illustration_gb
                else -> Res.drawable.vibrating_button_illustration
            }
        )
    )
}

package de.westnordost.streetcomplete.quests.traffic_signals_vibrate

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.ui.common.quest.Answer
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.quest.Answers
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.stringResource

class AddTrafficSignalsVibrationForm : AbstractOsmQuestForm<Boolean>() {

    @Composable
    override fun Content() {
        QuestForm(
            answers = Answers(
                Answer(stringResource(Res.string.quest_generic_hasFeature_no)) { applyAnswer(false) },
                Answer(stringResource(Res.string.quest_generic_hasFeature_yes)) { applyAnswer(true) }
            ),
            hintImages = listOf(getVibratingButtonIllustration(countryInfo.countryCode))
        )
    }
}

private fun getVibratingButtonIllustration(countryCode: String): DrawableResource =
    when (countryCode) {
        "AU" -> Res.drawable.vibrating_button_illustration_au
        "GB" -> Res.drawable.vibrating_button_illustration_gb
        else -> Res.drawable.vibrating_button_illustration
    }

package de.westnordost.streetcomplete.quests.traffic_signals_vibrate

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.vibrating_button_illustration
import de.westnordost.streetcomplete.resources.vibrating_button_illustration_au
import de.westnordost.streetcomplete.resources.vibrating_button_illustration_gb
import org.jetbrains.compose.resources.DrawableResource

class AddTrafficSignalsVibrationForm : AbstractOsmQuestForm<Boolean>() {

    override fun getHintImages(): List<DrawableResource> =
        listOf(getVibratingButtonIllustration(countryInfo.countryCode))

    override val buttonPanelAnswers = listOf(
        AnswerItem(R.string.quest_generic_hasFeature_no) { applyAnswer(false) },
        AnswerItem(R.string.quest_generic_hasFeature_yes) { applyAnswer(true) }
    )
}

private fun getVibratingButtonIllustration(countryCode: String): DrawableResource =
    when (countryCode) {
        "AU" -> Res.drawable.vibrating_button_illustration_au
        "GB" -> Res.drawable.vibrating_button_illustration_gb
        else -> Res.drawable.vibrating_button_illustration
    }

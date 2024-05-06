package de.westnordost.streetcomplete.quests.traffic_signals_vibrate

import android.os.Bundle
import android.view.View
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem

class AddTrafficSignalsVibrationForm : AbstractOsmQuestForm<Boolean>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val illustrationResId = getVibratingButtonIllustrationResId(countryInfo.countryCode)
        setHintImages(listOfNotNull(requireContext().getDrawable(illustrationResId)))
    }

    override val buttonPanelAnswers = listOf(
        AnswerItem(R.string.quest_generic_hasFeature_no) { applyAnswer(false) },
        AnswerItem(R.string.quest_generic_hasFeature_yes) { applyAnswer(true) }
    )
}

private fun getVibratingButtonIllustrationResId(countryCode: String): Int = when (countryCode) {
    "AU" -> R.drawable.vibrating_button_illustration_au
    "GB" -> R.drawable.vibrating_button_illustration_gb
    else -> R.drawable.vibrating_button_illustration
}

package de.westnordost.streetcomplete.quests.traffic_signals_vibrate

import android.os.Bundle
import android.view.View
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.QuestTrafficLightsVibrationBinding
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment
import de.westnordost.streetcomplete.quests.AnswerItem

class AddTrafficSignalsVibrationForm : AbstractQuestAnswerFragment<Boolean>() {

    override val contentLayoutResId = R.layout.quest_traffic_lights_vibration
    private val binding by contentViewBinding(QuestTrafficLightsVibrationBinding::bind)

    override val buttonPanelAnswers = listOf(
        AnswerItem(R.string.quest_generic_hasFeature_no) { applyAnswer(false) },
        AnswerItem(R.string.quest_generic_hasFeature_yes) { applyAnswer(true) }
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // this is necessary because the inflated image view uses the activity context rather than
        // the fragment / layout inflater context' resources to access it's drawable
        binding.buttonIllustrationImageView.setImageDrawable(context?.getDrawable(R.drawable.vibrating_button_illustration))
    }
}

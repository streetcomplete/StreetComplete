package de.westnordost.streetcomplete.quests.traffic_signals_blind

import android.os.Bundle
import android.view.View

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AbstractQuestFormAnswerFragment
import kotlinx.android.synthetic.main.quest_traffic_signals_blind_features.*

class AddTrafficSignalsBlindFeaturesForm : AbstractQuestFormAnswerFragment<TrafficSignalsBlindFeaturesAnswer>() {

    override val contentLayoutResId = R.layout.quest_traffic_signals_blind_features

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        soundSignalsCheckBox.setOnCheckedChangeListener(this::onChecked)
        vibrateCheckBox.setOnCheckedChangeListener(this::onChecked)
        arrowCheckBox.setOnCheckedChangeListener(this::onChecked)
        noneCheckBox.setOnCheckedChangeListener(this::onChecked)
    }

    override fun onClickOk() {
        applyAnswer(TrafficSignalsBlindFeaturesAnswer(
            soundSignalsCheckBox.isChecked,
            vibrateCheckBox.isChecked,
            arrowCheckBox.isChecked
        ))
    }

    override fun isFormComplete() =
        noneCheckBox.isChecked ||
        soundSignalsCheckBox.isChecked ||
        vibrateCheckBox.isChecked ||
        arrowCheckBox.isChecked

    private fun onChecked(buttonView: View, isChecked: Boolean) {
        // none-checkbox is exclusive with everything else
        if (isChecked) {
            if (buttonView.id == R.id.noneCheckBox) {
                soundSignalsCheckBox.isChecked = false
                vibrateCheckBox.isChecked = false
                arrowCheckBox.isChecked = false
            } else {
                noneCheckBox.isChecked = false
            }
        }
        checkIsFormComplete()
    }
}

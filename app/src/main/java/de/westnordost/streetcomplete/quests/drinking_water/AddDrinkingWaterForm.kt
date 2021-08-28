package de.westnordost.streetcomplete.quests.drinking_water

import android.os.Bundle
import android.view.View
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AbstractQuestFormAnswerFragment
import de.westnordost.streetcomplete.quests.drinking_water.DrinkingWater.*
import kotlinx.android.synthetic.main.quest_drinking_water.*

class AddDrinkingWaterForm : AbstractQuestFormAnswerFragment<DrinkingWater>() {

    override val defaultExpanded = false

    override val contentLayoutResId = R.layout.quest_drinking_water

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        radioButtonGroup.setOnCheckedChangeListener { _, _ -> checkIsFormComplete() }
    }

    override fun onClickOk() {
        applyAnswer(when (radioButtonGroup.checkedRadioButtonId) {
            R.id.potable_signed -> POTABLE_SIGNED
            R.id.potable_unsigned -> POTABLE_UNSIGNED
            R.id.not_potable_signed -> NOT_POTABLE_SIGNED
            R.id.not_potable_unsigned -> NOT_POTABLE_UNSIGNED
            else -> throw NullPointerException()
        })
    }

    override fun isFormComplete() = radioButtonGroup.checkedRadioButtonId != -1
}

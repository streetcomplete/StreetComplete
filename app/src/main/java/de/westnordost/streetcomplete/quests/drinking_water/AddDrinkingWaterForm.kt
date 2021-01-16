package de.westnordost.streetcomplete.quests.drinking_water

import android.os.Bundle
import android.view.View
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AbstractQuestFormAnswerFragment
import de.westnordost.streetcomplete.quests.OtherAnswer
import de.westnordost.streetcomplete.quests.drinking_water.DrinkingWater.*
import kotlinx.android.synthetic.main.quest_drinking_water.*

class AddDrinkingWaterForm : AbstractQuestFormAnswerFragment<DrinkingWater>() {

    override val defaultExpanded = false

    override val otherAnswers = listOf(
        OtherAnswer(R.string.quest_drinking_water_sign_is_wrong) { composeNote() },
        OtherAnswer(R.string.quest_drinking_water_seems_drinkable) { applyAnswer(SEEMS_DRINKABLE) }
    )

    override val contentLayoutResId = R.layout.quest_drinking_water

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        radioButtonGroup.setOnCheckedChangeListener { _, _ -> checkIsFormComplete() }
    }

    override fun onClickOk() {
        applyAnswer(when (radioButtonGroup.checkedRadioButtonId) {
            R.id.sign_drinkable -> DRINKABLE_SIGN
            R.id.sign_not_drinkable -> NOT_DRINKABLE_SIGN
            R.id.no_sign_drinkable -> DRINKABLE
            R.id.no_sign_not_drinkable -> NOT_DRINKABLE
            else -> throw NullPointerException()
        })
    }

    override fun isFormComplete() = radioButtonGroup.checkedRadioButtonId != -1
}

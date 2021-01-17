package de.westnordost.streetcomplete.quests.parking_access

import android.os.Bundle
import android.view.View

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AbstractQuestFormAnswerFragment
import de.westnordost.streetcomplete.quests.parking_access.ParkingAccess.*
import kotlinx.android.synthetic.main.quest_parking_access.*

class AddParkingAccessForm : AbstractQuestFormAnswerFragment<ParkingAccess>() {

    override val contentLayoutResId = R.layout.quest_parking_access

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        radioButtonGroup.setOnCheckedChangeListener { _, _ -> checkIsFormComplete() }
    }

    override fun onClickOk() {
        applyAnswer(when (radioButtonGroup.checkedRadioButtonId) {
            R.id.yes            -> YES
            R.id.customers      -> CUSTOMERS
            R.id.private_access -> PRIVATE
            else -> throw NullPointerException()
        })
    }

    override fun isFormComplete() = radioButtonGroup.checkedRadioButtonId != -1
}

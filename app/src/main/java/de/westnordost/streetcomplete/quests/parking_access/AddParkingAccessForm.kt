package de.westnordost.streetcomplete.quests.parking_access

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AbstractQuestFormAnswerFragment
import kotlinx.android.synthetic.main.quest_parking_access.*

class AddParkingAccessForm : AbstractQuestFormAnswerFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        setContentView(R.layout.quest_parking_access)

        radioButtonGroup.setOnCheckedChangeListener { _, _ -> checkIsFormComplete() }

        return view
    }

    override fun onClickOk() {
        val accessValue = when (radioButtonGroup.checkedRadioButtonId) {
            R.id.yes            -> "yes"
            R.id.customers      -> "customers"
            R.id.private_access -> "private"
            else -> null
        }

        val answer = Bundle()
        answer.putString(ACCESS, accessValue)
        applyAnswer(answer)
    }

    override fun isFormComplete() = radioButtonGroup.checkedRadioButtonId != -1

    companion object {
        const val ACCESS = "access"
    }
}

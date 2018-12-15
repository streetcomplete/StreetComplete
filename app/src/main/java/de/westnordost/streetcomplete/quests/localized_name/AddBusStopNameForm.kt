package de.westnordost.streetcomplete.quests.localized_name

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import de.westnordost.streetcomplete.R


class AddBusStopNameForm : AddLocalizedNameForm() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        val contentView = setContentView(R.layout.quest_localizedname)

        addOtherAnswer(R.string.quest_name_answer_noName) { confirmNoName() }
        addOtherAnswer(R.string.quest_streetName_answer_cantType) { showKeyboardInfo() }

        initLocalizedNameAdapter(contentView, savedInstanceState)

        return view
    }

    override fun onClickOk() {
        applyNameAnswer()
    }

    private fun confirmNoName() {
        AlertDialog.Builder(activity!!)
            .setTitle(R.string.quest_name_answer_noName_confirmation_title)
            .setPositiveButton(R.string.quest_name_noName_confirmation_positive) { _, _ ->
                val data = Bundle()
                data.putBoolean(AddLocalizedNameForm.NO_NAME, true)
                applyAnswer(data)
            }
            .setNegativeButton(R.string.quest_generic_confirmation_no, null)
            .show()
    }
}

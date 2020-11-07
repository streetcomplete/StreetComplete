package de.westnordost.streetcomplete.quests.board_type

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AbstractQuestFormAnswerFragment
import de.westnordost.streetcomplete.quests.OtherAnswer
import kotlinx.android.synthetic.main.quest_parking_access.*

class AddBoardTypeForm : AbstractQuestFormAnswerFragment<String>() {

    override val defaultExpanded = false

    override val otherAnswers = listOf(
            OtherAnswer(R.string.quest_board_type_map) { confirmOnMap() }
    )

    private fun confirmOnMap() {
        AlertDialog.Builder(requireContext())
                .setTitle(R.string.quest_board_type_map_title)
                .setMessage(R.string.quest_board_type_map_description)
                .setPositiveButton(R.string.quest_generic_hasFeature_yes) { _, _ -> applyAnswer("map") }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
    }

    override val contentLayoutResId = R.layout.quest_board_type

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        radioButtonGroup.setOnCheckedChangeListener { _, _ -> checkIsFormComplete() }
    }

    override fun onClickOk() {
        applyAnswer(when (radioButtonGroup.checkedRadioButtonId) {
            R.id.history -> "history"
            R.id.geology -> "geology"
            R.id.plants -> "plants"
            R.id.wildlife -> "wildlife"
            R.id.nature -> "nature"
            R.id.public_transport -> "public_transport"
            R.id.notice -> "notice"
            else -> throw NullPointerException()
        })
    }

    override fun isFormComplete() = radioButtonGroup.checkedRadioButtonId != -1
}

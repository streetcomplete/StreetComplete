package de.westnordost.streetcomplete.quests.board_type

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AbstractQuestFormAnswerFragment
import de.westnordost.streetcomplete.quests.OtherAnswer
import de.westnordost.streetcomplete.quests.board_type.BoardType.*
import kotlinx.android.synthetic.main.quest_board_type.*

class AddBoardTypeForm : AbstractQuestFormAnswerFragment<BoardType>() {

    override val defaultExpanded = false

    override val otherAnswers = listOf(
            OtherAnswer(R.string.quest_board_type_map) { confirmOnMap() }
    )

    private fun confirmOnMap() {
        AlertDialog.Builder(requireContext())
                .setTitle(R.string.quest_board_type_map_title)
                .setMessage(R.string.quest_board_type_map_description)
                .setPositiveButton(R.string.quest_generic_hasFeature_yes) { _, _ -> applyAnswer(MAP) }
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
            R.id.history -> HISTORY
            R.id.geology -> GEOLOGY
            R.id.plants -> PLANTS
            R.id.wildlife -> WILDLIFE
            R.id.nature -> NATURE
            R.id.public_transport -> PUBLIC_TRANSPORT
            R.id.notice -> NOTICE
            else -> throw NullPointerException()
        })
    }

    override fun isFormComplete() = radioButtonGroup.checkedRadioButtonId != -1
}

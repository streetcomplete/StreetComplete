package de.westnordost.streetcomplete.quests.board_type

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.QuestBoardTypeBinding
import de.westnordost.streetcomplete.quests.AbstractQuestFormAnswerFragment
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.quests.board_type.BoardType.*

class AddBoardTypeForm : AbstractQuestFormAnswerFragment<BoardType>() {

    override val contentLayoutResId = R.layout.quest_board_type
    private val binding by contentViewBinding(QuestBoardTypeBinding::bind)

    override val defaultExpanded = false

    override val otherAnswers = listOf(
        AnswerItem(R.string.quest_board_type_map) { confirmOnMap() }
    )

    private fun confirmOnMap() {
        AlertDialog.Builder(requireContext())
                .setTitle(R.string.quest_board_type_map_title)
                .setMessage(R.string.quest_board_type_map_description)
                .setPositiveButton(R.string.quest_generic_hasFeature_yes) { _, _ -> applyAnswer(MAP) }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.radioButtonGroup.setOnCheckedChangeListener { _, _ -> checkIsFormComplete() }
    }

    override fun onClickOk() {
        applyAnswer(when (binding.radioButtonGroup.checkedRadioButtonId) {
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

    override fun isFormComplete() = binding.radioButtonGroup.checkedRadioButtonId != -1
}

package de.westnordost.streetcomplete.quests.board_name

import androidx.appcompat.app.AlertDialog
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.QuestLocalizednameBinding
import de.westnordost.streetcomplete.osm.LocalizedName
import de.westnordost.streetcomplete.quests.AAddLocalizedNameForm
import de.westnordost.streetcomplete.quests.AnswerItem

class AddBoardNameForm : AAddLocalizedNameForm<BoardNameAnswer>() {

    override val contentLayoutResId = R.layout.quest_localizedname
    private val binding by contentViewBinding(QuestLocalizednameBinding::bind)

    override val addLanguageButton get() = binding.addLanguageButton
    override val namesList get() = binding.namesList

    override val otherAnswers = listOf(
        AnswerItem(R.string.quest_placeName_no_name_answer) { confirmNoName() },
        AnswerItem(R.string.quest_streetName_answer_cantType) { showKeyboardInfo() }
    )

    override fun onClickOk(names: List<LocalizedName>) {
        applyAnswer(BoardName(names))
    }

    private fun confirmNoName() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.quest_name_answer_noName_confirmation_title)
            .setPositiveButton(R.string.quest_name_noName_confirmation_positive) { _, _ -> applyAnswer(NoBoardName) }
            .setNegativeButton(R.string.quest_generic_confirmation_no, null)
            .show()
    }
}

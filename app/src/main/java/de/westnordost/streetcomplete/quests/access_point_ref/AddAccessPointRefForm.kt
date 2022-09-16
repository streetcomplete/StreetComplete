package de.westnordost.streetcomplete.quests.access_point_ref

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doAfterTextChanged
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.QuestRefBinding
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem

class AddAccessPointRefForm : AbstractOsmQuestForm<AccessPointRefAnswer>() {

    override val contentLayoutResId = R.layout.quest_ref
    private val binding by contentViewBinding(QuestRefBinding::bind)

    override val otherAnswers = listOf(
        AnswerItem(R.string.quest_ref_answer_noRef) { confirmNoRef() }
    )

    private val ref get() = binding.refInput.text?.toString().orEmpty().trim()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.refInput.doAfterTextChanged { checkIsFormComplete() }
    }

    override fun onClickOk() {
        applyAnswer(AccessPointRef(ref))
    }

    private fun confirmNoRef() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.quest_generic_confirmation_title)
            .setPositiveButton(R.string.quest_generic_confirmation_yes) { _, _ -> applyAnswer(NoAccessPointRef) }
            .setNegativeButton(R.string.quest_generic_confirmation_no, null)
            .show()
    }

    override fun isFormComplete() = ref.isNotEmpty()
}

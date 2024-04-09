package de.westnordost.streetcomplete.quests.general_ref

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doAfterTextChanged
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.QuestGeneralRefBinding
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.util.ktx.nonBlankTextOrNull

class AddGeneralRefForm : AbstractOsmQuestForm<GeneralRefAnswer>() {

    override val contentLayoutResId = R.layout.quest_general_ref
    private val binding by contentViewBinding(QuestGeneralRefBinding::bind)

    override val otherAnswers = listOf(
        AnswerItem(R.string.quest_ref_answer_noRef) { confirmNoRef() }
    )

    private val ref get() = binding.refInput.nonBlankTextOrNull

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.refInput.doAfterTextChanged { checkIsFormComplete() }

        if (element.tags.containsKey("guidepost") || element.tags["information"] == "guidepost") {
            binding.tvHint.setText(R.string.quest_guidepostRef_hint)
        }
    }

    override fun onClickOk() {
        applyAnswer(GeneralRef(ref!!))
    }

    private fun confirmNoRef() {
        val ctx = context ?: return
        AlertDialog.Builder(ctx)
            .setTitle(R.string.quest_generic_confirmation_title)
            .setPositiveButton(R.string.quest_generic_confirmation_yes) { _, _ -> applyAnswer(NoVisibleGeneralRef) }
            .setNegativeButton(R.string.quest_generic_confirmation_no, null)
            .show()
    }

    override fun isFormComplete() = ref != null
}

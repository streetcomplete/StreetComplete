package de.westnordost.streetcomplete.quests.step_count

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.QuestStepCountBinding
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.util.ktx.intOrNull

class AddStepCountForm : AbstractOsmQuestForm<Int>() {

    override val contentLayoutResId = R.layout.quest_step_count
    private val binding by contentViewBinding(QuestStepCountBinding::bind)

    private val count get() = binding.countInput.intOrNull ?: 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val resId = arguments?.getInt(ARG_DESCRIPTION) ?: 0
        if (resId > 0) {
            binding.descriptionLabel.setText(resId)
        } else {
            binding.descriptionLabel.visibility = View.GONE
        }

        binding.countInput.doAfterTextChanged { checkIsFormComplete() }
    }

    override fun isFormComplete() = count > 0

    override fun onClickOk() {
        applyAnswer(count)
    }

    companion object {
        private const val ARG_DESCRIPTION = "description"

        fun create(descriptionResId: Int): AddStepCountForm {
            val form = AddStepCountForm()
            form.arguments = bundleOf(ARG_DESCRIPTION to descriptionResId)
            return form
        }
    }
}

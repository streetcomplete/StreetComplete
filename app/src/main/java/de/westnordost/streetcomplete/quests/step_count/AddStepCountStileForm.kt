package de.westnordost.streetcomplete.quests.step_count

import android.os.Bundle
import android.view.View
import androidx.core.widget.doAfterTextChanged
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.QuestStepCountStileBinding
import de.westnordost.streetcomplete.quests.AbstractQuestFormAnswerFragment

class AddStepCountStileForm : AbstractQuestFormAnswerFragment<Int>() {

    override val contentLayoutResId = R.layout.quest_step_count_stile
    private val binding by contentViewBinding(QuestStepCountStileBinding::bind)

    private val count get() = binding.countInput.text?.toString().orEmpty().trim().toIntOrNull() ?: 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.countInput.doAfterTextChanged { checkIsFormComplete() }
    }

    override fun isFormComplete() = count > 0

    override fun onClickOk() {
        applyAnswer(count)
    }
}

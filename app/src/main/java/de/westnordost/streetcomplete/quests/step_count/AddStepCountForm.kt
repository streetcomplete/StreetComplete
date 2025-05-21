package de.westnordost.streetcomplete.quests.step_count

import android.os.Bundle
import android.view.View
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.QuestStepCountBinding
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm

class AddStepCountForm : AbstractOsmQuestForm<Int>() {

    override val contentLayoutResId = R.layout.quest_step_count
    private val binding by contentViewBinding(QuestStepCountBinding::bind)

    private lateinit var count: MutableState<Int>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.questStepCountBase.setContent {
            count = rememberSaveable { mutableIntStateOf(element.tags["step_count"]?.toIntOrNull() ?: 0) }
            StepCountForm(
                count = count.value,
                onCountChange = {
                    count.value = it
                    checkIsFormComplete()
                }
            )
        }
    }

    override fun isFormComplete() = count.value > 0

    override fun onClickOk() {
        applyAnswer(count.value)
    }
}

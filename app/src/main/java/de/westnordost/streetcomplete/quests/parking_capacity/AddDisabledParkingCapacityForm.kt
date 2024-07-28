package de.westnordost.streetcomplete.quests.parking_capacity

import android.os.Bundle
import android.view.View
import androidx.core.widget.doAfterTextChanged
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.QuestDisabledParkingCapacityBinding
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.util.ktx.intOrNull

class AddDisabledParkingCapacityForm : AbstractOsmQuestForm<String>() {

    override val contentLayoutResId = R.layout.quest_disabled_parking_capacity
    private val binding by contentViewBinding(QuestDisabledParkingCapacityBinding::bind)

    private val capacity get() = binding.capacityInput.intOrNull ?: 0

    override val otherAnswers = mutableListOf<AnswerItem>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.capacityInput.doAfterTextChanged { checkIsFormComplete() }
        if (element.tags["capacity:disabled"] != "yes") {
            otherAnswers.add(AnswerItem(R.string.quest_parking_capacity_disabled_answer_yes) {
                applyAnswer(
                    "yes"
                )
            })
        }
    }

    override fun isFormComplete() = binding.capacityInput.intOrNull != null

    override fun onClickOk() {
        if (capacity == 0) {
            applyAnswer("no")
        } else {
            applyAnswer(capacity.toString())
        }
    }
}

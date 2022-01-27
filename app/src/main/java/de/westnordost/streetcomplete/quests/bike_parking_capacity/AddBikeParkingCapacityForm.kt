package de.westnordost.streetcomplete.quests.bike_parking_capacity

import android.os.Bundle
import android.view.View
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.QuestBikeParkingCapacityBinding
import de.westnordost.streetcomplete.quests.AbstractQuestFormAnswerFragment
import de.westnordost.streetcomplete.util.TextChangedWatcher

class AddBikeParkingCapacityForm : AbstractQuestFormAnswerFragment<Int>() {

    override val contentLayoutResId = R.layout.quest_bike_parking_capacity
    private val binding by contentViewBinding(QuestBikeParkingCapacityBinding::bind)

    private val capacity get() = binding.capacityInput.text?.toString().orEmpty().trim().toIntOrNull() ?: 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.capacityInput.addTextChangedListener(TextChangedWatcher { checkIsFormComplete() })
    }

    override fun isFormComplete() = capacity > 0

    override fun onClickOk() {
        applyAnswer(capacity)
    }
}

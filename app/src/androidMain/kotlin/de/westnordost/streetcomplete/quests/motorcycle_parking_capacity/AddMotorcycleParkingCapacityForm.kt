package de.westnordost.streetcomplete.quests.motorcycle_parking_capacity

import android.os.Bundle
import android.view.View
import androidx.core.widget.doAfterTextChanged
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.QuestMotorcycleParkingCapacityBinding
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.util.ktx.intOrNull

class AddMotorcycleParkingCapacityForm : AbstractOsmQuestForm<Int>() {

    override val contentLayoutResId = R.layout.quest_motorcycle_parking_capacity
    private val binding by contentViewBinding(QuestMotorcycleParkingCapacityBinding::bind)

    private val capacity get() = binding.capacityInput.intOrNull ?: 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.capacityInput.doAfterTextChanged { checkIsFormComplete() }
    }

    override fun isFormComplete() = capacity > 0

    override fun onClickOk() {
        applyAnswer(capacity)
    }
}

package de.westnordost.streetcomplete.quests.bike_parking_capacity

import android.os.Bundle
import android.view.View
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.core.widget.doAfterTextChanged
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.QuestBikeParkingCapacityBinding
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.step_count.StepCountForm
import de.westnordost.streetcomplete.util.ktx.intOrNull

class AddBikeParkingCapacityForm : AbstractOsmQuestForm<Int>() {

    override val contentLayoutResId = R.layout.quest_bike_parking_capacity
    private val binding by contentViewBinding(QuestBikeParkingCapacityBinding::bind)

    private lateinit var capacity: MutableState<Int>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.questBikeParkingCapacityBase.setContent {
            capacity = rememberSaveable { mutableIntStateOf(element.tags["capacity"]?.toIntOrNull() ?: 0) }
            BikeParkingCapacityForm(
                count = capacity.value,
                onCountChange = {
                    capacity.value = it
                    checkIsFormComplete()
                }
            )
        }
    }

    override fun isFormComplete() = capacity.value > 0

    override fun onClickOk() {
        applyAnswer(capacity.value)
    }
}

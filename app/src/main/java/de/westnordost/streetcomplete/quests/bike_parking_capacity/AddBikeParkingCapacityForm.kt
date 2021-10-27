package de.westnordost.streetcomplete.quests.bike_parking_capacity

import android.os.Bundle
import android.view.View
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.databinding.QuestBikeParkingCapacityBinding
import de.westnordost.streetcomplete.quests.AbstractQuestFormAnswerFragment
import de.westnordost.streetcomplete.util.TextChangedWatcher

class AddBikeParkingCapacityForm : AbstractQuestFormAnswerFragment<Int>() {

    override val contentLayoutResId = R.layout.quest_bike_parking_capacity
    private val binding by contentViewBinding(QuestBikeParkingCapacityBinding::bind)

    private val capacity get() = binding.capacityInput.text?.toString().orEmpty().trim()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.capacityInput.addTextChangedListener(TextChangedWatcher { checkIsFormComplete() })
    }

    override suspend fun addInitialMapMarkers() {
        getMapData().filter("nodes, ways with amenity = bicycle_parking").forEach {
            putMarker(it, R.drawable.ic_pin_bicycle_parking)
        }
    }

    override fun isFormComplete() = capacity.isNotEmpty() && capacity.toInt() > 0

    override fun onClickOk() {
        applyAnswer(capacity.toInt())
    }
}

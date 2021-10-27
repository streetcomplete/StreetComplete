package de.westnordost.streetcomplete.quests.charging_station_capacity

import android.os.Bundle
import android.view.View
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.databinding.QuestChargingStationCapacityBinding
import de.westnordost.streetcomplete.quests.AbstractQuestFormAnswerFragment
import de.westnordost.streetcomplete.util.TextChangedWatcher

class AddChargingStationCapacityForm : AbstractQuestFormAnswerFragment<Int>() {

    override val contentLayoutResId = R.layout.quest_charging_station_capacity
    private val binding by contentViewBinding(QuestChargingStationCapacityBinding::bind)

    private val capacity get() = binding.capacityInput.text?.toString().orEmpty().trim()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.capacityInput.addTextChangedListener(TextChangedWatcher { checkIsFormComplete() })
    }

    override suspend fun addInitialMapMarkers() {
        getMapData().filter("nodes, ways with amenity = charging_station").forEach {
            putMarker(it, R.drawable.ic_pin_car_charger)
        }
    }

    override fun isFormComplete() = capacity.isNotEmpty() && capacity.toInt() > 0

    override fun onClickOk() {
        applyAnswer(capacity.toInt())
    }
}

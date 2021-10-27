package de.westnordost.streetcomplete.quests.charging_station_operator

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.quests.ANameWithSuggestionsForm

class AddChargingStationOperatorForm : ANameWithSuggestionsForm<String>() {

    override val suggestions: List<String>? get() = countryInfo.chargingStationOperators

    override suspend fun addInitialMapMarkers() {
        getMapData().filter("nodes, ways with amenity = charging_station").forEach {
            putMarker(it, R.drawable.ic_pin_car_charger)
        }
    }

    override fun onClickOk() {
        applyAnswer(name)
    }
}

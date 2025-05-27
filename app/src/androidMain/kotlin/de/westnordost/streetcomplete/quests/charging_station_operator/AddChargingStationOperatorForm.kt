package de.westnordost.streetcomplete.quests.charging_station_operator

import de.westnordost.streetcomplete.quests.ANameWithSuggestionsForm

class AddChargingStationOperatorForm : ANameWithSuggestionsForm<String>() {

    override val suggestions: List<String>? get() = countryInfo.chargingStationOperators

    override fun onClickOk() {
        applyAnswer(name!!)
    }
}

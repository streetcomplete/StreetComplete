package de.westnordost.streetcomplete.quests.atm_operator

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.quests.ANameWithSuggestionsForm

class AddAtmOperatorForm : ANameWithSuggestionsForm<String>() {

    override val suggestions: List<String>? get() = countryInfo.atmOperators

    override suspend fun addInitialMapMarkers() {
        getMapData().filter("nodes with amenity = atm").forEach {
            putMarker(it, R.drawable.ic_pin_money)
        }
    }

    override fun onClickOk() {
        applyAnswer(name)
    }
}
